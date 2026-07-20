package cn.magicvector.common.basic.job;

import cn.magicvector.common.basic.cache.Cache;
import cn.magicvector.common.basic.locks.DistLock;
import cn.magicvector.common.basic.util.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PostConstruct;
import java.text.ParseException;
import java.util.Date;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多实例下单日某「本地墙钟」时至多成功执行一次：{@link DistLock} + Redis 完成标记；触发时点为多个每日 {@code HH:mm}，
 * 与可选的系统启动补偿（当日已过期且未标记的调度槽中，仅对<strong>最近一次</strong>（墙钟最晚）槽执行一次{@link #executeTask()}）。
 * 墙钟与日界线与 {@link java.util.Date} 本地字段语义一致（JVM 默认时区）。
 * <p>
 * 每分钟整点比对当前「本地当日从 00:00 起的分钟偏移」是否与配置时点一致。
 * <p>
 * 分布式锁 key：{@code "Distributed_Singleton_Job_" + getClass().getName()}；
 * Redis 完成标记前缀：{@code "Distributed_Job_Done_Key_Prefix_" + getClass().getName()}，完整 key 为前缀 + yyyy-MM-dd + ":" + HH:mm。
 * <p>
 * 「完成标记」写入 Redis 时 TTL 固定为一天（{@link #DONE_FLAG_TTL_SECONDS}）。
 * <p>
 * 可选最小执行间隔毫秒（构造参数）：同一任务两次实际完成间隔若不足，则跳过 {@link #executeTask()}，
 * 但仍将当前时点槽写入完成标记，避免短时内另一槽（如启动补偿后紧接定时）重复跑；上次完成毫秒时间存
 * {@code "Distributed_Job_Last_Finish_" + getClass().getName()}。
 * <p>
 * 每次即将调用 {@link #executeTask()} 前会轮询 {@link #waitUntil()}；返回 {@code false} 时每轮休眠
 * {@value #WAIT_UNTIL_POLL_MS} 毫秒，直至返回 {@code true} 再执行业务。
 */
public abstract class DistributedSingletonJob {

    private static final int MINUTES_PER_DAY = 24 * 60;

    /** 任务成功后的 Redis 「已完成」标记存活时间（秒），固定一天，减轻 key 堆积。 */
    private static final int DONE_FLAG_TTL_SECONDS = 86400;

    /** 「上次 executeTask 成功结束」毫秒时间戳的 Redis TTL（秒）。 */
    private static final int LAST_FINISH_TTL_SECONDS = 86400 * 14;

    private static final long LOCK_TRY_WAIT_MS = 100L;

    private static final long LOCK_LEASE_MS = 60_000L;

    /** {@link #waitUntil()} 未满足时，每轮休眠毫秒数。 */
    private static final long WAIT_UNTIL_POLL_MS = 2000L;

    /** 每日触发时点字符串：两位数小时 {@code 00}–{@code 23}、两位数分 {@code 00}–{@code 59}，如 {@code "17:30"}（无日历、无秒） */
    private static final Pattern WALL_CLOCK_HM = Pattern.compile("^([01]\\d|2[0-3]):([0-5]\\d)$");

    private static final String MANUAL_TRIGGER_FAIL_RUNNING = "执行失败，任务正在执行...";

    private final Logger log = LoggerFactory.getLogger(getClass());

    private final String distributedLockKey;

    private final String redisDoneFlagKeyPrefix;

    private final String redisLastFinishKey;

    /** 调度槽：当日 00:00 起算，截断到分钟的偏移 [0, 1439]，去重且有序 */
    private final TreeSet<Integer> scheduleMinuteOffsets;

    private final boolean startupCatchupEnabled;

    /** 两次 {@link #executeTask()} 成功结束之间的最短间隔毫秒；{@code 0} 表示不校验。 */
    private final long minGapTimeMillis;

    /** 本实例是否正在执行 {@link #executeTask()}。 */
    private final AtomicBoolean taskRunning = new AtomicBoolean(false);

    /**
     * 与 {@link #DistributedSingletonJob(boolean, long, String...)} 等价，{@code minGapTimeMillis = 0}。
     *
     * @param enableStartupCatchup        启动补偿：在所有「当前时刻已<strong>晚于等于</strong>该时点当日开始时间」且当日尚无完成标记的槽中，
     *                                    仅对<strong>最近一次</strong>（墙钟最晚）槽补偿一次（仍走分布式锁与二次校验）；成功后为当日更早的过期待补偿槽补上完成标记
     * @param dailyRunWallClockTimePoints 每日触发墙上的 {@code HH:mm}（如 {@code "17:00"}）；多字符串落到同一分钟则去重
     */
    protected DistributedSingletonJob(boolean enableStartupCatchup, String... dailyRunWallClockTimePoints) {
        this(enableStartupCatchup, 0L, dailyRunWallClockTimePoints);
    }

    /**
     * @param enableStartupCatchup        启动补偿：在所有「当前时刻已<strong>晚于等于</strong>该时点当日开始时间」且当日尚无完成标记的槽中，
     *                                    仅对<strong>最近一次</strong>（墙钟最晚）槽补偿一次（仍走分布式锁与二次校验）；成功后为当日更早的过期待补偿槽补上完成标记
     * @param minGapTimeMillis           最短执行间隔（毫秒）；若距上次实际完成不足该间隔则跳过本次业务执行，仍将当前时点槽写入完成标记。
     *                                   {@code ≤0} 代表不校验最小逻辑。
     * @param dailyRunWallClockTimePoints 每日触发墙上的 {@code HH:mm}（如 {@code "17:00"}）；多字符串落到同一分钟则去重
     */
    protected DistributedSingletonJob(boolean enableStartupCatchup, long minGapTimeMillis, String... dailyRunWallClockTimePoints) {
        Class<?> impl = getClass();
        this.distributedLockKey = "Distributed_Singleton_Job_" + impl.getName();
        this.redisDoneFlagKeyPrefix = "Distributed_Job_Done_Key_Prefix_" + impl.getName();
        this.redisLastFinishKey = "Distributed_Job_Last_Finish_" + impl.getName();
        this.startupCatchupEnabled = enableStartupCatchup;
        this.minGapTimeMillis = Math.max(0L, minGapTimeMillis);
        if (dailyRunWallClockTimePoints == null || dailyRunWallClockTimePoints.length == 0) {
            throw new IllegalArgumentException("dailyRunWallClockTimePoints must be non-empty");
        }
        TreeSet<Integer> slots = new TreeSet<>();
        for (String raw : dailyRunWallClockTimePoints) {
            if (StringUtils.isBlank(raw)) {
                continue;
            }
            try {
                slots.add(minuteOfDayOffsetFromHm(raw.trim()));
            } catch (ParseException e) {
                throw new IllegalArgumentException(
                        "dailyRunWallClockTimePoints invalid, expect HH:mm (e.g. 17:30), got: " + raw.trim(),
                        e);
            }
        }
        if (slots.isEmpty()) {
            throw new IllegalArgumentException("dailyRunWallClockTimePoints resolved to empty after blank-filter");
        }
        this.scheduleMinuteOffsets = slots;
    }

    @Autowired(required = false)
    @Qualifier("redissonUnFairLock")
    private DistLock distLock;

    @Autowired(required = false)
    @Qualifier("redisCache")
    private Cache redisCache;

    @EventListener(ApplicationReadyEvent.class)
    @Async
    public final void startupCatchUpIfConfigured() {
        if (!startupCatchupEnabled) {
            return;
        }
        if (skipDate()) {
            return;
        }
        Date now = new Date();
        String dayKey = calendarDayIso(now);
        Integer latestMissedMod = null;
        for (Integer mod : scheduleMinuteOffsets) {
            Date slotStartSameDay = slotStartOnSameDay(now, mod);
            if (now.before(slotStartSameDay)) {
                continue;
            }
            if (hasDoneFlag(dayKey, mod)) {
                continue;
            }
            latestMissedMod = mod;
        }
        if (latestMissedMod == null) {
            return;
        }
        log.info("[{}] 检测到最近的任务没有被执行，现在正在补偿...", getJobName());
        runLocked("startup-catchup-" + formatSlotClock(latestMissedMod), latestMissedMod, false);
        log.info("[{}] 补偿任务已经执行完毕，完成时间：{}", getJobName(), DateUtil.toFormatDatetimeStr(new Date()));
        if (hasDoneFlag(dayKey, latestMissedMod)) {
            for (Integer mod : scheduleMinuteOffsets) {
                if (mod >= latestMissedMod) {
                    break;
                }
                Date earlierSlot = slotStartOnSameDay(now, mod);
                if (now.before(earlierSlot)) {
                    continue;
                }
                if (!hasDoneFlag(dayKey, mod)) {
                    setDoneFlag(dayKey, mod, DONE_FLAG_TTL_SECONDS);
                }
            }
        }
    }

    /** 每分钟第 0 秒触发；cron 无时区参数时使用 Spring / JVM 默认时区，与 {@link Date} 墙钟一致。 */
    @Scheduled(cron = "0 * * * * ?")
    public final void onScheduleMinuteTick() {
        int curr = minuteOfDayOffsetNow();
        if (!scheduleMinuteOffsets.contains(curr)) {
            return;
        }
        runLocked("scheduled-" + formatSlotClock(curr), curr, true);
    }

    /**
     * 当前「最近一个应已触发的调度周期」是否已有 Redis 完成标记。
     * <p>
     * 「最近周期」：本地当日配置槽中，墙钟已过点（{@code now >= 槽当日开始}）里分钟偏移最大者；
     * 若今日尚无过点槽，则取<strong>上一本地日历日</strong>配置中墙钟最晚的槽。
     * {@link #skipDate()} 为 {@code true} 时返回 {@code true}；本实例正在执行 {@link #executeTask()} 时返回 {@code false}。
     */
    public final boolean isLatestTaskDone() {
        if (taskRunning.get()) {
            return false;
        }
        if (skipDate()) {
            return true;
        }
        LatestDueSlot slot = resolveLatestDueSlot(new Date());
        return hasDoneFlag(slot.day, slot.minuteOffset);
    }

    /**
     * 获取当前时刻之前、最近一个已过点的调度槽时间（本地墙钟 {@code HH:mm:00}）。
     * <p>
     * 与 {@link #isLatestTaskDone()} 判定的「最近周期」一致：本地当日配置槽中墙钟已过点里分钟偏移最大者；
     * 若今日尚无过点槽，则取上一本地日历日配置中墙钟最晚的槽。
     */
    public final Date getLatestTaskSlot() {
        Date now = new Date();
        LatestDueSlot slot = resolveLatestDueSlot(now);
        if (calendarDayIso(now).equals(slot.day)) {
            return slotStartOnSameDay(now, slot.minuteOffset);
        }
        return slotStartOnSameDay(previousCalendarDay(now), slot.minuteOffset);
    }

    /**
     * 任务是否正在执行（本实例或集群其他节点持分布式锁）。
     * <p>
     * 与 {@link #triggerManually(boolean)}「任务正在执行」判定一致。
     */
    public final boolean isJobRunning() {
        if (taskRunning.get()) {
            return true;
        }
        return distLock != null && distLock.isLocked(distributedLockKey);
    }

    /**
     * 人工触发：仍走分布式锁，直接执行 {@link #executeTask()}；
     * 不校验 {@link #skipDate()}、Redis 完成标记与最小执行间隔。
     * <p>
     * {@code setSlotTaskDone == true} 时，在 {@link #executeTask()} 成功后为
     * 「最近周期」槽（与 {@link #isLatestTaskDone()} 判定一致）写入 Redis 完成标记并更新上次完成时间；
     * {@code false} 时不写入，不影响定时调度槽状态。
     *
     * @param setSlotTaskDone 是否在成功后标记最近周期槽为已完成
     * @return 任务已在执行（本实例或集群持锁）时返回 {@value #MANUAL_TRIGGER_FAIL_RUNNING}；成功返回 {@code null}
     */
    public final String triggerManually(boolean setSlotTaskDone) {
        if (distLock == null) {
            if (!tryBeginRunning()) {
                log.warn("[{}] {}", getJobName(), MANUAL_TRIGGER_FAIL_RUNNING);
                return MANUAL_TRIGGER_FAIL_RUNNING;
            }
            try {
                return runManualTrigger(setSlotTaskDone);
            } finally {
                endRunning();
            }
        }
        String token = distLock.lock(distributedLockKey, 0L, LOCK_LEASE_MS);
        if (StringUtils.isBlank(token)) {
            log.warn("[{}] {}", getJobName(), MANUAL_TRIGGER_FAIL_RUNNING);
            return MANUAL_TRIGGER_FAIL_RUNNING;
        }
        try {
            if (!tryBeginRunning()) {
                log.warn("[{}] {}", getJobName(), MANUAL_TRIGGER_FAIL_RUNNING);
                return MANUAL_TRIGGER_FAIL_RUNNING;
            }
            try {
                return runManualTrigger(setSlotTaskDone);
            } finally {
                endRunning();
            }
        } finally {
            distLock.unlock(distributedLockKey, token);
        }
    }

    /**
     * 是否跳过「调用时刻所属的」本地日历整日（JVM 默认时区）。
     *
     * @return {@code true}：该日不写完成标记也不执行 {@link #executeTask()}（定时触发、启动补偿在持锁前去重）；
     *         每日都跑返回 {@code false}
     */
    protected abstract boolean skipDate();

    /** 具体任务逻辑。 */
    protected abstract void executeTask();

    /**
     * 是否允许执行 {@link #executeTask()}。
     *
     * @return {@code true} 可执行；{@code false} 时框架每 {@value #WAIT_UNTIL_POLL_MS} 毫秒轮询一次，直至为 {@code true}
     */
    protected abstract boolean waitUntil();

    /** 日志等展示用的任务中文名，由子类命名。 */
    protected abstract String getJobName();

    /** 便于 {@link #skipDate()}：当前本地日历日 {@code yyyy-MM-dd} */
    protected final String calendarDayKeyNow() {
        return calendarDayIso(new Date());
    }

    private static int minuteOfDayOffsetFromHm(String trimmed) throws ParseException {
        Matcher m = WALL_CLOCK_HM.matcher(trimmed);
        if (!m.matches()) {
            throw new ParseException(trimmed, 0);
        }
        int h = Integer.parseInt(m.group(1));
        int mi = Integer.parseInt(m.group(2));
        int mod = h * 60 + mi;
        if (mod < 0 || mod >= MINUTES_PER_DAY) {
            throw new ParseException(trimmed, 0);
        }
        return mod;
    }

    @SuppressWarnings("deprecation")
    private static int minuteOfDayOffsetNow() {
        Date d = new Date();
        return d.getHours() * 60 + d.getMinutes();
    }

    @SuppressWarnings("deprecation")
    private static String calendarDayIso(Date when) {
        return String.format(
                "%04d-%02d-%02d", when.getYear() + 1900, when.getMonth() + 1, when.getDate());
    }

    /** 与 {@code when} 同一本地日的 {@code HH:mm}:00（{@link Date} 本地字段语义）。 */
    @SuppressWarnings("deprecation")
    private static Date slotStartOnSameDay(Date when, int minuteOffset) {
        return new Date(
                when.getYear(),
                when.getMonth(),
                when.getDate(),
                minuteOffset / 60,
                minuteOffset % 60,
                0);
    }

    private static String formatSlotClock(int minuteOffset) {
        return String.format("%02d:%02d", minuteOffset / 60, minuteOffset % 60);
    }

    private String runManualTrigger(boolean setSlotTaskDone) {
        try {
            log.info("[{}] 人工触发任务，开始执行...", getJobName());
            blockUntilWaitUntil();
            executeTask();
            if (setSlotTaskDone) {
                markLatestDueSlotDone();
            }
            log.info("[{}] 人工触发执行完毕，完成时间：{}", getJobName(), DateUtil.toFormatDatetimeStr(new Date()));
            return null;
        } catch (Exception ex) {
            log.warn("{} [manual]: {}", getJobName(), ex.toString());
            return null;
        }
    }

    /** 「最近周期」槽：与 {@link #isLatestTaskDone()} 判定一致。 */
    private LatestDueSlot resolveLatestDueSlot(Date now) {
        Integer mod = latestDueMinuteOffsetOnSameDay(now);
        String day;
        if (mod == null) {
            day = calendarDayIso(previousCalendarDay(now));
            mod = scheduleMinuteOffsets.last();
        } else {
            day = calendarDayIso(now);
        }
        return new LatestDueSlot(day, mod);
    }

    private void markLatestDueSlotDone() {
        LatestDueSlot slot = resolveLatestDueSlot(new Date());
        setDoneFlag(slot.day, slot.minuteOffset, DONE_FLAG_TTL_SECONDS);
        setLastRunFinishMillis(System.currentTimeMillis());
        log.debug(
                "{} [manual] marked latest slot done {} {}",
                getJobName(),
                slot.day,
                formatSlotClock(slot.minuteOffset));
    }

    /** 与 {@code when} 同一本地日、已过点的配置槽中，分钟偏移最大者；今日尚无过点槽则 {@code null}。 */
    private Integer latestDueMinuteOffsetOnSameDay(Date when) {
        Integer latest = null;
        for (Integer mod : scheduleMinuteOffsets) {
            if (when.before(slotStartOnSameDay(when, mod))) {
                continue;
            }
            latest = mod;
        }
        return latest;
    }

    @SuppressWarnings("deprecation")
    private static Date previousCalendarDay(Date when) {
        return new Date(when.getYear(), when.getMonth(), when.getDate() - 1);
    }

    /**
     * @param scheduledMinuteTick {@code true}：来自每分钟定时{@code @Scheduled} 命中配置的墙钟时点；{@code false}：启动补偿等
     */
    private void runLocked(String tag, int executionMinuteOffset, boolean scheduledMinuteTick) {
        if (!scheduleMinuteOffsets.contains(executionMinuteOffset)) {
            log.debug("{} [{}] skip, slot {} not in schedule", getJobName(), tag, formatSlotClock(executionMinuteOffset));
            return;
        }
        if (skipDate()) {
            log.debug("{} [{}] skip, skipDate() day {}", getJobName(), tag, calendarDayKeyNow());
            return;
        }
        if (distLock == null) {
            if (!tryBeginRunning()) {
                log.debug("{} [{}] skip, task already running", getJobName(), tag);
                return;
            }
            try {
                executeTaskAndMaybeMark(tag, executionMinuteOffset, scheduledMinuteTick);
            } catch (Exception ex) {
                log.warn("{} [{}]: {}", getJobName(), tag, ex.toString());
            } finally {
                endRunning();
            }
            return;
        }
        String token = distLock.lock(distributedLockKey, LOCK_TRY_WAIT_MS, LOCK_LEASE_MS);
        if (StringUtils.isBlank(token)) {
            return;
        }
        try {
            if (!tryBeginRunning()) {
                log.debug("{} [{}] skip, task already running", getJobName(), tag);
                return;
            }
            try {
                executeTaskAndMaybeMark(tag, executionMinuteOffset, scheduledMinuteTick);
            } catch (Exception ex) {
                log.warn("{} [{}]: {}", getJobName(), tag, ex.toString());
            } finally {
                endRunning();
            }
        } finally {
            distLock.unlock(distributedLockKey, token);
        }
    }

    private void blockUntilWaitUntil() throws InterruptedException {
        while (!waitUntil()) {
            Thread.sleep(WAIT_UNTIL_POLL_MS);
        }
    }

    private boolean tryBeginRunning() {
        return taskRunning.compareAndSet(false, true);
    }

    private void endRunning() {
        taskRunning.set(false);
    }

    private void executeTaskAndMaybeMark(String tag, int executionMinuteOffset, boolean scheduledMinuteTick)
            throws Exception {
        String day = calendarDayIso(new Date());
        if (hasDoneFlag(day, executionMinuteOffset)) {
            log.debug(
                    "{} [{}] skip (double-check), already marked {} {}",
                    getJobName(),
                    tag,
                    day,
                    formatSlotClock(executionMinuteOffset));
            return;
        }
        if (skipDate()) {
            log.debug("{} [{}] skip before executeTask, skipDate() day {}", getJobName(), tag, day);
            return;
        }
        if (shouldDeferForMinGap()) {
            log.info(
                    "[{}] [{}] 在{}秒内已经执行过，执行时间：{} {}",
                    getJobName(),
                    tag,
                    minGapTimeMillis/1000,
                    day,
                    formatSlotClock(executionMinuteOffset));
            setDoneFlag(day, executionMinuteOffset, DONE_FLAG_TTL_SECONDS);
            return;
        }
        if (scheduledMinuteTick) {
            log.info(
                    "[{}] 正常到达任务执行时间「{}」，开始正常执行...",
                    getJobName(),
                    formatSlotClock(executionMinuteOffset));
        }
        blockUntilWaitUntil();
        executeTask();
        setDoneFlag(day, executionMinuteOffset, DONE_FLAG_TTL_SECONDS);
        setLastRunFinishMillis(System.currentTimeMillis());
        log.debug("{} [{}] done {}", getJobName(), tag, formatSlotClock(executionMinuteOffset));
    }

    private boolean hasDoneFlag(String day, int minuteOffset) {
        if (redisCache == null || StringUtils.isBlank(day) || minuteOffset < 0 || minuteOffset >= MINUTES_PER_DAY) {
            return false;
        }
        try {
            String key = buildDoneRedisKey(day, minuteOffset);
            Object o = redisCache.get(key);
            return o != null && StringUtils.isNotBlank(String.valueOf(o));
        } catch (Exception e) {
            return false;
        }
    }

    private void setDoneFlag(String day, int minuteOffset, int ttlSec) {
        if (redisCache != null && StringUtils.isNotBlank(day) && minuteOffset >= 0 && minuteOffset < MINUTES_PER_DAY) {
            redisCache.set(buildDoneRedisKey(day, minuteOffset), "1", ttlSec);
        }
    }

    /**
     * 距上次 {@link #executeTask()} 成功结束的时间是否仍短于构造参数最小间隔。
     * 无 Redis 或无法解析上次完成时间则不拦截。
     */
    private boolean shouldDeferForMinGap() {
        if (minGapTimeMillis <= 0L || redisCache == null || StringUtils.isBlank(redisLastFinishKey)) {
            return false;
        }
        Long last = parseLastRunFinishMillis();
        if (last == null) {
            return false;
        }
        return System.currentTimeMillis() - last < minGapTimeMillis;
    }

    private Long parseLastRunFinishMillis() {
        try {
            Object o = redisCache.get(redisLastFinishKey);
            if (o == null) {
                return null;
            }
            String s = String.valueOf(o);
            if (StringUtils.isBlank(s)) {
                return null;
            }
            return Long.parseLong(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private void setLastRunFinishMillis(long millis) {
        if (redisCache != null && StringUtils.isNotBlank(redisLastFinishKey)) {
            redisCache.set(redisLastFinishKey, String.valueOf(millis), LAST_FINISH_TTL_SECONDS);
        }
    }

    private String buildDoneRedisKey(String day, int minuteOffset) {
        return redisDoneFlagKeyPrefix + day + ":" + formatSlotClock(minuteOffset);
    }

    private static final class LatestDueSlot {
        private final String day;
        private final int minuteOffset;

        private LatestDueSlot(String day, int minuteOffset) {
            this.day = day;
            this.minuteOffset = minuteOffset;
        }
    }
}

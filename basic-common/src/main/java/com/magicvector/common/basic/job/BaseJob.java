package com.magicvector.common.basic.job;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class BaseJob {
    private static final ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * second
     */
    private final Long interval;

    private final String name;

    public BaseJob(String name, Long interval){
        this.name = name;
        this.interval = interval;
    }

    @PostConstruct
    private void init(){

        preInit();

        log.info("[{}] 任务已经启动...", name);

        executorService.submit(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                while(true){
                    try{
                        run();
                    }
                    catch (Exception e){
                        log.error("[{}] 任务发生异常，错误信息：{}", name, e.getMessage());
                    }
                    finally {
                        TimeUnit.SECONDS.sleep(interval);
                    }
                }
            }
        });

    }

    protected abstract <T> T run();

    protected abstract void preInit();
}

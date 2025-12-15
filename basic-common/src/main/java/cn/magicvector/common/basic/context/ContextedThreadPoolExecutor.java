package cn.magicvector.common.basic.context;

import cn.magicvector.common.basic.model.ContextParam;

import java.util.concurrent.*;

/**
 * 带上下文的多线程执行器
 */
public class ContextedThreadPoolExecutor extends ThreadPoolExecutor {

    public ContextedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public ContextedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public ContextedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public ContextedThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }


    private static class RunnableWrapper implements Runnable{
        private ContextParam contextParam;
        private Runnable command;

        private RunnableWrapper(ContextParam contextParam, Runnable trueCommand){
            this.contextParam = contextParam;
            this.command = trueCommand;
        }

        @Override
        public void run() {
            GlobalContext.setContext(contextParam);
            command.run();
        }
    }

    public void execute(Runnable command){
       super.execute(new RunnableWrapper(GlobalContext.getContext(), command));
    }



}

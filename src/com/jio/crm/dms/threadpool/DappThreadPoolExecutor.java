package com.jio.crm.dms.threadpool;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.jio.crm.dms.logger.DappLoggerService;
import com.jio.telco.framework.pool.PoolingManager;

/*
 * Thread pool executor class to execute the process	
 */
public class DappThreadPoolExecutor extends ThreadPoolExecutor
{

	/**
	 * constructor
	 * 
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 * @param handler
	 */
	public DappThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
	}

	/**
	 * constructor
	 * 
	 * @param corePoolSize
	 * @param maximumPoolSize
	 * @param keepAliveTime
	 * @param unit
	 * @param workQueue
	 */
	public DappThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
			BlockingQueue<Runnable> workQueue)
	{
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
	}

	@Override
	protected void afterExecute(Runnable r, Throwable t)
	{
		super.afterExecute(r, t);

		try
		{
			PoolingManager.getPoolingManager().returnObject(r);
		} 
		
		catch (Exception e)
		{
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e,
					"Failed returning object to pool" + e.getMessage(), this.getClass().getName(), "afterExecute")
					.writeExceptionLog();
		}
	}

	/**
	 * @return the live thread pool information in readable format
	 */
	public String getThreadPoolInfo()
	{
		StringBuilder builder = new StringBuilder("");
		builder.append(
				"ActiveCount(The approximate number of threads that are actively executing tasks)              = "
						+ this.getActiveCount() + "\n");
		builder.append(
				"CompletedTaskCount(The approximate total number of tasks that have completed execution)       = "
						+ this.getCompletedTaskCount() + "\n");
		builder.append(
				"LargestPoolSize(The largest number of threads that have ever simultaneously been in the pool) = "
						+ this.getLargestPoolSize() + "\n");
		builder.append(
				"PoolSize(The current number of threads in the pool)                                           = "
						+ this.getPoolSize() + "\n");
		builder.append(
				"TaskCount(The approximate total number of tasks that have ever been scheduled for execution)  = "
						+ this.getTaskCount() + "\n");
		builder.append(
				"Queue Size(The number of elements in this queue)                                              = "
						+ this.getQueue().size() + "\n");
		builder.append("Remaining Capacity(The number of additional elements that this queue can ideally accept\n");
		builder.append(
				"without blocking, or Integer.MAX_VALUE if there is no intrinsic limit)                        = "
						+ this.getQueue().remainingCapacity());
		return builder.toString();
	}

	/**
	 * clears the queue maintained by the thread-pool
	 */
	public void clearQueue()
	{
		this.getQueue().clear();
	}

}

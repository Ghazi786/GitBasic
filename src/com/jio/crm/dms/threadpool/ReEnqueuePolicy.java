package com.jio.crm.dms.threadpool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;




/**
 * This class is custom rejection handler, which will first try to put the rejected task back into 
 * the work queue. If the work queue cannot accept the task (i.e. the queue is full), then it will
 * call the original rejection handler, which will either throw a RejectionException or handles 
 * the rejection according to user-defined logic.
 * 
 * @author Arun2.Maurya
 *
 */
public class ReEnqueuePolicy implements RejectedExecutionHandler {

	private RejectedExecutionHandler rejectedExecutionHandler;

	/**
	 * the rejectedExecutionHandler variable holds the original rejection handler of the thread pool.
	 * @param rejectedExecutionHandler
	 */
	public ReEnqueuePolicy(RejectedExecutionHandler rejectedExecutionHandler) {
		this.rejectedExecutionHandler = rejectedExecutionHandler;
	}

	@Override
	public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {

		// try to put the rejected task back into the work queue
		if (!executor.getQueue().add(r)) {
			rejectedExecutionHandler.rejectedExecution(r, executor);
		}

	}

}

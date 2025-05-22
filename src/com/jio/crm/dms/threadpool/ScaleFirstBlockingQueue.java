package com.jio.crm.dms.threadpool;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 
 * @author Arun2.Maurya
 *
 * @param <E>
 */
public class ScaleFirstBlockingQueue<E> implements BlockingQueue<E> {

	private final BlockingQueue<E> delegate;

	private AtomicInteger currentIdleThreadCount = new AtomicInteger(0);

	/**
	 * 
	 * @param delegate
	 */
	public ScaleFirstBlockingQueue(final BlockingQueue<E> delegate) {
		this.delegate = delegate;
	}

	/**
	 *this offer method  check whether there are any idle worker threads in the pool. If there are any, 
	 * we can try to enqueue the task so one of those threads can pick it up from there.
	 * Otherwise, we should return false from offer method, which will make the thread 
	 * pool to create a new worker thread for the task
	 */
	@Override
	public boolean offer(E e) {
		return currentIdleThreadCount.get() > 0 && this.delegate.offer(e);
	}

	
	
	/**
	 * {@link ThreadPoolExecutor}
	 * Runnable r = timed ? workQueue.poll(keepAliveTime,TimeUnit.NANOSECONDS) : workQueue.take();
	 * 
	 * the timed variable here basically indicates whether the thread pool is currently in a state 
	 * where it can evict some of the idle threads. This can be true either if the thread pool has 
	 * more worker threads than the core pool size or if the thread pool is configured to allow 
	 * evicting idle core threads.
	 * 
	 * First of all, let consider the scenario when timed is false. In this case , the thread will 
	 * call the take() method of the work queue. So it is obvious that any thread that comes into 
	 * the take() method is currently idle and, therefore, we can override the implementation of this
	 *  method in our work queue to increment the idleThreadCount at the beginning. Then we can call 
	 *  the actual take() method, which could result in one of the following two scenarios.
	 *  
	 *  1.If there are no tasks in the queue, the thread will be blocked at this call until a task 
	 *  is available. So it is still in the idle state and our incremented counter value is correct.
	 *  
	 *  2.If there is any task, then it will be returned by the method call. So now this thread is 
	 *  no longer idle and we can decrement our counter at this point.
	 *  
	 */
	@Override
	public E take() throws InterruptedException {

		currentIdleThreadCount.incrementAndGet();
		E take = this.delegate.take();
		currentIdleThreadCount.decrementAndGet();
		return take;
	}

	/**
	 * Then let's consider the other scenario : where timed is true. In this case, the thread will call 
	 * the poll(long timeout, TimeUnit unit) method of the work queue with a timeout value. So here, it 
	 * is also obvious that any thread that comes into the poll() method is currently idle and, therefore, 
	 * we can override the implementation of this method in our work queue to increment the idleThreadCount
	 * at the beginning. Then we can call the actual poll() method, which could result in one of the
	 * following two scenarios.
	 * 
	 * 1.If there are no tasks in the queue, the thread will wait for this call for the provided timeout and 
	 * then return null. By this time, the thread will be timed-out and will be soon evicted from the pool, 
	 * reducing the number of idle threads by 1. So we can decrement our counter at this point.
	 * 
	 * 2.If there is any task, it will be returned by the method call. So now this thread is no longer idle 
	 * and we can decrement our counter at this point, too.
	 */
	@Override
	public E poll(long timeout, TimeUnit unit) throws InterruptedException {
		currentIdleThreadCount.incrementAndGet();
		E poll = this.delegate.poll(timeout, unit);
		currentIdleThreadCount.decrementAndGet();
		return poll;
	}

	/**
	 * this method is used in ReEnqueuePolicy {@link ReEnqueuePolicy} custom rejection handler, which will first 
	 * try to put the rejected task back into the work queue.
	 * 
	 */
	@Override
	public boolean add(E e) {

		return this.delegate.offer(e);
	}

	@Override
	public E poll() {

		return this.delegate.poll();
	}

	@Override
	public E element() {

		return this.delegate.element();
	}

	@Override
	public E peek() {

		return this.delegate.peek();
	}

	@Override
	public E remove() {

		return this.delegate.remove();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {

		return this.delegate.addAll(c);
	}

	@Override
	public void clear() {

		this.delegate.clear();

	}

	@Override
	public boolean containsAll(Collection<?> c) {

		return this.delegate.containsAll(c);
	}

	@Override
	public boolean isEmpty() {

		return this.delegate.isEmpty();
	}

	@Override
	public Iterator<E> iterator() {

		return this.delegate.iterator();
	}

	@Override
	public boolean removeAll(Collection<?> c) {

		return this.delegate.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {

		return this.delegate.retainAll(c);
	}

	@Override
	public int size() {

		return this.delegate.size();
	}

	@Override
	public Object[] toArray() {

		return this.delegate.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {

		return this.delegate.toArray(a);
	}

	@Override
	public boolean contains(Object o) {

		return this.delegate.contains(o);
	}

	@Override
	public int drainTo(Collection<? super E> c) {

		return this.delegate.drainTo(c);
	}

	@Override
	public int drainTo(Collection<? super E> c, int maxElements) {
		return this.delegate.drainTo(c, maxElements);
	}

	@Override
	public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {

		return this.delegate.offer(e, timeout, unit);
	}

	@Override
	public void put(E e) throws InterruptedException {
		this.delegate.put(e);

	}

	@Override
	public int remainingCapacity() {

		return this.delegate.remainingCapacity();
	}

	@Override
	public boolean remove(Object o) {

		return this.delegate.remove(o);
	}

}

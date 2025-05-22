package com.jio.crm.dms.threadpool;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.MonitorInfo;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

import com.jio.crm.dms.logger.DappLoggerService;



/**
 * DappThreadAudit is singleton and load eagrely
 * This class is used to Monitor Thread throughout application
 * @author Arun2.Maurya
 *
 */
public class DappThreadAudit {
	
	public static final DappThreadAudit instance = new DappThreadAudit();
	private String threadDumpPath = "../dumps/Thread_Dump.txt";
	private String liveThreadDumpPath = "../dumps/LiveThread_Dump.txt";
	
	private DappThreadAudit(){
		
	}
	
	public static DappThreadAudit getInstance(){
		return instance;
	}

	/**
	 * Dump Thread Statistics in a file
	 * @return
	 */
	public String dumpThreadStats() {

		StringBuffer output = new StringBuffer();
		ThreadMXBean threadMXbean = ManagementFactory.getThreadMXBean();

		output.append("############################################\n")
			  .append("      Status of CMN Related Threads\n")
			  .append("############################################\n\n")
			  .append("isThreadCpuTimeSupported ----> " + threadMXbean.isThreadCpuTimeSupported() + "\n")
			  .append("isCurrentThreadCpuTimeSupported ----> " + threadMXbean.isCurrentThreadCpuTimeSupported()+"\n");
		
		
		try (   
				OutputStream foutThreadDump = new FileOutputStream(new File(threadDumpPath));
				OutputStream foutLiveThreadDump = new FileOutputStream(new File(liveThreadDumpPath));
				) {
			
			threadMXbean.setThreadContentionMonitoringEnabled(true);
			threadMXbean.setThreadCpuTimeEnabled(true);
			
			output.append("isThreadCpuTimeEnabled ----> " + threadMXbean.isThreadCpuTimeEnabled() + "\n");
			String peakMsg = "Peak Threads created by the application ----> ";
			output.append( + threadMXbean.getPeakThreadCount() + "\n");
			
			foutThreadDump.write((peakMsg + threadMXbean.getPeakThreadCount() + "\n \n")
					.getBytes());
			
			foutLiveThreadDump.write((peakMsg + threadMXbean.getPeakThreadCount() + "\n \n")
					.getBytes());

			output.append("Current live threads ----> " + threadMXbean.getThreadCount() + "\n");
			
			ThreadInfo[] liveThreadInfo = threadMXbean.dumpAllThreads(true, true);
			printThreadInfo(threadMXbean,foutLiveThreadDump,liveThreadInfo);		
			
			ThreadInfo[] threadInfo = threadMXbean.getThreadInfo(threadMXbean.getAllThreadIds());
			printThreadInfo(threadMXbean, foutThreadDump, threadInfo);
			
			
			
			
			
		} catch (FileNotFoundException e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "dumpThreadStats")
			.writeExceptionLog();
		} catch (IOException e) {
			
			DappLoggerService.GENERAL_ERROR_FAILURE_LOG.getExceptionLogBuilder(e, e.getMessage(), this.getClass().getName(), "dumpThreadStats")
			.writeExceptionLog();
		}
		
		return output.toString();
		
	}
	
	private void printThreadInfo(ThreadMXBean threadMXbean,OutputStream fout,ThreadInfo[] liveThreadInfo) throws IOException{
		
		for (ThreadInfo liveThreadInfo1 : liveThreadInfo) {
			long blockedTime = liveThreadInfo1.getBlockedTime();
			long waitedTime = liveThreadInfo1.getWaitedTime();
			long cpuTime = threadMXbean.getThreadCpuTime(liveThreadInfo1.getThreadId());
			long userTime = threadMXbean.getThreadUserTime(liveThreadInfo1.getThreadId());
			String msg = String.format("%s: %d ns cpu time, %d ns user time, blocked for %d ms, waited %d ms",
					liveThreadInfo1.getThreadName(), cpuTime, userTime, blockedTime, waitedTime);

			fout.write(msg.getBytes());
			fout.write(("Number of times this thread has been in BLOCKED state ---> "
					+ liveThreadInfo1.getBlockedCount() + "\n").getBytes());
			
			fout.write(("Number of monitors locked by this thread ----> "
					+ liveThreadInfo1.getLockedMonitors().length + "\n").getBytes());

		
			fout.write(("Current state of this thread -----> " + liveThreadInfo1.getThreadState() + "\n")
					.getBytes());
			if (liveThreadInfo1.getLockedMonitors().length > 0) {
				MonitorInfo[] monitors = liveThreadInfo1.getLockedMonitors();

				for (MonitorInfo m : monitors) {
					String msg2 = String.format(m.getLockedStackFrame().getClassName(), m.getLockedStackFrame()
							.getFileName(), m.getLockedStackFrame().getLineNumber(), m.getLockedStackFrame()
							.getMethodName());
					
					fout.write(("Monitor Information For Live Threads ----> " + msg2 + "\n").getBytes());
				}
				
			}
		}

	}
	
	
}

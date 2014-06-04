package processmanager;


import java.awt.Toolkit;
import java.util.Timer;
import java.util.TimerTask;

public class StaticCounter implements MigratableProcess{
	private volatile boolean suspending;
	Toolkit toolkit;
	Timer timer;
	private String query;
	private int interval;
	int counter = 0;
	
	
	public StaticCounter(String args[]) throws Exception
	{
		if (args.length != 3) {
			System.out.println("from grep: arr len ="+args.length);
			System.out.println("usage: GrepProcess <queryString> <inputFile> <outputFile>");
			throw new Exception("Invalid Arguments");
		}
		
		query = args[0];
		interval = Integer.parseInt(args[1]);
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (!suspending) {
			 System.out.println("About to schedule task.");
			    ReminderBeep(interval);
			    System.out.println("Task scheduled.");
		}
		suspending = false;
	}

	
	public void ReminderBeep(int seconds) {
		    toolkit = Toolkit.getDefaultToolkit();
		    timer = new Timer();
		    timer.schedule(new RemindTask(), seconds * 1000);
		  }
	    
	 class RemindTask extends TimerTask {
		    public void run() {
		      System.out.println("Time's up!"+counter);
		      toolkit.beep();
		      counter ++;
		      //timer.cancel(); //Not necessary because we call System.exit
		      System.exit(0); //Stops the AWT thread (and everything else)
		    }
	  }
	
	@Override
	public void suspend() {
		suspending = true;
		while (suspending);
		
	}

	@Override
	public void migrate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void remove() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString(String[] paramArray) {
		// TODO Auto-generated method stub
		return null;
	}




}

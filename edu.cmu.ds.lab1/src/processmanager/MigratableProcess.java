package processmanager;
import java.lang.Runnable;
import java.lang.Thread;
import java.io.Serializable;


public interface MigratableProcess extends Runnable, Serializable{
	// launch a new 
	public void launch();
	
	//must be called before object is serialized
	// so that it can enter known safe state
	public void suspend(); 
	
	public void migrate();
	
	public void remove();
	
	// this should at least return class name and 
	// a set of params with which the method was called 
	public String toString(String paramArray[]);
}

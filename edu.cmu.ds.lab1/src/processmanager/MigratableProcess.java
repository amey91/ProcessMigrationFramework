package processmanager;
import java.lang.Runnable;
import java.lang.Thread;
import java.io.Serializable;


public abstract class MigratableProcess implements Runnable, Serializable{

	//must be called before object is serialized
	// so that it can enter known safe state
	public abstract void suspend(); 
	
	public abstract void migrate();
	
	public abstract void remove();
	
	
	// this should at least return class name and 
	// a set of params with which the method was called 
	public abstract String toString(String paramArray[]);
	

}

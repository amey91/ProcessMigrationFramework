package transactionalio;

/**
 * When write is called from the class, it:
 * 1. Opens the file
 * 2. Set the file pointer to the desired position where the next write is going to happen
 * 3. Perform the operation
 * 4. Close the file.
 * 
 * To cache file handler, only close it when migrated.
 * 
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileOutputStream extends OutputStream implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8917691371282057967L;
	public String fileName;
	int offset;
	//this field will decide whether I use offset or not.
	public boolean useOffset; 
	
	public TransactionalFileOutputStream(String fileName, boolean b) throws IOException {
		this.fileName = fileName;
		System.out.println(fileName);
		useOffset = b;
		File tmp = new File(fileName);
		tmp.createNewFile();
		
	}
	
	//@referred  to http://www.java-tips.org/java-se-tips/java.io/how-to-use-random-access-file.html
	@Override
	public void write(int arg0) throws IOException {
		try{
			System.out.println("OUTPUT CALLED");
			RandomAccessFile raf = new RandomAccessFile(new File(fileName),"rw");
			raf.seek(offset);
			raf.write(arg0);
			offset = offset + 1;
			raf.close();
			
		}catch(Exception e){
			System.out.println("Transactional stream output error. Exiting. ");
			e.printStackTrace();
			System.exit(-1);
			
		}
		
	}

}

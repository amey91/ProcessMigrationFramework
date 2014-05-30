package transactionalio;

/**
 * When a read is requested from the class, it:
 * 1. open the file
 * 2. seek the required position
 * 3. perform the operation
 * 4. close the file.
 * 
 * To cache file handler, only close it when migrated.
 * 
 */

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;

public class TransactionalFileInputStream extends InputStream implements  Serializable{
	
	private String fileName;
	private static final long serialVersionUID = 1717622015524884281L;
	private long counter;
	/* cache the connection */
	private boolean migrated; /* flag for migration */
	private transient RandomAccessFile fileStream;
	
	public TransactionalFileInputStream(String fileName) {
		// TODO Auto-generated constructor stub
		this.fileName = fileName;
		counter = 0L;
		migrated = false;
		try {
			fileStream = new RandomAccessFile(fileName, "RandomFile");
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * read a byte from specified position.
	 */
	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		int readByte = 0;
		if (migrated) {
			fileStream = new RandomAccessFile(fileName, "rws");
			migrated = false;
		}
		
		fileStream.seek(counter);
		readByte = fileStream.read();
		if (readByte != -1) {
			counter++;
		}

		return readByte;
	}

	public void closeStream() {
		try {
			fileStream.close(); /* close the file handler of last node */
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("Error in closing input file");
			e.printStackTrace();
		} 
	}
	/**
	 * @return the migrated
	 */
	public boolean getMigrated() {
		return migrated;
	}

	/**
	 * @param migrated the migrated to set
	 */
	public void setMigrated(boolean migrated) {
		this.migrated = migrated;
	}
}

package transactionalio;

/**
 * When read is called from the class, it:
 * 1. Opens the file
 * 2. Set the file pointer to the desired position where the next read is going to happen
 * 3. Perform the operation
 * 4. Close the file.
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
	private long counter;	/* cache the connection */
	private boolean migrated;	/* flag for migration */
	private transient RandomAccessFile fileStream;	/* make fileStrem not serializable */
	
	public TransactionalFileInputStream(String fileName) {
		this.fileName = fileName;
		counter = 0L;
		migrated = false;
		try {
			fileStream = new RandomAccessFile(fileName, "rws"); /* read & write and synchronize */
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Read a byte of data from specified position.
	 */
	@Override
	public int read() throws IOException {
		int readByte = 0;
		if (migrated) {
			fileStream = new RandomAccessFile(fileName, "rws");
			migrated = false;
		}
		
		fileStream.seek(counter);	/* Sets the file-pointer offset, at which the next read occurs. */
		readByte = fileStream.read();
		if (readByte != -1) {
			counter++;
		}

		return readByte;
	}

	public void closeStream() {
		try {
			fileStream.close();	/* close the file handler of last node */
		} catch (IOException e) {
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

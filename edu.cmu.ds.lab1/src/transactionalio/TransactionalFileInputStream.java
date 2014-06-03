package transactionalio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
 
 public class TransactionalFileInputStream extends InputStream{
	public String fileName;
	long offset;
	int i;
	char c;
	public TransactionalFileInputStream(String fileName){
		offset=0;
		this.fileName = fileName;
		
	}

 
 	@Override //this method was done while referring to http://www.tutorialspoint.com/java/io/inputstream_read.htm
 	public int read() throws IOException {
 		//create new input stream
 		System.out.println("INPUT CALLED");
 		// @referred to: http://tutorials.jenkov.com/java-io/file.html
 		File dir = new File(fileName);
 		RandomAccessFile raf = new RandomAccessFile(dir, "r");
 		raf.seek(offset);
 		//read till end of stream
 		offset = offset + 1;
 		return raf.readInt();
 	}
 
 
 }
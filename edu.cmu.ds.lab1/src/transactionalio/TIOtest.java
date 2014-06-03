package transactionalio;

import grepprocess.GrepProcess;

public class TIOtest {
	public static void main(String atgs[]) throws Exception{
		String[] arrv = {"other","C:/test.txt","C:/javastuff/output.txt"};
		GrepProcess gp = new GrepProcess(arrv);
		new Thread(gp).start();
		
	}
}

package massim.competition2015.monitor;

import massim.competition2015.monitor.ReportGeneratorExtra;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Vector;

public class MultiReportGenerator {

	public static void main(String args[]) {
		try {
			
			String arg = null;
			boolean report = false;
			boolean achvs = false;
			boolean buys = false;
			for(int i = 0; i<args.length;i++){
				if (args[i].equalsIgnoreCase("-r")){
					report=true;
				}
				if (args[i].equalsIgnoreCase("-a")){
					achvs=true;
				}
				if (args[i].equalsIgnoreCase("-b")){
					buys=true;
				}
				if(args[i].equalsIgnoreCase("-dir")){
					arg = args[++i];
				}
				
					
			}
			if (arg != null){
				generateReports(arg, report, achvs, buys);
			} else {
				System.out.println("ReportGenerator -dir <directrory containing directrories the xmls>");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param buys 
	 * @param achvs 
	 * @param report 
	 * @param arg
	 * @throws java.io.FileNotFoundException
	 */
/*	protected static void generateReports(String arg)
			throws FileNotFoundException {
		
		
		new ReportGenerator(arg);
	}*/
	
	protected static void generateReports(String directory, boolean report, boolean achvs, boolean buys) throws FileNotFoundException {
		File dir = new File(directory);
		if (!dir.isDirectory()){
			throw new FileNotFoundException();
		}
		
		FileFilter filter = new FileFilter() {

			@Override
			public boolean accept(File f) {
				if (f.isDirectory()){
					return true;
				}
				return false;
			}
			
		};
		
		File [] files = dir.listFiles(filter);
		for (int i = 0; i < files.length; i++){
			try {
				if (report || achvs || buys){
					new ReportGeneratorExtra(directory+File.separator+files[i].getName(), report, achvs, buys);
				} else { 
					new ReportGeneratorExtra(directory+File.separator+files[i].getName());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

}

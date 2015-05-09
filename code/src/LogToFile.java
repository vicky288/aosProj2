import java.io.*;
import java.io.File;

public class LogToFile {
	
   public static void CreateWriteFile(String nodeid, String content)
   {
	   
	      File f = null;
	      boolean bool = false;
	      content="\n"+content;
	      //System.out.println("Content is"+content);
	     try
	      {
	         // create new file
	         f = new File(nodeid+".txt");
	         
	         // tries to create new file in the system
	         if(!f.exists())
	         {
	        	 bool = f.createNewFile();
	         }
	       
	         //System.out.println("File created: "+bool);
	                  
	        	FileWriter fw = new FileWriter(f.getAbsoluteFile(),true);
	 			BufferedWriter bw = new BufferedWriter(fw);
	 			bw.newLine();
	 			bw.write(content);
	 			bw.close();         
	      }
	      catch(Exception e)
	      {
	         e.printStackTrace();
	      }
   }
}
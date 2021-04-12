import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.xml.sax.SAXException;


public class CloneRepos {
	
	
	public static void main(String Args[]) throws InvalidRemoteException, TransportException, GitAPIException, IOException, SAXException, ParserConfigurationException, InterruptedException, ExecutionException{
	
		File yourFile = new File("track.txt");
		yourFile.createNewFile();
		FileInputStream fstream2 = new FileInputStream("track.txt");
    	DataInputStream in2 = new DataInputStream(fstream2);
    	BufferedReader br2 = new BufferedReader(new InputStreamReader(in2));
    	String line = null;
    	Set<String> track = new HashSet<String>();
    	
    	while((line = br2.readLine()) != null){
    		track.add(line);
    	}
    	br2.close();
    	in2.close();
    	fstream2.close();
    	
		File dir = new File("Repos Collection");
		File[] directoryListing = dir.listFiles();
		
		for(int j=0;j<directoryListing.length;j++){
			System.out.println("Searching in : "+ directoryListing[j].getName()+"!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			FileInputStream fstream = new FileInputStream(directoryListing[j]);
	    	DataInputStream in = new DataInputStream(fstream);
	    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
			line = null;
			String url = null;
			while ((line = br.readLine()) != null)  
			{  
				if(line.length()>8){
				   if(line.substring(0,8).equals("Repo url")){
					   System.out.println(line);
					   url = line;
					   line = br.readLine();
					  if(track.contains(line.substring(8,line.length())) == false){
						   track.add(line.substring(8,line.length()));
					       FileWriter fwrite = new FileWriter("track.txt",true);
				           BufferedWriter bw = new BufferedWriter(fwrite);
						   bw.write(line.substring(8,line.length()));
						   bw.newLine();
						   bw.close();
						   fwrite.close();
						   System.out.println(line.substring(8,line.length()));
						   ExecutorService executor = Executors.newSingleThreadExecutor();
					       Future<String> future = executor.submit(new Task(url));
		
					        try {
					        	future.get(300, TimeUnit.SECONDS);
					        } catch (TimeoutException e) {
					            future.cancel(true);
					        }
		
					        executor.shutdownNow();
					   }
	
				  }
				}
				
			}
		fstream.close();
		in.close();
		br.close();
		directoryListing[j].delete();
		File Track = new File("track.txt");
		Track.delete();
	}
	System.out.println("The programm has finished");
	}
	}
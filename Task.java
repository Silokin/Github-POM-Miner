import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;
import org.xml.sax.SAXException;


public class Task implements Callable<String> {
	
	String line;
	int randomId;
	static ArrayList<File> Poms = new ArrayList<File>();
	
	public Task(String line){
		this.line = line;
		this.randomId = line.substring(11,line.length()).hashCode();
	}
	
	 public String call() throws Exception {
	

						Runtime rt = Runtime.getRuntime();
						Process pr = rt.exec("git clone "+line.substring(11,line.length())+" TempProject"+File.separator+randomId);
						InputStream is = pr.getErrorStream();
						InputStreamReader isr = new InputStreamReader(is);
			            BufferedReader reader = new BufferedReader(isr);
						String line1;
						while ((line1=reader.readLine()) != null) {System.out.println(line1);}
						pr.waitFor();
						System.out.println("cloned!");
						PomScan("TempProject"+File.separator + randomId);
						addToPoms();
						File rep = new File( "TempProject"+File.separator+randomId+File.separator+".git");
						if(rep.exists()){
							Git git = Git.open( rep );
							List<Ref> list = git.tagList().call();
							for(int i=0;i<list.size();i++){
								System.out.println(list.get(i).toString());
								StringTokenizer st = new StringTokenizer(list.get(i).toString());
								st.nextToken("/");
								st.nextToken("t");
								pr = rt.exec("git checkout "+st.nextToken("="),null,new File("TempProject"+File.separator+randomId));
								InputStream is1 = pr.getErrorStream();
								InputStreamReader isr1 = new InputStreamReader(is1);
					            BufferedReader reader1 = new BufferedReader(isr1);
								String line2;
								while ((line2=reader1.readLine()) != null) {System.out.println(line2);}
								pr.waitFor();
								PomScan("TempProject"+File.separator+ randomId);
								addToPoms();
								
								is1.close();
								isr1.close();
								reader1.close();
							}
						}
						File localPath = new File("TempProject"+File.separator+randomId);
						deleteDirectory(localPath);
						randomId++;
						
						pr.destroy();
						is.close();
						isr.close();
						reader.close();

		 return "Ready";
 }
	//here we scan for all the pom of a repo
		public static void PomScan(String directoryName) throws SAXException, IOException, ParserConfigurationException {
		    
			File directory = new File(directoryName);
	        
			if(directory.exists()){
			    // get all the files from a directory
			    File[] fList = directory.listFiles();
				for (File file : fList) {
					if (file.isFile()) {
						if(file.getName().equalsIgnoreCase("pom.xml")){
							Poms.add(file);
						}
				    } else if (file.isDirectory()) {
				    	PomScan(file.getAbsolutePath());
				    }
				}
			}
		}
		    
		static void addToPoms() throws ParserConfigurationException, SAXException, IOException{
			// String[] ILLEGAL_CHARACTERS = { "/", "\n", "\r", "\t", "\0", "\f", "`", "?", "*", "\\", "<", ">", "|", "\"", ":" };
			String[] ILLEGAL_CHARACTERS = {"<", ">"};
			for(int i = 0; i < Poms.size(); i++) {   
				
				File source = new File(Poms.get(i).getAbsolutePath());
				
				String gId = "";
				String aId = "";
				String version = "";
				
				FileInputStream fstream = new FileInputStream(source);
		    	DataInputStream in = new DataInputStream(fstream);
		    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
				
		    	if (Poms.get(i).isFile() && Poms.get(i).canRead()) {
		    		String line = null;
			    	int done = 0;
		    		while ((line = br.readLine()) != null && done!=3) {
		    		
		    			if(line.contains("<groupId>")){
		    				line = line.substring(line.indexOf(">") + 1);
		    				if(line.length()>9){
		    				gId = line.substring(0,line.length()-10);
		    				done++;
		    				}
		    			}
		    			
		    			if(line.contains("<artifactId>")){
		    				line = line.substring(line.indexOf(">") + 1);
		    				if(line.length()>12){
		    				aId = line.substring(0,line.length()-13);
		    				done++;
		    				}
		    			}
		    			
		    			if(line.contains("<version>")){
		    				line = line.substring(line.indexOf(">") + 1);
		    				if(line.length()>9){
		    				version = line.substring(0,line.length()-10);
		    				done++;
		    				}
		    			}  
		    		}
		    	}
				
				if(version.contains("SNAPSHOT") == false){
				File newFile = new File("Poms");
				File dest = new File(newFile.getAbsolutePath()+File.separator+Poms.get(i).getName().replace("pom.xml", gId+"-"+aId+"-"+version+".pom.xml"));
				boolean flag = false;
				for(int j=0;j<ILLEGAL_CHARACTERS.length;j++){
					if(dest.getAbsolutePath().contains(ILLEGAL_CHARACTERS[j])){
						flag=true;
					}
				}
				if(dest.exists()== false && flag==false)
					Files.copy(source.toPath(), dest.toPath());
				}
				
			br.close(); 
			in.close();
			fstream.close();
			}	
			Poms.clear();
		}
		
		public static boolean deleteDirectory(File directory) {
		    
			if(directory.exists()){
		        File[] files = directory.listFiles();
		        if(null!=files){
		            for(int i=0; i<files.length; i++) {
		                if(files[i].isDirectory()) {
		                    deleteDirectory(files[i]);
		                }
		                else {
		                    files[i].delete();
		                }
		            }
		        }
		    }
		    return(directory.delete());
		}

	}
	 

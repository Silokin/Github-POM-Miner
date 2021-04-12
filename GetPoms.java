import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Parameters:
 * username
 * password
 * startGithubId = 2,000,000
 * range = 1,000,000
 * 
 * File name generation:
 * 
 * filename repos collection = startId.txt
 * filename Repos = startId_Repos.txt
 * 
 *
 */

public class GetPoms { 
	
	static int total;
	static int slept;
	static int rcount;
	static Set<String> repSet = new HashSet<String>();
	
	public static void main(String Args[]){
		//repo is the id of the repo we want to start searching from
		//String Account = Args[0];
		//String Password = Args[1];
		int startId =Integer.parseInt(Args[0]);
		int user = startId;
		// last id is given by user
		int end = Integer.parseInt(Args[1]);
		int count = 0 ;
		String userName;
	    
		//we need to tell our programm when to stop because github never stops expanding
		while(user<= end){
		
			myQtoTxt("https://api.github.com/users?per_page=100&since="+user,"Users"+startId+".txt");//,Account,Password);
			
	    	try{
                //read from Repos
	    		FileInputStream fstream = new FileInputStream("Users"+Args[0]+".txt");
		    	DataInputStream in = new DataInputStream(fstream);
		    	BufferedReader br = new BufferedReader(new InputStreamReader(in));
		    	String line = null;
		    	while ((line = br.readLine()) != null){
		    		if(line.contains("\"login\":")){
		    				userName = line.substring(14,line.length()-2);
		    				line = br.readLine();
		    			    user = Integer.parseInt(line.substring(10,line.length()-1));
		    			    if (user > end) break;
		    			    //again this line helps to make us see how we are progressing live
		    			    System.out.println(user);
		    				slept = 0;
		    				rcount++;
		    				hasPOM(userName,/*Account,Password,*/startId,1);
		    				if(rcount==0){
		    					System.out.println("We slept for : "+slept+" s");
		    					System.out.println("The programm has waited for : "+total+" s");
		    					System.out.println("We are currently past userID "+user);
		    					System.out.println("------------------------------------");
		    				}
		    				for (String s : repSet){
		    					count += 1;
		    				    add_to_Coll(s,userName+" "+user,count,startId);
		    				}
		    				repSet.clear();
		    		}//big if
		    	}//while
   		        br.close(); 
			    in.close();
			    fstream.close();
		    }//try
		    catch (Exception e){//Catch exception if any
		    	System.err.println("Error: " + e.getMessage());		  
		    }//catch
	   }//while
	}//main
	
	//adds a matching repo to the collection
  static void add_to_Coll(String name,String id,int c,int startId){
	  
	  System.out.println("HIT!");
	  
	  try{
		  //write to Repos Collection.txt
		  FileWriter fstream = new FileWriter("Collection\\Repos_Collection"+startId+".txt",true);
          BufferedWriter bw = new BufferedWriter(fstream);
          
          bw.write("Username and ID = "+id);
          bw.newLine();
          
          bw.write("Repo name = "+name);
          bw.newLine();
          
          bw.write("Repo url = https://github.com/"+name+".git");
          bw.newLine();
          
          bw.write("Count = "+ c );
          bw.newLine();
          bw.newLine();
          
          bw.close();
          fstream.close();
	  }//try
	  catch (Exception e){//Catch exception if any
			System.err.println("Error: " + e.getMessage());		  
	  }//catch
  }//add_to_Coll
  
  //checks if a repo is in Java or if it has any POM
  static void hasPOM(String userN,/*String Account,String Password,*/int startId,int page){
	 
	String repName;
	int count = 0; 

	myQtoTxt("https://api.github.com/search/code?q=pom.xml+in:path+user:"+userN+"&per_page=100&page="+page,"Sherlock"+startId+".txt");//,Account,Password);
     
	try{
	    
		//read from Sherlock.txt
	    FileInputStream fstream = new FileInputStream("Sherlock"+startId+".txt");
		DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
		
	    String line = null;
    	while ((line = br.readLine()) != null){
    		if(line.contains("API rate limit exceeded for ")==true){
	    	
		    //System.out.println("Waiting to get Rate Limit. . .");
		  	
		    //we wait for some time to take some rate limit back!
    			try { 
    				if(rcount>0)System.out.println("We counted "+rcount+ " users untill now.");
		  		    Thread.sleep(1000); //1000 milliseconds is one second.
		  		    slept++;
		  		    total++;
		  		    rcount = 0;
    			} 
    			catch(InterruptedException ex) {
    				Thread.currentThread().interrupt();
    			}
	  	  
    			hasPOM(userN,/*Account,Password,*/startId,page);
    		}
    		if(line.contains("\"total_count\":")){
    			count = Integer.parseInt(line.substring(17,line.length()-1));
    		}
    		if(line.contains("\"repository\":")){
    			line = br.readLine();
    			br.readLine();
    			line = br.readLine();
    			repName = line.substring(22,line.length()-2);
    			if(repSet.contains(repName) == false) repSet.add(repName);
    		}
    	}    	
    	if(count/100 > page || (count/100 == page && count % 100 > 0))hasPOM(userN,startId,page+1);
	    br.close();  
	    in.close();
	    fstream.close();
	}//try
	catch (Exception e){//Catch exception if any
		System.err.println("Error: " + e.getMessage());		  
	}//catch

	return;    
  }//Watson
  
  //here we create a client,authenticate ourselves make a query and post the answer in txt after we alter it to be humanly readable
  static void myQtoTxt(String q,String f/*,String Account,String Password*/){
	  
	  try{
      System.setProperty("socksProxyHost", "127.0.0.1");
      System.setProperty("socksProxyPort", "9050");
	  
	  
	  Client client = ClientBuilder.newClient();
	  //client.register(new HttpBasicAuthFilter(Account,Password ));
	  WebTarget webTarget = client.target(q);
	  Invocation.Builder invocationBuilder =webTarget.request(MediaType.APPLICATION_JSON);
	  Response response = invocationBuilder.get();
	  
	  //write to f
	  File fout = new File(f);
	  FileOutputStream fos = new FileOutputStream(fout);
	  BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
	  
	  //make it humanly readable
	  ScriptEngineManager manager = new ScriptEngineManager();
	  ScriptEngine scriptEngine = manager.getEngineByName("JavaScript");
	  scriptEngine.put("jsonString", (response.readEntity(String.class)));
	  scriptEngine.eval("result = JSON.stringify(JSON.parse(jsonString), null, 2)");
	  String prettyPrintedJson = (String) scriptEngine.get("result");
		  
	  bw.write(prettyPrintedJson);

	  bw.close();
	  fos.close();
	  response.close();
	  client.close(); 
	  }//try
	  catch (Exception e){//Catch exception if any
		  myQtoTxt(q,f);		  
	  }//catch
  }//myQtoTxt
}//GetPoms
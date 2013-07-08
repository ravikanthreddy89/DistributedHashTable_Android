package edu.buffalo.cse.cse486586.simpledht;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.Map.Entry;

//import javax.swing.text.html.HTMLDocument.Iterator;


public class Message {

	
	String request;
	String response;
	String update;
	String query;
	String queryhit;
	String movekeys;
	String insert;
	
	public String request_create() {
		 //request= "req"+":"+ SimpleDhtProvider.id+":"+SimpleDhtProvider.portno;
		//request=Functions.arr2str(req);
		 request= "request"+":"+Integer.toString(SimpleDhtProvider.port);
		return request;
	}
	
	public String response_create() {
		response= "response"+":"+ Integer.toString(SimpleDhtProvider.port)+":"+Integer.toString(SimpleDhtProvider.succ_port);
		//response= Functions.arr2str(res);
		return response;
		
	}
	
	public String update_create(int i){
		update= "update"+":"+ Integer.toString(i);
		//update=Functions.arr2str(upd);
		return update;
	}
	
	public String query_create(String query_string) {
		query= "query"+":"+Integer.toString(SimpleDhtProvider.port)+":"+query_string ;
		//query=Functions.arr2str(qry);
		return query;
	}
	
	
	public String queryhit_create(String query_string) {
		
		queryhit= "queryhit";
		
		//send all the values stored in local SimpleDhtProvider..........
		if(query_string.equals("####")){
		 java.util.Iterator<String> itr= SimpleDhtProvider.KeyValues.keySet().iterator();
		 

		 while(itr.hasNext()){
			 //modification........
			 //queryhit=queryhit+":"+SimpleDhtProvider.KeyValues.get(itr.next());
			 
			 
			 
			
			// added : start
			 String file_name= SimpleDhtProvider.KeyValues.get(itr.next());
			 byte[] contents= null;
			 String value=null;
			 
			 FileInputStream fis;
			try {
				fis = SimpleDhtProvider.context.openFileInput(file_name);
				contents=new byte[fis.available()];
				fis.read(contents);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("dude error in the mesage class gdump handling");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("dude error in the mesage class gdump handling");
			}
			 
			 value= new String(contents);
			 queryhit=queryhit+":"+file_name+"*"+value;
			 
			 
			 // added : end
			 
			 
			 
			 
			 
			 
		 }
		}
		//send only the hit value...........
		
		else {
		 
			System.out.println("case when it is single query...");
			String k=null;
			try {
				k = Functions.genHash(query_string);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("dude error in queryhit_creator for one key");
			}
			
			
			//modification........
			//queryhit= queryhit+":"+SimpleDhtProvider.KeyValues.get(k);
			
			
			
			
			// added : start 
						//String file_name= SimpleDhtProvider.KeyValues.get(k);
						String file_name=query_string;
						byte [] val= null;
						try {
							FileInputStream fis= SimpleDhtProvider.context.openFileInput(file_name);
							val=new byte[fis.available()];
							while(fis.read(val)!=-1){};
							String value= new String(val);
							
							System.out.println("creating queryhit :");
							System.out.println("file name or key value :"+file_name);
							System.out.println("contents /value = "+value);
							
							
							queryhit=queryhit+":"+file_name+"*"+value;
							System.out.println("query hit created and = "+queryhit);
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("dude error in queryhit_create method of message class...");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("dude error in byte reading operation : queryhit method , message class");
						}
						
			// added : end
			
						
						
		}
		System.out.println("query hit is : "+ queryhit);
		return queryhit;
	
	}
	
	
	
	public String movekeys_create() {
		System.out.println("movekeys create invoked........");
		
		movekeys="movekeys";
		String pred_id=SimpleDhtProvider.pred;
		
		int hits=0;
		java.util.Iterator<String> it = SimpleDhtProvider.KeyValues.keySet().iterator();
		while(it.hasNext()){
			String key=it.next();
			if(Functions.isLessThan(key, pred_id)) {
			      hits++;	
				}
		}// end of while loop........
 
		if(hits!=0){
			java.util.Iterator<String> it2 = SimpleDhtProvider.KeyValues.keySet().iterator();
			while(it2.hasNext()){
				String key=it2.next();
				if(Functions.isLessThan(key, pred_id)) {
				 movekeys=movekeys+":"+key+"---"+SimpleDhtProvider.KeyValues.get(key);
				// SimpleDhtProvider.KeyValues.remove(key);
				 
				}
				}
		
			Enumeration<String> keys= SimpleDhtProvider.KeyValues.keys();
			
			while(keys.hasMoreElements()){
				String key=keys.nextElement();
				if(Functions.isLessThan(key, pred_id)) {
				 //movekeys=movekeys+":"+key+"---"+SimpleDhtProvider.KeyValues.get(key);
				 SimpleDhtProvider.KeyValues.remove(key);
				 
				
			}
			
			}
		}
		else movekeys=movekeys+":"+"####";
			
		return movekeys;
	}
	
	public String insert_create(String key, String value){
		insert= "insert"+":"+ key+":"+ value;
		//insert=Functions.arr2str(insrt);
		return insert;
		
	}
	
	
}

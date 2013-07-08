package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

import android.content.Context;
import android.database.MatrixCursor;

//import javax.xml.crypto.Data;



public class Functions {

	static String genHash(String input) throws NoSuchAlgorithmException {
		MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
		byte[] sha1Hash = sha1.digest(input.getBytes());
		Formatter formatter = new Formatter();
		for (byte b : sha1Hash) {
		formatter.format("%02x", b);
		}
		return formatter.toString();
		}
	
		static byte[] int2byte(int i){
			byte[] result= new byte[2];
			result[0]= (byte)((i>>8)&255);
			result[1]=(byte)((i)&255);
			return result;
		}
		
	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		                           /////////// request handler////////////////////////
		
		
		static void request_handler(String[] incoming, String incoming_message) throws NoSuchAlgorithmException{
			
			
			System.out.println("request handler invoked......");
			System.out.println("message type :"+incoming[0]);
			System.out.println("remote portno :"+incoming[1]);
			
			int remote_portno=Integer.parseInt(incoming[1]);	
			String remote_id=null;
			
			if(remote_portno==11108) remote_id="5554";
			else if(remote_portno==11112) remote_id="5556";
			else if(remote_portno==11116) remote_id="5558";
			
			String incoming_id= Functions.genHash(remote_id);
			
		
			// case when there is only one node and a new node joins...........
			if(SimpleDhtProvider.id.equals(Functions.genHash("5554")) && SimpleDhtProvider.succ.equals(SimpleDhtProvider.id)){
				System.out.println("case when there is only one node");
				Message response= new Message();
				forward(response.response_create(), remote_portno);
				System.out.println("forwarding response message.......");
				Message update= new Message();
				forward(update.update_create(remote_portno), SimpleDhtProvider.succ_port);
				System.out.println("forwarding update message......");
				
				// added because update message wont update the predecessor port no in the 11108 node
				SimpleDhtProvider.succ_port=remote_portno;
				SimpleDhtProvider.succ=Functions.genHash(remote_id);
				SimpleDhtProvider.pred_port=remote_portno;
				SimpleDhtProvider.pred=Functions.genHash(remote_id);
				
			}
		
			// if you are last node...............
			else if(isLessThan(SimpleDhtProvider.succ,SimpleDhtProvider.id)){

				System.out.println("if you are last noed..........");
				if(isLessThan(incoming_id, SimpleDhtProvider.id)){
					if(isLessThan(incoming_id, SimpleDhtProvider.succ)){
						Message response = new Message();
						forward(response.response_create(), remote_portno);
						Message update= new Message();
						forward(update.update_create(remote_portno), SimpleDhtProvider.succ_port);
						SimpleDhtProvider.succ_port=remote_portno;
						SimpleDhtProvider.succ=Functions.genHash(remote_id);
					}
					else forward(incoming_message, SimpleDhtProvider.pred_port);
				}
				else {
					Message response = new Message();
					forward(response.response_create(), remote_portno);
					Message update= new Message();
					forward(update.update_create(remote_portno), SimpleDhtProvider.succ_port);
					
					SimpleDhtProvider.succ_port=remote_portno;
					SimpleDhtProvider.succ=Functions.genHash(remote_id);
				}
			}
			// end of last node handling...........................
			
			// if you are not last node ...........
			else {
				
				System.out.println("if you are not last node.....");
				if(isLessThan(incoming_id, SimpleDhtProvider.id)) forward(incoming_message, SimpleDhtProvider.pred_port);
				else if(isGreaterThan(incoming_id, SimpleDhtProvider.succ)) forward(incoming_message, SimpleDhtProvider.succ_port);
				else if(isLessThan(SimpleDhtProvider.id, incoming_id) && isGreaterThan(SimpleDhtProvider.succ, incoming_id)){
					Message response = new Message();
					forward(response.response_create(), remote_portno);
					Message update= new Message();
					forward(update.update_create(remote_portno), SimpleDhtProvider.succ_port);
					
					SimpleDhtProvider.succ_port=remote_portno;
					SimpleDhtProvider.succ=Functions.genHash(remote_id);
				}
			}
		
			}
			
			
			
		//////////////////////////////////////////////////////////////////////////////////////////////////////////
		//////////               response handler.                   /////////////////////////////////
		static void response_handler(String[] incoming){
			System.out.println("response handler invoked.........");
			int pred_port;
			int succ_port;
			
			pred_port=Integer.parseInt(incoming[1]);
			succ_port=Integer.parseInt(incoming[2]);
			
			String pred__id=null;
			String succ__id=null;
			
			if(pred_port==11108) pred__id="5554";
			else if(pred_port==11112) pred__id="5556";
			else if(pred_port==11116) pred__id="5558";
			
			if(succ_port==11108) succ__id="5554";
			else if(succ_port==11112) succ__id="5556";
			else if(succ_port==11116) succ__id="5558";
 			
			
			
			SimpleDhtProvider.pred_port=pred_port;
			System.out.println("predecessor updated");
			SimpleDhtProvider.succ_port=succ_port;
			System.out.println("succesor updated");
			
			try {
				SimpleDhtProvider.pred=genHash(pred__id);
				SimpleDhtProvider.succ=genHash(succ__id);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("error in response message handling....");
			}
			
		}
				
		static void update_handler(String[] incoming){
			System.out.println("update handler invoked..........");
			int pred_port;
			
			pred_port=Integer.parseInt(incoming[1]);
			
			String pred__id=null;
			
			if(pred_port==11108) pred__id="5554";
			else if(pred_port==11112) pred__id="5556";
			else if(pred_port==11116) pred__id="5558";
			
			
			
			SimpleDhtProvider.pred_port=pred_port;
			System.out.println("predecessor updated..........");
			try {
				SimpleDhtProvider.pred=genHash(pred__id);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("error in update message handler.......");
			}
			Message m= new Message();
			forward(m.movekeys_create(), pred_port);
			
		}
		
		
		
		static void query_handler(String[] incoming,String incoming_message){
			System.out.println("query handler invoked............");
			int remote_portno=0;
			remote_portno=Integer.parseInt(incoming[1]);
			
			String querystring=null;
			
			//case when there it is gdump message......
			
			if(incoming[2].equals("####")){
				
				querystring=incoming[2];
				//if you are not the one who queried......
				
				if(remote_portno != SimpleDhtProvider.port){
					
					Message m= new Message();
					String outgoing= incoming_message+":"+m.queryhit_create(querystring).substring(9);
					//forward(m.queryhit_create(querystring), remote_portno);
					forward(outgoing, SimpleDhtProvider.succ_port);
				}
				// you are the one who queried........
				else{
				
					String [] clns= {"key","value"};
					SimpleDhtProvider.result= new MatrixCursor(clns);
					
					
					int hits= incoming.length-1;
					
				   // add the incoming queryhits to result cursor..............
					for(int i=3; i<=hits; i++){
						String [] key_val= incoming[i].split("\\*");
						SimpleDhtProvider.result.addRow(key_val);
					}
					
					//add the local values to result curosr................
					if(SimpleDhtProvider.KeyValues.size()!=0){
						java.util.Iterator<String> it = SimpleDhtProvider.KeyValues.keySet().iterator();
						
						while(it.hasNext()){
							String key= it.next();
							System.out.println("Key : "+key);
							
							//modification....
							//String[] value= SimpleDhtProvider.KeyValues.get(key).split("\\*");
							
							
							
							
							
							// added : start 
							String file_name=SimpleDhtProvider.KeyValues.get(key);
							Context c=SimpleDhtProvider.context;
							FileInputStream fis;
							byte [] val = null;
							try {
								fis = c.openFileInput(file_name);
								val= new byte[fis.available()];
								while(fis.read(val)!=-1);
							} catch (FileNotFoundException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
								System.out.println("error in opening file for reading");
							} catch (IOException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
								System.out.println("error while reading the file....");
							}
							
							
							String [] value= {file_name , new String (val)};
							
							// added : end
							
							
							
							
							
							//////////////////////////////////////////////////////////////////////////////////////////////////////
							SimpleDhtProvider.result.addRow(value);
							System.out.println("Value :"+value);
							System.out.println("==============================================");
							
						}// end of while loop..
						
					}// end o fif case

					SimpleDhtProvider.arrived=false;
				}
				
			}// end of gdump message handling........ 
			
			//case when it is single query message................
			else {
				querystring= incoming[2];
				String key=null;
				try {
					key=Functions.genHash(querystring);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("some error in single query handling........");
				}
				
			if(SimpleDhtProvider.KeyValues.containsKey(key)) {
				Message m= new Message();
				Functions.forward(m.queryhit_create(incoming[2]), remote_portno);
			}else{
			    Functions.forward(incoming_message, SimpleDhtProvider.succ_port);	
			}
			}// end of single query handling...............
			
		}// end of query handler.......
		
		
		
		static void queryhit_handler(String[] incoming){
			System.out.println("query hit invoked...........");
			int hits=0;
			hits=(incoming.length-1);
			
			System.out.println("hits in the query hit are :"+hits);
			
			for(int i=1; i<=hits; i++){
				String[] key_value= incoming[i].split("\\*");
				String key=key_value[0];
				String value=key_value[1];
				System.out.println("key : "+key );
				System.out.println("value"+value);
				System.out.println("=========================================");
				String [] cols= {"key", "value"};
				SimpleDhtProvider.result = new MatrixCursor(cols);
				SimpleDhtProvider.result.addRow(key_value);
				SimpleDhtProvider.arrived=false;
			}
			
		}
				
		static void movekeys_handler(String[] incoming){
			System.out.println("move keys handler invoked............");
			if(incoming[1].equals("####")){
				
			}else {
			
				int hits=(incoming.length-1);
				for(int i=1; i<=hits; i++){
					String[] pair=incoming[i].split("---");
					SimpleDhtProvider.KeyValues.put(pair[0], pair[1]);
				}
			}
			
			
			
		}
				
		static void insert_handler(String [] incoming, String incoming_message){
			System.out.println("insert handler invoked..............");
			
			String key=null;
			try {
				key = Functions.genHash(incoming[1]);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("error in the insert handler hash generation..........");
			}
			//modification.......
			//String value=incoming[1]+"*"+incoming[2];
			String value=incoming[1];
			
			
			//case when there is only one node............
			if((SimpleDhtProvider.port==11108) && SimpleDhtProvider.id.equals(SimpleDhtProvider.succ)){
				//modification....
				//SimpleDhtProvider.KeyValues.put(key, value);
				
				
				
				// added : start
				SimpleDhtProvider.KeyValues.put(key, value);
				Context c= SimpleDhtProvider.context;
				try {
					FileOutputStream fos= c.openFileOutput(value, Context.MODE_WORLD_WRITEABLE);
					fos.write(incoming[2].getBytes());
					System.out.println("value written to the file :"+incoming[2]);
					fos.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("dude there is error in file saving in insert handler....");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("dude error in writing value bytes to the file.!!!!");
				}
				// added : end
				System.out.println("stored in the local SimpleDhtProvider......");
			} 
			//if you are first node ........
			else if(isLessThan(SimpleDhtProvider.id, SimpleDhtProvider.pred)){
				if(isLessThan(key, SimpleDhtProvider.id)){
					//modification....
					//SimpleDhtProvider.KeyValues.put(key, value);
					
					
					// added : start
					SimpleDhtProvider.KeyValues.put(key, value);
					Context c= SimpleDhtProvider.context;
					try {
						FileOutputStream fos= c.openFileOutput(value, Context.MODE_WORLD_WRITEABLE);
						fos.write(incoming[2].getBytes());
						System.out.println("value written to the file :"+incoming[2]);
						fos.close();
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						System.out.println("dude there is error in file saving in insert handler....");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						System.out.println("dude error in writing value bytes to the file.!!!!");
					}
					// added : end
					
					
					System.out.println("stored in the local SimpleDhtProvider......");
				}
				else {
					if(isGreaterThan(key, SimpleDhtProvider.pred)){
						//modification....
						//SimpleDhtProvider.KeyValues.put(key, value);
						
						
						// added : start
						SimpleDhtProvider.KeyValues.put(key, value);
						Context c= SimpleDhtProvider.context;
						try {
							FileOutputStream fos= c.openFileOutput(value, Context.MODE_WORLD_WRITEABLE);
							fos.write(incoming[2].getBytes());
							System.out.println("value written to the file :"+incoming[2]);
							fos.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("dude there is error in file saving in insert handler....");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("dude error in writing value bytes to the file.!!!!");
						}
						// added : end
						System.out.println("stored in the local SimpleDhtProvider......");
					}
					else forward(incoming_message, SimpleDhtProvider.succ_port);
				}
			}
			
			// if you are not the first node.........
			else {
				if(isLessThan(key, SimpleDhtProvider.id)){
					if(isGreaterThan(key, SimpleDhtProvider.pred)){
						//Modification...
						//SimpleDhtProvider.KeyValues.put(key, value);
						
						// added : start
						SimpleDhtProvider.KeyValues.put(key, value);
						Context c= SimpleDhtProvider.context;
						try {
							FileOutputStream fos= c.openFileOutput(value, Context.MODE_WORLD_WRITEABLE);
							fos.write(incoming[2].getBytes());
							System.out.println("value written to the file :"+incoming[2]);
							fos.close();
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("dude there is error in file saving in insert handler....");
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
							System.out.println("dude error in writing value bytes to the file.!!!!");
						}
						// added : end
						System.out.println("stored in the local SimpleDhtProvider......");
					}
					else forward(incoming_message, SimpleDhtProvider.pred_port);
				}
				else {
					forward(incoming_message, SimpleDhtProvider.succ_port);
				}
			}
					
		}
			
		
		// helper functions.........................................
		static boolean isLessThan(String a, String b){
			return a.compareTo(b)<0;
		};
		
		 //forward function which forwards the messages passed to it............
	    public static void forward(final String message, final int portno) {
			// TODO Auto-generated method stub
			new Thread(new Runnable() {
				public void run(){
					Socket remote;
					try {
						remote= new Socket("10.0.2.2", portno);
						PrintWriter out= new PrintWriter(new BufferedWriter(new OutputStreamWriter(remote.getOutputStream())));
						out.println(message);
						out.close();
						remote.close();
					}catch (Exception e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						System.out.println("dude there is error in forwarding the message....");
					}
				}
			}).start();
	    	
		}// end of forward method.......
		
		static boolean isGreaterThan(String a, String b){
			return a.compareTo(b)>0;
		}
		
		static String arr2str(String [] incoming){
			int length= incoming.length;
			
			String out="#";
			
			for(int i=0; i<length ; i++){
				out= out+":"+incoming[i];
			}
			return out;
		}
}

package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Formatter;
import java.io.DataInputStream;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.telephony.TelephonyManager;

public class SimpleDhtProvider extends ContentProvider {

	// initial variables for usage by content provider.........
	
	
	
	public static  int port;
	public static int succ_port;
	public static int pred_port;
	public static String portStr;
	public static String pred;
	public static String succ;
	public static String id;
	static Context context;	
	public static MatrixCursor result;
	public static java.util.Hashtable<String , String> KeyValues= new java.util.Hashtable<String , String >();
	public static boolean arrived=true;
	//ArrayList<Integer> node_list= new ArrayList<Integer>();
	TelephonyManager tel;
	
	@Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
		context=this.getContext();
		System.out.println("on create of provider executing.....");
        tel=(TelephonyManager)getContext().getSystemService(Context.TELEPHONY_SERVICE);
        portStr=tel.getLine1Number().substring(tel.getLine1Number().length()-4);
        
        System.out.println("portStr = "+portStr);
        if(portStr.equals("5554")) port=11108;
        else if(portStr.equals("5556")) port=11112;
        else if(portStr.equals("5558")) port=11116;
        
        System.out.println("port no ="+port);
        try{
        	id=genHash(portStr);
        	System.out.println("nod id "+id);
        }catch(Exception e){
        	System.out.println("Dude there is problem in hash generation !! check it out....");
        }
        
        //if you are bootstrap node.............
        if(port==11108){
        	try{
        		succ_port=port;
        		succ=genHash(portStr);
        		pred_port=port;
        		pred=genHash(portStr);
        		System.out.println("succ and pred updated...");
        	}catch(Exception e){
        		System.out.println("dude problem in hash generation of succ and pred...");
        	}
        }
        
        // if you are not bootstrap node.....
        else {
        	
        	
        	try {
				id=genHash(portStr);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				System.out.println("dude error in hash generation for other nodes....");
			}
        }
        new Server().start();
        System.out.println("server started");
        
        if(port!=11108){
        	Message req= new Message();
        	System.out.println("request message created.....and");
        	forward(req.request_create(), 11108);
        	System.out.println("request message forwarded...");
        }
		return false;
    }
	
	
	
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
    	System.out.println("insert request rxd.");
    	String key= values.getAsString("key");
    	String value= values.getAsString("value");
    	String outgoing= "insert:"+key+":"+value;
    	System.out.println("insert message constructed and is = "+outgoing);
    	forward(outgoing, port);
    	System.out.println("forwarded insert message to "+port);
    	return uri;
    }

    

	

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // TODO Auto-generated method stub
		String key=null;
		

		// ldump case...........
		if(selection.equals("ldump")){
			String [] coloumns= {"key", "value"};
			result= new MatrixCursor(coloumns);
			
			Enumeration<String> en= KeyValues.keys();
			
			while(en.hasMoreElements()){
				
				
				
				
				
				// added : start //
				byte[] contents=null;
				String file_name=KeyValues.get(en.nextElement());
				FileInputStream fis;
				try {
					fis = context.openFileInput(file_name);
					contents= new byte[fis.available()];
					fis.read(contents);
					fis.close();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("dude error in the provider class ldump handling...");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("dude error in the provider class ldump handling...");
				}
				
				// added : end
				String [] str= {file_name, new String (contents)};
				
				
				
				
				
				//String[] str=KeyValues.get(en.nextElement()).split("\\*");
				result.addRow(str);
			}
			
			
		}
		
		//gdump case..............
		
		else if(selection.equals("gdump")){
			
			key="####";
			
			Message m= new Message();
			Functions.forward(m.query_create(key), succ_port);
			while(arrived){
				/*try {
					//Thread.sleep(300);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("Thread sleep failed........");
				}*/
			}
			arrived=true;
		}
		
		
		//other cases..............
		else {
			try {
				key = Functions.genHash( selection);
			} catch (NoSuchAlgorithmException e1) {
				// TODO Auto-generated catch block
				//e1.printStackTrace();
				System.out.println("dude there is fucking error in query key hash generation....");
			}
			
			if(KeyValues.containsKey(key)){
			    System.out.println("found in the local database....");
				String [] coloumns= {"key", "value"};
				result= new MatrixCursor(coloumns);
				
				//modification.........
				
				//added : start 
				byte [] val= null;
				
				FileInputStream fis;
				try {
					fis = context.openFileInput(KeyValues.get(key));
					val= new byte[fis.available()];
					while(fis.read(val)!=-1);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("dude error in provider class opening local file");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.out.println("dude error in provider class reading value from local file");
				}
				
				
				String [] coloumn_values= {KeyValues.get(key), new String(val)};
				
				//added : end
				
				
				
				
				//String [] coloumn_values= KeyValues.get(key).split("\\*");
				result.addRow(coloumn_values);
				
				
			}else {
				System.out.println("dude key is not found in the local database.....");
				Message m= new Message();
				Functions.forward(m.query_create(selection), port);
				while(arrived){
					/*try {
						//Thread.sleep(300);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						System.out.println("Thread sleep failed........");
					}*/
				}
				arrived=true;
			}

		}
				
		
		return result;
		
    }
	
	

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    
    // helper functions..........................
    
    //hash generation function....
    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
        
    }

	
    //forward function which forwards the messages passed to it............
    private void forward(final String message, final int portno) {
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
    	
	}// end of forward method.............
    
      }// end of content provider class.............


/////////////////////////////////////////////////////////////////////////////////////////////////////
// server thread which handles incoming messages.........



 class Server extends Thread {

	ServerSocket server=null;
	Socket client=null;
	
	public Server(){
		
	}
	public void run() {
		try {
			System.out.println("server starting at 10000");
			server=new ServerSocket(10000);
			while(true){
				client=server.accept();
				BufferedReader br= new BufferedReader(new InputStreamReader(client.getInputStream()));
				String incoming=br.readLine();
				//DataInputStream dis= new DataInputStream(client.getInputStream());
				
				//byte[] incoming_bytes=new byte[4096];
				//dis.read(incoming_bytes);
				//while(br.readLine()!=null) incoming=br.readLine();
				//incoming= new String(incoming_bytes);
				br.close();
				client.close();
				
				System.out.println("incoming messag = "+incoming);
				String[] message=incoming.split(":");
				
				
				
				if(message[0].equals("request")){
					System.out.println("request message rxd");
					try {
						Functions.request_handler(message, incoming);
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}
					
				else if(message[0].equals("response")){
					System.out.println("response message rxd");
					Functions.response_handler(message);
				}
				else if(message[0].equals("update")){
					System.out.println("update message rxd");
					Functions.update_handler(message);
				}
				else if(message[0].equals("query")) {
					System.out.println("query message rxd");
					Functions.query_handler(message, incoming);
				}
				else if(message[0].equals("queryhit")) {
					System.out.println("query hit message rxd");
					Functions.queryhit_handler(message);
				}
				else if(message[0].equals("movekeys")){
					System.out.println("movekeys message rxd");
					Functions.movekeys_handler(message);
				}
				else if(message[0].equals("insert")){
					System.out.println("insert message rxd");
					Functions.insert_handler(message, incoming);
				}
				else System.out.println("invalid message rxd");
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			System.out.println("unable to create server socket");
		}
	}
 }



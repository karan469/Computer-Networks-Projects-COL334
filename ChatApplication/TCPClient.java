import java.io.*; 
import java.net.*; 
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class TCPClient { 

    public static void main(String argv[]) throws Exception 
    {
		int portNumber1=6789;
		int portNumber2=6788;
        
        //To read strings from the terminal 
        BufferedReader inFromUser = 
          new BufferedReader(new InputStreamReader(System.in)); 
        //get username
        System.out.println("Username: ");
        String username = inFromUser.readLine();
        ///////////////////////////////////

        ///////////////////////////////////
        // TCP Socket for sending the data//
        Socket clientSendSocket = new Socket("localhost", portNumber1);
        //CSS - clientSendSocket
        //Sends message to the server
        DataOutputStream CSSSendToServer = 
        new DataOutputStream(clientSendSocket.getOutputStream()); 
        //Acks from the server
        BufferedReader CSSAckFromServer = 
            new BufferedReader(new
            InputStreamReader(clientSendSocket.getInputStream())); 
        
        //Send for the registration
        //Server will check for the user correctness
        CSSSendToServer.writeBytes("REGISTER TOSEND "+ username + "\n");
		String ack = CSSAckFromServer.readLine();
        while (!ack.equals("REGISTERED TOSEND "+ username)) {
          System.out.println(ack);
          System.out.println("username is not alphanumeric without spaces");
          username = inFromUser.readLine();
          CSSSendToServer.writeBytes("REGISTER TOSEND"+ username);
          ack = CSSAckFromServer.readLine();
        }
        ///////////////////////////////////

        ///////////////////////////////////
        //TCP Socket for recieving messages//
        // Socket clientReceiveSocket = new Socket("localhost", portNumber2); 
        // //CRS - clientReceiveSocket
        // //Ack message to the server
        // DataOutputStream CRSAckToServer = 
        // new DataOutputStream(clientReceiveSocket.getOutputStream()); 
        // //Message from the server
        // BufferedReader CRSRecievefromServer = 
        //     new BufferedReader(new
        //     InputStreamReader(clientReceiveSocket.getInputStream())); 

        // //Register for recieving port
		// CRSAckToServer.writeBytes("REGISTERTORECV["+ username + "]\n"+ '\n');
		// String ack1 = CRSRecievefromServer.readLine();
		// System.out.println(ack1);
        //Assuming no username fault this time
        ///////////////////////////////////

        ///////////////////////////////////
        //start the client operation
        // Generate two threads for sending and recieving messages
		SendingThread sendthread = new SendingThread(username,
		clientSendSocket, CSSSendToServer, CSSAckFromServer, inFromUser);
		Thread threadforsending = new Thread(sendthread);
		threadforsending.start();
		// ReceivingThread receivethread = new ReceivingThread(username, 
		// clientReceiveSocket, CRSAckToServer, CRSRecievefromServer);
		// Thread threadforreceiving = new Thread(receivethread);
		// threadforreceiving.start();

		// if(!threadforsending.isAlive())
		// {
		// 	receivethread.stop();
		// }
    }
} 

/**
 * SendingThread
 */
class SendingThread implements Runnable{
	String username;
	Socket clientSendSocket;
	DataOutputStream CSSSendToServer;
	BufferedReader CSSAckFromServer;
	BufferedReader inFromUser;

	SendingThread (String username, Socket clientSendSocket, 
	DataOutputStream CSSSendToServer, BufferedReader CSSAckFromServer, BufferedReader inFromUser)
	{
		this.inFromUser = inFromUser;
		this.username = username;
		this.clientSendSocket = clientSendSocket;
		this.CSSAckFromServer = CSSAckFromServer;
		this.CSSSendToServer = CSSSendToServer;
	}

	String sendMessage() throws IOException
	{
		String str, sentence;
		Pattern message = Pattern.compile("@[A-Za-z0-9]+[ ]+.+");
		sentence = inFromUser.readLine();
		while(!message.matcher(sentence).matches())
		{
			if(sentence.equals("exit\n"))
			{
				return null;
			}
			System.out.println("Required Format is: @[recipient username][message]");
			sentence = inFromUser.readLine();
		}
		Matcher m1,m2;
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(sentence);
		m2 = Pattern.compile("@[a-zA-Z0-9]+[ ]+").matcher(sentence);
		m2.find();
		m1.find();
		str = "SEND "+m1.group(0)+"\n"+
				"Content-length: "+sentence.substring(m2.group(0).length()).length()+"\n"+
				"\n"+
				sentence.substring(m2.group(0).length()) + "\n";
		System.out.println(str);
		return str;
	}
	///////////////////////////////////
	@Override
	public void run()
	{
		String sendTaar="";
		String Ack;
		try {
			while(true) {
				try {

					sendTaar = sendMessage();
					if(sendTaar==null)
					{
						break;
					}
					CSSSendToServer.writeBytes(sendTaar); 
	
					Ack = CSSAckFromServer.readLine(); 
	
					System.out.println("FROM SERVER: "+ Ack);
				} catch (Exception e) {
					//TODO: handle exception
					System.out.println(e);
					clientSendSocket.close(); 
				}
		   }
		   clientSendSocket.close(); 
		} catch (Exception e) {
			//TODO: handle exception
			System.out.println(e);

		}
	}
	///////////////////////////////////
}

/**
 * ReceivingThread
 */
class ReceivingThread implements Runnable{
	String username;
	Socket clientReceiveSocket;
	DataOutputStream CRSAckToServer;
	BufferedReader CRSRecievefromServer;
	Boolean stop = false;
	ReceivingThread (String username, Socket clientReceiveSocket, 
	DataOutputStream CRSAckToServer, BufferedReader CRSRecievefromServer)
	{
		this.username = username;
		this.clientReceiveSocket = clientReceiveSocket;
		this.CRSRecievefromServer = CRSRecievefromServer;
		this.CRSAckToServer = CRSAckToServer;
	}
	
	public void stop()
	{
		stop = true;
	}
	List<String> receiveMessage() throws IOException
	{
		List<String> l1 = new ArrayList<String>();
		String output="";

		Matcher m1,m2;
		String username;
		username = CRSRecievefromServer.readLine();
		// System.out.println("1st line " + username);
		if(!username.matches("FORWARD[ ]+[a-zA-Z0-9]+"))
		{
			l1.add("ERROR 103 Header incomplete\n\n");
			return l1;
		}
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(username);
		String contentLength;
		contentLength = CRSRecievefromServer.readLine();
		// System.out.println("2nd line " + contentLength);
		if(!contentLength.matches("Content-length:[ ]+[0-9]+")) 
		{
			l1.add("ERROR 103 Header incomplete\n\n");
			return l1;
		}
		m2 = Pattern.compile("[0-9]+").matcher(contentLength);
		
		String nwl = CRSRecievefromServer.readLine();//this will be \n
		// System.out.println("3rd line " + nwl);
		if(!nwl.equals(""))
		{
			l1.add("ERROR 103 Header incomplete\n\n");
			return l1;
		}
		m1.find();m2.find();
		int length = Integer.valueOf(m2.group(0));
		// System.out.println("length "+length);
		int count = 0;
		String message;
		while(count!=length)
		{
			message = CRSRecievefromServer.readLine();
			if(message.length()+count<=length)
			{
				output = output + message;
				count = count + message.length();
			}
			else
			{
				output = output + message.substring(0, length-count-1);
				count = count + (length-count);
			}
		}
		m1.find();
		l1.add(m1.group(0));
		l1.add(output);
		return l1;
	}
	///////////////////////////////////
	@Override
	public void run()
	{
		List<String> receiveTaar;
		// String Ack;
		try {
			while(!stop) {
				try {

					receiveTaar = receiveMessage();
					if(receiveTaar.size()>1)
					{
						System.out.println(receiveTaar.get(0)+": "+receiveTaar.get(1));

						CRSAckToServer.writeBytes("RECEIVED "+ receiveTaar.get(0)+"\n\n");
					}
					else
					{
						System.out.println(receiveTaar.get(0));
						CRSAckToServer.writeBytes(receiveTaar.get(0));
						break;
					}
				} catch (Exception e) {
					//TODO: handle exception
					System.out.println(e);
					clientReceiveSocket.close(); 
					stop = true;
				}
		   }
		 	clientReceiveSocket.close(); 

		} catch (Exception e) {
			//TODO: handle exception
			System.out.println(e);
			// clientReceiveSocket.close(); 
		}

	}
	///////////////////////////////////
}
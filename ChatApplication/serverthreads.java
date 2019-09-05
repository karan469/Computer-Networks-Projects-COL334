import java.io.*; 
import java.net.*; 
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
import java.lang.*;
import java.nio.ByteBuffer;

class TCPServer { 
  public static Hashtable<String, SocketThread> table = new Hashtable<String, SocketThread>();

  public static void main(String argv[]) throws Exception 
    { 

      ServerSocket welcomeSocket = new ServerSocket(6789); 
      Hashtable<String, Socket> list = new Hashtable<String, Socket>();
  
      while(true) { 
 
          Socket connectionSocket = welcomeSocket.accept(); 

          // BufferedReader inFromClient = 
          //  new BufferedReader(new
          //  InputStreamReader(connectionSocket.getInputStream())); 


          // DataOutputStream outToClient = 
          //  new DataOutputStream(connectionSocket.getOutputStream()); 

      	  SocketThread socketThread1 = new SocketThread(connectionSocket, false); //TOSEND to server
          SocketThread socketThread2 = new SocketThread(connectionSocket, true);//TORECV from server
          Thread thread1 = new Thread(socketThread1);
          //Thread thread2 = new Thread(socketThread2);
          thread1.start();
          //thread2.start();  

      }

    } 
} 
 

class SocketThread implements Runnable {
  String clientSentence; 
  String returnSentence; 
  Socket connectionSocket;
  // BufferedReader inFromClient;
  // DataOutputStream outToClient;
  boolean toRecieveFromServer;
  boolean registeredToSend;
  boolean registeredToRecieve;


  SocketThread (Socket connectionSocket,/* BufferedReader inFromClient, DataOutputStream outToClient,*/ boolean toRecieveFromServer) {
  this.connectionSocket = connectionSocket;
  // this.inFromClient = inFromClient;
  // this.outToClient = outToClient;
  this.toRecieveFromServer = toRecieveFromServer;
  this.registeredToRecieve = false;
  this.registeredToSend = false;
  } 

  public void run() {
    System.out.println("Connected Socket: " + connectionSocket);
    try{
      Scanner in = new Scanner(connectionSocket.getInputStream());
      PrintWriter out = new PrintWriter(connectionSocket.getOutputStream(), true);
      // while(in.hasNextLine()){
      //   out.println(in.nextLine().toUpperCase());
      // }

      //REGISTRATION
      String username  = "";
      while(this.registeredToSend == false){
        String clientSentence = in.nextLine();
        if(clientSentence.indexOf("REGISTER TOSEND")!=-1 && Pattern.matches("^[a-zA-Z0-9]+$", clientSentence.split("REGISTER TOSEND ")[1])){
          username = clientSentence.split("REGISTER TOSEND ")[1];
          out.println("REGISTERED TOSEND " + clientSentence.split("REGISTER TOSEND ")[1]);
          this.registeredToSend = true;
          TCPServer.table.put(clientSentence.split("REGISTER TOSEND ")[1], this);
          break;
        }
        out.println("ERROR 100 Malformed username");
      }
      System.out.println("Registered user " + username);
      

      //MESSAGE SENDING
      boolean exitApp = false;
      while(!exitApp){
        // recieveMessage(connectionSocket, in, out);
        String packet = "";
        String recipient = "";
        int len = 0;
        String clientSentence = in.nextLine();
        boolean h1 = false;
        boolean h2 = false;
        // boolean h3 = false;
        if(clientSentence.indexOf("SEND ")!=-1){
          h1 = true;
          recipient = clientSentence.split("SEND ")[1];
        } else {out.println("ERR103");}
        clientSentence = in.nextLine();
        if(clientSentence.indexOf("Content-length: ")!=-1 && h1){
          h2 = true;
          len = Integer.parseInt(clientSentence.split("Content-length: ")[1]);
        } else {out.println("ERR103");}


        // clientSentence = in.nextLine();
        clientSentence = in.nextLine();

        if(h2){
          packet = clientSentence;
        }
        System.out.println(packet);

      }
      
      // System.out.println("mESSAGE RECVED BIATCH");

    } catch (Exception e) {
      System.out.println("Error: " + connectionSocket);
    } finally {
      try {connectionSocket.close();} catch (IOException e) {}
      System.out.println("Closed: "+connectionSocket);
    }
  }

  public void recieveMessage(Socket clientSocket, Scanner in, PrintWriter out){
    byte[] messageByte = new byte[1000];
    boolean end = false;
    String dataString = "";

    try 
    {
      String clientSentence = in.nextLine();
      if(clientSentence.indexOf("SEND ")!=-1){
        if(in.hasNextLine()) clientSentence = in.nextLine();
        System.out.println(clientSentence);
        
        if(clientSentence.indexOf("Length: ")!=-1){
          int len = Integer.parseInt(clientSentence.split("Length: ")[1]);
          System.out.println(len);
          for(int i=0;i<len;i++){
            dataString+=in.next().charAt(0);
          } 
        } else {
          out.println("ERROR103 WRONG Message Format");
        }
      } else {
        out.println("ERROR103 Wrong Message Format..");
        // break;
      }
      System.out.println("MESSAGE: " + dataString);
    }
    catch (Exception e)
    {
        e.printStackTrace();
    }
  }
}

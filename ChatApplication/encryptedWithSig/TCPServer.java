import java.io.*;
import java.net.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
import java.lang.*;
import java.nio.ByteBuffer;

class TCPServer {
  public static Hashtable<String, Scanner> tableIn = new Hashtable<String, Scanner>();
  public static Hashtable<String, PrintWriter> tableOut = new Hashtable<String, PrintWriter>();
  public static Hashtable<String, byte[]> publickeys = new Hashtable<String, byte[]>();
  
  public static void main(String argv[]) throws Exception {

    ServerSocket welcomeSocket = new ServerSocket(6789);
    Hashtable<String, Socket> list = new Hashtable<String, Socket>();

    while (true) {

      Socket connectionSocket1 = welcomeSocket.accept();
      Socket connectionSocket2 = welcomeSocket.accept();

      System.out.println("ClientSockets: " + connectionSocket1);
      System.out.println("ClientSockets: " + connectionSocket2);
      Scanner in1 = new Scanner(connectionSocket1.getInputStream());
      PrintWriter out1 = new PrintWriter(connectionSocket1.getOutputStream(), true);
      Scanner in2 = new Scanner(connectionSocket2.getInputStream());
      PrintWriter out2 = new PrintWriter(connectionSocket2.getOutputStream(), true);

      SocketThreadToSend socketThread1 = new SocketThreadToSend(in1, out1);
      SocketThreadToRecieve socketThread2 = new SocketThreadToRecieve(in2, out2);

      // SocketThread socketThread1 = new SocketThread(connectionSocket, false);
      // //TOSEND to server
      // SocketThread socketThread2 = new SocketThread(connectionSocket,
      // true);//TORECV from server
      Thread thread1 = new Thread(socketThread1);
      Thread thread2 = new Thread(socketThread2);
      thread1.start();
      thread2.start();

    }

  }
}

class SocketThreadToRecieve implements Runnable {
  String clientSentence;
  String returnSentence;
  Scanner in;
  PrintWriter out;
  boolean registeredToRecieve;

  SocketThreadToRecieve(Scanner in, PrintWriter out) {
    this.in = in;
    this.out = out;
    this.registeredToRecieve = false;
  }

  public void run() {
    // System.out.println("Connected Socket: " + connectionSocket);
    try {
      // REGISTRATION
      String username = "";
      while (this.registeredToRecieve == false) {
        String clientSentence = in.nextLine();
        if (clientSentence.indexOf("REGISTER TORECV") != -1
            && Pattern.matches("^[a-zA-Z0-9]+$", clientSentence.split("REGISTER TORECV ")[1])) {
          username = clientSentence.split("REGISTER TORECV ")[1];
          out.println("REGISTERED TORECV " + clientSentence.split("REGISTER TORECV ")[1]);
          this.registeredToRecieve = true;

          TCPServer.tableIn.put(clientSentence.split("REGISTER TORECV ")[1], in);
          TCPServer.tableOut.put(clientSentence.split("REGISTER TORECV ")[1], out);
          break;
        } else {
          out.println("ERROR 100 Malformed username");
        }
      }
      System.out.println("Registered user " + username);
      in.nextLine();
      String line = in.nextLine();
      String pubkey = "";
      System.out.println(line);
      if (line.indexOf("PUBLICKEY: ") != -1) {
        pubkey = line.split("PUBLICKEY: ")[1];
        out.println("[ACK] SERVER GOT THE KEY");
      }

      byte[] publickey = Base64.getDecoder().decode(pubkey);
      TCPServer.publickeys.put(username, publickey);

      // System.out.println(TCPServer.tableIn);
      // System.out.println(TCPServer.tableOut);

    } catch (Exception e) {
      System.out.println("Error: " + e);
    } finally {
      // try {connectionSocket.close();*/} catch (IOException e) {}
      System.out.println("Closed Receiving Thread: ");
    }
  }
}

class SocketThreadToSend implements Runnable {
  String clientSentence;
  String returnSentence;
  // BufferedReader inFromClient;
  // DataOutputStream outToClient;
  boolean registeredToSend;

  Scanner in;
  PrintWriter out;

  SocketThreadToSend(Scanner in, PrintWriter out) {
    // this.connectionSocket = connectionSocket;
    // this.inFromClient = inFromClient;
    // this.outToClient = outToClient;
    // this.toRecieveFromServer = toRecieveFromServer;
    // this.registeredToRecieve = false;
    this.registeredToSend = false;
    this.in = in;
    this.out = out;
  }

  public void run() {
    // System.out.println("Connected Socket: " + connectionSocket);
    try {
      // REGISTRATION
      String username = "";
      String clientSentence = "";
      while (this.registeredToSend == false) {
        clientSentence = in.nextLine();
        if (clientSentence.indexOf("REGISTER TOSEND") != -1
            && Pattern.matches("^[a-zA-Z0-9]+$", clientSentence.split("REGISTER TOSEND ")[1])) {
          username = clientSentence.split("REGISTER TOSEND ")[1];
          out.println("REGISTERED TOSEND " + clientSentence.split("REGISTER TOSEND ")[1]);
          this.registeredToSend = true;

          // TCPServer.table.put(clientSentence.split("REGISTER TOSEND ")[1], this);

          break;
        } else {
          out.println("ERROR 100 Malformed username");
          in.nextLine();
        }
      }
      System.out.println("Registered user " + username);
      // System.out.println(TCPServer.tableIn);
      // System.out.println(TCPServer.tableOut);
      
      in.nextLine();
      // MESSAGE SENDING
      boolean exitApp = false;
      while (!exitApp) {
        // recieveMessage(connectionSocket, in, out);4
        String pubkeyask = in.nextLine();
        // System.out.println(pubkeyask);
        String user = pubkeyask.split("GET PUBLICKEY ")[1];
        if (TCPServer.publickeys.containsKey(user)) {
          out.println("PUBLICKEY " + Base64.getEncoder().encodeToString(TCPServer.publickeys.get(user)));
        } else {
          out.println("Error 101 No User: " + user + " registered");
          System.out.println("Wrong user requested");
          continue;
        }

        String packet = "";
        String recipient = "";
        int len = 0;
        clientSentence = in.nextLine();
        boolean h1 = false;
        boolean h2 = false;
        boolean h3 = false;
        boolean h5 = false;
        String sigInString = "";

        System.out.println(clientSentence);
        if (clientSentence.equals("")) {
          continue;
        }
        if (clientSentence.indexOf("SEND ") != -1) {
          h1 = true;
          recipient = clientSentence.split("SEND ")[1];
        } else {
          out.println("ERROR 103 Incomplete Header\n");
          out.println("\n");
          continue;
        }

        if (TCPServer.tableOut.containsKey(recipient)) {
          h3 = true;
        }

        clientSentence = in.nextLine();
        if(clientSentence.indexOf("Signature: ")!=-1 && h1){
          h5 = true;
          sigInString = clientSentence.split("Signature: ")[1];
          System.out.println("Signature Intercepted: " + sigInString);
        } else {
          out.println("ERR1033 Incomplete Header\n");
          out.println("\n");
          continue;
        }

        clientSentence = in.nextLine();
        if (clientSentence.indexOf("Content-length: ") != -1 && h5) {
          h2 = true;
          len = Integer.parseInt(clientSentence.split("Content-length: ")[1]);
        } else {
          out.println("ERR103 Incomplete Header\n");
          out.println("\n");
          continue;
        }

        clientSentence = in.nextLine();
        clientSentence = in.nextLine();
        if (h2) {
          packet = clientSentence;
        }
        System.out.println("Message intercepted by SERVER: " + packet); // DEBUG

        if (TCPServer.tableOut.containsKey(recipient)) {
          Scanner inForSecondParty = TCPServer.tableIn.get(recipient);
          PrintWriter outForSecondParty = TCPServer.tableOut.get(recipient);
          byte[] sendersPubKey = TCPServer.publickeys.get(username);
          String sendersPubKeyInString = Base64.getEncoder().encodeToString(sendersPubKey);
          outForSecondParty.println("FORWARD " + username);
          System.out.println("FORWARD " + username);//DEBUG
          outForSecondParty.println("SENDER PUBLIC KEY: " + sendersPubKeyInString);//sig sending
          System.out.println("SENDER PUBLIC KEY: " + sendersPubKeyInString);//DEBUG
          outForSecondParty.println("Signature: " + sigInString);//sig sending
          outForSecondParty.println("Content-length: " + len);
          outForSecondParty.println("\n");
          outForSecondParty.println(packet);

          String responseFromSecondParty = inForSecondParty.nextLine();
          responseFromSecondParty = inForSecondParty.nextLine();
          System.out.println(responseFromSecondParty);
          if (responseFromSecondParty.indexOf("RECEIVED ") != -1) { // ACK is positive
            out.println("SENT " + recipient);
            out.println("\n");
          } else if (responseFromSecondParty.indexOf("ERROR 103 Header Incomplete") != -1) { // ACK is
                                                    // negative
            out.println("ERROR102 Unable to send");
            out.println("\n");
          }

        } else {
          out.println("Error 101 No User: " + recipient + " registered");
          out.println("\n");
          System.out.println("FK yea");
        }
      }
    } catch (Exception e) {
      // System.out.println("Error: " + connectionSocket);
      System.out.println(e);
    } finally {
      // try {connectionSocket.close();} catch (IOException e) {}
      System.out.println("Closed Sending Thread: "/* +connectionSocket */);
    }
  }
}
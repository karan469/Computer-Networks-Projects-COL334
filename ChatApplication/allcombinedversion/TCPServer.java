import java.io.*;
import java.net.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
import java.lang.*;
import java.nio.ByteBuffer;
class TerminateInServer extends Exception 
{ 
    public TerminateInServer() 
    { 
    } 
}
class TCPServer {
	public static Hashtable<String, Scanner> tableIn = new Hashtable<String, Scanner>();
	public static Hashtable<String, PrintWriter> tableOut = new Hashtable<String, PrintWriter>();
	public static Hashtable<String, byte[]> publickeys = new Hashtable<String, byte[]>();

	public static void main(String argv[]) throws Exception {

		ServerSocket welcomeSocket = new ServerSocket(3000);
		Hashtable<String, Socket> list = new Hashtable<String, Socket>();

		while (true) {
			String mode = "";
			// nonencrypt
			// encrypt
			// encryptwithsig
			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			if (argv.length == 0) {
				mode = "nonencrypt";
			} else {
				mode = argv[0];
			}
			while(!(mode.equals("encrypt")||mode.equals("encryptwithsig")||mode.equals("nonencrypt")))
			{
				System.out.println("Three mode only:\n1. nonencrypt\n2. encrypt\n3. encryptwithsig");
				System.out.println("mode: ");
				mode = inFromUser.readLine();
			}
			System.out.println(mode);

			Socket connectionSocket1 = welcomeSocket.accept();
			Socket connectionSocket2 = welcomeSocket.accept();
			// BufferedReader inFromClient =
			// new BufferedReader(new
			// InputStreamReader(connectionSocket.getInputStream()));

			// DataOutputStream outToClient =
			// new DataOutputStream(connectionSocket.getOutputStream());
			System.out.println("ClientSockets: " + connectionSocket1);
			System.out.println("ClientSockets: " + connectionSocket2);
			Scanner in1 = new Scanner(connectionSocket1.getInputStream());
			PrintWriter out1 = new PrintWriter(connectionSocket1.getOutputStream(), true);
			Scanner in2 = new Scanner(connectionSocket2.getInputStream());
			PrintWriter out2 = new PrintWriter(connectionSocket2.getOutputStream(), true);

			// SocketThreadToSend socketThread1 = new SocketThreadToSend(in1, out1);
			// SocketThreadToRecieve socketThread2 = new SocketThreadToRecieve(in2, out2);

			if (mode.matches("encrypt")) {
				SocketThreadToSendEncrypted socketThread1 = new SocketThreadToSendEncrypted(in1, out1);
				SocketThreadToRecieveEncrypted socketThread2 = new SocketThreadToRecieveEncrypted(in2, out2);
				Thread thread1 = new Thread(socketThread1);
				Thread thread2 = new Thread(socketThread2);
				thread1.start();
				thread2.start();
			} else if (mode.matches("encryptwithsig")) {
				SocketThreadToSendEncryptedWithSig socketThread1 = new SocketThreadToSendEncryptedWithSig(in1, out1);
				SocketThreadToRecieveEncryptedWithSig socketThread2 = new SocketThreadToRecieveEncryptedWithSig(in2, out2);
				Thread thread1 = new Thread(socketThread1);
				Thread thread2 = new Thread(socketThread2);
				thread1.start();
				thread2.start();
			} else {
				SocketThreadToSendUnencrypted socketThread1 = new SocketThreadToSendUnencrypted(in1, out1);
				SocketThreadToRecieveUnencrypted socketThread2 = new SocketThreadToRecieveUnencrypted(in2, out2);
				Thread thread1 = new Thread(socketThread1);
				Thread thread2 = new Thread(socketThread2);
				thread1.start();
				thread2.start();
			}
		}
	}
}
//////////////////////////////////////////////////////////////////
/**
 * For Unencrypted
 */

//////////////////////////////////////////////////////////////////

class SocketThreadToRecieveUnencrypted implements Runnable {
	String clientSentence;
	String returnSentence;
	Scanner in;
	PrintWriter out;
	boolean registeredToRecieve;

	SocketThreadToRecieveUnencrypted(Scanner in, PrintWriter out) {
		this.in = in;
		this.out = out;
		this.registeredToRecieve = false;
	}

	public void run() {
		// System.out.println("Connected Socket: " + connectionSocket);
		try {

			// System.out.println(TCPServer.tablePublicKeys);
			// 1. REGISTRATION
			String username = "";
			while (this.registeredToRecieve == false) {
				String clientSentence = in.nextLine();
				if (clientSentence.indexOf("REGISTER TORECV") != -1
						&& Pattern.matches("^[a-zA-Z0-9]+$", clientSentence.split("REGISTER TORECV ")[1])) {
					username = clientSentence.split("REGISTER TORECV ")[1];
					out.println("REGISTERED TORECV " + clientSentence.split("REGISTER TORECV ")[1]);
					out.println("\n");
					this.registeredToRecieve = true;

					TCPServer.tableIn.put(clientSentence.split("REGISTER TORECV ")[1], in);
					TCPServer.tableOut.put(clientSentence.split("REGISTER TORECV ")[1], out);

					break;
				} else {
					out.println("ERROR 100 Malformed username");
					out.println("\n");
				}
			}
			System.out.println("Registered user " + username);

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

class SocketThreadToSendUnencrypted implements Runnable {
	String clientSentence;
	String returnSentence;
	boolean registeredToSend;

	Scanner in;
	PrintWriter out;

	SocketThreadToSendUnencrypted(Scanner in, PrintWriter out) {
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
				&& clientSentence.split("REGISTER TOSEND ").length>0) {
					if (clientSentence.indexOf("REGISTER TOSEND") != -1
						&& Pattern.matches("^[a-zA-Z0-9]+$", clientSentence.split("REGISTER TOSEND ")[1])) {
						username = clientSentence.split("REGISTER TOSEND ")[1];
						if(TCPServer.tableIn.containsKey(username))
						{
							out.println("USERNAME TAKEN");
							out.println("\n");
							in.nextLine();
						}
						else
						{
							out.println("REGISTERED TOSEND " + clientSentence.split("REGISTER TOSEND ")[1]);
							out.println("\n");
							this.registeredToSend = true;
							break;
						}

						// TCPServer.table.put(clientSentence.split("REGISTER TOSEND ")[1], this);
					} else {
						out.println("ERROR 100 Malformed username");
						out.println("\n");
						in.nextLine();
					}
				}
				else {
					out.println("ERROR 100 Malformed usernamedkjfha");
					out.println("\n");
					in.nextLine();
					// in.nextLine();
				}
			}
			System.out.println("Registered user " + username + " for recieveing messages.");
			// System.out.println(TCPServer.tableIn);
			// System.out.println(TCPServer.tableOut);

			// MESSAGE SENDING
			boolean exitApp = false;
			while (!exitApp) {
				// recieveMessage(connectionSocket, in, out);
				String packet = "";
				String recipient = "";
				int len = 0;
				clientSentence = in.nextLine();
				boolean h1 = false;
				boolean h2 = false;
				boolean h3 = false;
				if(clientSentence.matches("UNREGISTER")){
					PrintWriter out = TCPServer.tableOut.get(username);
					out.println("UNREGISTER");
					TCPServer.tableIn.remove(username);
					TCPServer.publickeys.remove(username);
					TCPServer.tableOut.remove(username);
					throw new TerminateInServer();
				}
				System.out.println(clientSentence.length());
				if (clientSentence.length()==0) {
					continue;
				}
				if (clientSentence.indexOf("SEND ") != -1) {
					h1 = true;
					recipient = clientSentence.split("SEND ")[1];
				} else {
					out.println("On Send: ERROR 103 Incomplete Header\n");
					out.println("\n");
					continue;
				}

				if (TCPServer.tableOut.containsKey(recipient)) {
					h3 = true;
				}

				clientSentence = in.nextLine();
				if (clientSentence.indexOf("Content-length: ") != -1 && h1) {
					h2 = true;
					len = Integer.parseInt(clientSentence.split("Content-length: ")[1]);
				} else {
					out.println("On contentlength: ERR103 Incomplete Header\n");
					out.println("\n");
					continue;
				}
				clientSentence = in.nextLine();
				int count=-1;
				String out1 = "";
				while (count != len) {
					if(count>=0) out1 = out1+"\n";
					count = count + 1;
					clientSentence = in.nextLine();
					if (clientSentence.length() + count <= len) {
						out1 = out1 + clientSentence;
						count = count + clientSentence.length();
					} else {
						out1 = out1 + clientSentence.substring(0, len - count - 1);
						count = count + (len - count);
					}
				}
				if (h2) {
					packet = out1;
				}
				// System.out.printf("debug %s", packet); // DEBUG

				if (TCPServer.tableOut.containsKey(recipient)) {
					Scanner inForSecondParty = TCPServer.tableIn.get(recipient);
					PrintWriter outForSecondParty = TCPServer.tableOut.get(recipient);
					outForSecondParty.println("FORWARD " + username);
					outForSecondParty.println("Content-length: " + len);
					outForSecondParty.println("\n");
					outForSecondParty.println(packet);

					String responseFromSecondParty = inForSecondParty.nextLine();
					responseFromSecondParty = inForSecondParty.nextLine();
					System.out.println(responseFromSecondParty);
					if (responseFromSecondParty.indexOf("RECEIVED ") != -1) { // ACK is positive
						out.println("SENT " + recipient);
						out.println("\n");
					} else if (responseFromSecondParty.indexOf("ERROR 103") != -1) { // ACK is
																										// negative
						out.println("ERROR102 Header incomplete");
						out.println("\n");
					}

				} else {
					out.println("Error 101 No User: "+recipient+" registered");
					out.println("\n");
					System.out.println("FK yea");
				}
			}
		} catch (Exception e) {
			// System.out.println("Error: " + connectionSocket);
			System.out.println("Error in sending: " + e);
		} finally {
			// try {connectionSocket.close();} catch (IOException e) {}
			System.out.println("Closed Sending Thread: ");
		}
	}
}

//////////////////////////////////////////////////////////////////
/**
 * For Encrypted
 */

//////////////////////////////////////////////////////////////////

class SocketThreadToRecieveEncrypted implements Runnable {
	String clientSentence;
	String returnSentence;
	Scanner in;
	PrintWriter out;
	boolean registeredToRecieve;

	SocketThreadToRecieveEncrypted(Scanner in, PrintWriter out) {
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
					out.println("\n");
					this.registeredToRecieve = true;

					TCPServer.tableIn.put(clientSentence.split("REGISTER TORECV ")[1], in);
					TCPServer.tableOut.put(clientSentence.split("REGISTER TORECV ")[1], out);

					break;
				} else {
					out.println("ERROR 100 Malformed username");
					out.println("\n");
				}
			}
			System.out.println("Registered user " + username);
			in.nextLine();
			String line = in.nextLine();
			String pubkey = "";
			System.out.println(line);
			if (line.indexOf("PUBLICKEY: ") != -1) {
				pubkey = line.split("PUBLICKEY: ")[1];
				out.println("GOT THE KEY");
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

class SocketThreadToSendEncrypted implements Runnable {
	String clientSentence;
	String returnSentence;
	// BufferedReader inFromClient;
	// DataOutputStream outToClient;
	boolean registeredToSend;

	Scanner in;
	PrintWriter out;

	SocketThreadToSendEncrypted(Scanner in, PrintWriter out) {
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
				&& clientSentence.split("REGISTER TOSEND ").length>0) {
					if (clientSentence.indexOf("REGISTER TOSEND") != -1
						&& Pattern.matches("^[a-zA-Z0-9]+$", clientSentence.split("REGISTER TOSEND ")[1])) {
						username = clientSentence.split("REGISTER TOSEND ")[1];
						if(TCPServer.tableIn.containsKey(username))
						{
							out.println("USERNAME TAKEN");
							out.println("\n");
							in.nextLine();
						}
						else
						{
							out.println("REGISTERED TOSEND " + clientSentence.split("REGISTER TOSEND ")[1]);
							out.println("\n");
							this.registeredToSend = true;
							break;
						}

						// TCPServer.table.put(clientSentence.split("REGISTER TOSEND ")[1], this);
					} else {
						out.println("ERROR 100 Malformed username");
						out.println("\n");
						in.nextLine();
					}
				}
				else {
					out.println("ERROR 100 Malformed usernamedkjfha");
					out.println("\n");
					in.nextLine();
					// in.nextLine();
				}
			}
			System.out.println("Registered user " + username + " for recieveing messages.");
			// System.out.println(TCPServer.tableIn);
			// System.out.println(TCPServer.tableOut);
			
			in.nextLine();
			// MESSAGE SENDING
			boolean exitApp = false;
			while (!exitApp) {
				// recieveMessage(connectionSocket, in, out);4
				String pubkeyask = in.nextLine();
				if(pubkeyask.matches("UNREGISTER")){
					PrintWriter out = TCPServer.tableOut.get(username);
					out.println("UNREGISTER");
					TCPServer.tableIn.remove(username);
					TCPServer.publickeys.remove(username);
					TCPServer.tableOut.remove(username);
					throw new TerminateInServer();
				}
				// System.out.println(pubkeyask);
				String user = pubkeyask.split("GET PUBLICKEY ")[1];
				if (TCPServer.publickeys.containsKey(user)) {
					out.println("PUBLICKEY " + Base64.getEncoder().encodeToString(TCPServer.publickeys.get(user)));
				} else {
					out.println("Error 101 No User: " + user + " registered");
					System.out.println("FK yea");
					continue;
				}

				String packet = "";
				String recipient = "";
				int len = 0;
				clientSentence = in.nextLine();
				boolean h1 = false;
				boolean h2 = false;
				boolean h3 = false;

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
				if (clientSentence.indexOf("Content-length: ") != -1 && h1) {
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
				// System.out.println("debug " + packet); // DEBUG

				if (TCPServer.tableOut.containsKey(recipient)) {
					Scanner inForSecondParty = TCPServer.tableIn.get(recipient);
					PrintWriter outForSecondParty = TCPServer.tableOut.get(recipient);
					outForSecondParty.println("FORWARD " + username);
					outForSecondParty.println("Content-length: " + len);
					outForSecondParty.println("\n");
					outForSecondParty.println(packet);

					String responseFromSecondParty = inForSecondParty.nextLine();
					responseFromSecondParty = inForSecondParty.nextLine();
					System.out.println(responseFromSecondParty);
					if (responseFromSecondParty.indexOf("RECEIVED ") != -1) { // ACK is positive
						out.println("SENT " + recipient);
						out.println("\n");
					} else if (responseFromSecondParty.indexOf("ERROR 103") != -1) { // ACK is
																										// negative
						out.println("ERROR102 Header incomplete");
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

//////////////////////////////////////////////////////////////////
/**
 * For EncryptedWithSig
 */

//////////////////////////////////////////////////////////////////

class SocketThreadToRecieveEncryptedWithSig implements Runnable {
	String clientSentence;
	String returnSentence;
	Scanner in;
	PrintWriter out;
	boolean registeredToRecieve;

	SocketThreadToRecieveEncryptedWithSig(Scanner in, PrintWriter out) {
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
					out.println("\n");
					this.registeredToRecieve = true;

					TCPServer.tableIn.put(clientSentence.split("REGISTER TORECV ")[1], in);
					TCPServer.tableOut.put(clientSentence.split("REGISTER TORECV ")[1], out);

					break;
				} else {
					out.println("ERROR 100 Malformed username");
					out.println("\n");
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

class SocketThreadToSendEncryptedWithSig implements Runnable {
	String clientSentence;
	String returnSentence;
	// BufferedReader inFromClient;
	// DataOutputStream outToClient;
	boolean registeredToSend;

	Scanner in;
	PrintWriter out;

	SocketThreadToSendEncryptedWithSig(Scanner in, PrintWriter out) {
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
				&& clientSentence.split("REGISTER TOSEND ").length>0) {
					if (clientSentence.indexOf("REGISTER TOSEND") != -1
						&& Pattern.matches("^[a-zA-Z0-9]+$", clientSentence.split("REGISTER TOSEND ")[1])) {
						username = clientSentence.split("REGISTER TOSEND ")[1];
						if(TCPServer.tableIn.containsKey(username))
						{
							out.println("USERNAME TAKEN");
							out.println("\n");
							in.nextLine();
						}
						else
						{
							out.println("REGISTERED TOSEND " + clientSentence.split("REGISTER TOSEND ")[1]);
							out.println("\n");
							this.registeredToSend = true;
							break;
						}

						// TCPServer.table.put(clientSentence.split("REGISTER TOSEND ")[1], this);
					} else {
						out.println("ERROR 100 Malformed username");
						out.println("\n");
						in.nextLine();
					}
				}
				else {
					out.println("ERROR 100 Malformed usernamedkjfha");
					out.println("\n");
					in.nextLine();
					// in.nextLine();
				}
			}
			System.out.println("Registered user " + username + " for recieveing messages.");
			// System.out.println(TCPServer.tableIn);
			// System.out.println(TCPServer.tableOut);

			in.nextLine();
			// MESSAGE SENDING
			boolean exitApp = false;
			while (!exitApp) {
				// recieveMessage(connectionSocket, in, out);4
				String pubkeyask = in.nextLine();
				if(pubkeyask.matches("UNREGISTER")){
					PrintWriter out = TCPServer.tableOut.get(username);
					out.println("UNREGISTER");
					TCPServer.tableIn.remove(username);
					TCPServer.publickeys.remove(username);
					TCPServer.tableOut.remove(username);
					throw new TerminateInServer();
				}
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
				if (clientSentence.indexOf("Signature: ") != -1 && h1) {
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
					System.out.println("FORWARD " + username);// DEBUG
					outForSecondParty.println("SENDER PUBLIC KEY: " + sendersPubKeyInString);// sig sending
					System.out.println("SENDER PUBLIC KEY: " + sendersPubKeyInString);// DEBUG
					outForSecondParty.println("Signature: " + sigInString);// sig sending
					outForSecondParty.println("Content-length: " + len);
					outForSecondParty.println("\n");
					outForSecondParty.println(packet);

					String responseFromSecondParty = inForSecondParty.nextLine();
					responseFromSecondParty = inForSecondParty.nextLine();
					System.out.println(responseFromSecondParty);
					if (responseFromSecondParty.indexOf("RECEIVED ") != -1) { // ACK is positive
						out.println("SENT " + recipient);
						out.println("\n");
					} else if (responseFromSecondParty.indexOf("ERROR 103") != -1) { // ACK is
						// negative
						out.println("ERROR102 Header incomplete");
						out.println("\n");
					} else if(responseFromSecondParty.indexOf("ERROR 104") != -1)
					{
						out.println("ERROR104 Signature Not Same");
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



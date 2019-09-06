import java.io.*;
import java.net.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.*;
import java.lang.*;
import java.nio.ByteBuffer;

// import java.security.KeyFactory;
import java.security.KeyPair;
// import java.security.KeyPairGenerator;
// import java.security.NoSuchAlgorithmException;
// import java.security.NoSuchProviderException;
// import java.security.PrivateKey;
// import java.security.PublicKey;
// import java.security.SecureRandom;
// import java.security.spec.PKCS8EncodedKeySpec;
// import java.security.spec.X509EncodedKeySpec;

class TCPServer {
	public static Hashtable<String, Scanner> tableIn = new Hashtable<String, Scanner>();
	public static Hashtable<String, PrintWriter> tableOut = new Hashtable<String, PrintWriter>();
	public static Hashtable<String, byte[]> tablePublicKeys = new Hashtable<String, byte[]>();

	public static void main(String argv[]) throws Exception {

		ServerSocket welcomeSocket = new ServerSocket(6789);
		Hashtable<String, Socket> list = new Hashtable<String, Socket>();

		while (true) {

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

			System.out.println(TCPServer.tablePublicKeys);
			// 1. REGISTRATION
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

class SocketThreadToSend implements Runnable {
	String clientSentence;
	String returnSentence;
	boolean registeredToSend;

	Scanner in;
	PrintWriter out;

	SocketThreadToSend(Scanner in, PrintWriter out) {
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
					out.println("\n");
					in.nextLine();
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
					} else if (responseFromSecondParty.indexOf("ERROR 103 Header Incomplete") != -1) { // ACK is
																										// negative
						out.println("ERROR102 Unable to send");
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

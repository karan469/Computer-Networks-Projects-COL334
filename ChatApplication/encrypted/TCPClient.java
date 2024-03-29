import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
//
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class TCPClient {
	public static KeyPair generateKeyPair;
	public static byte[] publicKey;
	public static byte[] privateKey;

	public static void main(String argv[]) throws Exception {
		///////// INITIALIZING ENCRYPTION PAIR//////////////
		try {
			generateKeyPair = EncryptionRSA.generateKeyPair();
		} catch (NoSuchAlgorithmException e) {
			System.out.println("Error: ");
			System.out.println(e);
		} finally {
			generateKeyPair = EncryptionRSA.generateKeyPair();
		}
		publicKey = generateKeyPair.getPublic().getEncoded();
		privateKey = generateKeyPair.getPrivate().getEncoded();
		//////////// END OF INITIALIZING ENCRYPTED PAIR//////

		int portNumber1 = 6789;
		int portNumber2 = 6789;

		// To read strings from the terminal
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		// get username
		System.out.println("Username: ");
		String username = inFromUser.readLine();
		///////////////////////////////////

		///////////////////////////////////
		// TCP Socket for sending the data//
		Socket clientSendSocket = new Socket("localhost", portNumber1);
		// CSS - clientSendSocket
		// Sends message to the server
		DataOutputStream CSSSendToServer = new DataOutputStream(clientSendSocket.getOutputStream());
		// Acks from the server
		BufferedReader CSSAckFromServer = new BufferedReader(new InputStreamReader(clientSendSocket.getInputStream()));
		///////////////////////////////////

		///////////////////////////////////
		// TCP Socket for recieving messages//
		Socket clientReceiveSocket = new Socket("localhost", portNumber2);
		// CRS - clientReceiveSocket
		// Ack message to the server
		DataOutputStream CRSAckToServer = new DataOutputStream(clientReceiveSocket.getOutputStream());
		// Message from the server
		BufferedReader CRSRecievefromServer = new BufferedReader(
				new InputStreamReader(clientReceiveSocket.getInputStream()));

		// Assuming no username fault this time
		///////////////////////////////////

		///////////////////////////////////

		// Send for the registration
		// Server will check for the user correctness
		CSSSendToServer.writeBytes("REGISTER TOSEND " + username + "\n" + "\n");
		String ack = CSSAckFromServer.readLine();
		while (!ack.equals("REGISTERED TOSEND " + username)) {
			System.out.println(ack);
			System.out.println("username is not alphanumeric without spaces");
			username = inFromUser.readLine();
			CSSSendToServer.writeBytes("REGISTER TOSEND " + username + "\n" + "\n");
			System.out.println("send agained");
			ack = CSSAckFromServer.readLine();
			ack = CSSAckFromServer.readLine();
			ack = CSSAckFromServer.readLine();
		}
		System.out.println(ack);
		CSSAckFromServer.readLine();
		CSSAckFromServer.readLine();

		// Register for recieving port
		CRSAckToServer.writeBytes("REGISTER TORECV " + username + "\n\n");
		String ack1 = CRSRecievefromServer.readLine();
		System.out.println(ack1);
		CRSRecievefromServer.readLine();
        CRSRecievefromServer.readLine();

		// SEND PUBLIC KEY TO SERVER//
		CRSAckToServer
				.writeBytes("PUBLICKEY: " + Base64.getEncoder().encodeToString(TCPClient.publicKey) + "\n" + "\n");

		String ack2 = CRSRecievefromServer.readLine();
		System.out.println(ack2);

		// start the client operation
		// Generate two threads for sending and recieving messages
		SendingThread sendthread = new SendingThread(username, clientSendSocket, CSSSendToServer, CSSAckFromServer,
				inFromUser);
		Thread threadforsending = new Thread(sendthread);
		threadforsending.start();
		ReceivingThread receivethread = new ReceivingThread(username, clientReceiveSocket, CRSAckToServer,
				CRSRecievefromServer, privateKey);
		Thread threadforreceiving = new Thread(receivethread);
		threadforreceiving.start();

		if (!threadforsending.isAlive()) {
			receivethread.stop();
		}
	}
}

/**
 * SendingThread
 */
class SendingThread implements Runnable {
	String username;
	Socket clientSendSocket;
	DataOutputStream CSSSendToServer;
	BufferedReader CSSAckFromServer;
	BufferedReader inFromUser;

	SendingThread(String username, Socket clientSendSocket, DataOutputStream CSSSendToServer,
			BufferedReader CSSAckFromServer, BufferedReader inFromUser) {
		this.inFromUser = inFromUser;
		this.username = username;
		this.clientSendSocket = clientSendSocket;
		this.CSSAckFromServer = CSSAckFromServer;
		this.CSSSendToServer = CSSSendToServer;
	}

	String sendMessage() throws IOException {
		String str, sentence;
		Pattern message = Pattern.compile("@[A-Za-z0-9]+[ ]+.+");
		sentence = inFromUser.readLine();
		while (!message.matcher(sentence).matches()) {
			if (sentence.equals("exit\n")) {
				return null;
			}
			System.out.println("Required Format is: @[recipient username][message]");
			sentence = inFromUser.readLine();
		}
		///////////////////////////
		str = inFromUser.readLine();
		while(str.length()!=0)
		{
			sentence = sentence + '\n' + str;
			str = inFromUser.readLine();
		}
		//////////////////////////
		Matcher m1, m2;
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(sentence);
		m2 = Pattern.compile("@[a-zA-Z0-9]+[ ]+").matcher(sentence);
		m2.find();
		m1.find();
		///////////////////////////////////
		String mail="";
		try {
			CSSSendToServer.writeBytes("GET PUBLICKEY "+ m1.group(0)+"\n");
			String pubkey = CSSAckFromServer.readLine();
			System.out.println(pubkey);
			// inFromUser.readLine();
			if(pubkey.indexOf("Error 101 ")!=-1)
			{
				System.out.println("FROM SERVER: " + pubkey);
				return "error";
			}
			pubkey = pubkey.split("PUBLICKEY ")[1];

			byte[] encryptedData = EncryptionRSA.encrypt(Base64.getDecoder().decode(pubkey),
			sentence.substring(m2.group(0).length()).getBytes());

			mail = Base64.getEncoder().encodeToString(encryptedData);
		} catch (Exception e) {
			//TODO: handle exception]
			System.out.println("error in sending thread: " + e);
		}
		///////////////////////////////////		
		// System.out.println("mail lenght: "+mail.length());
		str = "SEND " + m1.group(0) + "\n" + "Content-length: " + sentence.substring(m2.group(0).length()).length()
				+ "\n" + "\n" + mail + "\n";
		// System.out.println(str);
		return str;
	}

	///////////////////////////////////
	@Override
	public void run() {
		String sendTaar = "";
		String kiskoDena = "";
		String Ack;
		try {
			while (true) {
				try {
					sendTaar = sendMessage();
					if (sendTaar == null) {
						break;
					}
					if(sendTaar.equals("error"))
					{
						continue;
					}
					CSSSendToServer.writeBytes(sendTaar);

					Ack = CSSAckFromServer.readLine();

					System.out.println("FROM SERVER: " + Ack);
					Ack = CSSAckFromServer.readLine();
					Ack = CSSAckFromServer.readLine();
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(e);
					clientSendSocket.close();
				}
			}
			clientSendSocket.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);

		}
	}
	///////////////////////////////////
}

/**
 * ReceivingThread
 */
class ReceivingThread implements Runnable {
	String username;
	Socket clientReceiveSocket;
	DataOutputStream CRSAckToServer;
	BufferedReader CRSRecievefromServer;
	Boolean stop = false;
	byte[] privateKey;
	ReceivingThread(String username, Socket clientReceiveSocket, DataOutputStream CRSAckToServer,
			BufferedReader CRSRecievefromServer, byte[] privateKey) {
		this.username = username;
		this.clientReceiveSocket = clientReceiveSocket;
		this.CRSRecievefromServer = CRSRecievefromServer;
		this.CRSAckToServer = CRSAckToServer;
		this.privateKey = privateKey;
	}

	public void stop() {
		stop = true;
	}

	List<String> receiveMessage() throws IOException {
		List<String> l1 = new ArrayList<String>();
		String output = "";

		Matcher m1, m2;
		String username;
		username = CRSRecievefromServer.readLine();
		// System.out.println("1st line " + username);
		if (!username.matches("FORWARD[ ]+[a-zA-Z0-9]+")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			return l1;
		}
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(username);
		String contentLength;
		contentLength = CRSRecievefromServer.readLine();
		// System.out.println("2nd line " + contentLength);
		if (!contentLength.matches("Content-length:[ ]+[0-9]+")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			return l1;
		}
		m2 = Pattern.compile("[0-9]+").matcher(contentLength);

		String nwl = CRSRecievefromServer.readLine();// this will be \n
		// System.out.println("3rd line " + nwl);
		if (!nwl.equals("")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			return l1;
		}
		m1.find();
		m2.find();
		int length = Integer.valueOf(m2.group(0));
		// System.out.println("length "+length);

		///////////////////////////////////
		String message;
		CRSRecievefromServer.readLine();
		message = CRSRecievefromServer.readLine();
		try {
			byte[] encryptedmessage = Base64.getDecoder().decode(message);
			byte[] decryptedData = EncryptionRSA.decrypt(privateKey, encryptedmessage);
			output = new String(decryptedData);
		} catch (Exception e) {
			//TODO: handle exception
			System.out.println("error in receving thread: "+e);
		}
		///////////////////////////////////
		///THIS PART IS NOT REQUIRED AS ENCRYPTED MESSAGE COME IN ONE LINE ONLY
		// int count = 0;
		// String message;
		// while (count != length) {
		// 	message = CRSRecievefromServer.readLine();
		// 	if (message.length() + count <= length) {
		// 		output = output + out;
		// 		count = count + message.length();
		// 	} else {
		// 		output = output + message.substring(0, length - count - 1);
		// 		count = count + (length - count);
		// 	}
		// }

		m1.find();
		l1.add(m1.group(0));
		l1.add(output);
		return l1;
	}

	///////////////////////////////////
	@Override
	public void run() {
		List<String> receiveTaar;
		// String Ack;
		try {
			while (!stop) {
				try {
					receiveTaar = receiveMessage();
					if (receiveTaar.size() > 1) {
						System.out.println(receiveTaar.get(0) + ": " + receiveTaar.get(1));

						CRSAckToServer.writeBytes("RECEIVED " + receiveTaar.get(0) + "\n\n");
					} else {
						System.out.println(receiveTaar.get(0));
						CRSAckToServer.writeBytes(receiveTaar.get(0));
						break;
					}
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(e);
					clientReceiveSocket.close();
					stop = true;
				}
			}
			clientReceiveSocket.close();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			// clientReceiveSocket.close();
		}

	}
	///////////////////////////////////
}
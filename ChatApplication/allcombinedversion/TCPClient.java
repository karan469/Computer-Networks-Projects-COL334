import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
// import java.nio.charset.*;
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
import java.security.*;

import java.security.MessageDigest;
import java.security.Signature;
class Terminate extends Exception 
{ 
    public Terminate() 
    { 
    } 
} 
public class TCPClient {
	public static KeyPair generateKeyPair;
	public static byte[] publicKey;
	public static byte[] privateKey;

	public static String sign(String plainText, PrivateKey privateKey) throws Exception {
		Signature privateSignature = Signature.getInstance("SHA1withRSA");
		privateSignature.initSign(privateKey);
		privateSignature.update(Base64.getDecoder().decode(plainText));

		byte[] signature = privateSignature.sign();

		return Base64.getEncoder().encodeToString(signature);
	}

	public static boolean verify(String plainText, String signature, PublicKey publicKey) throws Exception {
		Signature publicSignature = Signature.getInstance("SHA1withRSA");
		publicSignature.initVerify(publicKey);
		publicSignature.update(Base64.getDecoder().decode(plainText));

		byte[] signatureBytes = Base64.getDecoder().decode(signature);

		return publicSignature.verify(signatureBytes);
	}

	public static String SignatureString(byte[] encryptedData, PrivateKey privateKey) throws Exception {
		String result = "";
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] shaBytes = md.digest(encryptedData);

		Signature sig = Signature.getInstance("SHA1withRSA");
		sig.initSign(privateKey);
		sig.update(shaBytes);
		byte[] signatureBytes = sig.sign();
		result = Base64.getEncoder().encodeToString(signatureBytes);
		return result;
	}

	public static void main(String[] argv) throws Exception {
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
		String mode = "";
		// nonencrypt
		// encrypt
		// encryptwithsig
		// To read strings from the terminal
		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		if (argv.length == 0) {
			mode = "nonencrypt";
		} else {
			mode = argv[0];
		}
		System.out.println(mode);
		while(!(mode.equals("encrypt")||mode.equals("encryptwithsig")||mode.equals("nonencrypt")))
		{
			System.out.println("Three mode only:\n1. nonencrypt\n2. encrypt\n3. encryptwithsig");
			System.out.println("mode: ");
			mode = inFromUser.readLine();
		}
		int portNumber1 = 6789;
		int portNumber2 = 6789;
		
		System.out.println("IP: ");
		String server = inFromUser.readLine();
		// get username
		System.out.println("Username: ");
		String username = inFromUser.readLine();
		///////////////////////////////////

		///////////////////////////////////
		// TCP Socket for sending the data//
		Socket clientSendSocket = new Socket(server, portNumber1);
		// CSS - clientSendSocket
		// Sends message to the server
		DataOutputStream CSSSendToServer = new DataOutputStream(clientSendSocket.getOutputStream());
		// Acks from the server
		BufferedReader CSSAckFromServer = new BufferedReader(new InputStreamReader(clientSendSocket.getInputStream()));
		///////////////////////////////////

		///////////////////////////////////
		// TCP Socket for recieving messages//
		Socket clientReceiveSocket = new Socket(server, portNumber2);
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

		if (mode.matches("encrypt") || mode.matches("encryptwithsig")) {
			// SEND PUBLIC KEY TO SERVER//
			CRSAckToServer
					.writeBytes("PUBLICKEY: " + Base64.getEncoder().encodeToString(TCPClient.publicKey) + "\n" + "\n");

			String ack2 = CRSRecievefromServer.readLine();
			System.out.println(ack2);
		}
		// start the client operation
		// Generate two threads for sending and recieving messages
		if (mode.matches("encrypt")) {
			SendingThreadEncrypted sendthread = new SendingThreadEncrypted(username, clientSendSocket, CSSSendToServer, CSSAckFromServer,
			inFromUser);
			Thread threadforsending = new Thread(sendthread);
			threadforsending.start();
			ReceivingThreadEncrypted receivethread = new ReceivingThreadEncrypted(username, clientReceiveSocket, CRSAckToServer,
					CRSRecievefromServer, privateKey);
			Thread threadforreceiving = new Thread(receivethread);
			threadforreceiving.start();
			threadforsending.join();
			threadforreceiving.join();
		} else if (mode.matches("encryptwithsig")) {
			SendingThreadEncryptedWithSig sendthread = new SendingThreadEncryptedWithSig(username, clientSendSocket, CSSSendToServer, CSSAckFromServer,
			inFromUser);
			Thread threadforsending = new Thread(sendthread);
			threadforsending.start();
			ReceivingThreadEncryptedWithSig receivethread = new ReceivingThreadEncryptedWithSig(username, clientReceiveSocket, CRSAckToServer,
					CRSRecievefromServer, privateKey);
			Thread threadforreceiving = new Thread(receivethread);
			threadforreceiving.start();
			threadforsending.join();
			threadforreceiving.join();
		} else {
			SendingThreadUnencrypted sendthread = new SendingThreadUnencrypted(username, clientSendSocket, CSSSendToServer, CSSAckFromServer,
				inFromUser);
			Thread threadforsending = new Thread(sendthread);
			threadforsending.start();
			ReceivingThreadUnencrypted receivethread = new ReceivingThreadUnencrypted(username,
			clientReceiveSocket, CRSAckToServer, CRSRecievefromServer);
			Thread threadforreceiving = new Thread(receivethread);
			threadforreceiving.start();
			threadforsending.join();
			threadforreceiving.join();
		}
	}
}

//////////////////////////////////////////////////////////////////
/**
 * For Unencrypted
 */

//////////////////////////////////////////////////////////////////
/**
 * SendingThread
 */

class SendingThreadUnencrypted implements Runnable {
	String username;
	Socket clientSendSocket;
	DataOutputStream CSSSendToServer;
	BufferedReader CSSAckFromServer;
	BufferedReader inFromUser;

	SendingThreadUnencrypted(String username, Socket clientSendSocket, DataOutputStream CSSSendToServer,
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
		if(sentence.matches("UNREGISTER"))
		{
			CSSSendToServer.writeBytes(sentence+"\n");
			return "";
		}
		while (!message.matcher(sentence).matches()) {
			if (sentence.equals("exit\n")) {
				return null;
			}
			System.out.println("Required Format is: @[recipient username][message]");
			sentence = inFromUser.readLine();
			if(sentence.matches("UNREGISTER"))
			{
				CSSSendToServer.writeBytes(sentence+"\n");
				return "";
			}
		}
		str = inFromUser.readLine();
		while (str.length() != 0) {
			sentence = sentence + '\n' + str;
			str = inFromUser.readLine();
		}
		System.out.printf("%s", sentence);
		Matcher m1, m2;
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(sentence);
		m2 = Pattern.compile("@[a-zA-Z0-9]+[ ]+").matcher(sentence);
		m2.find();
		m1.find();
		str = "SEND " + m1.group(0) + "\n" + "Content-length: " + sentence.substring(m2.group(0).length()).length()
				+ "\n" + "\n" + sentence.substring(m2.group(0).length()) + "\n";
		System.out.println(str);
		return str;
	}

	///////////////////////////////////
	@Override
	public void run() {
		String sendTaar = "";
		String Ack;
		try {
			while (true) {
				try {

					sendTaar = sendMessage();
					if (sendTaar.length() == 0) {
						throw new Terminate();
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
					break;
				}
			}
			clientSendSocket.close();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
		}
		System.out.println("closing Send");
	}
	///////////////////////////////////
}

/**
 * ReceivingThread
 */
class ReceivingThreadUnencrypted implements Runnable {
	String username;
	Socket clientReceiveSocket;
	DataOutputStream CRSAckToServer;
	BufferedReader CRSRecievefromServer;
	Boolean stop = false;

	ReceivingThreadUnencrypted(String username, Socket clientReceiveSocket, DataOutputStream CRSAckToServer,
			BufferedReader CRSRecievefromServer) {
		this.username = username;
		this.clientReceiveSocket = clientReceiveSocket;
		this.CRSRecievefromServer = CRSRecievefromServer;
		this.CRSAckToServer = CRSAckToServer;
	}


	List<String> receiveMessage() throws IOException {
		List<String> l1 = new ArrayList<String>();
		String output = "";

		Matcher m1, m2;
		String username;
		username = CRSRecievefromServer.readLine();
		if(username.matches("UNREGISTER"))
		{
			l1.add("UNREGISTERED");
			return l1;
		}
		// System.out.println("1st line " + username);
		if (!username.matches("FORWARD[ ]+[a-zA-Z0-9]+")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(username);
		String contentLength;
		contentLength = CRSRecievefromServer.readLine();
		// System.out.println("2nd line " + contentLength);
		if (!contentLength.matches("Content-length:[ ]+[0-9]+")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		m2 = Pattern.compile("[0-9]+").matcher(contentLength);

		String nwl = CRSRecievefromServer.readLine();// this will be \n
		// System.out.println("3rd line " + nwl);
		if (!nwl.equals("")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		m1.find();
		m2.find();
		int length = Integer.valueOf(m2.group(0));
		System.out.println("length " + length);
		int count = -1;
		String message = "";
		CRSRecievefromServer.readLine();
		while (count != length) {
			if (count >= 0)
				output = output + "\n";
			count = count + 1;
			message = CRSRecievefromServer.readLine();
			if (message.length() + count <= length) {
				output = output + message;
				count = count + message.length();
			} else {
				output = output + message.substring(0, length - count - 1);
				count = count + (length - count);
			}
		}
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
						if (receiveTaar.get(0).matches("UNREGISTERED")) {
							System.out.println(receiveTaar.get(0));
							break;
						}
						System.out.println(receiveTaar.get(0));
						CRSAckToServer.writeBytes(receiveTaar.get(0));
					}
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(e);
					clientReceiveSocket.close();
					stop = true;
					break;
				}
			}
			clientReceiveSocket.close();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			// clientReceiveSocket.close();
		}
		System.out.println("closing Receving");
	}
	///////////////////////////////////
}

//////////////////////////////////////////////////////////////////
/**
 * For Encrypted
 */

//////////////////////////////////////////////////////////////////
/**
 * SendingThread
 */
class SendingThreadEncrypted implements Runnable {
	String username;
	Socket clientSendSocket;
	DataOutputStream CSSSendToServer;
	BufferedReader CSSAckFromServer;
	BufferedReader inFromUser;

	SendingThreadEncrypted(String username, Socket clientSendSocket, DataOutputStream CSSSendToServer,
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
		if(sentence.matches("UNREGISTER"))
		{
			CSSSendToServer.writeBytes(sentence+"\n");
			return "";
		}
		while (!message.matcher(sentence).matches()) {
			if (sentence.equals("exit\n")) {
				return null;
			}
			System.out.println("Required Format is: @[recipient username][message]");
			sentence = inFromUser.readLine();
			if(sentence.matches("UNREGISTER"))
			{
				CSSSendToServer.writeBytes(sentence+"\n");
				return "";
			}
		}
		///////////////////////////
		str = inFromUser.readLine();
		while (str.length() != 0) {
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
		String mail = "";
		try {
			CSSSendToServer.writeBytes("GET PUBLICKEY " + m1.group(0) + "\n");
			String pubkey = CSSAckFromServer.readLine();
			System.out.println(pubkey);
			// inFromUser.readLine();
			if (pubkey.indexOf("Error 101 ") != -1) {
				System.out.println("FROM SERVER: " + pubkey);
				return "error";
			}
			pubkey = pubkey.split("PUBLICKEY ")[1];

			byte[] encryptedData = EncryptionRSA.encrypt(Base64.getDecoder().decode(pubkey),
					sentence.substring(m2.group(0).length()).getBytes());

			mail = Base64.getEncoder().encodeToString(encryptedData);
		} catch (Exception e) {
			// TODO: handle exception]
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
					if (sendTaar.length() == 0) {
						throw new Terminate();
					}
					if (sendTaar.equals("error")) {
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
					break;
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
class ReceivingThreadEncrypted implements Runnable {
	String username;
	Socket clientReceiveSocket;
	DataOutputStream CRSAckToServer;
	BufferedReader CRSRecievefromServer;
	Boolean stop = false;
	byte[] privateKey;

	ReceivingThreadEncrypted(String username, Socket clientReceiveSocket, DataOutputStream CRSAckToServer,
			BufferedReader CRSRecievefromServer, byte[] privateKey) {
		this.username = username;
		this.clientReceiveSocket = clientReceiveSocket;
		this.CRSRecievefromServer = CRSRecievefromServer;
		this.CRSAckToServer = CRSAckToServer;
		this.privateKey = privateKey;
	}


	List<String> receiveMessage() throws IOException {
		List<String> l1 = new ArrayList<String>();
		String output = "";

		Matcher m1, m2;
		String username;
		username = CRSRecievefromServer.readLine();
		System.out.println("1st line " + username);
		if(username.matches("UNREGISTER"))
		{
			l1.add("UNREGISTERED");
			return l1;
		}
		if (!username.matches("FORWARD[ ]+[a-zA-Z0-9]+")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(username);
		String contentLength;
		contentLength = CRSRecievefromServer.readLine();
		// System.out.println("2nd line " + contentLength);
		if (!contentLength.matches("Content-length:[ ]+[0-9]+")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		m2 = Pattern.compile("[0-9]+").matcher(contentLength);

		String nwl = CRSRecievefromServer.readLine();// this will be \n
		// System.out.println("3rd line " + nwl);
		if (!nwl.equals("")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
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
			// TODO: handle exception
			System.out.println("error in receving thread: " + e);
		}
		///////////////////////////////////
		/// THIS PART IS NOT REQUIRED AS ENCRYPTED MESSAGE COME IN ONE LINE ONLY
		// int count = 0;
		// String message;
		// while (count != length) {
		// message = CRSRecievefromServer.readLine();
		// if (message.length() + count <= length) {
		// output = output + out;
		// count = count + message.length();
		// } else {
		// output = output + message.substring(0, length - count - 1);
		// count = count + (length - count);
		// }
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
						if (receiveTaar.get(0).matches("UNREGISTERED")) {
							System.out.println(receiveTaar.get(0));
							break;
						}
						System.out.println(receiveTaar.get(0));
						CRSAckToServer.writeBytes(receiveTaar.get(0));
					}
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(e);
					clientReceiveSocket.close();
					stop = true;
					break;
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


/**
 * SendingThread
 */
class SendingThreadEncryptedWithSig implements Runnable {
	String username;
	Socket clientSendSocket;
	DataOutputStream CSSSendToServer;
	BufferedReader CSSAckFromServer;
	BufferedReader inFromUser;

	SendingThreadEncryptedWithSig(String username, Socket clientSendSocket, DataOutputStream CSSSendToServer,
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
		if(sentence.matches("UNREGISTER"))
		{
			CSSSendToServer.writeBytes(sentence+"\n");
			return "";
		}
		while (!message.matcher(sentence).matches()) {
			if (sentence.equals("exit\n")) {
				return null;
			}
			System.out.println("Required Format is: @[recipient username][message]");
			sentence = inFromUser.readLine();
			if(sentence.matches("UNREGISTER"))
			{
				CSSSendToServer.writeBytes(sentence+"\n");
				return "";
			}
		}
		str = inFromUser.readLine();
		while (str.length() != 0) {
			sentence = sentence + '\n' + str;
			str = inFromUser.readLine();
		}
		Matcher m1, m2;
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(sentence);
		m2 = Pattern.compile("@[a-zA-Z0-9]+[ ]+").matcher(sentence);
		m2.find();
		m1.find();
		///////////////////////////////////
		String mail = "";
		String signatureBytesInString = "";
		try {
			CSSSendToServer.writeBytes("GET PUBLICKEY " + m1.group(0) + "\n");
			String pubkey = CSSAckFromServer.readLine();
			System.out.println(pubkey);
			// inFromUser.readLine();
			if (pubkey.indexOf("Error 101 ") != -1) {
				System.out.println("FROM SERVER: " + pubkey);
				return "error";
			}
			pubkey = pubkey.split("PUBLICKEY ")[1];

			byte[] encryptedData = EncryptionRSA.encrypt(Base64.getDecoder().decode(pubkey),
					sentence.substring(m2.group(0).length()).getBytes());

			// MessageDigest md = MessageDigest.getInstance("SHA-256");
			// byte[] shaBytes = md.digest(encryptedData);

			// Signature sig = Signature.getInstance("SHA1withRSA");
			// sig.initSign(TCPClient.generateKeyPair.getPrivate());
			// sig.update(shaBytes);
			// byte[] signatureBytes = sig.sign();
			// signatureBytesInString = Base64.getEncoder().encodeToString(signatureBytes);
			signatureBytesInString = TCPClient.SignatureString(encryptedData, TCPClient.generateKeyPair.getPrivate());

			mail = Base64.getEncoder().encodeToString(encryptedData);
		} catch (Exception e) {
			// TODO: handle exception]
			System.out.println("error in sending thread: " + e);
		}
		///////////////////////////////////
		// System.out.println("mail lenght: "+mail.length());
		str = "SEND " + m1.group(0) + "\n" + "Signature: " + signatureBytesInString + "\n" + "Content-length: "
				+ sentence.substring(m2.group(0).length()).length() + "\n" + "\n" + mail + "\n";
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
					if (sendTaar.length() == 0) {
						throw new Terminate();
					}
					if (sendTaar.equals("error")) {
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
					break;
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
class ReceivingThreadEncryptedWithSig implements Runnable {
	String username;
	Socket clientReceiveSocket;
	DataOutputStream CRSAckToServer;
	BufferedReader CRSRecievefromServer;
	Boolean stop = false;
	byte[] privateKey;

	ReceivingThreadEncryptedWithSig(String username, Socket clientReceiveSocket, DataOutputStream CRSAckToServer,
			BufferedReader CRSRecievefromServer, byte[] privateKey) {
		this.username = username;
		this.clientReceiveSocket = clientReceiveSocket;
		this.CRSRecievefromServer = CRSRecievefromServer;
		this.CRSAckToServer = CRSAckToServer;
		this.privateKey = privateKey;
	}


	List<String> receiveMessage() throws IOException {
		List<String> l1 = new ArrayList<String>();
		String output = "";

		Matcher m1, m2;
		String username;
		username = CRSRecievefromServer.readLine();
		if(username.matches("UNREGISTER"))
		{
			l1.add("UNREGISTERED");
			return l1;
		}
		// System.out.println("1st line " + username);
		if (!username.matches("FORWARD[ ]+[a-zA-Z0-9]+")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		m1 = Pattern.compile("[a-zA-Z0-9]+").matcher(username);

		String temp1 = "";
		String senderPublicKey = "";
		temp1 = CRSRecievefromServer.readLine();
		if (temp1.indexOf("SENDER PUBLIC KEY: ") != -1) {
			// System.out.println(temp1); //DEBUG
			senderPublicKey = temp1.split("SENDER PUBLIC KEY: ")[1];
		}
		else{
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		// Sig recieving
		String signatureInStringg = "";
		String temp = "";
		temp = CRSRecievefromServer.readLine();
		if (temp.indexOf("Signature: ") != -1) {
			signatureInStringg = temp.split("Signature: ")[1];
		}
		else{
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		String contentLength;
		contentLength = CRSRecievefromServer.readLine();
		// System.out.println("2nd line " + contentLength);
		if (!contentLength.matches("Content-length:[ ]+[0-9]+")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
			return l1;
		}
		m2 = Pattern.compile("[0-9]+").matcher(contentLength);

		String nwl = CRSRecievefromServer.readLine();// this will be \n
		// System.out.println("3rd line " + nwl);
		if (!nwl.equals("")) {
			l1.add("ERROR 103 Header incomplete\n\n");
			CRSRecievefromServer.readLine();
			CRSRecievefromServer.readLine();
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
		byte[] encryptedmessage = new byte[1024];
		try {
			encryptedmessage = Base64.getDecoder().decode(message);
			byte[] decryptedData = EncryptionRSA.decrypt(privateKey, encryptedmessage);
			output = new String(decryptedData);
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("error in receving thread: " + e);
		}
		///////////////////////////////////
		/// THIS PART IS NOT REQUIRED AS ENCRYPTED MESSAGE COME IN ONE LINE ONLY
		// int count = 0;
		// String message;
		// while (count != length) {
		// message = CRSRecievefromServer.readLine();
		// if (message.length() + count <= length) {
		// output = output + out;
		// count = count + message.length();
		// } else {
		// output = output + message.substring(0, length - count - 1);
		// count = count + (length - count);
		// }
		// }

		System.out.println(temp1);
		System.out.println(temp);

		// encryptedmessage | signatureInStringg | senderPublicKey
		// To-get: bool s2.verify(s1)
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] shaBytes = md.digest(encryptedmessage);

			byte[] pubKey = Base64.getDecoder().decode(senderPublicKey);
			X509EncodedKeySpec keySpec = new X509EncodedKeySpec(pubKey);
			KeyFactory keyFactory = KeyFactory.getInstance("RSA");
			PublicKey publickey = keyFactory.generatePublic(keySpec);
			// PublicKey publickey =
			// KeyFactory.getInstance("SHA1withRSA").generatePublic(new
			// PKCS8EncodedKeySpec(pubKey));
			boolean isSame = TCPClient.verify(Base64.getEncoder().encodeToString(shaBytes), signatureInStringg,
					publickey);
			if(!isSame)
			{
				l1.add("ERROR 104 Signature Not Same\n\n");
				return l1;
			}
			System.out.println("HOLA: " + isSame);
		} catch (Exception e) {
			System.out.println(e);
		}

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
						if (receiveTaar.get(0).matches("UNREGISTERED")) {
							System.out.println(receiveTaar.get(0));
							stop = true;
							throw new Terminate();
						}
						System.out.println(receiveTaar.get(0));
						CRSAckToServer.writeBytes(receiveTaar.get(0));
					}
				} catch (Exception e) {
					// TODO: handle exception
					System.out.println(e);
					clientReceiveSocket.close();
					stop = false;
					break;
				}
			}
			clientReceiveSocket.close();

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			// clientReceiveSocket.close();
		}
		System.out.println("Exit");
	}
	///////////////////////////////////
}

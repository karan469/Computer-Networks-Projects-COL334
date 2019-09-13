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
import java.security.Provider;
import java.security.*;
import java.util.Base64;

import java.security.MessageDigest;
import java.security.Signature;

public class test {
    public static KeyPair generateKeyPair;
	public static byte[] publicKey;
	public static byte[] privateKey;
    public static void main(String argv[]) throws Exception{
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
        System.out.println("PrivateKey: " + privateKey);
        System.out.println("Private Key not encoded: " + generateKeyPair.getPrivate());
        //////////////////////////////////
       
        //1. Data is encrypted with Public Key and encryptedData is created (binary Format)
        //2. Then this encryptedData is hashed to a fixed length using SHA256.
        //3. Signature Class now sign the hashed data (i.e. shaBytes) and signatureBytes (in Binary) is created.
        //   In the previous step, signature is created with generatedPair().getPrivate(); not with generatedPair().getPrivate().getEncoded().

        String message = "Hello";
        byte[] encryptedData = EncryptionRSA.encrypt(publicKey, message.getBytes());
        String encryptedInString = Base64.getEncoder().encodeToString(encryptedData);
        System.out.println("String encrypted with a public key: " + encryptedInString);
        

        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] shaBytes = md.digest(encryptedData);
        String shaBytesInString = Base64.getEncoder().encodeToString(shaBytes);
        System.out.println("Encrypted Data with SHA 256 Hashing: " + shaBytesInString);

        Signature sig = Signature.getInstance("SHA1WithRSA");
        sig.initSign(generateKeyPair.getPrivate());
        sig.update(shaBytes);
        byte[] signatureBytes = sig.sign();
        String signatureBytesInString = Base64.getEncoder().encodeToString(signatureBytes);
        System.out.println("Signature: " + signatureBytesInString);

        // For prining all security encrytion Algorithm. But, still contains some 
        TreeSet<String> algorithms = new TreeSet<>();
        for (Provider provider : Security.getProviders())
            // System.out.println(provider);   
        for (Provider.Service service : provider.getServices())
                if (service.getType().equals("Signature"))
                    // algorithms.add(service.getAlgorithm());
                    System.out.println(provider + service.getAlgorithm());
        // for (String algorithm : algorithms)
        //     System.out.println(algorithm);
    }
}
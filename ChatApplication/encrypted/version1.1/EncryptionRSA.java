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

import javax.crypto.Cipher;

public class EncryptionRSA {

    private static final String ALGORITHM = "RSA";

    public static byte[] encrypt(byte[] publicKey, byte[] inputData) throws Exception {
        PublicKey key = KeyFactory.getInstance(ALGORITHM).generatePublic(new X509EncodedKeySpec(publicKey));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        byte[] encryptedBytes = cipher.doFinal(inputData);

        return encryptedBytes;
    }

    public static byte[] decrypt(byte[] privateKey, byte[] inputData) throws Exception {

        PrivateKey key = KeyFactory.getInstance(ALGORITHM).generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        byte[] decryptedBytes = cipher.doFinal(inputData);

        return decryptedBytes;
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);

        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");

        // 512 is keysize
        keyGen.initialize(512, random);

        KeyPair generateKeyPair = keyGen.generateKeyPair();
        return generateKeyPair;
    }

    public static void main(String[] args) throws Exception {

        KeyPair generateKeyPair = generateKeyPair();
        byte[] publicKey = generateKeyPair.getPublic().getEncoded();
        byte[] privateKey = generateKeyPair.getPrivate().getEncoded();

        byte[] encryptedData = encrypt(publicKey, "Karanaman\nakldfjakl".getBytes());

        byte[] decryptedData = decrypt(privateKey, encryptedData);

        // System.out.println(new String(encryptedData));
        // System.out.println(new String(decryptedData));

        String a = Base64.getEncoder().encodeToString(publicKey);
        byte[] b = Base64.getDecoder().decode(a);
        System.out.println(a);

        String yo = Base64.getEncoder().encodeToString("Karanaman".getBytes());
        String po = new String(decryptedData);
        // these two below are same

        String en = Base64.getEncoder().encodeToString(encryptedData);
        byte[] end = Base64.getDecoder().decode(en);
        System.out.println("matching public key");
        System.out.println(b);
        System.out.println(publicKey);

        System.out.println("See encrypted message");
        System.out.println(Base64.getEncoder().encodeToString(encryptedData));

        System.out.println("Let's see above two methods");
        System.out.println(yo);
        System.out.println(po);

        System.out.println("check if we get encrypted byte");
        System.out.println(encryptedData);
        System.out.println(end);

        System.out.println("checking lengths");
        System.out.println(po.length());
        System.out.println(en.length());

        System.out.println("Let's print encrypted data");
        System.out.println(en);

        System.out.println("somewhere on net");
        byte[] bytes = "hello world".getBytes();

        // Convert byte[] to String
        String s = Base64.getEncoder().encodeToString(bytes);

        System.out.println(s);
        //

        // System.out.println(new String(publicKey));
        // System.out.println(new String(privateKey));

    }

}

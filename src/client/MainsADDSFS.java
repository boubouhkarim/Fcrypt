package client;

import uk.ac.ic.doc.jpair.api.Pairing;
import uk.ac.ic.doc.jpair.ibe.BFCipher;
import uk.ac.ic.doc.jpair.ibe.BFCtext;
import uk.ac.ic.doc.jpair.ibe.key.BFUserPublicKey;
import uk.ac.ic.doc.jpair.pairing.Predefined;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;

public class MainsADDSFS {

    // Files.write(cipherFile.toPath(), cryptedFile.getBytes());
    private static SecretKey generateRandomKey() {
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public static String getDecrypted(String data, String Key) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        PrivateKey pk = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(Key.getBytes())));
        cipher.init(Cipher.DECRYPT_MODE, pk);
        byte[] encryptedbytes = cipher.doFinal(Base64.getDecoder().decode(data.getBytes()));
        return new String(encryptedbytes);
    }

    private List<Key> generateKey(String s) {
        Pairing e = Predefined.nssTate();
        java.security.KeyPair masterKey = BFCipher.setup(e, new Random());
        java.security.KeyPair userKey = BFCipher.extract(masterKey, s, new Random());
        //"abcd" can be replaced with an ID for user
        PublicKey uPub = userKey.getPublic();       //to use in encrypt()
        PrivateKey uPri = userKey.getPrivate();     //to use in decrypt()

        return Arrays.asList(uPub, uPri);
    }

    public static String keyToString(SecretKey secretKey) {
        byte encoded[] = secretKey.getEncoded();
        return Base64.getEncoder().encodeToString(encoded);
    }

    public static SecretKey KeyFromString(String keyStr) {
        byte[] decodedKey = Base64.getDecoder().decode(keyStr);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    static void decryptFiiile(SecretKey key, File inputFile, File outputFile) {
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, key);

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | InvalidKeyException | BadPaddingException
                | IllegalBlockSizeException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello world from PKG");
    }

    public SecretKey randomNumber(int length) throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(length);
        return keyGen.generateKey();
    }

    public String encryptFile(SecretKey key, String filename) throws NoSuchAlgorithmException, NoSuchPaddingException, UnsupportedEncodingException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher ecipher = Cipher.getInstance("AES");
        ecipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] utf8 = filename.getBytes("UTF8");
        // Encrypt
        byte[] enc = ecipher.doFinal(utf8);
        // Encode bytes to base64 to get a string
        return new sun.misc.BASE64Encoder().encode(enc);

    }

    public BFCtext encryptKeyWithPublicKey(PublicKey upub, SecretKey K) {
        byte encoded[] = K.getEncoded();
        byte msgenc[] = new byte[50];
        msgenc = Base64.getEncoder().encodeToString(encoded).getBytes();
        BFCtext msgCipher = BFCipher.encrypt((BFUserPublicKey) upub, msgenc, new Random());
        return msgCipher;
    }

    public String decryptFile(String str, SecretKey key) throws Exception {
        Cipher dcipher = Cipher.getInstance("AES");
        dcipher.init(Cipher.DECRYPT_MODE, key);
        byte[] dec = new sun.misc.BASE64Decoder().decodeBuffer(str);
        byte[] utf8 = dcipher.doFinal(dec);
        return new String(utf8, "UTF8");
    }
}

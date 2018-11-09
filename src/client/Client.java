package client;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.sun.net.ssl.internal.ssl.Provider;
import pkg.SSLServer;
import protocol.Message;
import protocol.Protocol;
import uk.ac.ic.doc.jpair.ibe.BFCipher;
import uk.ac.ic.doc.jpair.ibe.BFCtext;
import uk.ac.ic.doc.jpair.ibe.key.BFUserPrivateKey;
import uk.ac.ic.doc.jpair.ibe.key.BFUserPublicKey;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class Client {

    private SSLSocket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private DbxClientV2 client;
    private List<String> userInfo;
    private List<String> policies;
    // Dropbox.Files(this.client);

    // Constructor
    public Client() throws IOException {
        this.client = new Dropbox().getClient();
        System.out.println("Client started ...");
        this.socket = createSocket("src/pkg/truststore", "boubouh");
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    public void start() throws Exception {
        // Login+info or reLogin in case of wrong infos
        handleLogin("karim", "karim123");

        // encrypt a local file and send it to the cloud service
        uploadFile("src/assets/plaintext", "hello.txt");

        // get file form cloud service and decrypt it.
        downloadFile("src/assets/", "hello.txt");

    }

    // Private functions ------------------------------------------------------

    private SSLSocket createSocket(String keystore, String password) throws IOException {
        Security.addProvider(new Provider());
        System.setProperty("javax.net.ssl.trustStore", keystore);
        System.setProperty("javax.net.ssl.trustStorePassword", password);
        // System.setProperty("javax.net.debug", "all");
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return (SSLSocket) sslsocketfactory.createSocket(SSLServer.SERVER_ADD, SSLServer.SERVER_PORT);
    }

    private void handleLogin(String login, String password) throws IOException, ClassNotFoundException {
        while (true){
            send(Protocol.login(login, password));
            Message m = receive();

            if (m.getType() == Protocol.LOGIN && m.getError() != Protocol.ERROR_LOGIN){
                this.userInfo = m.getUserInfo();
                this.policies = m.getPolicies();
                break;
            }
        }
    }

    public void uploadFile(String local, String remote) throws IOException, DbxException, ClassNotFoundException {
        // GET keys fo given policies
        List<Key> keys = getKeysFromPKG(this.policies);

        // Generate a random K
        String randomKey = generateRandomKeyString();

        // Encrypt plainFile
        File plainFile = new File(local);
        if (!plainFile.exists()) {plainFile.createNewFile();}
        File cipherFile = new File("src/assets/ciphertext");
        if (!cipherFile.exists()) {cipherFile.createNewFile();}
        fileProcessor(Cipher.ENCRYPT_MODE, randomKey, plainFile, cipherFile);

        // Encrypt K
        BFCtext cryptedKey = encryptKeyWithPublicKey((PublicKey) keys.get(0), randomKey);

        // Set metadata
        String policiesMeta = this.policies.stream().collect(Collectors.joining(";"));
        // TODO send encrypted key instead
        setMetadata(cipherFile, "user.key", new String(objectToByte(cryptedKey)));
        setMetadata(cipherFile, "user.policies", policiesMeta);

        // Send to Clouud
        Dropbox.UPLOAD(this.client,"src/assets/ciphertext",remote);
    }

    public void downloadFile(String  folder, String remote) throws Exception {
        // Download File
        Dropbox.DOWNLOAD(this.client,folder, remote);

        // Open Encrypted file
        File file = new File(folder + remote);

        // get PK from metadata
        BFCtext cryptedKey = (BFCtext) byteToObject(getMetadata(file, "key").getBytes());

        // get file policies from metadata
        String policies = getMetadata(file, "user.policies");

        // decrypt K from KP
        List<Key> keys = getKeysFromPKG(Arrays.asList(policies.split(";")));
        String randomKey = decryptKeyWithPrivateKey(cryptedKey, (PrivateKey) keys.get(1));

        // Decrypt F with K
        File plainFile = new File("src/assets/plaintext");
        fileProcessor(Cipher.DECRYPT_MODE, randomKey, file, plainFile);

        // Remove Key/Policies metadata
        deleteMetadata(plainFile,"user.key");
        deleteMetadata(plainFile,"user.policies");
    }

    private List<Key> getKeysFromPKG(List<String> policies) throws IOException, ClassNotFoundException {
        send(Protocol.getKeys(this.userInfo.get(0), policies));
        Message m = receive();
        if(m.getType() == Protocol.SEND_KEYS && m.getError() != Protocol.ERROR_KEY){
            return Arrays.asList(m.getPublicKey(), m.getPrivateKey());
        }else{
            System.out.println("Bad request ...");
            System.exit(0);
        }
        return null;
    }

    private void setMetadata(File file, String attr, String value) throws IOException {
        UserDefinedFileAttributeView view =
                Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);

        view.write(attr, Charset.defaultCharset().encode(value));
    }

    private String getMetadata(File file, String attr) throws IOException {
        UserDefinedFileAttributeView view =
                Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);
        ByteBuffer buf = ByteBuffer.allocate(view.size(attr));
        view.read(attr, buf);
        buf.flip();

        return Charset.defaultCharset().decode(buf).toString();
    }

    private void deleteMetadata(File file, String attr) throws IOException {
        UserDefinedFileAttributeView view =
                Files.getFileAttributeView(file.toPath(), UserDefinedFileAttributeView.class);

        view.delete(attr);
    }

    private void send(Message m) throws IOException {
        this.oos.flush(); // Empty buffer
        this.oos.writeObject(m);
        this.oos.reset(); // reset Object size
    }

    private Message receive() throws IOException, ClassNotFoundException {
        return  (Message) this.ois.readObject();
    }

    private void fileProcessor(int cipherMode, String key, File inputFile, File outputFile) {
        try {
            Key secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(cipherMode, secretKey);

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

    private String generateRandomKeyString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 16) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }

    private BFCtext encryptKeyWithPublicKey(PublicKey upub, String randomKey) {
        byte encoded[] = randomKey.getBytes();
        byte msgenc[] = Base64.getEncoder().encodeToString(encoded).getBytes();
        BFCtext msgCipher = BFCipher.encrypt((BFUserPublicKey) upub, msgenc, new Random());
        return msgCipher;
    }

    public String decryptKeyWithPrivateKey(BFCtext msgCipher,PrivateKey uPri){
        byte msgdec[] = new byte[50];
        msgdec = BFCipher.decrypt(msgCipher, (BFUserPrivateKey) uPri);
        String ok="";
        for (int j = 0; j < msgdec.length; j++) {
            ok+=(char) msgdec[j]+"";
        }
        return ok;
    }

    private byte[] objectToByte(Object o) throws IOException {
        ByteArrayOutputStream bStream = new ByteArrayOutputStream();
        ObjectOutput oo = new ObjectOutputStream(bStream);
        oo.writeObject(o);
        oo.close();
        return bStream.toByteArray();
    }

    private Object byteToObject(byte[] b) throws IOException, ClassNotFoundException {
        ObjectInputStream iStream = new ObjectInputStream(new ByteArrayInputStream(b));
        Object o = iStream.readObject();
        iStream.close();
        return o;
    }

    // MainsADDSFS function ----------------------------------------------------------
    public static void main(String[] args) {
        try {
            new Client().start();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
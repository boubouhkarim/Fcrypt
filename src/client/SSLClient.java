package client;

import com.sun.net.ssl.internal.ssl.Provider;
import pkg.SSLServer;
import protocol.Message;
import protocol.Protocol;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Security;
import java.util.Arrays;

/**
 * @author Joe Prasanna Kumar
 * This program simulates a client socket program which communicates with the SSL Server
 * <p>
 * Algorithm:
 * 1. Determine the SSL Server Name and port in which the SSL server is listening
 * 2. Register the JSSE provider
 * 3. Create an instance of SSLSocketFactory
 * 4. Create an instance of SSLSocket
 * 5. Create an OutputStream object to write to the SSL Server
 * 6. Create an InputStream object to receive messages back from the SSL Server
 */

public class SSLClient {

    private SSLSocket socket;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public SSLClient() throws IOException {
        this.socket = createSocket("src/pkg/truststore", "boubouh");
        System.out.println("here2");
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        System.out.println("here");
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    public static void main(String[] args) {
        try {
            new SSLClient().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        System.out.println("Client started ...");
        try {

            send(Protocol.getKeys("karim", Arrays.asList("2018", "DNA")));

            // job done
            end();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private SSLSocket createSocket(String keystore, String password) throws IOException {
        Security.addProvider(new Provider());
        System.setProperty("javax.net.ssl.trustStore", keystore);
        System.setProperty("javax.net.ssl.trustStorePassword", password);
        // System.setProperty("javax.net.debug", "all");
        SSLSocketFactory sslsocketfactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        return (SSLSocket) sslsocketfactory.createSocket(SSLServer.SERVER_ADD, SSLServer.SERVER_PORT);
    }

    public void send(Message m) throws IOException {
        this.oos.flush(); // Empty buffer
        this.oos.writeObject(m);
        this.oos.reset(); // reset Object size
    }

    public Message receive(int type) throws IOException, ClassNotFoundException {
        Message m = (Message) this.ois.readObject();
        return m;
    }

    private void end() throws IOException {
        this.oos.close();
        this.ois.close();
        this.socket.close();
        System.out.println("Client closed closed");
    }
}
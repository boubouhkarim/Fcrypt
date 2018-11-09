package pkg;

import com.sun.net.ssl.internal.ssl.Provider;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.Security;

// Steps
// keystore <=> private keys
// ====================================
// keytool -genkey -alias pkgserver -keyalg RSA -validity 365 -keystore keystore
// keytool -export -alias pkgserver -keystore keystore -file pkgserver.cer
// keytool -printcert -file pkgserver.cer
// --------------------------------------------------------------------------------------

// truststore <=> public certificates.
// ====================================
// keytool -import -alias pkgserver -file pkgserver.cer -keystore truststore

public class SSLServer {
    public static final int SERVER_PORT = 4443;
    public static final String SERVER_ADD = "localhost";

    private SSLServerSocket socket;

    public SSLServer() throws IOException {
        this.socket = createServerSocket("src/stores/keystore", "boubouh");
    }

    public void start() throws IOException {
        System.out.println("Server ready for connections ...");
        while (true) new ServerThread((SSLSocket) socket.accept()).start();
    }

    private SSLServerSocket createServerSocket(String keystore, String password) throws IOException {
        Security.addProvider(new Provider());
        System.setProperty("javax.net.ssl.keyStore", keystore);
        System.setProperty("javax.net.ssl.keyStorePassword", password);
        // System.setProperty("javax.net.debug", "all");
        SSLServerSocketFactory sslServerSocketfactory = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        return (SSLServerSocket) sslServerSocketfactory.createServerSocket(SERVER_PORT);
    }

    public static void main(String[] args) {
        try {
            new SSLServer().start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

package pkgauth;

import com.sun.net.ssl.internal.ssl.Provider;
import pkg.SQLiteJDBC;
import pkg.ServerThread;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.security.Security;
import java.sql.SQLException;

/**
 * @author Joe Prasanna Kumar
 * This program simulates an SSL Server listening on a specific port for client requests
 * <p>
 * Algorithm:
 * 1. Regsiter the JSSE provider
 * 2. Set System property for keystore by specifying the keystore which contains the server certificate
 * 3. Set System property for the password of the keystore which contains the server certificate
 * 4. Create an instance of SSLServerSocketFactory
 * 5. Create an instance of SSLServerSocket by specifying the port to which the SSL Server socket needs to bind with
 * 6. Initialize an object of SSLSocket
 * 7. Create InputStream object to read data sent by clients
 * 8. Create an OutputStream object to write data back to clients.
 */

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
        this.socket = createServerSocket("src/pkg/keystore", "boubouh");
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
            // Create a database
            SQLiteJDBC.creatDB();
            // Start Server
            new SSLServer().start();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}

package pkg;

import protocol.Message;
import protocol.Protocol;
import uk.ac.ic.doc.jpair.api.Pairing;
import uk.ac.ic.doc.jpair.ibe.BFCipher;
import uk.ac.ic.doc.jpair.pairing.Predefined;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ServerThread extends Thread {
    private SSLSocket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;

    public ServerThread(SSLSocket socket) throws IOException {
        this.socket = socket;
        this.oos = new ObjectOutputStream(socket.getOutputStream());
        this.ois = new ObjectInputStream(socket.getInputStream());
    }

    @Override
    public void run() {
        System.out.println("new Client ...");

        while (true) {
            try {
                Message m = receive();
                switch (m.getType()) {
                    case Protocol.LOGIN:
                        handleLogin(m);
                        break;
                    case Protocol.GET_KEYS:
                        handleKeysRequest(m);
                    default:
                        System.out.println("Unknown Message");
                        continue;
                }


            } catch (IOException | ClassNotFoundException | SQLException e) {
                e.printStackTrace();
                try {
                    end();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }

    }

    private void handleLogin(Message m) throws SQLException, ClassNotFoundException, IOException {
        List<String> userInfo = new ArrayList<>();
        List<String> policies = new ArrayList<>();

        if (SQLiteJDBC.checkUser(m.getUsername(), m.getPassword())) {

            String sql = "SELECT ID ,USERNANE FROM users WHERE USERNAME = ? and PASSWORD = ?";
            Connection c = null;
            PreparedStatement pstmt = null;
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection("jdbc:sqlite:Fcrypt.db");
            pstmt = c.prepareStatement(sql);
            pstmt.setString(1, m.getUsername());
            pstmt.setString(2, m.getPassword());

            ResultSet rs = pstmt.executeQuery();
            int userId = 0;
            while (rs.next()) {
                userId = rs.getInt("ID");
                userInfo.add(rs.getString("USERNAME"));
            }
            sql = "SELECT POLICY  FROM policies WHERE ID_USERS = ?";
            pstmt = c.prepareStatement(sql);
            pstmt.setInt(1, userId);
            while (rs.next()) {
                policies.add(rs.getString("POLICY"));
            }

            // Send infos to client
            send(Protocol.loginCheck(userInfo, policies, 0));
        } else {
            send(Protocol.loginCheck(null, null, Protocol.ERROR_LOGIN));
            System.out.println("Wrong login");
        }


    }

    private void end() throws IOException {
        this.oos.close();
        this.ois.close();
        this.socket.close();
        System.out.println("Connection closed");
        Thread.currentThread().interrupt();
    }

    public void send(Message m) throws IOException {
        this.oos.flush(); // Empty buffer
        this.oos.writeObject(m);
        this.oos.reset(); // reset Object size
    }

    public void handleKeysRequest(Message m) throws IOException {
        // check permission to have back the private key
        String identity = getIdentity(m);
        System.out.println(identity);

        // generate keys from identity (ID + P)
        List<Key> keys = generateKey(identity);

        // return back the keypair
        send(Protocol.sendKeys((PublicKey) keys.get(0), (PrivateKey) keys.get(1), 0));
    }

    private String getIdentity(Message m) {
        String identity = "";
        identity += m.getUsername() + "|";
        identity += m.getPolicies().stream().collect(Collectors.joining("|"));
        return identity;
    }

    public Message receive() throws IOException, ClassNotFoundException {
        Message m = (Message) this.ois.readObject();
        return m;
    }

    public List<Key> generateKey(String s) {
        Pairing e = Predefined.nssTate();
        java.security.KeyPair masterKey = BFCipher.setup(e, new Random());
        java.security.KeyPair userKey = BFCipher.extract(masterKey, s, new Random());
        PublicKey uPub = userKey.getPublic();       //to use in encrypt()
        PrivateKey uPri = userKey.getPrivate();     //to use in decrypt()
        return Arrays.asList(uPub, uPri);
    }

}

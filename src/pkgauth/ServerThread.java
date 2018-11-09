package pkgauth;

import pkg.SQLiteJDBC;
import protocol.Message;
import protocol.Protocol;

import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.sql.*;
import java.util.List;

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
                List<String> userInfo = null;
                List<String> policies = null;
                Message m = receive();

                if (m.getType() == Protocol.LOGIN) {

                    boolean ok = SQLiteJDBC.checkUser(m.getUsername(), m.getPassword());
                    if (ok == true) {
                        String sql = "SELECT ID ,USERNANE FROM users WHERE USERNAME = ?";
                        Connection c = null;
                        PreparedStatement pstmt = null;
                        Class.forName("org.sqlite.JDBC");
                        c = DriverManager.getConnection("jdbc:sqlite:Fcrypt.db");
                        pstmt = c.prepareStatement(sql);
                        pstmt.setString(1, m.getUsername());

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
                    }
                } else {
                    System.out.println("wrong login");
                    continue;
                }
                send(Protocol.loginCheck(userInfo, policies, 0));
                // job done
                end();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
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

    public Message receive() throws IOException, ClassNotFoundException {
        Message m = (Message) this.ois.readObject();
        return m;
    }

}

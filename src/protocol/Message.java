package protocol;

import java.io.Serializable;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;

public class Message implements Serializable {

    private int type;
    private int error;
    private String username;
    private String password;
    private List<String> userInfo;
    private List<String> policies;
    private PublicKey publicKey;
    private PrivateKey privateKey;

    // LOGIN
    public Message(int type, String username, String password) {
        this.type = type;
        this.username = username;
        this.password = password;
    }

    // LOGIN_CHECK
    public Message(int type, int error, List<String> userInfo, List<String> policies) {
        this.type = type;
        this.error = error;
        this.userInfo = userInfo;
        this.policies = policies;
    }

    // GET_KEYS
    public Message(int type, String username, List<String> policies) {
        this.type = type;
        this.username = username;
        this.policies = policies;
    }

    // SEND_KEYS
    public Message(int type, int error, PublicKey publicKey, PrivateKey privateKey) {
        this.type = type;
        this.error = error;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(List<String> userInfo) {
        this.userInfo = userInfo;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }
}



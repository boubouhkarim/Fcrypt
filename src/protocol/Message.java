package protocol;

import java.util.List;

public class Message {

    private int type;
    private int error;
    private String username;
    private String password;
    private List<String> userInfo;
    private List<String> policies;
    private String publicKey;
    private String privateKey;

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
    public Message(int type, int error, String publicKey, String privateKey) {
        this.type = type;
        this.error = error;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }
}



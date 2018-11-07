package protocol;

import java.util.List;

public class Protocol {

    // Types
    public static final int LOGIN = 1;
    public static final int USER_INFO = 2;
    public static final int IDENTITY = 3;
    public static final int GET_KEYS = 4;
    public static final int SEND_KEYS = 5;

    // Errors
    public static final int ERROR = 100;
    public static final int ERROR_LOGIN = 101;
    public static final int ERROR_KEY = 102;

    public static Message login(String username, String password){
        return new Message(Protocol.LOGIN, username, password);
    }

    public static Message loginCheck(List<String> userInfo, List<String> policies, int error){
        return new Message(Protocol.IDENTITY, error, userInfo, policies);
    }

    public static Message getKeys(String username, List<String> policies){
        return new Message(Protocol.GET_KEYS, username, policies);
    }

    public static Message sendKeys(String publicKey, String privateKey, int error){
        return new Message(Protocol.SEND_KEYS, error, publicKey, privateKey);
    }
}

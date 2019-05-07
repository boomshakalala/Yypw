package cn.sinata.rxnetty;

/**
 * netty消息监听器
 */

public interface OnMessageListener {
    void onMessageReceived(String message);
}

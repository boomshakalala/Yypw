package cn.sinata.rxnetty;

/**
 * netty发送消息监听器
 */

public interface OnSendListener {
    void onSend(String s);
}

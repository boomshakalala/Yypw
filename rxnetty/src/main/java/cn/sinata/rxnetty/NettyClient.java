package cn.sinata.rxnetty;

import android.app.job.JobScheduler;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.ArrayList;

/**
 * 使用的是rxjava1。而不是rxjava2。所以尽量不要在主项目中使用rxjava1相关类。以免交叉。不好维护修改。
 */

public class NettyClient {

    public boolean isStop = false;
    private Context mContext;
    private ArrayList<OnMessageListener> listeners ;
    private OnSendListener sendListener;
    private OnConnectListener connectListener;
    private OnCheckListener onCheckListener;

    public void init(Context context,String server,int port) {
        init(context,server,port,false);
    }

    public void init(Context context,String server,int port,boolean isStartForeground) {
        mContext = context.getApplicationContext();
        Config.SOCKET_SERVER = server;
        Config.SOCKET_PORT = port;
        Config.isStartForeground= isStartForeground;
    }

    public OnConnectListener getConnectListener() {
        return connectListener;
    }

    private static final class Singleton {
        private final static NettyClient INSTANCE = new NettyClient();
    }

    public static NettyClient getInstance() {
        return Singleton.INSTANCE;
    }

    ArrayList<OnMessageListener> getListeners() {
        return listeners;
    }



    /**
     * 添加消息监听
     * @param listener
     */
    public void addOnMessageListener(OnMessageListener listener) {
        if (listeners == null) {
            listeners = new ArrayList<>();
        }
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * 连接完成监听
     * @param listener
     */
    public void setOnConnectListener(OnConnectListener listener) {
        this.connectListener = listener;
    }

    /**
     *  移除消息监听
     * @param listener
     */
    public void removeOnMessageListener(OnMessageListener listener) {
        if (listeners == null) {
            return;
        }
        if (listener != null && listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    void setSendListener(OnSendListener listener) {
        this.sendListener = listener;
    }

    void setOnCheckListener(OnCheckListener listener) {
        this.onCheckListener = listener;
    }

    /**
     * 发送消息给服务器
     * @param msg
     */
    public void sendMessage(String msg) {
        if (sendListener != null) {
            sendListener.onSend(msg);
        }
    }

    /**
     * 检测netty连接状况
     */
    public void checkNettyState() {
        if (onCheckListener != null) {
            onCheckListener.doCheck();
        }
    }

    public void startService() {
        if (mContext == null) {
            return;
        }
        if (!isStop) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  {
                try {
                    Intent intent = new Intent(this.getContext(), NJobService.class);
                    this.mContext.stopService(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                this.mContext.stopService(new Intent(this.mContext, CoreService.class));
            }
        }
        isStop = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  {
            try {
                Intent intent = new Intent(this.getContext(), NJobService.class);
                this.mContext.startService(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.mContext.startService(new Intent(this.mContext, CoreService.class));
        }
    }

    public void stopService() {
        if (mContext == null) {
            return;
        }
        isStop = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)  {
            try {
                JobScheduler jobScheduler = (JobScheduler) this.mContext.getSystemService(Context.JOB_SCHEDULER_SERVICE);
                if(jobScheduler!=null)
                    jobScheduler.cancel(11);
                this.mContext.stopService(new Intent(this.mContext, NJobService.class));
                this.mContext.stopService(new Intent(this.mContext, CoreService.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.mContext.stopService(new Intent(this.mContext, CoreService.class));
        }
    }

    private Context getContext() {
        return mContext;
    }
}

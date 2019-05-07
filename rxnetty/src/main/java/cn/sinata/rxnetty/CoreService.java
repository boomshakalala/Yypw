package cn.sinata.rxnetty;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import cn.sinata.rxnetty.netStatus.NetChangeObserver;
import cn.sinata.rxnetty.netStatus.NetStateReceiver;
import cn.sinata.rxnetty.netStatus.NetUtils;
import cn.sinata.rxnetty.pipeline.LengthFieldConfigurator;
import io.netty.buffer.ByteBuf;
import io.reactivex.netty.RxNetty;
import io.reactivex.netty.channel.ObservableConnection;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * 长链接基本service。修改为监听器（接口方式）实现。
 */
public class CoreService extends Service {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    void init() {
        /*
         * 注册发送消息事件监听。
         */
        NettyClient.getInstance().setSendListener(new OnSendListener() {
            @Override
            public void onSend(String s) {
                //先检查通道状态，如果断开，则重连。
                checkState();
                Observable<Void> send = send(s + "\n");
//                Observable<Void> send = send(s);
                if (send != null) {
                    send.subscribe(new Subscriber<Void>() {
                        @Override
                        public void onCompleted() {
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onNext(Void aVoid) {
                        }
                    });
                }
            }
        });
        initCheckOb();
    }

    private void checkState() {
        if (mConnection == null || mConnection.getChannel() == null
                || !mConnection.getChannel().isActive()
                || !mConnection.getChannel().isWritable()) {
            reConnect();
        }
    }

    void initCheckOb() {
        NettyClient.getInstance().setOnCheckListener(new OnCheckListener() {
            @Override
            public void doCheck() {
                checkState();
            }
        });
    }

    protected NetChangeObserver mNetChangeObserver = null;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        init();

        NetStateReceiver.registerNetworkStateReceiver(this);
        mNetChangeObserver = new NetChangeObserver() {
            @Override
            public void onNetConnected(NetUtils.NetType type) {
                super.onNetConnected(type);
                checkState();
            }
        };
        NetStateReceiver.registerObserver(mNetChangeObserver);
        if (Config.isStartForeground) {
            startForeground(Config.NOTIFICATION_ID,getNotification());
        }
        return START_NOT_STICKY;
    }

    private Notification getNotification(){

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this,"101");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mBuilder.setShowWhen(true);
        }
        mBuilder.setAutoCancel(false);
        int icon = this.getResources().getIdentifier("ic_launcher","mipmap",this.getPackageName());
        mBuilder.setSmallIcon(icon);
        mBuilder.setContentText("正在运行");
        int name = this.getResources().getIdentifier("app_name","string",this.getPackageName());
        mBuilder.setContentTitle(getString(name));
        return mBuilder.build();
    }

    private boolean isDestroy = false;

    @Override
    public void onDestroy() {
        super.onDestroy();
        isDestroy = true;
        NetStateReceiver.unRegisterNetworkStateReceiver(this);
        if (subscriber != null && !subscriber.isUnsubscribed()) {
            subscriber.unsubscribe();
        }
        if (receiveSub != null && !receiveSub.isUnsubscribed()) {
            receiveSub.unsubscribe();
        }
        if (mConnection != null) {
            mConnection.close();
        }
    }

    private void connectServer() {

        if (NetUtils.isNetworkAvailable(this)) {
            connectionSub();
            connect(Config.SOCKET_SERVER, Config.SOCKET_PORT)
                    .subscribeOn(Schedulers.io())
                    .subscribe(subscriber);
        }

    }

    private Subscriber<Boolean> subscriber;

    private void connectionSub() {
        if (subscriber != null) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.unsubscribe();
            }
            subscriber = null;
        }
        subscriber = new Subscriber<Boolean>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                reConnect();
            }

            @Override
            public void onNext(Boolean aBoolean) {

                Observable<ByteBuf> observable = receive();
                if (observable != null) {
                    if (receiveSub == null || receiveSub.isUnsubscribed()) {
                        receiveSub = null;
                        initReceiveOb();
                    }
                    observable.subscribe(receiveSub);
                }
            }
        };

    }

    private Subscriber<ByteBuf> receiveSub;

    private void initReceiveOb() {
        receiveSub = new Subscriber<ByteBuf>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {
                reConnect();
            }

            @Override
            public void onNext(ByteBuf byteBuf) {
                String s = byteBuf.toString(Charset.forName("utf-8"));
                boolean contains = s.contains("�");
                if (contains) {
                    //乱码了
                    s = byteBuf.toString(Charset.forName("gbk"));
                }
                ArrayList<OnMessageListener> listeners = NettyClient.getInstance().getListeners();
                if (listeners != null) {
                    for (OnMessageListener listener : listeners) {
                        if (listener != null) {
                            listener.onMessageReceived(s);
                        }
                    }
                }
            }
        };
    }


    private void reConnect() {
        if (isDestroy) {
            return;
        }
        //reconnect
        Observable.timer(3, TimeUnit.SECONDS).subscribe(new Action1<Long>() {
            @Override
            public void call(Long aLong) {
                if (mConnection != null) {
                    mConnection.close();
                    mConnection = null;
                }
                connectServer();
            }
        });

    }


    ObservableConnection<ByteBuf, ByteBuf> mConnection;

    public Observable<Boolean> connect(final String url, final int port) {
        return RxNetty.createTcpClient(url, port, new LengthFieldConfigurator())
                .connect()
                .flatMap(new Func1<ObservableConnection<ByteBuf, ByteBuf>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(ObservableConnection<ByteBuf, ByteBuf> byteBufByteBufObservableConnection) {
                        mConnection = byteBufByteBufObservableConnection;
                        OnConnectListener listener = NettyClient.getInstance().getConnectListener();
                        if (listener != null) {
                            listener.onConnected();
                        }
                        return Observable.create(new Observable.OnSubscribe<Boolean>() {
                            @Override
                            public void call(Subscriber<? super Boolean> subscriber) {
                                subscriber.onNext(true);
                            }
                        });
                    }
                });
    }

    public Observable<ByteBuf> receive() {
        if (mConnection != null) {
            return mConnection.getInput();
        }
        return null;
    }

    public Observable<Void> send(String s) {
        if (mConnection != null) {
            return mConnection.writeBytesAndFlush(s.getBytes());
        }
        return null;
    }
}
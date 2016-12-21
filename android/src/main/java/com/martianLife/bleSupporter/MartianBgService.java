package com.martianLife.bleSupporter;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import com.facebook.react.bridge.ReadableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.martianLife.domain.Key;
import com.martianLife.domain.Token;
import com.martianLife.utils.LockManager;
import com.martianLife.utils.PeripheralConnector2;
import org.json.JSONArray;

import java.util.List;

/**
 * Created by Administrator on 2016/12/17.
 */
public class MartianBgService extends Service implements LockManager.LockEventListener {
    public final static String EXTRA_AUTO_CONNECT_KEYS = "com.martianLife.bleSupporter.MartianBgService.EXTRA_AUTO_CONNECT_KEYS";
    public final static String EXTRA_TOKEN = "com.martianLife.bleSupporter.MartianBgService.EXTRA_TOKEN";
    public final static String EXTRA_CLIENT_INFO = "com.martianLife.bleSupporter.MartianBgService.EXTRA_CLIENT_INFO";

    private boolean mStarted;
    private NotificationManager mNotificationManager;
    // private Notification mNotification;
    // private NotificationCompat.Builder mBuilder;
    private static int ONGOING_NOTIFICATION_ID = 0x12345;
    private LockManager mLockManager;

    @Override
    public void onCreate() {
        mStarted = false;
        super.onCreate();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mStarted == true) {
            return START_STICKY;
        }
        mStarted = true;

        if(mNotificationManager == null) {
            mNotificationManager = (NotificationManager) getSystemService(android.content.Context.NOTIFICATION_SERVICE);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_listener)
                .setContentTitle(getString(R.string.bls_title))
                .setContentText(getString(R.string.bls_listening));

        Notification notification = mBuilder.build();
        startForeground(ONGOING_NOTIFICATION_ID, notification);

        //
        String autoConnectKeysString = intent.getStringExtra(EXTRA_AUTO_CONNECT_KEYS);
        String tokenString = intent.getStringExtra(EXTRA_TOKEN);
        String clientInfoString = intent.getStringExtra(EXTRA_CLIENT_INFO);

        Gson gson = new Gson();
        Token token = gson.fromJson(tokenString, Token.class);
        List<Key> autoConnectKeys = gson.fromJson(autoConnectKeysString, new TypeToken<List<Key>>(){}.getType());

        mLockManager = new LockManager(this, autoConnectKeys, this);
        mLockManager.startWorking();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mLockManager.stopWorking();
        mStarted = false;

        stopForeground(true);
        // mNotification = null;
    }

    @Override
    public void onError(Key key, String error) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_listener)
                .setContentTitle(key.getName())
                .setContentText(error);

        Notification notification = mBuilder.build();
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
    }

    @Override
    public void onLockConnectedChanged(Key key, boolean connected) {
        Notification notification;
        if (connected) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_listener)
                    .setContentTitle(key.getName())
                    .setContentText("正在为您开锁");
            notification = mBuilder.build();
        }
        else {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_listener)
                    .setContentTitle(getString(R.string.bls_title))
                    .setContentText(getString(R.string.bls_listening));

            notification = mBuilder.build();
        }
        mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);

    }

    @Override
    public void onLockStateChanged(Key key, byte state) {
        if (state == PeripheralConnector2.LOCK_STATE_UNLOCKED) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_listener)
                    .setContentTitle(key.getName())
                    .setContentText("门锁已为您打开，请转动把手开门");

            Notification notification = mBuilder.build();
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
        }
    }

    @Override
    public void onDoorStateChanged(Key key, byte state) {
        if (state == PeripheralConnector2.DOOR_STATE_OPENED) {
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this).setSmallIcon(R.mipmap.ic_listener)
                    .setContentTitle(key.getName())
                    .setContentText("欢迎回家");

            Notification notification = mBuilder.build();
            mNotificationManager.notify(ONGOING_NOTIFICATION_ID, notification);
        }
    }
}

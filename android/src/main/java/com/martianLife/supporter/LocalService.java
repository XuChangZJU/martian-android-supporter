package com.martianLife.supporter;

import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

//import com.martianLife.android.supporter.ProcessService;

/**
 * Created by Administrator on 2017/2/9.
 */
public class LocalService extends Service {
    private  myBinder2 myBinder2;
    private  MyBinder binder;
    private MyConn conn;


    @Override
    public void onCreate() {
        super.onCreate();
        binder=new MyBinder();
        if (conn==null){
            conn=new MyConn();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }





    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocalService.this.bindService(new Intent(LocalService.this, RemoteService.class), conn, Context.BIND_IMPORTANT);

        return super.onStartCommand(intent, flags, startId);
    }

    class MyBinder extends  ProcessService.Stub{


        @Override
        public String getServiceName() throws RemoteException {
            return "LocalService";
        }
    }

    class MyConn implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.i("INFO","remoteService已经连接");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(LocalService.this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("remoteService被中止")
                            .setContentText("即将重启");

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());

            LocalService.this.startService(new Intent(LocalService.this, RemoteService.class));
            LocalService.this.bindService(new Intent(LocalService.this, RemoteService.class), conn, Context.BIND_IMPORTANT);
        }
    }
    class myBinder2 extends Binder {

        public LocalService getService(){
            return LocalService.this;
        }

    }

}

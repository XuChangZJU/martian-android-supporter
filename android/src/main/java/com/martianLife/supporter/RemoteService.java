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
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;
//import com.martianLife.android.supporter.ProcessService;

/**
 * Created by Administrator on 2017/2/9.
 */
public class RemoteService extends Service {
    private MyBinder binder;
    private  myBinder2 myBinder2;
    private  Myconn conn;

    @Override
    public void onCreate() {
        super.onCreate();
        binder=new MyBinder();
        if (conn==null){

            conn=new Myconn();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        RemoteService.this.bindService(new Intent(RemoteService.this,LocalService.class),conn, Context.BIND_IMPORTANT);
        return super.onStartCommand(intent, flags, startId);
    }

    class MyBinder extends  ProcessService.Stub{


        @Override
        public String getServiceName() throws RemoteException {
            return "RemoteService";
        }
    }

    class  Myconn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            Log.i("INFO","localService连接成功");
        }


        //�ڰ󶨶Ͽ���ʱ�� �����˷���
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(RemoteService.this)
                            .setSmallIcon(R.mipmap.ic_launcher)
                            .setContentTitle("localService被中止")
                            .setContentText("即将重启");

            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(1, mBuilder.build());

            RemoteService.this.startService(new Intent(RemoteService.this, LocalService.class));
            RemoteService.this.bindService(new Intent(RemoteService.this, LocalService.class),conn, Context.BIND_IMPORTANT);

        }
    }
    class myBinder2 extends Binder {

        public RemoteService getService(){
            return RemoteService.this;
        }

    }
}

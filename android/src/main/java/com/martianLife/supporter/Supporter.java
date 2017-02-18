package com.martianLife.supporter;

import android.annotation.TargetApi;
import android.content.Intent;
import com.facebook.react.bridge.*;


/**
 * Created by Administrator on 2016/2/28.
 */
@TargetApi(18)
public class Supporter extends ReactContextBaseJavaModule {

    private static final String TAG = Supporter.class.getSimpleName();

    public Supporter(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "MartianAndroidSupporter";
    }

    @ReactMethod
    public void startBgService() {
        getReactApplicationContext().startService(new Intent(getCurrentActivity(), LocalService.class));
        getReactApplicationContext().startService(new Intent(getCurrentActivity(), RemoteService.class));
    }
}

package com.martianLife.bleSupporter;

import android.annotation.TargetApi;
import android.content.Intent;
import com.facebook.react.bridge.*;
import com.martianLife.domain.Key;
import com.martianLife.domain.Lock;
import com.martianLife.domain.Token;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Administrator on 2016/2/28.
 */
@TargetApi(18)
public class BleSupporter extends ReactContextBaseJavaModule {

    private static final String TAG = BleSupporter.class.getSimpleName();

    public BleSupporter(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "BleSupporter";
    }

    @ReactMethod
    public void startListener(ReadableMap params) {
        Intent intent = new Intent(getReactApplicationContext(), MartianBgService.class);
        String autoConnectKeys = params.getString("autoConnectKeys");
        String token = params.getString("token");
        String clientInfo = params.getString("clientInfo");

        intent.putExtra(MartianBgService.EXTRA_AUTO_CONNECT_KEYS, autoConnectKeys);
        intent.putExtra(MartianBgService.EXTRA_CLIENT_INFO, clientInfo);
        intent.putExtra(MartianBgService.EXTRA_TOKEN, token);

        getReactApplicationContext().startService(intent);
    }


    @ReactMethod
    public void stopListener() {
        Intent intent = new Intent(getCurrentActivity(), MartianBgService.class);
        getReactApplicationContext().stopService(intent);
    }
}

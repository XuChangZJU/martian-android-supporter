package com.martianLife.bleSupporter;

import android.annotation.TargetApi;
import android.bluetooth.*;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import com.facebook.react.bridge.*;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Created by Administrator on 2016/2/28.
 */
@TargetApi(18)
public class BleSupporter extends ReactContextBaseJavaModule {

    private static final String TAG = BleSupporter.class.getSimpleName();

}

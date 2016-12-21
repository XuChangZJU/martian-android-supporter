package com.martianLife.utils;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import com.martianLife.domain.Key;
import com.martianLife.domain.Version;

import java.util.*;

/**
 * Created by Administrator on 2016/12/20.
 */
public class LockManager implements PeripheralConnector2.PcEventListener {
    public interface LockEventListener {
        void onError(Key key, String error);

        void onLockConnectedChanged(Key key, boolean connected);

        void onLockStateChanged(Key key, byte state);

        void onDoorStateChanged(Key key, byte state);
    }

    private Map<String, Key> keyMap;
    private Map<String, PeripheralConnector2> connector2Map;
    private boolean isWorking;
    private LockEventListener mListener;

    public LockManager(Context context, List<Key> keys, LockEventListener listener) {
        final BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        this.keyMap = new HashMap<>(keys.size());
        this.connector2Map = new HashMap<>(keys.size());

        Iterator<Key> iterator = keys.iterator();
        while (iterator.hasNext()) {
            Key key = iterator.next();
            assert (key.getLock().getCkwExpiredTime() != null);
            assert (key.getLock().getConstantKeyWord() != null);
            keyMap.put(key.getLock().getAddress(), key);
            PeripheralConnector2 connector2 = new PeripheralConnector2(context, adapter, this);
            connector2Map.put(key.getLock().getAddress(), connector2);
        }

        this.isWorking = false;
        this.mListener = listener;
    }

    public void startWorking() {
        this.isWorking = true;
        Iterator<Map.Entry<String, PeripheralConnector2>> iterator = connector2Map.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, PeripheralConnector2> entry = iterator.next();
            entry.getValue().connect(entry.getKey());
        }
    }

    public void stopWorking() {
        this.isWorking = false;
    }

    @Override
    public void onReceiveLockWord(byte[] lockWord, boolean bDisposable, PeripheralConnector2 connector2) {

    }

    @Override
    public void onReceiveKeyVerificationResult(byte result, boolean bDisposable, PeripheralConnector2 connector2) {

    }

    @Override
    public void onConnectionChanged(boolean connected, PeripheralConnector2.Mode mode, PeripheralConnector2 connector2) {
        Key key = keyMap.get(connector2.getAddress());
        if (!this.isWorking) {
            return;
        }
        if (key.getLock().getCkwExpiredTime() < System.currentTimeMillis()) {
            mListener.onError(key, "钥匙已过期，需要联网更新，请打开APP开锁");
        }
        else if(key.getExpiredTime() != null && key.getExpiredTime() < System.currentTimeMillis()) {
            mListener.onError(key, "钥匙已经失效");
        }
        else {
            if (connected) {
                // 连接
                switch (mode) {
                    case MODE_NORMAL: {
                        mListener.onLockConnectedChanged(key, true);
                        connector2.unlock(key.getLock().getConstantKeyWord(), false);
                        break;
                    }
                    default:{
                        mListener.onError(key, "连接上的锁模式异常，模式是" + mode.toString());
                    }
                }
            }
            else {
                mListener.onLockConnectedChanged(key, false);
                connector2.connect(connector2.getAddress());
            }
        }
    }

    @Override
    public void onLockStateChanged(byte state, PeripheralConnector2 connector2) {
        Key key = keyMap.get(connector2.getAddress());
        mListener.onLockStateChanged(key, state);
    }

    @Override
    public void onDoorStateChanged(byte state, PeripheralConnector2 connector2) {
        Key key = keyMap.get(connector2.getAddress());
        mListener.onDoorStateChanged(key, state);
    }

    @Override
    public void onVersionGot(Version version, PeripheralConnector2 connector2) {

    }

    @Override
    public void onStatesGot(byte[] state, PeripheralConnector2 connector2) {

    }

    @Override
    public void onWriteCharFailed(BluetoothGattCharacteristic characteristic, int status, PeripheralConnector2 connector2) {
        Key key = keyMap.get(connector2.getAddress());
        mListener.onError(key, "与蓝牙通信失败");
    }

    @Override
    public void onUnknownNotificationReceived(byte[] data, PeripheralConnector2 connector2) {
        Key key = keyMap.get(connector2.getAddress());
        mListener.onError(key, "不能理解的通知");
    }

    @Override
    public void onResetToDfuMode(byte result, PeripheralConnector2 connector2) {

    }

    @Override
    public void onDisposableEncryptWordSetResult(byte result, PeripheralConnector2 connector2) {

    }

    @Override
    public void onConstantEncryptWordSetResult(byte result, PeripheralConnector2 connector2) {

    }

    @Override
    public void onResetConstantLockWordResult(byte result, PeripheralConnector2 connector2) {

    }

    @Override
    public void onClearEncryptWordsResult(byte result, PeripheralConnector2 connector2) {

    }

    @Override
    public void onBatteryLevelWarning(byte level, PeripheralConnector2 connector2) {

    }

    @Override
    public void onFlashReadingWarning(byte status, PeripheralConnector2 connector2) {

    }

    @Override
    public void onDfuCtptWriteSuccess(byte[] value, PeripheralConnector2 connector2) {

    }

    @Override
    public void onDfuCtptResponse(byte proc, byte state, PeripheralConnector2 connector2) {

    }

    @Override
    public void onDfuCtptReceivedNotify(int sizeReceived, PeripheralConnector2 connector2) {

    }

    @Override
    public void onDfuPktWriteSuccess(byte[] value, PeripheralConnector2 connector2) {

    }
}

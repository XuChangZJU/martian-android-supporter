package com.martianLife.utils;

import android.annotation.TargetApi;
import android.bluetooth.*;
import android.content.Context;
import android.util.Log;
import com.martianLife.domain.Version;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Created by Administrator on 2015/5/14.
 */
@TargetApi(18)
public class PeripheralConnector2 {
    private static final String LOCK_SERVICE_UUID = "d62a2015-7fac-a2a3-bec3-a68869e0f2bf";
    private static final String LOCK_CTPT_CHARACTERISTIC_UUID = "d62a9900-7fac-a2a3-bec3-a68869e0f2bf";
    private static final String LOCK_CTPT_CHAR_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";
    private static final String DFU_SERVICE_UUID = "00001530-1212-efde-1523-785feabcd123";
    private static final String DFU_PKT_CHARACTERISTIC_UUID = "00001532-1212-efde-1523-785feabcd123";
    private static final String DFU_CTPT_CHARACTERISTIC_UUID = "00001532-1212-efde-1523-785feabcd123";
    private static final String DFU_CTPT_CHAR_DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb";


    public interface PcEventListener {
        void onReceiveLockWord(byte[] lockWord, boolean bDisposable, PeripheralConnector2 connector2);

        void onReceiveKeyVerificationResult(byte result, boolean bDisposable, PeripheralConnector2 connector2);

        // void onReceiveApplyForLeavingResult(boolean success, Command command, PeripheralConnector2 connector2);
        //void onStatesGot(Lock lock, PeripheralConnector2 connector2);

        void onConnectionChanged(boolean connected, Mode mode, PeripheralConnector2 connector2);

        void onLockStateChanged(byte state, PeripheralConnector2 connector2);

        void onDoorStateChanged(byte state, PeripheralConnector2 connector2);

        void onVersionGot(Version version, PeripheralConnector2 connector2);

        void onStatesGot(byte[] state, PeripheralConnector2 connector2);

        void onWriteCharFailed(BluetoothGattCharacteristic characteristic, int status, PeripheralConnector2 connector2);

        void onUnknownNotificationReceived(byte[] data, PeripheralConnector2 connector2);

        void onResetToDfuMode(byte result, PeripheralConnector2 connector2);

        void onDisposableEncryptWordSetResult(byte result, PeripheralConnector2 connector2);

        void onConstantEncryptWordSetResult(byte result, PeripheralConnector2 connector2);

        void onResetConstantLockWordResult(byte result, PeripheralConnector2 connector2);

        void onClearEncryptWordsResult(byte result, PeripheralConnector2 connector2);

        void onBatteryLevelWarning(byte level, PeripheralConnector2 connector2);

        void onFlashReadingWarning(byte status, PeripheralConnector2 connector2);

        // 以下是DFU模式下的回调接口
        void onDfuCtptWriteSuccess(byte[] value, PeripheralConnector2 connector2);

        void onDfuCtptResponse(byte proc, byte state, PeripheralConnector2 connector2);

        void onDfuCtptReceivedNotify(int sizeReceived, PeripheralConnector2 connector2);

        void onDfuPktWriteSuccess(byte[] value, PeripheralConnector2 connector2);
    }

    // lock相关的，约定好的OP CODE
    private final static byte OP_CODE_GET_DISPOSABLE_LOCK_WORD = 0;
    private final static byte OP_CODE_GET_CONSTANT_LOCK_WORD = 2;
    private final static byte OP_CODE_VERIFY_DISPOSABLE_LOCK_KEY = 1;
    private final static byte OP_CODE_VERIFY_CONSTANT_LOCK_KEY = 3;
    private final static byte OP_CODE_GET_STATES = 4;
    private final static byte OP_CODE_RESET_TO_DFU_MODE = 5;
    private final static byte OP_CODE_GET_ALL_VERSIONS = 6;
    private final static byte OP_CODE_RESET_CONSTANT_LOCK_WORD = 7;
    private final static byte OP_CODE_SET_DISPOSABLE_ENCRYPT_WORD = 33;
    private final static byte OP_CODE_SET_CONSTANT_ENCRYPT_WORD = 34;
    private final static byte OP_CODE_CLEAR_ECNRYT_WORDS = 35;


    private final static byte OP_CODE_RESPONSE_IN_LOCK_MODE = (byte) 200;
    private final static byte OP_CODE_NOTIFY_IN_LOCK_MODE = (byte) 201;
    private final static byte NOTIFY_OBJECT_DOOR = 16;
    private final static byte NOTIFY_OBJECT_LOCK = 17;
    private final static byte NOTIFY_OBJECT_BATTERY_LEVEL = 18;
    private final static byte NOTIFY_OBJECT_FLASH = 19;


    /**
     * 锁的状态部分
     */
    public final static int STATE_DOOR_OFFSET = 0;
    public final static int STATE_LOCK_OFFSET = 1;
    public final static int STATE_BATTERY_OFFSET = 2;
    public final static int STATE_ENCRYPT_OFFSET = 3;

    public final static int STATE_BYTES_SIZE = 4;

    public final static byte DOOR_STATE_OPENED = 0;
    public final static byte DOOR_STATE_CLOSED = 1;
    public final static byte LOCK_STATE_LOCKED = 0;
    public final static byte LOCK_STATE_UNLOCKED = 1;
    public final static byte LOCK_STATE_UNCERTAIN = 2;
    public final static byte FLASH_READ_ERROR = 0;
    public final static byte ENCRYPT_WORD_STATE_UNSET = 0;
    public final static byte ENCRYPT_WORD_STATE_SET = 1;

    public final static byte RESULT_SUCCESS = 0;
    public final static byte RESULT_KEY_WORD_VALIDATE_FAILED = 6;
    public final static byte RESULT_FLASH_WRITING_ERROR = 7;
    public final static byte RESULT_FLASH_READING_ERROR = 8;
    public final static byte RESULT_ACTION_DISALLOWED = 9;
    public final static byte RESULT_COMMAND_UNRECOGNIZED = 10;
    public final static byte RESULT_PARAM_LENGTH_ILLEGAL = 11;


    private final static int mLockWordLength = 16;
    private final static int mLockKeyLength = 16;

    // dfu相关的，约定好的OP CODE
    public final static byte OP_CODE_START_DFU = 1;
    public final static byte OP_CODE_RECEIVE_INIT = 2;
    public final static byte OP_CODE_RECEIVE_FW = 3;
    public final static byte OP_CODE_VALIDATE = 4;
    public final static byte OP_CODE_ACTIVATE_N_RESET = 5;
    public final static byte OP_CODE_SYS_RESET = 6;
    public final static byte OP_CODE_IMAGE_SIZE_REQ = 7;
    public final static byte OP_CODE_PKT_RCPT_NOTIF_REQ = 8;
    public final static byte OP_CODE_RESPONSE_IN_DFU_MODE = 16;
    public final static byte OP_CODE_PKT_RCPT_NOTIF_IN_DFU_MODE = 17;

    // dfu mode
    public final static byte MODE_DFU_UPDATE_SD = 0X01;
    public final static byte MODE_DFU_UPDATE_BL = 0X02;
    public final static byte MODE_DFU_UPDATE_APP = 0X04;

    // dfu init flag
    public final static byte INIT_RX = 0x00;
    public final static byte INIT_COMPLETE = 0x01;

    // dfu response state
    public final static byte RESPONSE_SUCCESS = 0X01;
    public final static byte RESPONSE_INVALID_STATE = 0X02;
    public final static byte RESPONSE_NOT_SUPPORTED = 0X03;
    public final static byte RESPONSE_DATA_SIZE_OVERFLOW = 0X04;
    public final static byte RESPONSE_CRC_ERROR = 0X05;
    public final static byte RESPONSE_OPERATION_FAIL = 0X06;

    // dfu response procedure
    public final static byte PROC_START = 0X01;
    public final static byte PROC_INIT = 0X02;
    public final static byte PROC_RECEIVE_IMAGE = 0X03;
    public final static byte PROC_VALIDATE = 0X04;
    public final static byte PROC_ACTIVATE = 0X05;      // 这个状态不会由芯片返回
    public final static byte PROC_PKT_RCPT_REQ = 0X08;







    public enum Mode{
        MODE_UNCONNECTED,
        MODE_NORMAL,            // 正常模式
        MODE_DFU,             // 升级模式
        MODE_ILLEGAL
    }

    private final static String TAG = PeripheralConnector2.class.getSimpleName();
    private PcEventListener mCallback;
    private BluetoothAdapter mAdapter;
    private Context mContext;
    private String mAddress;

    private Version mAppVersion;
    private Version mBootloaderVersion;
    private Version mSoftDeviceVersion;
    private Mode mMode;

    private byte mLockState;
    private byte mDoorState;
    private byte mBatteryLevel = 0;
    private byte mEncryptWordState;

    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mLockCtptChar;          // lock control point characteristic
    private BluetoothGattCharacteristic mDfuCtptChar;           // device firm update control point characteristic
    private BluetoothGattCharacteristic mDfuPktChar;            // device firm update packet characteristic

    public PeripheralConnector2(Context context, BluetoothAdapter adapter, PcEventListener callback) {
        mCallback = callback;
        mAdapter = adapter;
        mContext = context;
    }


    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection
                boolean bSucc = mBluetoothGatt.discoverServices();
                Log.i(TAG, "Attempting to start service discovery:" + bSucc
                );
                // onConnectionStateChanged(true);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i(TAG, "Disconnected from GATT server.");
                mBluetoothGatt.close();
                mBluetoothGatt = null;
                onConnectionStateChanged(false);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                UUID serviceUuid = UUID.fromString(LOCK_SERVICE_UUID);
                BluetoothGattService service = gatt.getService(serviceUuid);
                if(service != null) {
                    // 正常模式
                    mMode = Mode.MODE_NORMAL;
                    UUID charUuid = UUID.fromString(LOCK_CTPT_CHARACTERISTIC_UUID);
                    mLockCtptChar = service.getCharacteristic(charUuid);
                    assert mLockCtptChar != null;

                    mBluetoothGatt.setCharacteristicNotification(mLockCtptChar, true);
                    BluetoothGattDescriptor descriptor = mLockCtptChar.getDescriptor(
                            UUID.fromString(LOCK_CTPT_CHAR_DESCRIPTOR_UUID));
                    descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    mBluetoothGatt.writeDescriptor(descriptor);
                }
                else {
                    serviceUuid = UUID.fromString(DFU_SERVICE_UUID);
                    service = gatt.getService(serviceUuid);
                    if(service != null) {
                        // 升级模式
                        mMode = Mode.MODE_DFU;
                        UUID charUuid = UUID.fromString(DFU_CTPT_CHARACTERISTIC_UUID);
                        mDfuCtptChar = service.getCharacteristic(charUuid);
                        assert mDfuPktChar!= null;

                        charUuid = UUID.fromString(DFU_PKT_CHARACTERISTIC_UUID);
                        mDfuPktChar = service.getCharacteristic(charUuid);
                        assert mDfuPktChar != null;


                        mBluetoothGatt.setCharacteristicNotification(mDfuCtptChar, true);
                        BluetoothGattDescriptor descriptor = mDfuCtptChar.getDescriptor(
                                UUID.fromString(DFU_CTPT_CHAR_DESCRIPTOR_UUID));
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        mBluetoothGatt.writeDescriptor(descriptor);
                    }
                    else {
                        // 不支持的模式
                        mMode = Mode.MODE_ILLEGAL;
                        onUnknownServiceConnected();
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
                // todo
                gatt.disconnect();
            }
        }


        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            if (characteristic == mLockCtptChar) {
                final byte[] data = characteristic.getValue();
                System.out.println("mLockCtptChar");
                for (int i = 0; i < data.length; i++) {
                    System.out.print(data[i]);
                    System.out.print(',');
                }
                System.out.println();

                if (data != null && data.length > 0) {
                    switch (data[0]) {
                        case OP_CODE_NOTIFY_IN_LOCK_MODE:
                            switch (data[1]) {
                                case NOTIFY_OBJECT_DOOR: {
                                    onDoorStateChanged(data[3]);
                                    break;
                                }
                                case NOTIFY_OBJECT_LOCK: {
                                    onLockStateChanged(data[3]);
                                    break;
                                }
                                case NOTIFY_OBJECT_BATTERY_LEVEL: {
                                    onBatteryLevelNotified(data[3]);
                                    break;
                                }
                                case NOTIFY_OBJECT_FLASH: {
                                    onFlashReadingNotified(data[3]);
                                    break;
                                }
                                default:
                                    onUnknownNotificationReceived(data);
                                    break;
                            }
                            break;
                        case OP_CODE_RESPONSE_IN_LOCK_MODE: {
                            byte[] content = new byte[data.length - 2];
                            System.arraycopy(data, 2, content, 0, data.length - 2);
                            switch (data[1]) {
                                case OP_CODE_GET_CONSTANT_LOCK_WORD:
                                    onLockwordReceived(content, false);
                                    break;
                                case OP_CODE_GET_DISPOSABLE_LOCK_WORD:
                                    onLockwordReceived(content, true);
                                    break;
                                case OP_CODE_VERIFY_CONSTANT_LOCK_KEY:
                                    onVerifyKeyResultReceived(content, false);
                                    break;
                                case OP_CODE_VERIFY_DISPOSABLE_LOCK_KEY:
                                    onVerifyKeyResultReceived(content, true);
                                    break;
                                case OP_CODE_GET_STATES:
                                    onStatesGots(content);
                                    break;
                                case OP_CODE_RESET_TO_DFU_MODE:
                                    onResetResult(content);
                                    break;
                                case OP_CODE_GET_ALL_VERSIONS:
                                    onVersionGot(content);
                                    break;
                                case OP_CODE_SET_DISPOSABLE_ENCRYPT_WORD:
                                    onDisposableEncryptWordSetResult(content);
                                    break;
                                case OP_CODE_SET_CONSTANT_ENCRYPT_WORD:
                                    onConstantEncryptWordSetResult(content);
                                    break;
                                case OP_CODE_RESET_CONSTANT_LOCK_WORD:
                                    onResetConstantLockWordResult(content);
                                    break;
                                case OP_CODE_CLEAR_ECNRYT_WORDS:
                                    onClearEncryptWordsResult(content);
                                default:
                                    onUnknownNotificationReceived(data);
                                    break;
                            }
                            break;
                        }
                        default:
                            onUnknownNotificationReceived(data);
                            break;
                    }
                }
            }
            else if(characteristic == mDfuCtptChar) {
                final byte[] data = characteristic.getValue();
                System.out.println("mDfuCtptChar");
                for (byte aData : data) {
                    System.out.print(aData);
                    System.out.print(',');
                }
                System.out.println();

                onDfuCtptReceivedResult(data);
            }
            else {
                System.out.println("Outlier");
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status){

            onWriteCharacteristic(characteristic, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            if(status == BluetoothGatt.GATT_SUCCESS
                    && descriptor.getUuid().equals(UUID.fromString(LOCK_CTPT_CHAR_DESCRIPTOR_UUID))) {

                assert descriptor.getCharacteristic().getUuid() == mLockCtptChar.getUuid() ||
                        descriptor.getCharacteristic().getUuid() == mDfuCtptChar.getUuid();
                if(mLockCtptChar != null && descriptor.getCharacteristic().getUuid() == mLockCtptChar.getUuid()) {
                    getStates();
                }
                else if (mDfuCtptChar != null && descriptor.getCharacteristic().getUuid() == mDfuCtptChar.getUuid()) {
                    onConnectionStateChanged(true);
                }

            }
            else {
                Log.w(TAG, "接收到非法的descriptor写入");
            }
        }

    };

    public String getAddress() {
        return mAddress;
    }

    private void getVersions() {
        byte [] data = new byte[1];
        data[0] = OP_CODE_GET_ALL_VERSIONS;

        if (false == mLockCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }


    private void getStates() {
        byte [] data = new byte[1];
        data[0] = OP_CODE_GET_STATES;

        if (false == mLockCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public boolean connect(String address) {
        BluetoothDevice device = mAdapter.getRemoteDevice(address);
        if (device == null) {
            return false;
        } else {
            mAddress = address;
            mBluetoothGatt = device.connectGatt(mContext, true, mGattCallback);
            refreshDeviceCache(mBluetoothGatt);
            return true;
        }
    }

    public void disconnect() {
        mBluetoothGatt.disconnect();
    }

    public void unlock( byte [] keyWord, boolean bDisposable) {
        assert keyWord.length == mLockKeyLength;
        byte[] bytes = new byte[mLockKeyLength + 2];
        if (bDisposable)
            bytes[0] = OP_CODE_VERIFY_DISPOSABLE_LOCK_KEY;
        else
            bytes[0] = OP_CODE_VERIFY_CONSTANT_LOCK_KEY;

        bytes[1] = (byte) keyWord.length;
        System.arraycopy(keyWord, 0, bytes, 2, 16);


        if (false == mLockCtptChar.setValue(bytes) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void getLockWord(boolean bDisposable) {
        byte[] data = new byte[1];
        if (bDisposable)
            data[0] = OP_CODE_GET_DISPOSABLE_LOCK_WORD;
        else
            data[0] = OP_CODE_GET_CONSTANT_LOCK_WORD;

        if (false == mLockCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public Mode getMode() {
        return mMode;
    }

    public void dfuSwitchToDfuMode( byte [] keyWord) {
        assert(keyWord.length == mLockKeyLength);
        byte[] data = new byte[mLockKeyLength + 2];
        data[0] = OP_CODE_RESET_TO_DFU_MODE;
        data[1] = mLockKeyLength;

        System.arraycopy(keyWord, 0, data, 2, mLockKeyLength);


        if (false == mLockCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void dfuStart(byte mode) {
        Log.i(TAG, "[DFU]发送Start命令");

        byte[] data = new byte[2];
        data[0] = OP_CODE_START_DFU;
        data[1] = mode;

        if (false == mDfuCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void dfuSendStartData(byte[] data) {
        Log.i(TAG, "[DFU]发送Start数据");

        if(false == mDfuPktChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuPktChar)){
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void dfuInitBegin() {
        Log.i(TAG, "[DFU]发送init begin命令");
        byte[] data = new byte[2];
        data[0] = OP_CODE_RECEIVE_INIT;
        data[1] = INIT_RX;

        if (false == mDfuCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }


    public void dfuEndBegin() {
        Log.i(TAG, "[DFU]发送init end命令");
        byte[] data = new byte[2];
        data[0] = OP_CODE_RECEIVE_INIT;
        data[1] = INIT_COMPLETE;

        if (false == mDfuCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void dfuSendInitData(byte[] data) {
        Log.i(TAG, "[DFU]发送init数据");
        if(false == mDfuPktChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuPktChar)){
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }

    }

    public void dfuPktNotifyReq(short pktNotifyCount) {
        Log.i(TAG, "[DFU]发送Pkt Notify Req命令");
        byte[] data = new byte[3];
        data[0] = OP_CODE_PKT_RCPT_NOTIF_REQ;
        data[1] = (byte)(pktNotifyCount);
        data[2] = (byte)(pktNotifyCount >> 8);

        if(false == mDfuCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuCtptChar)){
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }


    public void dfuImageDataBegin() {
        Log.i(TAG, "[DFU]发送image begin命令");
        byte[] data = new byte[1];
        data[0] = OP_CODE_RECEIVE_FW;

        if (false == mDfuCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }


    public void dfuSendImageData(byte[] data) {
        Log.i(TAG, "[DFU]发送image数据");
        if(false == mDfuPktChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuPktChar)){
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }

    }

    public void dfuValidateImageBegin() {
        Log.i(TAG, "[DFU]发送validate image命令");
        byte[] data = new byte[1];
        data[0] = OP_CODE_VALIDATE;

        if (false == mDfuCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void dfuActivateImageBegin() {
        Log.i(TAG, "[DFU]发送activate image命令");
        byte[] data = new byte[1];
        data[0] = OP_CODE_ACTIVATE_N_RESET;

        if (false == mDfuCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mDfuCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mDfuCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }


    public Version getAppVersion() {
        return mAppVersion;
    }

    public void setDisposableEncryptWord(String encryptWord) {
        assert encryptWord.length() == mLockKeyLength;
        byte[] data = new byte[mLockKeyLength + 2];
        data[0] = OP_CODE_SET_DISPOSABLE_ENCRYPT_WORD;
        data[1] = (byte) encryptWord.length();

        System.arraycopy(stringToBytes(encryptWord), 0, data, 2, mLockKeyLength);

        if (false == mLockCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void setConstantEncryptWord(String encryptWord) {
        assert encryptWord.length() == mLockKeyLength;
        byte[] data = new byte[mLockKeyLength + 2];
        data[0] = OP_CODE_SET_CONSTANT_ENCRYPT_WORD;
        data[1] = (byte) encryptWord.length();

        System.arraycopy(stringToBytes(encryptWord), 0, data, 2, mLockKeyLength);

        if (false == mLockCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void resetConstantLockWord(String keyWord) {
        assert keyWord.length() == mLockKeyLength;
        byte[] data = new byte[mLockKeyLength + 2];
        data[0] = OP_CODE_RESET_CONSTANT_LOCK_WORD;
        data[1] = (byte) keyWord.length();

        System.arraycopy(stringToBytes(keyWord), 0, data, 2, mLockKeyLength);

        if (false == mLockCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }

    public void clearEncryptWords() {
        Log.i(TAG, "发送clearEncryptWords命令");
        byte[] data = new byte[1];
        data[0] = OP_CODE_CLEAR_ECNRYT_WORDS;

        if (false == mLockCtptChar.setValue(data) || false == mBluetoothGatt.writeCharacteristic(mLockCtptChar)) {
            Log.w(TAG, "蓝牙写characteristic失败");
            onWriteCharacteristic(mLockCtptChar, BluetoothGatt.GATT_FAILURE);
        }
    }


    /**
     * 得到锁的当前状态
     * @return 第一个byte是门的状态，第二个byte是锁的状态，第三个byte是锁的电量
     */
    public byte [] getState() {
        byte[] states = new byte[STATE_BYTES_SIZE];
        states[STATE_DOOR_OFFSET] = mDoorState;
        states[STATE_LOCK_OFFSET] = mLockState;
        states[STATE_BATTERY_OFFSET] = mBatteryLevel;
        states[STATE_ENCRYPT_OFFSET] = mEncryptWordState;

        return states;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////
    // 私有接口
    ///////////////////////////////////////////////////////////////////////////////////////////////////

    private static short bytesToShort(byte one, byte two) {
        short value = 0;
        value |= (one & 0xff);
        value |= ((two << 8) & 0xff00);
        return  value;
    }

    private static int bytesToInt(byte one, byte two, byte three, byte four) {
        int value = 0;
        value |= (one & 0xff);
        value |= ((two << 8) & 0xff00);
        value |= ((three << 16) & 0xff0000);
        value |= ((four << 24) & 0xff000000);
        return value;
    }

    private static byte[] intToBytes(int value) {
        byte[] data = new byte[4];
        data[0] = (byte)((value >>> 24) & 0xff);
        data[1] = (byte)((value>> 16) & 0xff);
        data[2] = (byte)((value >> 8) & 0xff);
        data[3] = (byte)(value & 0xff);
        return data;
    }

    private static byte[] shortToBytes(short value) {
        byte[] data = new byte[2];
        data[0] = (byte)((value >> 8) & 0xff);
        data[1] = (byte)(value & 0xff);
        return data;
    }

    private static byte[] stringToBytes(String s) {
        char c[] = s.toCharArray();
        byte b[] = new byte[c.length];
        for(int i = 0; i < c.length; i ++){
            b[i] = (byte)c[i];
        }

        return b;
    }

    private static String bytesToString(byte[] b) {
        char[] c = new char[b.length];
        for(int i = 0; i < b.length; i ++) {
            c[i] = (char)b[i];
        }
        return new String(c);
    }


    private void onDoorStateChanged(byte state) {
        mCallback.onDoorStateChanged(state, this);
    }

    private void onLockStateChanged(byte state) {
        mCallback.onLockStateChanged(state, this);
    }

    private void onUnknownNotificationReceived(byte[] data) {
        mCallback.onUnknownNotificationReceived(data, this);
    }

    private void onLockwordReceived(byte[] lockWordData, boolean bDisposable) {
        assert lockWordData[0] == mLockWordLength;
        byte[] lockWordData2 = new byte[mLockWordLength];
        System.arraycopy(lockWordData, 1, lockWordData2, 0, mLockWordLength);

        mCallback.onReceiveLockWord(lockWordData2, bDisposable, this);
    }

    private void onVerifyKeyResultReceived(byte[] data, boolean bDisposable) {
        assert data[0] == 1;
        mCallback.onReceiveKeyVerificationResult(data[1], bDisposable, this);
    }

    private void onConnectionStateChanged(boolean bConnected) {
        mCallback.onConnectionChanged(bConnected, mMode, this);
    }


    private void onStatesGots(byte[] data) {
        mDoorState = data[STATE_DOOR_OFFSET + 1];
        mLockState = data[STATE_LOCK_OFFSET + 1];
        mBatteryLevel = data[STATE_BATTERY_OFFSET + 1];
        mEncryptWordState = data[STATE_ENCRYPT_OFFSET + 1];

        // 确认了状态再通知上层连接成功
        onConnectionStateChanged(true);
    }

    private void onWriteCharacteristic(BluetoothGattCharacteristic characteristic, int status) {
        if(status != BluetoothGatt.GATT_SUCCESS) {
            mCallback.onWriteCharFailed(characteristic, status, this);
        }
        else {
            if(mDfuCtptChar!= null && characteristic.getUuid().equals(mDfuCtptChar.getUuid())) {
                byte[] value = characteristic.getValue();

                mCallback.onDfuCtptWriteSuccess(value, this);
            }
            else if(mDfuPktChar!= null && characteristic.getUuid().equals(mDfuPktChar.getUuid())) {
                byte[] value = characteristic.getValue();

                mCallback.onDfuPktWriteSuccess(value, this);
            }
        }
    }

    private void onBatteryLevelNotified(byte level) {
        mCallback.onBatteryLevelWarning(level, this);
    }

    private void onFlashReadingNotified(byte status) {
        mCallback.onFlashReadingWarning(status, this);
    }

    private void onUnknownServiceConnected() {
        // todo
    }

    private void onDfuCtptReceivedResult(byte[] data) {
        if(data[0] == OP_CODE_RESPONSE_IN_DFU_MODE) {
            mCallback.onDfuCtptResponse(data[1], data[2], this);
        }
        else {
            assert data[0] == OP_CODE_PKT_RCPT_NOTIF_IN_DFU_MODE;
            /*int sizeReceived = 0;
            sizeReceived |= (data[1] & 0xff);
            sizeReceived |= ((data[2] << 8) & 0xff00);
            sizeReceived |= ((data[3] << 16) & 0xff0000);
            sizeReceived |= ((data[4] << 24) & 0xff000000);
            */
            int sizeReceived = bytesToInt(data[1], data[2], data[3], data[4]);

            mCallback.onDfuCtptReceivedNotify(sizeReceived, this);
        }
    }


    private void onVersionGot(byte[] data) {
        mAppVersion = new Version();
        mAppVersion.setMinor(bytesToShort(data[1], data[2]));
        mAppVersion.setMajor(bytesToShort(data[3], data[4]));
/*
        mBootloaderVersion = new LockVersion(data[2], data[3]);

        mSoftDeviceVersion =new LockVersion(data[4], data[5]); */

        // 再去获取锁当前的states
        mCallback.onVersionGot(mAppVersion, this);
    }

    private void onResetResult(byte[] data) {
        assert data.length == 1;
        mCallback.onResetToDfuMode(data[1], this);
    }

    private void onDisposableEncryptWordSetResult(byte [] data) {
        assert data.length == 1;
        mCallback.onDisposableEncryptWordSetResult(data[1], this);
    }

    private void onConstantEncryptWordSetResult(byte [] data) {
        assert data.length == 1;
        mEncryptWordState = ENCRYPT_WORD_STATE_SET;
        mCallback.onConstantEncryptWordSetResult(data[1], this);
    }

    private void onResetConstantLockWordResult(byte[] data) {
        assert data.length == 1;
        mCallback.onResetConstantLockWordResult(data[1], this);
    }

    private void onClearEncryptWordsResult(byte[] data) {
        assert data.length == 1;
        mEncryptWordState = ENCRYPT_WORD_STATE_UNSET;
        mCallback.onClearEncryptWordsResult(data[1], this);
    }

    /*
    * 这个函数用来refresh掉缓存的service，以避免在dfu和normal模式之间切换时的service不一样
     */
    private boolean refreshDeviceCache(BluetoothGatt gatt){
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        }
        catch (Exception localException) {
            Log.e(TAG, "An exception occured while refreshing device");
        }
        return false;
    }
}

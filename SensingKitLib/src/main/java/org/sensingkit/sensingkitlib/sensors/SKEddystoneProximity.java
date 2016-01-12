package org.sensingkit.sensingkitlib.sensors;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.ParcelUuid;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.eddystone.Eddystone;

import org.sensingkit.sensingkitlib.SKException;
import org.sensingkit.sensingkitlib.SKExceptionErrorCode;
import org.sensingkit.sensingkitlib.SKSensorType;
import org.sensingkit.sensingkitlib.data.SKAbstractData;
import org.sensingkit.sensingkitlib.data.SKEddystoneProximityData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ming-Jiun Huang on 11/14/15.
 */

public class SKEddystoneProximity extends SKAbstractSensor {

    @SuppressWarnings("unused")
    private static final String TAG = "SKEddystoneProximity";

    //private final static int REQUEST_ENABLE_BLUETOOTH = 1;
    private final BluetoothAdapter mBluetoothAdapter;
    private ArrayList<SKEddystoneProximityData> mEddyStoneProximityData;
    private BeaconManager mBeaconManager;
    private static final ParcelUuid UUID = ParcelUuid.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D");


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public SKEddystoneProximity(Context context) throws SKException{
        super(context, SKSensorType.EDDYSTONE_PROXIMITY);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mEddyStoneProximityData = new ArrayList<>();

        if (mBluetoothAdapter == null){
            throw new SKException(TAG, "Bluetooth sensor module is not supported from the device.", SKExceptionErrorCode.UNKNOWN_ERROR);
        } else if (!mBluetoothAdapter.isEnabled()){
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //
        } else if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            throw new SKException(TAG, "Bluetooth sensor module is not supported from the device.", SKExceptionErrorCode.UNKNOWN_ERROR);
        }

        mBeaconManager.setEddystoneListener(new BeaconManager.EddystoneListener() {
            @Override
            public void onEddystonesFound(List<Eddystone> list) {
                Eddystone eddystone = list.get(list.size() - 1);
                String namespaceId = eddystone.namespace;
                String instanceId = eddystone.instance;
                int rssi = eddystone.rssi;
                int txPower = eddystone.calibratedTxPower;
                SKEddystoneProximityData eddystoneProximityData = null;
                eddystoneProximityData = new SKEddystoneProximityData(System.currentTimeMillis(), namespaceId, instanceId, rssi, txPower);

                mEddyStoneProximityData.add(eddystoneProximityData);
            }
        });
    }

    @Override
    protected boolean shouldPostSensorData(SKAbstractData data) {

        // Always post sensor data
        return true;    }

    @Override
    public void startSensing() throws SKException {

        this.isSensing = true;
        mBeaconManager.startEddystoneScanning();
        mBluetoothAdapter.startDiscovery();
    }

    @Override
    public void stopSensing() {

        this.isSensing = false;

        mBluetoothAdapter.cancelDiscovery();
    }
}

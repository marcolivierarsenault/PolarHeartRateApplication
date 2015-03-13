package org.marco45.polarheartmonitor;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

/**
 * This thread to the connection with the bluetooth device
 * @author Marco
 *
 */
@SuppressLint("NewApi")
public class H7ConnectThread{
	BluetoothAdapter mBluetoothAdapter;
	MainActivity ac;
	public H7ConnectThread(BluetoothDevice device, MainActivity ac) {
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		this.ac=ac;
		BluetoothSocket tmp = null;
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


		device.connectGatt(ac, false, btleGattCallback);
		
		

	}

	
	/** Will cancel an in-progress connection, and close the socket */
	public void cancel() {

	}

	
	private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
		 
	    @Override
	    public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
	    	byte[] data = characteristic.getValue();
	    	int bmp = data[1];
	    	DataHandler.getInstance().cleanInput(bmp);
	    }
	 
	    @Override
	    public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) { 
			gatt.discoverServices();
	    }
	 
	    @Override
	    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) { 
	    	BluetoothGattService service = gatt.getService(UUID.fromString("0000180D-0000-1000-8000-00805F9B34FB"));
			//BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString("00002A37-0000-1000-8000-00805F9B34FB"));
			List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
			for (BluetoothGattCharacteristic cc : characteristics)
				{
					for (BluetoothGattDescriptor descriptor : cc.getDescriptors()) {
					    //find descriptor UUID that matches Client Characteristic Configuration (0x2902)
					    // and then call setValue on that descriptor
						boolean aa = gatt.setCharacteristicNotification(cc,true);
						descriptor.setValue( BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
					    gatt.writeDescriptor(descriptor);
					}
				}
	    }
	};
}
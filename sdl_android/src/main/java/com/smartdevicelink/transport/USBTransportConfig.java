package com.smartdevicelink.transport;

import com.smartdevicelink.transport.enums.TransportType;

import android.content.Context;
import android.hardware.usb.UsbAccessory;
import android.os.ParcelFileDescriptor;

public class USBTransportConfig extends BaseTransportConfig {
	
	private Context mainActivity = null;
	private UsbAccessory usbAccessory = null;
	private Boolean queryUsbAcc = true;
	private ParcelFileDescriptor parcelFileDescriptor = null;
	
	public USBTransportConfig (Context mainActivity) {
		this.mainActivity = mainActivity;
	}
	
	public USBTransportConfig (Context mainActivity, ParcelFileDescriptor parcelFileDescriptor) {
		this.mainActivity = mainActivity;
		this.parcelFileDescriptor = parcelFileDescriptor;
	}

	public USBTransportConfig (Context mainActivity, UsbAccessory usbAccessory) {
		this.mainActivity = mainActivity;
		this.usbAccessory = usbAccessory;
	}
	
	public USBTransportConfig (Context mainActivity, boolean shareConnection, boolean queryUsbAcc) {
		this.mainActivity = mainActivity;
		this.queryUsbAcc = queryUsbAcc;
		super.shareConnection = shareConnection;
	}
	
	public USBTransportConfig (Context mainActivity, UsbAccessory usbAccessory, boolean shareConnection, boolean queryUsbAcc) {
		this.mainActivity = mainActivity;
		this.queryUsbAcc = queryUsbAcc;
		this.usbAccessory = usbAccessory;
		super.shareConnection = shareConnection;
	}
	
	public Boolean getQueryUsbAcc () {
		return queryUsbAcc;
	}
	
	public Context getUSBContext () {
		return mainActivity;
	}
	
	public UsbAccessory getUsbAccessory () {
		return usbAccessory;
	}

	public void setUsbAccessory (UsbAccessory value) { usbAccessory = value; }

	public TransportType getTransportType() {
		return TransportType.USB;
	}

	public void setParcelFileDescriptor(ParcelFileDescriptor parcelFileDescriptor){
		this.parcelFileDescriptor = parcelFileDescriptor;
	}

	public ParcelFileDescriptor getParcelFileDescriptor() {
		return parcelFileDescriptor;
	}
}
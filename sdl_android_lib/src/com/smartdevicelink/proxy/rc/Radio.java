package com.smartdevicelink.proxy.rc;

import com.smartdevicelink.proxy.OnUpdateListener;
import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZone;
import com.smartdevicelink.proxy.rc.enums.InteriorDataType;
import com.smartdevicelink.proxy.rc.rpc.GetInteriorVehicleData;

public class Radio extends Module{

	
	public Radio(InteriorZone zone) {
		super(InteriorDataType.RADIO,zone);
		
	}

	public void tuneUp(){
		//Send get radio status
		//Pase station
		//Send set radio status to station +2
		
	}
	
	public void tuneDown(){
		//Same as tuneup but -2
	}
	public void tune(){
		GetInteriorVehicleData getStatus = baseGetStatusRequest();
		getStatus.setOnUpdateListener(new OnUpdateListener(){

			@Override
			public void onFinish(int correlationId, RPCMessage message, long totalSize) {
				// TODO send a set rpc
				
			}
			
		});
	}
	

	
	private void getStatus(){
		
	}
}

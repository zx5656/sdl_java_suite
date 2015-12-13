package com.smartdevicelink.proxy.rc;

import com.smartdevicelink.proxy.RPCRequest;
import com.smartdevicelink.proxy.rc.datatypes.ClimateControlData;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZone;
import com.smartdevicelink.proxy.rc.enums.ModuleType;
import com.smartdevicelink.proxy.rc.rpc.SetInteriorVehicleData;

public class ClimateControl extends Module {

	
	public ClimateControl(InteriorZone zone) {
		super(ModuleType.CLIMATE,zone);
	}
	
	public void setFanSpeed(){
		
	}
	
	public RPCRequest setTemp(int desiredTemp){
		ClimateControlData climate = new ClimateControlData();
		climate.setDesiredTemp(desiredTemp);
		return new SetInteriorVehicleData(getBaseModuleData(climate));

	}
}

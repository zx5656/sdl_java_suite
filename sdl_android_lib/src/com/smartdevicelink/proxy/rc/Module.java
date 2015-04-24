package com.smartdevicelink.proxy.rc;

import java.util.ArrayList;
import java.util.List;

import com.smartdevicelink.proxy.rc.datatypes.InteriorZone;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZoneDescription;
import com.smartdevicelink.proxy.rc.enums.InteriorDataType;
import com.smartdevicelink.proxy.rc.rpc.GetInteriorVehicleData;

public abstract class Module {

	protected InteriorDataType type;
	protected InteriorZone zone;
	
	public Module(InteriorDataType type,InteriorZone zone ){
		this.type = type;
		this.zone = zone;
	}
	
	protected GetInteriorVehicleData baseGetStatusRequest(){
		GetInteriorVehicleData getData = new GetInteriorVehicleData();
		List<InteriorZoneDescription> caps = new ArrayList<InteriorZoneDescription>();
		caps.add(new InteriorZoneDescription(zone,type));
		getData.setInteriorZoneDescriptions(caps);		
		return getData;
	}
	
}

package com.smartdevicelink.proxy.rc.datatypes;

import java.util.Hashtable;

import com.smartdevicelink.proxy.RPCStruct;
import com.smartdevicelink.proxy.rc.enums.InteriorDataType;

public class ControlData extends RPCStruct{

	private InteriorDataType interiorDataType = null;

	public ControlData(){ }
	
	public ControlData(Hashtable<String, Object> hash) {
		super(hash);
	}
	
	
	public InteriorDataType getInteriorDataType() {
		return interiorDataType;
	}

	public void setInteriorDataType(InteriorDataType interiorDataType) {
		this.interiorDataType = interiorDataType;
	}
	
}

package com.smartdevicelink.proxy.rc;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import com.smartdevicelink.proxy.OnUpdateListener;
import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZone;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZoneDescription;
import com.smartdevicelink.proxy.rc.enums.InteriorDataType;
import com.smartdevicelink.proxy.rc.rpc.GetInteriorVehicleDataCapabilities;
import com.smartdevicelink.proxy.rc.rpc.GetInteriorVehicleDataCapabilitiesResponse;

public class RemoteControl {

	
	private static RemoteControl instance = null; //This needs to get cleared on d/c
	private InteriorZone zone;
	
	private HashMap<InteriorZone, Module> modules;	
	
	private RemoteControl(){
		modules = new HashMap<InteriorZone, Module>();
		//TODO should we just do a get capabilities right now?
	}
	
	public static RemoteControl getInstance(){
		if(instance == null){
			instance = new RemoteControl();
		}
		return instance;
	}
	/**
	 * Setting this zone will be the zone used for every request.
	 * @param zone
	 */
	public void setZone(InteriorZone zone){
		this.zone = zone;
	}
	
	public void getCapabilities(){
		GetInteriorVehicleDataCapabilities get = new GetInteriorVehicleDataCapabilities();
		get.setOnUpdateListener(new OnUpdateListener(){

			@Override
			public void onFinish(int correlationId, RPCMessage message,long totalSize) {
				
				GetInteriorVehicleDataCapabilitiesResponse resp = (GetInteriorVehicleDataCapabilitiesResponse)message;
				List<InteriorZoneDescription> caps = resp.getInteriorCapabilities();
				//If auto subscribed do we store a list?
				//Or do we store a list regardless, of at least the basics
				for(InteriorZoneDescription zoneDescription: caps){
					InteriorZone zone = zoneDescription.getZone();
					//TODO find if we have access to this zone
					List<InteriorDataType> types = zoneDescription.getDataTypes();
					for(InteriorDataType dataType: types){
						//add to modules
						modules.put(zone,createModule(dataType,zone));	
					}
					
				}
				
			}
			
		});
		//send(get);
	}
	
	private Module createModule(InteriorDataType type, InteriorZone zone){
		switch(type){
		case RADIO:
			return new Radio(zone);
		case CLIMATE:
			default:
				return null;
		}
	}
	
	private void send(){
		if(zone==null){
			throw new IllegalStateException("Zone is not set.");
		}
	}
	
}

package com.smartdevicelink.proxy.rc;

import java.util.List;
import java.util.Vector;

import android.util.Log;

import com.smartdevicelink.proxy.OnUpdateListener;
import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZone;
import com.smartdevicelink.proxy.rc.datatypes.ModuleDescription;
import com.smartdevicelink.proxy.rc.enums.ModuleType;
import com.smartdevicelink.proxy.rc.rpc.GetInteriorVehicleDataCapabilities;
import com.smartdevicelink.proxy.rc.rpc.GetInteriorVehicleDataCapabilitiesResponse;

public class RemoteControl {

	private static final String TAG = "SDL Remote Control";
	
	private InteriorZone zone;
	private IRemoteControlListener listener;
	private Vector<Module> modules;	
	
	public RemoteControl(InteriorZone zone, IRemoteControlListener listener){
		modules = new Vector<Module>();
		this.zone = zone;
		this.listener = listener;
		//TODO should we just do a get capabilities right now? 
		//For now...yes.
		getCapabilities(true);
	}
	

	/**
	 * Setting this zone will be the zone used for every request.
	 * @param zone
	 */
	public void setZone(InteriorZone zone){
		this.zone = zone;
	}
	
	/**
	 * When getting capabilities it will overwrite the current list of modules. This keeps the list
	 * up to date.
	 * @param writeable set if we only want writable zones, ie include our zone
	 */
	public void getCapabilities(boolean writeable){
		GetInteriorVehicleDataCapabilities get = new GetInteriorVehicleDataCapabilities();
		//We want to reset our list
		if(modules == null || modules.size()>0){
			modules.clear();
		}
		if(writeable){
			//If we are checking for just zones we can write to we will include our zone
			get.setZone(zone);
		}
		
		get.setOnUpdateListener(new OnUpdateListener(){

			@Override
			public void onFinish(int correlationId, RPCMessage message,long totalSize) {
				
				GetInteriorVehicleDataCapabilitiesResponse resp = (GetInteriorVehicleDataCapabilitiesResponse)message;
				List<ModuleDescription> caps = resp.getInteriorCapabilities();
				//If auto subscribed do we store a list?
				//Or do we store a list regardless, of at least the basics
				if(caps==null){
					Log.e(TAG, "Capabilities object was null.");
					return;
				}
				for(ModuleDescription zoneDescription: caps){
					modules.add(createModule(zoneDescription.getModuleType(),zoneDescription.getZone()));
				}
				if(listener!=null){
					listener.onRemoteControlReady();
				}
			}
			
		});
		send(get);
	}
	
	private Module createModule(ModuleType type, InteriorZone zone){
		switch(type){
			case RADIO:
				return new Radio(zone);
			case CLIMATE:
				return new ClimateControl(zone);
			default:
				return null;
		}
	}
	/**
	 * Will return default radio for this user's device's zone
	 * @return
	 
	public Radio getRadio(){
		return getRadio(zone);	
	}
	
	public Radio getRadio(InteriorZone zone){
		return (Radio)getModule(ModuleType.RADIO,zone);
	}
	
	public ClimateControl getClimateControl(){
		return getClimateControl(zone);	
	}
	
	public ClimateControl getClimateControl(InteriorZone zone){
		return (ClimateControl)getModule(ModuleType.CLIMATE,zone);
	}
	*/
	
	public List<Module> getModules(){
		return this.modules;
	}
	
	public Module getModule(ModuleType type){
		return getModule(type,zone);
	}
	
	public Module getModule(ModuleType type, InteriorZone zone){
		for(Module module:modules){
			if(module.type == type){
				if(module.zone.isContainedWithin(zone)){
					return module;
				}
			}
		}
		return null;
	}
	
	private void send(RPCMessage msg){
		if(zone==null){
			throw new IllegalStateException("Zone is not set.");
		}
		if(listener == null){
			throw new IllegalStateException("IRemoteControlListener is not set.");
		}
		listener.send(msg);
	}
	
	public interface IRemoteControlListener{
		public void onRemoteControlReady();
		public void send(RPCMessage message);
	}
}

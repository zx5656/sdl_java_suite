package com.smartdevicelink.proxy.rc.datatypes;

import java.util.Hashtable;

import com.smartdevicelink.proxy.RPCStruct;
import com.smartdevicelink.proxy.rc.enums.InteriorDataType;

/**
 * The interiorDataType indicates which type of data should be changed and identifies which data object exists in this struct. <p>For example, if the interiorDataType is CLIMATE then a "climateControlData" should exist
 * 
 * <P> The correct way to use this class is to check the data type first, and then use the corresponding method to find
 * @author Joey Grover
 *
 */
public class InteriorData extends RPCStruct {
	
	public static final String KEY_INTERIOR_DATA_TYPE		= "interiorDataType";
	public static final String KEY_RADIO_DATA				= "radioControlData";
	public static final String KEY_CLIMATE_DATA				= "climateControlData";
	
	/**
	 * Constructs a newly allocated InteriorData object
	 */
	public InteriorData() { }

	/**
	 * Constructs a newly allocated InteriorZone object indicated by the Hashtable parameter
	 * @param hash The Hashtable to use
	 */      
	public InteriorData(Hashtable<String, Object> hash) {
		super(hash);
	}
	
	public InteriorDataType getInteriorDataType(){
		return (InteriorDataType) store.get(KEY_INTERIOR_DATA_TYPE);
	}

	public void setInteriorDataType(InteriorDataType type){
		if (type!=null) {
			store.put(KEY_INTERIOR_DATA_TYPE, type);
		} else {
			store.remove(KEY_INTERIOR_DATA_TYPE);
		}
	}
	
	public ControlData getControlData(){
		InteriorDataType type = getInteriorDataType();
		if(type== null){
			return null;
		}
		switch(type){
			case RADIO:
				return (RadioControlData) store.get(KEY_RADIO_DATA);
			case CLIMATE:
				//TODO return (ClimateControlData) store.get(KEY_CLIMATE_DATA);
			//Anymore cases can be added here as we add more types
			default:
				return null;
		}
		
	}

	public void setControlData(ControlData data){
		if (data!=null) {
			store.put(KEY_INTERIOR_DATA_TYPE, data);
		} else {
			store.remove(KEY_INTERIOR_DATA_TYPE);
		}
	}
}

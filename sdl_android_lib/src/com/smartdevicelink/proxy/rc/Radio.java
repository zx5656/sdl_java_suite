package com.smartdevicelink.proxy.rc;

import com.smartdevicelink.proxy.RPCMessage;
import com.smartdevicelink.proxy.rc.datatypes.InteriorZone;
import com.smartdevicelink.proxy.rc.datatypes.RadioControlData;
import com.smartdevicelink.proxy.rc.enums.ModuleType;
import com.smartdevicelink.proxy.rc.enums.RadioBand;
import com.smartdevicelink.proxy.rc.rpc.SetInteriorVehicleData;
import com.smartdevicelink.proxy.rpc.enums.ButtonName;
import com.smartdevicelink.proxy.rpc.enums.ButtonPressMode;

public class Radio extends Module{


	public Radio(InteriorZone zone) {
		super(ModuleType.RADIO,zone);
		
	}

	public RPCMessage tuneUp(){
		return this.getButtonPress(ButtonName.TUNEUP, ButtonPressMode.SHORT);
				
	}
	
	public RPCMessage tuneDown(){
		return this.getButtonPress(ButtonName.TUNEDOWN,ButtonPressMode.SHORT);
	}
	
	public RPCMessage directTune(int freqInt, int freqFrac, RadioBand band){		
		RadioControlData radio = new RadioControlData();
		radio.setFrequencyInteger(freqInt);
		radio.setFrequencyFraction(freqFrac);
		radio.setBand(band);
		return new SetInteriorVehicleData(getBaseModuleData(radio));
		
	}
	
	
}

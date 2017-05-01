package com.smartdevicelink.proxy.interfaces;

import com.smartdevicelink.protocol.enums.SessionType;

public interface ISdlServiceListener {
    public void onSdlServiceStarted(SessionType type, boolean isEncrypted);
    public void onSdlServiceStartedNacked(SessionType type);
    public void onSdlServiceEnded(SessionType type);
    public void onSdlServiceEndedNacked(SessionType type);
}

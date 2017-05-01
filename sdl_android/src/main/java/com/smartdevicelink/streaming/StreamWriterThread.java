package com.smartdevicelink.streaming;

import com.smartdevicelink.SdlConnection.SdlSession;
import com.smartdevicelink.protocol.ProtocolMessage;
import com.smartdevicelink.protocol.enums.SessionType;
import com.smartdevicelink.proxy.SdlProxyALM;

public class StreamWriterThread extends Thread {
    private Boolean isHalted = false;
    private byte[] buf = null;
    private Integer size = 0;
    private final static Object lock = new Object();
    private SessionType serviceType;
    private SdlSession sdlSession;

    private boolean isServiceProtected;
    private final static int TLS_MAX_RECORD_SIZE = 16384;
    private final static int TLS_RECORD_HEADER_SIZE = 5;
    private final static int TLS_RECORD_MES_AUTH_CDE_SIZE = 32;
    private final static int TLS_MAX_RECORD_PADDING_SIZE = 256;
    private final static int ENC_READ_SIZE = TLS_MAX_RECORD_SIZE - TLS_RECORD_HEADER_SIZE - TLS_RECORD_MES_AUTH_CDE_SIZE - TLS_MAX_RECORD_PADDING_SIZE;


    public StreamWriterThread(SdlProxyALM sdlProxy, SessionType serviceType) {
        if (sdlProxy == null){
            return;
        }
        sdlProxy.setVideoStreamWriter(this);
        this.serviceType = serviceType;
    }

    public void handleSdlSession(SdlSession sdlSession){
        if (sdlSession == null){
            return;
        }
        this.sdlSession = sdlSession;
        isServiceProtected = sdlSession.isServiceProtected(serviceType);
    }

    public byte[] getByteBuffer() {
        synchronized (lock) {
            return buf;
        }
    }

    public void setByteBuffer(byte[] buf, Integer size) {
        synchronized (lock) {
            this.buf = buf;
            this.size = size;
        }
    }

    public void clearByteBuffer() {
        synchronized (lock) {
            try {
                if (buf != null) {
                    buf = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private ProtocolMessage createStreamPacket(byte[] byteData) {
        ProtocolMessage pm = new ProtocolMessage();
        pm.setSessionID(sdlSession.getSessionId());
        pm.setSessionType(serviceType);
        pm.setFunctionID(0);
        pm.setCorrID(0);
        pm.setData(byteData, byteData.length);
        pm.setPayloadProtected(isServiceProtected);
        return pm;
    }

    public static byte[][] divideArray(byte[] source, int chunksize) {
        byte[][] ret = new byte[(int)Math.ceil(source.length / (double)chunksize)][chunksize];

        int start = 0;

        for(int i = 0; i < ret.length; i++) {
            if(start + chunksize > source.length) {
                System.arraycopy(source, start, ret[i], 0, source.length - start);
            } else {
                System.arraycopy(source, start, ret[i], 0, chunksize);
            }
            start += chunksize ;
        }

        return ret;
    }

    private void writeToStream() {
        synchronized (lock) {
            if (buf == null || sdlSession == null) return;

            try {
                if (isServiceProtected && buf.length > ENC_READ_SIZE){
                    byte[][] ret = divideArray(buf, ENC_READ_SIZE);
                    for(int i = 0; i < ret.length; i++) {
                        byte[] byteData = ret[i];
                        ProtocolMessage pm = createStreamPacket(byteData);
                        sdlSession.sendStreamPacket(pm);
                    }
                }
                else {
                    ProtocolMessage pm = createStreamPacket(buf);
                    sdlSession.sendStreamPacket(pm);
                }

                clearByteBuffer();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public void run() {
        while (!isHalted) {
            writeToStream();
        }
    }

    /**
     * Method that marks thread as halted.
     */
    public void halt() {
        isHalted = true;
    }
}

package com.smartdevicelink.streaming;

import com.smartdevicelink.SdlConnection.SdlSession;
import com.smartdevicelink.protocol.ProtocolMessage;
import com.smartdevicelink.protocol.enums.SessionType;

import static com.smartdevicelink.trace.enums.Mod.proxy;

public class StreamWriterThread extends Thread {
    private Boolean isHalted = false;
    private byte[] buf = null;
    private Integer size = 0;
    private SessionType serviceType;
    private SdlSession session;
    public Boolean isWaiting = false;
    private final static Object lock = new Object();
    public final static Object BUFFER_LOCK = new Object();

    private boolean isServiceProtected;
    private final static int TLS_MAX_RECORD_SIZE = 16384;
    private final static int TLS_RECORD_HEADER_SIZE = 5;
    private final static int TLS_RECORD_MES_AUTH_CDE_SIZE = 32;
    private final static int TLS_MAX_RECORD_PADDING_SIZE = 256;
    private final static int ENC_READ_SIZE = TLS_MAX_RECORD_SIZE - TLS_RECORD_HEADER_SIZE - TLS_RECORD_MES_AUTH_CDE_SIZE - TLS_MAX_RECORD_PADDING_SIZE;


    public StreamWriterThread(SdlSession sdlSession, SessionType serviceType) {
        this.session = sdlSession;
        this.isServiceProtected = session.isServiceProtected(serviceType);
        this.serviceType = serviceType;

        this.setName(serviceType.getName()+"StreamWriter");
        this.setPriority(Thread.MAX_PRIORITY);
        this.setDaemon(true);
    }

    public byte[] getByteBuffer() {
        synchronized (lock) {
            return buf;
        }
    }

    public void setByteBuffer(byte[] buf, Integer size){
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
        pm.setSessionID(session.getSessionId());
        pm.setSessionType(serviceType);
        pm.setFunctionID(0);
        pm.setCorrID(0);
        pm.setData(byteData, byteData.length);
        pm.setPayloadProtected(isServiceProtected);
        return pm;
    }

    private static byte[][] divideArray(byte[] source, int chunksize) {
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
            if (buf == null || proxy == null) return;

            try {
                if (isServiceProtected && buf.length > ENC_READ_SIZE){
                    byte[][] ret = divideArray(buf, ENC_READ_SIZE);
                    for(int i = 0; i < ret.length; i++) {
                        byte[] byteData = ret[i];
                        ProtocolMessage pm = createStreamPacket(byteData);
                        session.sendStreamPacket(pm);
                    }
                }
                else {
                    ProtocolMessage pm = createStreamPacket(buf);
                    session.sendStreamPacket(pm);
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
            if(isWaiting){
                synchronized (BUFFER_LOCK){
                    BUFFER_LOCK.notify();
                }
            }
        }
    }

    /**
     * Method that marks thread as halted.
     */
    public void halt() {
        isHalted = true;
    }
}

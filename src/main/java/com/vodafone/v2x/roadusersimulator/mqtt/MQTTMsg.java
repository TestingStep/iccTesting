package com.vodafone.v2x.roadusersimulator.mqtt;

public class MQTTMsg {

    private String topic;
    private byte[] payload;
    private boolean retain;
    private int senderStationID;

    public MQTTMsg(String topic,byte[] payload,boolean retain,int senderStationID) {
        this.topic=topic;
        this.payload=payload;
        this.retain=retain;
        this.senderStationID=senderStationID;
    }

    public String getTopic() {
        return topic;
    }

    public byte[] getPayload() {
        return payload;
    }

    public boolean isRetain() {
        return retain;
    }

    public int getSenderStationID() {
        return senderStationID;
    }
}

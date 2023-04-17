package com.vodafone.v2x.roadusersimulator.mqtt;

public class MQTTConfig {


    private String mqttHostName;
    private String mqttUsername;
    private String mqttPassword;
    private int mqttPort;


    public MQTTConfig(String mqttHostName, String mqttUsername, String mqttPassword, int mqttPort) {
        this.mqttHostName = mqttHostName;
        this.mqttUsername = mqttUsername;
        this.mqttPassword = mqttPassword;
        this.mqttPort = mqttPort;
    }

    public String getMqttHostName() {
        return mqttHostName;
    }

    public String getMqttUsername() {
        return mqttUsername;
    }

    public String getMqttPassword() {
        return mqttPassword;
    }

    public int getMqttPort() {
        return mqttPort;
    }



    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("mqttHostName=").append(mqttHostName).append(",");
        sb.append("mqttPort=").append(mqttPort).append(",");
        sb.append("mqttUsername=").append(mqttUsername).append(",");
        sb.append("mqttPassword=").append(mqttPassword).append(",");
        return sb.toString();
    }
}

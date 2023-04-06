package com.vodafone.v2x.roadusersimulator.mqtt;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.UUID;
import java.util.Vector;

public class Gateway implements Runnable {

    private static Logger logger = LogManager.getLogger(Gateway.class);
    private MQTTConfig mqttConfig;
    private int nberOfClients;
    private ArrayList<MQTT3Client> mqttClients = new ArrayList<>();
    private int pointer=0;
    private Vector<MQTTMsg> buffer = new Vector<>();
    private boolean isRunning = false;
    private Object lock = new Object();
    private Thread thread;
    private int counter=0;


    public Gateway(MQTTConfig mqttConfig, int nberOfClients) {
        this.mqttConfig=mqttConfig;
        this.nberOfClients=nberOfClients;
        thread=new Thread(this);

    }

    public void start() {
        logger.info("Start the Gateway");
        logger.info("Please be patient, "+nberOfClients+" mqttClients need to be created & started");
        for (int i=0;i<nberOfClients;i++) {
            MQTT3Client mqttClient = new MQTT3Client(UUID.randomUUID().toString(),
                    mqttConfig.getMqttHostName(),
                    mqttConfig.getMqttPort(),
                    mqttConfig.getMqttUsername(),
                    mqttConfig.getMqttPassword());
            mqttClient.connect();
            mqttClients.add(mqttClient);
        }
        thread.start();
        logger.info("=> Gateway up and running");
    }

    public void publish(MQTTMsg msg) {
        buffer.add(msg);
        synchronized (lock) {
            lock.notifyAll();
        }
    }


    @Override
    public void run() {
        isRunning=true;
        while(isRunning) {
            while(buffer.size()>0) {
                pointer = (pointer+1) % mqttClients.size();
                MQTTMsg msg = buffer.elementAt(0);
                mqttClients.get(pointer).publish(msg.getTopic(),msg.getPayload(),msg.isRetain());
                counter++;
                logger.info("publish ,counter="+counter+" ,pointer="+pointer+" ,topic="+msg.getTopic()+" ,sender="+msg.getSenderStationID());
                buffer.removeElementAt(0);
            }
            synchronized(lock) {
                try {
                    lock.wait(0L);
                } catch (InterruptedException e) {
                   logger.error("E62");
                }
            }
        }
    }
}

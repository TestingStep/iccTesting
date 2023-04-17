package com.vodafone.v2x.roadusersimulator.mqtt;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.MqttClientState;
import com.hivemq.client.mqtt.MqttGlobalPublishFilter;
import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;
import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;
import com.hivemq.client.mqtt.mqtt3.message.subscribe.suback.Mqtt3SubAck;
import com.hivemq.client.mqtt.mqtt5.exceptions.Mqtt5UnsubAckException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class MQTT3Client  {


    private Mqtt3AsyncClient mqttClient;
    private String clientID;
    private String mqttHost;
    private int mqttPort;
    private String mqttUsername;
    private String mqttPassword;
    private MQTT3Listener listener;
    private static Logger logger = LogManager.getLogger(MQTT3Client.class);

    public MQTT3Client(String clientID, String mqttHost, int mqttPort, String mqttUsername, String mqttPassword) {
        this.clientID=clientID;
        this.mqttHost=mqttHost;
        this.mqttPort=mqttPort;
        this.mqttUsername=mqttUsername;
        this.mqttPassword=mqttPassword;
    }

    public void setListener(MQTT3Listener listener) {
        this.listener=listener;
    }

    public boolean connect() {
        logger.debug("connect()");
        boolean result = false;
        if (mqttClient==null || mqttClient.getState()== MqttClientState.DISCONNECTED) {
            try {
                // Create an instance of MQTT5 Async client
                if (mqttPort==8883) {
                    mqttClient = MqttClient.builder()
                            .useMqttVersion3()
                            .identifier(clientID)
                            .serverHost(mqttHost)
                            .serverPort(mqttPort)
                            .sslWithDefaultConfig()
                            .buildAsync();
                }else {
                    mqttClient = MqttClient.builder()
                            .useMqttVersion3()
                            .identifier(clientID)
                            .serverHost(mqttHost)
                            .serverPort(mqttPort)
                            .buildAsync();
                }
                // Callback for incoming messages
                mqttClient.publishes(MqttGlobalPublishFilter.ALL,(publish -> processIncomingData(publish)));
                // Connect to the broker (in blocking mode)

                mqttClient.toBlocking().connectWith()
                        .cleanSession(true)
                        .simpleAuth()
                        .username(mqttUsername)
                        .password(mqttPassword.getBytes())
                        .applySimpleAuth().send().getReturnCode().getCode();
                logger.debug("-> connect succeed");
                result=true;
            }catch(Exception e) {
                if (e.getCause()!=null && e.getCause().getMessage()!=null) {
                    logger.error("-> connect failed "+e.getCause().getMessage());
                }else{
                    logger.error("-> connect failed ",e);
                }
            }

        }else{
            logger.warn("-> connect attempt skipped, client is already connected");
            result=true;
        }
        return result;
    }

    private void processIncomingData(Mqtt3Publish publish) {
        if (listener!=null) {
            listener.processMQttMessage(publish);
        }
    }



    public boolean disconnect() {
        logger.debug("Disconnect() start");
        boolean result = false;
        if (mqttClient!=null && mqttClient.getState()!= MqttClientState.DISCONNECTED) {
            mqttClient.toBlocking().disconnect();
            mqttClient = null;
            logger.debug("disconnect succeeded");
            result=true;
        }else{
            logger.warn("disconnect() called while client is already disconnected (or while client is null)");
            result=true;
        }
        return result;
    }

    public boolean subscribe(ArrayList<String> topics) {
        boolean result = false;
        if (topics != null) {
            for (int i=0;i<topics.size();i++) {
                String topic = topics.get(i);
                if (!subscribe(topic)) {
                    break;
                }
            }
            result=true;
        }
        return result;
    }

    public boolean subscribe(String topic) {
        logger.debug("subscribe() , topic="+topic);

        boolean subscribeIsSuccess=false;
        if (mqttClient != null && mqttClient.getState()== MqttClientState.CONNECTED) {
            // Subscribe in blocking mode
            try {
                Mqtt3SubAck subAck = mqttClient.toBlocking().subscribeWith()
                        .topicFilter(topic)
                        .qos(MqttQos.AT_MOST_ONCE)
                        .send();
                logger.debug("-> reason Codes="+subAck.getReturnCodes().toString());
                logger.debug("-> subscribe succeed");
                subscribeIsSuccess=true;
            }catch(Exception e) {
                logger.error("-> subscribe failed",e);
                subscribeIsSuccess=false;
            }
        }else {
            logger.error("-> subscribe failed , client is not connected");
        }
        return subscribeIsSuccess;
    }


    public boolean unsubscribe(ArrayList<String> topics) {
        boolean result = false;
        if (topics != null) {
            for (int i=0;i<topics.size();i++) {
                String topic = topics.get(i);
                if (!unsubscribe(topic)) {
                    break;
                }
            }
            result=true;
        }
        return result;
    }

    private boolean unsubscribe(String topic) {
        logger.debug("unsubscribe() ,topic="+topic);
        boolean unsubscribeIsSuccess=false;
        if (mqttClient != null && mqttClient.getState()== MqttClientState.CONNECTED) {
            try {
                // Unsubscribe (in blocking mode)
                mqttClient.toBlocking().unsubscribeWith().topicFilter(topic).send();
                unsubscribeIsSuccess=true;
                logger.debug("-> unsubscribe succeed");
            }catch(Mqtt5UnsubAckException e) {
                logger.error("-> unsubscribe failed ",e);
                unsubscribeIsSuccess=false;
            }
        }else {
            logger.error("-> unsubscribe failed , client is not connected");
        }
        return unsubscribeIsSuccess;
    }


    public boolean publish(String topic, byte[] msg, boolean retain) {
        //logger.debug("publish() topic="+topic+" payload="+ Format.byteArrayToHexString(msg));
        boolean publishIsSuccess=false;
        if (mqttClient != null && mqttClient.getState()== MqttClientState.CONNECTED) {
            Mqtt3Publish publishMessage = Mqtt3Publish.builder()
                    .topic(topic)
                    .qos(MqttQos.AT_MOST_ONCE)
                    .payload(msg)
                    .retain(retain)
                    .build();

            try {
                mqttClient.toBlocking().publish(publishMessage);
                publishIsSuccess = true;
                //logger.debug("-> publish succeed");
            }catch(Exception e) {
                logger.error("-> publish failed ",e);
                publishIsSuccess=false;
            }
        } else {
            logger.error("-> publish failed, attempt to publish while client is not connected");
            if (mqttClient == null || mqttClient.getState()== MqttClientState.DISCONNECTED) {
                connect();
            }
        }
        return publishIsSuccess;
    }
}

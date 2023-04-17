package com.vodafone.v2x.roadusersimulator.mqtt;

import com.hivemq.client.mqtt.mqtt3.message.publish.Mqtt3Publish;

public interface MQTT3Listener {

    public void processMQttMessage(Mqtt3Publish publish);
}

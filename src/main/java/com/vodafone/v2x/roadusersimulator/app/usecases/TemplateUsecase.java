package com.vodafone.v2x.roadusersimulator.app.usecases;


import com.vodafone.v2x.roadusersimulator.denm.DENMManager;
import com.vodafone.v2x.roadusersimulator.mqtt.Gateway;
import com.vodafone.v2x.roadusersimulator.mqtt.MQTTConfig;
import com.vodafone.v2x.roadusersimulator.roaduser.RoadUserType;
import com.vodafone.v2x.roadusersimulator.roaduser.RoadUsersGroup;
import com.vodafone.v2x.sdk.android.facade.enums.DENMType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TemplateUsecase {
    private final static MQTTConfig V2XBrokerConfig = new MQTTConfig("v2x-gw.ovh", "hiveuser7", "Test123456789", 8883);
    private final static MQTTConfig IOTBrokerConfig = new MQTTConfig("iot-gw.ovh", "hiveuser7", "Test123456789", 8883);
    private final static MQTTConfig STEPDEVBrokerConfig = new MQTTConfig("dev-de-mn.mqtt.step.vodafone.com", "hiveuser7", "Test123456789", 8883);
    private static Logger logger = LogManager.getLogger(TemplateUsecase.class);
    private static String GrasseGeohash = "spubmsz";


    public static void main(String[] args) {
        start();
    }


    public static void start() {
        logger.info("TemplateUsecase is starting");

        // Select the STEP EndPoint where all the message will be sent
        MQTTConfig stepInstance = V2XBrokerConfig;
        // Create a Pool of MQTT Clients
        Gateway gw = new Gateway(stepInstance, 10);
        gw.start();
        // Start the DENMManager
        DENMManager denmManager = DENMManager.getInstance();
        denmManager.start(stepInstance);
        // group1
        RoadUsersGroup rug1 = new RoadUsersGroup("pedestrian1", gw, GrasseGeohash, 10, RoadUserType.PEDESTRIAN, true);
        rug1.build();
        rug1.activate();
        rug1.setAnimation(true);
        // group2
        RoadUsersGroup rug2 = new RoadUsersGroup("bicyclist1", gw, GrasseGeohash, 10, RoadUserType.BICYCLIST, true);
        rug2.build();
        rug2.activate();
        rug2.setAnimation(true);
        // group3
        RoadUsersGroup rug3 = new RoadUsersGroup("cars1", gw, GrasseGeohash, 10, RoadUserType.PASSENGER_CAR, true);
        rug3.build();
        rug3.activate();
        rug3.setAnimation(true);
        // Trigger a DENM during 1hour
        denmManager.triggerDENM(DENMType.ADVERSE_WEATHER_CONDITION_ADHESION, 43.663951f, 6.931881f, 3600, 100);
    }


}

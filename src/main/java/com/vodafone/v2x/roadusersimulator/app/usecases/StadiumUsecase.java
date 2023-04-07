package com.vodafone.v2x.roadusersimulator.app.usecases;

import com.vodafone.v2x.roadusersimulator.app.AppProperties;
import com.vodafone.v2x.roadusersimulator.denm.DENMManager;
import com.vodafone.v2x.roadusersimulator.mqtt.Gateway;
import com.vodafone.v2x.roadusersimulator.mqtt.MQTTConfig;
import com.vodafone.v2x.roadusersimulator.roaduser.RoadUserType;
import com.vodafone.v2x.roadusersimulator.roaduser.RoadUsersGroup;
import com.vodafone.v2x.sdk.android.facade.enums.DENMType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StadiumUsecase {

    private final static MQTTConfig V2XBrokerConfig = new MQTTConfig("v2x-gw.ovh", "hiveuser7", "Test123456789", 8883);
    private final static MQTTConfig IOTBrokerConfig = new MQTTConfig("iot-gw.ovh", "hiveuser7", "Test123456789", 8883);
    private final static MQTTConfig STEPDEVBrokerConfig = new MQTTConfig("dev-de-mn.mqtt.step.vodafone.com", "hiveuser7", "Test123456789", 8883);
    private static Logger logger = LogManager.getLogger(StadiumUsecase.class);
    private static String AllianzRivieraStadium_Center = "spv0esm";
    private static String AllianzRivieraStadium_North = "spv0est";
    private static String AllianzRivieraStadium_East = "spv0esq";
    private static String AllianzRivieraStadium_West = "spv0esk";
    private static String AllianzRivieraStadium_NE = "spv0esw";
    private static String AllianzRivieraStadium_NW = "spv0ess";
    private static String AllianzRivieraStadium_South = "spv0esj";
    private static String AllianzRivieraStadium_SE = "spv0esn";
    private static String AllianzRivieraStadium_SW = "spv0esh";


    public static void main(String[] args) {
        start();
    }


    public static void start() {
        logger.info("HighDensityOfPedestrian is starting");

        // Select the STEP EndPoint where all the message will be sent
        MQTTConfig stepInstance = V2XBrokerConfig;

        // Create a Pool of MQTT Clients
        Gateway gw = new Gateway(V2XBrokerConfig, 50);
        gw.start();

        // Start the DENMManager
        DENMManager denmManager = DENMManager.getInstance();
        denmManager.start(stepInstance);

        int numberOfPedestrianPerGroup=30;

        RoadUsersGroup rug1 = new RoadUsersGroup("pedestrian1", gw, AllianzRivieraStadium_Center, numberOfPedestrianPerGroup, RoadUserType.PEDESTRIAN, true);
        rug1.build();
        rug1.activate();
        rug1.setAnimation(true);
        RoadUsersGroup rug2 = new RoadUsersGroup("pedestrian2", gw, AllianzRivieraStadium_South, numberOfPedestrianPerGroup, RoadUserType.PEDESTRIAN, true);
        rug2.build();
        rug2.activate();
        rug2.setAnimation(true);

        // Trigger DENM1
        denmManager.triggerDENM(DENMType.ADVERSE_WEATHER_CONDITION_ADHESION,43.705731f,7.189140f,3600,500);

        // Trigger DENM2
        denmManager.triggerDENM(DENMType.ACCIDENT,43.709980f,7.197994f,3600,500);


    }
}

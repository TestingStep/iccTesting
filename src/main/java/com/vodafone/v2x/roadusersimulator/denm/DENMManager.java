package com.vodafone.v2x.roadusersimulator.denm;


import com.vodafone.v2x.roadusersimulator.mqtt.MQTTConfig;
import com.vodafone.v2x.sdk.android.facade.InvalidConfigException;
import com.vodafone.v2x.sdk.android.facade.LocationProvider;
import com.vodafone.v2x.sdk.android.facade.SDKConfiguration;
import com.vodafone.v2x.sdk.android.facade.V2XSDK;
import com.vodafone.v2x.sdk.android.facade.enums.DENMType;
import com.vodafone.v2x.sdk.android.facade.enums.LogLevel;
import com.vodafone.v2x.sdk.android.facade.enums.V2XConnectivityState;
import com.vodafone.v2x.sdk.android.facade.enums.V2XServiceState;
import com.vodafone.v2x.sdk.android.facade.models.GpsLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DENMManager {

    private static V2XSDK v2xsdk;
    private static Logger logger = LogManager.getLogger(DENMManager.class);
    private static DENMManager instance = new DENMManager();
    private MQTTConfig mqttConfig;
    private boolean isUpAndRunning = false;

    //Constructor
    private DENMManager() {

    }

    public static DENMManager getInstance() {
        return instance;
    }

    public void start(MQTTConfig mqttConfig) {
        try {
            logger.info("Start the DENMManager");
            this.mqttConfig = mqttConfig;
            v2xsdk = V2XSDK.getInstance();

            v2xsdk.setLogLevel(LogLevel.LEVEL_NONE);
            SDKConfiguration.SDKConfigurationBuilder config = new SDKConfiguration.SDKConfigurationBuilder();
            LocationProvider myLocationProvider = MyGNSSReceiver.getInstance();
            config.withLocationProvider(myLocationProvider);

            //--- STEP ENDPoint
            config.withMqttHost(mqttConfig.getMqttHostName());
            config.withMqttPort(mqttConfig.getMqttPort());
            config.withMqttUsername(mqttConfig.getMqttUsername());    //ApplicationID
            config.withMqttPassword(mqttConfig.getMqttPassword());    //ApplicationToken
            v2xsdk.initV2XService(config.build());

            v2xsdk.startV2XService();

            // Wait the completion of the SDK initialization
            logger.info("* Wait the completion of the V2X SDK initialization");
            long startTime = System.currentTimeMillis();
            while (v2xsdk.getV2XServiceState() != V2XServiceState.UP_AND_RUNNING) {
                try {
                    logger.debug(" .");
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    logger.error("E41", e);
                }
            }
            long endTime = System.currentTimeMillis();
            logger.info("* SDK initialized in " + (endTime - startTime) + " ms");


            // Start CAM & DENM Services
            v2xsdk.startCAMService();
            v2xsdk.startDENMService();


            // Wait until connection with STEP endpoint is established
            logger.info("* Wait until the module is able to establish the connection with the STEP instance");
            while (v2xsdk.getV2XConnectivityState() != V2XConnectivityState.CONNECTED) {
                try {
                    logger.debug(" + V2XConnectivity = " + v2xsdk.getV2XConnectivityState().name());
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    logger.error("E65", e);
                }
            }
            isUpAndRunning=true;
            logger.info("=> DENMManager is now up and running");


        }catch (InvalidConfigException e) {
            logger.error("E36", e);
        }

    }

    public void triggerDENM(DENMType denmType,GpsLocation location) {
        if (isUpAndRunning) {
            v2xsdk.denmTrigger(denmType,location);
            logger.info("DENM triggered");
        }
    }

    public void triggerDENM(DENMType denmType, float latitude,float longitude, int durationInSeconds,int relevantDistanceInMeters) {
        if (isUpAndRunning) {
            GpsLocation denmLocation = new GpsLocation(latitude, longitude, 450d, 0f, 0f, null, V2XSDK.getInstance().getUTCTimeInMs());
            v2xsdk.denmTrigger(denmType.getCause(), denmType.getSubCause(), denmLocation, durationInSeconds, 1000, relevantDistanceInMeters);
            logger.info("DENM triggered ,lat=" + latitude + ",lon=" + longitude + ",denmType=" + denmType + ",durationInSecond=" + durationInSeconds + ",relevantDistance=" + relevantDistanceInMeters);
        }else {
            logger.error("DENM not triggered");
        }
    }
}

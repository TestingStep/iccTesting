package com.vodafone.v2x.roadusersimulator.roaduser;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import com.vodafone.v2x.roadusersimulator.mqtt.Gateway;
import com.vodafone.v2x.roadusersimulator.mqtt.MQTTMsg;
import com.vodafone.v2x.sdk.android.facade.LatLng;
import com.vodafone.v2x.sdk.android.facade.V2XSDK;
import com.vodafone.v2x.sdk.android.facade.enums.StationType;
import com.vodafone.v2x.sdk.android.facade.enums.VAMProfile;
import com.vodafone.v2x.sdk.android.facade.records.vam.PathPointElement;
import com.vodafone.v2x.sdk.android.facade.records.vam.VAMRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class RoadUserPedestrian extends RoadUser {

    private static Logger logger = LogManager.getLogger(RoadUserPedestrian.class);
    private Gateway gateway;
    private GeoHash geohash ;
    private double latitude=0d;
    private double longitude=0d;
    private float headingInDegree = 0f;
    private float altitudeInMeter = 0f;
    private float speedInKmH = 0f;
    private VAMProfile vamProfile = VAMProfile.PEDESTRIAN_ORDINARY;
    private boolean isDynamic;


    public RoadUserPedestrian(Gateway gateway, String geohashString,boolean isDynamic) {
        super(StationType.PEDESTRIAN);
        this.gateway=gateway;
        this.geohash = GeoHash.fromGeohashString(geohashString);
        this.isDynamic=isDynamic;
        initializeLocation();

    }

    private void initializeLocation() {
        double latitudeSize = geohash.getBoundingBox().getLatitudeSize();
        double longitudeSize = geohash.getBoundingBox().getLongitudeSize();
        double deltaLat = Math.random()*latitudeSize;
        double deltaLong = Math.random()*longitudeSize;
        longitude = geohash.getBoundingBox().getWestLongitude()+deltaLong;
        latitude= geohash.getBoundingBox().getSouthLatitude()+deltaLat;
        headingInDegree = (float) (Math.random()*360d);
        if (isDynamic) {
            speedInKmH = (float) (Math.random() * 10d) + 1f;
        }else {
            speedInKmH=0f;
        }
    }


    @Override
    protected void computeNewLocation() {
        LatLng from = new LatLng(latitude,longitude);
        LatLng to = predict(from,headingInDegree,speedInKmH,1000L);
        latitude=to.getLatitude();
        longitude=to.getLongitude();
        if (!geohash.contains(new WGS84Point(latitude,longitude))) {
            initializeLocation();
        }
    }


    @Override
    protected void publishNewLocation() {
        if (gateway!=null) {
            V2XSDK v2xSdk = V2XSDK.getInstance();
            VAMRecord vamRecord = new VAMRecord(v2xSdk.getUTCTimeInMs(),
                    stationID,
                    stationType.getValue(),
                    (float)latitude,
                    (float)longitude,
                    altitudeInMeter,
                    speedInKmH,
                    headingInDegree,
                    0f,
                    vamProfile.getProfile(),
                    vamProfile.getSubProfile(),
                    new ArrayList<PathPointElement>(),
                    new ArrayList<PathPointElement>(),
                    5

            ) ;
            byte[] binaryVAMMsg = v2xSdk.buildGNPacketFromVAMRecord(vamRecord);
            if (binaryVAMMsg!=null && binaryVAMMsg.length>0) {
                String topic = computeTopic("vam",latitude,longitude);
                gateway.publish(new MQTTMsg(topic,binaryVAMMsg,false,stationID));

            }
        }
    }


}

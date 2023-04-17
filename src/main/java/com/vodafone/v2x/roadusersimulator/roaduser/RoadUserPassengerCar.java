package com.vodafone.v2x.roadusersimulator.roaduser;

import ch.hsr.geohash.GeoHash;
import ch.hsr.geohash.WGS84Point;
import com.vodafone.v2x.roadusersimulator.mqtt.Gateway;
import com.vodafone.v2x.roadusersimulator.mqtt.MQTTMsg;
import com.vodafone.v2x.sdk.android.facade.LatLng;
import com.vodafone.v2x.sdk.android.facade.V2XSDK;
import com.vodafone.v2x.sdk.android.facade.enums.StationType;
import com.vodafone.v2x.sdk.android.facade.enums.VehicleRole;
import com.vodafone.v2x.sdk.android.facade.records.cam.CAMRecord;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RoadUserPassengerCar extends RoadUser {
    private static Logger logger = LogManager.getLogger(RoadUserPassengerCar.class);
    private Gateway gateway;
    private GeoHash geohash ;
    private double latitude=0d;
    private double longitude=0d;
    private float headingInDegree = 0f;
    private float altitudeInMeter = 0f;
    private float speedInKmH = 0f;
    private boolean isDynamic;



    public RoadUserPassengerCar(Gateway gateway, String geohashString,boolean isDynamic) {
        super(StationType.PASSENGER_CAR);
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
            speedInKmH = (float) (Math.random() * 130d) + 1f;
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
            CAMRecord camRecord = new CAMRecord(
                    v2xSdk.getUTCTimeInMs(),
                    stationID,
                    stationType.getValue(),
                    VehicleRole.DEFAULT.getValue(),
                    (float)latitude,
                    (float)longitude,
                    speedInKmH,
                    headingInDegree,
                    5,
                    20L,
                    40L
                    );
            byte[] binaryCAMMsg = v2xSdk.buildGNPacketFromCAMRecord(camRecord);
            if (binaryCAMMsg!=null && binaryCAMMsg.length>0) {
                String topic = computeTopic("cam",latitude,longitude);
                gateway.publish(new MQTTMsg(topic,binaryCAMMsg,false,stationID));
            }
        }
    }
}

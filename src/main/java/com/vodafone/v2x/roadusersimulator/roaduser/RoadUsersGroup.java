package com.vodafone.v2x.roadusersimulator.roaduser;


import com.vodafone.v2x.roadusersimulator.mqtt.Gateway;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class RoadUsersGroup implements RoadUserInterface {

    private static Logger logger = LogManager.getLogger(RoadUsersGroup.class);
    private ArrayList<RoadUser> groupList;
    private int numberOfRoadUsers;
    private RoadUserType roadUserType;
    private Gateway gateway;
    private String geohashString;
    private String groupName;
    private boolean isDynamic;

    public RoadUsersGroup(String groupName, Gateway gateway, String geohashString, int numberOfRoadUsers , RoadUserType roadUserType, boolean isDynamic) {
        this.groupName=groupName;
        this.numberOfRoadUsers=numberOfRoadUsers;
        this.roadUserType=roadUserType;
        this.gateway=gateway;
        this.geohashString=geohashString;
        this.isDynamic=isDynamic;
        groupList = new ArrayList<>();
    }

    public void build() {
        logger.info("Build a the group "+groupName+" composed by "+numberOfRoadUsers+" "+roadUserType.toString()+"(s)");
        for (int i=0;i<numberOfRoadUsers;i++) {
            addRoadUser(roadUserType,isDynamic);
        }
        logger.info("=> group "+groupName+" created");
    }

    private void addRoadUser(RoadUserType roadUserType,boolean isDynamic) {
        if (roadUserType == RoadUserType.PEDESTRIAN) {
            groupList.add(new RoadUserPedestrian(gateway,geohashString,isDynamic));
        }else if (roadUserType == RoadUserType.BICYCLIST) {
            groupList.add(new RoadUserBicyclist(gateway,geohashString,isDynamic));
        }else if (roadUserType == RoadUserType.PASSENGER_CAR) {
            groupList.add(new RoadUserPassengerCar(gateway,geohashString,isDynamic));
        }else {
            logger.error("E38 RoadUserType NOT supported");
        }
    }

    @Override
    public void activate() {
        for (RoadUser ru : groupList) {
            ru.activate();
        }
    }

    @Override
    public void suspend() {
        for (RoadUser ru : groupList) {
            ru.suspend();
        }
    }

    @Override
    public void setAnimation(boolean request) {
        for (RoadUser ru : groupList) {
            ru.setAnimation(request);
        }
    }
}

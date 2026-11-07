package com.example.demo;

import java.util.HashMap;
import java.util.Map;

public class BeaconService {
    public BeaconService(){

    }

    // When the client recieves the response it will return ip and system info
    
    public Map<String, Object>sendResponse(){
        Map<String, Object> beaconMap = new HashMap<>();
        beaconMap.put("response", "confirmed");

        return beaconMap;
    }
}

package com.keemsa.tourguide.place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sebastian on 05/07/16.
 */
public class Park extends OutdoorsPlace {

    private List<String> facilities;

    public Park(String name, String mapLocation) {
        super(name, mapLocation);
        this.facilities = new ArrayList<String>();
    }

    public Park(String name, String mapLocation, String[] facilities) {
        super(name, mapLocation);
        this.facilities = Arrays.asList(facilities);
    }


    public Park(String name, String mapLocation, String facilities) {
        super(name, mapLocation);
        this.facilities = new ArrayList<String>();

        String[] aFacilities = facilities.split(",");
        for (String fac : aFacilities) {
            this.facilities.add(fac.trim());
            ;
        }
    }

    public List<String> getFacilities() {
        return facilities;
    }

    public void setFacilities(List<String> facilities) {
        this.facilities = facilities;
    }

    public void setFacilities(String[] facilities) {
        this.facilities = Arrays.asList(facilities);
    }

    public String getFacilitiesAsString() {
        String sFacilities = "";
        int c = 0;
        for (String fac : this.facilities) {
            if (c != 0)
                sFacilities += ", ";

            sFacilities += fac;
            c++;
        }

        return sFacilities;
    }
}

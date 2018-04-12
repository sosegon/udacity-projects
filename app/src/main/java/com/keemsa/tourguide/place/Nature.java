package com.keemsa.tourguide.place;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by sebastian on 05/07/16.
 */
public class Nature extends OutdoorsPlace {

    private List<String> attractions;

    public Nature(String name, String mapLocation) {
        super(name, mapLocation);
        this.attractions = new ArrayList<String>();
    }

    public Nature(String name, String mapLocation, String[] attractions) {
        super(name, mapLocation);
        this.attractions = Arrays.asList(attractions);
    }

    public Nature(String name, String mapLocation, String attractions) {
        super(name, mapLocation);
        this.attractions = new ArrayList<String>();

        String[] aAttractions = attractions.split(",");
        for (String att : aAttractions) {
            this.attractions.add(att.trim());
        }
    }

    public List<String> getAttractions() {
        return attractions;
    }

    public void setAttractions(List<String> attractions) {
        this.attractions = attractions;
    }

    public void setFacilities(String[] attractions) {
        this.attractions = Arrays.asList(attractions);
    }

    public String getAttractionsAsString() {
        String sAttractions = "";
        int c = 0;
        for (String att : this.attractions) {
            if (c != 0)
                sAttractions += ", ";

            sAttractions += att;
            c++;
        }

        return sAttractions;
    }
}

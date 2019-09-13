package com.theindiecorp.grocera.Data;

public class Sorter {
    String garageId;
    Float sortingParameter;

    public Sorter(String garageId, Float avgRating) {
        this.garageId = garageId;
        this.sortingParameter = avgRating;
    }

    public String getGarageId() {
        return garageId;
    }

    public void setGarageId(String garageId) {
        this.garageId = garageId;
    }

    public Float getRating() {
        return sortingParameter;
    }

    public void setRating(Float avgRating) {
        this.sortingParameter = avgRating;
    }
}

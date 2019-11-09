package com.theindiecorp.grocera.Data;

public class Sorter {
    Float sortingParameter;
    ShopDetails shopDetails;

    public Sorter(ShopDetails shopDetails, Float avgRating) {
        this.shopDetails = shopDetails;
        this.sortingParameter = avgRating;
    }

    public ShopDetails getShopDetails() {
        return shopDetails;
    }

    public void setShopDetails(ShopDetails shopDetails) {
        this.shopDetails = shopDetails;
    }

    public Float getRating() {
        return sortingParameter;
    }

    public void setRating(Float avgRating) {
        this.sortingParameter = avgRating;
    }
}

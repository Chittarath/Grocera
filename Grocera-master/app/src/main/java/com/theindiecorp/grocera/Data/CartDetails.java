package com.theindiecorp.grocera.Data;

public class CartDetails {
    String productId;

    public String getShopId() {
        return shopId;
    }

    public void setShopId(String shopId) {
        this.shopId = shopId;
    }

    String shopId;
    int quantity;
    private Double pricePerPiece,discount;

    public Double getDiscount() {
        return discount;
    }

    public void setDiscount(Double discount) {
        this.discount = discount;
    }

    public Double getPricePerPiece() {
        return pricePerPiece;
    }

    public void setPricePerPiece(Double pricePerPiece) {
        this.pricePerPiece = pricePerPiece;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}

package com.example.grocery.models;

public class ModelPromotion {

    // Fields for promotion data
     String id,promoCode,description,promoPrice,minimumOrderPrice,expireDate,
            timestamp;

    public ModelPromotion() {
    }

    public ModelPromotion(String id, String promoCode, String description, String promoPrice, String minimumOrderPrice, String expireDate, String timestamp) {
        this.id = id;
        this.promoCode = promoCode;
        this.description = description;
        this.promoPrice = promoPrice;
        this.minimumOrderPrice = minimumOrderPrice;
        this.expireDate = expireDate;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPromoPrice() {
        return promoPrice;
    }

    public void setPromoPrice(String promoPrice) {
        this.promoPrice = promoPrice;
    }

    public String getMinimumOrderPrice() {
        return minimumOrderPrice;
    }

    public void setMinimumOrderPrice(String minimumOrderPrice) {
        this.minimumOrderPrice = minimumOrderPrice;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
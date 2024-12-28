package com.dapittriandidev.patani.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Cart implements Parcelable {
    private String imageProduct;
    private String cartId;
    private String productId;

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getImageProduct() {
        return imageProduct;
    }

    public void setImageProduct(String imageProduct) {
        this.imageProduct = imageProduct;
    }

    public String getCartId() {
        return cartId;
    }

    public void setCartId(String cartId) {
        this.cartId = cartId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(Double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String productName;
    private Double productPrice;

    private String productImage;
    private int quantity;

    private Double totalPrice;
    private String userId;

    //    // Getters and Setters
//    public String getImageProduct() {
//        return imageProduct;
//    }

//    public void setImageProduct(String imageProduct) {
//        this.imageProduct = imageProduct;
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.imageProduct);
        dest.writeString(this.cartId);
        dest.writeString(this.productId);
        dest.writeString(this.productName);
        dest.writeValue(this.productPrice);
        dest.writeString(this.productImage);
        dest.writeInt(this.quantity);
        dest.writeValue(this.totalPrice);
        dest.writeString(this.userId);
    }

    public Cart() {
    }

    protected Cart(Parcel in) {
        this.imageProduct = in.readString();
        this.cartId = in.readString();
        this.productId = in.readString();
        this.productName = in.readString();
        this.productPrice = (Double) in.readValue(Double.class.getClassLoader());
        this.productImage = in.readString();
        this.quantity = in.readInt();
        this.totalPrice = (Double) in.readValue(Double.class.getClassLoader());
        this.userId = in.readString();
    }

    public static final Creator<Cart> CREATOR = new Creator<Cart>() {
        @Override
        public Cart createFromParcel(Parcel source) {
            return new Cart(source);
        }

        @Override
        public Cart[] newArray(int size) {
            return new Cart[size];
        }
    };
}

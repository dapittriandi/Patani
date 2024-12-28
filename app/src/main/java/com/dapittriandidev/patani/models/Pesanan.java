package com.dapittriandidev.patani.models;

import com.google.firebase.Timestamp;

public class Pesanan {
    private String orderId;
    private String petaniId;
    private String namaPenerima;
    private String noHP;
    private Timestamp orderDate; // Gunakan Timestamp Firebase
    private String estimasi;
    private String paymentMethod;
    private String productId;
    private String imageProduct;
    private String productName;
    private double productPrice;
    private int quantity;
    private String statusPesanan;
    private double totalPrice;
    private String alamat;
    private String pembeliId;
    private Timestamp tanggalPesananDiterima;

    // Constructor kosong diperlukan untuk Firestore
    public Pesanan() {
    }

    // Constructor lengkap
    public Pesanan(String orderId, String petaniId, String namaPenerima, String noHP, Timestamp orderDate, String estimasi,
                   String paymentMethod, String productId, String imageProduct, String productName, double productPrice,
                   int quantity, String statusPesanan, double totalPrice, String alamat, String pembeliId, Timestamp tanggalPesananDiterima) {
        this.orderId = orderId;
        this.petaniId = petaniId;
        this.namaPenerima = namaPenerima;
        this.noHP = noHP;
        this.orderDate = orderDate;
        this.estimasi = estimasi;
        this.paymentMethod = paymentMethod;
        this.productId = productId;
        this.imageProduct = imageProduct;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.statusPesanan = statusPesanan;
        this.totalPrice = totalPrice;
        this.alamat = alamat;
        this.pembeliId = pembeliId;
        this.tanggalPesananDiterima = tanggalPesananDiterima;
    }

    public Timestamp getTanggalPesananDiterima() {
        return tanggalPesananDiterima;
    }

    public void setTanggalPesananDiterima(Timestamp tanggalPesananDiterima) {
        this.tanggalPesananDiterima = tanggalPesananDiterima;
    }

    // Getter dan Setter
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPetaniId() {
        return petaniId;
    }

    public void setPetaniId(String petaniId) {
        this.petaniId = petaniId;
    }

    public String getNamaPenerima() {
        return namaPenerima;
    }

    public void setNamaPenerima(String namaPenerima) {
        this.namaPenerima = namaPenerima;
    }

    public String getNoHP() {
        return noHP;
    }

    public void setNoHP(String noHP) {
        this.noHP = noHP;
    }

    public Timestamp getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Timestamp orderDate) {
        this.orderDate = orderDate;
    }

    public String getEstimasi() {
        return estimasi;
    }

    public void setEstimasi(String estimasi) {
        this.estimasi = estimasi;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getImageProduct() {
        return imageProduct;
    }

    public void setImageProduct(String imageProduct) {
        this.imageProduct = imageProduct;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getStatusPesanan() {
        return statusPesanan;
    }

    public void setStatusPesanan(String statusPesanan) {
        this.statusPesanan = statusPesanan;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getPembeliId() {
        return pembeliId;
    }

    public void setPembeliId(String pembeliId) {
        this.pembeliId = pembeliId;
    }
}

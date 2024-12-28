package com.dapittriandidev.patani.models;

public class Produk {
    private String id;
    private String nama;
    private String deskripsi;
    private double harga;
    private String ketahanan;
    private int kuantitas;
    private String jenis;
    private String penjualId;

    public String getImageProduct() {
        return imageProduct;
    }

    public void setImageProduct(String imageProduct) {
        this.imageProduct = imageProduct;
    }

    private String imageProduct;

//    private String imageUrl;

    // Constructor tanpa argumen (diperlukan oleh Firebase)
    public Produk( ) {
    }

    // Constructor dengan argumen (opsional, untuk mempermudah inisialisasi objek)
    public Produk(String id, String nama, String deskripsi, double harga, String ketahanan, int kuantitas, String jenis, String penjualId, String imageProduct) {
        this.id = id;
        this.nama = nama;
        this.deskripsi = deskripsi;
        this.harga = harga;
        this.ketahanan = ketahanan;
        this.kuantitas = kuantitas;
        this.jenis = jenis;
        this.penjualId = penjualId;
        this.imageProduct =imageProduct;
    }


    // Getter dan Setter untuk semua variabel
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNama() {
        return nama;
    }

    public void setNama(String nama) {
        this.nama = nama;
    }

    public String getDeskripsi() {
        return deskripsi;
    }

    public void setDeskripsi(String deskripsi) {
        this.deskripsi = deskripsi;
    }

    public Double getHarga() {
        return harga;
    }

    public void setHarga(double harga) {
        this.harga = harga;
    }

    public String getKetahanan() {
        return ketahanan;
    }

    public void setKetahanan(String ketahanan) {
        this.ketahanan = ketahanan;
    }

    public int getKuantitas() {
        return kuantitas;
    }

    public void setKuantitas(int kuantitas) {
        this.kuantitas = Math.max(kuantitas, 0);
    }

    public String getJenis() {
        return jenis;
    }

    public void setJenis(String jenis) {
        this.jenis = jenis;
    }
    public String getPenjualId() {
        return penjualId;
    }

    public void setPenjualId(String penjualId) {
        this.penjualId = penjualId;
    }
}

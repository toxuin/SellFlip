package ru.toxuin.sellflip.entities;

import java.util.Date;

public class SingleAd {
    private String title;
    private float price; // -1 = Please contact, 0 = free
    private String email;
    private String phone;
    private String category;
    private String description;
    private Coordinates coords;
    private Date date;

    public SingleAd(String title, float price, String email, String phone, String category, String description, Coordinates coord, Date date) {
        this.title = title;
        this.price = price;
        this.email = email;
        this.phone = phone;
        this.category = category;
        this.description = description;
        this.coords = coord;
        this.date = date;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof SingleAd)) return false;
        SingleAd that = (SingleAd) other;

        return this.getTitle().equals(that.getTitle()) &&
                this.getPrice() == that.getPrice() &&
                this.getEmail().equals(that.getEmail()) &&
                this.getPhone().equals(that.getPhone()) &&
                this.getCategory().equals(that.getCategory()) &&
                this.getDescription().equals(that.getDescription()) &&
                this.getCoords().equals(that.getCoords()) &&
                this.getDate().equals(that.getDate());
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public float getPrice() {
        return price;
    }
    public void setPrice(float price) {
        this.price = price;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        this.category = category;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Coordinates getCoords() {
        return coords;
    }
    public void setCoords(Coordinates coords) {
        this.coords = coords;
    }

    public Date getDate() {
        return date;
    }
    public void setDate(Date date) {
        this.date = date;
    }
}

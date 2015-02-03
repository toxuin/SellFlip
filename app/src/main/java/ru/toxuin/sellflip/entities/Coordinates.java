package ru.toxuin.sellflip.entities;

public class Coordinates {
    private float lat;
    private float lng;
    private float radius;

    public Coordinates(float lat, float lng, float radius) {
        this.lat = lat;
        this.lng = lng;
        this.radius = radius;
    }

    public float getLat() {
        return lat;
    }
    public float getLng() {
        return lng;
    }
    public float getRadius() {
        return radius;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }
    public void setLng(float lng) {
        this.lng = lng;
    }
    public void setRadius(float radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Coordinates))return false;
        Coordinates that = (Coordinates) other;
        return this.getRadius() == that.getRadius() &&
                this.getLat() == that.getLat() &&
                this.getLng() == that.getLng();
    }
}

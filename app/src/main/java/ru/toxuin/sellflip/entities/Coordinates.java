package ru.toxuin.sellflip.entities;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.maps.model.LatLng;

public class Coordinates implements Parcelable {
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

    @Override
    public int describeContents() {
        return hashCode();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeFloat(lat);
        out.writeFloat(lng);
        out.writeFloat(radius);
    }

    public static final Parcelable.Creator<Coordinates> CREATOR = new Parcelable.Creator<Coordinates>() {
        public Coordinates createFromParcel(Parcel in) {
            return new Coordinates(in.readFloat(), in.readFloat(), in.readFloat());
        }

        public Coordinates[] newArray(int size) {
            return new Coordinates[size];
        }
    };

    public LatLng getLatLng() {
        return new LatLng(lat, lng);
    }
}

package cybulskij.nazar.avoidpoints.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nazar on 22.10.15.
 */
public class Line {

    @SerializedName("vehicle")
    Vehicle  vehicle;



    @SerializedName("short_name")
    String short_name;



    @SerializedName("name")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }





    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public String getShort_name() {
        return short_name;
    }

    public void setShort_name(String short_name) {
        this.short_name = short_name;
    }




}

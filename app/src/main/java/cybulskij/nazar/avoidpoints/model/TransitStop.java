package cybulskij.nazar.avoidpoints.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nazar on 22.10.15.
 */
public class TransitStop {
    @SerializedName("name")
    String name;

    @SerializedName("location")
    Location location;

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

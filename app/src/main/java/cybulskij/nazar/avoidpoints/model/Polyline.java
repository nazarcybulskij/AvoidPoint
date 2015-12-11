package cybulskij.nazar.avoidpoints.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nazar on 25.11.15.
 */
public class Polyline {

    @SerializedName("points")
    String points;

    public String getPoints() {
        return points;
    }

    public void setPoints(String points) {
        this.points = points;
    }
}

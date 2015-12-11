package cybulskij.nazar.avoidpoints.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by nazar on 22.10.15.
 */
public class Step {

    @SerializedName("html_instructions")
    String html_instructions;
    @SerializedName("duration")
    Duration duration;
    @SerializedName("distance")
    Distance distance;
    @SerializedName("end_location")
    Location end_location;
    @SerializedName("transit_details")
    TransitDetail detail;
    @SerializedName("start_location")
    Location start_location;
    @SerializedName("travel_mode")
    String travel_mode;
    @SerializedName("steps")
    ArrayList<Step> steps;
    @SerializedName("polyline")
    Polyline polyline;



    public String getTravel_mode() {
        return travel_mode;
    }

    public void setTravel_mode(String travel_mode) {
        this.travel_mode = travel_mode;
    }

    public TransitDetail getDetail() {
        return detail;
    }

    public void setDetail(TransitDetail detail) {
        this.detail = detail;
    }


    public String getHtml_instructions() {
        return html_instructions;
    }

    public void setHtml_instructions(String html_instructions) {
        this.html_instructions = html_instructions;
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public Location getEnd_location() {
        return end_location;
    }

    public void setEnd_location(Location end_location) {
        this.end_location = end_location;
    }

    public Location getStart_location() {
        return start_location;
    }

    public void setStart_location(Location start_location) {
        this.start_location = start_location;
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }

    public void setSteps(ArrayList<Step> steps) {
        this.steps = steps;
    }


    public Polyline getPolyline() {
        return polyline;
    }

    public void setPolyline(Polyline polyline) {
        this.polyline = polyline;
    }
}

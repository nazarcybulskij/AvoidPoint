package cybulskij.nazar.avoidpoints.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by nazar on 22.10.15.
 */
public class TransitDetail {



    @SerializedName("line")
    Line line;

    @SerializedName("departure_stop")
    TransitStop departure_stop;

    @SerializedName("arrival_stop")
    TransitStop arrival_stop;

    @SerializedName("arrival_time")
    TransitTime arrival_time;

    @SerializedName("departure_time")
    TransitTime departure_time;

    @SerializedName("headsign")
    String headsign;

    @SerializedName("headway")
    String headway;

    @SerializedName("num_stops")
    int num_stops;




    public Line getLine() {
        return line;
    }

    public void setLine(Line line) {
        this.line = line;
    }


    public TransitStop getArrival_stop() {
        return arrival_stop;
    }

    public void setArrival_stop(TransitStop arrival_stop) {
        this.arrival_stop = arrival_stop;
    }

    public int getNum_stops() {
        return num_stops;
    }

    public void setNum_stops(int num_stops) {
        this.num_stops = num_stops;
    }

    public String getHeadway() {
        return headway;
    }

    public void setHeadway(String headway) {
        this.headway = headway;
    }

    public String getHeadsign() {
        return headsign;
    }

    public void setHeadsign(String headsign) {
        this.headsign = headsign;
    }

    public TransitTime getDeparture_time() {
        return departure_time;
    }

    public void setDeparture_time(TransitTime departure_time) {
        this.departure_time = departure_time;
    }

    public TransitStop getDeparture_stop() {
        return departure_stop;
    }

    public void setDeparture_stop(TransitStop departure_stop) {
        this.departure_stop = departure_stop;
    }

    public TransitTime getArrival_time() {
        return arrival_time;
    }

    public void setArrival_time(TransitTime arrival_time) {
        this.arrival_time = arrival_time;
    }
}

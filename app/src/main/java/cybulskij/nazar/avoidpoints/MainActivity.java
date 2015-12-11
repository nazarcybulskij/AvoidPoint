package cybulskij.nazar.avoidpoints;

import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;


import cybulskij.nazar.avoidpoints.adapter.AutoCompleteEventLocationAdapter;
import cybulskij.nazar.avoidpoints.model.Location;
import cybulskij.nazar.avoidpoints.model.Step;
import cybulskij.nazar.avoidpoints.network.DirectionService;
import cybulskij.nazar.avoidpoints.network.ServiceGenerator;
import cybulskij.nazar.avoidpoints.util.DistanceUtil;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class MainActivity extends FragmentActivity implements View.OnClickListener {


    AutoCompleteTextView from;
    AutoCompleteTextView to;
    Button mLoadDirections;
    SupportMapFragment mapFragment;
    GoogleMap map;
    TextView mJsonTextView;

    Button  prev;
    Bundle next;
    int currentposution = 0;

    TextView mLine;
    DirectionService service;
    String mode = "driving";

    ArrayList<Step> stepslist = new ArrayList<Step>();

    ArrayList<ArrayList<Step>> allRoutes=new ArrayList<ArrayList<Step>>();

    private static final LatLngBounds BOUNDS_GREATER_MOSCOW = new LatLngBounds(
            new LatLng(55.151244, 37.018423), new LatLng(56.551244, 38.318423));

    int []colors = {Color.BLACK,Color.BLUE,Color.GRAY,Color.CYAN,Color.GREEN,Color.RED,Color.MAGENTA,Color.YELLOW};







    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);
        mJsonTextView = (TextView)findViewById(R.id.json);

        mLine = (TextView)findViewById(R.id.line);

        findViewById(R.id.radio_driving).setOnClickListener(this);
        findViewById(R.id.radio_walking).setOnClickListener(this);
        findViewById(R.id.radio_bicycling).setOnClickListener(this);
        findViewById(R.id.radio_transit).setOnClickListener(this);

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }





        
        from.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        to.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        mLoadDirections = (Button)findViewById(R.id.load_directions);
        service = ServiceGenerator.createService(DirectionService.class, getResources().getString(R.string.direction_url));

        mLoadDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String tostr =to.getText().toString();
                String fromstr =from.getText().toString();


                service.getDirection(fromstr, tostr, mode, getResources().getString(R.string.SERVER_API_KEY), false, "ru", true,new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        try {
                            map.clear();

                            JSONObject object = new JSONObject(new String(((TypedByteArray) response.getBody()).getBytes()));
                            String json = formatString(object.toString());

                            Type listType = new TypeToken<List<Step>>(){}.getType();



                            JSONArray results = object.optJSONArray("routes");
                            JSONObject route;
                            JSONArray legs;
                            JSONObject leg ;
                            JSONArray steps ;
                            Gson gson = new Gson();


                            for (int i =0;i<results.length();i++){
                                 route = results.optJSONObject(i);
                                 legs = route.optJSONArray("legs");
                                 leg = legs.optJSONObject(0);
                                 steps = leg.optJSONArray("steps");
                                 String jsonOutput = steps.toString();
                                 stepslist = (ArrayList<Step>) gson.fromJson(jsonOutput, listType);
//                                 for (Step tempstep:stepslist){
//                                     mLine.setText(mLine.getText()+tempstep.getPolyline().getPoints()+"\n" );
//
//                                 }
                                allRoutes.add(stepslist);

//                                 printLine();
//                                 mLine.setText(mLine.getText()+"/---------------------------------------------/"+"\n");

                            }

                            currentposution = 0;


                            if (allRoutes.size()>currentposution) {
                                for (ArrayList<Step> temp:allRoutes){
                                    onDrawRoutes(temp,currentposution);
                                    printPoute(allRoutes.get(currentposution));
                                    currentposution++;
                                }



                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }

                    @Override
                    public void failure(RetrofitError error) {

                    }
                });


            }
        });

    }

    private  void  printPoute(ArrayList<Step> steps){

        String str = "";
        for (Step tempstep:steps){
            if (tempstep.getDetail()!=null)
                str =str + " Маршрут = "+tempstep.getDetail().getLine().getName()+" "+tempstep.getDetail().getLine().getVehicle().getType()+
                    "  №  = "+tempstep.getDetail().getLine().getShort_name()+"\n";
        }





    }

    public void onDrawRoutes(ArrayList<Step> steps, int currentposution){
        //map.clear();
        for (Step tempstep:steps){
            if (tempstep.getTravel_mode().equals("WALKING")){
                drawDashedPolyLine(map, PolyUtil.decode(tempstep.getPolyline().getPoints()),Color.BLUE);

            }else{
                onDrawRoute(tempstep,currentposution);

            }
        }
    }

    public void onDrawRoute(Step step, int currentposution){

        List<LatLng> mPoints = PolyUtil.decode(step.getPolyline().getPoints());
        PolylineOptions line;

        int color = currentposution%colors.length;







             line = new PolylineOptions()
                    .color(colors[currentposution])
                    .width(5)
                    .visible(true)
                    .zIndex(30);



        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < mPoints.size(); i++) {
//            if (i == 0) {
//                MarkerOptions startMarkerOptions = new MarkerOptions().position(mPoints.get(i));
//                map.addMarker(startMarkerOptions);
//            } else if (i == mPoints.size() - 1) {
//                MarkerOptions endMarkerOptions = new MarkerOptions().position(mPoints.get(i));
//                map.addMarker(endMarkerOptions);
//            }
            line.add(mPoints.get(i));
            latLngBuilder.include(mPoints.get(i));
        }
        map.addPolyline(line);
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = latLngBuilder.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        map.moveCamera(track);
    }



    public static String formatString(String text){

        StringBuilder json = new StringBuilder();
        String indentString = "";

        for (int i = 0; i < text.length(); i++) {
            char letter = text.charAt(i);
            switch (letter) {
                case '{':
                case '[':
                    json.append("\n" + indentString + letter + "\n");
                    indentString = indentString + "\t";
                    json.append(indentString);
                    break;
                case '}':
                case ']':
                    indentString = indentString.replaceFirst("\t", "");
                    json.append("\n" + indentString + letter);
                    break;
                case ',':
                    json.append(letter + "\n" + indentString);
                    break;

                default:
                    json.append(letter);
                    break;
            }
        }

        return json.toString();
    }


    @Override
    public void onClick(View v) {
        int checkedId = v.getId();

        switch (checkedId){
            case R.id.radio_driving:
                mode = "driving";
                break;
            case R.id.radio_walking:
                mode = "walking";
                break;
            case R.id.radio_bicycling:
                mode = "bicycling";
                break;
            case R.id.radio_transit:
                mode = "transit";
                break;
        }
       // mJsonTextView.setText("");

    }

    private  void printLine(){
        for (Step temp:stepslist){
            if(temp.getTravel_mode().equals("TRANSIT")){
                printTransit(temp);
            }

            if(temp.getTravel_mode().equals("WALKING")){
                printWalking(temp);
            }

            mLine.setText(mLine.getText()+"\n");
        }
    }

    private  void printWalking(Step step){
        mLine.setText(mLine.getText()+"\n" + "Start = "+getAdress(step.getStart_location()));
        mLine.setText(mLine.getText()+"\n" + " Duration = "+step.getDuration().getText());
        mLine.setText(mLine.getText()+"\n" + " Distance = "+step.getDistance().getText());
        mLine.setText(mLine.getText()+"\n" + " Type = "+step.getTravel_mode().toLowerCase());
        mLine.setText(mLine.getText() + "\n" + " End = " + getAdress(step.getEnd_location()));

    }

    private  void printTransit(Step step){

        mLine.setText(mLine.getText()+"\n" + "Start = "+getAdress(step.getStart_location()));
        mLine.setText(mLine.getText()+"\n" + " Duration = "+step.getDuration().getText());
        mLine.setText(mLine.getText()+"\n" + " Distance = "+step.getDistance().getText());
        mLine.setText(mLine.getText()+"\n" + " Type = "+step.getTravel_mode().toLowerCase());


        mLine.setText(mLine.getText()+"\n" + " Start = "+step.getDetail().getDeparture_stop().getName());
        mLine.setText(mLine.getText()+"\n" + " Маршрут = "+step.getDetail().getLine().getName()+" "+step.getDetail().getLine().getVehicle().getType());
        mLine.setText(mLine.getText()+"\n" + "№  = "+step.getDetail().getLine().getShort_name());

        mLine.setText(mLine.getText()+"\n" + " Зупинок = "+step.getDetail().getNum_stops());
        mLine.setText(mLine.getText()+"\n" + " End = "+step.getDetail().getArrival_stop().getName());



        mLine.setText(mLine.getText() + "\n" + " End = " + getAdress(step.getEnd_location()));



    }




    private  String getAdress(Location location){

        Geocoder geocoder;
        geocoder = new Geocoder(this);


        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    location.getLat(),
                    location.getLng(),
                    1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses!=null){
            if (addresses.size()!=0){
                Address address = addresses.get(0);
                String result = address.getAddressLine(0) + ", " + address.getLocality();
                return result;
            }
        }

        return "";


    }

    private void drawDashedPolyLine(GoogleMap mMap, List<LatLng> listOfPoints, int color) {
    /* Boolean to control drawing alternate lines */
        boolean added = false;
        for (int i = 0; i < listOfPoints.size() - 1 ; i++) {
        /* Get distance between current and next point */
            double distance = getConvertedDistance(listOfPoints.get(i),listOfPoints.get(i + 1));

        /* If distance is less than 0.002 kms */
            if (distance < 0.002) {
                if (!added) {
                    mMap.addPolyline(new PolylineOptions()
                            .add(listOfPoints.get(i))
                            .add(listOfPoints.get(i + 1))
                            .color(color));
                    added = true;
                } else {/* Skip this piece */
                    added = false;
                }
            } else {
            /* Get how many divisions to make of this line */
                int countOfDivisions = (int) ((distance/0.002));

            /* Get difference to add per lat/lng */
                double latdiff = (listOfPoints.get(i+1).latitude - listOfPoints
                        .get(i).latitude) / countOfDivisions;
                double lngdiff = (listOfPoints.get(i + 1).longitude - listOfPoints
                        .get(i).longitude) / countOfDivisions;

            /* Last known indicates start point of polyline. Initialized to ith point */
                LatLng lastKnowLatLng = new LatLng(listOfPoints.get(i).latitude, listOfPoints.get(i).longitude);
                for (int j = 0; j < countOfDivisions; j++) {

                /* Next point is point + diff */
                    LatLng nextLatLng = new LatLng(lastKnowLatLng.latitude + latdiff, lastKnowLatLng.longitude + lngdiff);
                    if (!added) {
                        mMap.addPolyline(new PolylineOptions()
                                .add(lastKnowLatLng)
                                .add(nextLatLng)
                                .color(color));
                        added = true;
                    } else {
                        added = false;
                    }
                    lastKnowLatLng = nextLatLng;
                }
            }
        }
    }

    private double getConvertedDistance(LatLng latlng1, LatLng latlng2) {
        double distance = DistanceUtil.distance(latlng1.latitude,
                latlng1.longitude,
                latlng2.latitude,
                latlng2.longitude);
        BigDecimal bd = new BigDecimal(distance);
        BigDecimal res = bd.setScale(3, RoundingMode.DOWN);
        return res.doubleValue();
    }



}

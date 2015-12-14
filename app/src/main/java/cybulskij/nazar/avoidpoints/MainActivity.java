package cybulskij.nazar.avoidpoints;

import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;


import cybulskij.nazar.avoidpoints.adapter.AutoCompleteEventLocationAdapter;
import cybulskij.nazar.avoidpoints.model.Step;
import cybulskij.nazar.avoidpoints.network.DirectionService;
import cybulskij.nazar.avoidpoints.network.ServiceGenerator;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;

public class MainActivity extends FragmentActivity {

    public  static final   double MINDISTANCE = 100.0;


    AutoCompleteTextView from;
    AutoCompleteTextView to;
    Button mLoadDirections;
    SupportMapFragment mapFragment;
    GoogleMap map;
    TextView mJsonTextView;
    int currentposution = 0;

    TextView mLine;
    DirectionService service;
    String mode = "driving";

    ArrayList<Step> stepslist = new ArrayList<Step>();

    ArrayList<ArrayList<Step>> allRoutes=new ArrayList<ArrayList<Step>>();
    private static final LatLngBounds BOUNDS_GREATER_MOSCOW = new LatLngBounds(
            new LatLng(55.151244, 37.018423), new LatLng(56.551244, 38.318423));

    int []colors = {Color.BLACK,Color.BLUE,Color.GRAY,Color.CYAN,Color.GREEN,Color.RED,Color.MAGENTA,Color.YELLOW};

    Double[] arrayMinDistance;
    Marker marker = null;
    Location  markerLocation = new Location("");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        from = (AutoCompleteTextView) findViewById(R.id.from);
        to = (AutoCompleteTextView) findViewById(R.id.to);
        mJsonTextView = (TextView)findViewById(R.id.json);
        mLine = (TextView)findViewById(R.id.line);
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        map = mapFragment.getMap();
        if (map == null) {
            finish();
            return;
        }else{
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    if (marker != null) {
                        marker.remove();
                    }
                    map.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    marker = map.addMarker(markerOptions);
                    markerLocation.setLatitude(latLng.latitude);
                    markerLocation.setLongitude(latLng.longitude);;

                }
            });
        }
        
        from.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        to.setAdapter(new AutoCompleteEventLocationAdapter(this, BOUNDS_GREATER_MOSCOW));
        mLoadDirections = (Button)findViewById(R.id.load_directions);
        service = ServiceGenerator.createService(DirectionService.class, getResources().getString(R.string.direction_url));

        mLoadDirections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (marker==null){
                   Toast.makeText(getApplicationContext(),"please  add marker :)",Toast.LENGTH_SHORT).show();
                    return;

                }
                String tostr =to.getText().toString();
                String fromstr =from.getText().toString();


                service.getDirection(fromstr, tostr, mode, getResources().getString(R.string.SERVER_API_KEY), false, "ru", true,new Callback<Response>() {
                    @Override
                    public void success(Response response, Response response2) {
                        try {
                            map.clear();
                            if (markerLocation!=null){
                                MarkerOptions markerOptions = new MarkerOptions();
                                markerOptions.position(new LatLng(markerLocation.getLatitude(), markerLocation.getLongitude()));
                                marker = map.addMarker(markerOptions);;

                            }

                            JSONObject object = new JSONObject(new String(((TypedByteArray) response.getBody()).getBytes()));
                            Type listType = new TypeToken<List<Step>>(){}.getType();
                            JSONArray results = object.optJSONArray("routes");
                            JSONObject route;
                            JSONArray legs;
                            JSONObject leg ;
                            JSONArray steps ;
                            Gson gson = new Gson();
                            allRoutes.clear();
                            for (int i =0;i<results.length();i++){
                                 route = results.optJSONObject(i);
                                 legs = route.optJSONArray("legs");
                                 leg = legs.optJSONObject(0);
                                 steps = leg.optJSONArray("steps");
                                 String jsonOutput = steps.toString();
                                 stepslist = (ArrayList<Step>) gson.fromJson(jsonOutput, listType);
                                 allRoutes.add(stepslist);
                            }



                            arrayMinDistance = new Double[results.length()];
                            for (int i=0;i<results.length();i++){
                                arrayMinDistance[i]=40000.0*1000.0;
                            }


                            findBestRoute(allRoutes);

                            ArrayList<ArrayList<Step>> tempAllRoutes=new ArrayList<ArrayList<Step>>();
                            tempAllRoutes.clear();

                            int posution = 0;
                                for (ArrayList<Step> temp:allRoutes){
                                    if (arrayMinDistance[posution]>MINDISTANCE) {
                                        tempAllRoutes.add(temp);
                                    }

                                    posution++;
                                }



                            currentposution = 0;
                            if (tempAllRoutes.size()>currentposution) {
                                for (ArrayList<Step> temp:tempAllRoutes){
                                    onDrawRoutes(temp,currentposution);
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


    public void onDrawRoutes(ArrayList<Step> steps, int currentposution){
        for (Step tempstep:steps){
                onDrawRoute(tempstep,currentposution);
        }
    }

    public void onDrawRoute(Step step, int currentposution){
        List<LatLng> mPoints = PolyUtil.decode(step.getPolyline().getPoints());
        PolylineOptions line;
        int color = currentposution%colors.length;
        line = new PolylineOptions()
                    .color(colors[color])
                    .width(5)
                    .visible(true)
                    .zIndex(30);
        LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
        for (int i = 0; i < mPoints.size(); i++) {
            line.add(mPoints.get(i));
            latLngBuilder.include(mPoints.get(i));
        }
        map.addPolyline(line);
    }

    private   void  findBestRoute(ArrayList<ArrayList<Step>> allRoutes){

        int posution = 0;
        if (allRoutes.size()>posution) {
            for (ArrayList<Step> temp:allRoutes){
                findMin(temp, posution);
                posution++;
            }
        }

    }

    public void findMin(ArrayList<Step> steps, int currentposution){
        double temdistance;
        Location location=new Location("");

        for (Step tempstep:steps){
            List<LatLng> mPoints = PolyUtil.decode(tempstep.getPolyline().getPoints());
            for (LatLng temp: mPoints) {
                location.setLatitude(temp.latitude);
                location.setLongitude(temp.longitude);
                temdistance = markerLocation.distanceTo(location);
                if(temdistance<arrayMinDistance[currentposution]){
                    arrayMinDistance[currentposution] = temdistance;
                }
            }
        }

        Log.i("DS",arrayMinDistance[currentposution]+"");

    }



}

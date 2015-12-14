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

public class MainActivity extends FragmentActivity{


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
                                 allRoutes.add(stepslist);
                            }
                            currentposution = 0;
                            if (allRoutes.size()>currentposution) {
                                for (ArrayList<Step> temp:allRoutes){
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
        int size = getResources().getDisplayMetrics().widthPixels;
        LatLngBounds latLngBounds = latLngBuilder.build();
        CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 25);
        map.moveCamera(track);
    }


}

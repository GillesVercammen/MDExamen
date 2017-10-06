package com.example.gilles.voorbeeldexamen;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.OverlayItem;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final String LOCATION = "LOCATION";
    private static final String BESCHRIJVING = "BESCHRIJVING";

    private TextView searchField;
    private Button searchButton;
    private MapView mapView;
    private RequestQueue mRequestQueue;
    private JSONObject zones;
    final ArrayList<OverlayItem> items = new ArrayList<OverlayItem>();

    private String urlSearch = "http://nominatim.openstreetmap.org/search?q=";
    private String urlZones = "http://datasets.antwerpen.be/v4/gis/paparkeertariefzones.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mapView = (MapView)findViewById(R.id.mapview);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(18);

        //default = meistraat
        mapView.getController().setCenter(new GeoPoint(51.2244, 4.38566));

        mRequestQueue = Volley.newRequestQueue(this);
        searchField = (TextView)findViewById(R.id.search_txtview);
        searchButton = (Button)findViewById(R.id.search_button);


        searchButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Log.d("debug", "onClick: test");
                String searchString = "";
                try {
                    searchString = URLEncoder.encode(searchField.getText().toString(), "UTF-8");
                    Log.d("debug", "string: " + searchString);
                }
                catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                JsonArrayRequest jr = new JsonArrayRequest(urlSearch + searchString + "&format=json", new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            //Log.d("edu.ap.maps", response.toString());
                            Log.d("debug", "ik ben hier");
                            hideSoftKeyBoard();
                            JSONObject obj = response.getJSONObject(0);
                            GeoPoint g = new GeoPoint(obj.getDouble("lat"), obj.getDouble("lon"));
                            mapView.getController().setCenter(g);
                        }
                        catch(JSONException ex) {
                            Log.e("Error in 1e catch", ex.getMessage());
                        }
                    }
                },new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error in 2e catch", error.getMessage());
                        hideSoftKeyBoard();
                    }
                });
                mRequestQueue.add(jr);
            }
        });

        if(checkIntent()){
            addMarker((GeoPoint) getIntent().getSerializableExtra("LOCATION"));
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int actionType = ev.getAction();
        switch(actionType) {
            case MotionEvent.ACTION_UP:
                Projection proj = this.mapView.getProjection();
                GeoPoint loc = (GeoPoint)proj.fromPixels((int)ev.getX(), (int)ev.getY() - (searchField.getHeight() * 2));

                Intent nextScreenIntent = new Intent(this, SaverActivity.class);
                nextScreenIntent.putExtra(LOCATION, (Serializable) loc);
                startActivity(nextScreenIntent);
        }
        return super.dispatchTouchEvent(ev);
    }


    private boolean checkIntent(){
        GeoPoint location = (GeoPoint) getIntent().getSerializableExtra("LOCATION");
        boolean check = false;
        if(location != null) {
            check = true;
        }

        return check;
    }

    private void addMarker(GeoPoint g) {
        OverlayItem myLocationOverlayItem = new OverlayItem("Here", "Current Position", g);
        Drawable myCurrentLocationMarker = ResourcesCompat.getDrawable(getResources(), R.drawable.marker, null);
        myLocationOverlayItem.setMarker(myCurrentLocationMarker);

        items.add(myLocationOverlayItem);
        DefaultResourceProxyImpl resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());

        ItemizedIconOverlay<OverlayItem> currentLocationOverlay = new ItemizedIconOverlay<OverlayItem>(items,
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        return true;
                    }
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return true;
                    }
                }, resourceProxy);
        this.mapView.getOverlays().add(currentLocationOverlay);
        this.mapView.invalidate();
    }

    private void hideSoftKeyBoard(){
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        if(imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
}

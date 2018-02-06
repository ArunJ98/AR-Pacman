package com.arunj.pacman;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks, OnMapReadyCallback {

    private GoogleApiClient mGoogleApiClient;
    private final static int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
    private GoogleMap mymap;
    private int score = 0;
    private TextView headerValue;
    private ArrayList<Polyline> polylines = new ArrayList<Polyline>();


    private double[] dots = {
            42.357606, -71.137955,
            42.356797, -71.138287,
            42.355981, -71.138513,
            42.355101, -71.138631,
            42.354044, -71.139014,
            42.353866, -71.139641,
            42.354066, -71.140767,
            42.354420, -71.142139,
            42.354727, -71.143481,
            42.355046, -71.144730,
            42.355422, -71.146179,
            42.355787, -71.147582,
            42.356186, -71.149109,
            42.357063, -71.148523,
            42.357998, -71.147983,
            42.358864, -71.147382,
            42.358602, -71.145670,
            42.358408, -71.144174,
            42.358237, -71.142648,
            42.358009, -71.141090,
            42.357793, -71.139456,
            42.356995, -71.139194,
            42.357166, -71.140566,
            42.356505, -71.140998,
            42.356596, -71.142170,
            42.356767, -71.143419,
            42.356972, -71.144899,
            42.357223, -71.146811,
            42.356892, -71.147721,
            42.356471, -71.146950,
            42.356368, -71.145886,
            42.355639, -71.143774,
            42.355593, -71.142864,
            42.355491, -71.141661,
            42.355149, -71.142231,
            42.355964, -71.141807,
            42.355873, -71.140967,
            42.355554, -71.140134,
            42.355884, -71.139325,
            42.354517, -71.139479
    };

    private Marker[] marker_objects = new Marker[dots.length / 2];
    private Location mCurrentLocation;
    private String mLastUpdateTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        headerValue = (TextView) findViewById(R.id.score_text);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS);
        } else {
            getLocation();
        }

    }

    private void getLocation() {
        try {
            LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        } catch (SecurityException e) {

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getLocation();

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d("MainActivity","onConnected");
        startLocationUpdates();

    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, new LocationRequest().setInterval(2000), this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
        eatTest(location);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mymap=googleMap;
        //Disable cam movement
        mymap.getUiSettings().setAllGesturesEnabled(false);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);

        CameraPosition pos = new CameraPosition.Builder()
                .target(new LatLng(42.356700,-71.143717))
                .zoom(16)
                .bearing(-75)
                .build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));


        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.dot);
        for(int i = 0; i < dots.length; i += 2){
            Marker mark = googleMap.addMarker(new MarkerOptions()
                    .position(new LatLng(dots[i],dots[i+1]))
                    .icon(icon));

                System.out.println("ASDASDKJHASDKJAHSDLKAHJGSDKJASHGDKJASHDGAKJSHDGAKSJHDGASKJHD");
                marker_objects[i/2] = mark;

        }



    }

    public String makeURL (double sourcelat, double sourcelog, double destlat, double destlog ){
        StringBuilder urlString = new StringBuilder();
        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString
                .append(Double.toString( sourcelog));
        urlString.append("&destination=");// to
        urlString
                .append(Double.toString( destlat));
        urlString.append(",");
        urlString.append(Double.toString( destlog));
        urlString.append("&sensor=false&mode=driving&alternatives=true");
        urlString.append("&key=AIzaSyAF9ZwGEvuYLItnb6Ki7_NsFfa-F9gfANc");
        return urlString.toString();
    }



    public void drawPath(String  result) {

        try {
            //Tranform the string into a json object
            final JSONObject json = new JSONObject(result);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            Log.d("mainactivity",list.toString());
            Polyline line = mymap.addPolyline(new PolylineOptions()
                    .addAll(list)
                    .width(12)
                    .color(Color.parseColor("#05b1fb"))//Google maps blue color
                    .geodesic(true)
            );
            polylines.add(line);
           /*
           for(int z = 0; z<list.size()-1;z++){
                LatLng src= list.get(z);
                LatLng dest= list.get(z+1);
                Polyline line = mMap.addPolyline(new PolylineOptions()
                .add(new LatLng(src.latitude, src.longitude), new LatLng(dest.latitude,   dest.longitude))
                .width(2)
                .color(Color.BLUE).geodesic(true));
            }
           */
        }
        catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng( (((double) lat / 1E5)),
                    (((double) lng / 1E5) ));
            poly.add(p);
        }

        return poly;
    }

    private class connectAsyncTask extends AsyncTask<Void, Void, String> {
        private ProgressDialog progressDialog;
        String url;
        connectAsyncTask(String urlPass){
            url = urlPass;
        }
        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
            Log.d("mainactivity","connect");
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.setMessage("Fetching route, Please wait...");
            progressDialog.setIndeterminate(true);
            //progressDialog.show();
        }
        @Override
        protected String doInBackground(Void... params) {
            Log.d("mainactivity","connect doinback");
            JSONParser jParser = new JSONParser();
            String json = jParser.getJSONFromUrl(url);
            return json;
        }
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressDialog.hide();
            Log.d("mainactivity","result "+result);
            if(result!=null){
                drawPath(result);
            }
        }
    }

    private void eatTest(Location currentLocation){
        try {
            for(Polyline p: polylines){
                p.remove();
            }
            polylines.clear();
            new connectAsyncTask(makeURL(42.356186, -71.149109,
                    currentLocation.getLatitude(), currentLocation.getLongitude())).execute();

            for(int i = 0; i < dots.length; i+=2){
                double dist = 10;
                double distLat= 0;
                double distLng=0;
                if(marker_objects[i/2].isVisible()) {

                    //distLat = (Math.abs(currentLocation.getLatitude() - marker_objects[i].getPosition().latitude));
                    //distLng = (Math.abs(currentLocation.getLongitude() - marker_objects[i].getPosition().longitude));
                    distLat = (Math.abs(currentLocation.getLatitude()) - dots[i]);
                    System.out.println("Lattitue   " + distLat);
                    distLng = (Math.abs(currentLocation.getLongitude() - dots[i + 1]));
                    System.out.println("Long   " + distLng);


                    dist = Math.sqrt((Math.pow(distLat, 2)) + (Math.pow(distLng, 2)));
                    System.out.println("distance    " + dist);
                    System.out.println("");


                    if (dist < 0.0005) {
                        marker_objects[i / 2].setVisible(false);
                        score++;
                        String scoreString = "Score: " + score;
                        headerValue.setText(scoreString);
                    }
                }
            }

        } catch (SecurityException e) {

        }
    }
}

package maxmonessentials.ekart.shop.activity;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import maxmonessentials.ekart.shop.R;
import maxmonessentials.ekart.shop.helper.ApiConfig;
import maxmonessentials.ekart.shop.helper.Session;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MapLocationActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnMapLongClickListener {

    GoogleMap mGoogleMap;
    SupportMapFragment mapFrag;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    double latitude,longitude, c_longitude, c_latitude;
    Session session;
    FloatingActionButton fabSatellite, fabStreet, fabCurrent;
    TextView text;
    Toolbar toolbar;
    int mapType = GoogleMap.MAP_TYPE_NORMAL;
    boolean firstTime = true;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_location);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        session = new Session(MapLocationActivity.this);
        text = findViewById(R.id.tvLocation);
        fabSatellite = findViewById(R.id.fabSatellite);
        fabCurrent = findViewById(R.id.fabCurrent);
        fabStreet = findViewById(R.id.fabStreet);
        //getSupportActionBar().setTitle("Map Location Activity");

        mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFrag.getMapAsync(this);




        fabSatellite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapType = GoogleMap.MAP_TYPE_HYBRID;




                mapFrag.getMapAsync(MapLocationActivity.this);
            }
        });
        fabStreet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mapType = GoogleMap.MAP_TYPE_NORMAL;
                mapFrag.getMapAsync(MapLocationActivity.this);
            }
        });
        fabCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // mapType = GoogleMap.MAP_TYPE_NORMAL;
                LatLng latLng = new LatLng(c_latitude, c_longitude);
                saveLocation(c_latitude, c_longitude);
                mGoogleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .draggable(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        .title(getString(R.string.current_location)));

                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18));

                //text.setText("Latitude - " + latitude + "\nLongitude - " + longitude);
                text.setText(getString(R.string.location_1) + ApiConfig.getAddress(latitude, longitude, MapLocationActivity.this));
            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        //stop location updates when Activity is no longer active
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mGoogleMap=googleMap;

        //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.setMapType(mapType);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mGoogleMap.setMyLocationEnabled(true);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        }
        else {
            buildGoogleApiClient();
            mGoogleMap.setMyLocationEnabled(true);
        }

        LatLng ll = new LatLng(latitude,longitude);
        //googleMap.getMyLocation().getLongitude();

        //mGoogleMap.addMarker(new MarkerOptions().position(ll).draggable(true));
        moveMap(true);


//        mGoogleMap.setOnMapLongClickListener(this);
//
//        mGoogleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
//            @Override
//            public void onMapClick(LatLng latLng) {
//
//                latitude = latLng.latitude;
//                longitude = latLng.longitude;
//
//                //Moving the map
//                mGoogleMap.clear();
//                moveMap(false);
//
//                latitude = latLng.latitude;
//                longitude = latLng.longitude;
//                saveLocation(latitude, longitude);
//                //Moving the map
//                moveMap(false);
//
//            }
//        });

        //mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(ll));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {}

    @Override
    public void onLocationChanged(Location location)
    {
        mLastLocation = location;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }

        //Place current location marker

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        c_latitude = location.getLatitude();
        c_longitude = location.getLongitude();
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        mCurrLocationMarker = mGoogleMap.addMarker(markerOptions);

        //move map camera
        if(firstTime){
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,18));

        }
        firstTime = false;
        text.setText(getString(R.string.location_1) + ApiConfig.getAddress(latitude, longitude, MapLocationActivity.this));

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission, please accept to use location functionality")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(MapLocationActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mGoogleMap.setMyLocationEnabled(true);
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }


    private void moveMap(boolean isfirst) {


        LatLng latLng = new LatLng(latitude, longitude);


        mGoogleMap.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(getString(R.string.set_location)));

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

        if (isfirst)
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18));

        text.setText(getString(R.string.location_1) + ApiConfig.getAddress(latitude, longitude, MapLocationActivity.this));
        //  text.setText("Latitude - " + latitude + "\nLongitude - " + longitude);
    }


    @Override
    public void onMapLongClick(LatLng latLng) {

        mGoogleMap.clear();
        latitude = latLng.latitude;
        longitude = latLng.longitude;
        saveLocation(latitude, longitude);
        //Moving the map
        moveMap(false);

    }

    public void saveLocation(double latitude, double longitude) {
        session.setData(Session.KEY_LATITUDE, String.valueOf(latitude));
        session.setData(Session.KEY_LONGITUDE, String.valueOf(longitude));
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //  saveLocation(latitude, longitude);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void UpdateLocation(View view) {
        onBackPressed();
    }

}
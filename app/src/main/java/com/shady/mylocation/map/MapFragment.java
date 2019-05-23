package com.shady.mylocation.map;


import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.databinding.library.baseAdapters.BR;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.shady.mylocation.R;
import com.shady.mylocation.base.BaseFragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.content.Context.LOCATION_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class MapFragment extends BaseFragment {

    private GoogleMap mMap;
    private final int REQ_PERMISSION = 111;
    private final int REQUEST_CHECK_SETTINGS = 1000;
    private AsyncTask<Void, Void, Void> cityTask;
    private Marker myMarker;
    private Unbinder unbinder;
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 20;
    private MapViewModel mViewModel;

    @BindView(R.id.mapview)
    MapView mMapView;

    @BindView(R.id.address)
    TextView addressTextView;

    private class LocationListener implements android.location.LocationListener {
        //   CustomLocation mLastLocation;

        public LocationListener(String provider) {
            //  mLastLocation = new CustomLocation(provider);
        }

        @Override
        public void onLocationChanged(android.location.Location location) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    public MapFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onMessageEvent(Place place) {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 16));
    }

    private void setCurrentLocation() {
        if (cityTask != null && cityTask.getStatus() == AsyncTask.Status.RUNNING) {
            cityTask.cancel(true);
        }
        cityTask = new AsyncTask<Void, Void, Void>() {
            public Address address;
            private String pointCityName;
            private String pointCountryName;
            private LatLng center;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                center = mMap.getCameraPosition().target;
            }

            @Override
            protected Void doInBackground(Void... voids) {
                Log.d(this.getClass().getSimpleName(), "Do in background started");
                try {
                    Geocoder coder = new Geocoder(getActivity(), Locale.ENGLISH);
                    Log.v("PlacesAsyncTask", "coder.isPresent() == " + coder.isPresent());
                    List<Address> geocoderPlaces = coder.getFromLocation(center.latitude, center.longitude, 1);
                    Log.v("PlacesAsyncTask", "GeoCoder returned " + geocoderPlaces.size() + " Places");
                    for (int i = 0; i < geocoderPlaces.size(); i++) {
                        address = geocoderPlaces.get(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                Log.v("checkSameCitiesName", "checkSameCitiesName >> equal");
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                if (address == null) {
                    if (!isConnectedToInternet()) {
                        showToastMessage(getString(R.string.no_internet_message));
                    }
                    return;
                }
                pointCityName = address.getLocality();
                pointCountryName = address.getCountryName();

                addressTextView.setText(address.getAddressLine(0));

            }
        }.execute();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        // TODO: Use the ViewModel
    }

    @OnClick(R.id.add_address)
    public void onButtonClick(View view) {
      showToastMessage("Add address clicked");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewDataBinding binding = DataBindingUtil.bind(inflater.inflate(R.layout.fragment_map, container, false));
        unbinder = ButterKnife.bind(this, binding.getRoot());
        mViewModel = ViewModelProviders.of(this).get(MapViewModel.class);
        binding.setVariable(BR.viewmodel, mViewModel);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                Log.v("HomeFragment", "HomeFragment >>> map is ready");
                onGoogleMapReady(googleMap);
            }
        });

        initializeLocationManager();
        try {
            mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[1]);
        } catch (SecurityException ex) {

        } catch (IllegalArgumentException ex) {

        }
        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListeners[0]);
        } catch (SecurityException ex) {

        } catch (IllegalArgumentException ex) {

        }
        addressTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_map_fragment_to_address_fragment);
            }
        });
        return binding.getRoot();
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }
    }

    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {

        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("HomeFragment", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    public void onGoogleMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (checkPermission()) {
            enableMyLocaion();
        } else {
            askPermission();
        }
        moveToMyLocation();
    }


    // Check for permission to access Location
    private boolean checkPermission() {
        // Ask for permission if it wasn't granted yet
        return (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }


    // Asks for permission
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void askPermission() {
        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQ_PERMISSION);
    }

    private boolean isAirplaneModeOn() {
        return Settings.System.getInt(getActivity().getContentResolver(),
                Settings.Global.AIRPLANE_MODE_ON, 0) != 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    // Permission granted
//                    if (checkPermission())
                    enableMyLocaion();
                    moveToMyLocation();
                } else {
                    // Permission denied
                }
                break;
            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void enableMyLocaion() {
        isLocationEnabled();
        if (!checkPermission()) {
            askPermission();
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                if (isAirplaneModeOn()) {
                    Toast.makeText(getContext(), R.string.enable_airplane_mode_message, Toast.LENGTH_LONG).show();
                    return true;
                }
                if (isLocationEnabled()) {
                    moveToMyLocation();
                }
                return true;
            }
        });
        mMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                setCurrentLocation();
            }
        });
    }

    private boolean goToLocation() {
        if (isAirplaneModeOn()) {
            Toast.makeText(getActivity(), R.string.enable_airplane_mode_message, Toast.LENGTH_LONG).show();
            return true;
        }
        if (isLocationEnabled()) {
            moveToMyLocation();
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        moveToMyLocation();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to

                        break;
                    default:
                        break;
                }
                break;
        }
    }

    private boolean isLocationEnabled() {
        LocationManager lm = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if (!gps_enabled && !network_enabled) {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(getActivity()).addApi(LocationServices.API).build();
            googleApiClient.connect();
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(30 * 1000);
            locationRequest.setFastestInterval(5 * 1000);
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
            builder.setAlwaysShow(true);
            PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult result) {
                    final Status status = result.getStatus();
//                    final LocationSettingsStates state = result.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                e.printStackTrace();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.
                            break;
                    }
                }
            });
        }
        return true;
    }

    private void moveToMyLocation() {
        LocationManager service = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);
        if (!checkPermission()) {
            askPermission();
            return;
        }
        Location location = service.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
//            userLocation = location;
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            addMapLocationWIthMarker(userLocation, true);
            return;
        } else {
            location = service.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                addMapLocationWIthMarker(userLocation, true);
                return;
            }
        }

    }

    private void addMapLocationWIthMarker(LatLng userLocation, boolean animateToLocation) {
        if (myMarker != null) {
            myMarker.remove();
        }
        if (animateToLocation) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16));
        }
        MarkerOptions markerOptions = new MarkerOptions().position(userLocation);
//        markerOptions.icon(bitmapDescriptorFromVector(this, R.drawable.ic_my_location));
//        myMarker = mMap.addMarker(markerOptions);
        myMarker = mMap.addMarker(markerOptions/*.icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(user)))*/);
//        marker.setTag(user);
    }
}

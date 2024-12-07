package com.example.fyp1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class LocationPickerActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_ENABLE_GPS = 2;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView tvLocationAddress;
    private ImageView btnUseCurrentLocation;
    private AutoCompleteTextView manualEditLocation;  // Change to AutoCompleteTextView
    private Button btnConfirmLocation;
    private LocationCallback locationCallback;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        tvLocationAddress = findViewById(R.id.tvLocationAddress);
        btnUseCurrentLocation = findViewById(R.id.btnUseCurrentLocation);
        btnConfirmLocation = findViewById(R.id.btnConfirmLocation);
        manualEditLocation = findViewById(R.id.etSearchLocation);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout), (view, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;

            view.setPadding(
                    view.getPaddingLeft(),   // Keep original left padding
                    topInset,                // Set top padding to adjust for status bar or notch
                    view.getPaddingRight(),  // Keep original right padding
                    view.getPaddingBottom()  // Keep original bottom padding
            );

            return insets;
        });

        btnUseCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCurrentLocation();
            }
        });

        btnConfirmLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String locationAddress = tvLocationAddress.getText().toString();
                if (!locationAddress.isEmpty()) {
                    Intent intent = new Intent(LocationPickerActivity.this, MakeRecycleRequest.class);
                    intent.putExtra("location_address", locationAddress);
                    startActivity(intent);
                } else {
                    MotionToast.Companion.darkToast(LocationPickerActivity.this,
                            "Location Not Selected",
                            "Please select a location first",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(LocationPickerActivity.this, www.sanju.motiontoast.R.font.helveticabold));
                }
            }
        });

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        manualEditLocation.setAdapter(adapter);
        manualEditLocation.setThreshold(1);

        manualEditLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(runnable);
                runnable = () -> searchLocation(s.toString(), adapter);
                handler.postDelayed(runnable, 1000); // Delay of 1 second
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        manualEditLocation.setOnItemClickListener((parent, view, position, id) -> {
            String selectedLocation = (String) parent.getItemAtPosition(position);
            tvLocationAddress.setText(selectedLocation);
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateLocationAddress(location);
                        fusedLocationClient.removeLocationUpdates(locationCallback);
                        return;
                    }
                }
            }
        };
    }

    private boolean isGPSEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void getCurrentLocation() {
        if (!isGPSEnabled()) {
            promptEnableGPS();
            return;
        }

        getLocation();
    }

    private void promptEnableGPS() {
        new AlertDialog.Builder(this)
                .setMessage("GPS is required for this app to work. Do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent enableGPSIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGPSIntent, REQUEST_ENABLE_GPS);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    MotionToast.Companion.darkToast(LocationPickerActivity.this,
                            "GPS Required",
                            "GPS is required to get your location",
                            MotionToastStyle.INFO,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(LocationPickerActivity.this, www.sanju.motiontoast.R.font.helveticabold));

                })
                .show();
    }

    private void getLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        try {
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } catch (SecurityException e) {
            MotionToast.Companion.darkToast(LocationPickerActivity.this,
                    "Location Permission Needed",
                    "Location permission is required.",
                    MotionToastStyle.WARNING,
                    MotionToast.GRAVITY_BOTTOM,
                    MotionToast.LONG_DURATION,
                    ResourcesCompat.getFont(LocationPickerActivity.this, www.sanju.motiontoast.R.font.helveticabold));
        }
    }

    private void updateLocationAddress(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                String addressText = String.format("%s, %s, %s",
                        address.getAddressLine(0),
                        address.getLocality(),
                        address.getCountryName());
                tvLocationAddress.setText(addressText);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void searchLocation(String query, ArrayAdapter<String> adapter) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(query + ", Malaysia", 5); // Filter results to Malaysia
            List<String> suggestions = new ArrayList<>();
            for (Address address : addresses) {
                String addressText = String.format("%s, %s, %s",
                        address.getAddressLine(0),
                        address.getLocality(),
                        address.getCountryName());
                suggestions.add(addressText);
            }
            runOnUiThread(() -> {
                adapter.clear();
                adapter.addAll(suggestions);
                adapter.notifyDataSetChanged();
            });
        } catch (IOException e) {
            e.printStackTrace();
            runOnUiThread(() -> {
                MotionToast.Companion.darkToast(LocationPickerActivity.this,
                        "Error",
                        "Error searching location",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(LocationPickerActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                MotionToast.Companion.darkToast(LocationPickerActivity.this,
                        "Error",
                        "Location permission denied",
                        MotionToastStyle.ERROR,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(LocationPickerActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_GPS) {
            if (isGPSEnabled()) {
                getCurrentLocation();
            } else {
                MotionToast.Companion.darkToast(LocationPickerActivity.this,
                        "GPS Access Required",
                        "GPS is required to get your location",
                        MotionToastStyle.WARNING,
                        MotionToast.GRAVITY_BOTTOM,
                        MotionToast.LONG_DURATION,
                        ResourcesCompat.getFont(LocationPickerActivity.this, www.sanju.motiontoast.R.font.helveticabold));
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }
}

package com.campusbondhu.ui.bus;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.Bus;
import com.campusbondhu.data.repository.BusRepository;
import com.campusbondhu.databinding.ActivityBusTrackingBinding;
import com.campusbondhu.utils.SessionManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BusTrackingActivity extends AppCompatActivity {
    private ActivityBusTrackingBinding binding;
    private final List<Bus> data = new ArrayList<>();
    private BusAdapter adapter;
    private BusRepository repo = new BusRepository();
    private MapView mapView;
    
    // ✅ ডাটাবেসের সঠিক লিংক
    private final String RTDB_URL = "https://campusbondhu-c2b14-default-rtdb.asia-southeast1.firebasedatabase.app";
    
    private DatabaseReference rtdbRef, userLocRef;
    private ValueEventListener rtdbListener, userLocListener;
    
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private GeoPoint myCurrentLocation = null;
    private String myUid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Configuration.getInstance().load(getApplicationContext(),
            getSharedPreferences("osmdroid", MODE_PRIVATE));

        binding = ActivityBusTrackingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.btnBack.setOnClickListener(v -> finish());

        SessionManager session = new SessionManager(this);
        if ("admin".equals(session.getRole())) {
            binding.btnManageBus.setVisibility(View.VISIBLE);
            binding.btnManageBus.setOnClickListener(v ->
                startActivity(new Intent(this, ManageBusActivity.class)));
        }

        myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        
        mapView = binding.mapView;
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(false);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(22.8098, 89.5403));

        binding.btnMyLocation.setOnClickListener(v -> {
            if (myCurrentLocation != null) {
                mapView.getController().animateTo(myCurrentLocation);
                mapView.getController().setZoom(16.0);
            } else {
                Toast.makeText(this, "Waiting for RTDB location...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.rvBuses.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new BusAdapter();
        binding.rvBuses.setAdapter(adapter);

        checkLocationServices();
        startLocationUpdatesAndSyncToRTDB();
        listenToMyLocationFromRTDB();

        repo.getAllBuses().observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
            listenToRealtimeBusLocations();
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdatesAndSyncToRTDB() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1001);
            return;
        }

        // ✅ এখানে সঠিক URL দিয়ে ডাটাবেস কল করা হচ্ছে
        userLocRef = FirebaseDatabase.getInstance(RTDB_URL).getReference("user_locations").child(myUid);
        
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(3000);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        Map<String, Object> locMap = new HashMap<>();
                        locMap.put("lat", location.getLatitude());
                        locMap.put("lng", location.getLongitude());
                        locMap.put("timestamp", System.currentTimeMillis());
                        
                        userLocRef.setValue(locMap).addOnSuccessListener(aVoid -> {
                            Log.d("RTDB_DEBUG", "User location pushed to RTDB successfully");
                        }).addOnFailureListener(e -> {
                            Log.e("RTDB_DEBUG", "Failed to push location: " + e.getMessage());
                        });
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    private void listenToMyLocationFromRTDB() {
        // ✅ সঠিক URL
        userLocRef = FirebaseDatabase.getInstance(RTDB_URL).getReference("user_locations").child(myUid);
        userLocListener = userLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Double lat = snapshot.child("lat").getValue(Double.class);
                    Double lng = snapshot.child("lng").getValue(Double.class);
                    if (lat != null && lng != null) {
                        myCurrentLocation = new GeoPoint(lat, lng);
                        updateMapMarkers();
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void listenToRealtimeBusLocations() {
        if (rtdbRef != null && rtdbListener != null) {
            rtdbRef.removeEventListener(rtdbListener);
        }
        
        // ✅ সঠিক URL
        rtdbRef = FirebaseDatabase.getInstance(RTDB_URL).getReference("bus_locations");
        rtdbListener = rtdbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean hasUpdates = false;
                for (Bus bus : data) {
                    DataSnapshot locSnap = snapshot.child(bus.getBusId());
                    if (locSnap.exists()) {
                        Double lat = locSnap.child("lat").getValue(Double.class);
                        Double lng = locSnap.child("lng").getValue(Double.class);
                        if (lat != null && lng != null) {
                            bus.setLatitude(lat);
                            bus.setLongitude(lng);
                            hasUpdates = true;
                        }
                    }
                }
                if (hasUpdates) {
                    updateMapMarkers();
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void updateMapMarkers() {
        mapView.getOverlays().clear();

        if (myCurrentLocation != null) {
            Marker userMarker = new Marker(mapView);
            userMarker.setPosition(myCurrentLocation);
            userMarker.setTitle("My Live Location");
            userMarker.setIcon(ContextCompat.getDrawable(this, com.campusbondhu.R.drawable.ic_my_location_blue));
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(userMarker);
        }

        for (Bus b : data) {
            if (b.getLatitude() != 0.0 && b.getLongitude() != 0.0) {
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(b.getLatitude(), b.getLongitude()));
                marker.setTitle(b.getBusName() + " (" + b.getBusNumber() + ")");
                marker.setIcon(ContextCompat.getDrawable(this, org.osmdroid.library.R.drawable.marker_default));
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
            }
        }
        mapView.invalidate();
    }

    private void checkLocationServices() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gpsEnabled = false;
        try { gpsEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER); } catch (Exception ex) {}
        if (!gpsEnabled) Toast.makeText(this, "Please enable GPS", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1001 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdatesAndSyncToRTDB();
        }
    }

    @Override public void onResume() { super.onResume(); mapView.onResume(); }
    @Override public void onPause() { super.onPause(); mapView.onPause(); }
    
    @Override 
    protected void onDestroy() { 
        super.onDestroy(); 
        repo.removeListeners(); 
        if (rtdbRef != null && rtdbListener != null) rtdbRef.removeEventListener(rtdbListener);
        if (userLocRef != null && userLocListener != null) userLocRef.removeEventListener(userLocListener);
        if (fusedLocationClient != null && locationCallback != null) fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    private class BusAdapter extends RecyclerView.Adapter<BusAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(com.campusbondhu.R.layout.item_bus, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Bus b = data.get(pos);
            h.tvName.setText(b.getBusName() + " (" + b.getBusNumber() + ")");
            h.tvRoute.setText(b.getRouteName());
            h.itemView.setOnClickListener(v -> {
                if (b.getLatitude() != 0.0) {
                    mapView.getController().animateTo(new GeoPoint(b.getLatitude(), b.getLongitude()));
                    mapView.getController().setZoom(16.0);
                }
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvRoute;
            VH(View v) {
                super(v);
                tvName = v.findViewById(com.campusbondhu.R.id.tvBusName);
                tvRoute = v.findViewById(com.campusbondhu.R.id.tvBusRoute);
            }
        }
    }
}

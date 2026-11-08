package com.campusbondhu.ui.bus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.Bus;
import com.campusbondhu.data.repository.BusRepository;
import com.campusbondhu.databinding.ActivityBusTrackingBinding;
import com.campusbondhu.utils.SessionManager;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import java.util.ArrayList;
import java.util.List;

public class BusTrackingActivity extends AppCompatActivity {
    private ActivityBusTrackingBinding binding;
    private final List<Bus> data = new ArrayList<>();
    private BusAdapter adapter;
    private BusRepository repo = new BusRepository();
    private MapView mapView;

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

        mapView = binding.mapView;
        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(13.0);
        mapView.getController().setCenter(new GeoPoint(22.8098, 89.5403));

        binding.rvBuses.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new BusAdapter();
        binding.rvBuses.setAdapter(adapter);

        repo.getAllBuses().observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
            updateMapMarkers();
        });
    }

    private void updateMapMarkers() {
        mapView.getOverlays().clear();
        for (Bus b : data) {
            if (b.getLatitude() != 0.0 && b.getLongitude() != 0.0) {
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(b.getLatitude(), b.getLongitude()));
                // ✅ বাসের জন্য ইমোজি এবং টাইটেল সেট করা হলো
                marker.setTextIcon("🚌"); 
                marker.setTitle(b.getBusName() + " (" + b.getBusNumber() + ")");
                marker.setSnippet("Route: " + b.getRouteName());
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(marker);
            }
        }
        mapView.invalidate();
    }

    @Override
    protected void onResume() { super.onResume(); mapView.onResume(); }
    @Override
    protected void onPause() { super.onPause(); mapView.onPause(); }
    @Override
    protected void onDestroy() { super.onDestroy(); repo.removeListeners(); }

    private class BusAdapter extends RecyclerView.Adapter<BusAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_bus, p, false));
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

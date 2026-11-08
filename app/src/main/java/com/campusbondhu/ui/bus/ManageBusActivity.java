package com.campusbondhu.ui.bus;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.Bus;
import com.campusbondhu.data.repository.BusRepository;
import com.campusbondhu.databinding.ActivityManageBusBinding;
import java.util.ArrayList;
import java.util.List;

public class ManageBusActivity extends AppCompatActivity {
    private ActivityManageBusBinding binding;
    private final List<Bus> data = new ArrayList<>();
    private BusAdapter adapter;
    private BusRepository repo = new BusRepository();

    // ✅ খুলনার রুট পয়েন্ট (সোনাডাঙ্গা, গল্লামারী, দৌলতপুর)
    private final double[][] khulnaRoute = {
            {22.8098, 89.5403}, // Sonadanga
            {22.8302, 89.5286}, // Gollamari
            {22.8680, 89.5112}  // Daulatpur
    };
    private int routeStep = 0; // বাস এখন কোন পয়েন্টে আছে

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityManageBusBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvBuses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BusAdapter();
        binding.rvBuses.setAdapter(adapter);

        repo.getAllBuses().observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
        });

        binding.btnAdd.setOnClickListener(v -> {
            String name = binding.etBusName.getText().toString().trim();
            String number = binding.etBusNumber.getText().toString().trim();
            String route = binding.etRoute.getText().toString().trim();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(number)) {
                Toast.makeText(this, "Name and Number required", Toast.LENGTH_SHORT).show();
                return;
            }

            Bus bus = new Bus();
            bus.setBusName(name);
            bus.setBusNumber(number);
            bus.setRouteName(route);

            // ✅ নতুন বাস ডিফল্টভাবে সোনাডাঙ্গায় থাকবে
            bus.setLatitude(khulnaRoute[0][0]);
            bus.setLongitude(khulnaRoute[0][1]);
            bus.setLastUpdated(System.currentTimeMillis());

            repo.addBus(bus).observe(this, ok -> {
                if (Boolean.TRUE.equals(ok)) {
                    Toast.makeText(this, "Bus Added", Toast.LENGTH_SHORT).show();
                    binding.etBusName.setText("");
                    binding.etBusNumber.setText("");
                    binding.etRoute.setText("");
                }
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repo.removeListeners();
    }

    private class BusAdapter extends RecyclerView.Adapter<BusAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                    com.campusbondhu.R.layout.item_manage_bus, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Bus b = data.get(pos);
            h.tvName.setText(b.getBusName() + " (" + b.getBusNumber() + ")");

            h.btnUpdate.setOnClickListener(v -> {
                // ✅ প্রতি ক্লিকে বাস পরের স্টেশনে যাবে
                routeStep = (routeStep + 1) % khulnaRoute.length;
                double lat = khulnaRoute[routeStep][0];
                double lng = khulnaRoute[routeStep][1];

                repo.updateLocation(b.getBusId(), lat, lng).observe(ManageBusActivity.this, ok -> {
                    if (Boolean.TRUE.equals(ok)) {
                        String[] stationNames = {"Sonadanga", "Gollamari", "Daulatpur"};
                        Toast.makeText(ManageBusActivity.this,
                                "Bus moved to: " + stationNames[routeStep], Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName;
            com.google.android.material.button.MaterialButton btnUpdate;
            VH(View v) {
                super(v);
                tvName = v.findViewById(com.campusbondhu.R.id.tvManageBusName);
                btnUpdate = v.findViewById(com.campusbondhu.R.id.btnUpdateLoc);
            }
        }
    }
}
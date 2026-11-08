package com.campusbondhu.ui.academic;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.RoutineItem;
import com.campusbondhu.data.repository.AcademicRepository;
import com.campusbondhu.databinding.ActivityViewRoutineBinding;
import java.util.ArrayList;
import java.util.List;

public class ViewRoutineActivity extends AppCompatActivity {
    private ActivityViewRoutineBinding binding;
    private final List<RoutineItem> data = new ArrayList<>();
    private RoutineAdapter adapter;
    private AcademicRepository repo = new AcademicRepository();

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityViewRoutineBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvRoutine.setLayoutManager(new LinearLayoutManager(this));
        adapter = new RoutineAdapter();
        binding.rvRoutine.setAdapter(adapter);

        repo.getAllRoutines().observe(this, list -> {
            data.clear();
            if (list != null) data.addAll(list);
            adapter.notifyDataSetChanged();
            binding.tvEmpty.setVisibility(data.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        repo.removeListeners();
    }

    private class RoutineAdapter extends RecyclerView.Adapter<RoutineAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_routine, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            RoutineItem item = data.get(pos);
            h.tvCode.setText(item.getCourseCode());
            h.tvRoom.setText("Room: " + (item.getRoom() != null && !item.getRoom().isEmpty() ? item.getRoom() : "N/A"));
            h.tvTime.setText(item.getTime());
            h.tvDay.setText(item.getDay());
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvCode, tvRoom, tvTime, tvDay;
            VH(View v) {
                super(v);
                tvCode = v.findViewById(com.campusbondhu.R.id.tvRoutineCode);
                tvRoom = v.findViewById(com.campusbondhu.R.id.tvRoutineRoom);
                tvTime = v.findViewById(com.campusbondhu.R.id.tvRoutineTime);
                tvDay = v.findViewById(com.campusbondhu.R.id.tvRoutineDay);
            }
        }
    }
}

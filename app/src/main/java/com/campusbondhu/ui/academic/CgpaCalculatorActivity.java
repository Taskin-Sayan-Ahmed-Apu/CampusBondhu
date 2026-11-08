package com.campusbondhu.ui.academic;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.Course;
import com.campusbondhu.data.repository.AcademicRepository;
import com.campusbondhu.databinding.ActivityCgpaCalculatorBinding;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CgpaCalculatorActivity extends AppCompatActivity {
    private ActivityCgpaCalculatorBinding binding;
    private final List<Course> courseList = new ArrayList<>();
    private final Map<String, Double> gradePoints = new HashMap<>();
    private GradeAdapter adapter;

    // Grade -> Point Mapping
    private final String[] grades = {"A+ (4.00)", "A (3.75)", "A- (3.50)", "B+ (3.25)", "B (3.00)", "B- (2.75)", "C+ (2.50)", "C (2.25)", "D (2.00)", "F (0.00)"};
    private final double[] points = {4.00, 3.75, 3.50, 3.25, 3.00, 2.75, 2.50, 2.25, 2.00, 0.00};

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityCgpaCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvCourses.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GradeAdapter();
        binding.rvCourses.setAdapter(adapter);

        new AcademicRepository().getAllCourses().observe(this, list -> {
            courseList.clear();
            if (list != null) courseList.addAll(list);
            adapter.notifyDataSetChanged();
            if (courseList.isEmpty()) {
                binding.tvEmpty.setVisibility(View.VISIBLE);
            } else {
                binding.tvEmpty.setVisibility(View.GONE);
            }
        });

        binding.btnCalculate.setOnClickListener(v -> calculate());
    }

    private void calculate() {
        double totalCredits = 0;
        double totalPoints = 0;

        for (Course c : courseList) {
            Double point = gradePoints.get(c.getCourseId());
            if (point != null) {
                totalCredits += c.getCredit();
                totalPoints += (c.getCredit() * point);
            }
        }

        if (totalCredits == 0) {
            binding.tvResult.setText("Please select grades first.");
            return;
        }

        double gpa = totalPoints / totalCredits;
        binding.tvResult.setText("Your GPA: " + String.format("%.2f", gpa));
    }

    private class GradeAdapter extends RecyclerView.Adapter<GradeAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(LayoutInflater.from(p.getContext()).inflate(
                com.campusbondhu.R.layout.item_grade_input, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            Course c = courseList.get(pos);
            h.tvCode.setText(c.getCode() + " (" + c.getCredit() + " cr)");
            h.tvName.setText(c.getName());

            ArrayAdapter<String> adp = new ArrayAdapter<>(CgpaCalculatorActivity.this,
                android.R.layout.simple_spinner_item, grades);
            adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            h.spinner.setAdapter(adp);

            h.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override public void onItemSelected(AdapterView<?> p, View v, int p2, long id) {
                    gradePoints.put(c.getCourseId(), points[p2]);
                }
                @Override public void onNothingSelected(AdapterView<?> p) {}
            });
        }
        @Override public int getItemCount() { return courseList.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvCode, tvName;
            Spinner spinner;
            VH(View v) {
                super(v);
                tvCode = v.findViewById(com.campusbondhu.R.id.tvCourseCode);
                tvName = v.findViewById(com.campusbondhu.R.id.tvCourseName);
                spinner = v.findViewById(com.campusbondhu.R.id.spinnerGrade);
            }
        }
    }
}

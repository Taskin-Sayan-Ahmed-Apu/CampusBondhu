package com.campusbondhu.ui.admin;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.User;
import com.campusbondhu.databinding.ActivityManageUsersBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.ArrayList;
import java.util.List;

public class ManageUsersActivity extends AppCompatActivity {
    private ActivityManageUsersBinding binding;
    private final List<User> data = new ArrayList<>();
    private UserAdapter adapter;
    private ListenerRegistration listener;
    private final String[] roles = {"student", "teacher", "guardian", "admin"};

    @Override
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityManageUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.btnBack.setOnClickListener(v -> finish());

        binding.rvUsers.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdapter();
        binding.rvUsers.setAdapter(adapter);

        listener = FirebaseFirestore.getInstance().collection("users").addSnapshotListener((snap, e) -> {
            if (e != null || snap == null) return;
            data.clear();
            snap.forEach(d -> {
                User u = d.toObject(User.class);
                if (u != null) data.add(u);
            });
            adapter.notifyDataSetChanged();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (listener != null) listener.remove();
    }

    private class UserAdapter extends RecyclerView.Adapter<UserAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_manage_user, p, false));
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            User u = data.get(pos);
            h.tvName.setText(u.getName() + " (" + u.getEmail() + ")");
            h.tvId.setText("ID: " + (u.getStudentId() != null ? u.getStudentId() : "N/A"));

            ArrayAdapter<String> adp = new ArrayAdapter<>(ManageUsersActivity.this,
                android.R.layout.simple_spinner_item, roles);
            adp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            h.spinnerRole.setAdapter(adp);

            int currentRoleIndex = 0;
            for (int i = 0; i < roles.length; i++) {
                if (roles[i].equals(u.getRole())) { currentRoleIndex = i; break; }
            }
            h.spinnerRole.setSelection(currentRoleIndex);

            h.btnUpdate.setOnClickListener(v -> {
                String newRole = h.spinnerRole.getSelectedItem().toString();
                FirebaseFirestore.getInstance().collection("users").document(u.getUid())
                    .update("role", newRole)
                    .addOnSuccessListener(a -> Toast.makeText(ManageUsersActivity.this, "Role Updated", Toast.LENGTH_SHORT).show());
            });
        }
        @Override public int getItemCount() { return data.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvName, tvId;
            Spinner spinnerRole;
            com.google.android.material.button.MaterialButton btnUpdate;
            VH(View v) {
                super(v);
                tvName = v.findViewById(com.campusbondhu.R.id.tvUserName);
                tvId = v.findViewById(com.campusbondhu.R.id.tvUserId);
                spinnerRole = v.findViewById(com.campusbondhu.R.id.spinnerUserRole);
                btnUpdate = v.findViewById(com.campusbondhu.R.id.btnUpdateRole);
            }
        }
    }
}

package com.campusbondhu.ui.dashboard;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.campusbondhu.databinding.ActivityAdminDashboardBinding;
import com.campusbondhu.ui.academic.ManageCoursesActivity;
import com.campusbondhu.ui.academic.ManageRoutineActivity;
import com.campusbondhu.ui.admin.ManageMarketplaceActivity;
import com.campusbondhu.ui.admin.ManageUsersActivity;
import com.campusbondhu.ui.auth.LoginActivity;
import com.campusbondhu.ui.bus.ManageBusActivity;
import com.campusbondhu.ui.canteen.CanteenMenuActivity;
import com.campusbondhu.ui.library.AddBookActivity;
import com.campusbondhu.ui.notice.NoticeListActivity;
import com.campusbondhu.ui.profile.ProfileActivity;
import com.campusbondhu.ui.sos.SOSAlertsListActivity;
import com.campusbondhu.ui.studyhub.UploadResourceActivity;
import com.campusbondhu.ui.wallet.WalletActivity;
import com.campusbondhu.utils.AlarmHelper;
import com.campusbondhu.utils.NotificationHelper;
import com.campusbondhu.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class AdminDashboardActivity extends AppCompatActivity {
    private ActivityAdminDashboardBinding binding;
    private AlarmHelper alarmHelper = new AlarmHelper();
    private ListenerRegistration sosListener, marketListener, orderListener;
    private AlertDialog sosDialog;

    @Override 
    protected void onCreate(Bundle s) {
        super.onCreate(s);
        binding = ActivityAdminDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        SessionManager session = new SessionManager(this);

        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut(); session.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i); finish();
        });
        
        // ✅ ডিপার্টমেন্ট বাদ দেওয়া হলো
        String[][] modules = {
            {"👥","Users","Roles"},
            {"📚","Library","Books"}, {"🍔","Canteen","Menu"},
            {"💰","Wallet","Txns"}, {"🚌","Bus","Routes"},
            {"📖","Study Hub","Files"}, {"🛒","Marketplace","Moderate"},
            {"📊","CGPA","Courses"}, {"📅","Routine","Schedules"},
            {"🆘","SOS","Alerts"}, {"📢","Notices","Monitor"}
        };
        binding.rvAdminModules.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvAdminModules.setAdapter(new ModuleAdapter(modules, this::onModuleClick));

        listenForSOS();
        listenForMarketplaceListings();
        listenForNewOrders();
    }

    private void listenForSOS() {
        sosListener = FirebaseFirestore.getInstance().collection("emergencyAlerts")
            .whereEqualTo("status", "ACTIVE")
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                if (!snap.isEmpty()) {
                    if (sosDialog == null || !sosDialog.isShowing()) {
                        triggerSOSAlarm(snap.getDocuments().get(0).getString("studentName"));
                    }
                } else {
                    stopSOSAlarm();
                }
            });
    }

    private void listenForMarketplaceListings() {
        marketListener = FirebaseFirestore.getInstance().collection("marketplaceListings")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null || snap.isEmpty()) return;
                for (DocumentChange dc : snap.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        com.campusbondhu.data.model.MarketplaceListing item = dc.getDocument().toObject(com.campusbondhu.data.model.MarketplaceListing.class);
                        if (item != null && System.currentTimeMillis() - item.getTimestamp() < 15000) {
                            NotificationHelper.showNotification(this, "🛒 New Listing", item.getSellerName() + " listed " + item.getTitle(), 101);
                            new AlertDialog.Builder(this).setTitle("🛒 New Marketplace Listing")
                                .setMessage(item.getSellerName() + " has listed: " + item.getTitle() + " for ৳ " + item.getPrice())
                                .setPositiveButton("OK", null).show();
                        }
                    }
                }
            });
    }

    private void listenForNewOrders() {
        orderListener = FirebaseFirestore.getInstance().collection("orders")
            .whereEqualTo("status", "PENDING")
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null) return;
                for (DocumentChange dc : snap.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        com.campusbondhu.data.model.FoodOrder order = dc.getDocument().toObject(com.campusbondhu.data.model.FoodOrder.class);
                        if (order != null && System.currentTimeMillis() - order.getTimestamp() < 15000) {
                            NotificationHelper.showNotification(this, "🍔 New Food Order!", order.getStudentName() + " ordered " + order.getItemName(), 102);
                            new AlertDialog.Builder(this).setTitle("🍔 New Order Received")
                                .setMessage(order.getStudentName() + " ordered: " + order.getItemName() + " x" + order.getQuantity())
                                .setPositiveButton("OK", null).show();
                        }
                    }
                }
            });
    }

    private void triggerSOSAlarm(String studentName) {
        alarmHelper.playAlarm(this);
        sosDialog = new AlertDialog.Builder(this)
            .setTitle("🚨 EMERGENCY SOS ALERT 🚨")
            .setMessage(studentName + " is in danger! An alarm is ringing until you resolve this.")
            .setCancelable(false)
            .setPositiveButton("STOP ALARM & RESOLVE", (dialog, which) -> {
                stopSOSAlarm();
                FirebaseFirestore.getInstance().collection("emergencyAlerts")
                    .whereEqualTo("status", "ACTIVE").get().addOnSuccessListener(querySnapshot -> {
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot) {
                            doc.getReference().update("status", "RESOLVED");
                        }
                    });
            })
            .setNegativeButton("View on Map", (dialog, which) -> {
                startActivity(new Intent(AdminDashboardActivity.this, SOSAlertsListActivity.class));
                dialog.dismiss();
            }).show();
    }

    private void stopSOSAlarm() {
        alarmHelper.stopAlarm();
        if (sosDialog != null && sosDialog.isShowing()) sosDialog.dismiss();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sosListener != null) sosListener.remove();
        if (marketListener != null) marketListener.remove();
        if (orderListener != null) orderListener.remove();
        stopSOSAlarm();
    }

    private void onModuleClick(String title) {
        switch (title) {
            case "Library": startActivity(new Intent(this, AddBookActivity.class)); break;
            case "Notices": startActivity(new Intent(this, NoticeListActivity.class)); break;
            case "Canteen": startActivity(new Intent(this, CanteenMenuActivity.class)); break;
            case "Wallet": startActivity(new Intent(this, WalletActivity.class)); break;
            case "Bus": startActivity(new Intent(this, ManageBusActivity.class)); break;
            case "Study Hub": startActivity(new Intent(this, UploadResourceActivity.class)); break;
            case "CGPA": startActivity(new Intent(this, ManageCoursesActivity.class)); break;
            case "Routine": startActivity(new Intent(this, ManageRoutineActivity.class)); break;
            case "SOS": startActivity(new Intent(this, SOSAlertsListActivity.class)); break;
            case "Users": startActivity(new Intent(this, ManageUsersActivity.class)); break;
            case "Marketplace": startActivity(new Intent(this, ManageMarketplaceActivity.class)); break;
            default: Toast.makeText(this, "Admin: " + title + " — coming soon", Toast.LENGTH_SHORT).show();
        }
    }

    private static class ModuleAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ModuleAdapter.VH> {
        interface OnClick { void onClick(String t); }
        private final String[][] data; private final OnClick listener;
        ModuleAdapter(String[][] d, OnClick l) { data = d; listener = l; }
        @Override public VH onCreateViewHolder(android.view.ViewGroup p, int t) {
            return new VH(android.view.LayoutInflater.from(p.getContext())
                .inflate(com.campusbondhu.R.layout.item_dashboard_module, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            String[] m = data[pos];
            h.tvIcon.setText(m[0]); h.tvTitle.setText(m[1]); h.tvSubtitle.setText(m[2]);
            h.itemView.setOnClickListener(v -> listener.onClick(m[1]));
        }
        @Override public int getItemCount() { return data.length; }
        static class VH extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            android.widget.TextView tvIcon, tvTitle, tvSubtitle;
            VH(View v) { super(v);
                tvIcon = v.findViewById(com.campusbondhu.R.id.tvIcon);
                tvTitle = v.findViewById(com.campusbondhu.R.id.tvTitle);
                tvSubtitle = v.findViewById(com.campusbondhu.R.id.tvSubtitle);
            }
        }
    }
}

package com.campusbondhu.ui.dashboard;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.databinding.ActivityStudentDashboardBinding;
import com.campusbondhu.ui.academic.CgpaCalculatorActivity;
import com.campusbondhu.ui.academic.ViewRoutineActivity;
import com.campusbondhu.ui.assistant.AIChatActivity;
import com.campusbondhu.ui.attendance.ScanAttendanceActivity;
import com.campusbondhu.ui.auth.LoginActivity;
import com.campusbondhu.ui.bus.BusTrackingActivity;
import com.campusbondhu.ui.canteen.CanteenMenuActivity;
import com.campusbondhu.ui.library.LibraryHomeActivity;
import com.campusbondhu.ui.marketplace.MarketplaceActivity;
import com.campusbondhu.ui.notice.NoticeListActivity;
import com.campusbondhu.ui.profile.ProfileActivity;
import com.campusbondhu.ui.sos.SOSActivity;
import com.campusbondhu.ui.studyhub.StudyHubActivity;
import com.campusbondhu.ui.wallet.WalletActivity;
import com.campusbondhu.utils.NotificationHelper;
import com.campusbondhu.utils.SessionManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

public class StudentDashboardActivity extends AppCompatActivity {
    private ActivityStudentDashboardBinding binding;
    private SessionManager session;
    private FirebaseFirestore db;
    private String uid;

    private ListenerRegistration noticeListener, walletListener, orderListener, attendanceListener;
    private double lastKnownBalance = -1;
    private boolean isFirstWalletLoad = true;

    private final BroadcastReceiver airplaneReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_AIRPLANE_MODE_CHANGED.equals(intent.getAction())) {
                boolean isOn = intent.getBooleanExtra("state", false);
                String msg = isOn ? "Airplane Mode ON. Internet disabled!" : "Airplane Mode OFF. Connected!";
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                NotificationHelper.showNotification(context, "✈️ Network Status", msg, 999);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityStudentDashboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        session = new SessionManager(this);
        db = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        binding.tvGreeting.setText("Hello, " + (session.getName() != null ? session.getName() : "Student") + " 👋");

        binding.btnProfile.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class)));
        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            session.clear();
            Intent i = new Intent(this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i); finish();
        });

        binding.btnSosQuick.setOnClickListener(v -> startActivity(new Intent(this, SOSActivity.class)));
        
        loadAndInitWallet();
        setupGrid();
        startRealTimeAlerts();
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(airplaneReceiver, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(airplaneReceiver, filter);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        try { unregisterReceiver(airplaneReceiver); } catch (Exception ignored) {}
    }

    private void startRealTimeAlerts() {
        noticeListener = db.collection("notices")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null || snap.isEmpty()) return;
                for (DocumentChange dc : snap.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED) {
                        com.campusbondhu.data.model.Notice n = dc.getDocument().toObject(com.campusbondhu.data.model.Notice.class);
                        if (n != null && System.currentTimeMillis() - n.getTimestamp() < 15000) {
                            showPopup("📢 New Notice", n.getTitle());
                        }
                    }
                }
            });

        orderListener = db.collection("orders")
            .whereEqualTo("uid", uid)
            .whereEqualTo("status", "COLLECTED")
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null || snap.isEmpty()) return;
                for (DocumentChange dc : snap.getDocumentChanges()) {
                    if (dc.getType() == DocumentChange.Type.ADDED || dc.getType() == DocumentChange.Type.MODIFIED) {
                        com.campusbondhu.data.model.FoodOrder o = dc.getDocument().toObject(com.campusbondhu.data.model.FoodOrder.class);
                        if (o != null && System.currentTimeMillis() - o.getTimestamp() < 60000) {
                            showPopup("🍔 Food Ready!", "Your order " + o.getItemName() + " is ready for pickup.");
                        }
                    }
                }
            });

        walletListener = db.collection("wallets").document(uid).addSnapshotListener((doc, e) -> {
            if (e != null || doc == null || !doc.exists()) return;
            Double bal = doc.getDouble("balance");
            if (bal == null) return;

            if (isFirstWalletLoad) {
                lastKnownBalance = bal;
                binding.tvWalletBalance.setText("৳ " + String.format("%.2f", bal));
                isFirstWalletLoad = false;
            } else if (bal > lastKnownBalance) {
                double added = bal - lastKnownBalance;
                lastKnownBalance = bal;
                binding.tvWalletBalance.setText("৳ " + String.format("%.2f", bal));
                showPopup("💰 Wallet Credited", "৳ " + String.format("%.2f", added) + " added to your demo wallet.");
            } else {
                lastKnownBalance = bal;
                binding.tvWalletBalance.setText("৳ " + String.format("%.2f", bal));
            }
        });

        SharedPreferences prefs = getSharedPreferences("CampusPrefs", Context.MODE_PRIVATE);
        String lastSeenSession = prefs.getString("last_seen_attendance_session", "");

        attendanceListener = db.collection("attendanceSessions")
            .whereEqualTo("active", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(1)
            .addSnapshotListener((snap, e) -> {
                if (e != null || snap == null || snap.isEmpty()) return;
                com.campusbondhu.data.model.AttendanceSession sess = snap.getDocuments().get(0).toObject(com.campusbondhu.data.model.AttendanceSession.class);
                if (sess != null && sess.getSessionId() != null) {
                    if (System.currentTimeMillis() <= sess.getExpiresAt()) {
                        if (!sess.getSessionId().equals(lastSeenSession)) {
                            showPopup("📷 Attendance Live", "Session started for " + sess.getCourseCode() + ". Scan now!");
                            prefs.edit().putString("last_seen_attendance_session", sess.getSessionId()).apply();
                        }
                    }
                }
            });

        checkLibraryDueDates();
    }

    private void checkLibraryDueDates() {
        db.collection("libraryLoans")
            .whereEqualTo("studentId", uid)
            .whereEqualTo("status", "ISSUED")
            .get().addOnSuccessListener(snap -> {
                long oneDayMs = 24 * 60 * 60 * 1000L;
                for (com.google.firebase.firestore.DocumentSnapshot doc : snap) {
                    com.campusbondhu.data.model.LibraryLoan loan = doc.toObject(com.campusbondhu.data.model.LibraryLoan.class);
                    if (loan != null) {
                        long timeLeft = loan.getDueDate() - System.currentTimeMillis();
                        if (timeLeft > 0 && timeLeft < oneDayMs) {
                            showPopup("📚 Library Reminder", "Your book '" + loan.getBookTitle() + "' is due tomorrow!");
                            break;
                        }
                    }
                }
            });
    }

    private void showPopup(String title, String message) {
        NotificationHelper.showNotification(this, title, message, (int) System.currentTimeMillis());
        if (!isFinishing()) {
            new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
        }
    }

    private void loadAndInitWallet() {}

    private void setupGrid() {
        // ✅ ১২টা কার্ড (চ্যাট বাদ দেওয়া হলো)
        String[][] modules = {
            {"📷","Attendance","Scan QR"}, {"📢","Notices","Updates"},
            {"📚","Library","Books"}, {"💰","Wallet","Demo"},
            {"🍔","Canteen","Pre-order"}, {"🚌","Bus","Tracking"},
            {"📖","Study Hub","Notes"}, {"🛒","Marketplace","Buy/Sell"},
            {"📊","CGPA","Calculate"}, {"📅","Routine","Schedule"},
            {"🆘","SOS","Emergency"}, {"🤖","Assistant","Ask"}
        };
        binding.rvModules.setLayoutManager(new GridLayoutManager(this, 3));
        binding.rvModules.setAdapter(new ModuleAdapter(modules, this::onModuleClick));
    }

    private void onModuleClick(String title) {
        switch (title) {
            case "Attendance": startActivity(new Intent(this, ScanAttendanceActivity.class)); break;
            case "Notices": startActivity(new Intent(this, NoticeListActivity.class)); break;
            case "Library": startActivity(new Intent(this, LibraryHomeActivity.class)); break;
            case "Wallet": startActivity(new Intent(this, WalletActivity.class)); break;
            case "Canteen": startActivity(new Intent(this, CanteenMenuActivity.class)); break;
            case "Bus": startActivity(new Intent(this, BusTrackingActivity.class)); break;
            case "Study Hub": startActivity(new Intent(this, StudyHubActivity.class)); break;
            case "Marketplace": startActivity(new Intent(this, MarketplaceActivity.class)); break;
            case "CGPA": startActivity(new Intent(this, CgpaCalculatorActivity.class)); break;
            case "Routine": startActivity(new Intent(this, ViewRoutineActivity.class)); break;
            case "SOS": startActivity(new Intent(this, SOSActivity.class)); break;
            case "Assistant": startActivity(new Intent(this, AIChatActivity.class)); break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (noticeListener != null) noticeListener.remove();
        if (walletListener != null) walletListener.remove();
        if (orderListener != null) orderListener.remove();
        if (attendanceListener != null) attendanceListener.remove();
    }

    private static class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.VH> {
        interface OnClick { void onClick(String title); }
        private final String[][] data; private final OnClick listener;
        ModuleAdapter(String[][] d, OnClick l) { data = d; listener = l; }
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            return new VH(android.view.LayoutInflater.from(p.getContext())
                .inflate(com.campusbondhu.R.layout.item_dashboard_module, p, false));
        }
        @Override public void onBindViewHolder(VH h, int pos) {
            String[] m = data[pos];
            h.tvIcon.setText(m[0]); h.tvTitle.setText(m[1]); h.tvSubtitle.setText(m[2]);
            h.itemView.setOnClickListener(v -> listener.onClick(m[1]));
        }
        @Override public int getItemCount() { return data.length; }
        static class VH extends RecyclerView.ViewHolder {
            TextView tvIcon, tvTitle, tvSubtitle;
            VH(View v) {
                super(v);
                tvIcon = v.findViewById(com.campusbondhu.R.id.tvIcon);
                tvTitle = v.findViewById(com.campusbondhu.R.id.tvTitle);
                tvSubtitle = v.findViewById(com.campusbondhu.R.id.tvSubtitle);
            }
        }
    }
}

package com.campusbondhu.ui.assistant;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.campusbondhu.data.model.ChatMessage;
import com.campusbondhu.data.repository.AIAssistantRepository;
import com.campusbondhu.databinding.ActivityAiChatBinding;
import java.util.ArrayList;
import java.util.List;

public class AIChatActivity extends AppCompatActivity {
    private ActivityAiChatBinding binding;
    private final List<ChatMessage> messages = new ArrayList<>();
    private ChatAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAiChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        binding.btnBack.setOnClickListener(v -> finish());
        binding.rvChat.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter();
        binding.rvChat.setAdapter(adapter);

        addMessage("Hello! I am your CampusBondhu Assistant. Ask me about Notices, Routine, or Library.", false);

        binding.btnSend.setOnClickListener(v -> {
            String text = binding.etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addMessage(text, true);
                binding.etMessage.setText("");
                fetchReply(text);
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        messages.add(new ChatMessage(text, isUser));
        adapter.notifyItemInserted(messages.size() - 1);
        binding.rvChat.scrollToPosition(messages.size() - 1);
    }

    private void fetchReply(String query) {
        new AIAssistantRepository().askAssistant(query).observe(this, reply -> {
            addMessage(reply, false);
        });
    }

    private class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.VH> {
        @NonNull @Override
        public VH onCreateViewHolder(@NonNull ViewGroup p, int t) {
            View v = getLayoutInflater().inflate(
                com.campusbondhu.R.layout.item_chat_msg, p, false);
            return new VH(v);
        }
        @Override public void onBindViewHolder(@NonNull VH h, int pos) {
            ChatMessage msg = messages.get(pos);
            h.tvMsg.setText(msg.getText());
            if (msg.isUser()) {
                h.tvMsg.setBackgroundResource(com.campusbondhu.R.drawable.bg_chat_user);
                h.tvMsg.setTextColor(getColor(com.campusbondhu.R.color.white));
                ((android.widget.LinearLayout.LayoutParams)h.tvMsg.getLayoutParams()).gravity = android.view.Gravity.END;
            } else {
                h.tvMsg.setBackgroundResource(com.campusbondhu.R.drawable.bg_chat_ai);
                h.tvMsg.setTextColor(getColor(com.campusbondhu.R.color.white));
                ((android.widget.LinearLayout.LayoutParams)h.tvMsg.getLayoutParams()).gravity = android.view.Gravity.START;
            }
        }
        @Override public int getItemCount() { return messages.size(); }
        class VH extends RecyclerView.ViewHolder {
            TextView tvMsg;
            VH(View v) {
                super(v);
                tvMsg = v.findViewById(com.campusbondhu.R.id.tvChatMsg);
            }
        }
    }
}

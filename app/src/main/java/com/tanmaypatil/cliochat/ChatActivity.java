package com.tanmaypatil.cliochat;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.ai.client.generativeai.java.ChatFutures;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.android.material.textfield.TextInputEditText;

import io.noties.markwon.Markwon;

public class ChatActivity extends AppCompatActivity {

    private TextInputEditText queryEditText;
    private ImageView btnSend, logo, appIcon;
    private LinearLayout chatResponse;
    private ChatFutures chatModel;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                // Your custom logic goes here
                // For example, showing a confirmation dialog:
                Intent intent2 = new Intent(ChatActivity.this, MainActivity.class);
                finish();
                startActivity(intent2);
            }
        });

        chatModel = getChatModel();

        TextView heading_txt = findViewById(R.id.Heading_txt);
        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        heading_txt.setText(message);

        ImageButton back_btn = findViewById(R.id.back_btn);
        back_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(ChatActivity.this, MainActivity.class);
                finish();
                startActivity(intent1);
            }
        });

        EditText message_edt = findViewById(R.id.message_edt);

        chatBody("You", message, getDrawable(R.drawable.user));

        geminiResp.getResponse(chatModel, message, new ResponseCallback(){
            @Override
            public void onResponse(String response) {
                chatBody("AI", response, getDrawable(R.drawable.generative));
            }
            @Override
            public void onError(Throwable throwable) {
                chatBody("AI", "Sorry, I couldn't understand that.", getDrawable(R.drawable.generative));
            }
        });

        ImageButton sendbtn = findViewById(R.id.send_btn);

        sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = message_edt.getText().toString().trim();
                if(message.isEmpty()){
                    Toast.makeText(ChatActivity.this, "Enter your Prompt first!", Toast.LENGTH_SHORT).show();
                }
                else{
                    chatBody("You", message, getDrawable(R.drawable.user));

                    geminiResp.getResponse(chatModel, message, new ResponseCallback(){
                        @Override
                        public void onResponse(String response) {
                            chatBody("AI", response, getDrawable(R.drawable.generative));
                        }
                        @Override
                        public void onError(Throwable throwable) {
                            chatBody("AI", "Sorry, I couldn't understand that.", getDrawable(R.drawable.generative));
                        }
                    });
                    message_edt.setText("");
                }
            }
        });



    }
    private ChatFutures getChatModel(){
        geminiResp model = new geminiResp();
        GenerativeModelFutures modelFutures = model.getModel();
        return modelFutures.startChat();
    }

    private void chatBody(String username, String query, Drawable drawable) {
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.chat_message, null);
        ImageView logo = view.findViewById(R.id.logo);
        TextView name = view.findViewById(R.id.name);
        TextView message = view.findViewById(R.id.message);
        final Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(message, query);
        logo.setImageDrawable(drawable);
        name.setText(username);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                // Get the text from the message TextView
                String textToCopy = message.getText().toString();

                // Copy to clipboard
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("ClioChat", textToCopy);
                if (clipboard != null) {
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(ChatActivity.this, "Copied to Clipboard!", Toast.LENGTH_SHORT).show();
                }
                return true; // Return true so it doesn't trigger a normal click
            }
        });
        //message.setText(query);
        chatResponse = findViewById(R.id.chatResponse);
        chatResponse.addView(view);
        ScrollView scrollView = findViewById(R.id.scrollView);
        scrollView.post(() -> scrollView.fullScroll(View.FOCUS_DOWN));
    }
}
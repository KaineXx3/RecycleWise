package com.example.fyp1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.fyp1.Adapter.ChatMessageAdapter;
import com.example.fyp1.Model.ChatMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;

public class AiChatBot extends AppCompatActivity {
    private EditText editText;
    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    private String stringAPIKey = "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"; // Ensure this API key is valid
    private String stringURLEndPoint = "https://api-inference.huggingface.co/models/microsoft/Phi-3-mini-4k-instruct/v1/chat/completions";
    ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycler_view_chat_bot);

        recyclerView = findViewById(R.id.recyclerView);
        editText = findViewById(R.id.editTextText);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatMessageAdapter(chatMessages);
        recyclerView.setAdapter(adapter);

        chatMessages.add(new ChatMessage("What can I help you with?", false));
        adapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);
    }

    public void SentMessage(View v) {
        buttonPaLMAPI(v);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void buttonPaLMAPI(View view) {
        String stringInputText = editText.getText().toString();
        if (stringInputText.isEmpty()) {
            return;
        }

        // Add user's message to the chat
        chatMessages.add(new ChatMessage(stringInputText, true));
        adapter.notifyItemInserted(chatMessages.size() - 1);
        recyclerView.scrollToPosition(chatMessages.size() - 1);

        // Clear the input field
        editText.setText("");

        JSONObject jsonObject = new JSONObject();
        JSONArray jsonObjectMessageArray = new JSONArray();
        try {
            JSONObject jsonObjectMessage = new JSONObject();
            jsonObjectMessage.put("role", "user");
            jsonObjectMessage.put("content", stringInputText);
            jsonObjectMessageArray.put(jsonObjectMessage);

            jsonObject.put("messages", jsonObjectMessageArray);
            jsonObject.put("model", "microsoft/Phi-3-mini-4k-instruct");
            jsonObject.put("max_tokens", 500);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                stringURLEndPoint,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String stringOutput = response.getJSONArray("choices")
                                    .getJSONObject(0)
                                    .getJSONObject("message")
                                    .getString("content");

                            // Add AI's message to the chat
                            chatMessages.add(new ChatMessage(stringOutput, false));
                            adapter.notifyItemInserted(chatMessages.size() - 1);
                            recyclerView.scrollToPosition(chatMessages.size() - 1);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                String errorMessage = "Error: " + error.toString();
                if (error.networkResponse != null) {
                    int statusCode = error.networkResponse.statusCode;
                    String data = new String(error.networkResponse.data);
                    errorMessage += " Status Code: " + statusCode + " Data: " + data;
                }
                Log.e("VolleyError", errorMessage);

                if (!isNetworkAvailable()) {
                    MotionToast.Companion.darkToast(AiChatBot.this,
                            "No Internet Connection",
                            "Please check your internet settings.",
                            MotionToastStyle.WARNING,
                            MotionToast.GRAVITY_BOTTOM,
                            MotionToast.LONG_DURATION,
                            ResourcesCompat.getFont(AiChatBot.this, www.sanju.motiontoast.R.font.helveticabold));
                    return;
                }

                chatMessages.add(new ChatMessage(errorMessage, false));
                adapter.notifyItemInserted(chatMessages.size() - 1);
                recyclerView.scrollToPosition(chatMessages.size() - 1);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> mapHeader = new HashMap<>();
                mapHeader.put("Content-Type", "application/json");
                mapHeader.put("Authorization", "Bearer " + stringAPIKey);
                return mapHeader;
            }
        };

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                120000, // Timeout for long responses
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));
        Volley.newRequestQueue(getApplicationContext()).add(jsonObjectRequest);
    }
}

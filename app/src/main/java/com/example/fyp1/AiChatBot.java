package com.example.fyp1;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
import java.util.Iterator;
import java.util.Map;

import www.sanju.motiontoast.MotionToast;
import www.sanju.motiontoast.MotionToastStyle;


public class AiChatBot extends AppCompatActivity {
    private EditText editText;
    private RecyclerView recyclerView;
    private ChatMessageAdapter adapter;
    private ArrayList<ChatMessage> chatMessages = new ArrayList<>();
    private String stringAPIKey = "N2UzOWNjMWQtZTI5YS00NDBkLTkxY2ItY2ZlYWNhMGUwZGVkYjlhZjg2ZmQtZmY0_P0A1_caac8e9f-ddc7-4e4f-ab82-d031172a892e"; // Ensure this API key is valid
    private String stringURLEndPoint = "https://nzltcf.buildship.run/Recycle-Wise-Chatbot";

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

        // Prepare JSON object for API request
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("message", stringInputText); // Update to match the cURL format
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        // Create JSON request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                stringURLEndPoint,
                jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            // Log the entire response to inspect its structure
                            Log.d("AI Response", response.toString());

                            // Get the first (and only) key in the response object, which is the UUID
                            Iterator<String> keys = response.keys();
                            if (keys.hasNext()) {
                                String key = keys.next();
                                String stringOutput = response.getString(key);  // Get the recycling tip message
                                chatMessages.add(new ChatMessage(stringOutput, false));
                                adapter.notifyItemInserted(chatMessages.size() - 1);
                                recyclerView.scrollToPosition(chatMessages.size() - 1);
                            } else {
                                // Handle case where no keys are found
                                chatMessages.add(new ChatMessage("No response found", false));
                                adapter.notifyItemInserted(chatMessages.size() - 1);
                                recyclerView.scrollToPosition(chatMessages.size() - 1);
                            }
                        } catch (JSONException e) {
                            Log.e("JSON Error", "Error parsing response: " + e.getMessage());
                            chatMessages.add(new ChatMessage("Error parsing the response", false));
                            adapter.notifyItemInserted(chatMessages.size() - 1);
                            recyclerView.scrollToPosition(chatMessages.size() - 1);
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
                    Toast.makeText(AiChatBot.this, "No network connectivity. Please check your connection.", Toast.LENGTH_LONG).show();
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
                mapHeader.put("Content-Type", "application/json; charset=utf-8");
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

package com.pcordoba.speechtotexttest1;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RestPoster {

    Context context;

    public RestPoster(Context context) {
        this.context = context;
    }

    private String responseText;

    public void postVoiceResult(ArrayList<String> text, String restUrl, String restPort, Context context) {
        JSONObject toPost = jsonParser(text);

        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "http://" + restUrl + ":" + restPort;

        JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.POST, url, toPost, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                updateReturnedText(response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                updateReturnedText(error.toString());
            }
        }) {
        };

        queue.add(jsonRequest);

    }

    private JSONObject jsonParser(ArrayList<String> matches) {
        JSONObject jObject = new JSONObject();
        JSONArray jsonArray = new JSONArray();

        for (String result : matches) {
            jsonArray.put(result.toString());
        }
        try {
            jObject.put("mesasage", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jObject;
    }

    private void updateReturnedText(String response) {

        TextView returnedText = (TextView) ((Activity) context).findViewById(R.id.response);
        returnedText.setText("Response is: " + response);
    }

}

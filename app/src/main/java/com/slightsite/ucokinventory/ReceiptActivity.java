package com.slightsite.ucokinventory;

/**
 * Created by mrsvette on 13/03/18.
 */

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReceiptActivity extends MainActivity {
    Intent intent;
    ProgressDialog pDialog;
    int success;

    private String issue_list_url = Server.URL + "receipt/list-issue?api-key=" + Server.API_KEY;
    private String get_issue_url = Server.URL + "receipt/get-issue?api-key=" + Server.API_KEY;

    private static final String TAG = ReceiptActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_receipt);
        buildMenu();

        Button btn_login = (Button) findViewById(R.id.btn_next);
        btn_login.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //final Spinner feedbackSpinner = (Spinner) findViewById(R.id.ReceiptType);
                //String ReceiptType = feedbackSpinner.getSelectedItem().toString();
                LinearLayout step1 = (LinearLayout) findViewById(R.id.step1);
                step1.setVisibility(View.GONE);
                LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                step2.setVisibility(View.VISIBLE);

                EditText issue_number = (EditText) findViewById(R.id.txt_issue_no);

                Map<String, String> params = new HashMap<String, String>();
                params.put("issue_number", issue_number.getText().toString());

                _request(get_issue_url, Request.Method.GET, params);
            }
        });
    }

    @Override
    public void onBackPressed() {
        intent = new Intent(ReceiptActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void _request(String url, int method, final Map params) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Request data for " + params.get("issue_number"));
        showDialog();

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator2 = params.entrySet().iterator();

            while(iterator2.hasNext())
            {
                Map.Entry<String, String> pair = iterator2.next();
                url += "&" + pair.getKey() + "=" + pair.getValue();
            }
        }

        StringRequest strReq = new StringRequest(method,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "Response: " + response.toString());
                        hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            success = jObj.getInt(TAG_SUCCESS);

                            // Check for error node in json
                            if (success == 1) {
                                JSONObject data = jObj.getJSONObject("data");
                                String data_type = data.getString("type");

                                Log.e("Successfully Request!", data.toString());

                                Toast.makeText(getApplicationContext(), data_type, Toast.LENGTH_LONG).show();

                            } else {
                                Toast.makeText(getApplicationContext(),
                                        jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Login Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_LONG).show();

                        hideDialog();

                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        return params;
                    }
                };

        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
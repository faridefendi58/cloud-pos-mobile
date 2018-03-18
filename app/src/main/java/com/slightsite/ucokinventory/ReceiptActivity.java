package com.slightsite.ucokinventory;

/**
 * Created by mrsvette on 13/03/18.
 */

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
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
    int success, req;

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

                EditText issue_number = (EditText) findViewById(R.id.txt_issue_no);

                Map<String, String> params = new HashMap<String, String>();
                params.put("issue_number", issue_number.getText().toString());

                //_request(get_issue_url, Request.Method.GET, params);
                _string_request(Request.Method.GET, get_issue_url, params,
                        new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.e(TAG, "Response: " + result.toString());
                                hideDialog();
                                try {
                                    JSONObject jObj = new JSONObject(result);
                                    success = jObj.getInt(TAG_SUCCESS);
                                    // Check for error node in json
                                    if (success == 1) {
                                        JSONObject data = jObj.getJSONObject("data");
                                        String data_status = data.getString("status");
                                        String data_type = data.getString("type");

                                        Log.e("Successfully Request!", data.toString());

                                        LinearLayout step1 = (LinearLayout) findViewById(R.id.step1);
                                        step1.setVisibility(View.GONE);
                                        if (data_status.equals("onprocess")) {
                                            //set notes
                                            EditText txt_receipt_notes = (EditText) findViewById(R.id.txt_receipt_notes);
                                            //get session
                                            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
                                            //get issue numb
                                            String fin_issue_number, fin_from;
                                            if (data_type.equals("puchase_order")) {
                                                fin_issue_number = data.getString("po_number");
                                                fin_from = data.getString("supplier_name");
                                            } else if (data_type.equals("transfer_issue")) {
                                                fin_issue_number = data.getString("ti_number");
                                                fin_from = data.getString("warehouse_from_name");
                                            } else {
                                                fin_issue_number = data.getString("po_number");
                                                fin_from = data.getString("supplier_name");
                                            }
                                            TextView txt_step2_issue_no = (TextView) findViewById(R.id.txt_step2_issue_no);
                                            txt_step2_issue_no.setText(fin_issue_number);
                                            TextView txt_step2_from = (TextView) findViewById(R.id.txt_step2_from);
                                            txt_step2_from.setText(fin_from);
                                            //build the notes
                                            String notes = "Telah diterima oleh "+sharedpreferences.getString("name", null);
                                            notes += " dengan rincian :";
                                            TextView txt_step2_label1 = (TextView) findViewById(R.id.txt_step2_label1);
                                            txt_step2_label1.setText(notes);
                                            //txt_receipt_notes.setSelection(txt_receipt_notes.getText().length());

                                            LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                                            step2.setVisibility(View.VISIBLE);
                                        } else {
                                            LinearLayout step3 = (LinearLayout) findViewById(R.id.step3);
                                            step3.setVisibility(View.VISIBLE);
                                        }
                                    } else {
                                        Toast.makeText(getApplicationContext(),
                                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            }
        });
    }

    @Override
    public void onBackPressed() {
        intent = new Intent(ReceiptActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    /**
     * Basic volley request
     * @param url
     * @param method
     * @param params
     */
    public void _request(String url, int method, final Map params) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Request data for " + params.get("issue_number"));
        showDialog();

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
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

    public void _string_request(int method, String url, Map params, final VolleyCallback callback) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Request data ...");
        showDialog();

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
                url += "&" + pair.getKey() + "=" + pair.getValue();
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener < String > () {

            @Override
            public void onResponse(String Response) {
                callback.onSuccess(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(TAG, "Request Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
            }
        })
        {
            // set headers
            @Override
            public Map < String, String > getHeaders() throws com.android.volley.AuthFailureError {
                Map < String, String > params2 = new HashMap < String, String > ();
                params2.put("Authorization: Basic", Server.API_KEY);
                Iterator<Map.Entry<String, String>> iterator2 = params2.entrySet().iterator();
                while(iterator2.hasNext())
                {
                    Map.Entry<String, String> pair = iterator2.next();
                    params2.put(pair.getKey(), pair.getValue());
                }
                return params2;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }
}
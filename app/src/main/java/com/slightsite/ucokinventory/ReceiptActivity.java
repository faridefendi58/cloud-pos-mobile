package com.slightsite.ucokinventory;

/**
 * Created by mrsvette on 13/03/18.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class ReceiptActivity extends MainActivity {
    Intent intent;
    ProgressDialog pDialog;
    int success;

    private String issue_list_url = Server.URL + "receipt/list-issue-number?api-key=" + Server.API_KEY;
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

        //autocomplete
        ArrayList items = set_auto_complete();
        AutoCompleteTextView txt_issue_number = (AutoCompleteTextView)findViewById(R.id.txt_issue_no);
        ArrayAdapter adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1,items);

        txt_issue_number.setAdapter(adapter);
        txt_issue_number.setThreshold(1);

        Button btn_next = (Button) findViewById(R.id.btn_next);
        final Context ini = this;
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboardFrom(ini, v);

                AutoCompleteTextView issue_number = (AutoCompleteTextView) findViewById(R.id.txt_issue_no);

                Map<String, String> params = new HashMap<String, String>();
                params.put("issue_number", issue_number.getText().toString());

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
                                        if (data_status.equals("onprocess") || data_status.equals("pending")) {
                                            //set notes
                                            EditText txt_receipt_notes = (EditText) findViewById(R.id.txt_receipt_notes);
                                            //get session
                                            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
                                            //get issue numb
                                            TextView txt_step2_label1 = (TextView) findViewById(R.id.txt_step2_label1);
                                            String fin_issue_number, fin_from;
                                            if (data_type.equals("purchase_order")) {
                                                fin_issue_number = data.getString("po_number");
                                                fin_from = data.getString("supplier_name");
                                            } else if (data_type.equals("transfer_issue")) {
                                                fin_issue_number = data.getString("ti_number");
                                                fin_from = data.getString("warehouse_from_name");
                                                txt_receipt_notes.setVisibility(View.GONE);
                                                txt_step2_label1.setVisibility(View.GONE);
                                                Button btn_confirm = (Button) findViewById(R.id.btn_confirm);
                                                btn_confirm.setVisibility(View.GONE);
                                            } else {
                                                fin_issue_number = data.getString("po_number");
                                                fin_from = data.getString("supplier_name");
                                            }
                                            TextView txt_step2_issue_no = (TextView) findViewById(R.id.txt_step2_issue_no);
                                            txt_step2_issue_no.setText(fin_issue_number);
                                            TextView txt_step2_from = (TextView) findViewById(R.id.txt_step2_from);
                                            txt_step2_from.setText(fin_from);
                                            TextView txt_step2_type = (TextView) findViewById(R.id.txt_step2_type);
                                            txt_step2_type.setText(data.getString("type"));

                                            LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                                            step2.setVisibility(View.VISIBLE);

                                            //set the list
                                            ArrayList<String> list_items = new ArrayList<String>();
                                            JSONArray items_data = data.getJSONArray("items");
                                            for(int n = 0; n < items_data.length(); n++)
                                            {
                                                JSONObject json_obj_n = items_data.getJSONObject(n);
                                                list_items.add(
                                                        json_obj_n.getString("product_name")+" " +
                                                                "["+json_obj_n.getString("quantity")+" " +
                                                                ""+json_obj_n.getString("unit")+"]");
                                            }
                                            ArrayAdapter adapter2 = new ArrayAdapter<String>(ini,
                                                    R.layout.activity_list_view, list_items);

                                            ListView listView = (ListView) findViewById(R.id.list);
                                            listView.setAdapter(adapter2);
                                        } else {

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
        // second button action
        Button btn_confirm = (Button) findViewById(R.id.btn_confirm);
        btn_confirm_trigger(btn_confirm, ini);

        Spinner step2_receipt_type = (Spinner) findViewById(R.id.step2_receipt_type);
        select_receipt_type(step2_receipt_type, ini);
        Button btn_copy = (Button) findViewById(R.id.btn_copy);
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txt_step3_message = (TextView) findViewById(R.id.txt_step3_message);
                setClipboard(ini, txt_step3_message.getText().toString());
                Toast.makeText(getApplicationContext(),"Pesan berhasil disalin.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void btn_confirm_trigger(Button btn, Context ini) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText notes = (EditText) findViewById(R.id.txt_receipt_notes);
                if (notes.getText().toString().trim().length() <= 0 ) {
                    Toast.makeText(getApplicationContext(), "Kolom rincian barang masih kosong", Toast.LENGTH_LONG).show();
                }
                TextView txt_step2_issue_no = (TextView) findViewById(R.id.txt_step2_issue_no);
                TextView txt_step2_type = (TextView) findViewById(R.id.txt_step2_type);
                Map<String, String> params = new HashMap<String, String>();
                params.put("notes", notes.getText().toString());
                params.put("issue_number", txt_step2_issue_no.getText().toString());
                params.put("type", txt_step2_type.getText().toString());
                params.put("admin_id", sharedpreferences.getString("id", null));
                String confirm_url = Server.URL + "receipt/confirm?api-key=" + Server.API_KEY;
                _string_request(
                        Request.Method.POST,
                        confirm_url,
                        params,
                        new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                Log.e(TAG, "Response: " + result.toString());
                                hideDialog();
                                try {
                                    JSONObject jObj = new JSONObject(result);
                                    success = jObj.getInt(TAG_SUCCESS);
                                    if (success == 1) {
                                        TextView msg = (TextView) findViewById(R.id.txt_step3_message);
                                        TextView txt_step2_issue_no = (TextView) findViewById(R.id.txt_step2_issue_no);
                                        EditText txt_receipt_notes = (EditText) findViewById(R.id.txt_receipt_notes);
                                        TextView txt_step2_from = (TextView) findViewById(R.id.txt_step2_from);
                                        String success_msg = "Nomor pengadaan "+ txt_step2_issue_no.getText().toString()
                                                +" dari "+ txt_step2_from.getText().toString()
                                                +" telah diterima oleh "+ sharedpreferences.getString("name", null)
                                                + " dengan rincian : "+txt_receipt_notes.getText().toString();
                                        msg.setText(success_msg);

                                        LinearLayout step3 = (LinearLayout) findViewById(R.id.step3);
                                        step3.setVisibility(View.VISIBLE);
                                        LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                                        step2.setVisibility(View.GONE);
                                        LinearLayout step1 = (LinearLayout) findViewById(R.id.step1);
                                        step1.setVisibility(View.GONE);

                                        Toast.makeText(getApplicationContext(),
                                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
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

    public ArrayList set_auto_complete() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("status", "onprocess");

        final ArrayList<String> items = new ArrayList<String>();

        _string_request(Request.Method.GET, issue_list_url, params,
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
                                JSONArray data = jObj.getJSONArray("data");

                                for(int n = 0; n < data.length(); n++)
                                {
                                    items.add(data.getString(n));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        return items;
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

    public void _string_request(int method, String url, final Map params, final VolleyCallback callback) {
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
                hideDialog();
            }
        })
        {
            // set headers
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }

    public interface VolleyCallback{
        void onSuccess(String result);
    }

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void select_receipt_type(final Spinner select, Context ini) {
        select.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                String type = select.getSelectedItem().toString();
                Log.e("Selected item : ", type);
                TextView txt_step2_label1 = (TextView) findViewById(R.id.txt_step2_label1);
                EditText txt_receipt_notes = (EditText) findViewById(R.id.txt_receipt_notes);
                Button btn_confirm = (Button) findViewById(R.id.btn_confirm);
                if (type.equals("Bandara") || type.equals("Ekspedisi") || type.equals("Lainnya")) {
                    txt_step2_label1.setVisibility(View.VISIBLE);
                    txt_receipt_notes.setVisibility(View.VISIBLE);
                    btn_confirm.setVisibility(View.VISIBLE);
                } else {
                    txt_step2_label1.setVisibility(View.GONE);
                    txt_receipt_notes.setVisibility(View.GONE);
                    btn_confirm.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }
}
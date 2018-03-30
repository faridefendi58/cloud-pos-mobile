package com.slightsite.ucokinventory;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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

/**
 * Created by mrsvette on 26/03/18.
 */

public class StockActivity extends MainActivity {
    Intent intent;
    ProgressDialog pDialog;
    int success;

    private static final String TAG = StockActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_stock);
        buildMenu();

        // build spinner wh list
        Spinner step1_wh_list = (Spinner)findViewById(R.id.step1_wh_list);
        ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, get_list_warehouse());
        whAdapter.notifyDataSetChanged();
        step1_wh_list.setAdapter(whAdapter);

        final Context ini = this;
        Button btn_next = (Button) findViewById(R.id.btn_next);
        trigger_btn_step1(btn_next, ini);
        Button btn_back = (Button) findViewById(R.id.btn_back);
        trigger_btn_back(btn_back, ini);
    }

    @Override
    public void onBackPressed() {
        intent = new Intent(StockActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final StockActivity.VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Request data ...");
            showDialog();
        }

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
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
                if (show_dialog) {
                    hideDialog();
                }
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

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    private ArrayList get_list_warehouse() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "warehouse/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new StockActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                Log.e(TAG, "Response: " + data.toString());
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }

    private void trigger_btn_step1(Button btn, final Context ini) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide step1
                LinearLayout step1 = (LinearLayout) findViewById(R.id.step1);
                step1.setVisibility(View.GONE);
                // show step2
                LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                step2.setVisibility(View.VISIBLE);
                // set the header label
                Spinner step1_wh_list = (Spinner)findViewById(R.id.step1_wh_list);
                TextView textView2 = (TextView) findViewById(R.id.TextView2);
                textView2.setText(step1_wh_list.getSelectedItem().toString());
                // show the list
                set_list_stock(ini);
            }
        });
    }

    private void trigger_btn_back(Button btn, final Context ini) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide step1
                LinearLayout step1 = (LinearLayout) findViewById(R.id.step1);
                step1.setVisibility(View.VISIBLE);
                // show step2
                LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                step2.setVisibility(View.GONE);
            }
        });
    }

    private ArrayList set_list_stock(final Context ini) {
        Spinner step1_wh_list = (Spinner)findViewById(R.id.step1_wh_list);
        String wh_name = step1_wh_list.getSelectedItem().toString();
        Map<String, String> params = new HashMap<String, String>();
        params.put("warehouse_name", wh_name);
        //params.put("warehouse_id", "1");
        Log.e(TAG, "Response: " + params.toString());

        final ArrayList<String> items = new ArrayList<String>();

        String stock_url = Server.URL + "stock/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, stock_url, params, false,
                new StockActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                Log.e(TAG, "Response: " + data.toString());
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    String item = data_n.getString("product_name")+" : "
                                            + data_n.getString("quantity") + " " + data_n.getString("unit");
                                    items.add( item );
                                }
                                ArrayAdapter adapter = new ArrayAdapter<String>(ini,
                                        R.layout.activity_list_view, items);

                                ListView listView = (ListView) findViewById(R.id.list);
                                listView.setAdapter(adapter);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }
}

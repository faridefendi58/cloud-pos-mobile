package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
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
 * Created by mrsvette on 28/03/18.
 */

public class TransferActivity extends MainActivity {
    ProgressDialog pDialog;
    int success;

    private static final String TAG = TransferActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    ArrayList list_products;
    Map<String, String> list_items = new HashMap<String, String>();
    Map<String, String> product_names = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_transfer);
        buildMenu();

        // build spinner wh list
        Spinner wh_list_from = (Spinner)findViewById(R.id.wh_list_from);
        ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, get_list_warehouse());
        whAdapter.notifyDataSetChanged();
        wh_list_from.setAdapter(whAdapter);

        // build spinner wh list
        Spinner wh_list_to = (Spinner)findViewById(R.id.wh_list_to);
        whAdapter.notifyDataSetChanged();
        wh_list_to.setAdapter(whAdapter);

        final FrameLayout btn_add_container = (FrameLayout) findViewById(R.id.btn_add_container);
        wh_list_from.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!adapterView.getSelectedItem().toString().equals("-"))
                    btn_add_container.setVisibility(View.VISIBLE);
                else
                    btn_add_container.setVisibility(View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                btn_add_container.setVisibility(View.GONE);
            }
        });

        final Context ini = this;
        // define the product list
        list_products = get_list_product();
        btn_add_trigger(ini);
    }

    private void btn_add_trigger(final Context ini) {
        Button btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);

                final Spinner list_product = (Spinner) mView.findViewById(R.id.list_product);
                ArrayAdapter<String> productAdapter = new ArrayAdapter<String>(mView.getContext(), android.R.layout.simple_spinner_item, list_products);
                list_product.setAdapter(productAdapter);

                builder.setView(mView);
                final AlertDialog dialog = builder.create();

                Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
                btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.cancel();
                    }
                });

                final EditText txt_qty = (EditText) mView.findViewById(R.id.txt_qty);

                Button btn_dialog_submit = (Button) mView.findViewById(R.id.btn_dialog_submit);
                btn_dialog_submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int has_error = 0;
                        if (list_product.getSelectedItem().toString().length() <= 0) {
                            has_error = has_error + 1;
                            Toast.makeText(getApplicationContext(), "Produk tidak boleh dikosongi.", Toast.LENGTH_LONG).show();
                        }
                        if (txt_qty.getText().toString().length() <= 0) {
                            has_error = has_error + 1;
                            Toast.makeText(getApplicationContext(), "Jumlah barang tidak boleh dikosongi.", Toast.LENGTH_LONG).show();
                        } else {
                            boolean digitsOnly = TextUtils.isDigitsOnly(txt_qty.getText().toString());
                            if (digitsOnly) {
                                int tot_qty_val = Integer.parseInt(txt_qty.getText().toString());
                                if (tot_qty_val <= 0) {
                                    has_error = has_error + 1;
                                    Toast.makeText(getApplicationContext(), "Jumlah barang harus lebih dari 0.", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                has_error = has_error + 1;
                                txt_qty.setText("");
                                Toast.makeText(getApplicationContext(), "Jumlah barang harus dalam format angka.", Toast.LENGTH_LONG).show();
                            }
                        }
                        if (has_error == 0) {
                            list_items.put(list_product.getSelectedItem().toString(), txt_qty.getText().toString());
                            //Log.e(TAG, "Product list: " + product_names.toString());
                            Toast.makeText(getApplicationContext(), "Cart: " +list_items.toString(), Toast.LENGTH_LONG).show();
                            dialog.hide();
                            // show the added item
                            Iterator<Map.Entry<String, String>> iterator = list_items.entrySet().iterator();
                            ArrayList<String> arr_list_items = new ArrayList<String>();
                            Integer i = 0;
                            String product_stack_str = "";
                            String list_item_str = "";
                            while(iterator.hasNext())
                            {
                                Map.Entry<String, String> pair = iterator.next();
                                String r_label = pair.getKey()+" "+pair.getValue();
                                if (i > 0) {
                                    product_stack_str += "-" + product_names.get(pair.getKey()) + "," + pair.getValue();
                                    list_item_str += ", " + r_label;
                                } else {
                                    product_stack_str += product_names.get(pair.getKey()) + "," + pair.getValue();
                                    list_item_str += r_label;
                                }
                                arr_list_items.add(r_label);
                                i ++;
                            }

                            ArrayAdapter adapter2 = new ArrayAdapter<String>(ini, R.layout.activity_list_view, arr_list_items);

                            ListView listView = (ListView) findViewById(R.id.list_item);
                            listView.setAdapter(adapter2);

                            TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);
                            txt_item_select.setText(product_stack_str);

                            TextView txt_item_select_str = (TextView) findViewById(R.id.txt_item_select_str);
                            txt_item_select_str.setText(list_item_str);
                        }
                    }
                });

                dialog.show();
            }
        });
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private ArrayList get_list_product() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "product/list?api-key=" + Server.API_KEY;
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
                                    product_names.put(data_n.getString("title"), data_n.getString("id"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }
}

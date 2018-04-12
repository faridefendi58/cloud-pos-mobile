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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TabHost;
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

public class PurchaseActivity extends MainActivity {
    ProgressDialog pDialog;
    int success;

    private static final String TAG = PurchaseActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    ArrayList list_products;
    Map<String, String> list_items = new HashMap<String, String>();
    Map<String, String> list_prices = new HashMap<String, String>();
    ArrayList<String> list_product_items = new ArrayList<String>();
    Map<String, String> product_names = new HashMap<String, String>();
    Map<String, String> product_units = new HashMap<String, String>();
    ArrayList<String> assigned_whs = new ArrayList<String>();
    ArrayList<String> group_whs = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_purchase);
        buildMenu();

        // build spinner supplier list
        Spinner supplier_name = (Spinner)findViewById(R.id.supplier_name);
        ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, get_list_supplier());
        supplier_name.setAdapter(whAdapter);

        // build spinner shipment list
        Spinner shipment_name = (Spinner)findViewById(R.id.shipment_name);
        ArrayAdapter<String> whAdapter2 = new ArrayAdapter<String>(this, R.layout.spinner_item, get_list_shipment());
        shipment_name.setAdapter(whAdapter2);

        // define the roles
        set_list_assigned_wh();

        // build spinner of wh coverage
        Spinner wh_group_name = (Spinner)findViewById(R.id.wh_group_name);
        ArrayAdapter<String> whAdapter3 = new ArrayAdapter<String>(this, R.layout.spinner_item, group_whs);
        wh_group_name.setAdapter(whAdapter3);

        final FrameLayout btn_add_container = (FrameLayout) findViewById(R.id.btn_add_container);
        supplier_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        // copy btn trigger
        Button btn_copy = (Button) findViewById(R.id.btn_copy);
        btn_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txt_success_message = (TextView) findViewById(R.id.txt_success_message);
                setClipboard(ini, txt_success_message.getText().toString());
                Toast.makeText(getApplicationContext(),"Pesan berhasil disalin.", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
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

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }

    private ArrayList get_list_supplier() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "supplier/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of supplier request : " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                Log.e(TAG, "Supplier List : " + data.toString());
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

    private ArrayList get_list_shipment() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "shipment/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of shipment request : " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                Log.e(TAG, "Shipment List : " + data.toString());
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

    private ArrayList get_list_product() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "product/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of product request : " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                Log.e(TAG, "List Product : " + data.toString());
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                    list_product_items.add(data_n.getString("title"));
                                    product_names.put(data_n.getString("title"), data_n.getString("id"));
                                    product_units.put(data_n.getString("title"), data_n.getString("unit"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }

    private void btn_add_trigger(final Context ini) {
        Button btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_purchase, null);

                final Spinner list_product = (Spinner) mView.findViewById(R.id.list_product);
                ArrayAdapter<String> productAdapter = new ArrayAdapter<String>(mView.getContext(), R.layout.spinner_item, list_product_items);
                list_product.setAdapter(productAdapter);

                builder.setView(mView);
                final AlertDialog dialog = builder.create();

                // submit, cancel, and delete button trigger
                trigger_dialog_button(mView, ini, list_product, dialog);

                dialog.show();
            }
        });
    }

    private void trigger_dialog_button(final View mView, final Context ini, final Spinner list_product, final AlertDialog dialog) {
        // cancel method
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        final EditText txt_qty = (EditText) mView.findViewById(R.id.txt_qty);
        final EditText txt_price = (EditText) mView.findViewById(R.id.txt_price);

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
                // validation for price form
                if (txt_price.getText().toString().length() > 0) {
                    boolean pdigitsOnly = TextUtils.isDigitsOnly(txt_price.getText().toString());
                    if (pdigitsOnly) {
                        int tot_price_val = Integer.parseInt(txt_price.getText().toString());
                        if (tot_price_val <= 0) {
                            has_error = has_error + 1;
                            Toast.makeText(getApplicationContext(), "Harga barang harus lebih dari 0.", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        has_error = has_error + 1;
                        txt_price.setText("");
                        Toast.makeText(getApplicationContext(), "Harga barang harus dalam format angka.", Toast.LENGTH_LONG).show();
                    }
                }
                if (has_error == 0) {
                    list_items.put(list_product.getSelectedItem().toString(), txt_qty.getText().toString());
                    if (txt_price.getText().toString().length() > 0) {
                        list_prices.put(list_product.getSelectedItem().toString(), txt_price.getText().toString());
                    }
                    Toast.makeText(getApplicationContext(), "Berhasil menambahkan " + list_product.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "List price : " + list_prices.toString());
                    dialog.hide();
                    // show the added item
                    Iterator<Map.Entry<String, String>> iterator = list_items.entrySet().iterator();
                    ArrayList<String> arr_list_items = new ArrayList<String>();
                    Integer i = 0;
                    String product_stack_str = "";
                    String list_item_str = "";
                    String price_stack_str = "";
                    String list_price_str = "";
                    while(iterator.hasNext())
                    {
                        Map.Entry<String, String> pair = iterator.next();
                        String r_label = pair.getKey() + " " + pair.getValue() + " " + product_units.get(pair.getKey());
                        String s_label = "";
                        if (list_prices.containsKey(pair.getKey()) && list_prices.get(pair.getKey()).length() > 0) {
                            r_label += " @" + list_prices.get(pair.getKey());
                            s_label += pair.getKey() + " @" + list_prices.get(pair.getKey());
                        }
                        if (i > 0) {
                            product_stack_str += "-" + product_names.get(pair.getKey()) + "," + pair.getValue();
                            list_item_str += ", " + r_label;
                            if (s_label.length() > 0) {
                                price_stack_str += "-" + product_names.get(pair.getKey()) + "," + list_prices.get(pair.getKey());
                                list_price_str += ", " + s_label;
                            }
                        } else {
                            product_stack_str += product_names.get(pair.getKey()) + "," + pair.getValue();
                            list_item_str += r_label;
                            if (s_label.length() > 0) {
                                price_stack_str += product_names.get(pair.getKey()) + "," + list_prices.get(pair.getKey());
                                list_price_str += s_label;
                            }
                        }
                        arr_list_items.add(r_label);
                        i ++;
                    }

                    ArrayAdapter adapter2 = new ArrayAdapter<String>(ini, R.layout.activity_list_view, arr_list_items);

                    ListView listView = (ListView) findViewById(R.id.list_item);
                    listView.setAdapter(adapter2);

                    // and then set the list event for update and deletion
                    set_list_item_trigger(listView, ini);

                    TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);
                    txt_item_select.setText(product_stack_str);

                    TextView txt_item_select_str = (TextView) findViewById(R.id.txt_item_select_str);
                    txt_item_select_str.setText(list_item_str);

                    if (price_stack_str.length() > 0) {
                        TextView txt_price_select = (TextView) findViewById(R.id.txt_price_select);
                        txt_price_select.setText(price_stack_str);
                    }

                    if (list_price_str.length() > 0) {
                        TextView txt_price_select_str = (TextView) findViewById(R.id.txt_price_select_str);
                        txt_price_select_str.setText(list_price_str);
                    }

                    Log.e(TAG, "Price stack : " + price_stack_str);
                    Log.e(TAG, "List price : " + list_price_str);

                    Button btn_submit = (Button) findViewById(R.id.btn_submit);
                    btn_submit.setVisibility(View.VISIBLE);
                    btn_submit_trigger(btn_submit, ini);
                }
            }
        });

        // action of delete button
        Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);
        btn_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView stack_id = (TextView) mView.findViewById(R.id.stack_id);
                if (list_items.containsKey(stack_id.getText().toString())) {
                    list_items.remove(stack_id.getText().toString());
                }
                Log.e(TAG, "Cart stack : " + list_items.toString());
                Log.e(TAG, "Stack would be deleted : " + stack_id.getText().toString());

                Iterator<Map.Entry<String, String>> iterator = list_items.entrySet().iterator();
                ArrayList<String> arr_list_items = new ArrayList<String>();
                Integer i = 0;
                String product_stack_str = "";
                String list_item_str = "";
                String price_stack_str = "";
                String list_price_str = "";
                while(iterator.hasNext())
                {
                    Map.Entry<String, String> pair = iterator.next();
                    String r_label = pair.getKey() + " " + pair.getValue() + " " + product_units.get(pair.getKey());
                    String s_label = "";
                    if (list_prices.containsKey(pair.getKey()) && list_prices.get(pair.getKey()).length() > 0) {
                        r_label += " @" + list_prices.get(pair.getKey());
                        s_label += pair.getKey() + " @" + list_prices.get(pair.getKey());
                    }
                    if (i > 0) {
                        product_stack_str += "-" + product_names.get(pair.getKey()) + "," + pair.getValue();
                        list_item_str += ", " + r_label;
                        if (s_label.length() > 0) {
                            price_stack_str += "-" + product_names.get(pair.getKey()) + "," + list_prices.get(pair.getKey());
                            list_price_str += ", " + s_label;
                        }
                    } else {
                        product_stack_str += product_names.get(pair.getKey()) + "," + pair.getValue();
                        list_item_str += r_label;
                        if (s_label.length() > 0) {
                            price_stack_str += product_names.get(pair.getKey()) + "," + list_prices.get(pair.getKey());
                            list_price_str += s_label;
                        }
                    }
                    arr_list_items.add(r_label);
                    i ++;
                }

                ArrayAdapter adapter2 = new ArrayAdapter<String>(ini, R.layout.activity_list_view, arr_list_items);

                ListView listView = (ListView) findViewById(R.id.list_item);
                listView.setAdapter(adapter2);

                // and then set the list event for update and deletion
                set_list_item_trigger(listView, ini);

                TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);
                txt_item_select.setText(product_stack_str);

                TextView txt_item_select_str = (TextView) findViewById(R.id.txt_item_select_str);
                txt_item_select_str.setText(list_item_str);

                if (price_stack_str.length() > 0) {
                    TextView txt_price_select = (TextView) findViewById(R.id.txt_price_select);
                    txt_price_select.setText(price_stack_str);
                }

                if (list_price_str.length() > 0) {
                    TextView txt_price_select_str = (TextView) findViewById(R.id.txt_price_select_str);
                    txt_price_select_str.setText(list_price_str);
                }

                dialog.hide();
            }
        });
    }

    private void set_list_item_trigger(final ListView list, final Context ini) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String title = list.getItemAtPosition(i).toString();
                Log.e(TAG, "List items : " + list_items.toString());
                Iterator<Map.Entry<String, String>> iterator = list_items.entrySet().iterator();
                Integer j = 0;
                String current_val = "";
                String current_key = "";
                String current_price = "";
                while(iterator.hasNext())
                {
                    Map.Entry<String, String> pair = iterator.next();
                    if (j.equals(i)) {
                        String p_id = product_names.get(pair.getKey());
                        current_val = pair.getValue();
                        current_key = pair.getKey();
                        if (list_prices.containsKey(pair.getKey())) {
                            current_price = list_prices.get(pair.getKey());
                        }
                    }
                    j ++;
                }
                Log.e(TAG, "Current key :" + current_key);

                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_purchase, null);

                final Spinner list_product = (Spinner) mView.findViewById(R.id.list_product);
                ArrayAdapter<String> productAdapter = new ArrayAdapter<String>(mView.getContext(), R.layout.spinner_item, list_product_items);
                list_product.setAdapter(productAdapter);
                Log.e(TAG, "List product items : " + list_product_items.toString());
                Integer index_p_items = list_product_items.indexOf(current_key);
                list_product.setSelection(index_p_items);

                TextView txt_qty = (TextView) mView.findViewById(R.id.txt_qty);
                txt_qty.setText(current_val);

                TextView stack_id = (TextView) mView.findViewById(R.id.stack_id);
                stack_id.setText(current_key);

                TextView txt_price = (TextView) mView.findViewById(R.id.txt_price);
                txt_price.setText(current_price);

                builder.setView(mView);
                final AlertDialog dialog = builder.create();

                // submit, cancel, and delete button trigger
                trigger_dialog_button(mView, ini, list_product, dialog);

                // show button delete
                Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);
                btn_dialog_delete.setVisibility(View.VISIBLE);
                Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
                btn_dialog_cancel.setVisibility(View.GONE);

                dialog.show();
            }
        });
    }

    private void btn_submit_trigger(final Button btn, Context ini) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Spinner supplier_name = (Spinner) findViewById(R.id.supplier_name);
                final Spinner shipment_name = (Spinner) findViewById(R.id.shipment_name);
                final Spinner wh_group_name = (Spinner) findViewById(R.id.wh_group_name);
                TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);
                TextView txt_price_select = (TextView) findViewById(R.id.txt_price_select);

                Map<String, String> params = new HashMap<String, String>();
                params.put("items", txt_item_select.getText().toString());
                params.put("prices", txt_price_select.getText().toString());
                params.put("supplier_name", supplier_name.getSelectedItem().toString());
                params.put("shipment_name", shipment_name.getSelectedItem().toString());
                params.put("wh_group_name", wh_group_name.getSelectedItem().toString());
                params.put("admin_id", sharedpreferences.getString("id", null));
                Log.e(TAG, "Params : " + params.toString());

                String transfer_url = Server.URL + "purchase/create?api-key=" + Server.API_KEY;
                _string_request(
                        Request.Method.POST,
                        transfer_url,
                        params,
                        true,
                        new VolleyCallback(){
                            @Override
                            public void onSuccess(String result) {
                                Log.e(TAG, "Response of purchase api : " + result.toString());
                                hideDialog();
                                try {
                                    JSONObject jObj = new JSONObject(result);
                                    success = jObj.getInt(TAG_SUCCESS);
                                    if (success == 1) {
                                        String issue_number = jObj.getString("issue_number");
                                        String success_msg = "Pengadaan barang dengan kode " + issue_number + " dari Supplier "+ supplier_name.getSelectedItem().toString()
                                                +" telah di-request oleh "+ sharedpreferences.getString("name", null);

                                        if (shipment_name.getSelectedItem().toString().length() > 0) {
                                            success_msg += " dan akan dikirim melalui " + shipment_name.getSelectedItem().toString();
                                        }

                                        TextView txt_item_select_str = (TextView) findViewById(R.id.txt_item_select_str);
                                        success_msg += " dengan rincian : " + txt_item_select_str.getText().toString();

                                        TextView msg = (TextView) findViewById(R.id.txt_success_message);
                                        msg.setText(success_msg);
                                        msg.setVisibility(View.VISIBLE);

                                        // hide the other
                                        LinearLayout step1 = (LinearLayout) findViewById(R.id.step1);
                                        step1.setVisibility(View.GONE);

                                        LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                                        step2.setVisibility(View.VISIBLE);

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

    private void set_list_assigned_wh() {

        String roles = sharedpreferences.getString(TAG_ROLES, null);
        try {
            JSONObject jsonObject = new JSONObject(roles);
            JSONArray keys = jsonObject.names();

            for (int i = 0; i < keys.length (); ++i) {
                String key = keys.getString (i); // Here's your key
                String value = jsonObject.getString (key); // Here's your value
                JSONObject data_n = jsonObject.getJSONObject(key);
                assigned_whs.add(data_n.getString("warehouse_name"));
                if (!group_whs.contains(data_n.getString("warehouse_group_name"))) {
                    group_whs.add(data_n.getString("warehouse_group_name"));
                }

            }
            Log.e(TAG, "List Group WH : " + group_whs.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

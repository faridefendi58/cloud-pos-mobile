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
    ArrayList<String> list_product_items = new ArrayList<String>();
    Map<String, String> product_names = new HashMap<String, String>();
    Map<String, String> product_units = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_transfer);
        buildMenu();

        // build spinner wh list
        Spinner wh_list_from = (Spinner)findViewById(R.id.wh_list_from);
        ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, get_list_warehouse());
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

    private void btn_add_trigger(final Context ini) {
        Button btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item, null);

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
                    Toast.makeText(getApplicationContext(), "Berhasil menambahkan " + list_product.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
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
                        String r_label = pair.getKey() + " " + pair.getValue() + " " + product_units.get(pair.getKey());
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

                    // and then set the list event for update and deletion
                    set_list_item_trigger(listView, ini);

                    TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);
                    txt_item_select.setText(product_stack_str);

                    TextView txt_item_select_str = (TextView) findViewById(R.id.txt_item_select_str);
                    txt_item_select_str.setText(list_item_str);

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
                while(iterator.hasNext())
                {
                    Map.Entry<String, String> pair = iterator.next();
                    String r_label = pair.getKey() + " " + pair.getValue() + " " + product_units.get(pair.getKey());
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

                // and then set the list event for update and deletion
                set_list_item_trigger(listView, ini);

                TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);
                txt_item_select.setText(product_stack_str);

                TextView txt_item_select_str = (TextView) findViewById(R.id.txt_item_select_str);
                txt_item_select_str.setText(list_item_str);

                dialog.hide();
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
                new VolleyCallback() {
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

    private void btn_submit_trigger(final Button btn, Context ini) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Spinner wh_list_from = (Spinner) findViewById(R.id.wh_list_from);
                final Spinner wh_list_to = (Spinner) findViewById(R.id.wh_list_to);
                TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);

                Map<String, String> params = new HashMap<String, String>();
                params.put("items", txt_item_select.getText().toString());
                params.put("warehouse_from_name", wh_list_from.getSelectedItem().toString());
                params.put("warehouse_to_name", wh_list_to.getSelectedItem().toString());
                params.put("admin_id", sharedpreferences.getString("id", null));
                Log.e(TAG, params.toString());

                String transfer_url = Server.URL + "transfer/create?api-key=" + Server.API_KEY;
                _string_request(
                        Request.Method.POST,
                        transfer_url,
                        params,
                        true,
                        new VolleyCallback(){
                            @Override
                            public void onSuccess(String result) {
                                Log.e(TAG, "Response: " + result.toString());
                                hideDialog();
                                try {
                                    JSONObject jObj = new JSONObject(result);
                                    success = jObj.getInt(TAG_SUCCESS);
                                    if (success == 1) {
                                        String issue_number = jObj.getString("issue_number");
                                        String success_msg = "Perpindahan stok dengan kode " + issue_number + " dari Warehouse "+ wh_list_from.getSelectedItem().toString()
                                                +" telah dikirim oleh "+ sharedpreferences.getString("name", null);

                                        if (wh_list_to.getSelectedItem().toString().length() > 0) {
                                            success_msg += " ke Warehouse " + wh_list_to.getSelectedItem().toString();
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
                while(iterator.hasNext())
                {
                    Map.Entry<String, String> pair = iterator.next();
                    if (j.equals(i)) {
                        String p_id = product_names.get(pair.getKey());
                        current_val = pair.getValue();
                        current_key = pair.getKey();
                    }
                    j ++;
                }
                Log.e(TAG, "Current key :" + current_key);

                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_receipt, null);

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

    private void setClipboard(Context context, String text) {
        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", text);
        clipboard.setPrimaryClip(clip);
    }
}

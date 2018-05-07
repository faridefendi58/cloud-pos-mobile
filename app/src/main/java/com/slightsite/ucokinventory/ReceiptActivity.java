package com.slightsite.ucokinventory;

/**
 * Created by mrsvette on 13/03/18.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import org.w3c.dom.Text;

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

    final ArrayList<String> list_items = new ArrayList<String>();
    final ArrayList<String> list_product_items = new ArrayList<String>();
    final ArrayList<String> list_issues = new ArrayList<String>();
    final ArrayList<String> list_ids = new ArrayList<String>();
    final Map<String, String> product_ids = new HashMap<String, String>();
    final Map<String, String> product_units = new HashMap<String, String>();
    final Map<String, String> issue_origins = new HashMap<String, String>();

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_receipt);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // If needed build the advance search
        //buildAutoComplete();

        buildTheIssueList();
        buildTheReceiptList();
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
                        true,
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
                                        Spinner step2_receipt_type = (Spinner) findViewById(R.id.step2_receipt_type);
                                        String r_type = step2_receipt_type.getSelectedItem().toString();
                                        String success_msg = "Nomor pengadaan "+ txt_step2_issue_no.getText().toString()
                                                +" dari "+ txt_step2_from.getText().toString()
                                                +" telah diterima oleh "+ sharedpreferences.getString("name", null);

                                        if (!r_type.equals("Lainnya")) {
                                            success_msg += " di " + r_type;
                                        }
                                        success_msg += " dengan rincian : "+txt_receipt_notes.getText().toString();
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

        _string_request(Request.Method.GET, issue_list_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response: " + result.toString());
                        //hideDialog();
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
                Spinner step2_receipt_wh = (Spinner) findViewById(R.id.step2_receipt_wh);
                TextView txt_step2_label_wh = (TextView) findViewById(R.id.txt_step2_label_wh);
                if (type.equals("Bandara") || type.equals("Ekspedisi") || type.equals("Lainnya")) {
                    txt_step2_label1.setVisibility(View.VISIBLE);
                    txt_receipt_notes.setVisibility(View.VISIBLE);
                    btn_confirm.setVisibility(View.VISIBLE);
                    txt_receipt_notes.requestFocus();
                    step2_receipt_wh.setVisibility(View.GONE);
                    txt_step2_label_wh.setVisibility(View.GONE);
                    ListView listView = (ListView) findViewById(R.id.list_receipts);
                    listView.setVisibility(View.GONE);
                    TextView txt_step2_label_receipts = (TextView) findViewById(R.id.txt_step2_label_receipts);
                    txt_step2_label_receipts.setVisibility(View.GONE);
                    Button btn_confirm_receipt = (Button) findViewById(R.id.btn_confirm_receipt);
                    btn_confirm_receipt.setVisibility(View.GONE);
                    // also hide add item button
                    FrameLayout btn_add_container = (FrameLayout) findViewById(R.id.btn_add_container);
                    btn_add_container.setVisibility(View.GONE);
                } else {
                    txt_step2_label1.setVisibility(View.GONE);
                    txt_receipt_notes.setVisibility(View.GONE);
                    btn_confirm.setVisibility(View.GONE);
                    step2_receipt_wh.setVisibility(View.VISIBLE);
                    txt_step2_label_wh.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });
    }

    String product_id;
    Map<String, String> product_stack = new HashMap<String, String>();
    Map<String, String> product_names = new HashMap<String, String>();
    TextView current_view;
    int max_quantity = 0;

    private void setListEvent(final ListView listView, final Context ini, final JSONArray items_data) {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String title = listView.getItemAtPosition(position).toString();
                try {
                    JSONObject json_obj_n = items_data.getJSONObject(position);
                    //title = json_obj_n.getString("product_name").toString();
                    product_id = json_obj_n.getString("product_id").toString();
                    product_names.put(product_id, json_obj_n.getString("product_name").toString());
                    current_view = (TextView) view;
                    max_quantity = Integer.parseInt(json_obj_n.getString("quantity").toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                builder.setTitle(title);

                final EditText input = new EditText(ini);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setHint("Jumlah yg diterima ?");

                builder.setView(input);

                builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String m_Text = input.getText().toString();
                        String product_stack_str = "";
                        //push to stack
                        int i_text = Integer.parseInt(m_Text);
                        if (i_text > 0) {
                            if (max_quantity > 0 && i_text > max_quantity) {
                                m_Text = ""+ max_quantity;
                                Toast.makeText(getBaseContext(),"Quantity maksimum "+ m_Text,Toast.LENGTH_SHORT).show();
                            }
                            product_stack.put(product_id, m_Text);
                        } else {
                            if (product_stack.containsKey(product_id)) {
                                product_stack.remove(product_id);
                            }
                        }
                        Iterator<Map.Entry<String, String>> iterator = product_stack.entrySet().iterator();
                        Integer i = 0;
                        String list_receipt_str = "";
                        ArrayList<String> list_receipts = new ArrayList<String>();
                        while(iterator.hasNext())
                        {
                            Map.Entry<String, String> pair = iterator.next();
                            String r_label = product_names.get(pair.getKey())+" "+pair.getValue()+" "+product_units.get(pair.getKey());
                            if (i > 0) {
                                product_stack_str += "-" + pair.getKey() + "," + pair.getValue();
                                list_receipt_str += ", " + r_label;
                            } else {
                                product_stack_str += pair.getKey() + "," + pair.getValue();
                                list_receipt_str += r_label;
                            }
                            list_receipts.add(r_label);
                            i ++;
                        }

                        TextView txt_step2_item_select = (TextView) findViewById(R.id.txt_step2_item_select);
                        txt_step2_item_select.setText(product_stack_str);

                        // list in string
                        TextView txt_step2_item_select_str = (TextView) findViewById(R.id.txt_step2_item_select_str);
                        txt_step2_item_select_str.setText(list_receipt_str);

                        //set the list again
                        ArrayAdapter adapter2 = new ArrayAdapter<String>(ini,
                                R.layout.activity_list_view, list_receipts);

                        ListView listView = (ListView) findViewById(R.id.list_receipts);
                        listView.setAdapter(adapter2);
                        listView.setVisibility(View.VISIBLE);
                        // also show the label
                        TextView txt_step2_label_receipts = (TextView) findViewById(R.id.txt_step2_label_receipts);
                        txt_step2_label_receipts.setVisibility(View.VISIBLE);
                        Button btn_confirm_receipt = (Button) findViewById(R.id.btn_confirm_receipt);
                        btn_confirm_receipt.setVisibility(View.VISIBLE);
                    }
                });
                builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    public ArrayList get_list_warehouse() {
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

    private void btn_confirm_receipt_trigger(Button btn, Context ini) {
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView txt_step2_item_select = (TextView) findViewById(R.id.txt_step2_item_select);
                TextView txt_step2_issue_no = (TextView) findViewById(R.id.txt_step2_issue_no);
                TextView txt_step2_type = (TextView) findViewById(R.id.txt_step2_type);
                Spinner step2_receipt_wh = (Spinner) findViewById(R.id.step2_receipt_wh);
                Map<String, String> params = new HashMap<String, String>();
                params.put("items", txt_step2_item_select.getText().toString());
                params.put("issue_number", txt_step2_issue_no.getText().toString());
                params.put("type", txt_step2_type.getText().toString());
                params.put("warehouse_name", step2_receipt_wh.getSelectedItem().toString());
                params.put("admin_id", sharedpreferences.getString("id", null));
                Log.e(TAG, params.toString());
                String confirm_url = Server.URL + "receipt/confirm?api-key=" + Server.API_KEY;
                _string_request(
                        Request.Method.POST,
                        confirm_url,
                        params,
                        true,
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
                                        //EditText txt_receipt_notes = (EditText) findViewById(R.id.txt_receipt_notes);
                                        TextView txt_step2_from = (TextView) findViewById(R.id.txt_step2_from);
                                        Spinner step2_receipt_type = (Spinner) findViewById(R.id.step2_receipt_type);
                                        Spinner step2_wh = (Spinner) findViewById(R.id.step2_receipt_wh);
                                        String r_type = step2_receipt_type.getSelectedItem().toString();
                                        String r_wh = step2_wh.getSelectedItem().toString();
                                        String success_msg = "Nomor pengadaan "+ txt_step2_issue_no.getText().toString()
                                                +" dari "+ txt_step2_from.getText().toString()
                                                +" telah diterima oleh "+ sharedpreferences.getString("name", null)
                                                +" di warehouse "+r_wh;

                                        TextView txt_step2_item_select_str = (TextView) findViewById(R.id.txt_step2_item_select_str);
                                        success_msg += " dengan rincian : "+txt_step2_item_select_str.getText().toString();

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

    private void spinner_receipt_wh_trigger(Spinner spinner, Context ini) {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            final FrameLayout btn_add_container = (FrameLayout) findViewById(R.id.btn_add_container);
            final FrameLayout txt_item_container = (FrameLayout) findViewById(R.id.txt_item_container);
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!adapterView.getSelectedItem().toString().equals("-")) {
                    btn_add_container.setVisibility(View.VISIBLE);
                } else {
                    btn_add_container.setVisibility(View.GONE);
                }
                //txt_item_container.setVisibility(View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                btn_add_container.setVisibility(View.GONE);
            }
        });
    }

    private void btn_add_trigger(final Context ini) {
        Button btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide the detail first
                /*FrameLayout txt_item_container = (FrameLayout) findViewById(R.id.txt_item_container);
                txt_item_container.setVisibility(View.GONE);
                TextView show_items = (TextView) findViewById(R.id.show_items);
                show_items.setVisibility(View.VISIBLE);*/

                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_receipt, null);

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

    /**
     * Dialog button actions
     * @param mView
     * @param ini
     * @param list_product
     * @param dialog
     */
    private void trigger_dialog_button(final View mView, final Context ini, final Spinner list_product, final AlertDialog dialog) {
        // cancel method
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        // Saving or submiting method
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
                    product_stack.put(list_product.getSelectedItem().toString(), txt_qty.getText().toString());
                    Toast.makeText(getApplicationContext(), "Berhasil menambahkan " + list_product.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                    dialog.hide();
                    // show the added item
                    Iterator<Map.Entry<String, String>> iterator = product_stack.entrySet().iterator();
                    ArrayList<String> arr_list_items = new ArrayList<String>();
                    Integer i = 0;
                    String product_stack_str = "";
                    String list_item_str = "";
                    Log.e(TAG, "Product ids : " + product_ids.toString());
                    while(iterator.hasNext())
                    {
                        Map.Entry<String, String> pair = iterator.next();
                        String p_id = product_ids.get(pair.getKey());
                        String r_label = pair.getKey() + " " + pair.getValue() + " " + product_units.get(p_id);
                        if (i > 0) {
                            product_stack_str += "-" + p_id + "," + pair.getValue();
                            list_item_str += ", " + r_label;
                        } else {
                            product_stack_str += p_id + "," + pair.getValue();
                            list_item_str += r_label;
                        }
                        arr_list_items.add(r_label);
                        i ++;
                    }

                    ArrayAdapter adapter2 = new ArrayAdapter<String>(ini, R.layout.activity_list_view, arr_list_items);

                    ListView list_receipts = (ListView) findViewById(R.id.list_receipts);
                    list_receipts.setAdapter(adapter2);
                    list_receipts.setVisibility(View.VISIBLE);

                    DeliveryActivity.updateListViewHeight(list_receipts, 10);

                    // Then show the labels
                    TextView txt_step2_label_receipts = (TextView) findViewById(R.id.txt_step2_label_receipts);
                    txt_step2_label_receipts.setVisibility(View.VISIBLE);

                    // and then set the list event for update and deletion
                    set_list_receipt_trigger(list_receipts, ini);

                    TextView txt_item_select = (TextView) findViewById(R.id.txt_step2_item_select);
                    txt_item_select.setText(product_stack_str);

                    TextView txt_item_select_str = (TextView) findViewById(R.id.txt_step2_item_select_str);
                    txt_item_select_str.setText(list_item_str);

                    Button btn_confirm_receipt = (Button) findViewById(R.id.btn_confirm_receipt);
                    btn_confirm_receipt.setVisibility(View.VISIBLE);
                }
            }
        });

        // action of delete button
        Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);
        btn_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView stack_id = (TextView) mView.findViewById(R.id.stack_id);
                if (product_stack.containsKey(stack_id.getText().toString())) {
                    product_stack.remove(stack_id.getText().toString());
                }
                Log.e(TAG, product_stack.toString());
                Log.e(TAG, stack_id.getText().toString());
                Iterator<Map.Entry<String, String>> iterator = product_stack.entrySet().iterator();
                ArrayList<String> arr_list_items = new ArrayList<String>();
                Integer i = 0;
                String product_stack_str = "";
                String list_item_str = "";
                while(iterator.hasNext())
                {
                    Map.Entry<String, String> pair = iterator.next();
                    String p_id = product_ids.get(pair.getKey());
                    String r_label = pair.getKey() + " " + pair.getValue() + " " + product_units.get(p_id);
                    if (i > 0) {
                        product_stack_str += "-" + p_id + "," + pair.getValue();
                        list_item_str += ", " + r_label;
                    } else {
                        product_stack_str += p_id + "," + pair.getValue();
                        list_item_str += r_label;
                    }
                    arr_list_items.add(r_label);
                    i ++;
                }

                ArrayAdapter adapter2 = new ArrayAdapter<String>(ini, R.layout.activity_list_view, arr_list_items);
                ListView list_receipts = (ListView) findViewById(R.id.list_receipts);
                list_receipts.setAdapter(adapter2);

                TextView txt_item_select = (TextView) findViewById(R.id.txt_step2_item_select);
                txt_item_select.setText(product_stack_str);

                TextView txt_item_select_str = (TextView) findViewById(R.id.txt_step2_item_select_str);
                txt_item_select_str.setText(list_item_str);

                dialog.hide();
            }
        });
    }

    private void set_list_receipt_trigger(final ListView list, final Context ini) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String title = list.getItemAtPosition(i).toString();
                Log.e(TAG, "Product stack : " + product_stack.toString());
                Iterator<Map.Entry<String, String>> iterator = product_stack.entrySet().iterator();
                Integer j = 0;
                Integer k = 0;
                String current_val = "";
                String current_key = "";
                while(iterator.hasNext())
                {
                    Map.Entry<String, String> pair = iterator.next();
                    if (j.equals(i)) {
                        String p_id = product_ids.get(pair.getKey());
                        k = i;
                        current_val = pair.getValue();
                        current_key = pair.getKey();
                    }
                    j ++;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_receipt, null);

                final Spinner list_product = (Spinner) mView.findViewById(R.id.list_product);
                ArrayAdapter<String> productAdapter = new ArrayAdapter<String>(mView.getContext(), android.R.layout.simple_spinner_item, list_product_items);
                list_product.setAdapter(productAdapter);
                //list_product.setSelection(k);
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

    private void buildAutoComplete() {
        //autocomplete
        ArrayList items = set_auto_complete();
        AutoCompleteTextView txt_issue_number = (AutoCompleteTextView)findViewById(R.id.txt_issue_no);
        ArrayAdapter adapter = new
                ArrayAdapter(this,android.R.layout.simple_list_item_1,items);

        txt_issue_number.setAdapter(adapter);
        txt_issue_number.setThreshold(1);
    }

    private void buildTheIssueList() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("status", "onprocess");
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);

        final ArrayList<String> descs = new ArrayList<String>();
        _string_request(Request.Method.GET, issue_list_url, params, true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of list issue : " + result.toString());
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                JSONObject origins = jObj.getJSONObject("origin");
                                JSONObject destinations = jObj.getJSONObject("destination");

                                for(int n = 0; n < data.length(); n++)
                                {
                                    list_ids.add(""+ n);
                                    list_issues.add(data.getString(n));
                                    issue_origins.put(data.getString(n), origins.getString(data.getString(n)));
                                    descs.add("Dari : " + origins.getString(data.getString(n)) + ", Tujuan : " + destinations.getString(data.getString(n)));
                                }
                                Log.e(TAG, "List available issue : " + list_issues.toString());
                                Log.e(TAG, "List issue origin : " + issue_origins.toString());
                                /*ArrayAdapter adapter2 = new ArrayAdapter<String>(ini,
                                        R.layout.list_view_receipt, R.id.list_title, list_issues);*/
                                CustomListAdapter adapter2 = new CustomListAdapter(ReceiptActivity.this, list_ids, list_issues, descs, R.layout.list_view_receipt);

                                ListView list_available_issue = (ListView) findViewById(R.id.list_available_issue);
                                list_available_issue.setAdapter(adapter2);
                                DeliveryActivity.updateListViewHeight(list_available_issue, 10);
                                // begin the trigger event
                                itemListener();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void showDetailIssue(View view) {
        final Context ini = (Context) view.getContext();
        hideKeyboardFrom(ini, view);

        final View parent = (View) view.getParent();
        TextView issue_number = (TextView) parent.findViewById(R.id.list_title);
        Log.e(TAG, "Choosen : " + issue_number.getText().toString());

        Map<String, String> params = new HashMap<String, String>();
        params.put("issue_number", issue_number.getText().toString());

        _string_request(Request.Method.GET, get_issue_url, params, true,
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

                                LinearLayout step1 = (LinearLayout) findViewById(R.id.step1_1);
                                step1.setVisibility(View.GONE);

                                if (data_status.equals("onprocess") || data_status.equals("pending") || data_status.equals("processed")) {
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
                                        Spinner step2_receipt_type = (Spinner) findViewById(R.id.step2_receipt_type);
                                        step2_receipt_type.setVisibility(View.GONE);
                                        TextView txt_step2_label_type = (TextView) findViewById(R.id.txt_step2_label_type);
                                        txt_step2_label_type.setVisibility(View.GONE);
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
                                    JSONArray items_data = data.getJSONArray("items");
                                    for(int n = 0; n < items_data.length(); n++)
                                    {
                                        JSONObject json_obj_n = items_data.getJSONObject(n);
                                        list_items.add(
                                                json_obj_n.getString("product_name")+" " +
                                                        json_obj_n.getString("quantity")+" " +
                                                        json_obj_n.getString("unit"));
                                        // check the items still available to be received
                                        if (json_obj_n.has("available_qty")) {
                                            int available_qty = json_obj_n.getInt("available_qty");
                                            if (available_qty > 0)
                                                list_product_items.add(json_obj_n.getString("product_name"));
                                        }
                                        product_ids.put(json_obj_n.getString("product_name"), json_obj_n.getString("product_id"));
                                        product_units.put(json_obj_n.getString("product_id"), json_obj_n.getString("unit"));
                                    }
                                    ArrayAdapter adapter3 = new ArrayAdapter<String>(ini,
                                            R.layout.activity_list_view, list_items);

                                    ListView list_issue_items = (ListView) findViewById(R.id.list);
                                    list_issue_items.setAdapter(adapter3);
                                    list_issue_items.setVisibility(View.VISIBLE);
                                    DeliveryActivity.updateListViewHeight(list_issue_items, 10);

                                    // build spinner wh
                                    Spinner step2_receipt_wh = (Spinner)findViewById(R.id.step2_receipt_wh);

                                    ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(ini, R.layout.spinner_item, get_list_assigned_wh());
                                    whAdapter.notifyDataSetChanged();
                                    step2_receipt_wh.setAdapter(whAdapter);

                                    // trigger back button
                                    Button btn_step2_back = (Button) findViewById(R.id.btn_step2_back);
                                    btn_step2_back.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            LinearLayout step2 = (LinearLayout) findViewById(R.id.step2);
                                            step2.setVisibility(View.GONE);
                                            LinearLayout step1_1 = (LinearLayout) findViewById(R.id.step1_1);
                                            step1_1.setVisibility(View.VISIBLE);
                                            // Also empty the array data
                                            list_items.clear();
                                            list_product_items.clear();
                                            product_ids.clear();
                                            product_units.clear();
                                            product_stack.clear();
                                            FrameLayout btn_add_container = (FrameLayout) findViewById(R.id.btn_add_container);
                                            btn_add_container.setVisibility(View.GONE);
                                            // Hide several field
                                            Button btn_confirm_receipt = (Button) findViewById(R.id.btn_confirm_receipt);
                                            btn_confirm_receipt.setVisibility(View.GONE);
                                            TextView txt_step2_label_receipts = (TextView) findViewById(R.id.txt_step2_label_receipts);
                                            txt_step2_label_receipts.setVisibility(View.GONE);
                                            ListView list_receipts = (ListView) findViewById(R.id.list_receipts);
                                            list_receipts.setVisibility(View.GONE);
                                            TextView show_items = (TextView) findViewById(R.id.show_items);
                                            show_items.setVisibility(View.GONE);
                                            FrameLayout txt_item_container = (FrameLayout) findViewById(R.id.txt_item_container);
                                            txt_item_container.setVisibility(View.VISIBLE);
                                        }
                                    });
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

    @Override
    public ArrayList get_list_assigned_wh() {
        final ArrayList<String> items = new ArrayList<String>();

        String roles = sharedpreferences.getString(TAG_ROLES, null);
        try {
            JSONObject jsonObject = new JSONObject(roles);
            Log.e(TAG, "List Roles : " + jsonObject.toString());
            JSONArray keys = jsonObject.names();

            for (int i = 0; i < keys.length (); ++i) {
                String key = keys.getString (i); // Here's your key
                String value = jsonObject.getString (key); // Here's your value
                JSONObject data_n = jsonObject.getJSONObject(key);
                items.add(data_n.getString("warehouse_name"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return items;
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.e(TAG, "Position : " + position);
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    TabFragment1 tab1 = new TabFragment1();
                    return tab1;
                case 1:
                    TabFragment2 tab2 = new TabFragment2();
                    return tab2;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    public static class TabFragment1 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_receipt_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_receipt_2, container, false);
        }
    }

    private void itemListener() {
        final Context ini = ReceiptActivity.this;
        /*Button btn_next = (Button) findViewById(R.id.btn_next);
        btn_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboardFrom(ini, v);

                AutoCompleteTextView issue_number = (AutoCompleteTextView) findViewById(R.id.txt_issue_no);

                Map<String, String> params = new HashMap<String, String>();
                params.put("issue_number", issue_number.getText().toString());

                _string_request(Request.Method.GET, get_issue_url, params, true,
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
                                        if (data_status.equals("onprocess") || data_status.equals("pending") || data_status.equals("processed")) {
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
                                                Spinner step2_receipt_type = (Spinner) findViewById(R.id.step2_receipt_type);
                                                step2_receipt_type.setVisibility(View.GONE);
                                                TextView txt_step2_label_type = (TextView) findViewById(R.id.txt_step2_label_type);
                                                txt_step2_label_type.setVisibility(View.GONE);
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
                                            JSONArray items_data = data.getJSONArray("items");
                                            for(int n = 0; n < items_data.length(); n++)
                                            {
                                                JSONObject json_obj_n = items_data.getJSONObject(n);
                                                list_items.add(
                                                        json_obj_n.getString("product_name")+" " +
                                                                json_obj_n.getString("quantity")+" " +
                                                                json_obj_n.getString("unit"));
                                                // check the items still available to be received
                                                int available_qty = json_obj_n.getInt("available_qty");
                                                if (available_qty > 0)
                                                    list_product_items.add(json_obj_n.getString("product_name"));
                                                product_ids.put(json_obj_n.getString("product_name"), json_obj_n.getString("product_id"));
                                                product_units.put(json_obj_n.getString("product_id"), json_obj_n.getString("unit"));
                                            }
                                            ArrayAdapter adapter2 = new ArrayAdapter<String>(ini,
                                                    R.layout.activity_list_view, list_items);

                                            ListView listView = (ListView) findViewById(R.id.list);
                                            listView.setAdapter(adapter2);
                                            // hide the event due to conflict with add button
                                            //setListEvent(listView, ini, items_data);

                                            // build spinner wh
                                            Spinner step2_receipt_wh = (Spinner)findViewById(R.id.step2_receipt_wh);
                                            ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(ini, R.layout.spinner_item, get_list_warehouse());
                                            whAdapter.notifyDataSetChanged();
                                            step2_receipt_wh.setAdapter(whAdapter);
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
        });*/
        // second button action
        Button btn_confirm = (Button) findViewById(R.id.btn_confirm);
        btn_confirm_trigger(btn_confirm, ini);

        // confirm receipt button
        Button btn_confirm_receipt = (Button) findViewById(R.id.btn_confirm_receipt);
        btn_confirm_receipt_trigger(btn_confirm_receipt, ini);

        Spinner step2_receipt_type = (Spinner) findViewById(R.id.step2_receipt_type);
        select_receipt_type(step2_receipt_type, ini);

        Spinner step2_receipt_wh = (Spinner) findViewById(R.id.step2_receipt_wh);
        spinner_receipt_wh_trigger(step2_receipt_wh, ini);

        // triggering btn add item
        btn_add_trigger(ini);

        // toggle items detail
        TextView show_items = (TextView) findViewById(R.id.show_items);
        show_items.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                FrameLayout txt_item_container = (FrameLayout) findViewById(R.id.txt_item_container);
                txt_item_container.setVisibility(View.VISIBLE);
            }
        });

        // button copy action
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

    final ArrayList<String> list_receipt_ids = new ArrayList<String>();
    final ArrayList<String> list_receipt_items = new ArrayList<String>();
    final ArrayList<String> list_receipt_descs = new ArrayList<String>();

    final Map<String, String> list_receipt_details = new HashMap<String, String>();
    final Map<String, String> list_receipt_detail_items = new HashMap<String, String>();

    private void buildTheReceiptList() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);

        _string_request(
                Request.Method.GET,
                Server.URL + "receipt/list?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of receipts : " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                JSONObject details = new JSONObject(jObj.getString("detail"));
                                JSONObject items = new JSONObject(jObj.getString("items"));

                                for(int n = 0; n < data.length(); n++)
                                {
                                    list_receipt_items.add(data.getString(n));
                                    list_receipt_ids.add(data.getString(n));
                                    JSONObject detail_n = new JSONObject(details.getString(data.getString(n)));
                                    String desc = "Nomor Issue " + detail_n.getString("issue_number");
                                    if (detail_n.has("warehouse_name")) {
                                        desc += " diterima oleh " + detail_n.getString("warehouse_name") + "";
                                    }
                                    if (detail_n.has("warehouse_to_name") && detail_n.has("warehouse_from_name")) {
                                        desc += " dari " + detail_n.getString("warehouse_from_name") + " ke " + detail_n.getString("warehouse_to_name");
                                    }
                                    list_receipt_descs.add(desc);
                                    list_receipt_details.put(data.getString(n), details.getString(data.getString(n)));

                                    list_receipt_detail_items.put(data.getString(n), items.getString(data.getString(n)));
                                }

                                Log.e(TAG, "List receipt details: " + list_receipt_details.toString());

                                CustomListAdapter adapter3 = new CustomListAdapter(ReceiptActivity.this, list_receipt_ids, list_receipt_items, list_receipt_descs, R.layout.list_view_notification);

                                ListView list_receipts = (ListView) findViewById(R.id.list_archive_receipts);
                                list_receipts.setAdapter(adapter3);
                                DeliveryActivity.updateListViewHeight(list_receipts, 400);
                                itemReceiptListener(list_receipts);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void itemReceiptListener(final ListView listView)
    {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                LinearLayout layout_3 = (LinearLayout) findViewById(R.id.layout_2);
                layout_3.setVisibility(View.GONE);

                LinearLayout layout_3_detail = (LinearLayout) findViewById(R.id.layout_2_detail);
                layout_3_detail.setVisibility(View.VISIBLE);

                TextView detail_3_title = (TextView) findViewById(R.id.detail_2_title);
                String receipt_number = list_receipt_ids.get(i);
                detail_3_title.setText(receipt_number);

                try {
                    JSONObject detail = new JSONObject(list_receipt_details.get(receipt_number));

                    TextView txt_issue_number = (TextView) findViewById(R.id.txt_issue_number);
                    txt_issue_number.setText(detail.getString("issue_number"));

                    TextView txt_origin = (TextView) findViewById(R.id.txt_origin);
                    TextView txt_destination = (TextView) findViewById(R.id.txt_destination);
                    if (detail.has("supplier_name")) {
                        txt_origin.setText(detail.getString("supplier_name"));
                    }
                    if (detail.has("warehouse_name")) {
                        txt_destination.setText(detail.getString("warehouse_name"));
                    }
                    if (detail.has("warehouse_from_name")) {
                        txt_origin.setText(detail.getString("warehouse_from_name"));
                    }
                    if (detail.has("warehouse_to_name")) {
                        txt_destination.setText(detail.getString("warehouse_to_name"));
                    }

                    TextView txt_date_receipt = (TextView) findViewById(R.id.txt_date_receipt);
                    txt_date_receipt.setText(detail.getString("completed_at"));

                    JSONArray items_data = new JSONArray(list_receipt_detail_items.get(receipt_number));
                    ArrayList<String> list_r_items = new ArrayList<String>();
                    for(int n = 0; n < items_data.length(); n++)
                    {
                        JSONObject json_obj_n = items_data.getJSONObject(n);
                        list_r_items.add(
                                json_obj_n.getString("product_name")+" " +
                                        json_obj_n.getString("quantity")+" " +
                                        json_obj_n.getString("unit"));
                    }

                    ArrayAdapter adapter_r_items = new ArrayAdapter<String>(ReceiptActivity.this, R.layout.activity_list_view, list_r_items);

                    ListView receipt_item_list = (ListView) findViewById(R.id.receipt_item_list);
                    receipt_item_list.setAdapter(adapter_r_items);
                    DeliveryActivity.updateListViewHeight(receipt_item_list, 0);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // btn back trigger
                Button btn_3_back = (Button) findViewById(R.id.btn_2_back);
                btn_3_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        LinearLayout layout_3 = (LinearLayout) findViewById(R.id.layout_2);
                        layout_3.setVisibility(View.VISIBLE);

                        LinearLayout layout_3_detail = (LinearLayout) findViewById(R.id.layout_2_detail);
                        layout_3_detail.setVisibility(View.GONE);
                    }
                });
            }
        });
    }
}
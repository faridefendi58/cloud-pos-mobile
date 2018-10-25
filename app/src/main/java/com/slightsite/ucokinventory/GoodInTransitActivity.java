package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class GoodInTransitActivity extends MainActivity {
    ProgressDialog pDialog;
    int success;

    private static final String TAG = DeliveryActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

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

    static final int NUM_TAB_ITEMS = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_git);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(NUM_TAB_ITEMS);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        String issue_number = getIntent().getStringExtra("issue_number");
        String i_number = null;
        if (!TextUtils.isEmpty(issue_number)) {
            i_number = issue_number;
        }

        buildTheDeliveryOrderList();
        buildTheStockList();

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                buildTheStockOutForm();
            }
        }, 1000);
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
            // getItem is called to instantiate the fragment for the given page.
            switch (position) {
                case 0:
                    TabFragment1 tab1 = new TabFragment1();
                    return tab1;
                case 1:
                    TabFragment2 tab2 = new TabFragment2();
                    return tab2;
                case 2:
                    TabFragment3 tab3 = new TabFragment3();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return NUM_TAB_ITEMS;
        }
    }

    public static class TabFragment1 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_git_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_git_2, container, false);
        }
    }

    public static class TabFragment3 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_git_3, container, false);
        }
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    final ArrayList<String> list_do_items = new ArrayList<String>();
    final ArrayList<String> list_do_descs = new ArrayList<String>();
    final ArrayList<String> list_do_ids = new ArrayList<String>();

    final Map<String, String> list_do_details = new HashMap<String, String>();

    public void buildTheDeliveryOrderList() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("status", "onprocess");

        _string_request(
                Request.Method.GET,
                Server.URL + "delivery/list?api-key=" + Server.API_KEY,
                params,
                true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                JSONObject po_data = new JSONObject(jObj.getString("po_data"));
                                JSONObject origins = new JSONObject(jObj.getString("po_origin"));
                                JSONObject destinations = new JSONObject(jObj.getString("po_destination"));
                                JSONObject details = new JSONObject(jObj.getString("detail"));


                                for(int n = 0; n < data.length(); n++)
                                {
                                    list_do_items.add(data.getString(n));
                                    JSONObject detail_n = new JSONObject(details.getString(data.getString(n)));
                                    list_do_ids.add(detail_n.getString("status"));

                                    String desc = "Dikirim oleh "+ detail_n.getString("admin_name") +" dari " + origins.getString(data.getString(n)) +
                                            " tujuan " + destinations.getString(data.getString(n)) + " berdasarkan nomor pengadaan "+ detail_n.getString("po_number");
                                    list_do_descs.add(desc);
                                    list_do_details.put(data.getString(n), details.getString(data.getString(n)));
                                }

                                CustomListAdapter adapter3 = new CustomListAdapter(GoodInTransitActivity.this, list_do_ids, list_do_items, list_do_descs, R.layout.list_view_delivery_order);

                                ListView list_do_status = (ListView) findViewById(R.id.list_do_status);
                                list_do_status.setAdapter(adapter3);
                                //updateListViewHeight(list_do_status, 120);
                                incomingGoodListener(list_do_status);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void incomingGoodListener(final ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView id = (TextView) view.findViewById(R.id.list_id);
                TextView title = (TextView) view.findViewById(R.id.list_title);
                TextView desc = (TextView) view.findViewById(R.id.list_desc);

                TextView detail_title_status = (TextView) findViewById(R.id.detail_title_status);
                detail_title_status.setText(list_do_items.get(i));

                String detail_str = list_do_details.get(list_do_items.get(i));
                try {
                    JSONObject details = new JSONObject(detail_str);

                    TextView txt_po_number = (TextView) findViewById(R.id.txt_po_number);
                    txt_po_number.setText(details.getString("po_number"));

                    TextView txt_origin = (TextView) findViewById(R.id.txt_origin);
                    txt_origin.setText(details.getString("supplier_name"));

                    TextView txt_destination = (TextView) findViewById(R.id.txt_destination);
                    txt_destination.setText(details.getString("wh_group_name"));

                    TextView shipping_date = (TextView) findViewById(R.id.txt_shipping_date);
                    shipping_date.setText(details.getString("shipping_date"));

                    TextView txt_status = (TextView) findViewById(R.id.txt_status);
                    txt_status.setText(details.getString("status"));

                    TextView txt_resi_number_status = (TextView) findViewById(R.id.txt_resi_number_status);
                    txt_resi_number_status.setText(details.getString("resi_number"));

                    TextView txt_sender = (TextView) findViewById(R.id.txt_sender);
                    txt_sender.setText(details.getString("admin_name"));

                    FrameLayout status_notes_container = (FrameLayout) findViewById(R.id.notes_container);
                    TextView txt_notes = (TextView) findViewById(R.id.txt_notes);
                    if (details.getString("notes").length() > 0 && !details.getString("notes").equals("null")) {
                        txt_notes.setText(details.getString("notes"));
                        //status_notes_container.setVisibility(View.VISIBLE);
                    } else {
                        txt_notes.setText("-");
                        //status_notes_container.setVisibility(View.GONE);
                    }

                    Button btn_status_confirm = (Button) findViewById(R.id.btn_status_confirm);
                    TableRow receiver_container = (TableRow) findViewById(R.id.receiver_container);
                    TableRow received_date_container = (TableRow) findViewById(R.id.received_date_container);
                    if (details.getString("status").equals("completed")) {
                        btn_status_confirm.setVisibility(View.GONE);

                        TextView txt_receiver = (TextView) findViewById(R.id.txt_receiver);
                        txt_receiver.setText(details.getString("completed_by_name"));


                        TextView txt_received_date = (TextView) findViewById(R.id.txt_received_date);
                        txt_received_date.setText(details.getString("completed_at"));

                        receiver_container.setVisibility(View.VISIBLE);
                        received_date_container.setVisibility(View.VISIBLE);
                    } else {
                        btn_status_confirm.setVisibility(View.VISIBLE);
                        receiver_container.setVisibility(View.GONE);
                        received_date_container.setVisibility(View.GONE);
                    }

                    setDOListPOItems(view, details.getString("po_number"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                LinearLayout layout_2 = (LinearLayout) findViewById(R.id.layout_2);
                layout_2.setVisibility(View.GONE);
                LinearLayout layout_2_detail = (LinearLayout) findViewById(R.id.layout_2_detail);
                layout_2_detail.setVisibility(View.VISIBLE);

                triggerReceiptBtn(title.getText().toString());
            }
        });

        Button btn_status_back = (Button) findViewById(R.id.btn_status_back);
        btn_status_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout_2 = (LinearLayout) findViewById(R.id.layout_2);
                layout_2.setVisibility(View.VISIBLE);
                LinearLayout layout_2_detail = (LinearLayout) findViewById(R.id.layout_2_detail);
                layout_2_detail.setVisibility(View.GONE);
                /*Intent intent = new Intent(getApplicationContext(), GoodInTransitActivity.class);
                startActivity(intent);*/
            }
        });
    }

    private void triggerReceiptBtn(final String do_number)
    {
        Button btn_status_confirm = (Button) findViewById(R.id.btn_status_confirm);
        btn_status_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoodInTransitActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_confirm_receipt_do, null);
                builder.setView(mView);

                final AlertDialog dialog = builder.create();

                try {
                    setDODetail(do_number, mView);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // submit, cancel button trigger
                trigger_dialog_receipt(mView, dialog, do_number);

                dialog.show();
            }
        });
    }

    private void trigger_dialog_receipt(final View mView, final AlertDialog dialog, final String do_number) {
        // cancel method
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        Button btn_dialog_submit = (Button) mView.findViewById(R.id.btn_dialog_submit);
        btn_dialog_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int has_error = 0;
                EditText txt_notes = (EditText) mView.findViewById(R.id.txt_notes);
                if (txt_notes.getText().toString().length() <= 0) {
                    has_error = has_error + 1;
                    Toast.makeText(getApplicationContext(), "Berikan catatan penerimaan barang.", Toast.LENGTH_LONG).show();
                }
                if (has_error == 0) {
                    // do something
                    Map<String, String> params = new HashMap<String, String>();
                    String admin_id = sharedpreferences.getString(TAG_ID, null);
                    params.put("admin_id", admin_id);
                    params.put("notes", txt_notes.getText().toString());
                    params.put("do_number", do_number);

                    JSONArray detail_items = do_detail_items.get(do_number);
                    String the_items = "";
                    for(int n = 0; n < detail_items.length(); n++) {
                        try {
                            JSONObject item = detail_items.getJSONObject(n);
                            String str = item.getString("id")+","+item.getString("quantity");
                            if (n > 0) {
                                the_items += "-" + str;
                            } else {
                                the_items += str;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    params.put("items", the_items);
                    _string_request(
                            Request.Method.POST,
                            Server.URL + "delivery/confirm-receipt?api-key=" + Server.API_KEY,
                            params,
                            true,
                            new VolleyCallback() {
                                @Override
                                public void onSuccess(String result) {
                                    hideDialog();
                                    try {
                                        JSONObject jObj = new JSONObject(result);
                                        success = jObj.getInt(TAG_SUCCESS);
                                        // Check for error node in json
                                        if (success == 1) {
                                            Toast.makeText(getApplicationContext(),
                                                    jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                                            Button btn_status_confirm = (Button) findViewById(R.id.btn_status_confirm);
                                            btn_status_confirm.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(getApplicationContext(),
                                                    jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                                        }

                                        dialog.cancel();
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                }
            }
        });
    }

    final ArrayList<String> list_po_items = new ArrayList<String>();
    final ArrayList<String> list_product_items = new ArrayList<String>();
    final ArrayList<String> list_product_ids = new ArrayList<String>();
    final ArrayList<String> list_item_ids = new ArrayList<String>();

    final Map<String, String> product_ids = new HashMap<String, String>();
    final Map<String, String> product_units = new HashMap<String, String>();
    final Map<String, String> product_qtys = new HashMap<String, String>();

    private void setDOListPOItems(final View view, String issue_number) {
        //clear the array value first
        list_po_items.clear();
        list_product_items.clear();
        list_product_ids.clear();
        product_ids.clear();
        product_units.clear();
        product_qtys.clear();

        Map<String, String> params = new HashMap<String, String>();
        params.put("issue_number", issue_number);
        _string_request(
                Request.Method.GET,
                Server.URL + "receipt/get-issue?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONObject data = jObj.getJSONObject("data");
                                String data_status = data.getString("status");

                                if (data_status.equals("onprocess") || data_status.equals("pending") || data_status.equals("processed")) {
                                    //get session
                                    sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

                                    //set the list
                                    JSONArray items_data = data.getJSONArray("items");
                                    for(int n = 0; n < items_data.length(); n++)
                                    {
                                        JSONObject json_obj_n = items_data.getJSONObject(n);
                                        list_po_items.add(
                                                json_obj_n.getString("product_name")+" " +
                                                        json_obj_n.getString("quantity")+" " +
                                                        json_obj_n.getString("unit"));
                                        // check the items still available to be received
                                        if (json_obj_n.has("available_qty")) {
                                            int available_qty = json_obj_n.getInt("available_qty");
                                            if (available_qty > 0)
                                                list_product_items.add(json_obj_n.getString("product_name"));
                                        }
                                        list_product_ids.add(json_obj_n.getString("product_id"));
                                        list_item_ids.add(json_obj_n.getString("id"));
                                        product_ids.put(json_obj_n.getString("product_name"), json_obj_n.getString("product_id"));
                                        product_units.put(json_obj_n.getString("product_id"), json_obj_n.getString("unit"));
                                        product_qtys.put(json_obj_n.getString("product_name"), json_obj_n.getString("quantity"));
                                    }

                                    ArrayAdapter adapter_po = new ArrayAdapter<String>(GoodInTransitActivity.this, R.layout.activity_list_view_small, list_po_items);

                                    ListView list_po_item_status = (ListView) findViewById(R.id.list_po_item_status);
                                    list_po_item_status.setAdapter(adapter_po);
                                    //updateListViewHeight(list_po_item_status, 0);
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

    final Map<String, Object> do_detail_data = new HashMap<String, Object>();
    final Map<String, JSONArray> do_detail_items = new HashMap<String, JSONArray>(); //po items

    /**
     * Geting the Delivery order detail data
     * @param issue_number
     * @param mView
     */
    private void setDODetail(final String issue_number, final View mView) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("issue_number", issue_number);
        _string_request(
                Request.Method.GET,
                Server.URL + "delivery/detail?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONObject data = jObj.getJSONObject("data");
                                do_detail_data.put(issue_number, data);
                                do_detail_items.put(issue_number, jObj.getJSONArray("po_item_data"));

                                buildThePOItemTable(issue_number, mView);
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

    /**
     * Show the DO items on receipt confirm popup
     * @param issue_number
     * @param mView
     */
    private void buildThePOItemTable(final String issue_number, final View mView)
    {
        JSONArray detail_items = do_detail_items.get(issue_number);

        TableLayout table_layout = (TableLayout) mView.findViewById(R.id.table_layout);

        for(int n = 0; n < detail_items.length(); n++)
        {
            try {
                TableRow row = new TableRow(GoodInTransitActivity.this);
                final JSONObject item = detail_items.getJSONObject(n);
                TextView wh = new TextView(GoodInTransitActivity.this);
                wh.setLayoutParams(new TableRow.LayoutParams(
                        TableRow.LayoutParams.WRAP_CONTENT,
                        TableRow.LayoutParams.WRAP_CONTENT));

                wh.setGravity(Gravity.LEFT);
                wh.setPadding(5, 15, 0, 15);

                wh.setText(item.getString("title"));

                row.addView(wh);

                EditText tv2 = new EditText(GoodInTransitActivity.this);
                tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));

                tv2.setGravity(Gravity.CENTER_HORIZONTAL);
                tv2.setPadding(5, 15, 0, 15);
                tv2.setText(item.getString("quantity"));
                tv2.setInputType(InputType.TYPE_CLASS_NUMBER);
                tv2.setImeOptions(EditorInfo.IME_ACTION_DONE);
                tv2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {
                        System.out.println("afterTextChanged " + new String(editable.toString()));
                        String new_value = new String(editable.toString());
                        if (new_value.length() > 0) {
                            try {
                                item.put("quantity", new_value);
                                System.out.println("item " + item.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                row.addView(tv2);
                table_layout.addView(row);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (detail_items.length() > 0) {
            TableRow no_data = (TableRow) mView.findViewById(R.id.no_data);
            no_data.setVisibility(View.GONE);
        }
    }

    public void buildTheStockList()
    {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);

        _string_request(
                Request.Method.GET,
                Server.URL + "delivery/stock?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");

                                TableLayout table_layout = (TableLayout) findViewById(R.id.stock_table);
                                for(int n = 0; n < data.length(); n++)
                                {
                                    try {
                                        TableRow row = new TableRow(GoodInTransitActivity.this);
                                        final JSONObject item = data.getJSONObject(n);
                                        //Log.e(TAG, "item : "+ item.toString());

                                        list_product_items_out.add(item.getString("title"));
                                        product_names_out.put(item.getString("title"), item.getString("product_id"));
                                        product_units_out.put(item.getString("title"), item.getString("unit"));

                                        TextView wh = new TextView(GoodInTransitActivity.this);
                                        wh.setLayoutParams(new TableRow.LayoutParams(
                                                TableRow.LayoutParams.WRAP_CONTENT,
                                                TableRow.LayoutParams.WRAP_CONTENT));

                                        wh.setGravity(Gravity.LEFT);
                                        wh.setPadding(5, 15, 0, 15);

                                        wh.setText(item.getString("title"));

                                        row.addView(wh);

                                        TextView tv2 = new TextView(GoodInTransitActivity.this);
                                        tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));

                                        tv2.setGravity(Gravity.CENTER_HORIZONTAL);
                                        tv2.setPadding(5, 15, 0, 15);
                                        tv2.setText(item.getString("qty"));
                                        row.addView(tv2);

                                        table_layout.addView(row);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (data.length() > 0) {
                                    TableRow no_data = (TableRow) findViewById(R.id.no_data);
                                    no_data.setVisibility(View.GONE);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * Start to build the third tab - Barang Keluar
     */

    ArrayList<String> assigned_whs = new ArrayList<String>();
    ArrayList<String> group_whs = new ArrayList<String>();

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
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    ArrayList list_products_out;
    Map<String, String> list_items_out = new HashMap<String, String>();
    ArrayList<String> list_product_items_out = new ArrayList<String>();
    Map<String, String> product_names_out = new HashMap<String, String>();
    Map<String, String> product_units_out = new HashMap<String, String>();

    private void buildTheStockOutForm()
    {
        //define date picker
        initDatePicker();

        // define the roles
        set_list_assigned_wh();

        // build spinner of wh coverage
        Spinner wh_group_name = (Spinner)findViewById(R.id.wh_group_name);
        ArrayAdapter<String> whAdapter3 = new ArrayAdapter<String>(GoodInTransitActivity.this, R.layout.spinner_item, group_whs);
        wh_group_name.setAdapter(whAdapter3);

        final FrameLayout btn_add_container = (FrameLayout) findViewById(R.id.btn_add_container);
        wh_group_name.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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

        // define the product list
        if (list_product_items_out.size() <= 0) {
            list_products_out = get_list_product();
        }
        btn_add_trigger();
    }

    private DatePicker datePicker;
    private Calendar calendar;
    private TextView dateView;
    private int year, month, day;

    private void initDatePicker()
    {
        dateView = (TextView) findViewById(R.id.due_date);
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);

        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        showDate(year, month + 1, day);
    }

    @SuppressWarnings("deprecation")
    public void setDate(View view) {
        showDialog(999);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        // TODO Auto-generated method stub
        if (id == 999) {
            return new DatePickerDialog(this, myDateListener, year, month, day);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener myDateListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
            // arg1 = year, arg2 = month, arg3 = day
            showDate(arg1, arg2+1, arg3);
        }
    };

    private void showDate(int year, int month, int day) {
        dateView.setText(new StringBuilder().append(day).append("-")
                .append(month).append("-").append(year));
    }

    private void btn_add_trigger() {
        Button btn_add = (Button) findViewById(R.id.btn_add);
        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(GoodInTransitActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_transfer, null);

                final Spinner list_product = (Spinner) mView.findViewById(R.id.list_product);
                ArrayAdapter<String> productAdapter = new ArrayAdapter<String>(mView.getContext(), R.layout.spinner_item, list_product_items_out);
                list_product.setAdapter(productAdapter);

                builder.setView(mView);
                final AlertDialog dialog = builder.create();

                // submit, cancel, and delete button trigger
                trigger_dialog_move_stock_button(mView, GoodInTransitActivity.this, list_product, dialog);

                dialog.show();
            }
        });
    }

    Map<String, String> list_prices_out = new HashMap<String, String>();

    private void trigger_dialog_move_stock_button(final View mView, final Context ini, final Spinner list_product, final AlertDialog dialog) {
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
                    list_items_out.put(list_product.getSelectedItem().toString(), txt_qty.getText().toString());
                    if (txt_price.getText().toString().length() > 0) {
                        list_prices_out.put(list_product.getSelectedItem().toString(), txt_price.getText().toString());
                    }
                    Toast.makeText(getApplicationContext(), "Berhasil menambahkan " + list_product.getSelectedItem().toString(), Toast.LENGTH_LONG).show();
                    dialog.hide();
                    // show the added item
                    Iterator<Map.Entry<String, String>> iterator = list_items_out.entrySet().iterator();
                    ArrayList<String> arr_list_items = new ArrayList<String>();
                    Integer i = 0;
                    String product_stack_str = "";
                    String list_item_str = "";
                    String price_stack_str = "";
                    String list_price_str = "";
                    while(iterator.hasNext())
                    {
                        Map.Entry<String, String> pair = iterator.next();
                        String r_label = pair.getKey() + " " + pair.getValue() + " " + product_units_out.get(pair.getKey());
                        String s_label = "";
                        if (list_prices_out.containsKey(pair.getKey()) && list_prices_out.get(pair.getKey()).length() > 0) {
                            r_label += " @" + list_prices_out.get(pair.getKey());
                            s_label += pair.getKey() + " @" + list_prices_out.get(pair.getKey());
                        }
                        if (i > 0) {
                            product_stack_str += "-" + product_names_out.get(pair.getKey()) + "," + pair.getValue();
                            list_item_str += ", " + r_label;
                            if (s_label.length() > 0) {
                                price_stack_str += "-" + product_names_out.get(pair.getKey()) + "," + list_prices_out.get(pair.getKey());
                                list_price_str += ", " + s_label;
                            }
                        } else {
                            product_stack_str += product_names_out.get(pair.getKey()) + "," + pair.getValue();
                            list_item_str += r_label;
                            if (s_label.length() > 0) {
                                price_stack_str += product_names_out.get(pair.getKey()) + "," + list_prices_out.get(pair.getKey());
                                list_price_str += s_label;
                            }
                        }
                        arr_list_items.add(r_label);
                        i ++;
                    }

                    ArrayAdapter adapter2 = new ArrayAdapter<String>(ini, R.layout.activity_list_view, arr_list_items);

                    ListView listView = (ListView) findViewById(R.id.list_item);
                    listView.setAdapter(adapter2);
                    DeliveryActivity.updateListViewHeight(listView, 50);

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
                if (list_items_out.containsKey(stack_id.getText().toString())) {
                    list_items_out.remove(stack_id.getText().toString());
                }

                Iterator<Map.Entry<String, String>> iterator = list_items_out.entrySet().iterator();
                ArrayList<String> arr_list_items = new ArrayList<String>();
                Integer i = 0;
                String product_stack_str = "";
                String list_item_str = "";
                String price_stack_str = "";
                String list_price_str = "";
                while(iterator.hasNext())
                {
                    Map.Entry<String, String> pair = iterator.next();
                    String r_label = pair.getKey() + " " + pair.getValue() + " " + product_units_out.get(pair.getKey());
                    String s_label = "";
                    if (list_prices_out.containsKey(pair.getKey()) && list_prices_out.get(pair.getKey()).length() > 0) {
                        r_label += " @" + list_prices_out.get(pair.getKey());
                        s_label += pair.getKey() + " @" + list_prices_out.get(pair.getKey());
                    }
                    if (i > 0) {
                        product_stack_str += "-" + product_names_out.get(pair.getKey()) + "," + pair.getValue();
                        list_item_str += ", " + r_label;
                        if (s_label.length() > 0) {
                            price_stack_str += "-" + product_names_out.get(pair.getKey()) + "," + list_prices_out.get(pair.getKey());
                            list_price_str += ", " + s_label;
                        }
                    } else {
                        product_stack_str += product_names_out.get(pair.getKey()) + "," + pair.getValue();
                        list_item_str += r_label;
                        if (s_label.length() > 0) {
                            price_stack_str += product_names_out.get(pair.getKey()) + "," + list_prices_out.get(pair.getKey());
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
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                    list_product_items_out.add(data_n.getString("title"));
                                    product_names_out.put(data_n.getString("title"), data_n.getString("id"));
                                    product_units_out.put(data_n.getString("title"), data_n.getString("unit"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }

    private void set_list_item_trigger(final ListView list, final Context ini) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String title = list.getItemAtPosition(i).toString();
                Iterator<Map.Entry<String, String>> iterator = list_items_out.entrySet().iterator();
                Integer j = 0;
                String current_val = "";
                String current_key = "";
                String current_price = "";
                while(iterator.hasNext())
                {
                    Map.Entry<String, String> pair = iterator.next();
                    if (j.equals(i)) {
                        String p_id = product_names_out.get(pair.getKey());
                        current_val = pair.getValue();
                        current_key = pair.getKey();
                        if (list_prices_out.containsKey(pair.getKey())) {
                            current_price = list_prices_out.get(pair.getKey());
                        }
                    }
                    j ++;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(ini);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_transfer, null);

                final Spinner list_product = (Spinner) mView.findViewById(R.id.list_product);
                ArrayAdapter<String> productAdapter = new ArrayAdapter<String>(mView.getContext(), R.layout.spinner_item, list_product_items_out);
                list_product.setAdapter(productAdapter);
                Integer index_p_items = list_product_items_out.indexOf(current_key);
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
                trigger_dialog_move_stock_button(mView, ini, list_product, dialog);

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
                //final Spinner wh_list_from = (Spinner) findViewById(R.id.wh_list_from);
                final Spinner wh_group_name = (Spinner) findViewById(R.id.wh_group_name);
                TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);
                TextView due_date = (TextView) findViewById(R.id.due_date);

                Map<String, String> params = new HashMap<String, String>();
                params.put("items", txt_item_select.getText().toString());
                //params.put("warehouse_from_name", wh_list_from.getSelectedItem().toString());
                params.put("wh_group_name", wh_group_name.getSelectedItem().toString());
                params.put("due_date", due_date.getText().toString());
                // default to pre order due to every po should have approval
                params.put("admin_id", sharedpreferences.getString("id", null));
                //Log.e(TAG, "Params : " + params.toString());

                String transfer_url = Server.URL + "transfer/create?api-key=" + Server.API_KEY;
                _string_request(
                        Request.Method.POST,
                        transfer_url,
                        params,
                        true,
                        new VolleyCallback(){
                            @Override
                            public void onSuccess(String result) {
                                hideDialog();
                                try {
                                    JSONObject jObj = new JSONObject(result);
                                    success = jObj.getInt(TAG_SUCCESS);
                                    if (success == 1) {
                                        String issue_number = jObj.getString("issue_number");
                                        String success_msg = "Transfer barang dengan kode " + issue_number + " dari Good In Transit "
                                                +" telah dibuat oleh "+ sharedpreferences.getString("name", null);

                                        if (wh_group_name.getSelectedItem().toString().length() > 0) {
                                            success_msg += " dan akan dikirim ke area " + wh_group_name.getSelectedItem().toString();
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

    /**
     * End of building the third tab - Barang Keluar
     */
}

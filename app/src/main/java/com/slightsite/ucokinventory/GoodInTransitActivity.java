package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
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

    static final int NUM_TAB_ITEMS = 2;

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
}

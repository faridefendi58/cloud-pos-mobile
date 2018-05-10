package com.slightsite.ucokinventory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DeliveryActivity extends MainActivity {
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_delivery);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        buildThePreOrderList();
        buildTheDeliveryOrderList();
    }

    public void buildMenu() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View v = navigationView.getHeaderView(0);
        TextView txt_full_name = (TextView) v.findViewById(R.id.txt_full_name);
        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        txt_full_name.setText( sharedpreferences.getString(TAG_NAME, null) );
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
            // Show 2 total pages.
            return 2;
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

    final ArrayList<String> list_items = new ArrayList<String>();
    final ArrayList<String> list_descs = new ArrayList<String>();
    final ArrayList<String> list_ids = new ArrayList<String>();

    public void buildThePreOrderList() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("status", "pending");
        params.put("is_pre_order", "1");
        Log.e(TAG, "Params of pending PO : " + params.toString());

        final ArrayList<String> descs = new ArrayList<String>();
        _string_request(
                Request.Method.GET,
                Server.URL + "purchase/list?api-key=" + Server.API_KEY,
                params,
                true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of list pending PO : " + result.toString());
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                JSONObject origins = new JSONObject(jObj.getString("origin"));
                                JSONObject destinations = new JSONObject(jObj.getString("destination"));

                                for(int n = 0; n < data.length(); n++)
                                {
                                    list_items.add(data.getString(n));
                                    list_ids.add(data.getString(n));
                                    String desc = "Pengadaan dari " + origins.getString(data.getString(n)) +
                                            " dengan tujuan area warehouse " + destinations.getString(data.getString(n));
                                    list_descs.add(desc);
                                }

                                CustomListAdapter adapter2 = new CustomListAdapter(DeliveryActivity.this, list_ids, list_items, list_descs, R.layout.list_view_notification);

                                ListView list_pre_order = (ListView) findViewById(R.id.list_pre_order);
                                list_pre_order.setAdapter(adapter2);
                                updateListViewHeight(list_pre_order, 100);
                                itemListener(list_pre_order);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void itemListener(final ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String title = list.getItemAtPosition(i).toString();
                TextView id = (TextView) view.findViewById(R.id.list_id);
                TextView title = (TextView) view.findViewById(R.id.list_title);
                TextView desc = (TextView) view.findViewById(R.id.list_desc);

                TextView detail_title = (TextView) findViewById(R.id.detail_title);
                detail_title.setText(list_items.get(i));

                TextView detail_desc = (TextView) findViewById(R.id.detail_desc);
                //detail_desc.setText(list_descs.get(i));
                LinearLayout layout_1 = (LinearLayout) findViewById(R.id.layout_1);
                layout_1.setVisibility(View.GONE);
                LinearLayout layout_1_detail = (LinearLayout) findViewById(R.id.layout_1_detail);
                layout_1_detail.setVisibility(View.VISIBLE);
                Log.e(TAG, "id : " + id.getText().toString());
                setListPOItems(view);
                buildTheShipment();
            }
        });

        Button btn_pre_order_back = (Button) findViewById(R.id.btn_pre_order_back);
        btn_pre_order_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout layout_1 = (LinearLayout) findViewById(R.id.layout_1);
                layout_1.setVisibility(View.VISIBLE);
                LinearLayout layout_1_detail = (LinearLayout) findViewById(R.id.layout_1_detail);
                layout_1_detail.setVisibility(View.GONE);
            }
        });

        Button btn_pre_order_confirm = (Button) findViewById(R.id.btn_pre_order_confirm);
        btn_pre_order_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processDeliveryOrder();
            }
        });
    }

    final ArrayList<String> list_do_items = new ArrayList<String>();
    final ArrayList<String> list_do_descs = new ArrayList<String>();
    final ArrayList<String> list_do_ids = new ArrayList<String>();

    final Map<String, String> list_do_details = new HashMap<String, String>();

    public void buildTheDeliveryOrderList() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);

        _string_request(
                Request.Method.GET,
                Server.URL + "delivery/list?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response of delivery: " + result.toString());
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
                                    list_do_ids.add(data.getString(n));
                                    JSONObject detail_n = new JSONObject(details.getString(data.getString(n)));

                                    String desc = "Nomor PO "+ detail_n.getString("po_number") +", Dari " + origins.getString(data.getString(n)) +
                                            " Tujuan " + destinations.getString(data.getString(n));
                                    list_do_descs.add(desc);
                                    list_do_details.put(data.getString(n), details.getString(data.getString(n)));
                                }

                                Log.e(TAG, "List do items: " + list_do_items.toString());
                                Log.e(TAG, "List do ids: " + list_do_ids.toString());
                                Log.e(TAG, "List do descs: " + list_do_descs.toString());
                                Log.e(TAG, "List do details: " + list_do_details.toString());

                                CustomListAdapter adapter3 = new CustomListAdapter(DeliveryActivity.this, list_do_ids, list_do_items, list_do_descs, R.layout.list_view_notification);
                                /*ArrayAdapter adapter3 = new ArrayAdapter<String>(DeliveryActivity.this,
                                        R.layout.activity_list_view, list_do_items);*/

                                ListView list_do_status = (ListView) findViewById(R.id.list_do_status);
                                list_do_status.setAdapter(adapter3);
                                updateListViewHeight(list_do_status, 120);
                                itemStatusListener(list_do_status);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public static class TabFragment1 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_2, container, false);
        }
    }

    final ArrayList<String> list_po_items = new ArrayList<String>();
    final ArrayList<String> list_product_items = new ArrayList<String>();
    final ArrayList<String> list_product_ids = new ArrayList<String>();
    final ArrayList<String> list_item_ids = new ArrayList<String>();

    final Map<String, String> product_ids = new HashMap<String, String>();
    final Map<String, String> product_units = new HashMap<String, String>();
    final Map<String, String> product_qtys = new HashMap<String, String>();

    private void setListPOItems(final View view) {

        TextView issue_number = (TextView) view.findViewById(R.id.list_id);
        //define date picker
        initDatePicker();
        //clear the array value first
        list_po_items.clear();
        list_product_items.clear();
        list_product_ids.clear();
        product_ids.clear();
        product_units.clear();
        product_qtys.clear();

        Map<String, String> params = new HashMap<String, String>();
        params.put("issue_number", issue_number.getText().toString());
        _string_request(
                Request.Method.GET,
                Server.URL + "receipt/get-issue?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Items PO Response: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONObject data = jObj.getJSONObject("data");
                                String data_status = data.getString("status");
                                String data_type = data.getString("type");

                                if (data_status.equals("onprocess") || data_status.equals("pending") || data_status.equals("processed")) {
                                    //set notes
                                    EditText txt_receipt_notes = (EditText) findViewById(R.id.txt_receipt_notes);
                                    //get session
                                    sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
                                    //get issue numb
                                    String fin_issue_number, fin_from, fin_to;
                                    //if (data_type.equals("purchase_order")) {
                                        fin_issue_number = data.getString("po_number");
                                        fin_from = data.getString("supplier_name");
                                        fin_to = data.getString("wh_group_name");
                                    //}
                                    TextView detail_desc = (TextView) findViewById(R.id.detail_desc);
                                    detail_desc.setText(fin_from + " - " + fin_to);
                                    detail_desc.setVisibility(View.VISIBLE);

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

                                    //ArrayAdapter adapter_po = new ArrayAdapter<String>(DeliveryActivity.this, R.layout.activity_list_view, list_po_items);
                                    CustomListAdapter adapter_po = new CustomListAdapter(DeliveryActivity.this, list_item_ids, list_po_items, list_product_items, R.layout.list_view_delivery);

                                    ListView list_po_item = (ListView) findViewById(R.id.list_po_item);
                                    list_po_item.setAdapter(adapter_po);
                                    updateListViewHeight(list_po_item, 0);
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

    public static void updateListViewHeight(ListView myListView, Integer add_height) {
        ListAdapter myListAdapter = myListView.getAdapter();
        if (myListAdapter == null) {
            return;
        }
        // get listview height
        int totalHeight = 0;
        int adapterCount = myListAdapter.getCount();
        for (int size = 0; size < adapterCount; size++) {
            View listItem = myListAdapter.getView(size, null, myListView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
        // Change Height of ListView
        ViewGroup.LayoutParams params = myListView.getLayoutParams();
        params.height = (totalHeight
                + (myListView.getDividerHeight() * (adapterCount))) + add_height;
        myListView.setLayoutParams(params);
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

    public void changePOItem(View view)
    {
        Log.e(TAG, "List id : " + list_ids.toString());
        final Context ini = (Context) view.getContext();
        hideKeyboardFrom(ini, view);

        final View parent = (View) view.getParent();
        TextView issue_number = (TextView) parent.findViewById(R.id.list_title);
        Log.e(TAG, "Choosen : " + issue_number.getText().toString());

        AlertDialog.Builder builder = new AlertDialog.Builder(ini);
        View mView = getLayoutInflater().inflate(R.layout.dialog_update_item_delivery, null);

        TextView product_nm = (TextView) mView.findViewById(R.id.txt_product_name);
        TextView txt_qty = (TextView) mView.findViewById(R.id.txt_qty);
        TextView list_desc = (TextView) parent.findViewById(R.id.list_desc);
        TextView po_item_id = (TextView) mView.findViewById(R.id.po_item_id);
        TextView list_id = (TextView) parent.findViewById(R.id.list_id);

        product_nm.setText(list_desc.getText().toString());
        txt_qty.setText(product_qtys.get(list_desc.getText().toString()));
        po_item_id.setText(list_id.getText().toString());

        builder.setView(mView);
        final AlertDialog dialog = builder.create();

        // update, cancel, and delete button trigger
        trigger_dialog_button(mView, ini, dialog);

        dialog.show();
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    /**
     * Dialog button actions
     * @param mView
     * @param ini
     * @param dialog
     */
    private void trigger_dialog_button(final View mView, final Context ini, final AlertDialog dialog) {
        // cancel method
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        // cancel method
        ImageView image_close = (ImageView) mView.findViewById(R.id.image_close);
        image_close.setOnClickListener(new View.OnClickListener() {
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
                    //post update request
                    TextView po_item_id = (TextView) mView.findViewById(R.id.po_item_id);
                    EditText txt_qty = (EditText) mView.findViewById(R.id.txt_qty);

                    Map<String, String> update_params = new HashMap<String, String>();
                    update_params.put("po_item_id", po_item_id.getText().toString());
                    update_params.put("quantity", txt_qty.getText().toString());
                    _modify_po_item("update", update_params);
                    dialog.hide();
                }
            }
        });

        // action of delete button
        Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);
        btn_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TextView po_item_id = (TextView) mView.findViewById(R.id.po_item_id);

                Map<String, String> delete_params = new HashMap<String, String>();
                delete_params.put("po_item_id", po_item_id.getText().toString());
                _modify_po_item("delete", delete_params);
                dialog.hide();
            }
        });
    }

    private void _modify_po_item(String action, Map update_params) {
        String url = Server.URL;
        if (action.equals("update")) {
            url += "delivery/update-item?api-key=" + Server.API_KEY;
        } else if (action.equals("delete")) {
            url += "delivery/delete-item?api-key=" + Server.API_KEY;
        }
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        update_params.put("admin_id", admin_id);
        Log.e(TAG, update_params.toString());

        _string_request(
                Request.Method.POST,
                url,
                update_params,
                true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            if (success == 1) {
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
                }
        );
    }

    private void processDeliveryOrder()
    {
        TextView detail_title = (TextView) findViewById(R.id.detail_title);
        TextView due_date = (TextView) findViewById(R.id.due_date);
        EditText txt_resi_number = (EditText) findViewById(R.id.txt_resi_number);
        EditText txt_do_notes = (EditText) findViewById(R.id.txt_do_notes);
        Spinner txt_shipment_name = (Spinner) findViewById(R.id.txt_shipment_name);

        String issue_number = detail_title.getText().toString();
        String shipping_date = due_date.getText().toString();
        String resi_number = txt_resi_number.getText().toString();
        String notes = txt_do_notes.getText().toString();
        String shipment_name = txt_shipment_name.getSelectedItem().toString();

        Map<String, String> params = new HashMap<String, String>();
        params.put("issue_number", issue_number);
        params.put("shipping_date", shipping_date);
        if (resi_number.trim().length() > 0)
            params.put("resi_number", resi_number);
        if (notes.trim().length() > 0)
            params.put("notes", notes);
        if (shipment_name.length() > 0 && !shipment_name.equals("-"))
            params.put("shipment_name", shipment_name);

        Log.e(TAG, "Params : " + params.toString());
        _string_request(
                Request.Method.POST,
                Server.URL + "purchase/create-shipping?api-key=" + Server.API_KEY,
                params,
                true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            if (success == 1) {
                                LinearLayout layout_1_detail = (LinearLayout) findViewById(R.id.layout_1_detail);
                                layout_1_detail.setVisibility(View.GONE);
                                LinearLayout layout_1_submision = (LinearLayout) findViewById(R.id.layout_1_submision);
                                layout_1_submision.setVisibility(View.VISIBLE);

                                TextView txt_message = (TextView) findViewById(R.id.txt_message);
                                txt_message.setText(jObj.getString(TAG_MESSAGE));
                                txt_message.setVisibility(View.VISIBLE);
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );
    }

    private void buildTheShipment()
    {
        Spinner shipment = (Spinner) findViewById(R.id.txt_shipment_name);

        ArrayAdapter<String> shAdapter = new ArrayAdapter<String>(this, R.layout.spinner_item, get_list_shipment());
        shipment.setAdapter(shAdapter);
    }

    private ArrayList get_list_shipment() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "shipment/list?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                wh_url,
                params,
                false,
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

    private void itemStatusListener(final ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //String title = list.getItemAtPosition(i).toString();
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

                    Button btn_status_confirm = (Button) findViewById(R.id.btn_status_confirm);
                    if (details.getString("status").equals("completed")) {
                        btn_status_confirm.setVisibility(View.GONE);
                    } else {
                        btn_status_confirm.setVisibility(View.VISIBLE);
                    }

                    setDOListPOItems(view, details.getString("po_number"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                LinearLayout layout_2 = (LinearLayout) findViewById(R.id.layout_2);
                layout_2.setVisibility(View.GONE);
                LinearLayout layout_2_detail = (LinearLayout) findViewById(R.id.layout_2_detail);
                layout_2_detail.setVisibility(View.VISIBLE);
                Log.e(TAG, "id : " + id.getText().toString());

                triggerReceiptBtn(id.getText().toString());
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
            }
        });
    }

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

                                    ArrayAdapter adapter_po = new ArrayAdapter<String>(DeliveryActivity.this, R.layout.activity_list_view, list_po_items);

                                    ListView list_po_item_status = (ListView) findViewById(R.id.list_po_item_status);
                                    list_po_item_status.setAdapter(adapter_po);
                                    updateListViewHeight(list_po_item_status, 0);
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

    private void triggerReceiptBtn(final String do_number)
    {
        Button btn_status_confirm = (Button) findViewById(R.id.btn_status_confirm);
        btn_status_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DeliveryActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_confirm_receipt, null);
                builder.setView(mView);
                final AlertDialog dialog = builder.create();

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
}

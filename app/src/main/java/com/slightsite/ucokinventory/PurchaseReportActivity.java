package com.slightsite.ucokinventory;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class PurchaseReportActivity extends MainActivity {
    ProgressDialog pDialog;
    int success;
    JSONObject po_detail;
    static final int NUM_TAB_ITEMS = 3;
    private ArrayList<String> po_items = new ArrayList<String>();
    // po items data contain id, po_id, product_id, title, etc
    private JSONArray po_items_data = new JSONArray();
    // stack of deleted po items, contain the table id
    private ArrayList<String> po_item_deleted = new ArrayList<String>();

    private static final String TAG = PurchaseReportActivity.class.getSimpleName();
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
    private PurchaseReportActivity.SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_purchase_report);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        mViewPager.setOffscreenPageLimit(NUM_TAB_ITEMS);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        //tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                if (tab.getPosition() == 2) {
                    updatePO(tab.getCustomView());
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        final Context ini = this;

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // delay build the form after tabs fully finished
                String issue_number = getIntent().getStringExtra("issue_number");
                if (!TextUtils.isEmpty(issue_number)) {
                    set_detail_issue(issue_number);
                    initUi();
                }
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
            return inflater.inflate(R.layout.tab_fragment_p_detail_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_p_detail_2, container, false);
        }
    }

    public static class TabFragment3 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_p_detail_3, container, false);
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

    private void set_detail_issue(String issue_number) {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("issue_number", issue_number);

        _string_request(
                Request.Method.GET,
                Server.URL + "purchase/detail?api-key=" + Server.API_KEY,
                params, true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                po_detail = jObj.getJSONObject("data");

                                TextView txt_po_number = (TextView) findViewById(R.id.txt_po_number);
                                txt_po_number.setText(po_detail.getString("po_number"));
                                TextView txt_supplier_name = (TextView) findViewById(R.id.txt_supplier_name);
                                txt_supplier_name.setText(po_detail.getString("supplier_name"));
                                TextView txt_wh_group_name = (TextView) findViewById(R.id.txt_wh_group_name);
                                txt_wh_group_name.setText(po_detail.getString("wh_group_name"));
                                TextView txt_shipment_name = (TextView) findViewById(R.id.txt_shipment_name);
                                txt_shipment_name.setText(po_detail.getString("shipment_name"));
                                TextView txt_created_at = (TextView) findViewById(R.id.txt_created_at);
                                txt_created_at.setText(po_detail.getString("created_at"));
                                TextView txt_created_by = (TextView) findViewById(R.id.txt_created_by);
                                txt_created_by.setText(po_detail.getString("created_by_name"));

                                TextView txt_status = (TextView) findViewById(R.id.txt_status);
                                txt_status.setText(po_detail.getString("status"));

                                po_items_data = jObj.getJSONArray("items");

                                for(int n = 0; n < po_items_data.length(); n++)
                                {
                                    JSONObject data_n = new JSONObject(po_items_data.getString(n));
                                    String item_title = data_n.getString("title")+' '+data_n.getString("quantity")
                                            + " " + data_n.getString("unit");
                                    po_items.add(item_title);
                                }

                                ArrayAdapter adapter = new ArrayAdapter<String>(PurchaseReportActivity.this, R.layout.activity_list_view, po_items);

                                ListView txt_list_items = (ListView) findViewById(R.id.txt_list_items);
                                txt_list_items.setAdapter(adapter);
                                DeliveryActivity.updateListViewHeight(txt_list_items, 20);

                                JSONArray history = jObj.getJSONArray("history");
                                ArrayList<String> list_timeline_ids = new ArrayList<String>();
                                ArrayList<String> list_timeline_titles = new ArrayList<String>();
                                ArrayList<String> list_timeline_descs = new ArrayList<String>();

                                for(int m = 0; m < history.length(); m++)
                                {
                                    JSONObject data_n = new JSONObject(history.getString(m));
                                    list_timeline_ids.add(data_n.getString("date"));
                                    list_timeline_titles.add(data_n.getString("title"));

                                    if (!TextUtils.isEmpty(data_n.getString("notes"))) {
                                        list_timeline_descs.add(data_n.getString("notes"));
                                    } else {
                                        list_timeline_descs.add("-");
                                    }
                                }

                                CustomListAdapter adapter2 = new CustomListAdapter(PurchaseReportActivity.this, list_timeline_ids, list_timeline_titles, list_timeline_descs, R.layout.list_view_timeline);

                                ListView txt_list_timelines = (ListView) findViewById(R.id.txt_list_timelines);
                                txt_list_timelines.setAdapter(adapter2);
                                DeliveryActivity.updateListViewHeight(txt_list_timelines, 120);

                                //Log.e(TAG, "Titles : "+ list_timeline_titles.toString());
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void cancelPO(View view) {
        //Log.e(TAG, "Detail : " + po_detail.toString());
        String po_status = null;
        try {
            po_status = po_detail.getString("status");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (po_status.equals("pending")) {
            AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                    PurchaseReportActivity.this);
            quitDialog.setTitle(getResources().getString(R.string.dialog_remove_po));
            quitDialog.setPositiveButton(getResources().getString(R.string.btn_remove), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        executingCancelPO();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Intent newActivity = new Intent(PurchaseReportActivity.this,
                            PurchaseActivity.class);
                    startActivity(newActivity);
                }
            });

            quitDialog.setNegativeButton(getResources().getString(R.string.btn_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            quitDialog.show();
        } else {
            Toast.makeText(
                    getApplicationContext(),
                    getResources().getString(R.string.msg_unable_remove_po),
                    Toast.LENGTH_LONG).show();
        }
    }

    private TextView update_po_number;
    private Spinner supplier_name;
    private Spinner shipment_name;
    private Spinner wh_group_name;
    private TextView due_date;
    private ListView po_list_items;

    private ArrayList supplier_list;
    private ArrayList shipment_list;
    private ArrayList<String> shipment_list_id = new ArrayList<String>();
    private ArrayList<String> group_whs = new ArrayList<String>();
    private ArrayList<String> po_update_items = new ArrayList<String>();

    /**
     * Initialize ui expecially for tab update PO
     */
    private void initUi() {
        update_po_number = (TextView) findViewById(R.id.update_po_number);
        supplier_name = (Spinner) findViewById(R.id.supplier_name);
        shipment_name = (Spinner) findViewById(R.id.shipment_name);
        wh_group_name = (Spinner) findViewById(R.id.wh_group_name);
        due_date = (TextView) findViewById(R.id.due_date);
        po_list_items = (ListView) findViewById(R.id.po_list_items);

        supplier_list = get_list_supplier();
        shipment_list = get_list_shipment();
        list_assigned_wh();
        initDatePicker();

        po_update_items = po_items;
        reloadListItems();
        listItemListener();

        get_product_list();
    }

    private JSONArray supplier_list_data = new JSONArray();

    private ArrayList get_list_supplier() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();

        String wh_url = Server.URL + "supplier/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                supplier_list_data = jObj.getJSONArray("data");
                                for(int n = 0; n < supplier_list_data.length(); n++)
                                {
                                    JSONObject data_n = supplier_list_data.getJSONObject(n);
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

    private JSONArray shipment_list_data = new JSONArray();

    private ArrayList get_list_shipment() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        //items.add("-");

        String wh_url = Server.URL + "shipment/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, wh_url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                shipment_list_data = jObj.getJSONArray("data");
                                for(int n = 0; n < shipment_list_data.length(); n++)
                                {
                                    JSONObject data_n = shipment_list_data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                    shipment_list_id.add(data_n.getString("id"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

        return items;
    }

    private void list_assigned_wh() {

        String roles = sharedpreferences.getString(TAG_ROLES, null);
        try {
            JSONObject jsonObject = new JSONObject(roles);
            JSONArray names = jsonObject.names();

            for (int i = 0; i < names.length (); ++i) {
                String key = names.getString (i); // Here's your key
                String value = jsonObject.getString (key); // Here's your value
                JSONObject data_n = jsonObject.getJSONObject(key);
                if (!group_whs.contains(data_n.getString("warehouse_group_name"))) {
                    group_whs.add(data_n.getString("warehouse_group_name"));
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // product list in json format [{"id":"13","title":"Durian Cup","unit":"box"}]
    private JSONArray product_list_data = new JSONArray();
    private ArrayList<String> product_list = new ArrayList<String>();

    private void get_product_list() {
        final ArrayList<String> items = new ArrayList<String>();

        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

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
                                product_list_data = jObj.getJSONArray("data");
                                for(int n = 0; n < product_list_data.length(); n++)
                                {
                                    JSONObject data_n = product_list_data.getJSONObject(n);
                                    product_list.add(data_n.getString("title"));
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
        //showDate(year, month + 1, day);
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

    public void updatePO(View view) {
        mViewPager.setCurrentItem(2, true);

        try {
            update_po_number.setText(po_detail.getString("po_number"));

            // init the spinner of list supplier
            ArrayAdapter<String> spAdapter = new ArrayAdapter<String>(
                    PurchaseReportActivity.this,
                    R.layout.spinner_item, supplier_list);
            supplier_name.setAdapter(spAdapter);
            supplier_name.setSelection(supplier_list.indexOf(po_detail.getString("supplier_name")));

            // init the spinner of list shipment
            ArrayAdapter<String> shAdapter = new ArrayAdapter<String>(
                    PurchaseReportActivity.this,
                    R.layout.spinner_item, shipment_list);
            shipment_name.setAdapter(shAdapter);
            shipment_name.setSelection(shipment_list_id.indexOf(po_detail.getString("shipment_id")));

            // init the spinner of list wh group
            ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(
                    PurchaseReportActivity.this,
                    R.layout.spinner_item, group_whs);
            wh_group_name.setAdapter(whAdapter);
            wh_group_name.setSelection(group_whs.indexOf(po_detail.getString("wh_group_name")));

            // po date
            dateView.setText(AppController.parseDate(po_detail.getString("due_date"),"d-M-yyyy"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Rebuilding the PO items list
     */
    private void reBuildThePOItems() {
        po_update_items.clear();
        try {
            for(int n = 0; n < po_items_data.length(); n++)
            {
                JSONObject data_n = new JSONObject(po_items_data.getString(n));
                String item_title = data_n.getString("title")+' '+data_n.getString("quantity")
                        + " " + data_n.getString("unit");
                po_update_items.add(item_title);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reloading list po items on update fragment
     */
    private void reloadListItems() {
        ArrayAdapter adapter = new ArrayAdapter<String>(
                PurchaseReportActivity.this,
                R.layout.activity_list_view,
                po_update_items);

        po_list_items.setAdapter(adapter);
    }

    // text view on dialog
    private TextView dialog_txt_qty;
    private TextView dialog_txt_price;
    private TextView dialog_stack_id;

    // current value of the selected item
    Integer current_item_qty = 0;
    Integer current_item_price = 0;

    private void listItemListener() {
        po_list_items.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String title = po_list_items.getItemAtPosition(i).toString();

                try {
                    current_item_qty = po_items_data.getJSONObject(i).getInt("quantity");
                    current_item_price = po_items_data.getJSONObject(i).getInt("price");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(PurchaseReportActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_update_item_purchase, null);

                dialog_txt_qty = (TextView) mView.findViewById(R.id.txt_qty);
                dialog_txt_qty.setText(""+ current_item_qty);

                dialog_stack_id = (TextView) mView.findViewById(R.id.stack_id);
                dialog_stack_id.setText(""+ i);

                dialog_txt_price = (TextView) mView.findViewById(R.id.txt_price);
                dialog_txt_price.setText(""+ current_item_price);

                builder.setView(mView);
                final AlertDialog dialog = builder.create();

                // submit, cancel, and delete button trigger
                trigger_dialog_button(mView, dialog);

                // show button delete
                Button btn_dialog_delete = (Button) mView.findViewById(R.id.btn_dialog_delete);
                btn_dialog_delete.setVisibility(View.VISIBLE);
                Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
                btn_dialog_cancel.setVisibility(View.GONE);
                TextView dialog_title = (TextView) mView.findViewById(R.id.dialog_title);
                dialog_title.setText(title);

                dialog.show();
            }
        });
    }

    /**
     * Handle submision of the update dialog form
     * @param view
     * @param dialog
     */
    private void trigger_dialog_button(View view, final Dialog dialog) {
        Button btn_dialog_submit = (Button) view.findViewById(R.id.btn_dialog_submit);
        Button btn_dialog_delete = (Button) view.findViewById(R.id.btn_dialog_delete);

        btn_dialog_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer index = Integer.parseInt(dialog_stack_id.getText().toString());
                Integer post_qty = Integer.parseInt(dialog_txt_qty.getText().toString());
                Integer post_price = Integer.parseInt(dialog_txt_price.getText().toString());
                Boolean change_qty = false;
                Boolean change_price = false;
                if (!current_item_qty.equals(post_qty)) {
                    change_qty = true;
                }
                if (!current_item_price.equals(post_price)) {
                    change_price = true;
                }
                if (change_qty || change_price) {
                    try {
                        po_items_data.getJSONObject(index).put("quantity", post_qty);
                        po_items_data.getJSONObject(index).put("price", post_price);

                        reBuildThePOItems();

                        // then reloading the item list
                        reloadListItems();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                dialog.hide();
            }
        });

        btn_dialog_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer index = Integer.parseInt(dialog_stack_id.getText().toString());
                try {
                    po_item_deleted.add(po_items_data.getJSONObject(index).getString("id"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                po_items_data.remove(index);
                reBuildThePOItems();

                // then reloading the item list
                reloadListItems();
                dialog.hide();
            }
        });
    }

    public void cancelUpdate(View view) {
        mViewPager.setCurrentItem(0, true);
    }

    /**
     * Adding the PO items
     * @param view
     */
    public void addItem(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PurchaseReportActivity.this);
        View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_purchase, null);

        Spinner spinner_list_product =  (Spinner) mView.findViewById(R.id.list_product);
        // init the spinner of list product
        ArrayAdapter<String> pAdapter = new ArrayAdapter<String>(
                PurchaseReportActivity.this,
                R.layout.spinner_item, product_list);
        spinner_list_product.setAdapter(pAdapter);

        builder.setView(mView);
        AlertDialog dialog = builder.create();

        // submit, cancel, and delete button trigger
        trigger_add_dialog_button(mView, dialog);

        dialog.show();
    }

    /**
     * Handle add item dialog button
     * @param mView
     * @param dialog
     */
    private void trigger_add_dialog_button(final View mView, final Dialog dialog) {
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });

        Button btn_dialog_submit = (Button) mView.findViewById(R.id.btn_dialog_submit);
        btn_dialog_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Spinner spinner_list_product = (Spinner) mView.findViewById(R.id.list_product);
                EditText txt_qty = (EditText) mView.findViewById(R.id.txt_qty);
                EditText txt_price = (EditText) mView.findViewById(R.id.txt_price);

                int has_error = 0;
                if (spinner_list_product.getSelectedItem().toString().length() <= 0) {
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

                    // checking whether the item already on stack
                    try {
                        for (int n = 0; n < po_items_data.length(); n++) {
                            JSONObject data_n = new JSONObject(po_items_data.getString(n));
                            String item_title = data_n.getString("title");
                            if (item_title.equals(spinner_list_product.getSelectedItem().toString())) {
                                has_error = has_error + 1;
                                Toast.makeText(
                                        getApplicationContext(),
                                        spinner_list_product.getSelectedItem().toString() +" sudah ada di daftar barang.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (has_error == 0) {
                        JSONObject additional_data = new JSONObject();
                        try {
                            int index = spinner_list_product.getSelectedItemPosition();

                            additional_data.put("id", 0);
                            additional_data.put("product_id", product_list_data.getJSONObject(index).getString("id"));
                            additional_data.put("title", spinner_list_product.getSelectedItem().toString());
                            additional_data.put("quantity", Integer.parseInt(txt_qty.getText().toString()));
                            additional_data.put("unit", product_list_data.getJSONObject(index).getString("unit"));
                            additional_data.put("price", Integer.parseInt(txt_price.getText().toString()));
                            additional_data.put("product_name", spinner_list_product.getSelectedItem().toString());

                            po_items_data.put(additional_data);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        reBuildThePOItems();
                    }
                    dialog.hide();
                }
            }
        });
    }

    Map<String, String> post_params = new HashMap<String, String>();

    public void executingUpdatePO(View view) {
        int supplier_index = supplier_name.getSelectedItemPosition();

        try {
            post_params.put("id", po_detail.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        post_params.put("supplier_name", supplier_name.getSelectedItem().toString());
        post_params.put("wh_group_name", wh_group_name.getSelectedItem().toString());
        post_params.put("shipment_name", shipment_name.getSelectedItem().toString());
        post_params.put("due_date", due_date.getText().toString());
        post_params.put("admin_id", sharedpreferences.getString("id", null));

        String str_qtys = "";
        String str_prices = "";
        try {
            for(int n = 0; n < po_items_data.length(); n++)
            {
                JSONObject data_n = new JSONObject(po_items_data.getString(n));
                String str_qty = data_n.getString("product_id")+ "," + data_n.get("quantity");
                String str_price = data_n.getString("product_id")+ "," + data_n.get("price");

                if (str_qtys.length() > 0)
                    str_qtys += "-" + str_qty;
                else
                    str_qtys += str_qty;

                if (str_prices.length() > 0)
                    str_prices += "-" + str_price;
                else
                    str_prices += str_price;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (str_qtys.length() > 0) {
            post_params.put("items", str_qtys);
        }

        if (str_prices.length() > 0) {
            post_params.put("prices", str_prices);
        }

        Log.e(TAG, "post_params : "+ post_params.toString());

        String url = Server.URL + "purchase/update?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.POST,
                url,
                post_params, true,
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
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void executingCancelPO() {
        Map<String, String> delete_params = new HashMap<String, String>();

        try {
            delete_params.put("id", po_detail.getString("id"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        delete_params.put("admin_id", sharedpreferences.getString("id", null));
        //Log.e(TAG, "delete_params : "+ delete_params.toString());

        String url = Server.URL + "purchase/delete?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.POST,
                url,
                delete_params, true,
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
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}

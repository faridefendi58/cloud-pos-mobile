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
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
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

/**
 * Created by mrsvette on 02/04/18.
 */

public class InventoryActivity extends MainActivity {
    Intent intent;
    ProgressDialog pDialog;
    int success;

    private static final String TAG = NotificationActivity.class.getSimpleName();
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
    private EditText inputSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_inventory);
        buildMenu();

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // delay build the form after tabs fully finished
                // define the product list
                //ArrayList list_products = get_list_product();
                buildTheProductList();
                inputSearch = (EditText) findViewById(R.id.inputSearch);
                buildTheWHList();
                buildTheList(null);
                //define date picker
                initDatePicker();
            }
        }, 1000);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                /**
                 * Enabling Search Filter
                 * */
                inputSearch.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                        // When user changed the Text
                        arrayAdapter.getFilter().filter(cs);
                    }

                    @Override
                    public void afterTextChanged(Editable editable) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                                  int arg3) {
                        // TODO Auto-generated method stub

                    }
                });
            }
        }, 2000);
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

    public static class TabFragment1 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_inventory_1, container, false);
        }
    }

    public static class TabFragment2 extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return inflater.inflate(R.layout.tab_fragment_inventory_2, container, false);
        }
    }

    private ArrayList get_list_product() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String wh_url = Server.URL + "product/list?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                wh_url, params, false,
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

        strReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

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

    private void buildTheCardView(final RelativeLayout mRelativeLayout, String title, Integer i)
    {
        Context mContext = InventoryActivity.this;
        // Initialize a new CardView
        CardView card = new CardView(mContext);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        //params.width = 180;
        params.height = 100;
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.setMargins(10, 10 + (i*110), 10, 10);

        card.setLayoutParams(params);

        // Set CardView corner radius
        card.setRadius(9);

        // Set cardView content padding
        card.setContentPadding(15, 25, 15, 25);

        // Set a background color for CardView
        card.setCardBackgroundColor(Color.parseColor("#FFC6D6C3"));
        //card.setCardBackgroundColor(null);

        // Set the CardView maximum elevation
        card.setMaxCardElevation(1);

        // Set CardView elevation
        card.setCardElevation(1);
        //card.setMinimumWidth(180);

        card.setFitsSystemWindows(true);
        card.setClickable(true);

        // Initialize a new TextView to put in CardView
        TextView tv = new TextView(mContext);
        tv.setLayoutParams(params);
        tv.setText(title);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        tv.setTextColor(Color.RED);

        // Initialize image icon
        ImageView iv = new ImageView(mContext);
        iv.setLayoutParams(params);
        iv.setImageDrawable(getResources().getDrawable(R.drawable.ic_arrow_back_black_24dp));

        // Put the TextView in CardView
        card.addView(tv);
        card.addView(iv);

        // Finally, add the CardView in root layout
        mRelativeLayout.addView(card);
    }

    // Listview Adapter
    ArrayAdapter<String> arrayAdapter;
    final ArrayList<String> product_items = new ArrayList<String>();
    Map<String, String> product_names = new HashMap<String, String>();

    ArrayList<String> cart_stack = new ArrayList<String>();
    ArrayList<String> cart_stack_qty = new ArrayList<String>();

    String cart_stack_str = "";

    private void buildTheProductList()
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");

        _string_request(
                Request.Method.GET,
                Server.URL + "product/list?api-key=" + Server.API_KEY,
                params,
                false,
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
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    product_items.add(data_n.getString("title"));
                                    product_names.put(data_n.getString("title"), data_n.getString("id"));
                                }

                                ListView listView = (ListView)findViewById(R.id.list_available_product);
                                arrayAdapter = new ArrayAdapter<String>(
                                        InventoryActivity.this,
                                        android.R.layout.simple_list_item_activated_1,
                                        product_items);

                                listView.setAdapter(arrayAdapter);

                                itemListener(listView);
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
                //Log.e(TAG, "Choosen : "+ product_items.get(i));
                AlertDialog.Builder builder = new AlertDialog.Builder(InventoryActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dialog_add_item_inventory, null);

                Object listItem = list.getItemAtPosition(i);

                TextView txt_product_name = (TextView) mView.findViewById(R.id.txt_product_name);
                txt_product_name.setText(listItem.toString());

                builder.setView(mView);
                final AlertDialog dialog = builder.create();

                // submit, cancel, and delete button trigger
                trigger_dialog_button(mView, dialog);

                dialog.show();
            }
        });
    }

    private void trigger_dialog_button(final View mView, final AlertDialog dialog) {
        // cancel method
        Button btn_dialog_cancel = (Button) mView.findViewById(R.id.btn_dialog_cancel);
        btn_dialog_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
            }
        });

        final TextView txt_product_name = (TextView) mView.findViewById(R.id.txt_product_name);
        final EditText txt_qty = (EditText) mView.findViewById(R.id.txt_qty);

        Button btn_dialog_submit = (Button) mView.findViewById(R.id.btn_dialog_submit);
        btn_dialog_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int has_error = 0;
                if (txt_product_name.getText().toString().length() <= 0) {
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

                if (cart_stack.contains(txt_product_name.getText().toString())) {
                    Toast.makeText(getApplicationContext(), txt_product_name.getText().toString()+ " pernah ditambahkan sebelumnya.", Toast.LENGTH_LONG).show();
                    has_error = has_error + 1;
                    dialog.hide();
                }

                if (has_error == 0) {
                    //Toast.makeText(getApplicationContext(), "Berhasil menambahkan " + txt_product_name.getText().toString(), Toast.LENGTH_LONG).show();
                    dialog.hide();

                    cart_stack.add(txt_product_name.getText().toString());
                    cart_stack_qty.add(txt_qty.getText().toString());

                    String stack_str = product_names.get(txt_product_name.getText().toString()) + "," + txt_qty.getText().toString();
                    if (cart_stack.size() > 1) {
                        cart_stack_str += "-"+ stack_str;
                    } else {
                        cart_stack_str += stack_str;
                    }

                    refreshTheTable();

                    Button btn_submit = (Button) findViewById(R.id.btn_submit);
                    btn_submit.setVisibility(View.VISIBLE);

                    TableLayout table_layout2 = (TableLayout) findViewById(R.id.table_layout2);
                    table_layout2.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void refreshTheTable()
    {
        final Integer idx = cart_stack.size();

        TableLayout table_layout = (TableLayout) findViewById(R.id.table_layout);
        TableRow row = new TableRow(InventoryActivity.this);

        TextView tv1 = new TextView(InventoryActivity.this);
        tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));

        tv1.setGravity(Gravity.CENTER_HORIZONTAL);
        tv1.setPadding(5, 15, 0, 15);
        tv1.setText("" + idx);
        row.addView(tv1);

        TextView wh = new TextView(InventoryActivity.this);
        wh.setLayoutParams(new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT));

        wh.setGravity(Gravity.LEFT);
        wh.setPadding(5, 15, 0, 15);

        wh.setText(cart_stack.get(idx-1));

        //table_layout.addView(row);
        row.addView(wh);

        EditText tv2 = new EditText(InventoryActivity.this);
        tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));

        tv2.setGravity(Gravity.CENTER_HORIZONTAL);
        tv2.setPadding(5, 15, 0, 15);
        tv2.setText(cart_stack_qty.get(idx-1));
        tv2.setInputType(InputType.TYPE_CLASS_NUMBER);
        tv2.setImeOptions(EditorInfo.IME_ACTION_DONE);
        tv2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                //Toast.makeText(getApplicationContext(), "Di pencet "+ i, Toast.LENGTH_LONG).show();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                System.out.println("afterTextChanged " + new String(editable.toString()));
                String new_value = new String(editable.toString());
                if (new_value.length() > 0) {
                    cart_stack_qty.set((idx-1), new String(editable.toString()));
                    cart_stack_str = "";
                    for(int i=0; i<cart_stack.size(); i++) {
                        String stack_str = product_names.get(cart_stack.get(i)) +","+ cart_stack_qty.get(i);
                        if (i == 0) {
                            cart_stack_str += stack_str;
                        } else {
                            cart_stack_str += "-"+ stack_str;
                        }
                    }
                    Log.e(TAG, "New stack become : "+ cart_stack_str);
                }
            }
        });
        row.addView(tv2);

        Button bt1 = new Button(InventoryActivity.this);
        bt1.setLayoutParams(new TableRow.LayoutParams(10, TableRow.LayoutParams.WRAP_CONTENT));
        bt1.setGravity(Gravity.CENTER);
        bt1.setPadding(20, 0, 0, 0);
        bt1.setBackgroundColor(Color.TRANSPARENT);
        bt1.setId(idx-1);

        Context ctx = InventoryActivity.this;
        Drawable image = ctx.getResources().getDrawable( R.drawable.ic_delete_forever_black_24dp );
        int h = image.getIntrinsicHeight();
        int w = image.getIntrinsicWidth();
        image.setBounds( 0, 0, w, h );
        bt1.setCompoundDrawables( image, null, null, null );
        row.addView(bt1);

        if ((idx % 2) == 0) {
            row.setBackgroundColor(Color.parseColor("#ebebeb"));
        }
        table_layout.addView(row);

        row.setId(idx-1);

        if (idx > 0) {
            TableRow no_data = (TableRow) findViewById(R.id.no_data);
            no_data.setVisibility(View.GONE);
        }

        bt1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TableRow tbl_row = (TableRow) findViewById(v.getId());
                tbl_row.setVisibility(View.GONE);

                cart_stack.set(v.getId(), null);
                cart_stack_qty.set(v.getId(), null);
                cart_stack_str = "";
                for(int i=0; i<cart_stack.size(); i++) {
                    String stack_str = product_names.get(cart_stack.get(i)) +","+ cart_stack_qty.get(i);
                    if (i == 0) {
                        cart_stack_str += stack_str;
                    } else {
                        cart_stack_str += "-"+ stack_str;
                    }
                }
                //Toast.makeText(getApplicationContext(), "Di pencet "+ cart_stack.toString(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void buildTheWHList() {
        ArrayList<String> assigned_whs = new ArrayList<String>();

        String roles = sharedpreferences.getString(TAG_ROLES, null);
        try {
            JSONObject jsonObject = new JSONObject(roles);
            JSONArray keys = jsonObject.names();

            for (int i = 0; i < keys.length (); ++i) {
                String key = keys.getString (i); // Here's your key
                String value = jsonObject.getString (key); // Here's your value
                JSONObject data_n = jsonObject.getJSONObject(key);
                assigned_whs.add(data_n.getString("warehouse_name"));

            }

            Spinner wh_list = (Spinner) findViewById(R.id.wh_list);
            ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(
                    InventoryActivity.this,
                    R.layout.spinner_item, assigned_whs
            );
            wh_list.setAdapter(whAdapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        finish();
        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    public void createNewIssue(View view) {
        final Spinner wh_list = (Spinner) findViewById(R.id.wh_list);
        TextView txt_item_select = (TextView) findViewById(R.id.txt_item_select);
        EditText notes = (EditText) findViewById(R.id.notes);
        TextView effective_date = (TextView) findViewById(R.id.effective_date);

        Map<String, String> params = new HashMap<String, String>();
        params.put("items", cart_stack_str);
        params.put("warehouse_name", wh_list.getSelectedItem().toString());
        params.put("admin_id", sharedpreferences.getString("id", null));
        if (notes.getText().toString().length() > 0) {
            params.put("notes", notes.getText().toString());
        }
        if (effective_date.getText().toString().length() > 0) {
            params.put("effective_date", effective_date.getText().toString());
        }
        Log.e(TAG, "Params : " + params.toString());

        _string_request(
                Request.Method.POST,
                Server.URL + "inventory/create?api-key=" + Server.API_KEY,
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
                                String success_msg = "Transaksi berhasil disimpan";

                                Toast.makeText(getApplicationContext(),
                                        jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                            }

                            refresh();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    final ArrayList<String> list_ids = new ArrayList<String>();
    final ArrayList<String> list_issues = new ArrayList<String>();
    final Map<String, String> issue_origins = new HashMap<String, String>();

    private void buildTheList(final String i_number)
    {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("all_status", "1");

        final ArrayList<String> descs = new ArrayList<String>();
        _string_request(
                Request.Method.GET,
                Server.URL + "inventory/list?api-key=" + Server.API_KEY,
                params,
                true,
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
                                JSONArray details = jObj.getJSONArray("detail");
                                JSONObject items = jObj.getJSONObject("items");

                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject detail_n = new JSONObject(details.getString(n));
                                    String msg = detail_n.getString("created_at")+" : Dikeluarkan oleh " + origins.getString(data.getString(n)) + ".";
                                    String notes = "-";
                                    if (detail_n.getString("notes").length() > 0) {
                                        msg += " Alasan dan tujuan "+ detail_n.getString("notes");
                                        notes = detail_n.getString("notes");
                                    }
                                    if (!TextUtils.isEmpty(i_number) && data.toString().contains(i_number)) {
                                        if (data.getString(n).equals(i_number)) {
                                            list_ids.add(detail_n.getString("status"));
                                            list_issues.add(data.getString(n));
                                            issue_origins.put(data.getString(n), origins.getString(data.getString(n)));
                                            descs.add(msg);
                                        }
                                    } else {
                                        list_ids.add(detail_n.getString("status"));
                                        list_issues.add(data.getString(n));
                                        issue_origins.put(data.getString(n), origins.getString(data.getString(n)));
                                        descs.add(msg);
                                    }

                                    TableLayout table_layout = (TableLayout) findViewById(R.id.table_arsip);
                                    TableRow row = new TableRow(InventoryActivity.this);

                                    TextView tv1 = new TextView(InventoryActivity.this);
                                    tv1.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));

                                    tv1.setGravity(Gravity.CENTER_HORIZONTAL);
                                    tv1.setPadding(5, 15, 0, 15);
                                    tv1.setText("" + (n+1));
                                    row.addView(tv1);

                                    TextView wh = new TextView(InventoryActivity.this);
                                    wh.setLayoutParams(new TableRow.LayoutParams(
                                            TableRow.LayoutParams.WRAP_CONTENT,
                                            TableRow.LayoutParams.WRAP_CONTENT));

                                    wh.setGravity(Gravity.LEFT);
                                    wh.setPadding(5, 15, 0, 15);

                                    wh.setText(origins.getString(data.getString(n)));

                                    row.addView(wh);

                                    TextView it = new TextView(InventoryActivity.this);
                                    it.setLayoutParams(new TableRow.LayoutParams(
                                            TableRow.LayoutParams.WRAP_CONTENT,
                                            TableRow.LayoutParams.WRAP_CONTENT));

                                    it.setGravity(Gravity.LEFT);
                                    it.setPadding(5, 15, 0, 15);
                                    it.setText(items.getString(data.getString(n)));
                                    it.setMaxWidth(200);
                                    row.addView(it);

                                    TextView tv2 = new TextView(InventoryActivity.this);
                                    tv2.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT));

                                    tv2.setGravity(Gravity.CENTER_HORIZONTAL);
                                    tv2.setPadding(5, 15, 0, 15);
                                    tv2.setText(notes);
                                    tv2.setMaxWidth(200);
                                    row.addView(tv2);

                                    TextView tv3 = new TextView(InventoryActivity.this);
                                    tv3.setLayoutParams(new TableRow.LayoutParams(
                                            TableRow.LayoutParams.WRAP_CONTENT,
                                            TableRow.LayoutParams.WRAP_CONTENT));

                                    tv3.setGravity(Gravity.LEFT);
                                    tv3.setPadding(5, 15, 0, 15);

                                    tv3.setText(AppController.parseDate(detail_n.getString("created_at"), "dd/MM/yyyy"));

                                    row.addView(tv3);

                                    if ((n % 2) > 0) {
                                        row.setBackgroundColor(Color.parseColor("#ebebeb"));
                                    }
                                    table_layout.addView(row);

                                    row.setId(n);
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
        dateView = (TextView) findViewById(R.id.effective_date);
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
}

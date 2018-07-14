package com.slightsite.ucokinventory;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.LayoutRes;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    Button btn_logout;
    TextView txt_id, txt_username, txt_full_name;
    String id, username, full_name;
    SharedPreferences sharedpreferences;

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    public static final String TAG_ID = "id";
    public static final String TAG_USERNAME = "username";
    public static final String TAG_NAME = "name";
    public final static String TAG_IS_ADMIN = "is_admin";
    public final static String TAG_IS_PIC = "is_pic";
    public final static String TAG_ROLES = "roles";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        Boolean is_admin = Boolean.valueOf(sharedpreferences.getString(TAG_IS_ADMIN, null));
        Boolean is_pic = Boolean.valueOf(sharedpreferences.getString(TAG_IS_PIC, null));

        if (is_admin) {
            setDinamicContent(R.layout.app_bar_main);
        } else {
            setDinamicContent(R.layout.app_bar_main_staff);
        }

        buildMenu();

        // customize dashboard according to the roles
        buildDashboard();

        // dashboard receipt menu
        LinearLayout dashboard_menu1 = (LinearLayout) findViewById(R.id.dashboard_menu1);
        dashboard_menu1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ReceiptActivity.class);
                startActivity(intent);
            }
        });
        // dashboard transfer menu
        LinearLayout dashboard_menu2 = (LinearLayout) findViewById(R.id.dashboard_menu2);
        dashboard_menu2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), TransferActivity.class);
                startActivity(intent);
            }
        });
        // dashboard purchase menu
        LinearLayout dashboard_menu3 = (LinearLayout) findViewById(R.id.dashboard_menu3);
        dashboard_menu3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PurchaseActivity.class);
                startActivity(intent);
            }
        });
        // dashboard stock menu
        LinearLayout dashboard_menu4 = (LinearLayout) findViewById(R.id.dashboard_menu4);
        dashboard_menu4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), StockActivity.class);
                startActivity(intent);
            }
        });
        // dashboard inventory issue menu
        LinearLayout dashboard_menu5 = (LinearLayout) findViewById(R.id.dashboard_menu5);
        dashboard_menu5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
                startActivity(intent);
            }
        });
        // dashboard notification menu
        LinearLayout dashboard_menu6 = (LinearLayout) findViewById(R.id.dashboard_menu6);
        dashboard_menu6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NotificationActivity.class);
                startActivity(intent);
            }
        });
        // dashboard delivery menu
        LinearLayout dashboard_menu7 = (LinearLayout) findViewById(R.id.dashboard_menu7);
        dashboard_menu7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DeliveryActivity.class);
                startActivity(intent);
            }
        });
    }

    public void setDinamicContent(@LayoutRes int app_bar) {
        LinearLayout dynamicContent = (LinearLayout) findViewById(R.id.dynamic_content);

        View wizardView = getLayoutInflater()
                .inflate(app_bar, dynamicContent, false);

        dynamicContent.addView(wizardView);
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

        //set the access list for dashboard menu roles
        build_the_access();

        //block the unnecessary menu
        if (has_purchase_receipt_access <= 0) {
            navigationView.getMenu().findItem(R.id.nav_stock_in_container).setVisible(false);
        }
        if (has_transfer_issue_access <= 0) {
            navigationView.getMenu().findItem(R.id.nav_transfer_out).setVisible(false);
        }
        if (has_inventory_issue_access <= 0) {
            navigationView.getMenu().findItem(R.id.nav_inventory_out).setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent=new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        } else if (id == R.id.nav_stock_in) {
            Intent intent = new Intent(getApplicationContext(), ReceiptActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_transfer_out) {
            Intent intent = new Intent(getApplicationContext(), TransferActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_inventory_out) {
            Intent intent = new Intent(getApplicationContext(), InventoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_profile) {

        } else if (id == R.id.nav_logout) {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putBoolean(LoginActivity.session_status, false);
            editor.putString(TAG_ID, null);
            editor.putString(TAG_USERNAME, null);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            finish();
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void buildDashboard() {
        Boolean is_admin = Boolean.valueOf(sharedpreferences.getString(TAG_IS_ADMIN, null));
        Boolean is_pic = Boolean.valueOf(sharedpreferences.getString(TAG_IS_PIC, null));

        if (!is_admin || !is_pic) {
            // Check stock menu
            CardView stock_menu_wrapper = (CardView) findViewById(R.id.stock_menu_wrapper);
            stock_menu_wrapper.setVisibility(View.GONE);
        }

        // just dump
        //Log.e(TAG, "Is Admin : " + is_admin );
        //Log.e(TAG, "PIC : " + is_pic );
        //Log.e(TAG, "List Assigned WH : " + get_list_assigned_wh().toString());

        // notif counter
        String className = this.getClass().getSimpleName();
        if (className.equals(TAG)) {
            set_notification_counter();
        }

        if (has_purchase_order_access <= 0) {
            CardView dashboard_menu3_container = (CardView) findViewById(R.id.menu3_container);
            dashboard_menu3_container.setVisibility(View.GONE);
        }
        if (has_purchase_receipt_access <= 0) {
            CardView menu1_container = (CardView) findViewById(R.id.menu1_container);
            menu1_container.setVisibility(View.GONE);
        }
        if (has_transfer_issue_access <= 0) {
            CardView menu2_container = (CardView) findViewById(R.id.menu2_container);
            menu2_container.setVisibility(View.GONE);
        }
        if (has_inventory_issue_access <= 0) {
            CardView menu5_container = (CardView) findViewById(R.id.menu5_container);
            menu5_container.setVisibility(View.GONE);
        }
        if (has_delivery_order_access <= 0) {
            CardView menu7_container = (CardView) findViewById(R.id.menu7_container);
            menu7_container.setVisibility(View.GONE);
        }
    }

    /**
     * Geting the list assigned warehouse
     * @return
     */
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

    public void set_notice(String title, String message)
    {
        /*final Handler ha=new Handler();
                ha.postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        //call function
                        set_notice();
                        ha.postDelayed(this, 10000);
                    }
                }, 10000);*/
        Intent intent = new Intent(MainActivity.this, NotificationActivity.class);
        intent.putExtra("msg", message);
        intent.setAction(Long.toString(System.currentTimeMillis()));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(MainActivity.this, 0,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_notifications_24dp)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setColor(getResources().getColor(R.color.yellow))
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(alarmSound);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(1, mBuilder.build());
    }

    int success;

    public void set_notification_counter()
    {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);

        final ArrayList<String> descs = new ArrayList<String>();
        _str_request(
                Request.Method.GET,
                Server.URL + "notification/count?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Notif Response: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                TextView badge_notification_counter = (TextView) findViewById(R.id.badge_notification_counter);
                                int count = jObj.getInt("count");
                                if (count > 0) {
                                    badge_notification_counter.setText(jObj.getString("count"));
                                    badge_notification_counter.setVisibility(View.VISIBLE);

                                    // also set popup notif notification
                                    String notif_message = sharedpreferences.getString("notif_message", null);
                                    SharedPreferences.Editor editor = sharedpreferences.edit();
                                    Boolean notice = false;
                                    if (!TextUtils.isEmpty(notif_message)) {
                                        if (!notif_message.equals(jObj.getString("message"))) {
                                            notice = true;
                                        }
                                    } else {
                                        notice = true;
                                    }

                                    if (notice) {
                                        set_notice(getString(R.string.app_name), jObj.getString("message"));
                                        editor.putString("notif_message", jObj.getString("message"));
                                        editor.commit();
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    ProgressDialog pDialog;

    public void _str_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
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

    private interface VolleyCallback{
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

    Integer has_purchase_order_access = 0;
    Integer has_purchase_receipt_access = 0;
    Integer has_transfer_issue_access = 0;
    Integer has_inventory_issue_access = 0;
    Integer has_delivery_order_access = 0;

    public void build_the_access() {
        String roles = sharedpreferences.getString(TAG_ROLES, null);
        try {
            JSONObject jsonObject = new JSONObject(roles);
            JSONArray keys = jsonObject.names();

            for (int i = 0; i < keys.length (); ++i) {
                String key = keys.getString (i); // Here's your key
                String value = jsonObject.getString (key); // Here's your value
                JSONObject data_n = jsonObject.getJSONObject(key);
                if (data_n.has("roles")) {
                    JSONObject jsonRoles = new JSONObject(data_n.getString("roles"));
                    if (jsonRoles.has("purchase_order")) {
                        has_purchase_order_access = has_purchase_order_access + 1;
                    }
                    if (jsonRoles.has("purchase_receipt")) {
                        has_purchase_receipt_access = has_purchase_receipt_access + 1;
                    }
                    if (jsonRoles.has("transfer_issue")) {
                        has_transfer_issue_access = has_transfer_issue_access + 1;
                    }
                    if (jsonRoles.has("inventory_issue")) {
                        has_inventory_issue_access = has_inventory_issue_access + 1;
                    }
                    if (jsonRoles.has("delivery_order")) {
                        has_delivery_order_access = has_delivery_order_access + 1;
                    }
                }
            }
            //Log.e(TAG, "Has PO Access : "+ has_purchase_order_access.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

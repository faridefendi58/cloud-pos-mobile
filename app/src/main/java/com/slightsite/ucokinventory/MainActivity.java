package com.slightsite.ucokinventory;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.LayoutRes;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;

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

        setDinamicContent(R.layout.app_bar_main);
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
        Log.e(TAG, "Is Admin : " + is_admin );
        Log.e(TAG, "PIC : " + is_pic );
        Log.e(TAG, "List Assigned WH : " + get_list_assigned_wh().toString());
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
}

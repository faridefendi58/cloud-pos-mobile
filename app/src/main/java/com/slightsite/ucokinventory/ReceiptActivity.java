package com.slightsite.ucokinventory;

/**
 * Created by mrsvette on 13/03/18.
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

public class ReceiptActivity extends MainActivity {
    Intent intent;

    private String url = Server.URL + "user/register?api-key=" + Server.API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_receipt);
        buildMenu();
    }

    @Override
    public void onBackPressed() {
        intent = new Intent(ReceiptActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }
}

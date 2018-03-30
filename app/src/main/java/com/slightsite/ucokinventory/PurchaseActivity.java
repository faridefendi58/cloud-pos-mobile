package com.slightsite.ucokinventory;

import android.os.Bundle;

/**
 * Created by mrsvette on 28/03/18.
 */

public class PurchaseActivity extends MainActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setDinamicContent(R.layout.app_bar_purchase);
        buildMenu();

    }
}

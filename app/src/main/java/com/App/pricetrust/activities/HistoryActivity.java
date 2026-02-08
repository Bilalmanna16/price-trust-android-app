package com.App.pricetrust.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.App.pricetrust.R;
import com.App.pricetrust.database.DBHelper;

import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        ListView listView = findViewById(R.id.listViewHistory);

        DBHelper dbHelper = new DBHelper(this);
        List<String> historyList = dbHelper.getAllPriceEntries();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                historyList
        );

        listView.setAdapter(adapter);
    }
}

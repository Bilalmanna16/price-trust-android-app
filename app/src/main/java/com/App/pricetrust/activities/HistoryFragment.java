package com.App.pricetrust;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.App.pricetrust.database.DBHelper;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 🔥 Toolbar back
        MaterialToolbar toolbar = view.findViewById(R.id.historyToolbar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        // 🔥 ListView
        ListView listView = view.findViewById(R.id.listViewHistory);

        DBHelper db = new DBHelper(requireContext());
        List<String> data = db.getAllPriceEntries();

        if (data == null || data.isEmpty()) {
            data = new ArrayList<>();
            data.add("No history available yet");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                data
        );

        listView.setAdapter(adapter);
    }
}
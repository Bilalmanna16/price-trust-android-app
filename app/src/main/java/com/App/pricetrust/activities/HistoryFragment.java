package com.App.pricetrust;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.App.pricetrust.database.DBHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class HistoryFragment extends Fragment {

    public HistoryFragment() {
        super(R.layout.fragment_history);
    }

    private List<String> data;
    private DBHelper db;
    private ListView listView;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialToolbar toolbar = view.findViewById(R.id.historyToolbar);
        toolbar.setNavigationOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        listView = view.findViewById(R.id.listViewHistory);
        db = new DBHelper(requireContext());

        loadData();
    }

    private void loadData() {
        data = db.getAllPriceEntries();

        if (data == null || data.isEmpty()) {
            data = new ArrayList<>();
            data.add("No history available yet");
        }

        listView.setAdapter(new HistoryAdapter());
    }

    // 🔥 CUSTOM ADAPTER
    private class HistoryAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return data.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_history, parent, false);

            TextView tvItem = view.findViewById(R.id.tvItem);
            MaterialButton btnDelete = view.findViewById(R.id.btnDelete);

            String item = data.get(position);
            tvItem.setText(item);

            // 🔥 SAFE DELETE
            btnDelete.setOnClickListener(v -> {

                if (item.equals("No history available yet")) return;

                try {

                    String[] parts = item.split("- ₹");

                    String name = parts[0].trim();
                    double price = Double.parseDouble(parts[1].trim());

                    db.deleteEntry(name, price);

                    data.remove(position);
                    notifyDataSetChanged();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            return view;
        }
    }
}
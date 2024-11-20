package com.example.grocery;

import android.widget.Filter;

import com.example.grocery.adapters.AdapterProductSeller;
import com.example.grocery.models.ModelProduct;

import java.util.ArrayList;
import java.util.List;

public class FilterProduct extends Filter {
    private AdapterProductSeller adapter;
    private ArrayList<ModelProduct> filterList;

    public FilterProduct(AdapterProductSeller adapter, ArrayList<ModelProduct> filterList) {
        this.adapter = adapter;
        this.filterList = filterList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        FilterResults results = new FilterResults();
        // Validate data for search query
        if (constraint != null && constraint.length() > 0) {

            // Change the uppercase to make case insensitive
            constraint = constraint.toString().toUpperCase();
            ArrayList<ModelProduct> filteredModels = new ArrayList<>();
            for (int i = 0; i < filterList.size(); i++) {
                // Check search by title and category
                if (filterList.get(i).getProductTitle().toUpperCase().contains(constraint) ||
                        filterList.get(i).getProductCategory().toUpperCase().contains(constraint)) {
                    // Add filtered data to list
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count = filteredModels.size();
            results.values = filteredModels;
        } else {
            // Search field empty, not searching, return original/all/complete list
            results.count = filterList.size();
            results.values = filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapter.productList = (ArrayList<ModelProduct>) results.values; // Use the filtered list
        // Refresh adapter
        adapter.notifyDataSetChanged();
    }
}
//checked code
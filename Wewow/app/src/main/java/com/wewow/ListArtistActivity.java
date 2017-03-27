package com.wewow;


import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.wewow.utils.Utils;

/**
 * Created by iris on 17/3/24.
 */
public class ListArtistActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setActivityToBeFullscreen(this);

        setContentView(R.layout.activity_list_artist);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.toolbar, menu);
        MenuItem menuItem = menu.findItem(R.id.search);
        menuItem.setVisible(true);

        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        final SearchView searchView =
                (SearchView) menu.findItem(R.id.search).getActionView();

        searchView.setQueryHint(getResources().getString(R.string.search_hint));


        ((ImageView) searchView.findViewById(android.support.v7.appcompat.R.id.search_button)).setImageResource(R.drawable.selector_btn_search);


        final String[] testStrings = getResources().getStringArray(R.array.test_array);
//        int completeTextId = searchView.getContext().getResources().getIdentifier("android:id/search_src_text", null, null);
//        AutoCompleteTextView completeText = (AutoCompleteTextView) searchView
//                .findViewById(completeTextId) ;


        AutoCompleteTextView completeText = (SearchView.SearchAutoComplete) searchView.findViewById(R.id.search_src_text);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item_search, R.id.text, testStrings);

        completeText.setAdapter(adapter);
        completeText.setTextColor(getResources().getColor(R.color.search_text_view_color));
        completeText.setHintTextColor(getResources().getColor(R.color.search_text_view_hint_color));
        completeText.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                searchView.setQuery(testStrings[position], true);
            }
        });

        completeText.setThreshold(0);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Toast.makeText(ListArtistActivity.this, query, Toast.LENGTH_SHORT).show();
//                LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
//                layout.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(getComponentName()));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.search) {
//            LinearLayout layout = (LinearLayout) findViewById(R.id.layoutCover);
//            layout.setVisibility(View.VISIBLE);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

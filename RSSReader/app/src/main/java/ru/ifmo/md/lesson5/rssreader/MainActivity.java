package ru.ifmo.md.lesson5.rssreader;

import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends ListActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {
    private EditText mEditTextAdd;
    private SimpleCursorAdapter mAdapter;

    private static final int LOADER_RSS = 1;
    private static final String EXTRA_RSS_ID = "RSS_ID";

    private RssManager mManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mManager = RssManager.get(this);

        setupViews();
    }

    private void setupViews() {
        mEditTextAdd = (EditText) findViewById(R.id.editText_addUrl);

        mEditTextAdd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String potentialUrl = mEditTextAdd.getText().toString();
                if (Patterns.WEB_URL.matcher(potentialUrl).matches()) {
                    //TODO: constants for colors, make 9-patch
                    mEditTextAdd.setBackgroundColor(Color.argb(64, 0x5C, 0x85, 0x5C));
                } else {
                    mEditTextAdd.setBackgroundColor(Color.argb(64, 0xD9, 0x53, 0x4F));
                }
            }
        });

        mEditTextAdd.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == getResources().getInteger(R.integer.actionAdd)) {
                    String potentialUrl = mEditTextAdd.getText().toString();
                    if (Patterns.WEB_URL.matcher(potentialUrl).matches()) {
                        addRss(potentialUrl);
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.bad_url), Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
                return false;
            }
        });

        mAdapter = new SimpleCursorAdapter(
                this,
                android.R.layout.simple_list_item_1,
                null,
                new String[]{RssDatabaseHelper.COLUMN_RSS_URL},
                new int[]{android.R.id.text1},
                0
        );

        setListAdapter(mAdapter);
        registerForContextMenu(findViewById(android.R.id.list));

        getLoaderManager().initLoader(LOADER_RSS, null, this);

    }

    @Override
    protected void onListItemClick(ListView lv, View v, int position, long id) {
        Cursor cursor = (Cursor) mAdapter.getItem(position);
        long rssId = cursor.getInt(cursor.getColumnIndex("_id"));
        Intent intent = new Intent(this, RssActivity.class);
        intent.putExtra(EXTRA_RSS_ID, rssId);
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getMenuInflater().inflate(R.menu.rss_context, menu);
        super.onCreateContextMenu(menu, v, menuInfo);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo acmi =
                (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        long id = acmi.id;
        Log.d("TAG", "id: " + id);
        switch (item.getItemId()) {
            case R.id.delete:
                mManager.deleteRss(id);
                getLoaderManager().getLoader(LOADER_RSS).forceLoad();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void addRss(String url) {
        Log.d("TAG", "Add url: " + url);
        RssItem rssItem = new RssItem();
        rssItem.setUrl(url);
        rssItem.setName(url);
        rssItem.setFavourite(0);
        long id = mManager.insertRss(rssItem);
        getLoaderManager().getLoader(LOADER_RSS).forceLoad();
        Log.d("TAG", "Rss id: " + id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new RssLoader(this, mManager);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

}

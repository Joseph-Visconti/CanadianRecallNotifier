package com.example.visconti.canadianrecall;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Intent iRecent = new Intent(this, RecentRecallsActivity.class);
        final Intent iSearch = new Intent(this, SearchRecallsActivity.class);

        Button btnRecentRecalls = (Button) findViewById(R.id.btnRecentRecalls);
        Button btnSearchRecalls = (Button) findViewById(R.id.btnSearchRecalls);

        btnRecentRecalls.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(iRecent);
            }
        });

        btnSearchRecalls.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(iSearch);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
}

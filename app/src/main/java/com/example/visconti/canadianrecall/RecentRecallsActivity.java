package com.example.visconti.canadianrecall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class RecentRecallsActivity extends ActionBarActivity {

    private static final String TAG_JSON_RESULTS = "results";
    private static final String TAG_CATEGORY_ALL = "ALL";
    private static final String TAG_CATEGORY_FOOD = "FOOD";
    private static final String TAG_CATEGORY_VEHICLES = "VEHICLE";
    private static final String TAG_CATEGORY_HEALTH = "HEALTH";
    private static final String TAG_CATEGORY_CPS = "CPS";
    private static final String TAG_RECALL_ID = "recallId";
    private static final String TAG_TITLE = "title";
    private static final String TAG_CATEGORY_TYPE = "category";
    private static final String TAG_DATE_PUBLISHED = "date_published";
    private static final String TAG_URL = "url";
    private static final String TAG_DEPARTMENT = "department";

    private String url = "http://healthycanadians.gc.ca/recall-alert-rappel-avis/api/recent/en";
    private String selectedCategory = TAG_CATEGORY_ALL;
    private ProgressDialog pDialog;

    JSONArray recalls = null;

    ArrayList<HashMap<String, String>> recallList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_recalls);

        ImageButton btnFood = (ImageButton) findViewById(R.id.btnFood);
        btnFood.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setSelected(TAG_CATEGORY_FOOD);
                new GetRecallList().execute();
            }
        });

        ImageButton btnVechicle = (ImageButton) findViewById(R.id.btnVechicle);
        btnVechicle.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setSelected(TAG_CATEGORY_VEHICLES);
                new GetRecallList().execute();
            }
        });

        ImageButton btnHealth = (ImageButton) findViewById(R.id.btnHealth);
        btnHealth.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setSelected(TAG_CATEGORY_HEALTH);
                new GetRecallList().execute();
            }
        });

        ImageButton btnConsumerProducts = (ImageButton) findViewById(R.id.btnConsumerProducts);
        btnConsumerProducts.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                setSelected(TAG_CATEGORY_CPS);
                new GetRecallList().execute();
            }
        });

        ListView lstRecalls = (ListView)findViewById(R.id.lstRecalls);
        lstRecalls.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String recallID = ((TextView) view.findViewById(R.id.recallID))
                        .getText().toString();

                Intent in = new Intent(getApplicationContext(), RecallDetailsActivity.class);
                in.putExtra(TAG_RECALL_ID, recallID);
                startActivity(in);
            }
        });

        new GetRecallList().execute();
    }

    private void setSelected(String selected)
    {
        ImageButton btnFood = (ImageButton) findViewById(R.id.btnFood);
        ImageButton btnVehicle = (ImageButton) findViewById(R.id.btnVechicle);
        ImageButton btnHealth = (ImageButton) findViewById(R.id.btnHealth);
        ImageButton btnConsumerProducts = (ImageButton) findViewById(R.id.btnConsumerProducts);

        if(selected.equals(selectedCategory))
        {
            selectedCategory = TAG_CATEGORY_ALL;
            btnFood.setSelected(false);
            btnVehicle.setSelected(false);
            btnHealth.setSelected(false);
            btnConsumerProducts.setSelected(false);
        }
        else if(selected.equals(TAG_CATEGORY_FOOD))
        {
            selectedCategory = selected;
            btnFood.setSelected(true);
            btnVehicle.setSelected(false);
            btnHealth.setSelected(false);
            btnConsumerProducts.setSelected(false);
        }
        else if(selected.equals(TAG_CATEGORY_VEHICLES))
        {
            selectedCategory = selected;
            btnFood.setSelected(false);
            btnVehicle.setSelected(true);
            btnHealth.setSelected(false);
            btnConsumerProducts.setSelected(false);
        }
        else if(selected.equals(TAG_CATEGORY_HEALTH))
        {
            selectedCategory = selected;
            btnFood.setSelected(false);
            btnVehicle.setSelected(false);
            btnHealth.setSelected(true);
            btnConsumerProducts.setSelected(false);
        }
        else if(selected.equals(TAG_CATEGORY_CPS))
        {
            selectedCategory = selected;
            btnFood.setSelected(false);
            btnVehicle.setSelected(false);
            btnHealth.setSelected(false);
            btnConsumerProducts.setSelected(true);
        }
    }

    private class GetRecallList extends AsyncTask<Void, Void, Void>
    {

        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pDialog = new ProgressDialog(RecentRecallsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();


            recallList = new ArrayList<HashMap<String, String>>();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            JSONHandler jh = new JSONHandler();

            String jsonStr = jh.makeJSONCall(url, JSONHandler.GET);

            if (jsonStr != null)
            {
                try
                {
                    JSONObject jsonObj = new JSONObject(jsonStr);

                    recalls = jsonObj.getJSONObject(TAG_JSON_RESULTS)
                            .getJSONArray(selectedCategory);

                    for (int i = 0; i < recalls.length(); i++)
                    {
                        JSONObject r = recalls.getJSONObject(i);

                        String id = r.getString(TAG_RECALL_ID);
                        String title = r.getString(TAG_TITLE);
                        String categoryType = r.getString(TAG_CATEGORY_TYPE);
                        String recallURL = r.getString(TAG_URL);
                        String datePublished = r.getString(TAG_DATE_PUBLISHED);

                        categoryType = categoryType.equals("[\"1\"]") ? "Food"
                                : categoryType.equals("[\"2\"]") ? "Vehicle"
                                : categoryType.equals("[\"3\"]") ? "Health"
                                : categoryType.equals("[\"4\"]") ? "Consumer Products"
                                : "";
                        datePublished = new SimpleDateFormat("MMM dd, yyyy").format(new Date(Long.valueOf(datePublished)*1000));

                        HashMap<String, String> recall = new HashMap<String, String>();
                        recall.put(TAG_RECALL_ID, id);
                        recall.put(TAG_TITLE, title);
                        recall.put(TAG_DEPARTMENT, "");
                        recall.put(TAG_CATEGORY_TYPE, categoryType);
                        recall.put(TAG_DATE_PUBLISHED, datePublished);
                        recall.put(TAG_URL, recallURL);

                        recallList.add(recall);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            } else
            {
                Log.e("JSONHandler", "Couldn't get any data from the url");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();

            ListView lv = (ListView)findViewById(R.id.lstRecalls);
            lv.setAdapter(new SimpleAdapter(
                    RecentRecallsActivity.this,
                    recallList,
                    R.layout.list_item_recall,
                    new String[]{TAG_RECALL_ID, TAG_TITLE, TAG_DATE_PUBLISHED, TAG_CATEGORY_TYPE},
                    new int[]{R.id.recallID, R.id.title, R.id.datePublished, R.id.category}
            ));
        }
    }
}

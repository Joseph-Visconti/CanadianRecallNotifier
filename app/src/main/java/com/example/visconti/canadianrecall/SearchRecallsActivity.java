package com.example.visconti.canadianrecall;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
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


public class SearchRecallsActivity extends ActionBarActivity {

    private static final String TAG_JSON_RESULTS = "results";
    private static final String TAG_JSON_RESULTS_COUNT = "results_count";
    private static final String TAG_RECALL_ID = "recallId";
    private static final String TAG_TITLE = "title";
    private static final String TAG_DEPARTMENT = "department";
    private static final String TAG_DATE_PUBLISHED = "date_published";
    private static final String TAG_CATEGORY_TYPE = "category";
    private static final String TAG_URL = "url";

    private String url = "";
    private String search = "";
    private String lang = "en";
    private int pageNumber = 1;
    private int numPerPage = 15;

    private int resultsCount = 0;

    private ProgressDialog pDialog;

    JSONArray recalls = null;

    ArrayList<HashMap<String, String>> recallList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_recalls);

        Button btnSearch = (Button)findViewById(R.id.btnSearch);
        btnSearch.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                EditText edtSearch = (EditText)findViewById(R.id.edtSearch);
                if(!edtSearch.getText().toString().isEmpty())
                {
                    pageNumber = 1;
                    resultsCount = 0;
                    search = edtSearch.getText().toString();
                    new SearchRecallList().execute();
                }
            }
        });

        Button btnPageForwards = (Button)findViewById(R.id.btnPageForwards);
        btnPageForwards.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                pageNumber++;
                new SearchRecallList().execute();
            }
        });

        Button btnPageBackwards = (Button)findViewById(R.id.btnPageBackwards);
        btnPageBackwards.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {
                pageNumber--;
                new SearchRecallList().execute();
            }
        });

        ListView lstRecalls = (ListView)findViewById(R.id.lstRecalls);
        lstRecalls.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String recallID = ((TextView) view.findViewById(R.id.recallID)).getText().toString();

                Intent in = new Intent(getApplicationContext(), RecallDetailsActivity.class);
                in.putExtra(TAG_RECALL_ID, recallID);
                startActivity(in);
            }
        });
    }

    private class SearchRecallList extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pDialog = new ProgressDialog(SearchRecallsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            recallList = new ArrayList<HashMap<String, String>>();

            url = "http://healthycanadians.gc.ca/recall-alert-rappel-avis/api/search?";
            url += "&search=" + search;
            url += "&lang=" + lang;
            url += "&lim=" + Integer.toString(numPerPage);
            url += "&off=" + Integer.toString((pageNumber-1)*numPerPage);
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
                    resultsCount = Integer.parseInt(jsonObj.getString(TAG_JSON_RESULTS_COUNT));
                    recalls = jsonObj.getJSONArray(TAG_JSON_RESULTS);

                    for (int i = 0; i < recalls.length(); i++)
                    {
                        JSONObject r = recalls.getJSONObject(i);

                        String id = r.getString(TAG_RECALL_ID);
                        String title = r.getString(TAG_TITLE);
                        String department = r.getString(TAG_DEPARTMENT);
                        String datePublished = r.getString(TAG_DATE_PUBLISHED);
                        String categoryType = r.getString(TAG_CATEGORY_TYPE);
                        String recallURL = r.getString(TAG_URL);

                        categoryType = categoryType.equals("[\"1\"]") ? "Food"
                                : categoryType.equals("[\"2\"]") ? "Vehicle"
                                : categoryType.equals("[\"3\"]") ? "Health"
                                : categoryType.equals("[\"4\"]") ? "Consumer Products"
                                : "";
                        datePublished = new SimpleDateFormat("MMM dd, yyyy").format(new Date(Long.valueOf(datePublished)*1000));

                        HashMap<String, String> recall = new HashMap<String, String>();
                        recall.put(TAG_RECALL_ID, id);
                        recall.put(TAG_TITLE, title);
                        recall.put(TAG_DEPARTMENT, department);
                        recall.put(TAG_DATE_PUBLISHED, datePublished);
                        recall.put(TAG_CATEGORY_TYPE, categoryType);
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
                    SearchRecallsActivity.this,
                    recallList,
                    R.layout.list_item_recall,
                    new String[]{TAG_RECALL_ID, TAG_TITLE, TAG_DATE_PUBLISHED, TAG_DEPARTMENT},
                    new int[]{R.id.recallID, R.id.title, R.id.datePublished,  R.id.category}
            ));

            TextView txtNumberResults = (TextView)findViewById(R.id.txtNumberResults);
            txtNumberResults.setText(resultsCount + " related recalls!");

            int totalPages = (int)(Math.ceil((double)resultsCount/(double)numPerPage));

            Button btnPageBackwards = (Button)findViewById(R.id.btnPageBackwards);
            if(pageNumber > 1){ btnPageBackwards.setEnabled(true); }
            else { btnPageBackwards.setEnabled(false); }

            Button btnPageForwards = (Button)findViewById(R.id.btnPageForwards);
            if(pageNumber < totalPages){ btnPageForwards.setEnabled(true); }
            else { btnPageForwards.setEnabled(false); }

            TextView txtPage = (TextView)findViewById(R.id.txtPage);
            txtPage.setText("Page " + (pageNumber) + " of " + totalPages);
        }
    }
}

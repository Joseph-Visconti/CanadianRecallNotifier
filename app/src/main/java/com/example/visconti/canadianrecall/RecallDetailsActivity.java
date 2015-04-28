package com.example.visconti.canadianrecall;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class RecallDetailsActivity extends ActionBarActivity {

    private static final String TAG_URL = "url";
    private static final String TAG_RECALL_ID = "recallId";
    private static final String TAG_TITLE = "title";
    private static final String TAG_START_DATE = "start_date";
    private static final String TAG_DATE_PUBLISHED = "date_published";
    private static final String TAG_CATEGORY = "category";
    private static final String TAG_PANELS = "panels";
    private static final String TAG_PANELS_NAME = "panelName";
    private static final String TAG_PANELS_TITLE = "title";
    private static final String TAG_PANELS_TEXT = "text";

    private String recallID = "";
    private String url = "";
    private String title = "";
    private String startDate;
    private String datePublished;
    private String category = "";
    ArrayList<HashMap<String, String>> panelList;

    private ProgressDialog pDialog;
    JSONArray details = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recall_details);

        recallID = getIntent().getStringExtra(TAG_RECALL_ID);

        new GetRecallDetails().execute();
    }

    private class PanelAdapter extends ArrayAdapter<HashMap<String, String>>
    {
        private PanelAdapter(ArrayList<HashMap<String, String>> list)
        {
            super(RecallDetailsActivity.this, R.layout.list_item_panel_details, list);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View row = convertView;
            if (row == null)
            {
                LayoutInflater inflater = RecallDetailsActivity.this.getLayoutInflater();
                row = inflater.inflate(R.layout.list_item_panel_details, parent, false);
            }

            HashMap<String, String> panel = getItem(position);
            ((TextView)row.findViewById(R.id.title)).setText(panel.get(TAG_PANELS_TITLE));
            ((TextView)row.findViewById(R.id.text)).setMovementMethod(LinkMovementMethod.getInstance());
            ((TextView)row.findViewById(R.id.text)).setText(Html.fromHtml(panel.get(TAG_PANELS_TEXT)));
            return row;
        }

    }

    private class GetRecallDetails extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {
            super.onPreExecute();

            pDialog = new ProgressDialog(RecallDetailsActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

            panelList = new ArrayList<HashMap<String, String>>();
        }

        @Override
        protected Void doInBackground(Void... arg0)
        {
            String baseURL = "http://healthycanadians.gc.ca/recall-alert-rappel-avis/api/";

            JSONHandler jh = new JSONHandler();
            String jsonStr = jh.makeJSONCall(baseURL + recallID + "/en", JSONHandler.GET);
            Log.d("VISCONTI", baseURL + recallID + "/en");
            if (jsonStr != null)
            {
                try
                {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    url = jsonObj.getString(TAG_URL);
                    recallID = jsonObj.getString(TAG_RECALL_ID);
                    title = jsonObj.getString(TAG_TITLE);
                    startDate = new SimpleDateFormat("MMM dd, yyyy").format(new Date(Long.valueOf(jsonObj.getString(TAG_START_DATE))*1000));
                    datePublished = new SimpleDateFormat("MMM dd, yyyy").format(new Date(Long.valueOf(jsonObj.getString(TAG_DATE_PUBLISHED))*1000));
                    category = jsonObj.getString(TAG_CATEGORY);
                    category = category.equals("[\"1\"]") ? "Food"
                            : category.equals("[\"2\"]") ? "Vehicle"
                            : category.equals("[\"3\"]") ? "Health"
                            : category.equals("[\"4\"]") ? "Consumer Products"
                            : "";
                    details = jsonObj.getJSONArray(TAG_PANELS);

                    for (int i = 0; i < details.length(); i++)
                    {
                        JSONObject r = details.getJSONObject(i);
                        String panelName = r.getString(TAG_PANELS_NAME);
                        String panelTitle = r.getString(TAG_PANELS_TITLE);
                        String panelText = r.getString(TAG_PANELS_TEXT);

                        HashMap<String, String> recall = new HashMap<String, String>();
                        recall.put(TAG_PANELS_NAME, panelName);
                        recall.put(TAG_PANELS_TITLE, panelTitle);
                        recall.put(TAG_PANELS_TEXT, panelText);

                        panelList.add(recall);
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

            ListView lv = (ListView)findViewById(R.id.lstPanels);
            lv.setAdapter(new PanelAdapter(panelList));

            TextView txtTitle = (TextView)findViewById(R.id.txtTitle);
            txtTitle.setText(title.replace("\n", ""));

            TextView txtRecallID = (TextView)findViewById(R.id.txtRecallID);
            txtRecallID.setText(recallID);

            TextView txtCategory = (TextView)findViewById(R.id.txtCategory);
            txtCategory.setText(category);

            TextView txtDatePublished = (TextView)findViewById(R.id.txtDatePublished);
            txtDatePublished.setText("Published : " + datePublished);

            TextView txtStartDate = (TextView)findViewById(R.id.txtStartDate);
            txtStartDate.setText("Start : " + startDate);

            TextView txtMoreInfo = (TextView)findViewById(R.id.txtMoreInfo);
            txtMoreInfo.setMovementMethod(LinkMovementMethod.getInstance());
            txtMoreInfo.setText(Html.fromHtml("<a href=\"" + url + "\">" + getText(R.string.link_more_info) + "</a>"));
        }
    }
}

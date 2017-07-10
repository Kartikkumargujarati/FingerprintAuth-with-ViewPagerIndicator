package com.kartik.newmalauzaiproject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.widget.Toast;

import com.viewpagerindicator.CirclePageIndicator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ImagesActivity extends FragmentActivity {
    ViewPager viewPager;
    SwipeRefreshLayout swipeRefreshLayout;
    List<Fragment> fragmentList;
    LoadImages loadImages;
    String flickrJsonUrl = "https://api.flickr.com/services/feeds/photos_public.gne?format=json";
    boolean showSpinner = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.images_activity);
        loadImages = new LoadImages();

        //Load the first set of images
        loadImages.execute(flickrJsonUrl);

        //swipe from top to refresh the images
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark, R.color.colorAccent);
        viewPager = (ViewPager) findViewById(R.id.viewPage);

        //using fragment Adapter
        FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getFragments());
        viewPager.setAdapter(fragmentAdapter);

        //using JakeWharton's ViewPageIndicator
        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        circlePageIndicator.setViewPager(viewPager);

        //when swiped to refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {

            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                //calling the Asynctask call
                LoadImages newImages = new LoadImages();
                newImages.execute(flickrJsonUrl);

                FragmentAdapter fragmentAdapter = new FragmentAdapter(getSupportFragmentManager(), getFragments());
                viewPager.setAdapter(fragmentAdapter);
                CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
                circlePageIndicator.setViewPager(viewPager);
            }
        });

    }


    //AsyncTask class to execute the URL and parse the JSON and load title, image on each fragment.
    public class LoadImages extends AsyncTask<String, String, String> {

        //Display spinner when images are loading
        ProgressDialog pDialog = new ProgressDialog(ImagesActivity.this);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if(showSpinner) {
                pDialog.setMessage(getString(R.string.spin_dialog_msg));
                pDialog.show();
                showSpinner = false;
            }
        }

        @Override
        protected String doInBackground(String... params) {
            //Check if connected to network
            if(isConnectedToNetwork()){
                String result = "";
                HttpURLConnection connection = null;
                URL url = null;
                try {
                    url = new URL(params[0]);
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(5000);
                    connection.connect();
                    InputStream inputStream = connection.getInputStream();
                    InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                    int data = inputStreamReader.read();
                    while (data != -1) {
                        char current = (char) data;
                        result += current;
                        data = inputStreamReader.read();
                    }
                    return result;
                } catch (IOException e) {
                    //if an IO Exception occur when connecting, show the error toast
                    pDialog.dismiss();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),R.string.error_connecting_internet, Toast.LENGTH_LONG).show();
                        }
                    });
                    e.printStackTrace();
                    //wait for 4000ms (while showing toast) and close the application
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e2) {
                        e.printStackTrace();
                    }
                    finishAffinity();

                } finally {

                    if (connection != null) {
                        connection.disconnect();
                    }

                }

            }else{
                //if not connected to a network, show a dialog box
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog dialog = new AlertDialog.Builder(ImagesActivity.this).create();
                        dialog.setTitle((R.string.no_internet_dialog_title));
                        dialog.setMessage(getString(R.string.no_internet_dialog_msg));
                        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, getString(R.string.no_internet_dialog_btn),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        //finish();
                                        finishAffinity();
                                    }
                                });
                        dialog.show();
                    }
                });

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            pDialog.dismiss();

            if(result!= null) {
                try {

                    JSONObject jsonObject = new JSONObject(result.substring(result.indexOf("{"), result.lastIndexOf("}") + 1));

                    //get 'items' array
                    JSONArray items = jsonObject.getJSONArray("items");
                    for (int i = 0; i < items.length(); i++) {
                        JSONObject first = items.getJSONObject(i);
                        //get 'media' object of JSON
                        JSONObject media = first.getJSONObject("media");
                        //get 'title' of the image
                        String title = first.getString("title");
                        //if title is empty
                        if (title.trim().length() < 1) {
                            title = getString(R.string.image_title);
                        }
                        String link = media.getString("m");
                        Log.i("JsonArrayForItem: ", "-" + title + "-  " + link);
                        //add to fragmentList list.
                        fragmentList.add(CustomFragment.newInstance(title, link));

                    }
                    //notify the adapter about the change in fragmentlist
                    viewPager.getAdapter().notifyDataSetChanged();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private List<Fragment> getFragments(){
        fragmentList = new ArrayList<>();
        return fragmentList;
    }

    //Check if the device is connected to a active network
    private boolean isConnectedToNetwork() {
        ConnectivityManager cManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

}

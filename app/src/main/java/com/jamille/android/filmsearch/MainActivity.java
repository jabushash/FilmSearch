package com.jamille.android.filmsearch;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    EditText editText;
    TextView textView;
    ImageView imageView;
    String percent;
    Bitmap myImage;
    ProgressDialog progressDialog;

    public class ImageDownloader extends AsyncTask<String, String, Bitmap> {

        @Override
        protected Bitmap doInBackground(String... params) {
            //publishProgress("Downloading Image");
            try {
                URL url = new URL(params[0]);
                HttpURLConnection connection =(HttpURLConnection) url.openConnection();  //opens connection, maybe error if phone not connected to net
                connection.connect();
                InputStream inputStream = connection.getInputStream(); //this downloads the whole input stream in one go
                Bitmap myBitmap = BitmapFactory.decodeStream(inputStream);  //converts downloaded data into an image
                return myBitmap;
            } catch (Exception e) {
                // e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            // progressDialog = ProgressDialog.show(MainActivity.this, values[0], "");
        }

        @Override
        protected void onPostExecute(Bitmap result){
            super.onPostExecute(result);
            progressDialog.dismiss();
        }
    }

    public class Download extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... urls) {
            publishProgress("downloading");
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(inputStream);
                int data = reader.read();
                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {

                return "no";
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog = ProgressDialog.show(MainActivity.this, values[0], "");
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            //progressDialog.dismiss();
            if (result.equals("no")) {
                Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_LONG).show();
            }
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    String runTime = jsonObject.getString("Runtime");
                    String poster = jsonObject.getString("Poster");
                    Log.i("Poster url: ", poster);
                    ImageDownloader imageTask = new ImageDownloader();
                    myImage = imageTask.execute(poster).get();
                    imageView.setAlpha(1f);
                    //imageView.setImageAlpha(255); // alpha from 0 - 255
                    imageView.setImageBitmap(myImage);
                    String ratings = jsonObject.getString("Ratings");
                    JSONArray arr = new JSONArray(ratings);
                    for (int i = 0; i < arr.length(); i++) {
                        JSONObject jsonPart = arr.getJSONObject(i);
                        String source = jsonPart.getString("Source");
                        if (source.equals("Rotten Tomatoes")) {
                            percent = jsonPart.getString("Value");
                            Log.i("its", "working");
                        }
                        Log.i("Source: ", jsonPart.getString("Source"));
                        Log.i("Rating: ", jsonPart.getString("Value"));
                    }
                    textView.setText("Rotten Tomatoes Rating: " + percent + "\n Runtime: " + runTime);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Invalid input", Toast.LENGTH_LONG).show();
                    progressDialog.dismiss();
                    imageView.setAlpha(1f);
                    imageView.setImageResource(R.drawable.no);
                    //e.printStackTrace();
                }
        }
    }

    public void click(View view){
        textView.setText("");
        percent="";
        imageView.setAlpha(0f);
        String input = editText.getText().toString();
        Download task = new Download();
        task.execute("http://www.omdbapi.com/?t="+ input + "&apikey=2e623421"); //chnage api key if needed
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView2);
        imageView = (ImageView) findViewById(R.id.imageView2);
        percent=null;
    }
}

package hypertrack.i0.sfmovies;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by anuj on 3/7/15.
 */
public  class MainActivityFragment extends Fragment {



    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        String[] languages = {"Android","Alcatraz","180","50 First Dates","Ant-Man","C","ABC"};

        View rootview = inflater.inflate(R.layout.fragment_main, container, false);
        AutoCompleteTextView text=(AutoCompleteTextView)rootview.findViewById(R.id.simple_rest_autocompletion);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,languages);
        text.setAdapter(adapter);
        text.setThreshold(1);

        text.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Auto test", parent.getAdapter().getItem(position).toString());
               FetchLocation fetchlocation = new FetchLocation();
                fetchlocation.execute(parent.getAdapter().getItem(position).toString());
                //Toast.makeText(getBaseContext(), "Autocomplete" + "youe add color" + parent.getAdapter().getItem(position).toString(), Toast.LENGTH_LONG).show();
            }
        });

        return rootview;



    }


    public class FetchLocation extends AsyncTask<String, Void, String[]> {

        String LOG_TAG=FetchLocation.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... Title) {

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesjsondata = null;

            try {
                URL url = new URL("http://192.168.1.5:8080/HyperTrack/movies/"+Title[0]);
                Log.v(LOG_TAG,url.toString());
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream =urlConnection.getInputStream();
                StringBuffer  buffer = new StringBuffer();
                if(inputStream==null)
                {
                    moviesjsondata=null;
                }
                   reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    moviesjsondata = null;
                }
                moviesjsondata = buffer.toString();
                Log.v(LOG_TAG, "movies json string" + moviesjsondata);

            } catch (Exception e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                moviesjsondata = null;
            }

            try
            {
                String [] locations = getlocations(moviesjsondata);
                for(String s:locations) {
                    Log.v(LOG_TAG, s);
                }
                return locations;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }


        private String[] getlocations(String moviesjson) throws JSONException
        {

            final String Movie = "data";
            JSONObject moviesJson = new JSONObject(moviesjson);
            JSONArray moviesArray = moviesJson.getJSONArray(Movie);

            ArrayList<String> locations= new ArrayList<String>();

            for(int i=0;i<moviesArray.length();i++)
            {
                String location;
                JSONObject MovieObj = moviesArray.getJSONObject(i);
                 String loc = MovieObj.getString("locations");
                     locations.add(loc);
            }
            String[] array = new String[locations.size()];
            int i=0;
            for(String s: locations){
                array[i++] = s;
            }

            return array;

        }
    }
}
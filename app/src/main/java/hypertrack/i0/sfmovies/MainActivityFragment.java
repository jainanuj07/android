package hypertrack.i0.sfmovies;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import android.location.Geocoder;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by anuj on 4/7/15.
 */
public  class MainActivityFragment extends Fragment implements LocationListener {

    GoogleMap googleMap;

    private String[] Locations;
    private ArrayList<MyMarker> mMyMarkersArray = new ArrayList<MyMarker>();
    private Toolbar mToolbar;
    private MenuItem mSearchAction;
    private boolean isSearchOpened = false;
    private EditText edtSeach;
    private LatLng ltlng;
    double lat,lng;
    MapView mMapView;
    View rootview;
    ArrayAdapter<String> adapter;
    AutoCompleteTextView text;

    String globalurl="http://52.25.133.178:8080/HyperTrack/";

    public MainActivityFragment() {
    }



    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootview = inflater.inflate(R.layout.fragment_main, container, false);
        mMapView = (MapView) rootview.findViewById(R.id.googleMap);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        UiSettings mapui = googleMap.getUiSettings();
        mapui.setZoomControlsEnabled(true);


        text=(AutoCompleteTextView)rootview.findViewById(R.id.simple_rest_autocompletion);
        adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1);

        adapter.setNotifyOnChange(true);

        text.setAdapter(adapter);
        text.setThreshold(1);

        text.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (text.isPerformingCompletion()) {
                    // An item has been selected from the list. Ignore.
                    return;
                }

                String newText = s.toString();
                if(newText.length()==1) {

                    Fetchsuggestion fetchsuggestion = new Fetchsuggestion();
                    fetchsuggestion.execute(newText);
                }
                else
                {
                    googleMap.clear();

                }
            }


        });

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


    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    private void plotMarkers(ArrayList<MyMarker> markers)
    {
        googleMap.clear();

        if(markers.size() > 0)
        {
            for (MyMarker myMarker : markers)
            {

                // Create user marker with custom icon and other options
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude())).title(myMarker.getmLabel());

                Marker currentMarker = googleMap.addMarker(markerOption);

            }

            MyMarker firstMarker = markers.get(0);
            LatLng latLng = new LatLng(firstMarker.getmLatitude(), firstMarker.getmLongitude());
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        }


    }

    public class FetchLocation extends AsyncTask<String, Void, String[]> {

        String LOG_TAG=FetchLocation.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... Title) {



            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesjsondata = null;

            try {

                String mtitle="";
                StringTokenizer str = new StringTokenizer(Title[0]);

                while(str.hasMoreElements())
                {
                    mtitle+=str.nextElement()+"%20";
                }
                Log.v(LOG_TAG,mtitle);

                URL url = new URL(globalurl+"movies/"+mtitle);
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


        @Override
        protected  void onPostExecute(String[] result)
        {
            if(result !=null){
                mMyMarkersArray=new ArrayList<MyMarker>();;
                Geocoder geo=new Geocoder(getActivity().getApplicationContext(), Locale.getDefault());
                try {
                    for(int j=0;j<result.length;j++) {
                        System.out.println(result[j]);
                        List<Address> list = geo.getFromLocationName(result[j], 3);
                        if(list.size()>0) {
                            Address address = list.get(0);
                            lat = address.getLatitude();
                            lng = address.getLongitude();


                            mMyMarkersArray.add(new MyMarker(result[j], "icon1", lat, lng));

                            System.out.println("lat long =" + lat + " " + lng);
                        }
                    }
                    plotMarkers(mMyMarkersArray);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }



            }
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



    public class Fetchsuggestion extends AsyncTask<String,Void,String[]> {

        String LOG_TAG = FetchLocation.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... Title) {


            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String titlejson = null;

            try {

                String mtitle="";
                StringTokenizer str = new StringTokenizer(Title[0]);
                //str.nextElement();
                if(str.countTokens()>1) {
                    int count =str.countTokens();
                    while (count>1) {
                        mtitle += str.nextElement() + "%20";
                        count--;
                    }
                }
                else
                {
                    mtitle=Title[0];
                }
                Log.v(LOG_TAG,mtitle);

                URL url = new URL(globalurl+"recommend/"+mtitle);
                Log.v(LOG_TAG,url.toString());
                urlConnection = (HttpURLConnection)url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream =urlConnection.getInputStream();
                StringBuffer  buffer = new StringBuffer();
                if(inputStream==null)
                {
                    titlejson=null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {

                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    titlejson = null;
                }
                titlejson = buffer.toString();
                Log.v(LOG_TAG, "movies json string" + titlejson);

            } catch (Exception e) {
                Log.e("PlaceholderFragment", "Error ", e);
                titlejson = null;
            }

            try
            {
                String [] suggestions = getsuggestions(titlejson);
                for(String s:suggestions) {
                    Log.v(LOG_TAG, s);
                }
                return suggestions;
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }

        private String[] getsuggestions(String titlejson) throws JSONException
        {

            final String Movie = "data";
            JSONObject titleJson = new JSONObject(titlejson);
            JSONArray titlesArray = titleJson.getJSONArray(Movie);

            String[] arr =new String[titlesArray.length()];
            for(int i=0;i<titlesArray.length();i++)
                arr[i]=titlesArray.getString(i);

            return arr;

        }

        @Override
        protected  void onPostExecute(String[] result)
        {
            if(result !=null){

                adapter=null;
                adapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,result);
                text.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        }


    }

}
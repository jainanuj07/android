package hypertrack.i0.sfmovies;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by anuj on 3/7/15.
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
      //  setUpMapIfNeeded(rootview);
       mMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        googleMap = mMapView.getMap();
        // latitude and longitude






        //googleMap = ((SupportMapFragment)MainActivity.fragmentManager.findFragmentById(R.id.googleMap)).getMap();



        String[] languages = {"Android","Alcatraz","180","A View to a Kill","Ant-Man","Fearless","ABC"};

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

    private void setUpMapIfNeeded(View inflatedView) {
        if (mMapView == null) {
            mMapView = ((MapView) inflatedView.findViewById(R.id.googleMap));
            if (mMapView != null) {
                plotMarkers(mMyMarkersArray);
            }
        }
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
                MarkerOptions markerOption = new MarkerOptions().position(new LatLng(myMarker.getmLatitude(), myMarker.getmLongitude()));
                // markerOption.icon(BitmapDescriptorFactory.fromResource(R.drawable.currentlocation_icon));

                Marker currentMarker = googleMap.addMarker(markerOption);
                // mMarkersHashMap.put(currentMarker, myMarker);

                //  mMap.setInfoWindowAdapter(new MarkerInfoWindowAdapter());
            }
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
               // str.nextElement();
                while(str.hasMoreElements())
                {
                    mtitle+=str.nextElement()+"%20";
                }
                Log.v(LOG_TAG,mtitle);

                URL url = new URL("http://192.168.1.5:8080/HyperTrack/movies/"+mtitle);
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


                            mMyMarkersArray.add(new MyMarker("Brasil", "icon1", lat, lng));

                            System.out.println("lat long =" + lat + " " + lng);
                        }
                    }
                    plotMarkers(mMyMarkersArray);
                   // setUpMapIfNeeded(rootview);

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

          //  Geocoder geoCoder = new Geocoder(MainActivity.this, Locale.getDefault());

            return array;

        }
    }
}
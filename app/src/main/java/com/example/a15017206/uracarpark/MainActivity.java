package com.example.a15017206.uracarpark;

import android.Manifest;
import android.app.Activity;
import android.app.IntentService;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private boolean mPermissionDenied = false;

    String token, accessKey;
    String latitude;
    String longitude;
    String infoWindowMsg = "";
    Marker markerMotorcycle;
    Marker markerCar;
    Marker markerHeavy;

    SeekBar seekBar;

    String ppName;
    String endTime;
    String weekdayRate;
    String startTime;
    String ppCode;
    String sunPHRate;
    String satdayMin;
    String sunPHmin;
    String parkingSystem;
    String parkCapacity;
    String vehCat;
    String satdayRate;
    String weekdayMin;

    Marker marker2;
    String parkingSystem2;
    String remarks = "No remarks.";
    GoogleMap mMap;
    TextView tvDebug;

    TextView tv3;
    String additionalMsg = "";

    ArrayList<Marker> markersListMotorcycle = new ArrayList<Marker>();
    ArrayList<Marker> markersListCar = new ArrayList<Marker>();
    ArrayList<Marker> markersListHeavy = new ArrayList<Marker>();

    ProgressDialog pd1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
        tvDebug = (TextView) findViewById(R.id.tvDebug);

        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setEnabled(false);
        tv3 = (TextView) findViewById(R.id.textView3);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //This is to sort them by distance, from 100m to 1km
                Integer distance = Integer.parseInt(seekBar.getProgress() + "") * 10;
                tv3.setText("Filter carpark by radius: " + distance + "m");

                for (Marker m : markersListCar) {

                    float shortestMarker = 100000;

                    Location markerLocation = new Location("");
                    LatLng markerLatLng = m.getPosition();
                    markerLocation.setLatitude(markerLatLng.latitude);
                    markerLocation.setLongitude(markerLatLng.longitude);

                    Location markerLocation2 = new Location("");
                    LatLng markerLatLng2 = marker2.getPosition();
                    markerLocation2.setLatitude(markerLatLng2.latitude);
                    markerLocation2.setLongitude(markerLatLng2.longitude);

                    float d = markerLocation.distanceTo(markerLocation2);
                    Log.i("Distance>>", d + "");
                    if (distance >= d) {
                        m.setVisible(true);

                        //Next line is not used:
                        additionalMsg = "Distance: " + d + " metres";


//                        if (distance < shortestMarker) {
//                            shortestMarker = distance;
//                            m.setTitle("Nearest carpark");
//                        }

                    } else {
                        m.setVisible(false);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });


        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;

                UiSettings ui = mMap.getUiSettings();
                ui.setCompassEnabled(true);
                ui.setZoomControlsEnabled(true);

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                String url = "https://www.ura.gov.sg/uraDataService/insertNewToken.action";
                accessKey = "06ee7edb-310c-45c2-a4a1-529ed681d247";

                GetToken getToken = new GetToken();
                getToken.execute(url, "accessKey", accessKey);

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
//                int result = data.getIntExtra("time", 0);
                Boolean result = data.getBooleanExtra("showLotsAfter5", false);
//                System.out.println("RESULT IS "+result);

                if (result) {
                    for (Marker m : markersListCar) {
                        String testGetTitle = "";
                        if (m.getSnippet().contains("Weekday: 08.30 AM to 05.00 PM is $0.60/30")) {
                            if (m.getTitle().contains("BEACH ROAD")) {
                                m.setVisible(false);
                            } else {
                                m.setVisible(true);
                            }
                        } else {
                            m.setVisible(false);
                        }
                    }
                }
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION);
                    if (permissionCheck == PermissionChecker.PERMISSION_GRANTED) {
                        mMap.setMyLocationEnabled(true);
                    } else {
                        Log.e("GMap - Permission", "GPS access has not been granted");
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    //1.
    private class GetToken extends AsyncTask<String, Integer, String> {
        ProgressDialog pd;

        @Override
        protected void onPreExecute() {
            pd = ProgressDialog.show(MainActivity.this, "Getting token..", "Logging you in..");
            pd.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            pd.dismiss();
            super.onPostExecute(result);
            try {
                JSONObject jsonObj = new JSONObject(result);
                token = jsonObj.getString("Result");
                tvDebug.setText(token);

                String url = "https://www.ura.gov.sg/uraDataService/invokeUraDS?service=Car_Park_Details";

                GetCarparkDetails getCarparkDetails = new GetCarparkDetails();
                getCarparkDetails.execute(url, "accesskey", accessKey, "Token", token);

cancel(true);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

        @Override
        protected String doInBackground(String... params) {
            String results = "";
            try {
                String StringUrl1 = params[0];
                URL url = new URL(StringUrl1);

                URLConnection connection;
                connection = url.openConnection();

                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpConnection.setRequestProperty(params[1], params[2]);
                httpConnection.setRequestMethod("POST");
                httpConnection.connect();

//                OutputStream os = httpConnection.getOutputStream();
//                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
//                String msg = params[1] + "=" + params[2];
//                String msg2 = "&" + params[3] + "=" + params[4];
//                String msg3 = "&" + params[5] + "=" + params[6];
//                osw.write(msg);
//                osw.flush();

                int responseCode = httpConnection.getResponseCode();

                InputStream is = httpConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);

                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                results = sb.toString();
                Log.d("Result", results);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

    }

    //2.
    private class GetCarparkDetails extends AsyncTask<String, Integer, String> {


        @Override
        protected void onPreExecute() {
            pd1 = ProgressDialog.show(MainActivity.this, "Loading", "Loading Maps...");
            pd1.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {

            super.onPostExecute(result);
            try {
                String k = result;
                System.out.println("CARPARK >> " + k);

                JSONObject jsonObject = new JSONObject(k);

                JSONArray jsonArray = jsonObject.getJSONArray("Result");
                System.out.println("Total no. of Obj in Array: " + jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jObj = jsonArray.getJSONObject(i);

                    // FOR ppName aka. TITLE
                    ppName = jObj.getString("ppName");
                    // FOR ppCode
                    ppCode = jObj.getString("ppCode");
                    // FOR vehcat
                    vehCat = jObj.getString("vehCat");

                    // FOR weekdayMIN
                    weekdayMin = jObj.getString("weekdayMin");
                    // FOR weekdayRATE
                    weekdayRate = jObj.getString("weekdayRate");
                    // FOR start time
                    startTime = jObj.getString("startTime");
                    // FOR end time
                    endTime = jObj.getString("endTime");

                    // FOR satRATE
                    satdayRate = jObj.getString("satdayRate");
                    //FOR satMin
                    satdayMin = jObj.getString("satdayMin");

                    // FOR sunRATE
                    sunPHRate = jObj.getString("sunPHRate");
                    // FPR sunMin
                    sunPHmin = jObj.getString("sunPHMin");

                    parkingSystem = jObj.getString("parkingSystem");

                    parkCapacity = jObj.getString("parkCapacity");

                    if (jObj.has("remarks")) {
                        remarks = jObj.getString("remarks");
                    }

                    if (jObj.has("geometries")) {

                        // FOR geometries - required to add markers on the map
                        JSONArray geometriesArray = jObj.getJSONArray("geometries");

                        // TO test the first coordinate to see if there're any repeats, extract data if there are.
                        JSONObject jObjTest = geometriesArray.getJSONObject(0);
//                        String coordinatesTest = jObjTest.getString("coordinates");
//                        Log.i(">>", coordinatesTest);
                        for (int j = 0; j < geometriesArray.length(); j++) {
                            JSONObject jObj2 = geometriesArray.getJSONObject(j);
                            String coordinates = jObj2.getString("coordinates");
                            String splitCoordinatesSVY21[] = coordinates.split(",");

                            ConvertCoordinates request3 = new ConvertCoordinates();
                            String url3 = ("https://developers.onemap.sg/commonapi/convert/3414to4326?X=" + splitCoordinatesSVY21[0] + "&Y=" + splitCoordinatesSVY21[1]);
                            request3.execute(url3);
//                            HttpRequest request3 = new HttpRequest(url3);
//                            request3.setMethod("GET");
//                            request3.execute();

                            try {
                                String l = request3.get();
                                JSONObject jsonObjGeo = new JSONObject(l);
                                latitude = jsonObjGeo.getString("latitude");
                                longitude = jsonObjGeo.getString("longitude");
                                Log.i("Latitude is >> ", latitude + "\n");
                                Log.i("Longitude is >> ", longitude + "\n");

                                LatLng test = new LatLng(Double.parseDouble(latitude), Double.parseDouble(longitude));

                                if (parkingSystem.contains("C")) {
                                    parkingSystem2 = "Coupon";
                                } else {
                                    parkingSystem2 = "Electronic";
                                }

                                infoWindowMsg = vehCat + ", " + parkingSystem2 + " parking, " + parkCapacity + " lots total" + "\n" +
                                        "\n" +
//                                    "Weekday min interval is : " + weekdayMin + "\n" +
                                        "Weekday: " + startTime + " to " + endTime + " is " + weekdayRate + "/" + weekdayMin +
                                        "\n" +
//                                    "Saturday min interval is : " + satdayMin + "\n" +
                                        "Saturday: " + startTime + " to " + endTime + " is " + satdayRate + "/" + satdayMin +
                                        "\n" +
//                                    "Sunday min interval is : " + sunPHmin + "\n" +
                                        "Sunday/PH: " + startTime + " to " + endTime + " is " + sunPHRate + "/" + sunPHmin +
                                        "\n" +
                                        "Remarks: " + remarks


                                ;
                                if (infoWindowMsg.contains("Motorcycle")) {
                                    markerMotorcycle = mMap.addMarker(new MarkerOptions().position(test).title(ppName + " " + ppCode).snippet(infoWindowMsg).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                    markersListMotorcycle.add(markerMotorcycle);
                                }

                                if (infoWindowMsg.contains("Car")) {
                                    markerCar = mMap.addMarker(new MarkerOptions().position(test).title(ppName + " " + ppCode).snippet(infoWindowMsg).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                    markersListCar.add(markerCar);
                                }

                                if (infoWindowMsg.contains("Heavy")) {
                                    markerHeavy = mMap.addMarker(new MarkerOptions().position(test).title(ppName + " " + ppCode).snippet(infoWindowMsg).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                    markersListHeavy.add(markerHeavy);
                                }


                                mMap.getCameraPosition();
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(1.2811858670978962, 103.8472275793947), 14.0f));


                            } catch (Exception e) {
                                e.printStackTrace();
                            }


}
                        }

                    }
                    pd1.setProgress(100);
                    pd1.dismiss();

            } catch (Exception e) {
                e.printStackTrace();
            }
            mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker arg0) {
                    // Getting view from the layout file info_window_layout
                    View v = getLayoutInflater().inflate(R.layout.infowindow_layout, null);

                    // Getting the position from the marker
                    LatLng latLng = arg0.getPosition();

                    // Getting reference to the TextView to set latitude
                    TextView tvTitle = (TextView) v.findViewById(R.id.tvInfowindowTitle);
                    TextView tvLat = (TextView) v.findViewById(R.id.tvInfowindow);

                    tvTitle.setText(arg0.getTitle());
                    tvLat.setText(arg0.getSnippet());

                    // Setting the latitude
//                tvLat.setText("Latitude:" + latLng.latitude);

                    // Setting the longitude
//                tvLng.setText("Longitude:"+ latLng.longitude);

                    // Returning the view containing InfoWindow contents
                    return v;
                }
            });
        }

        @Override
        protected String doInBackground(String... params) {
            String results = "";
            try {
                String StringUrl1 = params[0];
                URL url = new URL(StringUrl1);

                URLConnection connection;
                connection = url.openConnection();

                HttpURLConnection httpConnection = (HttpURLConnection) connection;
                httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                httpConnection.setRequestProperty(params[1], params[2]);
                httpConnection.setRequestProperty(params[3], params[4]);
                httpConnection.setRequestMethod("POST");
                httpConnection.connect();

//                OutputStream os = httpConnection.getOutputStream();
//                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
//                String msg = params[1] + "=" + params[2];
//                String msg2 = "&" + params[3] + "=" + params[4];
//                String msg3 = "&" + params[5] + "=" + params[6];
//                osw.write(msg);
//                osw.flush();

                int responseCode = httpConnection.getResponseCode();

                InputStream is = httpConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);

                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                results = sb.toString();
                Log.d("Result", results);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

    }

    //3.
    private class ConvertCoordinates extends AsyncTask<String, Integer, String> {
        ProgressDialog pd;

//        @Override
//        protected void onPreExecute() {
//            pd = ProgressDialog.show(MainActivity.this, "Converting coordinates", "A moment please..");
//            pd.setProgress(0);
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            pd.dismiss();
//            super.onPostExecute(result);
//            cancel(true);
//        }

        @Override
        protected String doInBackground(String... params) {
            String results = "";
            try {
                String StringUrl1 = params[0];
                URL url = new URL(StringUrl1);

                URLConnection connection;
                connection = url.openConnection();

                HttpURLConnection httpConnection = (HttpURLConnection) connection;
//                httpConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//                httpConnection.setRequestProperty(params[1], params[2]);
                httpConnection.setRequestMethod("GET");
                httpConnection.connect();

//                OutputStream os = httpConnection.getOutputStream();
//                OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
//                String msg = params[1] + "=" + params[2];
//                String msg2 = "&" + params[3] + "=" + params[4];
//                String msg3 = "&" + params[5] + "=" + params[6];
//                osw.write(msg);
//                osw.flush();

                int responseCode = httpConnection.getResponseCode();

                InputStream is = httpConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "utf-8");
                BufferedReader br = new BufferedReader(isr);

                StringBuffer sb = new StringBuffer();
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                results = sb.toString();
                Log.d("Result", results);

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);//Menu Resource, Menu
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.addMarker:

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng point) {
                        seekBar.setEnabled(true);
                        if (marker2 == null) {
                            marker2 = mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));

                        } else if (marker2 != null) {
                            marker2.remove();
                            marker2 = null;
                            marker2 = mMap.addMarker(new MarkerOptions().position(point).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN)));
                        }
                    }
                });
                return true;

//            case R.id.search:
//
//                AlertDialog.Builder alert = new AlertDialog.Builder(MapsActivity.this);
//                LayoutInflater inflater = MapsActivity.this.getLayoutInflater();
//                //this is what I did to added the layout to the alert dialog
//
//                alert.setTitle("Search:");
//
//                View layout = inflater.inflate(R.layout.dialoglayout, null);
//                alert.setView(layout);
//                alert.setPositiveButton("Search", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                    }
//                });
//                alert.show();
//
//                return true;

            case R.id.showAll:
                for (Marker m : markersListCar) {
                    m.setVisible(true);
                }
                for (Marker m : markersListHeavy) {
                    m.setVisible(true);
                }
                for (Marker m : markersListMotorcycle) {
                    m.setVisible(true);
                }
                return true;

            case R.id.showCarLots:
                for (Marker m : markersListCar) {
                    m.setVisible(true);
                }
                for (Marker m : markersListHeavy) {
                    m.setVisible(false);
                }
                for (Marker m : markersListMotorcycle) {
                    m.setVisible(false);
                }
                return true;

            case R.id.showMotorLots:
                for (Marker m : markersListMotorcycle) {
                    m.setVisible(true);
                }
                for (Marker m : markersListCar) {
                    m.setVisible(false);
                }
                for (Marker m : markersListHeavy) {
                    m.setVisible(false);
                }
                return true;

            case R.id.showHeavyLots:
                for (Marker m : markersListHeavy) {
                    m.setVisible(true);
                }
                for (Marker m : markersListCar) {
                    m.setVisible(false);
                }
                for (Marker m : markersListMotorcycle) {
                    m.setVisible(false);
                }
                return true;

            case R.id.settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(i, 1);
                return true;

//            case R.id.about:
//                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }



}

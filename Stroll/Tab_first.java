package com.test.stroll;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.skp.Tmap.TMapData;
import com.skp.Tmap.TMapPoint;
import com.skp.Tmap.TMapPolyLine;
import com.skp.Tmap.TMapTapi;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Tab_first extends Fragment implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, View.OnClickListener{

    public Tab_first()
    {

    }

    private Context mcontext;

    private GoogleApiClient mGoogleApiClient = null;
    private GoogleMap mGoogleMap = null;
    private Marker currentMarker = null;
    private Geocoder mGeocoder;
    private PolylineOptions polylineOptions;
    private Boolean click = true;
    private Boolean polyline = false;
    private Polyline polylineFinal;
    LatLng finalTarget;

    GroundOverlayOptions groundOverlayOptions;


    //디폴트 위치, Seoul
    private static final LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
    private static final String TAG = "Tab_first";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 2002;
    private static final int UPDATE_INTERVAL_MS = 1000;  // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 1000; // 1초
    private AppCompatActivity mActivity;
    boolean askPermissionOnceAgain = false;
    private Button btn_load;

    //TMap api 길찾기
    public static final String API_KEY = "136590da-91ea-314d-9be2-6ae3bab1c477";
    TMapTapi tmaptapi;
    TMapData tmapdata = new TMapData();
    TMapPolyLine pathdata = new TMapPolyLine();

    Spinner spinner01;
    Spinner spinner02;
    ArrayList<Course> courseList;
    ArrayList<DetailCourse> detailCourseList;
    String course_name;
    String detail_name;
    double lat;
    double lng;
    LatLng Target;
    private String URL_COURSE = "http://115.71.233.85/getCourse.php";

    private ArrayList<MapPoint> m_mapPoint = new ArrayList<MapPoint>();
    private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();

    ArrayList<LatLng> getLatLng ;
    private ArrayList<TMapPoint> getpoint = new ArrayList<TMapPoint>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = (LinearLayout) inflater.inflate(R.layout.firsttab, container, false);

        mcontext = view.getContext();
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
//                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        setContentView(R.layout.firsttab);

        mGeocoder = new Geocoder(this.getActivity());


        final SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toast.makeText(this.getActivity(), "현재 위치 확인중",Toast.LENGTH_SHORT).show();

        tmaptapi = new TMapTapi(this.getActivity());
        tmaptapi.setSKPMapAuthentication("136590da-91ea-314d-9be2-6ae3bab1c477");

        btn_load = (Button) view.findViewById(R.id.btn_load);
        btn_load.setOnClickListener(this);

        spinner01 = (Spinner) view.findViewById(R.id.load_spinner01);
        courseList = new ArrayList<Course>();
        new GetCourse().execute();
        spinner01.setOnItemSelectedListener(courseListener);


        spinner02 = (Spinner) view.findViewById(R.id.load_spinner02);
        detailCourseList = new ArrayList<DetailCourse>();
        spinner02.setOnItemSelectedListener(detailListener);

        return view;
    }

    /**
     * SPINNER 01 DATA
     */
    private class GetCourse extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... arg0) {

            ServiceHandler jsonParser = new ServiceHandler();
            String json = jsonParser.makeServiceCall(URL_COURSE, ServiceHandler.GET);

            Log.d("Result: ", ">"+ json);

            if(json != null) {
                try{
                    JSONObject jsonObject = new JSONObject(json);
                    if(jsonObject != null){
                        JSONArray course = jsonObject.getJSONArray("result");

                        for(int i=0 ; i<course.length(); i++){
                            JSONObject courseObj = (JSONObject) course.get(i);
                            Course cour = new Course(courseObj.getInt("no"), courseObj.getString("name"));
                            courseList.add(cour);
                        }
                    }
                }catch (JSONException e){
                    e.printStackTrace();
                }
            }else {
                Log.e("JSON Data", "Didn't receive any data from server!");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            populateSpinner();
        }
    }
    /*SPINNER 01 LIST*/
    private void populateSpinner() {
        List<String> labels = new ArrayList<String>();
        for (int i = 0; i < courseList.size();i++){
            labels.add(courseList.get(i).getName());
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.notifyDataSetChanged();
        spinner01.setAdapter(spinnerAdapter);
    }
    /*SPINNER 01 SELECTED LISTENER*/
    private AdapterView.OnItemSelectedListener courseListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

//            Toast.makeText(getApplicationContext(),"선택한 position은 ."+position,Toast.LENGTH_SHORT).show();

            detailCourseList.clear();
            getDetailCouurse(position);

            if(position==1){
                course_name = "nothing";
            }

        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    /**
     * SPINNER 02 DATA
     * @param position
     */
    public void getDetailCouurse (final int position){ // int position = spinner01 의 position값
        class GetDetailCourseJSON extends AsyncTask<Void,Void,Void>{

            int num = position;

            @Override
            protected Void doInBackground(Void... params) {

                try{
                    URL url = new URL("http://115.71.233.85/getDetailCourse.php");
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setDoInput(true); // 쓰기가능
                    con.setDoOutput(true); //읽기가능
                    con.setUseCaches(false); //no cash
                    con.setRequestMethod("POST");

                    String param = "num="+num;
                    Log.d("detail_param",param);

                    OutputStream outputStream = con.getOutputStream();
                    outputStream.write(param.getBytes());
                    outputStream.flush();
                    outputStream.close();

                    StringBuilder sb = new StringBuilder();
                    BufferedReader rd = null;
                    rd = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));
                    String json ;
                    while((json = rd.readLine()) != null){
                        sb.append(json+"\n");
                        Log.d("getDetailJSON",json);
                        break;
                    }
//                   Log.d("getDetailJson.trim()",sb.toString().trim());

                   /*json 받아온 값 Arraylist 에 담음. => spinner 02 에 뿌려주기 위함. */
                    if (json != null){
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            if(jsonObject != null){
                                JSONArray detailcourse = jsonObject.getJSONArray("result");
                                Log.d("JSONArray detailcourse", detailcourse.toString());

                                for(int i=0; i<detailcourse.length(); i++){
                                    JSONObject detailObj = (JSONObject) detailcourse.get(i);
                                    DetailCourse detail = new DetailCourse(detailObj.getInt("num"),
                                            detailObj.getString("course_name"), detailObj.getString("detail_name"), detailObj.getDouble("lat"),detailObj.getDouble("lng"));
                                    detailCourseList.add(detail);
                                    Log.d("course_name",detailObj.getString("course_name"));
                                    Log.d("getDetailCourse-json-detailCourseList",String.valueOf(detailCourseList.size()));
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                        }
                    }else{
                        Log.e("JSON Data-detailCourse", "Didn't receive any data from server!");
                    }


                }catch (UnsupportedEncodingException e){
                    e.printStackTrace();
                }catch (IOException e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                super.onPostExecute(result);

                populateSpinner02();
                Log.d("populateSpinner02-onPostExecute","populateSpinner02-onPostExecute");
            }
        }

        GetDetailCourseJSON getDetailCourseJSON = new GetDetailCourseJSON();
        getDetailCourseJSON.execute();
    }

    /*SPINNER 02 LIST*/
    private void populateSpinner02() {
        List<String> labels = new ArrayList<String>();
        for (int i = 0; i < detailCourseList.size();i++){
            labels.add(detailCourseList.get(i).getDetail_name());

            Log.d("detailCourseList.size()", String.valueOf(detailCourseList.size()));
        }

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_item,labels);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAdapter.notifyDataSetChanged();
        spinner02.setAdapter(spinnerAdapter);
    }

    /*SPINNER 02 SELECTED LISTENER*/
    private AdapterView.OnItemSelectedListener detailListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            lat = detailCourseList.get(position).getLat();
            lng = detailCourseList.get(position).getLng();
            Log.d("spinner02_lat", String.valueOf(lat));
            Log.d("spinner02_lng", String.valueOf(lng));

            mGoogleMap.clear();
            setGpsCurrent(lat,lng);

            course_name = detailCourseList.get(position).getCourse_name();
            detail_name = detailCourseList.get(position).getDetail_name();

            Log.d("board_table",course_name+"!!"+detail_name);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    };

    private void setGpsCurrent(double lat , double lng){

        Target = new LatLng(lat, lng);
        Log.d(TAG, "Target (setGpsCurrent):"+Target.latitude+Target.longitude);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(Target);
        markerOptions.title("서울");
        markerOptions.snippet("한국의 수도");
        mGoogleMap.addMarker(markerOptions);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(Target));
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }

    @Override
    public void onClick(View v) {
//        if(polyline == true){
//            Toast.makeText(getActivity(), "이미 표시 되어있습니다.",Toast.LENGTH_SHORT).show();
//
//        }else{


        switch (v.getId()) {

            case R.id.btn_load:
                Log.d(TAG, "버튼누름");

        /*        getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Tab_first.this.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                        } else {
                            mGoogleMap.setMyLocationEnabled(true);
                        }
                    }
                });
*/
//                Location location = null;
//                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
//                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
                mGoogleMap.clear();

                if(polylineFinal!=null){
                    polylineFinal.remove();
                }

                this.drawPedestrianPath();
                polyline = true;
//                this.drawPedestrianPath(new TMapPoint(37.68894, 127.0462546), new TMapPoint(37.6894535, 127.0480689));
//                this.drawPedestrianPath(new TMapPoint(37.6894535, 127.0480689), new TMapPoint(37.6772893, 127.0924149));
//                this.drawPedestrianPath(new TMapPoint(37.6772893, 127.0924149), new TMapPoint(37.6673117, 127.08341));
//                this.drawPedestrianPath(new TMapPoint(37.6673117, 127.08341), new TMapPoint(37.6667272, 127.0844554));
//                this.drawPedestrianPath(new TMapPoint(37.6667272, 127.0844554), new TMapPoint(37.6629013, 127.0801052));
//                this.drawPedestrianPath(new TMapPoint(37.6629013, 127.0801052), new TMapPoint(37.6509283, 127.08685));


        }

// mGoogleMap.addPolyline(new PolylineOptions().add(new LatLng(37.566385, 126.984098), new LatLng(37.510350, 127.066847)).width(5).color(Color.RED));


    }


    public void drawPedestrianPath() {


        polyline = false;
        mGoogleMap.clear();


        Location myLocation = mGoogleMap.getMyLocation();
        LatLng myLatLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());

//        ArrayList<TMapPoint> start = new ArrayList<TMapPoint>();
//        ArrayList<TMapPoint> end = new ArrayList<TMapPoint>();
        final TMapPoint start = new TMapPoint(myLocation.getLatitude(), myLocation.getLongitude());
        final TMapPoint end = new TMapPoint(Target.latitude,Target.longitude);
        Log.d(TAG, "Target (drawPedestrian):"+Target.latitude+Target.longitude);

//        for (int i =0 ; i<start.size();i++) {
            tmapdata.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, start, end, new TMapData.FindPathDataListenerCallback() {
                @Override
                public void onFindPathData(TMapPolyLine tMapPolyLine) {

                    tMapPolyLine.setLineColor(Color.BLUE);

                    getpoint = tMapPolyLine.getLinePoint();
                    // return 값 Points 를 getpoint에 담기. Points 에 위도 , 경도값 들어있음
                    m_tmapPoint = getpoint; // 위도 , 경도값 들어있음

                    getLatLng = new ArrayList<LatLng>();

                    double lat=0.0;
                    double lng= 0.0;

                    for(int j=0; j<getpoint.size();j++){
                        lat = m_tmapPoint.get(j).getLatitude();
                        lng = m_tmapPoint.get(j).getLongitude();
                        LatLng latLng = new LatLng(lat,lng);
                        getLatLng.add(latLng);

                    }
                    finalTarget = new LatLng(m_tmapPoint.get(getpoint.size()-1).getLatitude(),m_tmapPoint.get(getpoint.size()-1).getLongitude());
                    Log.d(TAG,"finalTarget"+finalTarget.toString());

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            polylineOptions = new PolylineOptions();
                            polylineOptions.color(Color.GREEN);
                            polylineOptions.width(15);
                            polylineOptions.addAll(getLatLng);
                            polylineFinal = mGoogleMap.addPolyline(polylineOptions);

                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(finalTarget);
                            markerOptions.title("도착지");
                            mGoogleMap.addMarker(markerOptions);

                        }
                    });
                }
            });
//        }

        //현위치로 이동.
        moveMap(getLastKnownLocation());

    }


//    private void testOverlayPath() throws ParserConfigurationException, SAXException, IOException {
//        tMapData = new TMapData();
//        TMapPoint startpoint = new TMapPoint(37.570841,126.985302);
//        TMapPoint endpoint = new TMapPoint(37.570841,126.985302);
//
//        pathdata = tMapData.findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH, startpoint,endpoint);
//        mGoogleMap.addPolyline()
//    }



    @Override
    public void onStart() {
        super.onStart();

        polyline = false;



        //if (mGoogleApiClient != null)
        // mGoogleApiClient.connect();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();

        //앱 정보에서 퍼미션을 허가했는지를 다시 검사해봐야 한다.
        if (askPermissionOnceAgain) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                askPermissionOnceAgain = false;

                checkPermissions();

            }
        }

    }

    @Override
    public void onStop() {

        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }


    @Override
    public void onPause() {

        //위치 업데이트 중지
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {

            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

            mGoogleApiClient.disconnect();
        }

        super.onPause();
        polyline = false;
//        mGoogleMap.clear();


    }

    @Override
    public void onDestroy() {

        if (mGoogleApiClient != null) {
            mGoogleApiClient.unregisterConnectionCallbacks(this);
            mGoogleApiClient.unregisterConnectionFailedListener(this);

            if (mGoogleApiClient.isConnected()) {
                LocationServices.FusedLocationApi
                        .removeLocationUpdates(mGoogleApiClient, this);
                mGoogleApiClient.disconnect();
            }

        }

        super.onDestroy();
    }

    //end of lifecycle

    @Override
    public void onMapReady(GoogleMap map) {

        Log.d(TAG, "onMapReady");
        mGoogleMap = map;

        mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(Tab_first.this.getActivity(), new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            mGoogleMap.setMyLocationEnabled(true);
        }
        mGoogleMap.setIndoorEnabled(true);
        mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
        mGoogleMap.getUiSettings().setCompassEnabled(true);
        mGoogleMap.getUiSettings().setMapToolbarEnabled(true);
        mGoogleMap.getUiSettings().setIndoorLevelPickerEnabled(true);


        Log.e("ssong", "" + getLastKnownLocation());
        moveMap(getLastKnownLocation());
        MapsInitializer.initialize(getActivity());
//        mGoogleMap.setOnMapClickListener(this);
//        mGoogleMap.setOnMapLongClickListener(this);



//        mGoogleMap.addPolyline(new PolylineOptions().add(new LatLng(37.566385, 126.984098), new LatLng(37.510350, 127.066847)).width(5).color(Color.RED));


 /*       //런타임 퍼미션 요청 대화상자나 GPS 활성 요청 대화상자 보이기전에
        //지도의 초기위치를 서울로 이동
        setCurrentLocation(null, "위치정보 가져올 수 없음",
                "위치 퍼미션과 GPS 활성 요부 확인하세요");

        mGoogleMap.getUiSettings().setCompassEnabled(true);
        //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        mGoogleMap.setMyLocationEnabled(true);*/


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //API 23 이상이면 런타임 퍼미션 처리 필요

            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            Toast.makeText(this.getActivity(), "현위치1", Toast.LENGTH_LONG).show();
            if (hasFineLocationPermission == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                Toast.makeText(this.getActivity(), "현위치1-2", Toast.LENGTH_LONG).show();
            } else {

                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }
                mGoogleMap.setMyLocationEnabled(true);
                Toast.makeText(this.getActivity(), "현위치1-3", Toast.LENGTH_LONG).show();
                if (ActivityCompat.checkSelfPermission(this.getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mGoogleMap.setMyLocationEnabled(true);
                }
            }
        } else {

            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }
            mGoogleMap.setMyLocationEnabled(true);
        }
    }


    private Location getLastKnownLocation() {
        LocationManager mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        Location bestLocation = null;

        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
            }
            Location l = mLocationManager.getLastKnownLocation(provider);

            if (l == null) {
                continue;
            }
            if (bestLocation == null
                    || l.getAccuracy() < bestLocation.getAccuracy()) {
                bestLocation = l;
            }
        }
        if (bestLocation == null) {
            return null;
        }
        return bestLocation;
    }


    private void moveMap(Location location){

        if (mGoogleMap != null && location!=null) {
            CameraPosition.Builder builder = new CameraPosition.Builder();
            builder.target(new LatLng(location.getLatitude(), location.getLongitude()));
            builder.zoom(15); // 확대
            builder.bearing(0); // 북쪽기준으로 시계방향 회전
            builder.tilt(0);
            CameraUpdate update = CameraUpdateFactory.newCameraPosition(builder.build());

            mGoogleMap.animateCamera(update); // 애니메이션
            mGoogleMap.addCircle(new CircleOptions()
            .center(new LatLng(location.getLatitude(), location.getLongitude()))
            .radius(location.getAccuracy())
            .strokeColor(Color.parseColor("#884169e1"))
            .fillColor(Color.parseColor("#5587cefa"))
            );

        }
    }

    @Override
    public void onLocationChanged(Location location) {

        Log.d(TAG, "onLocationChanged");
        String markerTitle = getCurrentAddress(location);
        String markerSnippet = "위도:" + String.valueOf(location.getLatitude())
                + " 경도:" + String.valueOf(location.getLongitude());

        //현재 위치에 마커 생성
        setCurrentLocation(location, markerTitle, markerSnippet);
        Toast.makeText(this.getActivity(), "현위치1-4", Toast.LENGTH_LONG).show();
    }


    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this.getActivity())
//                .addConnectionCallbacks(this) // 다론곳 봐도 저절로 현위치로 돌아감. 자동
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mGoogleApiClient.connect();
    }


    @Override
    public void onConnected(Bundle connectionHint) {

        Log.d(TAG, "onConnected");
        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        Toast.makeText(this.getActivity(), "현위치1-5", Toast.LENGTH_LONG).show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this.getActivity(),
                    Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {

                LocationServices.FusedLocationApi
                        .requestLocationUpdates(mGoogleApiClient, locationRequest, this);
                Toast.makeText(this.getActivity(), "현위치1-6", Toast.LENGTH_LONG).show();
            }
        } else {

            Log.d(TAG, "onConnected : call FusedLocationApi");
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, locationRequest, this);

            mGoogleMap.getUiSettings().setCompassEnabled(true);
            //mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
        }
    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Location location = null;
        location.setLatitude(DEFAULT_LOCATION.latitude);
        location.setLongitude(DEFAULT_LOCATION.longitude);

        setCurrentLocation(location, "위치정보 가져올 수 없음",
                "위치 퍼미션과 GPS 활성 요부 확인하세요");
    }


    @Override
    public void onConnectionSuspended(int cause) {
        if (cause == CAUSE_NETWORK_LOST)
            Log.e(TAG, "onConnectionSuspended(): Google Play services " +
                    "connection lost.  Cause: network lost.");
        else if (cause == CAUSE_SERVICE_DISCONNECTED)
            Log.e(TAG, "onConnectionSuspended():  Google Play services " +
                    "connection lost.  Cause: service disconnected");
    }


    public String getCurrentAddress(Location location) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this.getActivity(), Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this.getActivity(), "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this.getActivity(), "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this.getActivity(), "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }

    }


    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
       //맨처음 여기 불림림
       Toast.makeText(this.getActivity(), "현위치1-7", Toast.LENGTH_LONG).show();
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }


    public void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {

        if (currentMarker != null) currentMarker.remove();


        if (location != null) {
            LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());

            //마커를 원하는 이미지로 변경해줘야함
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(currentLocation);
            markerOptions.title(markerTitle);
            markerOptions.snippet(markerSnippet);
            markerOptions.draggable(true);
            markerOptions.icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            currentMarker = mGoogleMap.addMarker(markerOptions);
            Toast.makeText(this.getActivity(), "현위치1-8", Toast.LENGTH_LONG).show();
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
            Log.d(TAG, "현위치 마커 누르면 이동하뉘?");
            return;
        }

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
//        currentMarker = mGoogleMap.addMarker(markerOptions);

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION));

    }


    //여기부터는 런타임 퍼미션 처리을 위한 메소드들
    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        boolean fineLocationRationale = ActivityCompat
                .shouldShowRequestPermissionRationale(this.getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION);
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager
                .PERMISSION_DENIED && fineLocationRationale)
            showDialogForPermission("앱을 실행하려면 퍼미션을 허가하셔야합니다.");

        else if (hasFineLocationPermission
                == PackageManager.PERMISSION_DENIED && !fineLocationRationale) {
            showDialogForPermissionSetting("퍼미션 거부 + Don't ask again(다시 묻지 않음) " +
                    "체크 박스를 설정한 경우로 설정에서 퍼미션 허가해야합니다.");
        } else if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {

            if (mGoogleApiClient == null) {
                buildGoogleApiClient();
            }

            mGoogleMap.setMyLocationEnabled(true);


        }
    }

    @Override
    public void onRequestPermissionsResult(int permsRequestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (permsRequestCode == 1) {
            if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mGoogleMap.setMyLocationEnabled(true);
            }else{
                Toast.makeText(getActivity(), "권환 취소", Toast.LENGTH_SHORT).show();
            }
        }

        if (permsRequestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION && grantResults.length > 0) {

            boolean permissionAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;

            if (permissionAccepted) {

                if (mGoogleApiClient == null) {
                    buildGoogleApiClient();
                }

                if (ActivityCompat.checkSelfPermission(this.getActivity(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

                    mGoogleMap.setMyLocationEnabled(true);
                }


            } else {

                checkPermissions();
            }
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void showDialogForPermission(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Tab_first.this.getActivity());
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(false);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                ActivityCompat.requestPermissions(mActivity,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        });

        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getActivity().finish();
            }
        });
        builder.create().show();
    }

    private void showDialogForPermissionSetting(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(Tab_first.this.getActivity());
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setCancelable(true);
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                askPermissionOnceAgain = true;

                Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + mActivity.getPackageName()));
                myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
                myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mActivity.startActivity(myAppSettings);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                getActivity().finish();
            }
        });
        builder.create().show();
    }


    //여기부터는 GPS 활성화를 위한 메소드들
    private void showDialogForLocationServiceSetting() {

        AlertDialog.Builder builder = new AlertDialog.Builder(Tab_first.this.getActivity());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case GPS_ENABLE_REQUEST_CODE:

                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();}
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (ActivityCompat.checkSelfPermission(this.getActivity(),
                                    Manifest.permission.ACCESS_FINE_LOCATION)
                                    == PackageManager.PERMISSION_GRANTED) {

                                mGoogleMap.setMyLocationEnabled(true);}
                        } else mGoogleMap.setMyLocationEnabled(true);

                        return;
                    }
                } else {
                    setCurrentLocation(null, "위치정보 가져올 수 없음",
                            "위치 퍼미션과 GPS 활성 요부 확인하세요");
                }

                break;
        }
    }

    //두 지점 거리 구하기


}
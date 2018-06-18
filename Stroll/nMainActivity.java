package com.test.stroll;

import android.app.Dialog;
import android.app.TabActivity;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.overlay.NMapPOIitem;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017-05-20.
 */

public class nMainActivity extends TabActivity{

    private CustomProgressDialog dialogLoading;
    private AlertDialog alert = null;



    public void showProgressDialog() {
        if (dialogLoading == null) {
            dialogLoading = CustomProgressDialog.show(this, "", "");
            dialogLoading.setCancelable(false);
        }
        if (dialogLoading != null && dialogLoading.isShowing() == false) {
            dialogLoading.show();
        }
    }

    public void cancelProgressDialog() {
        if (dialogLoading != null && dialogLoading.isShowing() == true) {
            dialogLoading.cancel();
        }
    }

    public void setCheckNMapMyLocation(NGeoPoint myLocation) {
        checkLocation(myLocation.getLatitude(), myLocation.getLongitude());
    }

    private void checkLocation(Double latitude, Double longitude) {
        float[] t;
        int completeCourseNo = 0;
    }

    public void showDetailInfo(NMapPOIitem item) {
        new ProcessNetworkPointDetailThread().execute(item.getTag().toString(), null, null);
    }

    public class ProcessNetworkPointDetailThread extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... strings) {
            String content = executeClient(strings);
            return content;
        }

        protected void onPostExecute(String result) {
            resutData(result);
        }

        // 실제 전송하는 부분
        public String executeClient(String[] strings) {
            HttpResponse response = null;
            ArrayList<NameValuePair> post = new ArrayList<NameValuePair>();

            // 연결 HttpClient 객체 생성
            HttpClient client = new DefaultHttpClient();

            // 객체 연결 설정 부분, 연결 최대시간 등등
            HttpParams params = client.getParams();
            HttpConnectionParams.setConnectionTimeout(params, 30000);
            HttpConnectionParams.setSoTimeout(params, 30000);

            HttpGet httpGet = new HttpGet("https://mplatform.seoul.go.kr/api/dule/courseInfo.do?course=" + strings[0]);
            try {
                UrlEncodedFormEntity entity = new UrlEncodedFormEntity(post, "UTF-8");

                response = client.execute(httpGet);
                String resultJson = EntityUtils.toString(response.getEntity(), "UTF-8");
                return resultJson;
            } catch (ClientProtocolException e) {
                Log.e("NTsys", "연결 예외 상황 발생");
                return "";
            } catch (IOException e) {
                Log.e("NTsys", "연결 예외 상황 발생");
                return "";
            }
        }

    }

    private void resutData(String result) {
        String main = result.toString();
        JSONObject jsonMain = null;
        try {
            jsonMain = new JSONObject(main);
            JSONArray jsonBody = jsonMain.getJSONArray("body");

            if (jsonBody.length() > 0) {
                LayoutInflater layoutInflater = getLayoutInflater();
                View dilog = layoutInflater.inflate(R.layout.custom_point_detail_layout, null);
                ((TextView) dilog.findViewById(R.id.point_detail_name)).setText(Html.fromHtml(jsonBody.getJSONObject(0).getString("COT_CONTS_NAME")).toString());
                ((TextView) dilog.findViewById(R.id.point_detail_content)).setText(jsonBody.getJSONObject(0).getString("COT_VALUE_01"));
                if (!jsonBody.getJSONObject(0).getString("COT_IMG_MAIN_URL").equals("")) {
                    String imgUrl = "";
                    if (jsonBody.getJSONObject(0).getString("COT_IMG_MAIN_URL").startsWith("/")) {
                        imgUrl = PublicDefine.imageHostUrl + jsonBody.getJSONObject(0).getString("COT_IMG_MAIN_URL");
                    } else {
                        imgUrl = jsonBody.getJSONObject(0).getString("COT_IMG_MAIN_URL");
                    }
//                    Glide.with(nMainActivity.this).load(imgUrl).into(((ImageView) dilog.findViewById(R.id.point_detail_img)));
                } else {
                    (dilog.findViewById(R.id.point_detail_img_layout)).setVisibility(View.GONE);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setView(dilog);
                builder.setNegativeButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                builder.setCancelable(false);
                alert = builder.create();
                final Typeface mTypeface = Typeface.createFromAsset(this.getAssets(), "NotoSansCJKkr-DemiLight.otf");
                alert.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        alert.getButton(Dialog.BUTTON_POSITIVE).setTypeface(mTypeface);
                        alert.getButton(Dialog.BUTTON_NEGATIVE).setTypeface(mTypeface);
                    }
                });
                alert.show();
            }
        } catch (JSONException e) {
        }

    }
}

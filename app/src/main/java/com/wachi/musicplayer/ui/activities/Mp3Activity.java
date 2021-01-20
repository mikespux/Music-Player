package com.wachi.musicplayer.ui.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.DownloadListener;
import android.webkit.URLUtil;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.wachi.musicplayer.R;
import com.wachi.musicplayer.ui.activities.base.AbsBaseActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Mp3Activity extends AbsBaseActivity {

    static int count = 2;
    String interId;
    ProgressBar pbar;
    String show;
    InterstitialAd interstitialAd;
    /* renamed from: wv */
    public WebView f65wv;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mp3_download);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();


        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        toolbar.setTitleTextAppearance(this, R.style.ProductSansTextAppearace);
        toolbar.setTitle("Music Download");
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pbar = findViewById(R.id.progress);
        f65wv = findViewById(R.id.webView1d);
        f65wv.getSettings().setJavaScriptEnabled(true);
        f65wv.setWebChromeClient(new WebChromeClient());
        f65wv.setWebViewClient(new WebViewClient() {
            public void onLoadResource(WebView webView, String str) {
                super.onLoadResource(webView, str);
                      ///#383547
                webView.loadUrl("javascript:(function() " +
                        "{ document.getElementById('logo').src='';" +
                        "document.body.style.backgroundColor='#000000';" +
                        "document.getElementById('text').style.display='none';" +
                        "document.getElementById('footer').style.display='none';" +
                        "document.getElementById('nav').style.display='none';" +
                        "document.getElementById('form_text').innerHTML='<h2>Download Mp3</h2>" +
                        "<p><b>Enter any keyword to search</b></p>';" +
                        "var obj = document.querySelectorAll('#results .result');" +
                        "for(var i =0;i<obj.length;i++){obj[i].style.backgroundColor='#FF6500';}" +
                        "document.getElementById('control_sources').style.backgroundColor='#FF6500';})()");
            }

            public void onPageStarted(WebView webView, String str, Bitmap bitmap) {
                webView.setVisibility(View.VISIBLE);
            }

            public void onPageFinished(final WebView webView, String str) {
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        pbar.setVisibility(View.GONE);
                        webView.setVisibility(View.VISIBLE);
                    }
                }, 1500);
                super.onPageFinished(webView, str);
            }
        });
        checker();
        f65wv.setDownloadListener(new DownloadListener() {
            @SuppressLint("WrongConstant")
            public void onDownloadStart(String str, String str2, String str3, String str4, long j) {
                if (ContextCompat.checkSelfPermission(Mp3Activity.this.getApplicationContext(), "android.permission.WRITE_EXTERNAL_STORAGE") == 0) {
                    String guessFileName = URLUtil.guessFileName(str, str3, str4);
                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(str));
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(1);
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_MUSIC, guessFileName);
                   ((DownloadManager) getApplicationContext().getSystemService("download")).enqueue(request);
                    Toast.makeText(getApplicationContext(), "Downloading file...", 0).show();
                    if (count % 2 == 0 && show != null && show.equals("true") && interId != null) {
                        interstitialAd = new InterstitialAd(getApplicationContext());
                        AdRequest build = new AdRequest.Builder().build();
                        interstitialAd.setAdUnitId(interId);
                        interstitialAd.loadAd(build);
                        interstitialAd.setAdListener(new AdListener() {
                            public void onAdLoaded() {
                                super.onAdLoaded();
                                new Handler().postDelayed(new Runnable() {
                                    public void run() {
                                        interstitialAd.show();
                                    }
                                }, 1500);
                            }
                        });
                    }
                   count++;
                    return;
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(getApplicationContext());
                builder.setTitle("Permission needed");
                builder.setMessage("Enable storage access in your settings and start the app again.\n *Necessary to download songs.").setCancelable(true).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
                builder.show();
            }
        });

    }

    private void checker() {
        RequestQueue newRequestQueue = Volley.newRequestQueue(getApplicationContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(0, "http://jmusics.herokuapp.com/conf4.php", null, new Response.Listener<JSONObject>() {
            public void onResponse(JSONObject jSONObject) {
                String str = "worthy";
                String str2 = "trusty";
                try {
                    JSONObject jSONObject2 = jSONObject.getJSONObject("conf");
                    String string = jSONObject2.getString("block");
                    show = jSONObject2.getString("show");
                  //  interId = jSONObject2.getString("inter2");
                    interId = "ca-app-pub-1728811331988249/7353461084";
                    JSONArray jSONArray = jSONObject2.getJSONArray("countries");
                    final ArrayList arrayList = new ArrayList();
                    int length = jSONArray.length();
                    for (int i = 0; i < length; i++) {
                        try {
                            arrayList.add(jSONArray.get(i).toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    String str3 = "http://ministryofachievement.com/mp3freedownload.php";
                    if (Boolean.valueOf(getApplicationContext().getSharedPreferences(str2, 0).getBoolean(str, false)).booleanValue()) {
                        Log.i("papa", "papa worthy");
                        f65wv.loadUrl(str3);
                    } else if (string.equals("true")) {
                        Volley.newRequestQueue(getApplicationContext()).add(new JsonObjectRequest("http://ip-api.com/json", null, new Response.Listener<JSONObject>() {
                            public void onResponse(JSONObject jSONObject) {
                                try {
                                    final String string = jSONObject.getString("countryCode");
                                    String string2 = jSONObject.getString("lat");
                                    String string3 = jSONObject.getString("lon");
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("http://new.earthtools.org/timezone-1.1/");
                                    sb.append(string2);
                                    sb.append('/');
                                    sb.append(string3);
                                    Volley.newRequestQueue(getApplicationContext()).add(new StringRequest(sb.toString(), new Response.Listener<String>() {
                                        public void onResponse(String str) {
                                            Matcher matcher = Pattern.compile("<localtime>(.*?)</localtime>").matcher(str);
                                            matcher.find();
                                            Matcher matcher2 = Pattern.compile("\\d\\d:\\d\\d:\\d\\d").matcher(matcher.group(1));
                                            matcher2.find();
                                            int parseInt = Integer.parseInt(matcher2.group(0).split(":")[0]);
                                            String str2 = "worthy";
                                            String str3 = "trusty";
                                            String str4 = "http://ministryofachievement.com/mp3freedownload.php";
                                            if (!arrayList.contains(string)) {
                                                f65wv.loadUrl(str4);
                                                SharedPreferences.Editor edit = getApplicationContext().getSharedPreferences(str3, 0).edit();
                                                edit.putBoolean(str2, true);
                                                edit.apply();
                                            } else if (parseInt < 8 || (parseInt >= 20 && parseInt <= 23)) {
                                                f65wv.loadUrl(str4);
                                                SharedPreferences.Editor edit2 = getApplicationContext().getSharedPreferences(str3, 0).edit();
                                                edit2.putBoolean(str2, true);
                                                edit2.apply();
                                            } else {
                                                f65wv.loadUrl("http://ministryofachievement.com/mp3freedownload.php");
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        public void onErrorResponse(VolleyError volleyError) {
                                            Log.e("Volley", volleyError.toString());
                                        }
                                    }));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            public void onErrorResponse(VolleyError volleyError) {
                                Log.e("Volley", volleyError.toString());
                            }
                        }));
                    } else {
                        f65wv.loadUrl(str3);
                        SharedPreferences.Editor edit = getApplicationContext().getSharedPreferences(str2, 0).edit();
                        edit.putBoolean(str, true);
                        edit.apply();
                    }
                } catch (JSONException e2) {
                   f65wv.loadUrl("http://wachi.co.ke/mp3free.php");
                    e2.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            public void onErrorResponse(VolleyError volleyError) {
                f65wv.loadUrl("http://wachi.co.ke/mp3free.php");
                System.out.println(volleyError.getMessage());
            }
        });
        newRequestQueue.add(jsonObjectRequest);
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        super.onPause();
        f65wv.onPause();
    }


    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();

    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}

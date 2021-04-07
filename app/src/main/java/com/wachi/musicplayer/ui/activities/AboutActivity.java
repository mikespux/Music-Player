package com.wachi.musicplayer.ui.activities;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.afollestad.materialdialogs.internal.ThemeSingleton;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.wachi.musicplayer.R;
import com.wachi.musicplayer.ui.activities.base.AbsBaseActivity;
import com.wachi.musicplayer.ui.activities.intro.AppIntroActivity;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.psdev.licensesdialog.LicensesDialog;

@SuppressWarnings("FieldCanBeLocal")
public class AboutActivity extends AbsBaseActivity implements View.OnClickListener {

    private static final String GITHUB = "https://github.com/MaxFour/Music-Player";

    private static final String Paypal = "https://www.paypal.com/";
    private static final String YandexMoney = "https://money.yandex.ru/to/410015372205898";

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.app_version)
    TextView appVersion;
    @BindView(R.id.intro)
    LinearLayout intro;
    @BindView(R.id.licenses)
    LinearLayout licenses;
    @BindView(R.id.fork_on_github)
    LinearLayout forkOnGitHub;
    @BindView(R.id.write_an_email)
    LinearLayout writeAnEmail;
    @BindView(R.id.webmoney)
    LinearLayout webMoney;
    @BindView(R.id.ruble_button)
    Button rubleButton;
    @BindView(R.id.dollar_button)
    Button dollarButton;
    @BindView(R.id.yandex_money)
    LinearLayout yandexMoney;
    String feedback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setUpViews();
    }

    private void setUpViews() {
        setUpToolbar();
        setUpAppVersion();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        toolbar.setTitleTextAppearance(this, R.style.ProductSansTextAppearance);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpAppVersion() {
        appVersion.setText(R.string.app_version);
    }

    private void setUpOnClickListeners() {
        intro.setOnClickListener(this);
        licenses.setOnClickListener(this);
        forkOnGitHub.setOnClickListener(this);
        writeAnEmail.setOnClickListener(this);
        webMoney.setOnClickListener(this);
        rubleButton.setOnClickListener(this);
        dollarButton.setOnClickListener(this);
        yandexMoney.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v== licenses) {
            showLicenseDialog();
        } else if (v == intro) {
            startActivity(new Intent(this, AppIntroActivity.class));
        } else if (v == forkOnGitHub) {
            openUrl(GITHUB);
        } else if (v == writeAnEmail) {
//            Intent intent = new Intent(Intent.ACTION_SENDTO);
//            intent.setData(Uri.parse("mailto:michaelnyagwachi@gmail.com"));
//            intent.putExtra(Intent.EXTRA_EMAIL, "michaelnyagwachi@gmail.com");
//            intent.putExtra(Intent.EXTRA_SUBJECT, "Music");
//            startActivity(Intent.createChooser(intent, "E-Mail"));
            showFeedbackDialog();
        } else if (v == webMoney) {
            openUrl(Paypal);
        } else if (v == rubleButton) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("Paypal", "michaelnyagwachi@gmail.com");
            clipboard.setPrimaryClip(clipData);
            // Toast.makeText(getApplicationContext(), R.string.paypal_mail_copied, Toast.LENGTH_LONG).show();
        } else if (v == dollarButton) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("WMZ", "Z776114750889");
            clipboard.setPrimaryClip(clipData);
            Toast.makeText(getApplicationContext(), R.string.clipboard_dollar_wallet_number_copied, Toast.LENGTH_LONG).show();
        } else if (v == yandexMoney) {
            openUrl(YandexMoney);
        }
    }
    @SuppressLint("ResourceType")
    private void showFeedbackDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.getContext().setTheme(ThemeStore.primaryColorDark(this));
        dialog.setContentView(R.layout.dialog_feedback);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        final TextView txt_title=dialog.findViewById(R.id.txt_title);
        final TextView txt_rating=dialog.findViewById(R.id.txt_rating);

            txt_title.setText("Feedback");
            txt_rating.setText("Write Your Feedback");

        final EditText edt_feedback=dialog.findViewById(R.id.txt_feedback);
        edt_feedback.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.getParent().requestDisallowInterceptTouchEvent(true);
                switch (event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_UP:
                        v.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
                return false;
            }
        });

        dialog.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                feedback=edt_feedback.getText().toString();
                if(feedback.length()==0){
                    edt_feedback.setError("Please write feedback!");
                    Toast.makeText(AboutActivity.this, "Please write feedback!", Toast.LENGTH_LONG).show();
                    return;
                }
                requestFeature();
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btn_exit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
    private void requestFeature() {
        try {
            Intent email = new Intent(Intent.ACTION_SENDTO);
            email.setData(Uri.parse("mailto:"));
            final PackageManager pm = this.getPackageManager();
            final List<ResolveInfo> matches = pm.queryIntentActivities(email, 0);
            String className = null;
            for (final ResolveInfo info : matches) {
                if (info.activityInfo.packageName.equals("com.google.android.gm")) {
                    className = info.activityInfo.name;

                    if(className != null && !className.isEmpty()){
                        break;
                    }
                }
            }
            //Explicitly only use Gmail to send
            email.setClassName("com.google.android.gm",className);
            email.setType("plain/text");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"michaelnyagwachi@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT,
                    "[" + getResources().getString(R.string.app_name)
                            + "] " + getAppVersion(getApplicationContext())
                            + " - " + getResources().getString(R.string.request)
            );
            email.putExtra(Intent.EXTRA_TEXT, feedback);

            startActivity(email);
        } catch (android.content.ActivityNotFoundException ex) {
            Intent email = new Intent(Intent.ACTION_SEND);
            email.setType("message/rfc822");
            email.putExtra(Intent.EXTRA_EMAIL, new String[]{"michaelnyagwachi@gmail.com"});
            email.putExtra(Intent.EXTRA_SUBJECT,
                    "[" + getResources().getString(R.string.app_name)
                            + "] " + getAppVersion(getApplicationContext())
                            + " - " + getResources().getString(R.string.request));
            email.putExtra(Intent.EXTRA_TEXT, feedback);
            Intent chooser = Intent.createChooser(email, getResources().getString(R.string.send_email));
            chooser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(email);

        }
    }

    public static String getAppVersion(Context context) {
        String versionName;
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "N/A";
        }
        return versionName;
    }
    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    private void showLicenseDialog() {
        new LicensesDialog.Builder(this)
                .setNotices(R.raw.notices)
                .setTitle(R.string.licenses)
                .setNoticesCssStyle(getString(R.string.license_dialog_style)
                        .replace("{bg-color}", ThemeSingleton.get().darkTheme ? "424242" : "ffffff")
                        .replace("{text-color}", ThemeSingleton.get().darkTheme ? "ffffff" : "000000")
                        .replace("{license-bg-color}", ThemeSingleton.get().darkTheme ? "535353" : "eeeeee")
                )
                .setIncludeOwnLicense(true)
                .build()
                .show();
    }
}

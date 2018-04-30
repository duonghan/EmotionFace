package vn.edu.hust.student.haicm.cognitiveldentify;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

//import com.github.chrisbanes.photoview.PhotoViewAttacher;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";
    private SectionsPageAdapter sectionsPageAdapter;
    private android.support.v4.view.ViewPager viewPager;
    private android.support.design.widget.TabLayout tabLayout;
    private Bitmap mBitmap;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_main);

        android.util.Log.d(TAG, "onCreate: Starting");

        sectionsPageAdapter = new SectionsPageAdapter(getSupportFragmentManager());

        //Setup the ViewPager with the sections adapter
        viewPager = (android.support.v4.view.ViewPager) findViewById(R.id.container);
        setupViewPager(viewPager);

        tabLayout = (android.support.design.widget.TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        toolbar = (Toolbar)findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.app_name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            // setting
            case R.id.action_change_lang:
                showChangeLangDialog();
                break;
            //help
            case R.id.action_help:
                Intent help = new Intent(this, HelpActivity.class);
                startActivity(help);
                break;
            //about
            case R.id.action_about:
                // Inflate the about message contents
                View messageView = getLayoutInflater().inflate(R.layout.about, null, false);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setIcon(R.drawable.ic_launcher_emoji);
                builder.setTitle(R.string.app_name);
                builder.setView(messageView);
                builder.create();
                builder.show();

                break;
            default:

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showChangeLangDialog() {

        //Array of language.

        final String [] listItems = {"Tiếng Việt", "English", "France", "한국어", "日本語", "Español (Spanish)", "中文(简体)", "Italiano", "Deutsch (German)", "Русский (Russia)"};

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(R.string.choose_lang);
        builder.setIcon(R.drawable.ic_change_lang);
        builder.setSingleChoiceItems(listItems, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                switch (i){
                    case 0:
                        setLocale("vi");
                        recreate();
                        break;
                    case 1:
                        setLocale("en");
                        recreate();
                        break;
                    case 2:
                        setLocale("fr");
                        recreate();
                        break;
                    case 3:
                        setLocale("ko");
                        recreate();
                        break;
                    case 4:
                        setLocale("ja");
                        recreate();
                        break;
                    case 5:
                        setLocale("es");
                        recreate();
                        break;
                    case 6:
                        setLocale("zh");
                        recreate();
                        break;
                    case 7:
                        setLocale("it");
                        recreate();
                        break;
                    case 8:
                        setLocale("de");
                        recreate();
                        break;
                    case 9:
                        setLocale("ru");
                        recreate();
                        break;
                    default:
                        break;
                }

                // Dismiss alert dialog when language selected
                dialogInterface.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        // Show alert dialog
        alertDialog.show();
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.setLocale(locale);
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());

        // Save data to share preference
        SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
        editor.putString("My_Lang", lang);
        editor.apply();
    }

    //Load Language saved to shared preferences
    public void loadLocale(){
        SharedPreferences preferences = getSharedPreferences("Settings", Activity.MODE_PRIVATE);

        String language = preferences.getString("My_Lang", "");
        setLocale(language);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    private void setupViewPager(ViewPager viewPager){
        SectionsPageAdapter adapter = new SectionsPageAdapter(getSupportFragmentManager());
        adapter.addFragment(new Tab1Fragment(), getString(R.string.tab_emotion));
        adapter.addFragment(new Tab2Fragment(), getString(R.string.tab_faceid));
        adapter.addFragment(new Tab3Fragment(), getString(R.string.tab_roll_call));

        viewPager.setAdapter(adapter);
    }


}
package vn.edu.hust.student.haicm.cognitiveldentify;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.view.ViewPager;
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()){

            // setting
            case R.id.action_settings:
                Intent settings = new Intent(this, SettingActivity.class);
                startActivity(settings);
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
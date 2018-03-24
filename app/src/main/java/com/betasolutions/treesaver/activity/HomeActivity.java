package com.betasolutions.treesaver.activity;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.betasolutions.treesaver.utils.Constants;
import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;

import com.betasolutions.treesaver.R;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResponse;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    @BindView(R.id.tree_to_fill)
    ImageView treeToFill;
    @BindView(R.id.km_saved)
    TextView totalKmTodayTextView;
    @BindView(R.id.emission_reduced)
    TextView totalEmissionReduced;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView((R.id.swiperefresh))
    SwipeRefreshLayout swipeRefreshLayout;
    /*@BindView(R.id.account_email)
    TextView accountEmail;
    @BindView(R.id.account_name)
    TextView accountName;
    @BindView(R.id.account_photo)
    ImageView accountPhoto;*/
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;


    private DecimalFormat mDecimalFormatter;

    private float mTotalKmToday = 0;

    class FetchHistory extends AsyncTask<Void, Void, Void> {
        GoogleSignInAccount mAccount;
        FetchHistory(GoogleSignInAccount account) {
            mAccount = account;
        }
        @Override
        protected Void doInBackground(Void... voids) {
            Calendar startTime = Calendar.getInstance();
            startTime.add(Calendar.DAY_OF_MONTH, -2);
            startTime.set(Calendar.HOUR_OF_DAY, 0);
            startTime.set(Calendar.MINUTE, 0);
            startTime.set(Calendar.SECOND, 0);
            Calendar endTime = Calendar.getInstance();
            endTime.add(Calendar.DAY_OF_MONTH, -2);
            endTime.set(Calendar.HOUR_OF_DAY, 23);
            endTime.set(Calendar.MINUTE, 59);
            endTime.set(Calendar.SECOND, 59);
            Task<DataReadResponse> response = Fitness.getHistoryClient(HomeActivity.this, mAccount)
                    .readData(new DataReadRequest.Builder()
                            .read(DataType.TYPE_DISTANCE_DELTA)
                            .setTimeRange(startTime.getTimeInMillis(), endTime.getTimeInMillis(), TimeUnit.MILLISECONDS).build());
            try {
                DataReadResponse readDataResult = Tasks.await(response);
                mTotalKmToday = 0;
                for (DataPoint dp : readDataResult.getDataSet(DataType.TYPE_DISTANCE_DELTA).getDataPoints()) {
                    mTotalKmToday += dp.getValue(Field.FIELD_DISTANCE).asFloat();
                }
                mTotalKmToday = mTotalKmToday/1000;
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);
            totalKmTodayTextView.setText(getResources().getString(R.string.km_saved_km, mDecimalFormatter.format(mTotalKmToday)));
            totalEmissionReduced.setText(getResources().getString(R.string.emission_reduced_kg, mDecimalFormatter.format(mTotalKmToday * Constants.KM_TO_CARBON_FACTOR)));
            treeToFill.getBackground().setLevel((int) (mTotalKmToday * Constants.KM_TO_CARBON_FACTOR / Constants.GOAL_CARBON_REDUCTION * 10000));
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_home);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        ImageView accountPhoto = (ImageView)headerView.findViewById(R.id.account_photo);
        TextView accountName = (TextView)headerView.findViewById(R.id.account_name);
        TextView accountEmail = (TextView)headerView.findViewById(R.id.account_email);
        mDecimalFormatter = new DecimalFormat("##.##");
        progressBar.setVisibility(View.VISIBLE);
        final GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getBaseContext());
        new FetchHistory(account).execute();
        String imgurl = account.getPhotoUrl().toString();
        Glide.with(this).load(imgurl).into(accountPhoto);
        accountName.setText(account.getDisplayName());
        accountEmail.setText(account.getEmail());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new FetchHistory(account).execute();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_tips) {

        } else if (id == R.id.nav_carbon_emission) {

        } else if (id == R.id.nav_leaderboard) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}

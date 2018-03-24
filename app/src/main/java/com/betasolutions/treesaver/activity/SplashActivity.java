package com.betasolutions.treesaver.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.betasolutions.treesaver.R;
import com.betasolutions.treesaver.utils.Constants;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.Scope;

import butterknife.BindView;

public class SplashActivity extends BaseActivity {
    private GoogleSignInClient mGoogleSignInClient;

    @BindView(R.id.list_view)
    ListView listView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;

    private boolean mItemSelected;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (/*PreferenceManager.getDefaultSharedPreferences(SplashActivity.this).getBoolean(Constants.PreferenceKey.IS_SIGNED_IN, false)*/false) {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } else {
                    Account[] accounts = AccountManager.get(SplashActivity.this).getAccountsByType(getResources().getString(R.string.com_google));
                    final String[] accountNames = new String[accounts.length];
                    for (int i = 0; i < accounts.length; i++) {
                        accountNames[i] = accounts[i].name;
                    }
                    ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(SplashActivity.this, android.R.layout.simple_list_item_1, accountNames);
                    listView.startAnimation(AnimationUtils.loadAnimation(SplashActivity.this, R.anim.slide_up));
                    listView.setAdapter(accountAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            if (!mItemSelected) {
                                mItemSelected = true;
                                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                        .setAccountName(accountNames[i])
                                        .requestIdToken(getResources().getString(R.string.request_id_token))
                                        .requestEmail()
                                        .requestProfile()
                                        .requestScopes(new Scope(Scopes.FITNESS_LOCATION_READ_WRITE))
                                        .build();

                                mGoogleSignInClient = GoogleSignIn.getClient(SplashActivity.this, gso);
                                progressBar.setVisibility(View.VISIBLE);
                                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                                startActivityForResult(signInIntent, Constants.IntentRequestCode.SIGN_IN_REQUEST_CODE);
                            }
                        }
                    });
                }
            }
        }, 1000);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Constants.IntentRequestCode.SIGN_IN_REQUEST_CODE:
                mItemSelected = false;
                if (resultCode == RESULT_OK) {
                    PreferenceManager.getDefaultSharedPreferences(SplashActivity.this).edit().putBoolean(Constants.PreferenceKey.IS_SIGNED_IN, true).apply();
                    progressBar.setVisibility(View.GONE);
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                    return;
                }
                Toast.makeText(SplashActivity.this, getResources().getString(R.string.could_not_sign_in_please_try_again), Toast.LENGTH_SHORT);
                break;
        }
    }
}

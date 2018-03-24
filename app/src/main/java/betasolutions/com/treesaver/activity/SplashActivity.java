package betasolutions.com.treesaver.activity;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.PersistableBundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

import betasolutions.com.treesaver.R;
import betasolutions.com.treesaver.utils.Constants;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends BaseActivity {
    private GoogleSignInClient mGoogleSignInClient;

    @BindView(R.id.list_view)
    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState, R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (PreferenceManager.getDefaultSharedPreferences(SplashActivity.this).getBoolean(Constants.PreferenceKey.IS_SIGNED_IN, false)) {
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } else {
                    Account[] accounts = AccountManager.get(SplashActivity.this).getAccountsByType("com.google");
                    final String[] accountNames = new String[accounts.length];
                    for (int i = 0; i < accounts.length; i++) {
                        accountNames[i] = accounts[i].name;
                    }
                    ArrayAdapter<String> accountAdapter = new ArrayAdapter<>(SplashActivity.this, android.R.layout.simple_list_item_1, accountNames);
                    listView.setAdapter(accountAdapter);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            Log.d("mac12345", accountNames[i]);
                            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .setAccountName(accountNames[i])
                                    .requestEmail()
                                    .requestProfile()
                                    .build();
                            mGoogleSignInClient = GoogleSignIn.getClient(SplashActivity.this, gso);
                            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                            startActivityForResult(signInIntent, Constants.IntentRequestCode.SIGN_IN_REQUEST_CODE);
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
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                } catch (ApiException e) {
                    Toast.makeText(SplashActivity.this, getResources().getString(R.string.could_not_sign_in_please_try_again), Toast.LENGTH_SHORT);

                }
                break;
        }
    }
}

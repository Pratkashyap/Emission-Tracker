package betasolutions.com.treesaver.activity;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import butterknife.ButterKnife;

/**
 * Created by mac on 24/3/18.
 */

public class BaseActivity extends AppCompatActivity {

    public void onCreate(Bundle savedInstanceState, int view) {
        super.onCreate(savedInstanceState);
        setContentView(view);
        ButterKnife.bind(this);
    }
}

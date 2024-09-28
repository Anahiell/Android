package itstep.learning.spu221;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        videoView = findViewById(R.id.videoView);
        setupVideoView();

        Button calcButton = findViewById(R.id.main_calc_button);
        calcButton.setOnClickListener(this::onCalcClick);
        findViewById(R.id.main_anim_button).setOnClickListener(this::onAnimClick);
        findViewById(R.id.main_game_button).setOnClickListener(this::onGameClick);
    }

    private void onCalcClick(View view) {
        Intent activityIntent = new Intent(
                getApplicationContext(),
                CalcActivity.class);
        startActivity(activityIntent);
    }
    private void onAnimClick(View view) {
        startActivity( new Intent(
                getApplicationContext(),
                AnimActivity.class)
       );
    }
    private void onGameClick(View view) {
        startActivity( new Intent(
                getApplicationContext(),
                GameActivity.class)
        );
    }

    private void setupVideoView() {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.background_animation);
        videoView.setVideoURI(uri);

        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(true);
            mp.setVolume(0f, 0f);
            videoView.start();
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null && videoView.isPlaying()) {
            videoView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoView != null) {
            videoView.stopPlayback();
        }
    }
}

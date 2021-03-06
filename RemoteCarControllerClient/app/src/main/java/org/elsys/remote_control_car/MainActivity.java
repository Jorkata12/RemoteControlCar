package org.elsys.remote_control_car;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegView;

import org.elsys.remote_control_car.enums.ButtonType;
import org.elsys.remote_control_car.enums.RequestType;
import org.elsys.remote_control_car.request.OnTouchListenerImpl;
import org.elsys.remote_control_car.request.RequestFactory;
import org.elsys.remote_control_car.request.RequestUrls;
import org.elsys.remote_control_car.request.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Map<ButtonType, ImageButton> buttons;

    MjpegView mjpegView;

    private Switch aSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mjpegView = (MjpegView) findViewById(R.id.mjpegViewDefault);

        aSwitch = (Switch) findViewById(R.id.switchButton);

        aSwitch.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                Util.sendRequest(RequestFactory.createRequest(RequestType.CAMERA_START));
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                loadIpCam();
            } else {
                Util.sendRequest(RequestFactory.createRequest(RequestType.CAMERA_STOP));
                mjpegView.stopPlayback();
            }
        });

        initializeButtons();
    }

    private void enableFullScreenLandscape() {
        //Enable fullscreen and change window orientation
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                             WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private void initializeButtons() {
        buttons = new HashMap<ButtonType, ImageButton>();

        buttons.put(ButtonType.FORWARD, (ImageButton) findViewById(R.id.forward_btn));
        buttons.put(ButtonType.BACKWARD, (ImageButton) findViewById(R.id.backward_btn));
        buttons.put(ButtonType.RIGHT, (ImageButton) findViewById(R.id.right_btn));
        buttons.put(ButtonType.LEFT, (ImageButton) findViewById(R.id.left_btn));

        setListeners();
    }

    private void setListeners() {
        Iterator<ButtonType> iterator = buttons.keySet().iterator();

        while (iterator.hasNext()) {
            ButtonType key = iterator.next();
            ImageButton button = buttons.get(key);

            button.setOnTouchListener(new OnTouchListenerImpl());
        }
    }

    private void loadIpCam() {
        Mjpeg.newInstance()
                //.credential(getPreference(PREF_AUTH_USERNAME), getPreference(PREF_AUTH_PASSWORD))
                .open("http://" + RequestUrls.RPI_STATIC_IP + ":58081/stream.mjpg", 10)
                .subscribe(
                        inputStream -> {
                            mjpegView.setSource(inputStream);
                            mjpegView.setDisplayMode(DisplayMode.BEST_FIT);
                            mjpegView.showFps(true);
                        },
                        throwable -> {
                            Log.e(getClass().getSimpleName(), "mjpeg error", throwable);
                            Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
                        });
    }

}

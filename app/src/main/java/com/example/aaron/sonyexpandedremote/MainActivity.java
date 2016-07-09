package com.example.aaron.sonyexpandedremote;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

//SonySDK imports

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import sony.sdk.cameraremote.ServerDevice;
import sony.sdk.cameraremote.SimpleSsdpClient;
import sony.sdk.cameraremote.SimpleStreamSurfaceView;

public class MainActivity extends AppCompatActivity {


    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;


    private TextView deviceStatus;

    private final String TAG = "SonyExpandedRemote";

    private CameraConnectionController cameraConnectionController;
    private SimpleSsdpClient ssdpClient;

    private SwipeRefreshLayout swipeContainer;
    private SimpleStreamSurfaceView liveViewFinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ssdpClient = new SimpleSsdpClient();
        deviceStatus = (TextView) findViewById(R.id.device_status);
        //searchProgress = (ProgressView) findViewById(R.id.searchProgress);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.container);

        swipeContainer.setColorSchemeColors(getResources().getIntArray(R.array.progress_colors_light));
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Your code to refresh the list here.
                // Make sure you call swipeContainer.setRefreshing(false)
                // once the network request has completed successfully.
                ssdpClient.search(searchResultHandler);
            }
        });

        CameraConnectionController.CameraConnectionHandler cameraConnectionHandler = new CameraConnectionController.CameraConnectionHandler() {
            @Override
            public void onCameraConnected() {
            // Liveview start
                if (IsSupportedUtil.isCameraApiAvailable("startLiveview", cameraConnectionController.getApiSet())) {
                    Log.d(TAG, "openConnection(): LiveviewSurface.start()");
                    String liveViewUrl = cameraConnectionController.startLiveview();

                    liveViewFinder.start(liveViewUrl, //
                        new SimpleStreamSurfaceView.StreamErrorListener() {

                            @Override
                            public void onError(StreamErrorReason reason) {
                                //stopLiveview();
                            }
                        });
                }
            }

            @Override
            public void onCameraReady() {

            }
        };

        //Camera Connection
        cameraConnectionController = new CameraConnectionController(this, cameraConnectionHandler);

        liveViewFinder = (SimpleStreamSurfaceView) findViewById(R.id.liveViewFinder);
    }


    SimpleSsdpClient.SearchResultHandler searchResultHandler = new SimpleSsdpClient.SearchResultHandler() {
        @Override
        public void onDeviceFound(ServerDevice serverDevice) {
            cameraConnectionController.onDeviceFound(serverDevice);
            final String deviceAddress = serverDevice.getDDUrl();

            Runnable updateUITask = new Runnable() {
                @Override
                public void run() {
                    deviceStatus.setText(deviceAddress);
                }
            };

            runOnUiThread(updateUITask);
        }

        @Override
        public void onFinished() {

            Runnable updateUITask = new Runnable() {
                @Override
                public void run() {
                    swipeContainer.setRefreshing(false);
                }
            };
            runOnUiThread(updateUITask);
            CameraConnectionController.openConnection();
        }

        @Override
        public void onErrorFinished() {

        }
    };

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
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

}
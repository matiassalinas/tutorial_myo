package io.github.matiassalinas.myo_tutorial;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.thalmic.myo.AbstractDeviceListener;
import com.thalmic.myo.Arm;
import com.thalmic.myo.DeviceListener;
import com.thalmic.myo.Hub;
import com.thalmic.myo.Myo;
import com.thalmic.myo.Pose;
import com.thalmic.myo.Quaternion;
import com.thalmic.myo.XDirection;
import com.thalmic.myo.scanner.ScanActivity;

public class MainActivity extends AppCompatActivity {
    private TextView mTextView;
    private TextView mLockStateView;

    private DeviceListener mListener = new AbstractDeviceListener() {
        // onConnect() cuando el Myo se conecta.
        @Override
        public void onConnect(Myo myo, long timestamp) {
            mTextView.setTextColor(Color.CYAN);
        }
        // onDisconnect() cuando el Myo se desconecta
        @Override
        public void onDisconnect(Myo myo, long timestamp) {
            mTextView.setTextColor(Color.RED);
        }
        // onArmSync() cuando el Myo se sincroniza. Se mostrara en que brazo se tiene el dispositivo
        @Override
        public void onArmSync(Myo myo, long timestamp, Arm arm, XDirection xDirection) {
            mTextView.setText(myo.getArm() == Arm.LEFT ? R.string.arm_left : R.string.arm_right);
        }
        // onArmUnsync() cuando el Myo se desincroniza
        @Override
        public void onArmUnsync(Myo myo, long timestamp) {
            mTextView.setText(R.string.hello_world);
            //addHistorial("Desincronizado");
        }
        // onUnlock() cuando el Myo se desbloquea
        @Override
        public void onUnlock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.unlocked);
        }
        // onLock() cuando el Myo se bloquea
        @Override
        public void onLock(Myo myo, long timestamp) {
            mLockStateView.setText(R.string.locked);
        }
        // onOrientationData() cuando se mueve el brazo con el Myo
        @Override
        public void onOrientationData(Myo myo, long timestamp, Quaternion rotation) {
            // Calculate Euler angles (roll, pitch, and yaw) from the quaternion.
            float roll = (float) Math.toDegrees(Quaternion.roll(rotation));
            float pitch = (float) Math.toDegrees(Quaternion.pitch(rotation));
            float yaw = (float) Math.toDegrees(Quaternion.yaw(rotation));
            // Adjust roll and pitch for the orientation of the Myo on the arm.
            if (myo.getXDirection() == XDirection.TOWARD_ELBOW) {
                roll *= -1;
                pitch *= -1;
            }
            // Next, we apply a rotation to the text view using the roll, pitch, and yaw.
            mTextView.setRotation(roll);
            mTextView.setRotationX(pitch);
            mTextView.setRotationY(yaw);
        }
        // onPose() cuando se detecta una accion
        @Override
        public void onPose(Myo myo, long timestamp, Pose pose) {
            // Handle the cases of the Pose enumeration, and change the text of the text view
            // based on the pose we receive.
            switch (pose) {
                case UNKNOWN:
                    mTextView.setText(getString(R.string.hello_world));
                    break;
                case REST:
                case DOUBLE_TAP:
                    int restTextId = R.string.hello_world;
                    switch (myo.getArm()) {
                        case LEFT:
                            restTextId = R.string.arm_left;
                            break;
                        case RIGHT:
                            restTextId = R.string.arm_right;
                            break;
                    }
                    mTextView.setText(getString(restTextId));
                    break;
                case FIST:
                    mTextView.setText(getString(R.string.pose_fist));
                    break;
                case WAVE_IN:
                    mTextView.setText(getString(R.string.pose_wavein));
                    break;
                case WAVE_OUT:
                    mTextView.setText(getString(R.string.pose_waveout));
                    break;
                case FINGERS_SPREAD:
                    mTextView.setText(getString(R.string.pose_fingersspread));
                    break;
            }
            if (pose != Pose.UNKNOWN && pose != Pose.REST) {
                // Tell the Myo to stay unlocked until told otherwise. We do that here so you can
                // hold the poses without the Myo becoming locked.
                myo.unlock(Myo.UnlockType.HOLD);
                // Notify the Myo that the pose has resulted in an action, in this case changing
                // the text on the screen. The Myo will vibrate.
                myo.notifyUserAction();
            } else {
                // Tell the Myo to stay unlocked only for a short period. This allows the Myo to
                // stay unlocked while poses are being performed, but lock after inactivity.
                myo.unlock(Myo.UnlockType.TIMED);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLockStateView = (TextView) findViewById(R.id.lock_state);
        mTextView = (TextView) findViewById(R.id.text);
        //Hub Singleton
        Hub hub = Hub.getInstance();
        if (!hub.init(this)) {
            Toast.makeText(this, "No se pudo inicializar el Hub", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        // DeviceListener callbacks.
        hub.addListener(mListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sync, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.sync:
                Toast.makeText(this, "Sync", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, ScanActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

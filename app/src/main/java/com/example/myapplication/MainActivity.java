package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    public native void initNetwork(String ip, int port);
    public native void sendInput(short lx, short ly, short rx, short ry, int btns);

    private short leftStickX = 0;
    private short leftStickY = 0;
    private short rightStickX = 0;
    private short rightStickY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initNetwork("192.168.3.5", 8888);

        JoystickView joystickLeft = findViewById(R.id.joystickLeft);
        JoystickView joystickRight = findViewById(R.id.joystickRight);

        joystickLeft.setJoystickListener((xPercent, yPercent) -> {
            leftStickX = (short) (xPercent * 32767);
            leftStickY = (short) (yPercent * 32767);
            sendCombinedInput();
        });

        joystickRight.setJoystickListener((xPercent, yPercent) -> {
            rightStickX = (short) (xPercent * 32767);
            rightStickY = (short) (yPercent * 32767);
            sendCombinedInput();
        });
    }

    private void sendCombinedInput() {
        sendInput(leftStickX, leftStickY, rightStickX, rightStickY, 0);
    }
}

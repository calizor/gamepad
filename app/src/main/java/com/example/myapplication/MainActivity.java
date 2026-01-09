package com.example.myapplication;import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;


public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("native-lib");
    }

    public native String initNetwork(String ip);
    public native void sendInput(short lx, short ly, short rx, short ry, int btns);


    private short leftStickX = 0;
    private short leftStickY = 0;
    private short rightStickX = 0;
    private short rightStickY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Получаем IP-адрес устройства
        String ipAddress = getWifiIpAddress(this);
        String serverAddres = initNetwork(ipAddress);





        // Выводим IP на экран для отладки
        TextView ipTextView = findViewById(R.id.statusText); // Убедитесь, что у вас есть TextView с таким id в activity_main.xml
        if (ipTextView != null) {
            ipTextView.setText("IP: " + (serverAddres != null ? serverAddres : "Not Found"));
        }

        // Используем полученный IP. Если IP не найден, ничего не делаем.
        if (ipAddress != null) {

            Log.d("NetworkInit", "Initializing with IP: " + ipAddress);
        } else {
            Log.e("NetworkInit", "Could not get Wi-Fi IP address.");
            // Можно показать пользователю сообщение об ошибке
        }


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

    /**
     * Получает IPv4 адрес устройства в сети Wi-Fi.
     * @param context Контекст приложения.
     * @return Строка с IP-адресом или null, если не удалось его получить.
     */
    private String getWifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (wifiManager != null) {
            // Formatter.formatIpAddress устарел, но отлично работает для преобразования int в строку
            // и не требует дополнительных проверок на IPv4/IPv6.
            @SuppressWarnings("deprecation")
            String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
            // Проверка, что IP не равен "0.0.0.0" (что означает отсутствие подключения)
            if ("0.0.0.0".equals(ipAddress)) {
                return null;
            }
            return ipAddress;
        }
        return null;
    }
}

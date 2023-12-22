package com.example.samplesensorproviderapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.hivemq.client.mqtt.datatypes.MqttQos;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client;

import java.nio.charset.StandardCharsets;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    public static final String brokerURI = "ec2-44-217-65-214.compute-1.amazonaws.com";

    Activity thisActivity;

    private SensorManager sensorManager;
    private LightSensorAccess lightSensorAccess;
    private TemperatureSensorAccess temperatureSensorAccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_access_sensors);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        TextView textViewLuminosity = (TextView) findViewById(R.id.textViewLuminosity);
        TextView textViewTemperature = (TextView) findViewById(R.id.textViewTemperature);

        lightSensorAccess = new LightSensorAccess(sensorManager, textViewLuminosity);
        temperatureSensorAccess = new TemperatureSensorAccess(sensorManager, textViewTemperature);

        Timer sensorsTimer = new Timer();

        // Enviar os daods dos sensores a cada 1 seg
        sensorsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                publishMessage("luminosity", textViewLuminosity.getText().toString());
                publishMessage("temperature", textViewTemperature.getText().toString());
            }
        },
        1000,
        1000);

    }

    public void publishMessage(String topic, String payload) {

        Mqtt5BlockingClient client = Mqtt5Client.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(brokerURI)
                .buildBlocking();

        client.connect();
        client.publishWith()
                .topic(topic)
                .qos(MqttQos.AT_LEAST_ONCE)
                .payload(payload.getBytes())
                .send();
        client.disconnect();
    }


}
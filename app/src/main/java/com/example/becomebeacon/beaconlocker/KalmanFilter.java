package com.example.becomebeacon.beaconlocker;

/**
 * Created by 함상혁입니다 on 2017-04-24.
 */

public class KalmanFilter {
    public double X = 0;           // Filtered Value(���� ���Ⱚ)
    public double Q = 0.00001;     // Process Noise
    public double R = 0.001;       // Sensor Noise

    public double P = 1;           // Estimated Error
    public double K;               // Kalman Gain

    public KalmanFilter() {

    }

    KalmanFilter(double initValue) {
        X = initValue;
    }

    private void measurementUpdate(){
        K = (P + Q) / (P + Q + R);
        P = R * (P + Q) / (R + P + Q);
    }

    public double update(double measurement){
        measurementUpdate();
        X = X + (measurement - X) * K;

        return X;
    }
}

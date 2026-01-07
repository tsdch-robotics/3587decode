package org.firstinspires.ftc.teamcode.opmodes.test;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

/**
 * Test OpMode for color sensor calibration.
 * Displays raw and HSV color values.
 */
@TeleOp(name = "Color Sensor Test", group = "Test")
public class ColorSensorTest extends LinearOpMode {

    NormalizedColorSensor colorSensor;

    @Override
    public void runOpMode() {
        float gain = 2;
        final float[] hsvValues = new float[3];

        colorSensor = hardwareMap.get(NormalizedColorSensor.class, "sensor_color");
        colorSensor.setGain(gain);

        waitForStart();

        while (opModeIsActive()) {
            telemetry.addData("Gain", gain);

            NormalizedRGBA colors = colorSensor.getNormalizedColors();
            Color.colorToHSV(colors.toColor(), hsvValues);

            telemetry.addLine()
                    .addData("Red", "%.3f", colors.red)
                    .addData("Green", "%.3f", colors.green)
                    .addData("Blue", "%.3f", colors.blue);
            telemetry.addLine()
                    .addData("Hue", "%.3f", hsvValues[0])
                    .addData("Saturation", "%.3f", hsvValues[1])
                    .addData("Value", "%.3f", hsvValues[2]);
            telemetry.addData("Alpha", "%.3f", colors.alpha);

            // Color detection logic
            if (hsvValues[0] > 200) {
                telemetry.addData("Detected Color", "PURPLE");
            } else if (hsvValues[0] >= 130 && hsvValues[0] < 200) {
                telemetry.addData("Detected Color", "GREEN");
            } else {
                telemetry.addData("Detected Color", "NONE");
            }

            telemetry.update();
        }
    }
}


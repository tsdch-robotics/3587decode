package org.firstinspires.ftc.teamcode.subsystems.vision;

import android.graphics.Color;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.Subsystem;

/**
 * Color sensors subsystem for detecting game piece colors.
 * Manages three color sensors and provides color detection logic.
 */
public class ColorSensors extends Subsystem {

    private NormalizedColorSensor colorSensor1;
    private NormalizedColorSensor colorSensor2;
    private NormalizedColorSensor colorSensor3;
    
    // Cached color readings
    private float[] hsvValues1 = new float[3];
    private float[] hsvValues2 = new float[3];
    private float[] hsvValues3 = new float[3];
    
    private DetectedColor color1 = DetectedColor.NONE;
    private DetectedColor color2 = DetectedColor.NONE;
    private DetectedColor color3 = DetectedColor.NONE;
    
    // Hue thresholds for color detection
    public static final float PURPLE_THRESHOLD = 200;
    public static final float GREEN_MIN_THRESHOLD = 130;
    public static final float GREEN_MAX_THRESHOLD = 200;

    public enum DetectedColor {
        PURPLE,
        GREEN,
        NONE
    }

    public ColorSensors(HardwareMap hardwareMap) {
        float gain = 2;
        
        colorSensor1 = hardwareMap.get(NormalizedColorSensor.class, "Color1");
        colorSensor1.setGain(gain);
        
        colorSensor2 = hardwareMap.get(NormalizedColorSensor.class, "Color2");
        colorSensor2.setGain(gain);
        
        colorSensor3 = hardwareMap.get(NormalizedColorSensor.class, "Color3");
        colorSensor3.setGain(gain);
    }

    @Override
    public void periodic() {
        // Read colors from sensors
        NormalizedRGBA colors1 = colorSensor1.getNormalizedColors();
        NormalizedRGBA colors2 = colorSensor2.getNormalizedColors();
        NormalizedRGBA colors3 = colorSensor3.getNormalizedColors();
        
        // Convert to HSV
        Color.colorToHSV(colors1.toColor(), hsvValues1);
        Color.colorToHSV(colors2.toColor(), hsvValues2);
        Color.colorToHSV(colors3.toColor(), hsvValues3);
        
        // Detect colors
        color1 = detectColor(hsvValues1[0]);
        color2 = detectColor(hsvValues2[0]);
        color3 = detectColor(hsvValues3[0]);
    }

    @Override
    public void writeTelemetry(Telemetry telemetry) {
        telemetry.addData("Color1", "%s (H:%.1f)", color1, hsvValues1[0]);
        telemetry.addData("Color2", "%s (H:%.1f)", color2, hsvValues2[0]);
        telemetry.addData("Color3", "%s (H:%.1f)", color3, hsvValues3[0]);
    }

    /**
     * Detect color from hue value.
     */
    private DetectedColor detectColor(float hue) {
        if (hue > PURPLE_THRESHOLD) {
            return DetectedColor.PURPLE;
        } else if (hue >= GREEN_MIN_THRESHOLD && hue < GREEN_MAX_THRESHOLD) {
            return DetectedColor.GREEN;
        } else {
            return DetectedColor.NONE;
        }
    }

    // =========== GETTERS ===========

    /**
     * Get detected color for sensor 1.
     */
    public DetectedColor getColor1() {
        return color1;
    }

    /**
     * Get detected color for sensor 2.
     */
    public DetectedColor getColor2() {
        return color2;
    }

    /**
     * Get detected color for sensor 3.
     */
    public DetectedColor getColor3() {
        return color3;
    }

    /**
     * Get raw HSV values for sensor 1.
     */
    public float[] getHSV1() {
        return hsvValues1;
    }

    /**
     * Get raw HSV values for sensor 2.
     */
    public float[] getHSV2() {
        return hsvValues2;
    }

    /**
     * Get raw HSV values for sensor 3.
     */
    public float[] getHSV3() {
        return hsvValues3;
    }

    /**
     * Check if any sensor detects purple.
     */
    public boolean anyPurple() {
        return color1 == DetectedColor.PURPLE || 
               color2 == DetectedColor.PURPLE || 
               color3 == DetectedColor.PURPLE;
    }

    /**
     * Check if any sensor detects green.
     */
    public boolean anyGreen() {
        return color1 == DetectedColor.GREEN || 
               color2 == DetectedColor.GREEN || 
               color3 == DetectedColor.GREEN;
    }
}


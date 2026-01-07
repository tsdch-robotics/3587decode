package org.firstinspires.ftc.teamcode.common;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.subsystems.drive.MecanumDrive;
import org.firstinspires.ftc.teamcode.subsystems.intake.Intake;
import org.firstinspires.ftc.teamcode.subsystems.shooter.Shooter;
import org.firstinspires.ftc.teamcode.subsystems.servos.PodServos;
import org.firstinspires.ftc.teamcode.subsystems.vision.ColorSensors;

import java.util.ArrayList;
import java.util.List;

/**
 * Central hub for all subsystems.
 * Initializes and coordinates robot hardware.
 * 
 * Subsystems: Drive, Intake, Shooter, PodServos, ColorSensors
 */
public class Robot {
    // --- Infrastructure ---
    private List<LynxModule> allHubs;
    private VoltageSensor batteryVoltageSensor;

    // --- Subsystems ---
    public MecanumDrive drive;
    public Intake intake;
    public Shooter shooter;
    public PodServos podServos;
    public ColorSensors colorSensors;
    
    private ArrayList<Subsystem> subsystems = new ArrayList<>();

    // --- State ---
    public double batteryVoltage = 12.0;

    public Robot(HardwareMap hardwareMap, Telemetry telemetry) {
        try {
            // Setup bulk reads for faster loop times
            allHubs = hardwareMap.getAll(LynxModule.class);
            for (LynxModule module : allHubs) {
                module.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
            }

            batteryVoltageSensor = hardwareMap.voltageSensor.iterator().next();

            // Initialize subsystems
            drive = new MecanumDrive(hardwareMap);
            intake = new Intake(hardwareMap);
            shooter = new Shooter(hardwareMap);
            podServos = new PodServos(hardwareMap);
            colorSensors = new ColorSensors(hardwareMap);
            
            // Register subsystems for periodic updates
            subsystems.add(drive);
            subsystems.add(intake);
            subsystems.add(shooter);
            subsystems.add(podServos);
            subsystems.add(colorSensors);

        } catch (Exception e) {
            throw new RuntimeException("Hardware initialization failed. Check your robot configuration. Error: " + e.getMessage());
        }
    }

    /**
     * Read hardware inputs (call at start of each loop).
     * Clears bulk cache for fresh sensor readings.
     */
    public void read() {
        for (LynxModule module : allHubs) {
            module.clearBulkCache();
        }
        batteryVoltage = batteryVoltageSensor.getVoltage();
    }

    /**
     * Update all subsystems (call every loop).
     * Each subsystem's periodic() method updates its internal state.
     */
    public void periodic() {
        for (Subsystem subsystem : subsystems) {
            subsystem.periodic();
        }
    }

    /**
     * Write telemetry data (call at end of each loop).
     * Outputs subsystem status and robot state to driver station.
     */
    public void write(Telemetry telemetry) {
        for (Subsystem subsystem : subsystems) {
            subsystem.writeTelemetry(telemetry);
        }
        telemetry.addData("Voltage", "%.1f V", batteryVoltage);
        telemetry.update();
    }
    
    /**
     * Calculate voltage compensation scale factor.
     * Returns >1.0 when battery is below 12V to compensate for voltage drop.
     * @return Scale factor for motor power compensation
     */
    public double getVoltageScale() {
        return 12.0 / Math.max(batteryVoltage, 10.0);
    }
}


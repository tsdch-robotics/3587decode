package org.firstinspires.ftc.teamcode.subsystems.intake;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.Subsystem;

/**
 * Intake subsystem controlling front and back intake motors.
 */
public class Intake extends Subsystem {

    private DcMotor frontIntake;
    private DcMotor backIntake;
    
    private double frontPower = 0;
    private double backPower = 0;

    public Intake(HardwareMap hardwareMap) {
        frontIntake = hardwareMap.get(DcMotor.class, "FIntake");
        frontIntake.setDirection(DcMotorSimple.Direction.REVERSE);
        
        backIntake = hardwareMap.get(DcMotor.class, "BIntake");
        backIntake.setDirection(DcMotorSimple.Direction.FORWARD);
    }

    @Override
    public void periodic() {
        frontIntake.setPower(frontPower);
        backIntake.setPower(backPower);
    }

    @Override
    public void writeTelemetry(Telemetry telemetry) {
        telemetry.addData("Front Intake", "%.2f", frontPower);
        telemetry.addData("Back Intake", "%.2f", backPower);
    }

    // =========== CONTROL METHODS ===========

    /**
     * Run front intake at full power.
     */
    public void runFrontIntake() {
        frontPower = 1.0;
    }

    /**
     * Run back intake at full power.
     */
    public void runBackIntake() {
        backPower = 1.0;
    }

    /**
     * Run both intakes at full power.
     */
    public void runBothIntakes() {
        frontPower = 1.0;
        backPower = 1.0;
    }

    /**
     * Stop front intake.
     */
    public void stopFrontIntake() {
        frontPower = 0;
    }

    /**
     * Stop back intake.
     */
    public void stopBackIntake() {
        backPower = 0;
    }

    /**
     * Stop both intakes.
     */
    public void stopAll() {
        frontPower = 0;
        backPower = 0;
    }

    /**
     * Reverse front intake (for unjamming).
     */
    public void reverseFrontIntake() {
        frontPower = -1.0;
    }

    /**
     * Reverse back intake (for unjamming).
     */
    public void reverseBackIntake() {
        backPower = -1.0;
    }

    /**
     * Set front intake power directly.
     */
    public void setFrontPower(double power) {
        frontPower = power;
    }

    /**
     * Set back intake power directly.
     */
    public void setBackPower(double power) {
        backPower = power;
    }

    /**
     * Check if front intake is running.
     */
    public boolean isFrontRunning() {
        return frontPower != 0;
    }

    /**
     * Check if back intake is running.
     */
    public boolean isBackRunning() {
        return backPower != 0;
    }
}


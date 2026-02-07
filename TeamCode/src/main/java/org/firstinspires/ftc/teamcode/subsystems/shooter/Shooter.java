package org.firstinspires.ftc.teamcode.subsystems.shooter;

import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.Subsystem;

/**
 * Shooter subsystem controlling the flywheel motor and hood servo.
 */
public class Shooter extends Subsystem {

    private DcMotorEx shootMotor;
    private Servo hood;
    
    private double shooterPower = 0;
    private double hoodPosition = 0;
    
    // TODO: VERIFY ON NEW ROBOT — Legacy TeleOps used hood.setDirection(FORWARD) with values 0.07-0.099.
    //  This subsystem uses REVERSE direction with 0.5. Test on actual hardware before changing.
    // Hood position constants
    public static final double HOOD_DOWN = 0.0;
    public static final double HOOD_UP = 0.5;

    public Shooter(HardwareMap hardwareMap) {
        shootMotor = hardwareMap.get(DcMotorEx.class, "Shoot");
        shootMotor.setDirection(DcMotorSimple.Direction.REVERSE);
        shootMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
        
        hood = hardwareMap.get(Servo.class, "Hood");
        hood.setDirection(Servo.Direction.REVERSE);
        hood.setPosition(HOOD_DOWN);
        hoodPosition = HOOD_DOWN;
    }

    @Override
    public void periodic() {
        shootMotor.setPower(shooterPower);
        hood.setPosition(hoodPosition);
    }

    @Override
    public void writeTelemetry(Telemetry telemetry) {
        telemetry.addData("Shooter Power", "%.2f", shooterPower);
        telemetry.addData("Shooter Velocity", "%.0f", shootMotor.getVelocity());
        telemetry.addData("Hood Position", "%.2f", hoodPosition);
    }

    // =========== SHOOTER CONTROL ===========

    /**
     * Start the shooter at full power.
     */
    public void shoot() {
        shooterPower = 1.0;
    }

    /**
     * Set shooter power.
     */
    public void setShooterPower(double power) {
        shooterPower = power;
    }

    /**
     * Stop the shooter.
     */
    public void stopShooter() {
        shooterPower = 0;
    }

    /**
     * Get current shooter velocity in ticks per second.
     */
    public double getVelocity() {
        return shootMotor.getVelocity();
    }

    /**
     * Check if shooter is running.
     */
    public boolean isRunning() {
        return shooterPower > 0;
    }

    // =========== HOOD CONTROL ===========

    /**
     * Set hood to down position.
     */
    public void hoodDown() {
        hoodPosition = HOOD_DOWN;
    }

    /**
     * Set hood to up position.
     */
    public void hoodUp() {
        hoodPosition = HOOD_UP;
    }

    /**
     * Set hood position directly.
     */
    public void setHoodPosition(double position) {
        hoodPosition = Math.max(0, Math.min(1, position));
    }

    /**
     * Get current hood position.
     */
    public double getHoodPosition() {
        return hoodPosition;
    }
}


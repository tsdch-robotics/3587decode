package org.firstinspires.ftc.teamcode.subsystems.drive;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.Subsystem;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

/**
 * Mecanum drive subsystem wrapping Pedro Pathing Follower.
 * Provides both teleop and autonomous driving capabilities.
 * Includes heading lock (PedroLock) for consistent shot alignment.
 */
public class MecanumDrive extends Subsystem {
    
    public Follower follower;
    
    private boolean slowMode = false;
    private double slowModeMultiplier = 0.5;
    private boolean robotCentric = true;

    // Heading lock state
    private boolean headingLockEnabled = false;
    private double headingLockTarget = 0;
    private static final double HEADING_LOCK_KP = 0.5;

    public MecanumDrive(HardwareMap hardwareMap) {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(new Pose());
    }

    @Override
    public void periodic() {
        follower.update();
    }

    @Override
    public void writeTelemetry(Telemetry telemetry) {
        telemetry.addData("Drive Position", follower.getPose());
        telemetry.addData("Drive Velocity", follower.getVelocity());
        telemetry.addData("Slow Mode", slowMode ? "ON" : "off");
        if (headingLockEnabled) {
            telemetry.addData("Heading Lock", "ON (target=%.1f deg)",
                    Math.toDegrees(headingLockTarget));
        }
    }

    // =========== TELEOP METHODS ===========
    
    /**
     * Start teleop driving mode.
     * Call this in OpMode start().
     */
    public void startTeleop() {
        follower.startTeleopDrive();
    }

    /**
     * Set drive from gamepad input.
     * If heading lock is enabled, the turn axis is overridden with the P-controller output.
     * @param gamepad The gamepad to read from
     */
    public void setTeleOpDrive(Gamepad gamepad) {
        double multiplier = slowMode ? slowModeMultiplier : 1.0;
        double turn;
        if (headingLockEnabled) {
            turn = computeHeadingLockTurn();
        } else {
            turn = -gamepad.right_stick_x * multiplier;
        }
        follower.setTeleOpDrive(
            -gamepad.left_stick_y * multiplier,
            -gamepad.left_stick_x * multiplier,
            turn,
            robotCentric
        );
    }

    /**
     * Set drive from raw values.
     */
    public void setTeleOpDrive(double forward, double strafe, double turn) {
        double multiplier = slowMode ? slowModeMultiplier : 1.0;
        follower.setTeleOpDrive(
            forward * multiplier,
            strafe * multiplier,
            turn * multiplier,
            robotCentric
        );
    }

    // =========== HEADING LOCK ===========

    /**
     * Enable heading lock to a target angle.
     * When enabled, the turn axis is overridden with a P-controller that holds the target heading.
     * @param targetRadians Target heading in radians
     */
    public void enableHeadingLock(double targetRadians) {
        headingLockEnabled = true;
        headingLockTarget = targetRadians;
    }

    /**
     * Disable heading lock, returning turn control to the driver.
     */
    public void disableHeadingLock() {
        headingLockEnabled = false;
    }

    /**
     * Check if heading lock is enabled.
     */
    public boolean isHeadingLockEnabled() {
        return headingLockEnabled;
    }

    /**
     * Compute turn power from heading error using a P-controller.
     * Ported from BlueSmallRoboto PedroLock() method.
     */
    private double computeHeadingLockTurn() {
        double currentHeading = follower.getPose().getHeading();
        double headingError = headingLockTarget - currentHeading;
        // Normalize error to [-PI, PI]
        while (headingError > Math.PI) headingError -= 2 * Math.PI;
        while (headingError < -Math.PI) headingError += 2 * Math.PI;
        return headingError * HEADING_LOCK_KP;
    }

    // =========== SLOW MODE ===========

    /**
     * Toggle slow mode on/off.
     */
    public void toggleSlowMode() {
        slowMode = !slowMode;
    }

    /**
     * Set slow mode state.
     */
    public void setSlowMode(boolean enabled) {
        slowMode = enabled;
    }

    /**
     * Adjust slow mode multiplier.
     */
    public void adjustSlowModeMultiplier(double delta) {
        slowModeMultiplier = Math.max(0.1, Math.min(1.0, slowModeMultiplier + delta));
    }

    /**
     * Set whether drive is robot-centric (true) or field-centric (false).
     */
    public void setRobotCentric(boolean robotCentric) {
        this.robotCentric = robotCentric;
    }

    // =========== AUTONOMOUS METHODS ===========

    /**
     * Set the starting pose for autonomous.
     */
    public void setStartingPose(Pose pose) {
        follower.setStartingPose(pose);
    }

    /**
     * Follow a path.
     */
    public void followPath(Path path) {
        follower.followPath(path);
    }

    /**
     * Follow a path chain.
     */
    public void followPath(PathChain pathChain, boolean holdEnd) {
        follower.followPath(pathChain, holdEnd);
    }

    /**
     * Check if the follower is currently busy following a path.
     */
    public boolean isBusy() {
        return follower.isBusy();
    }

    /**
     * Get current pose.
     */
    public Pose getPose() {
        return follower.getPose();
    }

    /**
     * Check if slow mode is enabled.
     */
    public boolean isSlowMode() {
        return slowMode;
    }

    /**
     * Get slow mode multiplier.
     */
    public double getSlowModeMultiplier() {
        return slowModeMultiplier;
    }
}

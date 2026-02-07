package org.firstinspires.ftc.teamcode.opmodes.teleop;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.common.Robot;
import org.firstinspires.ftc.teamcode.subsystems.shooter.Shooter;
import org.firstinspires.ftc.teamcode.utils.GamepadEvents;

/**
 * Main TeleOp for competition.
 * Uses the Robot class and subsystems for clean, organized control.
 * 
 * DRIVER 1 (Gamepad 1) - Movement & Mechanisms:
 * - Left Stick: Drive (strafe)
 * - Right Stick X: Rotate
 * - Right Trigger: Front intake
 * - Left Trigger: Back intake
 * - D-Pad Up: Shooter on (velocity-based)
 * - D-Pad Down: Shooter off
 * - D-Pad Left: Reverse both intakes (unjam)
 * - Left Bumper: Heading lock (hold to aim at goal)
 * - Right Bumper: SpinTop right
 * - B: Stop intakes / Extend Pod1
 * - Y: Extend Pod2
 * - X: Extend Pod3
 * - A: Retract all pods
 * 
 * DRIVER 2 (Gamepad 2) - Settings:
 * - D-Pad Up: Hood up
 * - D-Pad Down: Hood down
 * - Left Bumper: SpinTop left
 * - Right Bumper: Toggle slow mode
 * - X: Increase slow mode multiplier
 * - Y: Decrease slow mode multiplier
 * - A: Start automated path following
 * - B: Stop automated path following
 */
@TeleOp(name = "Main TeleOp", group = "Competition")
public class MainTeleOp extends OpMode {
    
    private Robot robot;
    private GamepadEvents player1;
    private GamepadEvents player2;
    
    private boolean automatedDrive = false;

    @Override
    public void init() {
        robot = new Robot(hardwareMap, telemetry);
        player1 = new GamepadEvents(gamepad1);
        player2 = new GamepadEvents(gamepad2);
        
        // Load pose from autonomous if available
        if (Robot.lastAutoPose != null) {
            robot.drive.setStartingPose(Robot.lastAutoPose);
            telemetry.addLine("=== Main TeleOp Ready ===");
            telemetry.addData("Pose from Auto", Robot.lastAutoPose);
        } else {
            telemetry.addLine("=== Main TeleOp Ready ===");
            telemetry.addLine("(No auto pose — starting at origin)");
        }
        telemetry.addLine("Press START to begin");
        telemetry.update();
    }

    @Override
    public void start() {
        robot.drive.startTeleop();
    }

    @Override
    public void loop() {
        // === READ INPUTS ===
        robot.read();

        // === DRIVER 1: MOVEMENT ===
        if (!automatedDrive) {
            robot.drive.setTeleOpDrive(gamepad1);
        }

        // === DRIVER 1: INTAKES ===
        if (gamepad1.dpad_left) {
            // Reverse both intakes for unjamming
            robot.intake.reverseFrontIntake();
            robot.intake.reverseBackIntake();
        } else if (gamepad1.right_trigger > 0.5) {
            robot.intake.runFrontIntake();
        } else {
            robot.intake.stopFrontIntake();
        }
        
        if (!gamepad1.dpad_left) {
            if (gamepad1.left_trigger > 0.5) {
                robot.intake.runBackIntake();
            } else {
                robot.intake.stopBackIntake();
            }
        }
        
        // B button stops intakes AND extends Pod1
        if (player1.bPressed()) {
            robot.intake.stopAll();
            robot.podServos.extendPod1();
        }

        // === DRIVER 1: SHOOTER ===
        if (gamepad1.dpad_up) {
            robot.shooter.shootAtVelocity(Shooter.DEFAULT_VELOCITY);
        }
        if (gamepad1.dpad_down) {
            robot.shooter.stopShooter();
        }

        // === DRIVER 1: POD SERVOS ===
        if (gamepad1.y) {
            robot.podServos.extendPod2();
        }
        if (gamepad1.x) {
            robot.podServos.extendPod3();
        }
        if (gamepad1.a) {
            robot.podServos.retractAll();
        }

        // === DRIVER 1: HEADING LOCK ===
        // TODO: Make heading lock target alliance-aware (negative for red, positive for blue)
        if (gamepad1.left_bumper) {
            robot.drive.enableHeadingLock(Math.toRadians(-40));
        } else {
            robot.drive.disableHeadingLock();
        }

        // === DRIVER 1: SPIN TOP ===
        if (gamepad1.right_bumper) {
            robot.podServos.spinRight();
        } else {
            robot.podServos.stopSpin();
        }

        // === DRIVER 2: HOOD CONTROL ===
        if (gamepad2.dpad_down) {
            robot.shooter.hoodDown();
        }
        if (gamepad2.dpad_up) {
            robot.shooter.hoodUp();
        }

        // === DRIVER 2: SPIN TOP LEFT ===
        if (gamepad2.left_bumper) {
            robot.podServos.spinLeft();
        }

        // === DRIVER 2: SLOW MODE ===
        if (player2.rightBumperPressed()) {
            robot.drive.toggleSlowMode();
        }
        if (player2.xPressed()) {
            robot.drive.adjustSlowModeMultiplier(0.25);
        }
        if (player2.yPressed()) {
            robot.drive.adjustSlowModeMultiplier(-0.25);
        }

        // === DRIVER 2: AUTOMATED PATH (if needed) ===
        if (player2.aPressed()) {
            // Start automated path following here if needed
            automatedDrive = true;
        }
        if (automatedDrive && (player2.bPressed() || !robot.drive.isBusy())) {
            robot.drive.startTeleop();
            automatedDrive = false;
        }

        // === UPDATE SUBSYSTEMS ===
        robot.periodic();

        // === UPDATE BUTTON STATES ===
        player1.update();
        player2.update();

        // === TELEMETRY ===
        telemetry.addData("-- DRIVE --", "");
        telemetry.addData("Position", robot.drive.getPose());
        telemetry.addData("Slow Mode", robot.drive.isSlowMode() ? 
            String.format("ON (%.0f%%)", robot.drive.getSlowModeMultiplier() * 100) : "off");
        telemetry.addData("Heading Lock", robot.drive.isHeadingLockEnabled() ? "LOCKED" : "off");
        telemetry.addData("Automated", automatedDrive);
        telemetry.addLine("");
        
        robot.write(telemetry);
    }
}


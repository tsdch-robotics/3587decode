package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.common.Robot;

/**
 * Blue alliance autonomous program.
 * Uses Robot class for subsystem access while maintaining existing path logic.
 *
 * State machine drives paths AND activates intake/shooter at appropriate times:
 *   Drive to score → Shoot (timed) → Drive to pickup (intake ON) → Intake (timed) → repeat
 */
@Autonomous(name = "BigBlueAuto", group = "Competition")
public class BigBlueAuto extends OpMode {

    private Robot robot;
    private Timer pathTimer, actionTimer, opmodeTimer;

    private int pathState;

    // Timing constants — adjust these on the field
    private static final double SHOOT_TIME = 1.5;   // seconds to shoot a volley
    private static final double INTAKE_TIME = 0.5;   // seconds to wait at pickup for intake
    private static final double PATH_TIMEOUT = 5.0;  // max seconds to wait for any path

    // Path poses
    private final Pose startPose = new Pose(16, 128, Math.toRadians(0));
    private final Pose FirstUPPose = new Pose(62, 106, Math.toRadians(125));
    private final Pose scorePose = new Pose(62, 106, Math.toRadians(125));
    private final Pose pickup1Pose = new Pose(20, 116, Math.toRadians(180));
    private final Pose pickup2Pose = new Pose(20, 86, Math.toRadians(180));
    private final Pose MidCurve2 = new Pose(55, 86, Math.toRadians(180));
    private final Pose pickup3Pose = new Pose(20, 66, Math.toRadians(180));
    private final Pose MidCurve3 = new Pose(55, 66, Math.toRadians(180));

    // Paths
    private Path FirstUP;
    private PathChain grabPickup1, scorePickup1, grabPickup2P1, grabPickup2P2;
    private PathChain scorePickup2, grabPickup3P1, grabPickup3P2, scorePickup3;

    @Override
    public void init() {
        pathTimer = new Timer();
        actionTimer = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();

        robot = new Robot(hardwareMap, telemetry);
        robot.drive.setStartingPose(startPose);

        buildPaths();

        telemetry.addLine("=== BigBlueAuto Ready ===");
        telemetry.update();
    }

    private void buildPaths() {
        FirstUP = new Path(new BezierLine(startPose, FirstUPPose));
        FirstUP.setLinearHeadingInterpolation(startPose.getHeading(), FirstUPPose.getHeading());

        grabPickup1 = robot.drive.follower.pathBuilder()
                .addPath(new BezierLine(FirstUPPose, pickup1Pose))
                .setLinearHeadingInterpolation(FirstUPPose.getHeading(), pickup1Pose.getHeading())
                .build();

        scorePickup1 = robot.drive.follower.pathBuilder()
                .addPath(new BezierLine(pickup1Pose, scorePose))
                .setLinearHeadingInterpolation(pickup1Pose.getHeading(), scorePose.getHeading())
                .build();

        grabPickup2P1 = robot.drive.follower.pathBuilder()
                .addPath(new BezierLine(scorePose, MidCurve2))
                .setLinearHeadingInterpolation(scorePose.getHeading(), MidCurve2.getHeading())
                .build();

        grabPickup2P2 = robot.drive.follower.pathBuilder()
                .addPath(new BezierLine(MidCurve2, pickup2Pose))
                .setLinearHeadingInterpolation(MidCurve2.getHeading(), pickup2Pose.getHeading())
                .build();

        scorePickup2 = robot.drive.follower.pathBuilder()
                .addPath(new BezierLine(pickup2Pose, scorePose))
                .setLinearHeadingInterpolation(pickup2Pose.getHeading(), scorePose.getHeading())
                .build();

        grabPickup3P1 = robot.drive.follower.pathBuilder()
                .addPath(new BezierLine(scorePose, MidCurve3))
                .setLinearHeadingInterpolation(scorePose.getHeading(), MidCurve3.getHeading())
                .build();

        grabPickup3P2 = robot.drive.follower.pathBuilder()
                .addPath(new BezierLine(MidCurve3, pickup3Pose))
                .setLinearHeadingInterpolation(MidCurve3.getHeading(), pickup3Pose.getHeading())
                .build();

        scorePickup3 = robot.drive.follower.pathBuilder()
                .addPath(new BezierLine(pickup3Pose, scorePose))
                .setLinearHeadingInterpolation(pickup3Pose.getHeading(), scorePose.getHeading())
                .build();
    }

    @Override
    public void init_loop() {
        // Can add pre-start checks here
    }

    @Override
    public void start() {
        opmodeTimer.resetTimer();
        setPathState(0);
    }

    @Override
    public void loop() {
        robot.read();
        robot.periodic();
        autonomousPathUpdate();

        // Telemetry
        telemetry.addData("Path State", pathState);
        telemetry.addData("Path Timer", "%.1f", pathTimer.getElapsedTimeSeconds());
        telemetry.addData("Action Timer", "%.1f", actionTimer.getElapsedTimeSeconds());
        telemetry.addData("X", robot.drive.getPose().getX());
        telemetry.addData("Y", robot.drive.getPose().getY());
        telemetry.addData("Heading", Math.toDegrees(robot.drive.getPose().getHeading()));
        telemetry.update();
    }

    /**
     * State machine with mechanism actions.
     *
     * States 0-1:   Drive to first score position + shoot preload
     * States 2-4:   Grab pickup 1 → intake wait → drive to score + shoot
     * States 5-9:   Grab pickup 2 (via midcurve) → intake wait → drive to score + shoot
     * States 10-14: Grab pickup 3 (via midcurve) → intake wait → drive to score + shoot
     * State 15-16:  Final shoot → idle
     */
    private void autonomousPathUpdate() {
        switch (pathState) {
            // === PRELOAD: Drive to score position, spin up shooter ===
            case 0:
                robot.shooter.shoot();
                robot.shooter.hoodUp();
                robot.drive.followPath(FirstUP);
                setPathState(1);
                break;

            // Arrived at score → start shooting timer
            case 1:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    actionTimer.resetTimer();
                    setPathState(2);
                }
                break;

            // Shooting preload — wait for shoot time, then go to pickup 1
            case 2:
                if (actionTimer.getElapsedTimeSeconds() > SHOOT_TIME) {
                    robot.shooter.stopShooter();
                    robot.intake.runBothIntakes();
                    robot.drive.followPath(grabPickup1, true);
                    setPathState(3);
                }
                break;

            // === CYCLE 1: Drive to pickup 1, intake running ===
            case 3:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    actionTimer.resetTimer();
                    setPathState(4);
                }
                break;

            // Intake wait at pickup 1
            case 4:
                if (actionTimer.getElapsedTimeSeconds() > INTAKE_TIME) {
                    robot.intake.stopAll();
                    robot.shooter.shoot();
                    robot.drive.followPath(scorePickup1, true);
                    setPathState(5);
                }
                break;

            // Arrived at score → start shooting timer
            case 5:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    actionTimer.resetTimer();
                    setPathState(6);
                }
                break;

            // Shooting cycle 1 — wait, then go to pickup 2
            case 6:
                if (actionTimer.getElapsedTimeSeconds() > SHOOT_TIME) {
                    robot.shooter.stopShooter();
                    robot.intake.runBothIntakes();
                    robot.drive.followPath(grabPickup2P1, true);
                    setPathState(7);
                }
                break;

            // === CYCLE 2: Drive to midcurve 2 (intake stays on) ===
            case 7:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    robot.drive.followPath(grabPickup2P2, true);
                    setPathState(8);
                }
                break;

            // Drive to pickup 2
            case 8:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    actionTimer.resetTimer();
                    setPathState(9);
                }
                break;

            // Intake wait at pickup 2
            case 9:
                if (actionTimer.getElapsedTimeSeconds() > INTAKE_TIME) {
                    robot.intake.stopAll();
                    robot.shooter.shoot();
                    robot.drive.followPath(scorePickup2, true);
                    setPathState(10);
                }
                break;

            // Arrived at score → start shooting timer
            case 10:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    actionTimer.resetTimer();
                    setPathState(11);
                }
                break;

            // Shooting cycle 2 — wait, then go to pickup 3
            case 11:
                if (actionTimer.getElapsedTimeSeconds() > SHOOT_TIME) {
                    robot.shooter.stopShooter();
                    robot.intake.runBothIntakes();
                    robot.drive.followPath(grabPickup3P1, true);
                    setPathState(12);
                }
                break;

            // === CYCLE 3: Drive to midcurve 3 (intake stays on) ===
            case 12:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    robot.drive.followPath(grabPickup3P2, true);
                    setPathState(13);
                }
                break;

            // Drive to pickup 3
            case 13:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    actionTimer.resetTimer();
                    setPathState(14);
                }
                break;

            // Intake wait at pickup 3
            case 14:
                if (actionTimer.getElapsedTimeSeconds() > INTAKE_TIME) {
                    robot.intake.stopAll();
                    robot.shooter.shoot();
                    robot.drive.followPath(scorePickup3, true);
                    setPathState(15);
                }
                break;

            // Arrived at score → start shooting timer
            case 15:
                if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > PATH_TIMEOUT) {
                    actionTimer.resetTimer();
                    setPathState(16);
                }
                break;

            // Final shooting — then stop everything
            case 16:
                if (actionTimer.getElapsedTimeSeconds() > SHOOT_TIME) {
                    robot.shooter.stopShooter();
                    robot.shooter.hoodDown();
                    robot.intake.stopAll();
                    setPathState(-1);
                }
                break;
        }
    }

    private void setPathState(int pState) {
        pathState = pState;
        pathTimer.resetTimer();
    }

    @Override
    public void stop() {
        // Save final pose for teleop handoff
        Robot.lastAutoPose = robot.drive.getPose();
    }
}

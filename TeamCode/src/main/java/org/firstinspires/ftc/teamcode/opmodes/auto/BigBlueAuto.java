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
 */
@Autonomous(name = "BigBlueAuto", group = "Competition")
public class BigBlueAuto extends OpMode {

    private Robot robot;
    private Timer pathTimer, opmodeTimer;

    private int pathState;
    
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

        // scorePickup3 = robot.drive.follower.pathBuilder()
        //         .addPath(new BezierLine(pickup3Pose, scorePose))
        //         .setLinearHeadingInterpolation(pickup3Pose.getHeading(), scorePose.getHeading())
        //         .build();
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
        telemetry.addData("X", robot.drive.getPose().getX());
        telemetry.addData("Y", robot.drive.getPose().getY());
        telemetry.addData("Heading", Math.toDegrees(robot.drive.getPose().getHeading()));
        telemetry.update();
    }

    private void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                robot.drive.followPath(FirstUP);
                setPathState(1);
                break;
                
            case 1:
                if (!robot.drive.isBusy()) {
                    // Score Preload
                    robot.drive.followPath(grabPickup1, true);
                    setPathState(2);
                }
                break;
                
            case 2:
                if (!robot.drive.isBusy()) {
                    // Grab Sample
                    robot.drive.followPath(scorePickup1, true);
                    setPathState(3);
                }
                break;
                
            case 3:
                if (!robot.drive.isBusy()) {
                    // Score Sample
                    robot.drive.followPath(grabPickup2P1, true);
                    setPathState(4);
                }
                break;
                
            case 4:
                if (!robot.drive.isBusy()) {
                    robot.drive.followPath(grabPickup2P2, true);
                    setPathState(5);
                }
                break;
                
            case 5:
                if (!robot.drive.isBusy()) {
                    robot.drive.followPath(scorePickup2, true);
                    setPathState(6);
                }
                break;
                
            case 6:
                if (!robot.drive.isBusy()) {
                    robot.drive.followPath(grabPickup3P1, true);
                    setPathState(7);
                }
                break;
                
            case 7:
                if (!robot.drive.isBusy()) {
                    robot.drive.followPath(grabPickup3P2, true);
                    setPathState(8);
                }
                break;
                
            case 8:
                if (!robot.drive.isBusy()) {
                    // robot.drive.followPath(scorePickup3, true);
                    setPathState(9);
                }
                break;
                
            case 9:
                if (!robot.drive.isBusy()) {
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
        // Cleanup if needed
    }
}


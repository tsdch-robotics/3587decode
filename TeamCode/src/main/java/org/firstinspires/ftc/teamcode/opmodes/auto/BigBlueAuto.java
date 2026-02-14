package org.firstinspires.ftc.teamcode.opmodes.auto; // make sure this aligns with class location
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import  com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

@Autonomous(name = "BigBlueAuto", group = "Examples")
public class BigBlueAuto extends OpMode {
    public DcMotor Intake;
    public Servo Rail;
    public DcMotorEx Shoot1;
    public DcMotorEx Shoot2;
    double ticksPerRevolution = 28;
    double currentRPM = 0;
    //public DcMotor Lift;
    // public Servo Hood;
    //  public Servo SpinTop;

    double LP;
    double HeadingError;

    private Follower follower;
    private Timer pathTimer, actionTimer, opmodeTimer;
    private int pathState;
    private final Pose startPose = new Pose(0, 0, Math.toRadians(0)); // Start Pose of our robot.
    private final Pose FirstUPPose = new Pose(-22, -.5, Math.toRadians(0));//score PreLoad
    private final Pose scorePose = new Pose(-22, -.5, Math.toRadians(0)); // Scoring Pose of our robot.
    private final Pose pickup1Pose = new Pose(43, 8, Math.toRadians(45));
    private final Pose GrabLine1Pose = new Pose(32, 28, Math.toRadians(45));
    private final Pose pickup2Pose = new Pose(57, 28, Math.toRadians(45));
    private final Pose GrabLine2Pose = new Pose(38, 44,Math.toRadians(45));
    private final Pose pickup3Pose = new Pose(73, 43, Math.toRadians(45));
    private final Pose GrabLine3Pose = new Pose(53, 60, Math.toRadians(45));
    private final Pose OffLinePose = new Pose(10,-19,Math.toRadians(45));
    private final Pose OffTickPose = new Pose (32, 28, 44);

    //private Path scorePreload;
    private Path FirstUP;
    private PathChain grabPickup1, grabPickup1P2, scorePickup1, grabPickup2P1, grabPickup2P2, scorePickup2, grabPickup3P1, grabPickup3P2, scorePickup3, EndPoint, EndPoint2;

    public void buildPaths() {
        /* This is our scorePreload path. We are using a BezierLine, which is a straight line. */
        // scorePreload = new Path(new BezierLine(startPose, scorePose));
        // scorePreload.setLinearHeadingInterpolation(startPose.getHeading(), scorePose.getHeading());

        FirstUP = new Path(new BezierLine(startPose, FirstUPPose));
        FirstUP.setLinearHeadingInterpolation(startPose.getHeading(), FirstUPPose.getHeading());

    /* Here is an example for Constant Interpolation
    scorePreload.setConstantInterpolation(startPose.getHeading()); */

        /* This is our grabPickup1 PathChain. We are using a single path with a BezierLine, which is a straight line. */
        grabPickup1 = follower.pathBuilder()
                .addPath(new BezierLine(FirstUPPose, pickup1Pose))
                .setLinearHeadingInterpolation(FirstUPPose.getHeading(), pickup1Pose.getHeading())
                .build();
        grabPickup1P2 = follower.pathBuilder()
                .addPath(new BezierLine(pickup1Pose, GrabLine1Pose))
                .setLinearHeadingInterpolation(pickup1Pose.getHeading(), GrabLine1Pose.getHeading())
                .build();
        /* This is our grabPickup2 PathChain. We are using a single path with a BezierLine, which is a straight line. */
        scorePickup1 = follower.pathBuilder()
                .addPath(new BezierLine(GrabLine1Pose, scorePose))
                .setLinearHeadingInterpolation(GrabLine1Pose.getHeading(), scorePose.getHeading())
                .build();
        grabPickup2P1 = follower.pathBuilder()
                .addPath(new BezierLine(scorePose, pickup2Pose))
                .setLinearHeadingInterpolation(scorePose.getHeading(), pickup2Pose.getHeading())
                .build();
        // /* This is our scorePickup2 PathChain. We are using a single path with a BezierLine, which is a straight line. */
        grabPickup2P2 = follower.pathBuilder()
                .addPath(new BezierLine(pickup2Pose, GrabLine2Pose))
                .setLinearHeadingInterpolation(pickup2Pose.getHeading(), GrabLine2Pose.getHeading())
                .build();
        // /* This is our grabPickup3 PathChain. We are using a single path with a BezierLine, which is a straight line. */
        scorePickup2 = follower.pathBuilder()
                .addPath(new BezierLine(GrabLine2Pose, scorePose))
                .setLinearHeadingInterpolation(GrabLine2Pose.getHeading(), scorePose.getHeading())
                .build();
        grabPickup3P1 = follower.pathBuilder()
                .addPath(new BezierLine( scorePose, pickup3Pose))
                .setLinearHeadingInterpolation(scorePose.getHeading(), pickup3Pose.getHeading())
                .build();
        grabPickup3P2 = follower.pathBuilder()
                .addPath(new BezierLine(pickup3Pose, GrabLine3Pose))
                .setLinearHeadingInterpolation(pickup3Pose.getHeading(), GrabLine3Pose.getHeading())
                .build();
        scorePickup3 = follower.pathBuilder()
                .addPath(new BezierLine(GrabLine3Pose, scorePose))
                .setLinearHeadingInterpolation(GrabLine3Pose.getHeading(), scorePose.getHeading())
                .build();
        EndPoint = follower.pathBuilder()
                .addPath(new BezierLine(scorePose, OffLinePose))
                .setLinearHeadingInterpolation(scorePose.getHeading(), OffLinePose.getHeading())
                .build();
        EndPoint2 = follower.pathBuilder()
                .addPath(new BezierLine(scorePose, OffTickPose))
                .setLinearHeadingInterpolation(scorePose.getHeading(), OffTickPose.getHeading())
                .build();


        // /* This is our scorePickup3 PathChain. We are using a single path with a BezierLine, which is a straight line. */
        // scorePickup3 = follower.pathBuilder()
        //         .addPath(new BezierLine(pickup3Pose, scorePose))
        //         .setLinearHeadingInterpolation(pickup3Pose.getHeading(), scorePose.getHeading())
        //         .build();
    }

    public void autonomousPathUpdate() {
        switch (pathState) {
            case 0:
                follower.followPath(FirstUP);
                setPathState(1);
                break;

            case 1:
                //Shoots Preload
                if (!follower.isBusy()) {
                    double targetRPM = 4000; // Set your desired RPM here
                    double velocityTPS = (targetRPM * ticksPerRevolution) / 60.0;

                    // Use setVelocity instead of setPower
                    Shoot1.setVelocity(velocityTPS);
                    Shoot2.setVelocity(velocityTPS);

                    if (currentRPM >= (targetRPM * 0.95) || pathTimer.getElapsedTimeSeconds() > 1.5) {
                        Rail.setPosition(0.015); // Push the artifact
                        Intake.setPower(1);

                        if (pathTimer.getElapsedTimeSeconds() > 4.0) {
                            Intake.setPower(0);
                            Rail.setPosition(0);
                            setPathState(2);
                        }
                    }
                }
                break;

            case 2:
                // Stops shoot
                Intake.setPower(0);
                if (!follower.isBusy()) {
                    Rail.setPosition(0);
                    Shoot1.setVelocity(0);
                    Shoot2.setVelocity(0);
                    follower.followPath(grabPickup1, true);
                    setPathState(4);
                }
                break;


            case 4:
                //Intakes Line1
                if (!follower.isBusy()) {
                    /* Grab Sample */
                    Intake.setPower(1);
                    follower.setMaxPower(.35);
                    follower.followPath(grabPickup1P2, true);
                    setPathState(5);
                }
                break;

            case 5:
                // stops intake and goes to score Line1
                if (!follower.isBusy()) {
                    Intake.setPower(0);
                    follower.setMaxPower(1);
                    follower.followPath(scorePickup1, true);
                    setPathState(6);
                }
                break;


            case 6:
                //scores line 1
                if (!follower.isBusy()) {
                    double targetRPM = 4000; // Set your desired RPM here
                    double velocityTPS = (targetRPM * ticksPerRevolution) / 60.0;

                    // Use setVelocity instead of setPower
                    Shoot1.setVelocity(velocityTPS);
                    Shoot2.setVelocity(velocityTPS);

                    if (currentRPM >= (targetRPM * 0.95) || pathTimer.getElapsedTimeSeconds() > 1.5) {
                        Rail.setPosition(0.015); // Push the artifact
                        Intake.setPower(1);
                        // Give the servo 0.5 seconds to push before ending this state
                        if (pathTimer.getElapsedTimeSeconds() > 4.0) {
                            Intake.setPower(0);
                            Rail.setPosition(0);
                            setPathState(7);
                        }
                    }
                }

                break;

            case 7:
                // goes to Line 2
                if(!follower.isBusy()){
                    Shoot1.setVelocity(0);
                    Shoot2.setVelocity(0);
                    follower.followPath(grabPickup2P1, true);
                    setPathState(8);
                }
                break;

            case 8:
                //grabs Line 2
                if(!follower.isBusy()){
                    Intake.setPower(.9);
                    follower.setMaxPower(.4);
                    follower.followPath(grabPickup2P2, true);
                    setPathState(9);
                }
                break;

            case 9:
                // goes to score
                if(!follower.isBusy()){
                    Intake.setPower(0);
                    follower.setMaxPower(1);
                    follower.followPath(scorePickup2, true);
                    setPathState(10);
                }
                break;

            case 10:
                // scores line2
                if(!follower.isBusy()){
                    double targetRPM = 4000; // Set your desired RPM here
                    double velocityTPS = (targetRPM * ticksPerRevolution) / 60.0;

                    // Use setVelocity instead of setPower
                    Shoot1.setVelocity(velocityTPS);
                    Shoot2.setVelocity(velocityTPS);

                    if (currentRPM >= (targetRPM * 0.95) || pathTimer.getElapsedTimeSeconds() > 2.3) {
                        Rail.setPosition(0.015); // Push the artifact
                        Intake.setPower(1);
                        // Give the servo 0.5 seconds to push before ending this state
                        if (pathTimer.getElapsedTimeSeconds() > 4.0) {
                            Intake.setPower(0);
                            Rail.setPosition(0);
                            setPathState(11);
                        }
                    }
                }
                break;

            case 11:
                // turns off shoot and goes to Line3
                Intake.setPower(0);
                if(!follower.isBusy()) {
                    Shoot1.setVelocity(0);
                    Shoot2.setVelocity(0);
                    follower.followPath(grabPickup3P1, true);
                    setPathState(12);
                }
                break;

            case 12:
                // grabs Line3
                if(!follower.isBusy()) {
                    Intake.setPower(1);
                    follower.setMaxPower(.4);
                    follower.followPath(grabPickup3P2, true);
                    setPathState(13);
                }
                break;

            case 13:
                //goes to score
                if(!follower.isBusy()) {
                    Intake.setPower(0);
                    follower.setMaxPower(1);
                    follower.followPath(scorePickup3, true);
                    setPathState(14);
                }
                break;

            case 14:
                //scores Line3
                if(!follower.isBusy()){
                    double targetRPM = 4000; // Set your desired RPM here
                    double velocityTPS = (targetRPM * ticksPerRevolution) / 60.0;

                    // Use setVelocity instead of setPower
                    Shoot1.setVelocity(velocityTPS);
                    Shoot2.setVelocity(velocityTPS);

                    if (currentRPM >= (targetRPM * 0.95) || pathTimer.getElapsedTimeSeconds() > 2.0) {
                        Rail.setPosition(0.015); // Push the artifact
                        Intake.setPower(1);
                        // Give the servo 0.5 seconds to push before ending this state
                        if (pathTimer.getElapsedTimeSeconds() > 5.0) {
                            Intake.setPower(0);
                            Rail.setPosition(0);
                            setPathState(16);
                        }
                    }
                }
                break;
            case 15:
                //Option1: Stops Shoot and goes to wall
                if(!follower.isBusy()) {
                    follower.setMaxPower(1);
                    Intake.setPower(0);
                    Shoot1.setVelocity(0);
                    Shoot2.setVelocity(0);

                    follower.followPath(EndPoint);
                    setPathState(16);
                }
                break;
            case 16:
                //Option2: Stops Shoot and goes to Line
                if(!follower.isBusy()) {
                    follower.setMaxPower(1);
                    Intake.setPower(0);
                    Shoot1.setVelocity(0);
                    Shoot2.setVelocity(0);
                    follower.followPath(EndPoint,true);
                    setPathState(17);
                }


            case 17:
                /* This case checks the robot's position and will wait until the robot position is close (1 inch away) from the scorePose's position */
                if (!follower.isBusy()) {
                    /* Set the state to a Case we won't use or define, so it just stops running an new paths */
                    setPathState(-1);
                }
                break;
        }
    }


    /**
     * These change the states of the paths and actions. It will also reset the timers of the individual switches
     **/
    public void setPathState(int pState) {
        pathState = pState;
        pathTimer.resetTimer();
    }

    /**
     * This is the main loop of the OpMode, it will run repeatedly after clicking "Play".
     **/
    @Override
    public void loop() {


        // These loop the movements of the robot, these must be called continuously in order to work
        follower.update();
        autonomousPathUpdate();
        currentRPM = (Shoot1.getVelocity() * 60.0) / ticksPerRevolution;
        // Feedback to Driver Hub for debugging
        telemetry.addData("path state", pathState);
        telemetry.addData("x", follower.getPose().getX());
        telemetry.addData("y", follower.getPose().getY());
        telemetry.addData("heading", follower.getPose().getHeading());
        telemetry.update();


    }

    /**
     * This method is called once at the init of the OpMode.
     **/
    @Override
    public void init() {
        pathTimer = new Timer();
        opmodeTimer = new Timer();
        opmodeTimer.resetTimer();


        follower = Constants.createFollower(hardwareMap);
        buildPaths();
        follower.setStartingPose(startPose);

        //== other motors ==
        Intake = hardwareMap.get(DcMotor.class, "Intake");
        Intake.setDirection(DcMotorSimple.Direction.REVERSE);
        Shoot1 = (DcMotorEx) hardwareMap.get(DcMotor.class, "Shoot1");
        Shoot1.setDirection(DcMotorSimple.Direction.REVERSE);
        if (Shoot1 != null) {
            Shoot1.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            Shoot1.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        }
        Shoot2 = (DcMotorEx) hardwareMap.get(DcMotor.class, "Shoot2");
        Shoot2.setDirection(DcMotorSimple.Direction.FORWARD);
        if (Shoot2 != null) {
            Shoot2.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            Shoot2.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        }
        // Lift = hardwareMap.get(DcMotor.class, "Lift");


        //== Servos ==
        /*
        Hood= hardwareMap.get(Servo.class, "Hood");
        Hood.setDirection(Servo.Direction.FORWARD);
        Hood.setPosition(0);
        SpinTop= hardwareMap.get(Servo.class, "SpinTop");
        SpinTop.setDirection(Servo.Direction.FORWARD);
        SpinTop.setPosition(0);
        */
        Rail= hardwareMap.get(Servo.class, "Rail");
        Rail.setDirection(Servo.Direction.REVERSE);
        Rail.setPosition(0);

    }

    /**
     * This method is called continuously after Init while waiting for "play".
     **/
    @Override
    public void init_loop() {
    }

    /**
     * This method is called once at the start of the OpMode.
     * It runs all the setup actions, including building paths and starting the path system
     **/
    @Override
    public void start() {
        opmodeTimer.resetTimer();
        setPathState(0);
    }

    /**
     * We do not use this because everything should automatically disable
     **/
    @Override
    public void stop() {
    }


}
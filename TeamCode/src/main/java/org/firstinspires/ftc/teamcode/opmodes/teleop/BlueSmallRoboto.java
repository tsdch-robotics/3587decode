package org.firstinspires.ftc.teamcode.opmodes.teleop;
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.HeadingInterpolator;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.pedroPathing.Constants;

import java.util.function.Supplier;


/**
 * Lock - Left_bumper
 * Intake- right_trigger
 * Outtake- left_trigger
 * Shoot- dpad_up
 * dontshoot- dpad_down
 *F
 */
@Disabled
@Configurable
@TeleOp
public class BlueSmallRoboto extends OpMode {
    private Follower follower;
    public static Pose startingPose; //See ExampleAuto to understand how to use this
    private boolean automatedDrive;
    private Supplier<PathChain> pathChain;
    private TelemetryManager telemetryM;
    public DcMotor Intake;
    public Servo Rail;
    public DcMotorEx Shoot1;
    public DcMotorEx Shoot2;
    //public DcMotor Lift;
    // public Servo Hood;
    //  public Servo SpinTop;

    double ticksPerRevolution= 28;
    double LP;
    double HeadingError;

    private ElapsedTime sequenceTimer = new ElapsedTime();


    @Override
    public void init() {


        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startingPose == null ? new Pose() : startingPose);
        follower.update();
        telemetryM = PanelsTelemetry.INSTANCE.getTelemetry();

        pathChain = () -> follower.pathBuilder() //Lazy Curve Generation
                .addPath(new Path(new BezierLine(follower::getPose, new Pose(45, 98))))
                .setHeadingInterpolation(HeadingInterpolator.linearFromPoint(follower::getHeading, Math.toRadians(45), 0.8))
                .build();


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

    @Override
    public void start() {
        //The parameter controls whether the Follower should use break mode on the motors (using it is recommended).
        //In order to use float mode, add .useBrakeModeInTeleOp(true); to your Drivetrain Constants in Constant.java (for Mecanum)
        //If you don't pass anything in, it uses the default (false)
        follower.startTeleopDrive();
    }

    @Override
    public void loop() {
        //Call this once per loop
        follower.update();
        telemetryM.update();
// Inside loop()
        double headingRadians = follower.getPose().getHeading();
        double headingDegrees = Math.toDegrees(headingRadians);
        PedroLock();
        // Determine the rotation power
        // If left_bumper is held, use LP. Otherwise, use the right stick.
        double rotationPower = (gamepad1.left_bumper) ? LP : -gamepad1.right_stick_x;

        if (!automatedDrive) {
            follower.setTeleOpDrive(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    rotationPower, // <--- Add your calculated value here
                    false
            );
        }



        //Automated PathFollowing
        if (gamepad2.aWasPressed()) {
            follower.followPath(pathChain.get());
            automatedDrive = true;
        }

        //Stop automated following if the follower is done
        if (automatedDrive && (gamepad2.bWasPressed() || !follower.isBusy())) {
            follower.startTeleopDrive();
            automatedDrive = false;
        }



        // === intakes ===

        if (gamepad1.right_trigger > 0.5) {
            Intake.setPower(1);

        }

        else if (gamepad1.left_trigger > 0.5) {
            Intake.setPower(-1);

        }
        else {
            Intake.setPower(0);

        }


        // === Shooter ===
        //get distance from limelight and put shooter in correct position


        if(gamepad1.dpad_up){
            //shoot at correct speed
            double targetRPM = 4000; // Set your desired RPM here
            double velocityTPS = (targetRPM * ticksPerRevolution) / 60.0;

            // Use setVelocity instead of setPower
            Shoot1.setVelocity(velocityTPS);
            Shoot2.setVelocity(velocityTPS);
            Rail.setPosition(.01);
        }



        if(gamepad1.dpad_down){
            double targetRPM = 0; // Set your desired RPM here
            double velocityTPS = (targetRPM * ticksPerRevolution) / 60.0;

            // Use setVelocity instead of setPower
            Shoot1.setVelocity(velocityTPS);
            Shoot2.setVelocity(velocityTPS);
            Rail.setPosition(0);
        }
        double rpm = 0;
        if (Shoot1 != null) {
            double ticksPerSecond = Shoot1.getVelocity();
            rpm = (ticksPerSecond / ticksPerRevolution) * 60;
        } else {
            // This will show up in Telemetry so you know why it's not working
            telemetry.addLine("!!! SHOOT MOTOR NOT FOUND IN CONFIG !!!");
        }



        // === Servos ===
        /*
        if(gamepad1.a){
            Rail.setPosition(.01);
        }
        if (gamepad1.y){
            Rail.setPosition(0);
        }


         */


        // === SpinTop ===
        /*if(gamepad1.left_bumper){
            SpinTop.setPosition(-.3);
        }

        if (gamepad1.right_bumper){
            SpinTop.setPosition(+.3);
        }
         */


        // === Hood ===
        /*
        if(gamepad2.dpad_down){
            Hood.setPosition(0);
        }

        if(gamepad2.dpad_up){
            Hood.setPosition(.099);
        }



         */



        telemetryM.debug("position", follower.getPose());
        telemetryM.debug("automatedDrive", automatedDrive);
        telemetry.addData("position", follower.getPose());

        telemetry.addLine()
                .addData("Shoot Speed", rpm);
        updateTelemetry(telemetry);




    }
    public void PedroLock(){
        double targetHeading = Math.toRadians(-40);
        double currentHeading = follower.getPose().getHeading();
        HeadingError = targetHeading - currentHeading;
        while (HeadingError > Math.PI) HeadingError -= 2 * Math.PI;
        while (HeadingError < -Math.PI) HeadingError += 2 * Math.PI;

        // Simple P-loop (Proportional) to turn the error into power
        double kP = .5; // Tuning constant: increase if it turns too slow
        LP = HeadingError * kP;
    }

}

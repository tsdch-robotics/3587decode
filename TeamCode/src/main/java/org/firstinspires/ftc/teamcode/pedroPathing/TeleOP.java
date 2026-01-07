package org.firstinspires.ftc.teamcode.pedroPathing;
import com.bylazar.configurables.annotations.Configurable;
import com.bylazar.telemetry.PanelsTelemetry;
import com.bylazar.telemetry.TelemetryManager;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.HeadingInterpolator;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import android.graphics.Color;
import java.util.function.Supplier;

//testgithubchange
@Configurable
@TeleOp
public class TeleOP extends OpMode {
    private Follower follower;
    public static Pose startingPose; //See ExampleAuto to understand how to use this
    private boolean automatedDrive;
    private Supplier<PathChain> pathChain;
    private TelemetryManager telemetryM;
    private boolean slowMode = false;
    private double slowModeMultiplier = 0.5;

    public DcMotor FIntake;
    public DcMotor BIntake;
    public DcMotorEx Shoot;
    //public DcMotor Lift;
    public Servo Hood;
    public Servo SpinTop;
    public Servo Pod1;
    public Servo Pod2;
    public Servo Pod3;
    NormalizedColorSensor ColorSns1;
    NormalizedColorSensor ColorSns2;
    NormalizedColorSensor ColorSns3;
    //boolean FIntakeOn = false;
    //boolean BIntakeOn = false;
// Add these fields to the class (around line 34-37)
    private ElapsedTime servoTimer1 = new ElapsedTime();
    private boolean servo1Extended = false;
    private boolean servo1Waiting = false;
    private ElapsedTime servoTimer2 = new ElapsedTime();
    private boolean servo2Extended = false;
    private boolean servo2Waiting = false;
    private ElapsedTime servoTimer3 = new ElapsedTime();
    private boolean servo3Extended = false;
    private boolean servo3Waiting = false;


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


        //other motors
        FIntake = hardwareMap.get(DcMotor.class, "FIntake");
        FIntake.setDirection(DcMotorSimple.Direction.REVERSE);
        BIntake = hardwareMap.get(DcMotor.class, "BIntake");
        Shoot = (DcMotorEx) hardwareMap.get(DcMotor.class, "Shoot");
        if (Shoot != null) {
            Shoot.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
            Shoot.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        }
       // Lift = hardwareMap.get(DcMotor.class, "Lift");


        //Servos
        Hood= hardwareMap.get(Servo.class, "Hood");
        Hood.setDirection(Servo.Direction.REVERSE);
        Hood.setPosition(0);
        SpinTop= hardwareMap.get(Servo.class, "SpinTop");
        SpinTop.setDirection(Servo.Direction.FORWARD);
        SpinTop.setPosition(0);
        Pod1= hardwareMap.get(Servo.class, "Pod1");
        Pod1.setDirection(Servo.Direction.FORWARD);
        Pod1.setPosition(0);
        Pod2= hardwareMap.get(Servo.class, "Pod2");
        Pod2.setDirection(Servo.Direction.REVERSE);
        Pod2.setPosition(.1);
        Pod3= hardwareMap.get(Servo.class, "Pod3");
        Pod3.setDirection(Servo.Direction.FORWARD);
        Pod3.setPosition(0);


        //for color sensors
        float gain = 2;
        ColorSns1 = hardwareMap.get(NormalizedColorSensor.class, "Color1");
        ColorSns1.setGain(gain);
        ColorSns2 = hardwareMap.get(NormalizedColorSensor.class, "Color2");
        ColorSns2.setGain(gain);
        ColorSns3 = hardwareMap.get(NormalizedColorSensor.class, "Color3");
        ColorSns3.setGain(gain);

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


        if (!automatedDrive) {
            //Make the last parameter false for field-centric
            //In case the drivers want to use a "slowMode" you can scale the vectors

            //This is the normal version to use in the TeleOp
            if (!slowMode) follower.setTeleOpDrive(
                    -gamepad1.left_stick_y,
                    -gamepad1.left_stick_x,
                    -gamepad1.right_stick_x,
                    true // Robot Centric
            );

                //This is how it looks with slowMode on
            else follower.setTeleOpDrive(
                    -gamepad1.left_stick_y * slowModeMultiplier,
                    -gamepad1.left_stick_x * slowModeMultiplier,
                    -gamepad1.right_stick_x * slowModeMultiplier,
                    true // Robot Centric
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

        //Slow Mode
        if (gamepad2.rightBumperWasPressed()) {
            slowMode = !slowMode;
        }

        //Optional way to change slow mode strength
        if (gamepad2.xWasPressed()) {
            slowModeMultiplier += 0.25;
        }

        //Optional way to change slow mode strength
        if (gamepad2.yWasPressed()) {
            slowModeMultiplier -= 0.25;
        }

        //intakes
        if (gamepad1.right_trigger > 0.5) {
            FIntake.setPower(1);
        }
        else {
            FIntake.setPower(0);
        }
        if (gamepad1.left_trigger > 0.5) {
            BIntake.setPower(1);
        }
        else {
            BIntake.setPower(0);
        }
        if(gamepad1.bWasPressed()){
            FIntake.setPower(0);
            BIntake.setPower(0);
        }


        //get distance from limelight and put shooter in correct position
        if(gamepad1.dpad_up){
            //shoot at correct speed
            Shoot.setPower(1);
        }

        if(gamepad1.dpad_down){
            Shoot.setPower(0);
        }

        if(Shoot.getVelocity()> 40000){
            gamepad1.rumble(1000);
        }


        if(gamepad1.b ){
            Pod1.setPosition(.4);
            servo1Extended = true;
            servo1Waiting = true;
            servoTimer1.reset();
        }

        if (servo1Waiting && servoTimer1.seconds() >= 1.3) {
            // Move servo back to home position
            Pod1.setPosition(0.0);  // Replace with your actual servo
            servo1Extended = false;
            servo1Waiting = false;
        }

        if(gamepad1.y ){
            Pod2.setPosition(.4);
            servo2Extended = true;
            servo2Waiting = true;
            servoTimer2.reset();
        }

        if (servo2Waiting && servoTimer2.seconds() >= 1.3) {
            // Move servo back to home position
            Pod2.setPosition(0.0);  // Replace with your actual servo
            servo2Extended = false;
            servo2Waiting = false;
        }

        if(gamepad1.x ){
            Pod3.setPosition(.4);
            servo3Extended = true;
            servo3Waiting = true;
            servoTimer3.reset();
        }

        if (servo3Waiting && servoTimer3.seconds() >= 1.3) {
            // Move servo back to home position
            Pod3.setPosition(0.0);  // Replace with your actual servo
            servo3Extended = false;
            servo3Waiting = false;
        }

        if(gamepad1.a){
            Pod1.setPosition(0);
            Pod2.setPosition(.0 );
            Pod3.setPosition(0);

        }

        if(gamepad1.left_bumper){
            SpinTop.setPosition(-.3);
        }

        if (gamepad1.right_bumper){
            SpinTop.setPosition(+.3);
        }

        if(gamepad2.dpad_down){
            Hood.setPosition(0);
        }

        if(gamepad2.dpad_up){
            Hood.setPosition(.07);
        }
        if(BIntake.isBusy()){
            Shoot.setPower(0);
        }
        if(FIntake.isBusy()){
            Shoot.setPower(0);
        }

        double rpm = 0;
        if (Shoot != null) {
            double ticksPerSecond = Shoot.getVelocity();
            double ticksPerRevolution = 28;
            rpm = (ticksPerSecond / ticksPerRevolution) * 60;
        } else {
            // This will show up in Telemetry so you know why it's not working
            telemetry.addLine("!!! SHOOT MOTOR NOT FOUND IN CONFIG !!!");
        }
        NormalizedRGBA colors1 = ColorSns1.getNormalizedColors();
        NormalizedRGBA colors2 = ColorSns2.getNormalizedColors();
        NormalizedRGBA colors3 = ColorSns3.getNormalizedColors();
        /* Use telemetry to display feedback on the driver station. We show the red, green, and blue
         * normalized values from the sensor (in the range of 0 to 1), as well as the equivalent
         * HSV (hue, saturation and value) values. See http://web.archive.org/web/20190311170843/https://infohost.nmt.edu/tcc/help/pubs/colortheory/web/hsv.html
         * for an explanation of HSV color. */
        final float[] hsvValues1 = new float[3];
        final float[] hsvValues2 = new float[3];
        final float[] hsvValues3 = new float[3];
        // Update the hsvValues array by passing it to Color.colorToHSV()
        Color.colorToHSV(colors1.toColor(), hsvValues1);
        Color.colorToHSV(colors2.toColor(), hsvValues2);
        Color.colorToHSV(colors3.toColor(), hsvValues3);


        telemetry.addLine()
                .addData("Red", "%.3f", colors1.red)
                .addData("Green", "%.3f", colors1.green)
                .addData("Blue", "%.3f", colors1.blue);
        telemetry.addLine()
                .addData("Hue", "%.3f", hsvValues1[0])
                .addData("Saturation", "%.3f", hsvValues1[1])
                .addData("Value", "%.3f", hsvValues1[2]);
        telemetry.addData("Alpha", "%.3f", colors1.alpha);

        telemetry.addLine()
                .addData("Red", "%.3f", colors2.red)
                .addData("Green", "%.3f", colors2.green)
                .addData("Blue", "%.3f", colors2.blue);
        telemetry.addLine()
                .addData("Hue", "%.3f", hsvValues2[0])
                .addData("Saturation", "%.3f", hsvValues2[1])
                .addData("Value", "%.3f", hsvValues2[2]);
        telemetry.addData("Alpha", "%.3f", colors2.alpha);

        telemetry.addLine()
                .addData("Red", "%.3f", colors3.red)
                .addData("Green", "%.3f", colors3.green)
                .addData("Blue", "%.3f", colors3.blue);
        telemetry.addLine()
                .addData("Hue", "%.3f", hsvValues3[0])
                .addData("Saturation", "%.3f", hsvValues3[1])
                .addData("Value", "%.3f", hsvValues3[2]);
        telemetry.addData("Alpha", "%.3f", colors3.alpha);

        if (hsvValues1[0] > 200){
            telemetry.addData("color","purple");

        }
        if (130 <= hsvValues1[0] && hsvValues1[0] < 200){
            telemetry.addData("color","green");

        }
        if (130 >= hsvValues1[0]) {
            telemetry.addData("color","none");
        }
        if (hsvValues2[0] > 200){
            telemetry.addData("color","purple");

        }
        if (130 <= hsvValues2[0] && hsvValues2[0] < 200){
            telemetry.addData("color","green");

        }
        if (130 >= hsvValues2[0]) {
            telemetry.addData("color","none");
        }
        if (hsvValues3[0] > 200){
            telemetry.addData("color","purple");

        }
        if (130 <= hsvValues3[0] && hsvValues3[0] < 200){
            telemetry.addData("color","green");

        }
        if (130 >= hsvValues3[0]) {
            telemetry.addData("color","none");
        }



        telemetryM.debug("position", follower.getPose());
        telemetryM.debug("velocity", follower.getVelocity());
        telemetryM.debug("automatedDrive", automatedDrive);
        telemetry.addData("position", follower.getPose());

        telemetry.addLine()
                .addData("Shoot Speed", rpm);
        updateTelemetry(telemetry);


    }
}

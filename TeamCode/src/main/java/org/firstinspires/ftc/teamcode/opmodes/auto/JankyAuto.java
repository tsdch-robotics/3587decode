package org.firstinspires.ftc.teamcode.opmodes.auto;

import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;
import com.pedropathing.util.Timer;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;

import org.firstinspires.ftc.teamcode.common.Robot;

@Autonomous
public class JankyAuto extends OpMode {
    public DcMotor RF;
    public DcMotor RR;
    public DcMotor LR;
    public DcMotor LF;
    private int pathState;
    private Timer pathTimer, opmodeTimer;

    // Path poses
    private final Pose startPose = new Pose(16, 128, Math.toRadians(0));
    private final Pose FirstUPPose = new Pose(62, 106, Math.toRadians(125));
    private final Pose scorePose = new Pose(62, 106, Math.toRadians(125));
    private final Pose pickup1Pose = new Pose(20, 116, Math.toRadians(180));
    private final Pose pickup2Pose = new Pose(20, 86, Math.toRadians(180));
    private final Pose MidCurve2 = new Pose(55, 86, Math.toRadians(180));
    private final Pose pickup3Pose = new Pose(20, 66, Math.toRadians(180));
    private Path FirstUP;
    private PathChain grabPickup1, scorePickup1, grabPickup2P1, grabPickup2P2;

    @Override
    public void init() {
        RF = hardwareMap.get(DcMotor.class, "RF");
        RF.setDirection(DcMotorSimple.Direction.FORWARD);
        RR = hardwareMap.get(DcMotor.class, "RR");
        RR.setDirection(DcMotorSimple.Direction.FORWARD);
        LR = hardwareMap.get(DcMotor.class, "LR");
        LR.setDirection(DcMotorSimple.Direction.REVERSE);
        LF = hardwareMap.get(DcMotor.class, "LF");
        LF.setDirection(DcMotorSimple.Direction.REVERSE);

        RF.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        RR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LF.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        LR.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
    }
    @Override
    public void loop() {

    }

}

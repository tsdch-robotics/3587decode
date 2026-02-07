package org.firstinspires.ftc.teamcode.pedroPathing;

import com.pedropathing.control.FilteredPIDFCoefficients;
import com.pedropathing.control.PIDFCoefficients;
import com.pedropathing.follower.Follower;
import com.pedropathing.follower.FollowerConstants;
import com.pedropathing.ftc.FollowerBuilder;
import com.pedropathing.ftc.drivetrains.MecanumConstants;
import com.pedropathing.ftc.localization.constants.PinpointConstants;
import com.pedropathing.paths.PathConstraints;
import com.qualcomm.hardware.gobilda.GoBildaPinpointDriver;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Constants {
    // TODO: RE-TUNE ON FIELD — translational P=0.005 is likely too low after the strafeEncoderDirection
    //  fix. Try increasing P to 0.015-0.02. Robot may feel sluggish and drift at current value.
    public static FollowerConstants followerConstants = new FollowerConstants()
            .mass(13.6)
            .forwardZeroPowerAcceleration(-41.09371208)
            .lateralZeroPowerAcceleration(-57.19652149)
           .translationalPIDFCoefficients(new PIDFCoefficients(0.005, 0, 0.000, 0.02))
           .headingPIDFCoefficients(new PIDFCoefficients(0.5,0,0.00,.025))
           .drivePIDFCoefficients(new FilteredPIDFCoefficients(0.01,0.0,0.00,0.6,0.02));

    public static MecanumConstants driveConstants = new MecanumConstants()
            .maxPower(1)
             .xVelocity(71.85)
             .yVelocity(52.48)
            .rightFrontMotorName("RF")
            .rightRearMotorName("RR")
            .leftRearMotorName("LR")
            .leftFrontMotorName("LF")
            .leftFrontMotorDirection(DcMotorSimple.Direction.REVERSE)
            .leftRearMotorDirection(DcMotorSimple.Direction.REVERSE)
            .rightFrontMotorDirection(DcMotorSimple.Direction.FORWARD)
            .rightRearMotorDirection(DcMotorSimple.Direction.FORWARD)
            .useBrakeModeInTeleOp(true);




    // TODO: VERIFY ON NEW ROBOT — forwardPodY(1) and strafePodX(-1) were changed from 0.
    //  Measure actual pod offsets from robot center with a ruler on the new robot.
    public static PinpointConstants localizerConstants = new PinpointConstants()
            .forwardPodY(1)
            .strafePodX(-1)
            .distanceUnit(DistanceUnit.INCH)
            .hardwareMapName("Pin")
            .encoderResolution(GoBildaPinpointDriver.GoBildaOdometryPods.goBILDA_4_BAR_POD)
            //.customEncoderResolution(2000)
            .forwardEncoderDirection(GoBildaPinpointDriver.EncoderDirection.FORWARD)
            .strafeEncoderDirection(GoBildaPinpointDriver.EncoderDirection.REVERSED);

    public static PathConstraints pathConstraints = new PathConstraints(0.99, 100, 1, 1);

    public static Follower createFollower(HardwareMap hardwareMap) {
        return new FollowerBuilder(followerConstants, hardwareMap)
                .pathConstraints(pathConstraints)
                .mecanumDrivetrain(driveConstants)
                .pinpointLocalizer(localizerConstants)
                .build();

    }

}

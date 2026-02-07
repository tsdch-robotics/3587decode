package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

/**
 * Test OpMode for shooter motor.
 * Simple motor test for calibration.
 */
@Disabled
@TeleOp(name = "Shooter Test", group = "Test")
public class ShooterTest extends OpMode {
    
    public DcMotor shootMotor;

    @Override
    public void init() {
        shootMotor = hardwareMap.get(DcMotor.class, "Shoot");
        shootMotor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    @Override
    public void start() {
        // Ready to test
    }

    @Override
    public void loop() {
        // Use triggers to control power
        double power = gamepad1.right_trigger - gamepad1.left_trigger;
        shootMotor.setPower(power);
        
        telemetry.addData("Power", "%.2f", power);
        telemetry.addData("Velocity", "%.0f ticks/sec", 
                ((com.qualcomm.robotcore.hardware.DcMotorEx) shootMotor).getVelocity());
        telemetry.addData("Controls", "RT = Forward, LT = Reverse");
        telemetry.update();
    }
}


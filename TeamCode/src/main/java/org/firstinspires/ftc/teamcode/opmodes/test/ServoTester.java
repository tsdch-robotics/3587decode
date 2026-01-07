package org.firstinspires.ftc.teamcode.opmodes.test;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * Test OpMode for servo positioning.
 * Use this to find correct servo positions.
 */
@TeleOp(name = "Servo Tester", group = "Test")
public class ServoTester extends LinearOpMode {

    public Servo Triangles;

    @Override
    public void runOpMode() {
        Triangles = hardwareMap.get(Servo.class, "Triangles");
        Triangles.setDirection(Servo.Direction.FORWARD);
        Triangles.scaleRange(0.0, 1.0);
        Triangles.setPosition(0.0);

        waitForStart();
        
        while (opModeIsActive()) {
            if (gamepad1.a) {
                Triangles.setPosition(0.0);
            }
            if (gamepad1.b) {
                Triangles.setPosition(0.3);
            }
            
            telemetry.addData("Servo Position", Triangles.getPosition());
            telemetry.addData("Controls", "A = 0.0, B = 0.3");
            telemetry.update();
        }
    }
}


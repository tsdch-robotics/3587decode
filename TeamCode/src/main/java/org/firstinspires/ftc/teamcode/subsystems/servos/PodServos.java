package org.firstinspires.ftc.teamcode.subsystems.servos;

import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.common.Subsystem;

/**
 * Pod servos subsystem controlling Pod1, Pod2, Pod3, and SpinTop servos.
 * Includes timed extend/retract functionality for pods.
 */
public class PodServos extends Subsystem {

    private Servo pod1;
    private Servo pod2;
    private Servo pod3;
    private Servo spinTop;
    
    // Servo positions
    private double pod1Position = 0;
    private double pod2Position = 0.1;  // Pod2 starts at 0.1
    private double pod3Position = 0;
    private double spinTopPosition = 0;
    
    // Timing for auto-retract
    private ElapsedTime servoTimer1 = new ElapsedTime();
    private ElapsedTime servoTimer2 = new ElapsedTime();
    private ElapsedTime servoTimer3 = new ElapsedTime();
    
    private boolean pod1Waiting = false;
    private boolean pod2Waiting = false;
    private boolean pod3Waiting = false;
    
    // Constants
    public static final double POD_EXTENDED = 0.4;
    public static final double POD_RETRACTED = 0.0;
    public static final double POD2_RETRACTED = 0.1;  // Pod2 has different home
    public static final double EXTEND_TIME_SECONDS = 1.3;

    public PodServos(HardwareMap hardwareMap) {
        pod1 = hardwareMap.get(Servo.class, "Pod1");
        pod1.setDirection(Servo.Direction.FORWARD);
        
        pod2 = hardwareMap.get(Servo.class, "Pod2");
        pod2.setDirection(Servo.Direction.REVERSE);
        
        pod3 = hardwareMap.get(Servo.class, "Pod3");
        pod3.setDirection(Servo.Direction.FORWARD);
        
        spinTop = hardwareMap.get(Servo.class, "SpinTop");
        spinTop.setDirection(Servo.Direction.FORWARD);
        
        // Initialize positions
        pod1.setPosition(POD_RETRACTED);
        pod2.setPosition(POD2_RETRACTED);
        pod3.setPosition(POD_RETRACTED);
        spinTop.setPosition(0);
    }

    @Override
    public void periodic() {
        // Handle auto-retract timing for pod1
        if (pod1Waiting && servoTimer1.seconds() >= EXTEND_TIME_SECONDS) {
            pod1Position = POD_RETRACTED;
            pod1Waiting = false;
        }
        
        // Handle auto-retract timing for pod2
        if (pod2Waiting && servoTimer2.seconds() >= EXTEND_TIME_SECONDS) {
            pod2Position = POD2_RETRACTED;
            pod2Waiting = false;
        }
        
        // Handle auto-retract timing for pod3
        if (pod3Waiting && servoTimer3.seconds() >= EXTEND_TIME_SECONDS) {
            pod3Position = POD_RETRACTED;
            pod3Waiting = false;
        }
        
        // Apply positions
        pod1.setPosition(pod1Position);
        pod2.setPosition(pod2Position);
        pod3.setPosition(pod3Position);
        spinTop.setPosition(spinTopPosition);
    }

    @Override
    public void writeTelemetry(Telemetry telemetry) {
        telemetry.addData("Pod1", "%.2f%s", pod1Position, pod1Waiting ? " (waiting)" : "");
        telemetry.addData("Pod2", "%.2f%s", pod2Position, pod2Waiting ? " (waiting)" : "");
        telemetry.addData("Pod3", "%.2f%s", pod3Position, pod3Waiting ? " (waiting)" : "");
        telemetry.addData("SpinTop", "%.2f", spinTopPosition);
    }

    // =========== POD CONTROL ===========

    /**
     * Extend pod1 with auto-retract.
     */
    public void extendPod1() {
        pod1Position = POD_EXTENDED;
        pod1Waiting = true;
        servoTimer1.reset();
    }

    /**
     * Extend pod2 with auto-retract.
     */
    public void extendPod2() {
        pod2Position = POD_EXTENDED;
        pod2Waiting = true;
        servoTimer2.reset();
    }

    /**
     * Extend pod3 with auto-retract.
     */
    public void extendPod3() {
        pod3Position = POD_EXTENDED;
        pod3Waiting = true;
        servoTimer3.reset();
    }

    /**
     * Retract all pods immediately.
     */
    public void retractAll() {
        pod1Position = POD_RETRACTED;
        pod2Position = POD2_RETRACTED;
        pod3Position = POD_RETRACTED;
        pod1Waiting = false;
        pod2Waiting = false;
        pod3Waiting = false;
    }

    /**
     * Set pod1 position directly.
     */
    public void setPod1Position(double position) {
        pod1Position = position;
        pod1Waiting = false;
    }

    /**
     * Set pod2 position directly.
     */
    public void setPod2Position(double position) {
        pod2Position = position;
        pod2Waiting = false;
    }

    /**
     * Set pod3 position directly.
     */
    public void setPod3Position(double position) {
        pod3Position = position;
        pod3Waiting = false;
    }

    // =========== SPIN TOP CONTROL ===========

    /**
     * Spin top servo left.
     */
    public void spinLeft() {
        spinTopPosition = -0.3;
    }

    /**
     * Spin top servo right.
     */
    public void spinRight() {
        spinTopPosition = 0.3;
    }

    /**
     * Stop spin top servo.
     */
    public void stopSpin() {
        spinTopPosition = 0;
    }

    /**
     * Set spin top position directly.
     */
    public void setSpinTopPosition(double position) {
        spinTopPosition = position;
    }
}


package org.firstinspires.ftc.teamcode.utils;

import com.qualcomm.robotcore.hardware.Gamepad;

/**
 * Utility class for detecting gamepad button press events (edge detection).
 * Detects when a button transitions from not pressed to pressed.
 * 
 * Usage:
 *   GamepadEvents events = new GamepadEvents(gamepad1);
 *   // In loop:
 *   events.update();
 *   if (events.aPressed()) { ... }
 */
public class GamepadEvents {
    
    private Gamepad gamepad;
    
    // Previous button states
    private boolean prevA = false;
    private boolean prevB = false;
    private boolean prevX = false;
    private boolean prevY = false;
    private boolean prevLeftBumper = false;
    private boolean prevRightBumper = false;
    private boolean prevDpadUp = false;
    private boolean prevDpadDown = false;
    private boolean prevDpadLeft = false;
    private boolean prevDpadRight = false;
    private boolean prevBack = false;
    private boolean prevStart = false;
    private boolean prevLeftStickButton = false;
    private boolean prevRightStickButton = false;

    public GamepadEvents(Gamepad gamepad) {
        this.gamepad = gamepad;
    }

    /**
     * Update button states. Call once per loop iteration.
     */
    public void update() {
        prevA = gamepad.a;
        prevB = gamepad.b;
        prevX = gamepad.x;
        prevY = gamepad.y;
        prevLeftBumper = gamepad.left_bumper;
        prevRightBumper = gamepad.right_bumper;
        prevDpadUp = gamepad.dpad_up;
        prevDpadDown = gamepad.dpad_down;
        prevDpadLeft = gamepad.dpad_left;
        prevDpadRight = gamepad.dpad_right;
        prevBack = gamepad.back;
        prevStart = gamepad.start;
        prevLeftStickButton = gamepad.left_stick_button;
        prevRightStickButton = gamepad.right_stick_button;
    }

    /**
     * Read current button states and detect edges.
     * Call at start of loop, before update().
     */
    public void read() {
        // This method exists for compatibility - some patterns call read() then check buttons
        // For edge detection, call wasPressed methods before update()
    }

    // =========== EDGE DETECTION (wasPressed) ===========
    // Returns true only on the frame when button transitions from false to true

    public boolean aPressed() {
        return gamepad.a && !prevA;
    }

    public boolean bPressed() {
        return gamepad.b && !prevB;
    }

    public boolean xPressed() {
        return gamepad.x && !prevX;
    }

    public boolean yPressed() {
        return gamepad.y && !prevY;
    }

    public boolean leftBumperPressed() {
        return gamepad.left_bumper && !prevLeftBumper;
    }

    public boolean rightBumperPressed() {
        return gamepad.right_bumper && !prevRightBumper;
    }

    public boolean dpadUpPressed() {
        return gamepad.dpad_up && !prevDpadUp;
    }

    public boolean dpadDownPressed() {
        return gamepad.dpad_down && !prevDpadDown;
    }

    public boolean dpadLeftPressed() {
        return gamepad.dpad_left && !prevDpadLeft;
    }

    public boolean dpadRightPressed() {
        return gamepad.dpad_right && !prevDpadRight;
    }

    public boolean backPressed() {
        return gamepad.back && !prevBack;
    }

    public boolean startPressed() {
        return gamepad.start && !prevStart;
    }

    public boolean leftStickButtonPressed() {
        return gamepad.left_stick_button && !prevLeftStickButton;
    }

    public boolean rightStickButtonPressed() {
        return gamepad.right_stick_button && !prevRightStickButton;
    }

    // =========== RELEASE DETECTION (wasReleased) ===========
    // Returns true only on the frame when button transitions from true to false

    public boolean aReleased() {
        return !gamepad.a && prevA;
    }

    public boolean bReleased() {
        return !gamepad.b && prevB;
    }

    public boolean xReleased() {
        return !gamepad.x && prevX;
    }

    public boolean yReleased() {
        return !gamepad.y && prevY;
    }

    // =========== CURRENT STATE ===========
    // Returns current button state (not edge detection)

    public boolean a() {
        return gamepad.a;
    }

    public boolean b() {
        return gamepad.b;
    }

    public boolean x() {
        return gamepad.x;
    }

    public boolean y() {
        return gamepad.y;
    }

    public boolean leftBumper() {
        return gamepad.left_bumper;
    }

    public boolean rightBumper() {
        return gamepad.right_bumper;
    }

    public double leftTrigger() {
        return gamepad.left_trigger;
    }

    public double rightTrigger() {
        return gamepad.right_trigger;
    }

    public double leftStickX() {
        return gamepad.left_stick_x;
    }

    public double leftStickY() {
        return gamepad.left_stick_y;
    }

    public double rightStickX() {
        return gamepad.right_stick_x;
    }

    public double rightStickY() {
        return gamepad.right_stick_y;
    }
}


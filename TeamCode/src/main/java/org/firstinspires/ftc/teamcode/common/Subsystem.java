package org.firstinspires.ftc.teamcode.common;

import org.firstinspires.ftc.robotcore.external.Telemetry;

/**
 * Base class for all robot subsystems.
 * 
 * Subsystems are modular components that encapsulate specific robot functionality.
 * Each subsystem manages its own hardware and state.
 * 
 * Lifecycle:
 * 1. Constructor: Initialize hardware from HardwareMap
 * 2. periodic(): Called every loop - update state machines, PIDs, etc.
 * 3. writeTelemetry(): Called every loop - output debug data
 * 
 * All subsystems are registered with Robot and updated automatically.
 */
public abstract class Subsystem {
    
    /**
     * Updates the internal logic of the subsystem.
     * Called every loop by Robot.periodic().
     * 
     * Use this for:
     * - State machine transitions
     * - PID calculations
     * - Sensor processing
     * - Motor/servo commands
     */
    public abstract void periodic();

    /**
     * Outputs debug data to the telemetry stream.
     * Called every loop by Robot.write().
     * 
     * @param telemetry The telemetry object to write to
     */
    public abstract void writeTelemetry(Telemetry telemetry);
}


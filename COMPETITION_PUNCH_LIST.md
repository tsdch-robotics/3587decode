# Team 3587 — DECODE Regional Qualifier Punch List

**Date:** February 6, 2026
**Target:** Top-3 finish at Regional Qualifier → NC State Championships
**Timeline:** ~7 days of development remaining
**Branch:** `refactor-object-oriented` (HEAD: `5ebd72e`)
**Robot:** "Big Robot" — dual intake (FIntake/BIntake), single shooter (Shoot), 3 pod servos, SpinTop, Hood, 3 color sensors, Pinpoint odometry

---

## ✅ COMPLETED (February 6, 2026)

**High-Confidence Fixes Implemented:**

- ✅ **BUG 1-4:** Rewrote `BigBlueAuto` to use Robot class, fixed follower initialization crash, uncommented `scorePickup3`, added missing `break` statements, changed group to "Competition"
- ✅ **BUG 5:** Fixed Shooter motor direction — added `shootMotor.setDirection(DcMotorSimple.Direction.REVERSE)`
- ✅ **BUG 9:** Added timeout fallbacks (`|| pathTimer.getElapsedTimeSeconds() > 5.0`) to all `isBusy()` checks in both `BigRedAuto` and `BigBlueAuto`
- ✅ **BUG 10:** Added `@Disabled` annotations to 16 non-competition OpModes (all test autos, legacy TeleOps, test files)
- ✅ **BUG 15:** Removed dead import `import static org.firstinspires.ftc.teamcode.pedroPathing.Tuning.follower;` from `Constants.java`
- ✅ **OPPORTUNITY D:** Uncommented `scorePickup3` path and `followPath` call in `BigRedAuto` — 3rd scoring cycle now active
- ✅ **TODO Comments Added:** Flagged hardware-dependent items in `Shooter.java` (hood values), `PodServos.java` (retracted positions, spinLeft bug), and `Constants.java` (odometry offsets, PID tuning) for physical robot verification

**Status:** All critical crash bugs fixed. Both autos now functional with timeout safety. Driver station cleaned up. Ready for Day 1 hardware verification and mechanism integration.

---

## Table of Contents

1. [Game Scoring Quick Reference](#1-game-scoring-quick-reference)
2. [Strategic Auto Plan](#2-strategic-auto-plan)
3. [Critical Bugs — Fix Before Anything Else](#3-critical-bugs--fix-before-anything-else)
4. [Day-by-Day Punch List](#4-day-by-day-punch-list)
5. [Detailed Bug List with File Locations](#5-detailed-bug-list-with-file-locations)
6. [Detailed Opportunity List](#6-detailed-opportunity-list)
7. [Codebase Architecture Notes](#7-codebase-architecture-notes)
8. [Hardware Issues to Escalate](#8-hardware-issues-to-escalate)

---

## 1. Game Scoring Quick Reference

### Match Structure

| Phase | Duration | Notes |
|-------|----------|-------|
| AUTO | 30 seconds | Robot operates autonomously. Sensors decode the randomized motif. |
| Transition | 8 seconds | Drivers prepare. |
| TELEOP | 2 minutes | Human-controlled. Continue scoring, then return to base. |

### Scoring Elements

| Element | Points | Phase | Notes |
|---------|--------|-------|-------|
| **Leave** (off launch line) | ~2 per robot | AUTO | Free points. Just move. |
| **Classified Artifact** | **3** | AUTO + TELEOP | Ball passes through the square to the ramp. |
| **Overflow Artifact** | 1 | AUTO + TELEOP | Ball goes into goal but doesn't reach ramp. |
| **Depot Artifact** | 1 | TELEOP | Scored at the depot area. |
| **Pattern Match** | **2 bonus** per match | AUTO + TELEOP | Correct color in correct ramp position per the motif. |
| **Motif Complete** | Bonus | End of AUTO + End of MATCH | All pattern positions correctly matched. |
| **Partial Base Return** | ~10 | End of TELEOP | Robot partially in base zone. |
| **Full Base Return** | ~15-20 | End of TELEOP | Robot fully in base zone. |
| **2-Robot Full Return** | **30 bonus** | End of TELEOP | Both alliance robots fully in base. |

### Ranking Point Thresholds

| RP | Threshold |
|----|-----------|
| Movement RP | 16 points |
| Goal RP | 36 artifacts through the gate |
| Pattern RP | 18 points |
| Win/Tie RP | Win the match |

### AprilTag IDs (for Motif Detection)

| Tag ID | Meaning |
|--------|---------|
| 20 | Blue Goal |
| 21 | Motif: **GPP** (Green-Purple-Purple) |
| 22 | Motif: **PGP** (Purple-Green-Purple) |
| 23 | Motif: **PPG** (Purple-Purple-Green) |
| 24 | Red Goal |

### The Math That Matters

> **Scoring a "wrong" color artifact = 3 points.
> Spending 5 seconds looking for the "right" color = 0 points during that delay.**
>
> For a volume-first robot with no color sorting: **3 points (blind cycle) > 2 points (pattern bonus).**
> Your strategy: intake everything, shoot everything, leave pattern optimization to your alliance partner or endgame.

---

## 2. Strategic Auto Plan

### Primary Strategy: "Blind Volume Cycle" (Target: 30-45 pts solo)

This strategy maximizes raw throughput. Ignore artifact color. Treat every artifact as a valid scoring object.

#### The Routine

| Phase | Time Window | Action | Points |
|-------|-------------|--------|--------|
| **Preload Burst** | 0-4s | Curve to goal, rapid-fire 3 preloaded artifacts | ~9 pts |
| **Spike Sweep 1** | 4-12s | Drive through nearest spike mark stack, intake 3, return to goal, shoot all 3 | ~9 pts |
| **Spike Sweep 2** | 12-25s | Repeat on second stack or human player depot area | ~9 pts |
| **Park** | 25-30s | Drive to base zone | ~10 pts |
| | | **Solo Total** | **~37 pts** |

If your alliance partner does similar: ~74 + 6 (leave) = **80+ points** before any pattern luck.

#### Suggested Pedro Pathing Coordinates

```java
// These are starting points — MUST be tuned on the actual field
Pose startPose    = new Pose(28.5, 128, Math.toRadians(180));
Pose scorePose    = new Pose(60, 85, Math.toRadians(135));  // Angled shots prevent bounce-out
Pose spikeMark1   = new Pose(37, 121, Math.toRadians(0));
Pose spikeMark2   = new Pose(37, 97, Math.toRadians(0));    // Second stack
Pose basePose     = new Pose(28.5, 128, Math.toRadians(180)); // Return to base
```

### Fallback Strategy: "Reliable 1+1" (Target: 20-25 pts solo)

If intake jams on stacks or odometry drifts during high-speed cycles, fall back to this. **Better to score 20 points every time than 40 points once.**

| Phase | Action | Points |
|-------|--------|--------|
| Preload Score | Drive to goal, shoot 3 | ~9 pts |
| Single Stack | Drive to closest spike mark, intake slowly (no jam) | — |
| Score & Park | Return to goal, shoot 3, drive to base | ~9 + 10 pts |
| | **Total** | **~28 pts** |

### Implementation: Time-Based State Machine (No Color Sorting Required)

```java
public enum AutoState {
    START,
    PATH_TO_SCORE_PRELOAD,
    SHOOTING_PRELOAD,
    PATH_TO_SPIKE_MARK,
    INTAKING,
    PATH_TO_SCORE_CYCLE_1,
    SHOOTING_CYCLE_1,
    PARK,
    IDLE
}

// Inside loop():
switch (currentState) {
    case START:
        follower.followPath(scorePreloadPath);
        setState(PATH_TO_SCORE_PRELOAD);
        break;

    case PATH_TO_SCORE_PRELOAD:
        if (!follower.isBusy() || pathTimer.seconds() > 5.0) {  // TIMEOUT!
            shooterTimer.reset();
            shooter.shoot();
            setState(SHOOTING_PRELOAD);
        }
        break;

    case SHOOTING_PRELOAD:
        if (shooterTimer.seconds() > 1.5) {  // 1.5s to shoot 3 balls
            shooter.stop();
            intake.runBothIntakes();  // Turn ON before reaching spike mark
            follower.followPath(spikeMarkPath);
            setState(PATH_TO_SPIKE_MARK);
        }
        break;

    case PATH_TO_SPIKE_MARK:
        if (!follower.isBusy() || pathTimer.seconds() > 5.0) {
            intakeTimer.reset();
            setState(INTAKING);
        }
        break;

    case INTAKING:
        if (intakeTimer.seconds() > 0.5) {  // Wait for balls to settle
            follower.followPath(scoreCycle1Path);
            setState(PATH_TO_SCORE_CYCLE_1);
        }
        break;

    case PATH_TO_SCORE_CYCLE_1:
        if (!follower.isBusy() || pathTimer.seconds() > 5.0) {
            intake.stopAll();
            shooterTimer.reset();
            shooter.shoot();
            setState(SHOOTING_CYCLE_1);
        }
        break;

    case SHOOTING_CYCLE_1:
        if (shooterTimer.seconds() > 1.5) {
            shooter.stop();
            follower.followPath(parkPath);
            setState(PARK);
        }
        break;

    case PARK:
        if (!follower.isBusy() || pathTimer.seconds() > 5.0) {
            setState(IDLE);
        }
        break;

    case IDLE:
        // Done. Do nothing.
        break;
}
```

### Tuning Tips for 1-Week Crunch

1. **Tuning priority:** Spend days 1-2 getting Follower PID tight. If the robot shakes or drifts, the blind strategy fails because you miss the spike mark.
2. **Slow down:** Don't move at 100% speed. Set Pedro path speed constraints to 60-70%. *"Slow is smooth, smooth is fast."* Accuracy hitting the spike mark is worth more than getting there 0.5s faster and missing.
3. **Intake while moving:** Enable intake BEFORE reaching the spike mark. Drive through the stack without stopping — much faster than stop-intake-go.
4. **Always have timeouts:** Every `!follower.isBusy()` check MUST have a `|| pathTimer.seconds() > X` timeout. If the robot gets stuck, the auto must recover, not freeze.

---

## 3. Critical Bugs — Fix Before Anything Else

These will cause crashes, wrong behavior, or lost matches if not addressed.

### BUG 1: `BigBlueAuto` — Follower is NEVER initialized (CRASH)

**File:** `opmodes/auto/BigBlueAuto.java`, line 232
**Issue:** `follower = Constants.createFollower(hardwareMap);` is commented out.
**Result:** NullPointerException on init. Blue-side auto is completely non-functional.
**Fix:**
```java
// Uncomment this line:
follower = Constants.createFollower(hardwareMap);

// Also: move buildPaths() AFTER setStartingPose()
// Also: change group from "Examples" to "Competition"
// Also: consider rewriting to use Robot class like BigRedAuto
```

### BUG 2: `BigBlueAuto` — Missing `break` in cases 7 and 8 (fall-through)

**File:** `opmodes/auto/BigBlueAuto.java`, lines 173-187
**Issue:** Cases 7 and 8 have no `break;` statements. Switch fall-through causes paths to be overwritten immediately.
**Fix:** Add `break;` after each case body.

### BUG 3: `BigBlueAuto` — `scorePickup3` is null (CRASH)

**File:** `opmodes/auto/BigBlueAuto.java`, lines 90-93
**Issue:** Path creation is commented out but case 8 still calls `follower.followPath(scorePickup3, true)`.
**Fix:** Uncomment the path creation code.

### BUG 4: `BigBlueAuto` — Doesn't use Robot class

**File:** `opmodes/auto/BigBlueAuto.java`
**Issue:** Uses raw `Follower` directly. Has no access to intake, shooter, or pod subsystems during auto. Even if fixed, it can only drive — it cannot score.
**Fix:** Rewrite to use `Robot` class like `BigRedAuto`, or create a new competition blue auto from scratch.

### BUG 5: `Shooter` subsystem — Motor direction is WRONG

**File:** `subsystems/shooter/Shooter.java`, line 28
**Issue:** No `setDirection()` call — defaults to `FORWARD`. Every legacy TeleOp sets it to `REVERSE`. If `MainTeleOp` is used, the **shooter spins backwards**.
**Fix:**
```java
shootMotor = hardwareMap.get(DcMotorEx.class, "Shoot");
shootMotor.setDirection(DcMotorSimple.Direction.REVERSE);  // ADD THIS
shootMotor.setMode(DcMotorEx.RunMode.RUN_USING_ENCODER);
```

### BUG 6: `Shooter` subsystem — Hood UP position is 5-7x too high

**File:** `subsystems/shooter/Shooter.java`, line 23
**Issue:** `HOOD_UP = 0.5` but every tested legacy TeleOp uses `0.07` to `0.099`. The value 0.5 could over-travel the servo and damage the hood mechanism, or send shots wildly off target.
**Fix:**
```java
public static final double HOOD_UP = 0.099;  // Match tested value from legacy code
```

### BUG 7: `PodServos` — SpinTop given NEGATIVE servo position

**File:** `subsystems/servos/PodServos.java`, line 179
**Issue:** `spinTopPosition = -0.3;` — Servo positions must be 0.0-1.0. The SDK clamps -0.3 to 0.0, making "spin left" identical to "stop."
**Fix:** Determine the correct servo value. If SpinTop is a continuous rotation servo, positions should be around 0.0 (full reverse), 0.5 (stop), 1.0 (full forward). If standard servo, use appropriate tested positions.

### BUG 8: Both autos — NO mechanism actions during autonomous

**File:** `opmodes/auto/BigRedAuto.java`, `BigBlueAuto.java`
**Issue:** The state machines drive paths but **never activate intake, shooter, or pods**. Zero artifacts are collected or scored during the entire 30-second auto period.
**Fix:** Add intake/shooter commands at appropriate states (see Section 2 for the strategy implementation).

### BUG 9: Both autos — No timeout on path waits

**File:** `opmodes/auto/BigRedAuto.java`, lines 128-175
**Issue:** Every state uses `if (!robot.drive.isBusy())` with no timeout. If the robot gets stuck (wall, partner, field element), the state machine waits **forever** and never recovers.
**Fix:** Add timeout to every path check:
```java
if (!robot.drive.isBusy() || pathTimer.getElapsedTimeSeconds() > 5.0) {
    // proceed to next state
}
```

### BUG 10: 20+ OpModes visible on Driver Station — None disabled

**Issue:** No `@Disabled` annotations anywhere. The driver sees 6 autonomous programs and 14+ teleop programs in the menu. Under competition pressure, selecting the wrong one is near-certain.
**Fix:** Add `@Disabled` to every non-competition OpMode. Keep ONLY:
- `BigRedAuto` (Competition)
- `BigBlueAuto` (Competition)
- `MainTeleOp` (Competition) — or whichever TeleOp is chosen
- `Tuning` (Pedro Pathing — for pit tuning only)

Disable ALL of these:
- `BackAuto`, `JustForward`, `JustLeft`, `JustRight`
- `PedroLockTeleOp`, `MaxTeleOp`, `GyroHeadingTeleOp`
- `BlueSmallRoboto`, `RedSmallRoboto` (wrong robot hardware)
- `pedroPathing/TeleOP` (development file)
- `BasicOmniOpMode_Linear` (sample code)
- `ShooterTest`, `ServoTester`, `LimelightTest`, `ColorSensorTest`, `AprilTagTest`

### BUG 11: `BackAuto` — Multiple null paths referenced (CRASH)

**File:** `opmodes/auto/BackAuto.java`
**Issue:** `scorePickup1`, `scorePickup2`, `grabPickup2`, `grabPickup3`, `scorePickup3` are all commented out but the state machine still references them.
**Fix:** Either uncomment the paths or `@Disable` this OpMode (recommended — it's a test auto).

### BUG 12: `GyroHeadingTeleOp` — Illegal JDK import

**File:** `opmodes/teleop/GyroHeadingTeleOp.java`, line 4
**Issue:** `import static com.sun.tools.javac.jvm.ByteCodes.error;` — JDK internal API not available on Android. May cause compile failure.
**Fix:** Remove the import (it's unused) or `@Disable` the whole file.

### BUG 13: `PedroLockTeleOp` — `PedroLock()` method never called

**File:** `opmodes/teleop/PedroLockTeleOp.java`
**Issue:** The heading lock method exists but is never invoked from `loop()`. The `LP` variable stays at 0. The heading lock button does nothing.
**Fix:** If keeping this file, add `PedroLock();` to the top of `loop()`. Better: `@Disable` and port the feature to `MainTeleOp`.

### BUG 14: Legacy TeleOps — `isBusy()` safety check never works

**File:** `PedroLockTeleOp.java`, `MaxTeleOp.java`, `GyroHeadingTeleOp.java`, `pedroPathing/TeleOP.java`
**Issue:** `BIntake.isBusy()` and `FIntake.isBusy()` are used to prevent shooting while intaking. But `isBusy()` only returns true in `RUN_TO_POSITION` mode. These motors use default mode, so it **always returns false**. The safety never triggers.
**Fix:** Check motor power instead: `if (FIntake.getPower() != 0)`.

### BUG 15: `Constants.java` — Dead import creates fragile dependency

**File:** `pedroPathing/Constants.java`, line 3
**Issue:** `import static org.firstinspires.ftc.teamcode.pedroPathing.Tuning.follower;` — unused import that creates a compile-time dependency on `Tuning.java`.
**Fix:** Remove the import line.

### BUG 16: Hood direction inconsistency

| Source | Hood Direction | Hood UP |
|--------|---------------|---------|
| `Shooter.java` subsystem | `REVERSE` | 0.5 |
| `pedroPathing/TeleOP.java` | `FORWARD` | 0.07 |
| `PedroLockTeleOp` | `FORWARD` | 0.099 |
| `MaxTeleOp` | `FORWARD` | 0.099 |

**Fix:** Determine the correct direction and position for the new robot's hood. Update `Shooter.java` to match.

### BUG 17: `PodServos` — Retracted positions inconsistent

**File:** `subsystems/servos/PodServos.java`
**Issue:** `POD2_RETRACTED = 0.3` and `POD3_RETRACTED = 0.3` but all legacy code uses `0.0` as home. The subsystem will retract pods to different positions than the legacy code.
**Fix:** Verify correct retracted positions on the new robot and update constants.

### BUG 18: Automated path target is hardcoded and alliance-blind

**File:** Every TeleOp with automated path following
**Issue:** `new Pose(45, 98)` is hardcoded in all TeleOps. This is the same coordinate regardless of alliance color. On the wrong alliance, this could drive into the opponents' side.
**Fix:** Make the target pose alliance-aware, or remove automated driving until it's properly implemented.

---

## 4. Day-by-Day Punch List

### Day 0: Decision Point (30 minutes)

- [ ] **Confirm the new "big robot" hardware config is final** — dual intake, single shooter, pods, color sensors
- [ ] **Confirm hardware names match code** — `LF`, `LR`, `RF`, `RR`, `FIntake`, `BIntake`, `Shoot`, `Hood`, `Pod1`, `Pod2`, `Pod3`, `SpinTop`, `Color1`, `Color2`, `Color3`, `Pin`
- [ ] **Measure and record odometry pod offsets** with a ruler — `forwardPodY` and `strafePodX` from robot center. Currently set to (1, -1) inches. If these are wrong, every auto path misses.

### Day 1: Stop the Bleeding (4-6 hours)

- [x] **Fix BUG 5:** Add `shootMotor.setDirection(REVERSE)` to `Shooter.java` ✅ **COMPLETED**
- [ ] **Fix BUG 6:** Change `HOOD_UP` to 0.099 (or measured value for new robot) ⚠️ **TODO added — needs hardware verification**
- [ ] **Fix BUG 7:** Fix SpinTop servo positions in `PodServos.java` ⚠️ **TODO added — needs hardware verification**
- [x] **Fix BUG 15:** Remove dead import from `Constants.java` ✅ **COMPLETED**
- [ ] **Fix BUG 16:** Set correct Hood direction in `Shooter.java` for new robot ⚠️ **TODO added — needs hardware verification**
- [ ] **Fix BUG 17:** Verify pod retracted positions on new robot ⚠️ **TODO added — needs hardware verification**
- [x] **Fix BUG 10:** Add `@Disabled` to ALL non-competition OpModes (see list in BUG 10) ✅ **COMPLETED** (16 files)
- [ ] **Port PedroLock** heading lock feature into `MainTeleOp` (from `BlueSmallRoboto`)
- [ ] **Port velocity-based shooting** into `Shooter.java` subsystem (replace `setPower(1.0)` with `setVelocity()`)
- [ ] **Verify `MainTeleOp` works end-to-end** on the new robot with all fixes

### Day 2: Fix Autonomous Foundation (4-6 hours)

- [x] **Rewrite `BigBlueAuto`** — Use Robot class, fix all crashes (BUGs 1-4), mirror BigRedAuto structure ✅ **COMPLETED**
- [x] **Add timeout fallbacks** to both BigRedAuto and BigBlueAuto (BUG 9) ✅ **COMPLETED**
- [ ] **Add Leave verification** — confirm first path clears launch line
- [ ] **Update odometry offsets** in `Constants.java` if Day 0 measurement differs from current (1, -1) ⚠️ **TODO added — needs hardware measurement**
- [ ] **Re-tune translational PID** — start at P=0.015, since the strafe encoder direction bug is now fixed. The current P=0.005 was a band-aid for the old reversed encoder. Test on field. ⚠️ **TODO added — needs field testing**

### Day 3: Make Auto Score (4-6 hours)

- [ ] **Implement "Reliable 1+1" auto** (see Section 2):
  - State machine with enum states (not integer switch)
  - Preload → shoot → spike mark → intake → shoot → park
  - Timing-based shooting (1.5s per volley)
  - Intake ON before reaching spike mark
  - Timeouts on every path
- [ ] **Test "Reliable 1+1" until it works 10 times in a row without failure**
- [ ] **Create mirrored version for blue alliance** (flip coordinates)

### Day 4: Expand Auto + Vision (4-6 hours)

- [ ] **Add second cycle** to create "Blind Volume Cycle" auto (Section 2 Tier 1)
- [ ] **Add AprilTag motif detection** during `init_loop()`:
  - Initialize `AprilTagProcessor` and `VisionPortal` in `init()`
  - Read obelisk AprilTag (IDs 21/22/23) during `init_loop()`
  - Display detected motif on telemetry for driver verification
  - Set low exposure + high gain for competition lighting
  - Store motif for potential pattern-aware scoring later
- [ ] **Add auto-to-teleop pose handoff** — save final auto pose to a `static Pose` field, load in TeleOp init

### Day 5: TeleOp Polish (3-4 hours)

- [ ] **Add dynamic aiming** to MainTeleOp:
  ```java
  double goalX = /* red or blue goal X */;
  double goalY = /* red or blue goal Y */;
  double targetHeading = Math.atan2(goalY - robotY, goalX - robotX);
  ```
- [ ] **Add endgame auto-return-to-base**:
  - Button press (e.g., gamepad2.a) triggers automated path back to base zone
  - Or timer-based: telemetry countdown at 15 seconds remaining
- [ ] **Add voltage compensation** to shooter — apply `robot.getVoltageScale()` to velocity target
- [ ] **Add color sensor feedback** to telemetry — show detected pattern to help drivers

### Day 6: Integration Testing (Full day on the field)

- [ ] **Run full auto+teleop cycles** simulating real match conditions
- [ ] **Time auto cycles** — if Blind Volume Cycle finishes >28s, cut to Reliable 1+1
- [ ] **Test alliance color switching** — verify both red and blue autos work
- [ ] **Test edge cases:**
  - What happens if robot gets bumped during auto?
  - What happens if a path takes too long (timeout recovery)?
  - What happens if shooter takes too long to spin up?
  - What happens if intake jams?
- [ ] **Tune path coordinates on actual field** — adjust spike mark poses, score poses

### Day 7: Competition Prep (2-3 hours)

- [ ] **Final code freeze** — no new features, only critical bug fixes
- [ ] **Verify only competition OpModes are visible** on driver station
- [ ] **Label driver station controls clearly** for drivers
- [ ] **Write a 1-page driver reference card:**
  - Which OpMode to select for Red auto / Blue auto / TeleOp
  - TeleOp button mapping
  - What to do if something goes wrong (restart procedure)
- [ ] **Pack spare parts, USB cables, laptop charger**
- [ ] **Driver practice: minimum 10 full mock matches**

---

## 5. Detailed Bug List with File Locations

| # | Severity | File | Line(s) | Issue | Status |
|---|----------|------|---------|-------|--------|
| 1 | **CRITICAL** | `BigBlueAuto.java` | 232 | Follower never initialized (NPE crash) | ✅ **FIXED** |
| 2 | **CRITICAL** | `BigBlueAuto.java` | 173-187 | Missing `break` in cases 7, 8 (fall-through) | ✅ **FIXED** |
| 3 | **CRITICAL** | `BigBlueAuto.java` | 90-93 | `scorePickup3` null (NPE crash) | ✅ **FIXED** |
| 4 | **CRITICAL** | `BigBlueAuto.java` | whole file | Doesn't use Robot class — can't score | ✅ **FIXED** |
| 5 | **CRITICAL** | `Shooter.java` | 28 | Motor direction not set (spins backwards) | ✅ **FIXED** |
| 6 | **HIGH** | `Shooter.java` | 23 | `HOOD_UP = 0.5` should be ~0.099 | ⚠️ **TODO ADDED** (needs hardware verification) |
| 7 | **HIGH** | `PodServos.java` | 179 | `spinTopPosition = -0.3` (invalid, clamped to 0) | ⚠️ **TODO ADDED** (needs hardware verification) |
| 8 | **HIGH** | `BigRedAuto.java` | all states | No intake/shooter/pod commands in auto | 🔄 **PENDING** (Day 3) |
| 9 | **HIGH** | `BigRedAuto.java` | 128-175 | No timeout on path waits | ✅ **FIXED** |
| 10 | **HIGH** | All OpModes | annotations | No `@Disabled` on non-competition OpModes | ✅ **FIXED** |
| 11 | **MEDIUM** | `BackAuto.java` | 42-75, 100 | Multiple null paths referenced | ✅ **DISABLED** |
| 12 | **MEDIUM** | `GyroHeadingTeleOp.java` | 4 | Illegal `com.sun.tools` import | ✅ **DISABLED** |
| 13 | **MEDIUM** | `PedroLockTeleOp.java` | loop() | `PedroLock()` never called | ✅ **DISABLED** |
| 14 | **MEDIUM** | Legacy TeleOps | intake checks | `isBusy()` always false on non-RTP motors | ✅ **DISABLED** |
| 15 | **LOW** | `Constants.java` | 3 | Dead import of `Tuning.follower` | ✅ **FIXED** |
| 16 | **LOW** | `Shooter.java` vs legacy | varies | Hood direction inconsistency | ⚠️ **TODO ADDED** (needs hardware verification) |
| 17 | **LOW** | `PodServos.java` | 40-41 | Pod2/3 retracted positions inconsistent with legacy | ⚠️ **TODO ADDED** (needs hardware verification) |
| 18 | **LOW** | All TeleOps | path target | Automated path (45,98) is alliance-blind | 🔄 **PENDING** (Day 5) |

---

## 6. Detailed Opportunity List

### Scoring Opportunities (ordered by expected point impact)

| # | Opportunity | Est. Points/Match | Effort | Priority | Status |
|---|------------|-------------------|--------|----------|--------|
| A | **Auto mechanism actions** — intake + shoot during auto | +10-20 | 3-4 hr | CRITICAL | 🔄 **PENDING** (Day 3) |
| B | **Auto motif detection** — AprilTag reads obelisk, branch paths | +10-18 (Pattern RP) | 4-6 hr | HIGH | 🔄 **PENDING** (Day 4) |
| C | **Endgame auto-return-to-base** — automated path to base zone | +15-30 | 2-3 hr | HIGH | 🔄 **PENDING** (Day 5) |
| D | **Complete 3rd auto cycle** — uncomment + implement 3rd pickup | +3-9 | 1 hr | HIGH | ✅ **COMPLETED** |
| E | **Dynamic aiming** in teleop — atan2 toward goal | +5-10 (consistency) | 1-2 hr | MEDIUM | 🔄 **PENDING** (Day 5) |
| F | **Velocity-based shooting** — setVelocity() instead of setPower() | +3-5 (consistency) | 1 hr | MEDIUM | 🔄 **PENDING** (Day 1) |
| G | **Voltage compensation** on shooter | +2-3 (late-match) | 30 min | MEDIUM | 🔄 **PENDING** (Day 5) |
| H | **Color sensor telemetry** for drivers — show ramp pattern | +5-10 (pattern) | 1 hr | MEDIUM | 🔄 **PENDING** (Day 5) |
| I | **BezierCurve paths** for smoother auto arcs | +3-6 (time savings) | 2-3 hr + tuning | LOW (risky) | 🔄 **PENDING** |
| J | **Field-centric driving** toggle | Comfort | 30 min | LOW | 🔄 **PENDING** |

### Reliability Opportunities

| # | Opportunity | Impact | Effort |
|---|------------|--------|--------|
| K | **Auto-to-teleop pose handoff** | Enables field-aware teleop features | 30 min |
| L | **Re-tune PID** after encoder direction fix (P=0.005 → 0.015) | Faster, tighter auto paths | 2-3 hr field time |
| M | **Camera calibration** for your specific webcam | More accurate AprilTag detection | 1-2 hr |
| N | **Consolidate to 1 TeleOp** — eliminate 7 redundant copies | Prevents value inconsistency bugs | 2-3 hr |

---

## 7. Codebase Architecture Notes

### Current Architecture (Subsystem-Based)

```
Robot.java (central hub)
├── MecanumDrive (wraps Pedro Pathing Follower)
├── Intake (front + back motors)
├── Shooter (flywheel + hood)
├── PodServos (Pod1/2/3 + SpinTop with auto-retract)
└── ColorSensors (3x color sensors, HSV detection)

Lifecycle per loop:
  robot.read()      → clear bulk cache, read battery
  robot.periodic()  → update all subsystems
  robot.write()     → telemetry output
```

### File Inventory (29 .java files)

| Category | Competition | Development/Test | Should Disable |
|----------|------------|-----------------|----------------|
| **Auto** | `BigRedAuto`, `BigBlueAuto` | `BackAuto`, `JustForward`, `JustLeft`, `JustRight` | Yes (4 files) |
| **TeleOp** | `MainTeleOp` | `PedroLockTeleOp`, `MaxTeleOp`, `GyroHeadingTeleOp`, `BlueSmallRoboto`, `RedSmallRoboto`, `pedroPathing/TeleOP` | Yes (6 files) |
| **Test** | — | `AprilTagTest`, `LimelightTest`, `ColorSensorTest`, `ShooterTest`, `ServoTester` | Yes (5 files) |
| **Sample** | — | `BasicOmniOpMode_Linear` | Yes (1 file) |
| **Tuning** | `Tuning` (keep for pit) | — | No |
| **Infrastructure** | `Robot`, `Subsystem`, `GamepadEvents`, `Constants` | — | N/A (not OpModes) |

### Key Dependencies

```
FTC SDK: 11.0.0
Pedro Pathing: 2.0.1
Pedro Telemetry: 0.0.6
Bylazar Panels: 1.0.9
Bylazar Field: 1.0.5
Bylazar Docs: 1.0.0
Limelight Proxy: COMMENTED OUT (not available)
```

### Two Robot Configs in Codebase

The codebase contains code for TWO different physical robots:

| | "Big Robot" (NEW — competition) | "Small Robot" (OLD — retire) |
|---|---|---|
| **Files** | `MainTeleOp`, `BigRedAuto`, `BigBlueAuto`, all subsystems | `BlueSmallRoboto`, `RedSmallRoboto` |
| **Intake** | Dual: `FIntake` + `BIntake` | Single: `Intake` |
| **Shooter** | Single: `Shoot` | Dual: `Shoot1` + `Shoot2` |
| **Servos** | `Hood`, `Pod1-3`, `SpinTop` | `Rail` |
| **Sensors** | 3x color sensors | None |
| **Action** | Use for competition | `@Disable` all files |

---

## 8. Hardware Issues to Escalate

These are NOT code fixes. Escalate to the mechanical/build team immediately.

### H1: Red Shooter Friction (if carrying over from old robot)

**Evidence:** `RedSmallRoboto` uses 5000 RPM; `BlueSmallRoboto` uses 4000 RPM to achieve the same shot.
**Likely Cause:** Bad bearing, rubbing wire, friction, or different gear ratio.
**Action:** Physically inspect the shooter mechanism. A 25% RPM difference indicates a hardware problem that code cannot fix.

### H2: Intake Jamming on 3-Ball Stacks

**Context:** The "Blind Volume Cycle" auto requires intaking entire 3-ball spike mark stacks at speed.
**Risk:** Ground intakes jamming on 3 artifacts simultaneously is the #1 failure mode for this strategy.
**Action:** Ensure the intake tunnel has a "compliant" roof (surgical tubing or sprung plastic) that can expand if 3 balls enter at once. Test repeatedly.

### H3: Odometry Pod Mounting

**Context:** `Constants.java` specifies `forwardPodY(1)` and `strafePodX(-1)` — meaning 1 inch forward and 1 inch left of robot center.
**Action:** Physically measure the actual pod positions on the NEW robot with a ruler. Previous values were (0, -6) which are completely different. Wrong offsets = wrong autonomous positioning.

### H4: Camera Mounting for AprilTag Detection

**Context:** To read the obelisk motif, the camera needs a clear view of the AprilTag from the starting position.
**Challenges:**
- Direct sunlight can wash out AprilTags (common in gymnasiums)
- Multiple AprilTags on the obelisk may be visible simultaneously
- Camera must see the front face, not a side face
**Action:** Mount the webcam with a clear forward-facing view. Test AprilTag detection at the venue if possible. Use low exposure + high gain camera settings.

---

## Quick Reference: What to Run at Competition

### Before Each Match

1. Power on robot
2. Connect driver station
3. Select the correct OpMode:
   - **Red Alliance Auto:** `BigRedAuto`
   - **Blue Alliance Auto:** `BigBlueAuto`
4. Verify telemetry shows "Ready" and detected motif (once AprilTag is implemented)
5. Wait for match start

### After Auto → TeleOp Transition

1. Select `Main TeleOp`
2. Press init, then start

### Driver Controls (MainTeleOp)

| Control | Action |
|---------|--------|
| Left Stick | Drive (strafe) |
| Right Stick X | Rotate |
| Right Trigger | Front intake |
| Left Trigger | Back intake |
| D-Pad Up | Shooter ON |
| D-Pad Down | Shooter OFF |
| B | Stop intakes + Extend Pod1 |
| Y | Extend Pod2 |
| X | Extend Pod3 |
| A | Retract all pods |
| Left Bumper | SpinTop left |
| Right Bumper | SpinTop right |
| **Gamepad 2** D-Pad Up/Down | Hood up/down |
| **Gamepad 2** Right Bumper | Toggle slow mode |
| **Gamepad 2** X/Y | Adjust slow mode speed |
| **Gamepad 2** A | Start automated path |
| **Gamepad 2** B | Stop automated path |

---

*Last updated: February 6, 2026. Good luck at regionals — go get that top 3!*

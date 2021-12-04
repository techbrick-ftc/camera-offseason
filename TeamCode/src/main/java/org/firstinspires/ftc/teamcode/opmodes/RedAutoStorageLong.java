package org.firstinspires.ftc.teamcode.opmodes;

import static org.firstinspires.ftc.teamcode.libs.Globals.*;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;

import org.firstinspires.ftc.teamcode.libs.AutoImport;
import org.firstinspires.ftc.teamcode.libs.Globals;

@Autonomous(name="RedAutoStorageLong", group="teleop")
public class RedAutoStorageLong extends AutoImport {

    public RedAutoStorageLong() { super(65, -40, 55, 120, 185, 120); }

    public void runOpMode() {
        super.runOpMode();

        if (opModeIsActive()) {
            // Goes to spinner and does spinny
            slauto.drive(50, -60, -90, 0.75, this);
            setSpinny(true, 1000);

            // Goes to the shipping hub and delivers based on the team element position
            // 1 is added to elementPosition because height's 0 is ground level, not the first layer
            setArm(elementPosition + 1, 1);
            slauto.drive(25, -55, -90, 0.75, 0, this, false, true);
            slauto.drive(25, -33, -90, 0.75, 0, this, true, false);
            deposit(true);
            //runIntake(1, 1000);
            sleep(2000);
            deposit(false);

            // Goes over to the warehouse
            slauto.drive(0, -26, -90, 0.75, 0, this, false, true);
            slauto.drive(15, 6, 180, 0.75, 0, this, true, false);
            sleep(500);

            // Goes into the warehouse
            packet.put("zooming", "commenced");
            dashboard.sendTelemetryPacket(packet);
            slauto.drive(15, 37, 180, 1, 0, this, true, false);
            slauto.drive(15, 36, -90, 0.75, this);
            slauto.drive(40, 36, -90, 1, 0, this, true, false);

            // Lowers arm
            setArm(0, 1);
            while (armY.isBusy()){
                sleep(10);
            }

            stopCamera();
        }
    }
}
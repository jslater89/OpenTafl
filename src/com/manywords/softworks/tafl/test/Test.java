package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.test.ai.*;
import com.manywords.softworks.tafl.test.consistency.*;
import com.manywords.softworks.tafl.test.mechanics.ExternalEngineHostTest;
import com.manywords.softworks.tafl.test.mechanics.GameClockTest;
import com.manywords.softworks.tafl.test.mechanics.ServerTickThreadTest;
import com.manywords.softworks.tafl.test.rules.*;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void run() {
        List<TaflTest> tests = new ArrayList<TaflTest>();

        // Consistency tests
        tests.add(new GameSerializerConsistencyTest());
        tests.add(new MoveSerializerConsistencyTest());
        tests.add(new PositionSerializerConsistencyTest());
        tests.add(new RulesSerializerConsistencyTest());
        tests.add(new TranspositionTableConsistencyTest());
        tests.add(new NetworkPacketConsistencyTests());
        tests.add(new BerserkHistoryDuplicationTest());

        // Rules tests
        tests.add(new RestrictedFortReentryTest());
        tests.add(new ThreefoldDrawTest());
        tests.add(new ThreefoldVictoryTest());
        tests.add(new TablutKingCaptureTest());
        tests.add(new StrongKingCaptureTest());
        tests.add(new DoubleCaptureTest());
        tests.add(new CaptureTest());
        tests.add(new EdgeVictoryTest());
        tests.add(new CornerVictoryTest());
        tests.add(new RestrictedSpaceTest());
        tests.add(new EncirclementTest());
        tests.add(new StrictShieldwallTest());
        tests.add(new ShieldwallTest());
        tests.add(new EdgeFortEscapeTest());
        tests.add(new EdgeFortEscapeFailedTest());
        tests.add(new CommanderCaptureVictoryTest());
        tests.add(new CommanderCornerCaptureVictoryTest());
        tests.add(new JumpCaptureBerserkerTest());
        tests.add(new BerserkMoveDuplicationTest());
        tests.add(new BadFetlarCaptureTest());
        tests.add(new BadCopenhagenCaptureTest());

        // Mechanics tests
        tests.add(new ExternalEngineHostTest());
        tests.add(new GameClockTest());
        tests.add(new ServerTickThreadTest());
        tests.add(new KingMissingPositionRecordTest());

        // AI tests
        tests.add(new AIMatchingZobristTest());
        tests.add(new AICertainKingCaptureTest());
        tests.add(new AITwoCornerEscapeAndRulesLoadingTest());
        tests.add(new AITwoEdgeEscapeAndRulesLoadingTest());
        tests.add(new AIMoveRepetitionTest());

        for (TaflTest test : tests) {
            try {
                System.out.print(test.getClass().getSimpleName() + ": ");
                test.run();
                System.out.println("passed");
            } catch (AssertionError e) {
                System.out.println("FAILED");
                e.printStackTrace(System.out);
                System.exit(0);
            } catch (Exception e) {
                System.out.println("CRASHED");
                e.printStackTrace(System.out);
                System.exit(0);
            }
        }

        System.exit(0);
    }
}
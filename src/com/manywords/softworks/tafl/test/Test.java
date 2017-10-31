package com.manywords.softworks.tafl.test;

import com.manywords.softworks.tafl.Log;
import com.manywords.softworks.tafl.test.ai.*;
import com.manywords.softworks.tafl.test.ai.tactics.AITacticsEscapeTest;
import com.manywords.softworks.tafl.test.consistency.*;
import com.manywords.softworks.tafl.test.mechanics.*;
import com.manywords.softworks.tafl.test.network.HeadlessAITest;
import com.manywords.softworks.tafl.test.network.LoadServerGameTest;
import com.manywords.softworks.tafl.test.network.ServerTest;
import com.manywords.softworks.tafl.test.rules.*;
import com.manywords.softworks.tafl.ui.Ansi;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void run() {
        List<TaflTest> tests = new ArrayList<TaflTest>();

        // Initial tests (debug only)

        // Mechanics tests
        tests.add(new RepetitionHashTableTest());
        tests.add(new MoveRecordRotationMirrorTest());
        tests.add(new MoveAddressTest());
        tests.add(new ReplayGameTest());
        tests.add(new KingMissingPositionRecordTest());

        // Consistency tests
        tests.add(new GameSerializerConsistencyTest());
        tests.add(new MoveSerializerConsistencyTest());
        tests.add(new PositionSerializerConsistencyTest());
        tests.add(new RulesSerializerConsistencyTest());
        tests.add(new TranspositionTableConsistencyTest());
        tests.add(new NetworkPacketConsistencyTests());
        tests.add(new BerserkHistoryDuplicationTest());
        tests.add(new GenericRulesCopyTest());

        // Rules tests
        tests.add(new LinnaeanCaptureTest());
        tests.add(new MercenarySideSwitchTest());
        tests.add(new KingHammerAnvilTest());
        tests.add(new KingUnsafeAgainstBoardEdgeTest());
        tests.add(new SpeedLimitTest());
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
        tests.add(new RestrictedSpaceJumpTest());
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

        // Long-running mechanics tests
        tests.add(new ExternalEngineHostTest());
        tests.add(new GameClockTest());

        // Network tests
        tests.add(new ServerTickThreadTest());
        tests.add(new ServerTest());
        tests.add(new LoadServerGameTest());
        tests.add(new HeadlessAITest()); // also tests client connection somewhat

        // AI tests
        tests.add(new AIThreatEvaluationTest());
        tests.add(new AIMatchingZobristTest());
        tests.add(new AIMoveRepetitionTest());
        tests.add(new AIMoveConsiderationCrashTest());
        tests.add(new AIIncompleteContinuationTest());
        tests.add(new AISearchEquivalenceTest());
        tests.add(new AICertainKingEscapeTest());
        tests.add(new AICertainKingCaptureTest());
        tests.add(new AITwoCornerEscapeAndRulesLoadingTest());
        tests.add(new AITwoEdgeEscapeAndRulesLoadingTest());

        // Tactics tests
        tests.add(new AITacticsEscapeTest());


        for (TaflTest test : tests) {
            try {
                Log.println(Log.Level.CRITICAL, Ansi.UNDERLINE + test.getClass().getSimpleName() + Ansi.UNDERLINE_OFF + ": STARTING");
                test.run();
                Log.println(Log.Level.CRITICAL, Ansi.UNDERLINE + test.getClass().getSimpleName() + Ansi.UNDERLINE_OFF + ": PASSED");

            } catch (AssertionError e) {
                Log.println(Log.Level.CRITICAL, Ansi.UNDERLINE + test.getClass().getSimpleName() + Ansi.UNDERLINE_OFF + ": FAILED");
                Log.println(Log.Level.CRITICAL, "Assertion: " + e);
                Log.stackTrace(Log.Level.CRITICAL, e);
                System.exit(1);
            } catch (Exception e) {
                Log.println(Log.Level.CRITICAL, Ansi.UNDERLINE + test.getClass().getSimpleName() + Ansi.UNDERLINE_OFF + ": CRASHED");
                Log.println(Log.Level.CRITICAL, "Exception: " + e);
                Log.stackTrace(Log.Level.CRITICAL, e);
                System.exit(1);
            }

            System.out.println();
        }

        System.exit(0);
    }
}
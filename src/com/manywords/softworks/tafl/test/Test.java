package com.manywords.softworks.tafl.test;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void run() {
        List<TaflTest> tests = new ArrayList<TaflTest>();

        tests.add(new AIMatchingZobristTest());
        tests.add(new AICertainKingCaptureTest());
        tests.add(new AITwoCornerEscapeTest());
        tests.add(new AITwoEdgeEscapeTest());
        tests.add(new AIMoveRepetitionTest());
        tests.add(new StrongKingCaptureTest());
        tests.add(new DoubleCaptureTest());
        tests.add(new CaptureTest());
        tests.add(new EdgeVictoryTest());
        tests.add(new CornerVictoryTest());
        tests.add(new RestrictedSpaceTest());
        tests.add(new EncirclementTest());
        tests.add(new ShieldwallTest());
        tests.add(new StrictShieldwallTest());
        tests.add(new EdgeFortEscapeTest());
        tests.add(new EdgeFortEscapeFailedTest());
        tests.add(new CommanderCaptureVictoryTest());
        tests.add(new CommanderCornerCaptureVictoryTest());
        tests.add(new JumpCaptureBerserkerTest());
        tests.add(new BerserkMoveDuplicationTest());
        tests.add(new BadFetlarCaptureTest());
        tests.add(new BadCopenhagenCaptureTest());

        for (TaflTest test : tests) {
            try {
                test.run();
                System.out.println(test.getClass().getSimpleName() + ": passed");
            } catch (AssertionError e) {
                System.out.println(test.getClass().getSimpleName() + ": FAILED");
                //System.exit(0);
            }
        }
    }
}
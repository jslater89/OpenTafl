package com.manywords.softworks.tafl;

import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.manywords.softworks.tafl.ui.AdvancedTerminalHelper;
import com.manywords.softworks.tafl.ui.RawTerminal;

import java.io.IOException;
import java.util.Map;

public class Debug {
    public static void run(Map<String, String> args) {

        /*
        String rulesString = RulesSerializer.getRulesRecord(SeaBattle.newSeaBattle9());
        System.out.println(rulesString);
        Rules r = RulesSerializer.loadRulesRecord(rulesString);
        rulesString = RulesSerializer.getRulesRecord(r);
        System.out.println(rulesString);

        Board b = r.getBoard();
        RawTerminal.renderBoard(b);
        */

        DefaultTerminalFactory factory = new DefaultTerminalFactory();
        Terminal t = null;

        if(args.size() == 1) {
            t = factory.createSwingTerminal();
        }
        else {
            factory.setForceTextTerminal(true);
            try {
                t = factory.createTerminal();
            } catch(IOException e) {
                System.out.println("Unable to start.");
            }
        }

        if(t != null) {
            AdvancedTerminalHelper<? extends Terminal> th = new AdvancedTerminalHelper<>(t);
        }
        else {
            System.out.println("Exiting");
        }

		/*
		Rules rules = null;
		int depth = -1;
		for (Map.Entry<String, String> entry : args.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
		    if(key.equals("-v")) {
		    	if(value.startsWith("b")) {
		    		rules = Brandub.newBrandub7();
		    	}
		    	if(value.startsWith("f")) {
		    		rules = Fetlar.newFetlar11();
		    	}
		    	if(value.startsWith("c")) {
		    		rules = Copenhagen.newCopenhagen11();
		    	}
		    }
		    if(key.equals("-d")) {
		    	try {
		    		depth = Integer.parseInt(value);
		    	}
		    	catch (NumberFormatException e) {
		    		depth = -1;
		    	}
		    }
		}
		
		if(rules == null) {
			rules = Brandub.newBrandub7();
			System.out.println("No variant provided, using Brandub 7");
		}
		if(depth == -1) {
			System.out.println("No depth provided, using 3");
			depth = 3;
		}
		
		Game game = new Game(rules, null);
		AiWorkspace workspace = new AiWorkspace(game, game.getCurrentState());

		System.console().readLine("Begin? ");

		System.out.println("Exploration started.");
		long start = System.currentTimeMillis();
		workspace.explore(depth);
		while(!workspace.isThreadPoolIdle()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// Don't care
			}
			long finish = System.currentTimeMillis();
			double timeTaken = (finish - start) / 1000d;
			System.out.println("Time taken: " + timeTaken);
			System.out.println("States explored: " + workspace.getGameTree().size());
		}
		workspace.stopExploring();
		long finish = System.currentTimeMillis();
		double timeTaken = (finish - start) / 1000d;
		System.out.println("Exploration returned.");
		System.out.println("Time taken: " + timeTaken);
		System.out.println("States explored: " + workspace.getGameTree().size());
		double statesPerSec = workspace.getGameTree().size() / ((finish - start) / 1000d);
		System.out.println("States/sec: " + statesPerSec); 
		
		int[] treeSize = new int[depth];
		int[] totalBranchingFactor = new int[depth];
		GameTreeState bestState = null;
		System.out.println("Finding best state...");
		
		bestState = workspace.getTreeRoot().findBestChild(depth);
		
		System.out.println("Best state scored " + bestState.getEvaluation() + ".");
		RawTerminal.renderGameState(bestState);
		
		/*
		for(GameTreeState state : workspace.getGameTree()) {
			/*
			if(state.mBranches != null && state.mBranches.size() > 0) {
				treeSize[state.mDepth]++;
				totalBranchingFactor[state.mDepth] += state.mBranches.size();
			}
		}
		*/
		/*
		for(int i = 0; i < depth; i++) {
			System.out.println("Branching factor at depth " + i + ": " + ((double) totalBranchingFactor[i] / treeSize[i]));
		}
		*/
    }
}

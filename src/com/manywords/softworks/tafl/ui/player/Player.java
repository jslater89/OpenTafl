package com.manywords.softworks.tafl.ui.player;

import com.manywords.softworks.tafl.engine.Game;
import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.ui.UiCallback;

public abstract class Player {
    private Game mGame;
    private boolean mAttackingSide;

    public enum Type {
        HUMAN,
        NETWORK,
        AI,
        ENGINE
    }

    public interface PlayerCallback {
        public void onMoveDecided(Player player, MoveRecord record);
        public void notifyResignation(Player player);
    }

    public void setupPlayer() {

    }

    public Game getGame() {
        return mGame;
    }

    public boolean isAttackingSide() {
        return mAttackingSide;
    }

    public void setGame(Game game) {
        mGame = game;
    }

    public void setAttackingSide(boolean isAttackingSide) {
        mAttackingSide = isAttackingSide;
    }

    public void statusText(String text) {
        mGame.getUiCallback().statusText(text);
    }

    public abstract void getNextMove(UiCallback ui, Game game, int thinkTime);
    public abstract void moveResult(int moveResult);
    public abstract void opponentMove(MoveRecord move);
    public abstract void stop();
    public abstract void timeUpdate();
    public abstract void onMoveDecided(MoveRecord record);
    public abstract void setCallback(PlayerCallback callback);

    public abstract Type getType();

    public static Player getNewPlayer(Type type) {
        switch(type) {
            case HUMAN:
                return new LocalHuman();
            case NETWORK:
                return new LocalHuman();
            case AI:
                return new LocalAi();
            case ENGINE:
                return new ExternalEnginePlayer();
        }

        return null;
    }
}

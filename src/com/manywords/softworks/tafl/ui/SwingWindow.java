package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.engine.MoveRecord;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.command.CommandResult;
import com.manywords.softworks.tafl.command.player.Player;
import com.manywords.softworks.tafl.ui.swing.SquarePanel;

import javax.swing.*;
import java.awt.*;

public class SwingWindow extends JFrame implements UiCallback {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public SwingWindow() {
        setTitle("OpenTafl");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setVisible(true);

        getContentPane().add(initBoardUI());
    }

    private void initMenuUI() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    }

    private JPanel initBoardUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel boardPanel = new JPanel(new FlowLayout());
        boardPanel.setBackground(Color.gray);
        boardPanel.setPreferredSize(new Dimension(700, 700));

        SquarePanel board = new SquarePanel();
        board.setBackground(Color.DARK_GRAY);
        boardPanel.add(board);

        mainPanel.add(boardPanel, BorderLayout.CENTER);

        JPanel infoPanel = new JPanel();
        infoPanel.setBackground(Color.white);
        infoPanel.setPreferredSize(new Dimension(150, 700));

        mainPanel.add(infoPanel, BorderLayout.EAST);
        return mainPanel;
    }

    @Override
    public void gameStarting() {

    }

    @Override
    public void modeChanging(Mode mode, Object gameObject) {

    }

    @Override
    public void awaitingMove(Player currentPlayer, boolean isAttackingSide) {

    }

    @Override
    public void timeUpdate(Side side) {

    }

    @Override
    public void moveResult(CommandResult result, MoveRecord move) {

    }

    @Override
    public void statusText(String text) {

    }

    @Override
    public void modalStatus(String title, String text) {

    }

    @Override
    public void gameStateAdvanced() {

    }

    @Override
    public void victoryForSide(Side side) {

    }

    @Override
    public void gameFinished() {

    }

    @Override
    public MoveRecord waitForHumanMoveInput() {
        return null;
    }

    @Override
    public boolean inGame() {
        return false;
    }
}

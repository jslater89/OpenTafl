package com.manywords.softworks.tafl.ui;

import com.manywords.softworks.tafl.engine.UiCallback;
import com.manywords.softworks.tafl.rules.Side;
import com.manywords.softworks.tafl.ui.swing.SquarePanel;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame implements UiCallback {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public Window() {
        setTitle("OpenTafl");
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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
    public void gameStateAdvanced() {
        // TODO Auto-generated method stub

    }

    @Override
    public void victoryForSide(Side side) {
        // TODO Auto-generated method stub

    }
}

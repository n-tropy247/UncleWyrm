/*
 * Copyright (C) 2019 Ryan Castelli
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package unclewyrm;

import java.util.HashSet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Toolkit;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Re-creating an old TI-83 game from scratch. Credit for idea: Badja.
 * http://www.ticalc.org/archives/files/fileinfo/96/9683.html
 *
 * @author NTropy
 * @version 4.14.2019
 */
public class WyrmBoard extends JPanel implements ActionListener {

    /**
     * Board dimensions.
     */
    private static final int BOARD_HEIGHT = 500, BOARD_WIDTH = 500;

    /**
     * Game pieces dimensions.
     */
    private static final int BODY_RAD = 5, APPLE_RAD = 15;

    /**
     * Limits on number of pieces.
     */
    private static final int START_BODY_PIECES = 3,
            MAX_BODY_PIECES_BUFFERED = 50, MAX_BODY_PIECES_NO_BUFFER = 36;

    /**
     * Seed value for random placement of apples.
     */
    private static final int RAND_POS = 23;

    /**
     * Thread delay to limit fps.
     */
    private static final int DELAY = 100;

    /**
     * Delay on rotation thread.
     */
    private static final int ROTATE_DELAY = 20;

    /**
     * Positions of wyrm pieces.
     */
    private static final int[] X = new int[MAX_BODY_PIECES_BUFFERED],
            Y = new int[MAX_BODY_PIECES_BUFFERED];

    /**
     * Font size of status messaging.
     */
    private static final int FONT_SIZE = 14;

    /**
     * Score gain on hitting apple.
     */
    private static final int SCORE_INC = 5;

    /**
     * Tracks number of wyrm pieces in play.
     */
    private int pieces;

    /**
     * Position of current apple.
     */
    private int appleX, appleY;

    /**
     * Current position modifier per update.
     */
    private int xSpeed, ySpeed;

    /**
     * Tracks score.
     */
    private int scoreCount;

    /**
     * Current angle of rotation.
     */
    private double degree;

    /**
     * Tracks if game-ending scenario has been occurred.
     */
    private boolean inGame;

    /**
     * Tracks if game-winning is allowed.
     */
    private boolean allowWin;

    /**
     * Tracks if game has been won.
     */
    private boolean winGame;

    /**
     * Timer thread for game.
     */
    private Timer timer;

    /**
     * Sets final values for board and wyrm pieces.
     */
    public WyrmBoard() {
        this.degree = 0.0;

        this.inGame = true;
        this.allowWin = false;
        this.winGame = false;

        this.scoreCount = 0;

        configureBoard();
        initGame();
    }

    /**
     * Applies standard configuration to game window.
     */
    public final void configureBoard() {
        addKeyListener(new UWAdapter());
        setBackground(Color.gray);
        setFocusable(true);
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));

    }

    /**
     * Initializes starting values for game board and starts movement timer.
     */
    public final void initGame() {
        final int startX = 50, xSpacing = 10, startY = 50;

        pieces = START_BODY_PIECES;

        for (int j = 0; j < pieces; j++) {
            X[j] = startX - j * xSpacing;
            Y[j] = startY;
        }

        locateApple();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Runs on repaint, passes graphics to draw method.
     *
     * @param g Graphics of frame
     */
    @Override
    public final void paintComponent(final Graphics g) {
        super.paintComponent(g);
        if (winGame) {
            winGameScreen(g);
        } else {
            doDrawing(g);
        }
    }

    /**
     * Draws in wyrm components, apple, and scoreboard.
     *
     * @param g Graphics of frame
     */
    private void doDrawing(final Graphics g) {
        final int borderStartX = 2, borderY = 20, normalBorderBottomMargin = 45,
                normalBorderRightMargin = 5, winBorderBottomMargin = 25,
                winBorderRightMargin = 4, winBorderHoleWidth = 20,
                winMsgX = 5, winMsgY = 15;

        if (inGame) {
            if (allowWin) {
                //top border with hole
                g.drawLine(borderStartX, borderY, BOARD_WIDTH / 2
                        - winBorderHoleWidth, borderY);
                g.drawLine(BOARD_WIDTH / 2 + winBorderHoleWidth, borderY,
                        BOARD_WIDTH - winBorderRightMargin, borderY);

                g.drawLine(borderStartX, borderY, borderStartX, BOARD_HEIGHT
                        - winBorderBottomMargin); //left
                g.drawLine(BOARD_WIDTH - winBorderRightMargin, borderY,
                        BOARD_WIDTH - winBorderRightMargin,
                        BOARD_HEIGHT - winBorderBottomMargin); //right
                g.drawLine(borderStartX, BOARD_HEIGHT - winBorderBottomMargin,
                        BOARD_WIDTH - winBorderRightMargin,
                        BOARD_HEIGHT - winBorderBottomMargin); //bottom
            } else {
                g.drawRect(borderStartX, borderY, BOARD_WIDTH
                        - normalBorderRightMargin, BOARD_HEIGHT
                        - normalBorderBottomMargin);

                g.setColor(Color.red);
                g.fillOval(appleX, appleY, APPLE_RAD, APPLE_RAD);
            }
            for (int j = 0; j < pieces; j++) {
                if (j == 0) {
                    g.setColor(Color.green);
                    g.fillOval(X[j], Y[j], BODY_RAD, BODY_RAD);
                } else {
                    g.setColor(Color.orange);
                    g.fillOval(X[j], Y[j], BODY_RAD, BODY_RAD);
                }
            }

            String score = "Score: " + scoreCount;
            Font scoreFont = new Font("Helvetica", Font.BOLD, FONT_SIZE);
            g.setColor(Color.black);
            g.setFont(scoreFont);
            g.drawString(score, winMsgX, winMsgY);

            Toolkit.getDefaultToolkit().sync();
        } else if (!inGame) {
            gameOver(g);
        }
    }

    /**
     * Draws game over screen if collision detected.
     *
     * @param g Graphics of frame
     */
    private void gameOver(final Graphics g) {
        final int endMsgYMargin = 15;

        String end = "You Died";
        String score = "Score: " + scoreCount;
        Font endFont = new Font("Helvetica", Font.BOLD, FONT_SIZE);
        FontMetrics metr = getFontMetrics(endFont);
        g.setColor(Color.black);
        g.setFont(endFont);
        g.drawString(end, (BOARD_WIDTH - metr.stringWidth(end)) / 2,
                BOARD_HEIGHT / 2 - endMsgYMargin);
        g.drawString(score, (BOARD_WIDTH - metr.stringWidth(end)) / 2,
                BOARD_HEIGHT / 2);
    }

    /**
     * Checks for collision with apple.
     */
    private void checkApple() {
        if (pieces < MAX_BODY_PIECES_NO_BUFFER) {
            if ((X[0] >= appleX - 2 && X[0] <= appleX + APPLE_RAD)
                    && (Y[0] >= appleY && Y[0] <= appleY + APPLE_RAD)) {
                pieces += START_BODY_PIECES;
                scoreCount += SCORE_INC;
                for (int j = pieces - START_BODY_PIECES; j <= pieces; j++) {
                    X[j] = X[(j - 1)];
                    Y[j] = Y[(j - 1)];
                }
                locateApple();
            }
        }
    }

    /**
     * Moves wyrm by updating position of body pieces to that of its
     * predecessor.
     */
    private void move() {
        final double speedMultiplier = 10.0;

        for (int j = pieces; j > 0; j--) {
            X[j] = X[(j - 1)];
            Y[j] = Y[(j - 1)];
        }

        xSpeed = (int) (speedMultiplier * Math.cos(Math.toRadians(degree)));

        ySpeed = (int) (speedMultiplier * Math.sin(Math.toRadians(degree)));

        X[0] += xSpeed;

        Y[0] += ySpeed;
    }

    /**
     * Checks for collision with walls or body of wyrm.
     */
    private void checkCollision() {
        final int headPieceBuffer = 4, lowerWallHeightNonWin = 35,
                lowerWallHeightWin = 25, upperWallHeight = 20,
                rightWallBuffer = 11, leftWallMargin = 7, winPortalWidth = 20,
                pieceSpacing = 2;

        for (int j = pieces; j > 0; j--) {
            if ((j > headPieceBuffer) && (X[0] >= X[j] && X[0] <= X[j]
                    + BODY_RAD + pieceSpacing) && (Y[0] >= Y[j] && Y[0] <= Y[j]
                    + BODY_RAD + pieceSpacing)) {
                inGame = false;
            }
        }

        if (!allowWin) {
            if (Y[0] >= BOARD_HEIGHT - lowerWallHeightNonWin) {
                inGame = false;
            }
        } else {
            if (Y[0] >= BOARD_HEIGHT - lowerWallHeightWin) {
                inGame = false;
            }
        }

        if (Y[0] <= upperWallHeight) {
            inGame = false;
        }

        if (X[0] >= BOARD_WIDTH - rightWallBuffer) {
            inGame = false;
        } else if (X[0] <= leftWallMargin) {
            inGame = false;
        }

        if (pieces >= MAX_BODY_PIECES_NO_BUFFER) {
            allowWin = true;
        }

        if (allowWin && X[0] >= BOARD_WIDTH / 2 - winPortalWidth
                && X[0] <= BOARD_WIDTH / 2 + winPortalWidth
                && Y[0] <= upperWallHeight) {
            this.timer.stop();
            inGame = false;
            winGame = true;
        }

        if (!inGame) {
            this.timer.stop();
        }
        for (int j = pieces; j > 0; j--) {
            if ((j > headPieceBuffer) && (X[0] >= X[j] && X[0] <= X[j]
                    + BODY_RAD + pieceSpacing) && (Y[0] >= Y[j] && Y[0] <= Y[j]
                    + BODY_RAD + pieceSpacing)) {
                inGame = false;
            }
        }
    }

    /**
     * Draws in win screen.
     *
     * @param g Graphics of frame
     */
    private void winGameScreen(final Graphics g) {
        String win = "You Win!";
        Font endFont = new Font("Helvetica", Font.BOLD, FONT_SIZE);
        FontMetrics metr = getFontMetrics(endFont);
        g.setColor(Color.black);
        g.setFont(endFont);
        g.drawString(win, (BOARD_WIDTH - metr.stringWidth(win)) / 2,
                BOARD_HEIGHT / 2);
    }

    /**
     * Finds new apple position once an apple has been collected.
     */
    private void locateApple() {
        final int appleXBuffer = 5, appleLowerYBuffer = 25,
                appleUpperYBuffer = 20;

        int random = (int) (Math.random() * RAND_POS);
        appleX = random * APPLE_RAD;

        while (appleX > BOARD_WIDTH - (appleXBuffer + (2 * APPLE_RAD))
                || appleX < appleXBuffer) {
            random = (int) (Math.random() * RAND_POS);
            appleX = random * APPLE_RAD;
        }

        random = (int) (Math.random() * RAND_POS);
        appleY = random * APPLE_RAD;
        while (appleY > BOARD_HEIGHT - appleLowerYBuffer
                || appleY < appleUpperYBuffer) {
            random = (int) (Math.random() * RAND_POS);
            appleY = random * APPLE_RAD;
        }
    }

    /**
     * Runs major game methods on timer refresh.
     *
     * @param e ActionEvent passed on every timer iteration
     */
    @Override
    public final void actionPerformed(final ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }
        repaint();
    }

    /**
     * Implementation of KeyListener to handle rotation of wyrm.
     */
    public class UWAdapter implements KeyListener {

        /**
         * Tracks keys pressed.
         */
        private final HashSet<Integer> pressed = new HashSet<>();

        /**
         * Degrees of rotation gained/lost on keypress.
         */
        private final double degInc = 11.25, maxDeg = 360.0;

        /**
         * Constructor starts timer for rotation on separate thread.
         */
        public UWAdapter() {
            if (inGame) {
                new Timer(ROTATE_DELAY, (ActionEvent unused) -> {
                    if (pressed.contains(KeyEvent.VK_RIGHT)) {
                        degree += degInc;
                        if (degree >= maxDeg) {
                            degree = 0.0;
                        }
                    } else if (pressed.contains(KeyEvent.VK_LEFT)) {
                        degree -= degInc;
                        if (degree < 0) {
                            degree = maxDeg - degInc;
                        }
                    }
                }).start();
            }
        }

        /**
         * Does nothing, necessary override.
         *
         * @param e KeyEvent passed on key type
         */
        @Override
        public void keyTyped(final KeyEvent e) {
        }

        /**
         * Adds key pressed to HashSet.
         *
         * @param e KeyEvent passed on key press
         */
        @Override
        public final void keyPressed(final KeyEvent e) {
            pressed.add(e.getKeyCode());
        }

        /**
         * Removes key released from HashSet.
         *
         * @param e KeyEvent passed on key release
         */
        @Override
        public final void keyReleased(final KeyEvent e) {
            pressed.remove(e.getKeyCode());
        }
    }
}

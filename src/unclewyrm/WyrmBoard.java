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
 * Re-creating an old TI-83 game from scratch Credit for idea: Badja.
 * http://www.ticalc.org/archives/files/fileinfo/96/9683.html
 *
 * @author NTropy
 * @version 4.14.2019
 */
public class WyrmBoard extends JPanel implements ActionListener {

    /**
     * Height of board.
     */
    private static final int BOARD_HEIGHT = 500;
    /**
     * Width of level one.
     */
    private static final int BOARD_WIDTH_ONE = 500;
    /**
     * Width of level two.
     */
    private static final int BOARD_WIDTH_TWO = 400;
    /**
     * Width of level three.
     */
    private static final int BOARD_WIDTH_THREE = 300;
    /**
     * Radius of wyrm body pieces.
     */
    private static final int DOT_RAD = 5;
    /**
     * Radius of apples.
     */
    private static final int APPLE_RAD = 15;
    /**
     * Buffered max number of wyrm pieces.
     */
    private static final int NUM_DOTS = 50;
    /**
     * Seed value for random placement of apples.
     */
    private static final int RAND_POS = 23;
    /**
     * Thread delay to limit fps.
     */
    private static final int DELAY = 100;
    /**
     * Delay on rotate thread.
     */
    private static final int ROTATE_DELAY = 20;

    /**
     * Starting number of pieces.
     */
     private static final int START_DOTS = 3;

    /**
     * Horizontal positions of wyrm pieces.
     */
    private static final int[] X = new int[NUM_DOTS];
    /**
     * Vertical positions of wyrm pieces.
     */
    private static final int[] Y = new int[NUM_DOTS];

    /**
     * Tracks number of wyrm pieces in play.
     */
    private int dots;
    /**
     * Horizontal position of current apple.
     */
    private int appleX;
    /**
     * Vertical position of current apple.
     */
    private int appleY;
    /**
     * Current horizontal position modifier per update.
     */
    private int xSpeed;
    /**
     * Current vertical position modifier per update.
     */
    private int ySpeed;
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
     * Tracks if in level one.
     */
    private boolean levelOne;
    /**
     * Tracks if in level two.
     */
    private boolean levelTwo;
    /**
     * Tracks if in level three.
     */
    private boolean levelThree;
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
        this.levelOne = true;
        this.levelTwo = false;
        this.levelThree = false;
        this.allowWin = false;

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
        setPreferredSize(new Dimension(BOARD_WIDTH_ONE, BOARD_HEIGHT));

    }

    /**
     * Initializes starting values for game board and starts movement timer.
     */
    public final void initGame() {
        dots = START_DOTS;
        final int baseLevelAdjust = 50, levelTwoAdjust = 100,
                levelThreeAdjust = 150, posMultiplier = 10;

        if (levelOne) {
            for (int j = 0; j < dots; j++) {
                X[j] = baseLevelAdjust - j * posMultiplier;
                Y[j] = baseLevelAdjust;
            }
        } else if (levelTwo) {
            for (int j = 0; j < dots; j++) {
                X[j] = levelTwoAdjust - j * posMultiplier;
                Y[j] = baseLevelAdjust;
            }
        } else if (levelThree) {
            for (int j = 0; j < dots; j++) {
                X[j] = levelThreeAdjust - j * posMultiplier;
                Y[j] = baseLevelAdjust;
            }
        }

        winGame = false;

        locateApple();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Runs on repaint, passes graphics to draw method.
     *
     * @param g
     *          Graphics of frame
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
     * @param g
     *          Graphics of frame
     */
    private void doDrawing(final Graphics g) {
        if (inGame && levelOne) {
            if (allowWin) {
                final int topBorderY = 20, topBorderXAdjust = 4,
                        sideBorderYAdjust = 25;
                //top border with hole
                g.drawLine(2, topBorderY, BOARD_WIDTH_ONE / 2 - topBorderY,
                        topBorderY);
                g.drawLine(BOARD_WIDTH_ONE / 2 + topBorderY, topBorderY,
                        BOARD_WIDTH_ONE - topBorderXAdjust, topBorderY);

                //left
                g.drawLine(2, topBorderY, 2, BOARD_HEIGHT - sideBorderYAdjust);
                g.drawLine(BOARD_WIDTH_ONE - topBorderXAdjust, topBorderY,
                        BOARD_WIDTH_ONE - topBorderXAdjust,
                        BOARD_HEIGHT - sideBorderYAdjust); //right
                g.drawLine(2, BOARD_HEIGHT - sideBorderYAdjust,
                        BOARD_WIDTH_ONE - topBorderXAdjust,
                        BOARD_HEIGHT - sideBorderYAdjust); //bottom
            } else {
                final int borderTopLeft = 20, borderWAdjust = 5,
                        borderHAdjust = 45;
                g.drawRect(2, borderTopLeft, BOARD_WIDTH_ONE - borderWAdjust,
                        BOARD_HEIGHT - borderHAdjust);
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, APPLE_RAD, APPLE_RAD);
            }
            for (int j = 0; j < dots; j++) {
                if (j == 0) {
                    g.setColor(Color.green);
                    g.fillOval(X[j], Y[j], DOT_RAD, DOT_RAD);
                } else {
                    g.setColor(Color.orange);
                    g.fillOval(X[j], Y[j], DOT_RAD, DOT_RAD);
                }
            }
            final int fontSize = 14, scoreH = 5, scoreLen = 15;
            String score = "Score: " + scoreCount;
            Font scoreFont = new Font("Helvetica", Font.BOLD, fontSize);
            g.setColor(Color.black);
            g.setFont(scoreFont);
            g.drawString(score, scoreH, scoreLen);

            Toolkit.getDefaultToolkit().sync();
        } else if (inGame && levelTwo) {
            if (allowWin) {
                final int topBorderX = 52, topBorderY = 20,
                        topBorderXAdjust = 84, topBorderYAdjust = 47,
                        sideBorderYAdjust = 25;
                //top border with hole
                g.drawLine(topBorderX, topBorderY, (BOARD_WIDTH_TWO
                        + topBorderXAdjust) / 2 - topBorderY, topBorderY);
                g.drawLine((BOARD_WIDTH_TWO + topBorderXAdjust) / 2
                        + topBorderY, topBorderY, BOARD_WIDTH_TWO
                        + topBorderYAdjust, topBorderY);

                g.drawLine(topBorderX, topBorderY, topBorderX,
                        BOARD_HEIGHT - sideBorderYAdjust); //left
                g.drawLine(BOARD_WIDTH_TWO + topBorderYAdjust, topBorderY,
                        BOARD_WIDTH_TWO + topBorderYAdjust,
                        BOARD_HEIGHT - sideBorderYAdjust); //right
                g.drawLine(topBorderX, BOARD_HEIGHT - sideBorderYAdjust,
                        BOARD_WIDTH_TWO + topBorderYAdjust,
                        BOARD_HEIGHT - sideBorderYAdjust); //bottom
            } else {
                final int borderTopLeftY = 20, borderXAdjust = 5,
                        borderYAdjust = 45, borderTopLeftX = 52;
                g.drawRect(borderTopLeftX, borderTopLeftY,
                        BOARD_WIDTH_TWO - borderXAdjust,
                        BOARD_HEIGHT - borderYAdjust);
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, APPLE_RAD, APPLE_RAD);
            }
            for (int j = 0; j < dots; j++) {
                if (j == 0) {
                    g.setColor(Color.green);
                    g.fillOval(X[j], Y[j], DOT_RAD, DOT_RAD);
                } else {
                    g.setColor(Color.orange);
                    g.fillOval(X[j], Y[j], DOT_RAD, DOT_RAD);
                }
            }
            final int fontSize = 14, scoreH = 5, scoreLen = 15;
            String score = "Score: " + scoreCount;
            Font scoreFont = new Font("Helvetica", Font.BOLD, fontSize);
            g.setColor(Color.black);
            g.setFont(scoreFont);
            g.drawString(score, scoreH, scoreLen);

            Toolkit.getDefaultToolkit().sync();
        } else if (inGame && levelThree) {
            if (allowWin) {
                final int topBorderX = 102, topBorderY = 20,
                        topBorderLeftXAdjust = 200, topBorderRightXAdjust = 97,
                        topBorderYAdjust = 25;
                //top border with hole
                g.drawLine(topBorderX, topBorderY, (BOARD_WIDTH_THREE
                        + topBorderLeftXAdjust) / 2 - topBorderY, topBorderY);
                g.drawLine((BOARD_WIDTH_THREE + topBorderLeftXAdjust) / 2
                        + topBorderY, topBorderY, BOARD_WIDTH_THREE
                                + topBorderRightXAdjust, topBorderY);

                g.drawLine(topBorderX, topBorderY, topBorderX,
                        BOARD_HEIGHT - topBorderYAdjust); //left
                g.drawLine(BOARD_WIDTH_THREE + topBorderRightXAdjust,
                        topBorderY, BOARD_WIDTH_THREE + topBorderRightXAdjust,
                        BOARD_HEIGHT - topBorderYAdjust); //right
                g.drawLine(topBorderX, BOARD_HEIGHT - topBorderYAdjust,
                        BOARD_WIDTH_THREE + topBorderRightXAdjust,
                        BOARD_HEIGHT - topBorderYAdjust); //bottom
            } else {
                final int borderX = 102, borderY = 20, borderXAdjust = 5,
                        borderYAdjust = 45;
                g.drawRect(borderX, borderY, BOARD_WIDTH_THREE - borderXAdjust,
                        BOARD_HEIGHT - borderYAdjust);
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, APPLE_RAD, APPLE_RAD);
            }
            for (int j = 0; j < dots; j++) {
                if (j == 0) {
                    g.setColor(Color.green);
                    g.fillOval(X[j], Y[j], DOT_RAD, DOT_RAD);
                } else {
                    g.setColor(Color.orange);
                    g.fillOval(X[j], Y[j], DOT_RAD, DOT_RAD);
                }
            }
            final int fontSize = 14, scoreH = 5, scoreLen = 15;
            String score = "Score: " + scoreCount;
            Font scoreFont = new Font("Helvetica", Font.BOLD, fontSize);
            g.setColor(Color.black);
            g.setFont(scoreFont);
            g.drawString(score, scoreH, scoreLen);

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
        final int fontSize = 14, scoreHAdjust = 15;
        String end = "You Died";
        String score = "Score: " + scoreCount;
        Font endFont = new Font("Helvetica", Font.BOLD, fontSize);
        FontMetrics metr = getFontMetrics(endFont);
        g.setColor(Color.black);
        g.setFont(endFont);
        g.drawString(end, (BOARD_WIDTH_ONE - metr.stringWidth(end)) / 2,
                BOARD_HEIGHT / 2 - scoreHAdjust);
        g.drawString(score, (BOARD_WIDTH_ONE - metr.stringWidth(end)) / 2,
                BOARD_HEIGHT / 2);
    }

    /**
     * Checks for collision with apple.
     */
    private void checkApple() {
        final int maxDots = 36, dotGain = 3, scoreGain = 5;
        if (dots < maxDots) {
            if ((X[0] >= appleX - 2 && X[0] <= appleX + APPLE_RAD)
                    && (Y[0] >= appleY && Y[0] <= appleY + APPLE_RAD)) {
                dots += dotGain;
                scoreCount += scoreGain;
                for (int j = dots - dotGain; j <= dots; j++) {
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
        for (int j = dots; j > 0; j--) {
            X[j] = X[(j - 1)];
            Y[j] = Y[(j - 1)];
        }
        final double speedAdjust = 10.0;
        xSpeed = (int) (speedAdjust * Math.cos(Math.toRadians(degree)));

        ySpeed = (int) (speedAdjust * Math.sin(Math.toRadians(degree)));

        X[0] += xSpeed;

        Y[0] += ySpeed;
    }

    /**
     * Checks for collision with walls or body of wyrm.
     */
    private void checkCollision() {
        final int minDots = 4, noWinHAdjust = 35, winHAdjust = 25,
                leftWallX = 20, maxDots = 36;
        for (int j = dots; j > 0; j--) {
            if ((j > minDots) && (X[0] >= X[j] && X[0] <= X[j] + DOT_RAD + 2)
                    && (Y[0] >= Y[j] && Y[0] <= Y[j] + DOT_RAD + 2)) {
                inGame = false;
            }
        }

        if (!allowWin) {
            if (Y[0] >= BOARD_HEIGHT - noWinHAdjust) {
                inGame = false;
            }
        } else {
            if (Y[0] >= BOARD_HEIGHT - winHAdjust) {
                inGame = false;
            }
        }

        if (Y[0] <= leftWallX) {
            inGame = false;
        }

        if (levelOne) {
            final int rightWallXAdjust = 11, lvlOneLeftWallX = 5,
                    collisionAdjust = 20;
            if (X[0] >= BOARD_WIDTH_ONE - rightWallXAdjust) {
                inGame = false;
            } else if (X[0] <= lvlOneLeftWallX) {
                inGame = false;
            }

            if (dots >= maxDots) {
                allowWin = true;
            }

            if (allowWin && X[0] >= BOARD_WIDTH_ONE / 2 - collisionAdjust
                    && X[0] <= BOARD_WIDTH_ONE / 2 + collisionAdjust
                    && Y[0] <= collisionAdjust) {
                this.timer.stop();
                nextLevel();
            }
        } else if (levelTwo) {
            final int rightWallXAdjust = 35, lvlTwoLeftWallX = 55,
                    collisionAdjust = 84, dimAdjust = 20;
            if (X[0] >= BOARD_WIDTH_TWO + rightWallXAdjust) {
                inGame = false;
            } else if (X[0] <= lvlTwoLeftWallX) {
                inGame = false;
            }

            if (dots >= maxDots) {
                allowWin = true;
            }

            if (allowWin && X[0] >= (BOARD_WIDTH_TWO + collisionAdjust) / 2
                    - dimAdjust && X[0] <= (BOARD_WIDTH_TWO + collisionAdjust)
                    / 2 + dimAdjust && Y[0] <= dimAdjust) {
                this.timer.stop();
                nextLevel();
            }
        } else if (levelThree) {
            final int rightWallXAdjust = 85, lvlThreeLeftWallX = 105,
                    collisionAdjust = 200, dimAdjust = 20;
            if (!allowWin && X[0] >= BOARD_WIDTH_THREE + rightWallXAdjust) {
                inGame = false;
            } else if (allowWin && X[0] >= BOARD_WIDTH_THREE
                    + rightWallXAdjust) {
                inGame = false;
            } else if (X[0] <= lvlThreeLeftWallX) {
                inGame = false;
            }

            if (dots >= maxDots) {
                allowWin = true;
            }

            if (allowWin && X[0] >= (BOARD_WIDTH_THREE + collisionAdjust) / 2
                    - dimAdjust && X[0] <= (BOARD_WIDTH_THREE + collisionAdjust)
                    / 2 + dimAdjust && Y[0] <= dimAdjust) {
                this.timer.stop();
                winGame = true;
            }
        }

        if (!inGame) {
            this.timer.stop();
        }
        for (int j = dots; j > 0; j--) {
            if ((j > minDots) && (X[0] >= X[j] && X[0] <= X[j] + DOT_RAD + 2)
                    && (Y[0] >= Y[j] && Y[0] <= Y[j] + DOT_RAD + 2)) {
                inGame = false;
            }
        }
    }

    /**
     * Sets default values for next level.
     */
    private void nextLevel() {
        if (levelOne) {
            levelOne = false;
            levelTwo = true;
        } else if (levelTwo) {
            levelTwo = false;
            levelThree = true;
        }
        this.degree = 0.0;

        this.inGame = true;
        this.allowWin = false;

        initGame();
    }

    /**
     * Draws in win screen.
     *
     * @param g
     *          Graphics of frame
     */
    private void winGameScreen(final Graphics g) {
        final int fontSize = 14;
        String win = "You Win!";
        Font endFont = new Font("Helvetica", Font.BOLD, fontSize);
        FontMetrics metr = getFontMetrics(endFont);
        g.setColor(Color.black);
        g.setFont(endFont);
        g.drawString(win, (BOARD_WIDTH_ONE - metr.stringWidth(win)) / 2,
                BOARD_HEIGHT / 2);
    }

    /**
     * Finds new apple position once an apple has been collected.
     */
    private void locateApple() {
        int r = (int) (Math.random() * RAND_POS);
        appleX = r * APPLE_RAD;
        if (levelOne) {
            final int xAdjust = 5;
            while (appleX > BOARD_WIDTH_ONE - (xAdjust + (2 * APPLE_RAD))
                    || appleX < xAdjust) {
                r = (int) (Math.random() * RAND_POS);
                appleX = r * APPLE_RAD;
            }
        } else if (levelTwo) {
            final int xAdjust = 55;
            while (appleX > BOARD_WIDTH_ONE - (xAdjust + (2 * APPLE_RAD))
                    || appleX < xAdjust) {
                r = (int) (Math.random() * RAND_POS);
                appleX = r * APPLE_RAD;
            }
        } else if (levelThree) {
            final int xAdjust = 105;
            while (appleX > BOARD_WIDTH_ONE - (xAdjust + (2 * APPLE_RAD))
                    || appleX < xAdjust) {
                r = (int) (Math.random() * RAND_POS);
                appleX = r * APPLE_RAD;
            }
        }
        r = (int) (Math.random() * RAND_POS);
        appleY = r * APPLE_RAD;
        final int heightAdjust = 25, yAdjust = 20;
        while (appleY > BOARD_HEIGHT - heightAdjust || appleY < yAdjust) {
            r = (int) (Math.random() * RAND_POS);
            appleY = r * APPLE_RAD;
        }
    }

    /**
     * Runs major game methods on timer refresh.
     *
     * @param e
     *          ActionEvent passed on every timer iteration
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
         * Constructor starts timer for rotation on separate thread.
         */
        public UWAdapter() {
            final double degInc = 11.25, maxDeg = 360.0;
            if (inGame) {
                new Timer(ROTATE_DELAY, (ActionEvent arg0) -> {
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
         * @param e
         *          KeyEvent passed on key type
         */
        @Override
        public void keyTyped(final KeyEvent e) {
        }

        /**
         * Adds key pressed to HashSet.
         *
         * @param e
         *          KeyEvent passed on key press
         */
        @Override
        public final void keyPressed(final KeyEvent e) {
            pressed.add(e.getKeyCode());
        }

        /**
         * Removes key released from HashSet.
         *
         * @param e
         *          KeyEvent passed on key release
         */
        @Override
        public final void keyReleased(final KeyEvent e) {
            pressed.remove(e.getKeyCode());
        }
    }
}

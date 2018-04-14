/*
 * Copyright (C) 2018 Ryan Castelli
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
package UncleWyrm;

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
 * Re-creating an old TI-83 game from scratch Credit for idea: Badja
 * http://www.ticalc.org/archives/files/fileinfo/96/9683.html
 *
 * @author NTropy
 * @version 2.0
 */
public class WyrmBoard extends JPanel implements ActionListener {

    private final int BOARD_HEIGHT;
    private final int BOARD_WIDTH_ONE;
    private final int BOARD_WIDTH_TWO;
    private final int BOARD_WIDTH_THREE;
    private final int DOT_RAD;
    private final int APPLE_RAD;
    private final int NUM_DOTS;
    private final int RAND_POS;
    private final int DELAY;
    private final int ROTATE_DELAY;

    private final int x[];
    private final int y[];

    private int dots;
    private int appleX;
    private int appleY;
    private int xSpeed;
    private int ySpeed;
    private int scoreCount;

    private double degree;

    private boolean inGame;
    private boolean allowWin;
    private boolean levelOne;
    private boolean levelTwo;
    private boolean levelThree;
    private boolean winGame;

    private Timer timer;

    /**
     * Sets final values for board and wyrm pieces
     */
    public WyrmBoard() {
        this.BOARD_HEIGHT = 500;
        this.BOARD_WIDTH_ONE = 500;
        this.BOARD_WIDTH_TWO = 400;
        this.BOARD_WIDTH_THREE = 300;
        this.NUM_DOTS = 50; //max number of wyrm pieces with buffer
        this.APPLE_RAD = 15;
        this.DOT_RAD = 5;
        this.DELAY = 100;
        this.RAND_POS = 23;
        this.ROTATE_DELAY = 20;

        this.degree = 0.0;

        this.inGame = true;
        this.levelOne = true;
        this.levelTwo = false;
        this.levelThree = false;
        this.allowWin = false;

        this.y = new int[NUM_DOTS];
        this.x = new int[NUM_DOTS];

        this.scoreCount = 0;

        configureBoard();
        initGame();
    }

    /**
     * Applies standard configuration to game window
     */
    public final void configureBoard() {
        addKeyListener(new UWAdapter());
        setBackground(Color.gray);
        setFocusable(true);
        setPreferredSize(new Dimension(BOARD_WIDTH_ONE, BOARD_HEIGHT));

    }

    /**
     * Initializes starting values for game board and starts movement timer
     */
    public final void initGame() {
        dots = 33;

        if (levelOne) {
            for (int j = 0; j < dots; j++) {
                x[j] = 50 - j * 10;
                y[j] = 50;
            }
        } else if (levelTwo) {
            for (int j = 0; j < dots; j++) {
                x[j] = 100 - j * 10;
                y[j] = 50;
            }
        } else if (levelThree) {
            for (int j = 0; j < dots; j++) {
                x[j] = 150 - j * 10;
                y[j] = 50;
            }
        }

        winGame = false;

        locateApple();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Runs on repaint, passes graphics to draw method
     *
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (winGame) {
            winGameScreen(g);
        } else {
            doDrawing(g);
        }
    }
    
    private void drawWin(Graphics g) {
        
    }

    /**
     * Draws in wyrm components, apple, and scoreboard
     *
     * @param g
     */
    private void doDrawing(Graphics g) {
        if (inGame && levelOne) {
            if (allowWin) {
                //top border with hole
                g.drawLine(2, 20, BOARD_WIDTH_ONE / 2 - 20, 20);
                g.drawLine(BOARD_WIDTH_ONE / 2 + 20, 20, BOARD_WIDTH_ONE - 4, 20);

                g.drawLine(2, 20, 2, BOARD_HEIGHT - 25); //left
                g.drawLine(BOARD_WIDTH_ONE - 4, 20, BOARD_WIDTH_ONE - 4, BOARD_HEIGHT - 25); //right
                g.drawLine(2, BOARD_HEIGHT - 25, BOARD_WIDTH_ONE - 4, BOARD_HEIGHT - 25); //bottom
            } else {
                g.drawRect(2, 20, BOARD_WIDTH_ONE - 5, BOARD_HEIGHT - 45);
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, APPLE_RAD, APPLE_RAD);
            }
            for (int j = 0; j < dots; j++) {
                if (j == 0) {
                    g.setColor(Color.green);
                    g.fillOval(x[j], y[j], DOT_RAD, DOT_RAD);
                } else {
                    g.setColor(Color.orange);
                    g.fillOval(x[j], y[j], DOT_RAD, DOT_RAD);
                }
            }

            String score = "Score: " + scoreCount;
            Font scoreFont = new Font("Helvetica", Font.BOLD, 14);
            g.setColor(Color.black);
            g.setFont(scoreFont);
            g.drawString(score, 5, 15);

            Toolkit.getDefaultToolkit().sync();
        } else if (inGame && levelTwo) {
            if (allowWin) {
                //top border with hole
                g.drawLine(52, 20, (BOARD_WIDTH_TWO + 84) / 2 - 20, 20);
                g.drawLine((BOARD_WIDTH_TWO + 84) / 2 + 20, 20, BOARD_WIDTH_TWO + 47, 20);

                g.drawLine(52, 20, 52, BOARD_HEIGHT - 25); //left
                g.drawLine(BOARD_WIDTH_TWO + 47, 20, BOARD_WIDTH_TWO + 47, BOARD_HEIGHT - 25); //right
                g.drawLine(52, BOARD_HEIGHT - 25, BOARD_WIDTH_TWO + 47, BOARD_HEIGHT - 25); //bottom
            } else {
                g.drawRect(52, 20, BOARD_WIDTH_TWO - 5, BOARD_HEIGHT - 45);
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, APPLE_RAD, APPLE_RAD);
            }
            for (int j = 0; j < dots; j++) {
                if (j == 0) {
                    g.setColor(Color.green);
                    g.fillOval(x[j], y[j], DOT_RAD, DOT_RAD);
                } else {
                    g.setColor(Color.orange);
                    g.fillOval(x[j], y[j], DOT_RAD, DOT_RAD);
                }
            }

            String score = "Score: " + scoreCount;
            Font scoreFont = new Font("Helvetica", Font.BOLD, 14);
            g.setColor(Color.black);
            g.setFont(scoreFont);
            g.drawString(score, 5, 15);

            Toolkit.getDefaultToolkit().sync();
        } else if (inGame && levelThree) {
            if (allowWin) {
                //top border with hole
                g.drawLine(102, 20, (BOARD_WIDTH_THREE + 200) / 2 - 20, 20);
                g.drawLine((BOARD_WIDTH_THREE + 200) / 2 + 20, 20, BOARD_WIDTH_THREE + 97, 20);

                g.drawLine(102, 20, 102, BOARD_HEIGHT - 25); //left
                g.drawLine(BOARD_WIDTH_THREE + 97, 20, BOARD_WIDTH_THREE + 97, BOARD_HEIGHT - 25); //right
                g.drawLine(102, BOARD_HEIGHT - 25, BOARD_WIDTH_THREE + 97, BOARD_HEIGHT - 25); //bottom
            } else {
                g.drawRect(102, 20, BOARD_WIDTH_THREE - 5, BOARD_HEIGHT - 45);
                g.setColor(Color.red);
                g.fillOval(appleX, appleY, APPLE_RAD, APPLE_RAD);
            }
            for (int j = 0; j < dots; j++) {
                if (j == 0) {
                    g.setColor(Color.green);
                    g.fillOval(x[j], y[j], DOT_RAD, DOT_RAD);
                } else {
                    g.setColor(Color.orange);
                    g.fillOval(x[j], y[j], DOT_RAD, DOT_RAD);
                }
            }

            String score = "Score: " + scoreCount;
            Font scoreFont = new Font("Helvetica", Font.BOLD, 14);
            g.setColor(Color.black);
            g.setFont(scoreFont);
            g.drawString(score, 5, 15);

            Toolkit.getDefaultToolkit().sync();
        } else if (!inGame) {
            gameOver(g);
        }
    }

    /**
     * Draws game over screen if collision detected
     *
     * @param g
     */
    private void gameOver(Graphics g) {
        String end = "You Died";
        String score = "Score: " + scoreCount;
        Font endFont = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = getFontMetrics(endFont);
        g.setColor(Color.black);
        g.setFont(endFont);
        g.drawString(end, (BOARD_WIDTH_ONE - metr.stringWidth(end)) / 2, BOARD_HEIGHT / 2 - 15);
        g.drawString(score, (BOARD_WIDTH_ONE - metr.stringWidth(end)) / 2, BOARD_HEIGHT / 2);
    }

    /**
     * Checks for collision with apple
     */
    private void checkApple() {
        if (dots < 36) {
            if ((x[0] >= appleX - 2 && x[0] <= appleX + APPLE_RAD) && (y[0] >= appleY && y[0] <= appleY + APPLE_RAD)) {
                dots += 3;
                scoreCount += 5;
                for (int j = dots - 3; j <= dots; j++) {
                    x[j] = x[(j - 1)];
                    y[j] = y[(j - 1)];
                }
                locateApple();
            }
        }
    }

    /**
     * Moves wyrm by updating position of body pieces to that of its predecessor
     */
    private void move() {
        for (int j = dots; j > 0; j--) {
            x[j] = x[(j - 1)];
            y[j] = y[(j - 1)];
        }

        xSpeed = (int) (10.0 * Math.cos(Math.toRadians(degree)));

        ySpeed = (int) (10.0 * Math.sin(Math.toRadians(degree)));

        x[0] += xSpeed;

        y[0] += ySpeed;
    }

    /**
     * Checks for collision with walls or body of wyrm
     */
    private void checkCollision() {
        for (int j = dots; j > 0; j--) {
            if ((j > 4) && (x[0] >= x[j] && x[0] <= x[j] + DOT_RAD + 2) && (y[0] >= y[j] && y[0] <= y[j] + DOT_RAD + 2)) {
                inGame = false;
            }
        }

        if (!allowWin) {
            if (y[0] >= BOARD_HEIGHT - 35) {
                inGame = false;
            }
        } else {
            if (y[0] >= BOARD_HEIGHT - 25) {
                inGame = false;
            }
        }

        if (y[0] <= 20) {
            inGame = false;
        }

        if (levelOne) {
            if (x[0] >= BOARD_WIDTH_ONE - 11) {
                inGame = false;
            } else if (x[0] <= 5) {
                inGame = false;
            }

            if (dots >= 36) {
                allowWin = true;
            }

            if (allowWin && x[0] >= BOARD_WIDTH_ONE / 2 - 20 && x[0] <= BOARD_WIDTH_ONE / 2 + 20 && y[0] <= 20) {
                this.timer.stop();
                nextLevel();
            }
        } else if (levelTwo) {
            if (x[0] >= BOARD_WIDTH_TWO + 35) {
                inGame = false;
            } else if (x[0] <= 55) {
                inGame = false;
            }

            if (dots >= 36) {
                allowWin = true;
            }

            if (allowWin && x[0] >= (BOARD_WIDTH_TWO + 84) / 2 - 20 && x[0] <= (BOARD_WIDTH_TWO + 84) / 2 + 20 && y[0] <= 20) {
                this.timer.stop();
                nextLevel();
            }
        } else if (levelThree) {
            if (!allowWin && x[0] >= BOARD_WIDTH_THREE + 85) {
                inGame = false;
            } else if (allowWin && x[0] >= BOARD_WIDTH_THREE + 85) {
                inGame = false;
            } else if (x[0] <= 105) {
                inGame = false;
            }

            if (dots >= 36) {
                allowWin = true;
            }

            if (allowWin && x[0] >= (BOARD_WIDTH_THREE + 200) / 2 - 20 && x[0] <= (BOARD_WIDTH_THREE + 200) / 2 + 20 && y[0] <= 20) {
                this.timer.stop();
                winGame = true;
            }
        }

        if (!inGame) {
            this.timer.stop();
        }
        for (int j = dots; j > 0; j--) {
            if ((j > 4) && (x[0] >= x[j] && x[0] <= x[j] + DOT_RAD + 2) && (y[0] >= y[j] && y[0] <= y[j] + DOT_RAD + 2)) {
                inGame = false;
            }
        }
    }

    /**
     * Sets default values for next level
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
     * Draws in win screen
     *
     * @param g
     */
    private void winGameScreen(Graphics g) {
        String win = "You Win!";
        Font endFont = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = getFontMetrics(endFont);
        g.setColor(Color.black);
        g.setFont(endFont);
        g.drawString(win, (BOARD_WIDTH_ONE - metr.stringWidth(win)) / 2, BOARD_HEIGHT / 2);
    }

    /**
     * Finds new apple position once an apple has been collected
     */
    private void locateApple() {
        int r = (int) (Math.random() * RAND_POS);
        appleX = r * APPLE_RAD;
        if (levelOne) {
            while (appleX > BOARD_WIDTH_ONE - (5 + (2 * APPLE_RAD)) || appleX < 5) {
                r = (int) (Math.random() * RAND_POS);
                appleX = r * APPLE_RAD;
            }
        } else if (levelTwo) {
            while (appleX > BOARD_WIDTH_ONE - (55 + (2 * APPLE_RAD)) || appleX < 55) {
                r = (int) (Math.random() * RAND_POS);
                appleX = r * APPLE_RAD;
            }
        } else if (levelThree) {
            while (appleX > BOARD_WIDTH_ONE - (105 + (2 * APPLE_RAD)) || appleX < 105) {
                r = (int) (Math.random() * RAND_POS);
                appleX = r * APPLE_RAD;
            }
        }
        r = (int) (Math.random() * RAND_POS);
        appleY = r * APPLE_RAD;
        while (appleY > BOARD_HEIGHT - 25 || appleY < 20) {
            r = (int) (Math.random() * RAND_POS);
            appleY = r * APPLE_RAD;
        }
    }

    /**
     * Runs major game methods on timer refresh
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e
    ) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }
        repaint();
    }

    /**
     * Implementation of KeyListener to handle rotation of wyrm
     */
    public class UWAdapter implements KeyListener {

        private final HashSet<Integer> pressed = new HashSet<>();

        public Timer timer;

        /**
         * Constructor starts timer for rotation on separate thread
         */
        public UWAdapter() {
            if (inGame) {
                new Timer(ROTATE_DELAY, (ActionEvent arg0) -> {
                    if (pressed.contains(KeyEvent.VK_RIGHT)) {
                        degree += 11.25;
                        if (degree >= 360.0) {
                            degree = 0.0;
                        }
                    } else if (pressed.contains(KeyEvent.VK_LEFT)) {
                        degree -= 11.25;
                        if (degree < 0) {
                            degree = 360.0 - 11.25;
                        }
                    }
                }).start();
            }
        }

        /**
         * Does nothing, necessary override
         *
         * @param e
         */
        @Override
        public void keyTyped(KeyEvent e) {
        }

        /**
         * Adds key pressed to HashSet
         *
         * @param e
         */
        @Override
        public void keyPressed(KeyEvent e) {
            pressed.add(e.getKeyCode());
        }

        /**
         * Removes key released from HashSet
         *
         * @param e
         */
        @Override
        public void keyReleased(KeyEvent e) {
            pressed.remove(e.getKeyCode());
        }
    }
}

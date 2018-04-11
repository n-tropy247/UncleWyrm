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
 * Coding an old TI-83 game from scratch
 *
 * @author NTropy
 * @version 3/21/18
 */
public class WyrmBoard extends JPanel implements ActionListener {

    private final int BOARD_WIDTH;
    private final int BOARD_HEIGHT;
    private final int DOT_RAD;
    private final int APPLE_RAD;
    private final int NUM_DOTS;
    private final int RAND_POS;
    private final int DELAY;

    private final int x[];
    private final int y[];

    private int dots;
    private int appleX;
    private int appleY;
    private int xSpeed;
    private int ySpeed;
    private int widthDecrease;

    private double degree;

    private boolean inGame;
    private boolean allowWin;
    private boolean advance;

    private Timer timer;

    /**
     * Sets final values for board and wyrm pieces
     */
    public WyrmBoard() {
        this.BOARD_HEIGHT = 500;
        this.BOARD_WIDTH = 500;
        this.NUM_DOTS = 40; //max number of wyrm pieces with buffer
        this.APPLE_RAD = 15;
        this.DOT_RAD = 5;
        this.DELAY = 100;
        this.RAND_POS = 23;

        this.degree = 0.0;

        this.widthDecrease = 0;

        this.inGame = true;
        this.allowWin = false;

        this.y = new int[NUM_DOTS];
        this.x = new int[NUM_DOTS];

        configureBoard();
        initGame();
    }

    /**
     * Sets values for JFrame and adds listener
     */
    public final void configureBoard() {
        addKeyListener(new UWAdapter());
        setBackground(Color.gray);
        setFocusable(true);
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));

    }

    /**
     * Sets starting values and starts timer
     */
    public final void initGame() {
        dots = 3;

        for (int j = 0; j < dots; j++) {
            x[j] = (50 + widthDecrease) - j * 10;
            y[j] = 50;
        }

        locateApple();

        timer = new Timer(DELAY, this);
        timer.start();
    }

    /**
     * Method runs on every timer iteration, calls drawing method
     * @param g
     */
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);
    }

    /**
     * Draws in components of board and wyrm
     * @param g 
     */
    private void doDrawing(Graphics g) {
        if (inGame) {
            if (allowWin) {
                //top border with hole
                g.drawLine(2 + widthDecrease, 20, ((BOARD_WIDTH - (widthDecrease / 2)) / 2) - 20, 20);
                g.drawLine(((BOARD_WIDTH - (widthDecrease / 2)) / 2) + 20, 20, BOARD_WIDTH - 5 - widthDecrease, 20);

                g.drawLine(2 + widthDecrease, 20, 2 + widthDecrease, BOARD_HEIGHT - 25); //left
                g.drawLine(BOARD_WIDTH - 5 - widthDecrease, 20, BOARD_WIDTH - 5 - widthDecrease, BOARD_HEIGHT - 25); //right
                g.drawLine(2 + widthDecrease, BOARD_HEIGHT - 25, BOARD_WIDTH - 5 - widthDecrease, BOARD_HEIGHT - 25); //bottom
            } else {
                g.drawRect(2 + widthDecrease, 20, BOARD_WIDTH - 5 - (widthDecrease * 2), BOARD_HEIGHT - 45);
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

            String score = "Score: " + (dots - 3);
            Font scoreFont = new Font("Helvetica", Font.BOLD, 14);
            g.setColor(Color.black);
            g.setFont(scoreFont);
            g.drawString(score, 5, 15);

            Toolkit.getDefaultToolkit().sync();
        } else {
            gameOver(g);
        }
    }

    /**
     * Shows game over screen
     * @param g 
     */
    private void gameOver(Graphics g) {
        String end = "You Died";
        Font endFont = new Font("Helvetica", Font.BOLD, 14);
        FontMetrics metr = getFontMetrics(endFont);
        g.setColor(Color.black);
        g.setFont(endFont);
        g.drawString(end, (BOARD_WIDTH - metr.stringWidth(end)) / 2, BOARD_HEIGHT / 2);
    }

    /**
     * Checks for collision with apple, if true it finds the next one
     */
    private void checkApple() {
        if ((x[0] >= appleX && x[0] <= appleX + (APPLE_RAD * 2)) && (y[0] >= appleY && y[0] <= appleY + (APPLE_RAD * 2))) {
            dots += 3;
            for (int j = dots - 3; j <= dots; j++) {
                x[j] = x[(j - 1)];
                y[j] = y[(j - 1)];
            }
            locateApple();
        }
    }

    /**
     * Sets new positions of wyrm pieces
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
     * Checks for collisions with walls and wyrm body
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

        if (x[0] >= (BOARD_WIDTH - 11) - widthDecrease) {
            inGame = false;
        }

        if (x[0] <= 3 + widthDecrease) {
            inGame = false;
        }

        if (dots >= 36) {
            allowWin = true;
        }

        if (allowWin && x[0] >= (BOARD_WIDTH + (widthDecrease / 2)) / 2 - 20 && x[0] <= (BOARD_WIDTH + (widthDecrease / 2)) / 2 + 20 && y[0] <= 20) {
            widthDecrease += 50;
            this.timer.stop();
            nextLevel();
        }

        if (!inGame) {
            this.timer.stop();
        }
    }

    /**
     * Sets default values for next level
     */
    private void nextLevel() {
        this.degree = 0.0;

        this.inGame = true;
        this.allowWin = false;
        this.advance = false;

        setSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        initGame();
    }

    /**
     * Finds new apple position
     */
    private void locateApple() {
        int r = (int) (Math.random() * RAND_POS);
        appleX = r * APPLE_RAD;
        while (appleX > BOARD_WIDTH - (5 + (2 * APPLE_RAD)) - widthDecrease || appleX < (5 + widthDecrease)) {
            r = (int) (Math.random() * RAND_POS);
            appleX = r * APPLE_RAD;
        }
        r = (int) (Math.random() * RAND_POS);
        appleY = r * APPLE_RAD;
        while (appleY > BOARD_HEIGHT - 25 || appleY < 20) {
            r = (int) (Math.random() * RAND_POS);
            appleY = r * APPLE_RAD;
        }
    }

    /**
     * Timer iteration triggers this method, runs major gameplay methods
     * @param e 
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame) {
            checkApple();
            checkCollision();
            move();
        }
        repaint();
    }

    /**
     * Adaptation of key listener specific to this game
     */
    public class UWAdapter implements KeyListener {

        public boolean t = true;
        private final HashSet<Integer> pressed = new HashSet<>();

        public Timer timer;

        /**
         * Contains timer to rotate wyrm
         */
        public UWAdapter() {
            if (inGame) {
                new Timer(20, (ActionEvent arg0) -> {
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
         * Necessary override, does nothing
         * @param e
         */
        @Override
        public void keyTyped(KeyEvent e) {
        }

        /**
         * Adds key pressed to Hashset
         * @param e 
         */
        @Override
        public void keyPressed(KeyEvent e) {
            pressed.add(e.getKeyCode());
        }

        /**
         * Removes key released from Hashset
         * @param e 
         */
        @Override
        public void keyReleased(KeyEvent e) {
            pressed.remove(e.getKeyCode());
        }
    }
}
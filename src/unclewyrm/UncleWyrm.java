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
package unclewyrm;

import java.awt.EventQueue;
import javax.swing.JFrame;
/**
 * Creates actual game utilizing WyrmBoard class.
 *
 * @author NTropy
 * @version 9/3/2018
 */
public class UncleWyrm extends JFrame {

    /**
     * Constructor for JFrame.
     */
    public UncleWyrm() {
        configureBoard();
    }

    /**
     * Configures JFrame options.
     */
    private void configureBoard() {
        add(new WyrmBoard());
        setResizable(false);
        pack();

        setTitle("Uncle Wyrm");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Creates the game runnable.
     * @param args
     *          command-line arguments
     */
    public static void main(final String[] args) {
        EventQueue.invokeLater(() -> {
            JFrame ex = new UncleWyrm();
            ex.setVisible(true);
        });
    }
}
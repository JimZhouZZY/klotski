/**
 * EnhancedKlotskiGame.java
 * 
 * This class extends the KlotskiGame class to provide an enhanced version of the game.
 * 
 * ** TODO **
 * - REFACROT TO DELETED OR IMPROVE THIS CLASS
 * - THIS CLASS SHOULD NOT BE A "CLASS" IF IT DOES NOT PROVIDE ANY NEW FUNCTIONALITY
 * - IT SHOULD BE INSIDE KltoskiGame.java, AS PART OF THE GAME LOGIC, OR PROVIDE MORE LOGIC
 * 
 * @author Tommy-SUStech
 * @author JimZhouZZY
 * @version 1.5
 * @since 2025-05-26
 * 
 * Change log:
 * 2025-05-27: Generated comment
 * 2025-05-27: implement levels for 'enhanced' game
 * 2025-05-27: Multilevel for blocked
 * 2025-05-27: fix: arrow key causes crash when selecting blocked pieces
 * 2025-05-27: implement blocked pieces
 */

package io.github.jimzhouzzy.klotski.logic;

public class EnhancedKlotskiGame extends KlotskiGame {
    public int level;
    
    /**
     * Retrieves the blocked identifier associated with a specified game level. The mapping between levels
     * and blocked IDs is predefined as follows:
     * <ul>
     * <li>Level1: Blocked ID2</li>
     * <li>Level2: Blocked ID9</li>
     * <li>Level3: Blocked ID1</li>
     * <li>Level4: Blocked ID3</li>
     * <li>Level5: Blocked ID4</li>
     * </ul>
     * If the provided level does not match any of the predefined cases (1-5), the method returns -1
     * to indicate an invalid or unrecognized level.
     *
     * @param level The game level for which to retrieve the blocked identifier. Valid values are1 through5.
     * @return The blocked ID corresponding to the specified level, or -1 if the level is invalid.
     */
    public static int getBlockedIdForLevel(int level) {
            switch (level) {
                case 1:
                    return 2;
                case 2:
                    return 9;
                case 3:
                    return 1;
                case 4:
                    return 3;
                case 5:
                    return 4;
                default:
                    // default to level 1
                    return -1;
            }
        }

    public EnhancedKlotskiGame(String level) {
        // Parse string to int
        this.level = Integer.parseInt(level);
        switch (this.level) {
            case 1:
                this.blockedId = 2;
                pieces = new KlotskiPiece[10];
                // Cao Cao (2x2) (row x col)
                // Cao Cao should always have id = 0
                pieces[0] = new KlotskiPiece(0, "Cao Cao", 'C', 2, 2, new int[]{0, 1});
                // Guan Yu (2x1)
                pieces[1] = new KlotskiPiece(1, "Guan Yu", 'Y', 2, 1, new int[]{3, 1});
                // Generals (1x2)
                pieces[2] = new KlotskiPiece(2, "General 1", 'G', 1, 2, new int[]{0, 0});
                pieces[3] = new KlotskiPiece(3, "General 2", 'G', 1, 2, new int[]{0, 3});
                pieces[4] = new KlotskiPiece(4, "General 3", 'G', 1, 2, new int[]{2, 0});
                pieces[5] = new KlotskiPiece(5, "General 4", 'G', 1, 2, new int[]{-1, -1});
                // Soldiers (1x1)
                pieces[6] = new KlotskiPiece(6, "Soldier 1", 'S', 1, 1, new int[]{4, 0});
                pieces[7] = new KlotskiPiece(7, "Soldier 2", 'S', 1, 1, new int[]{4, 1});
                pieces[8] = new KlotskiPiece(8, "Soldier 3", 'S', 1, 1, new int[]{4, 2});
                pieces[9] = new KlotskiPiece(9, "Soldier 4", 'S', 1, 1, new int[]{3, 3});

                moveCount = 0;
                break;
            case 2:
                this.blockedId = 9;
                pieces = new KlotskiPiece[10];
                // Cao Cao (2x2) (row x col)
                // Cao Cao should always have id = 0
                pieces[0] = new KlotskiPiece(0, "Cao Cao", 'C', 2, 2, new int[]{0, 1});
                // Guan Yu (2x1)
                pieces[1] = new KlotskiPiece(1, "Guan Yu", 'Y', 2, 1, new int[]{3, 1});
                // Generals (1x2)
                pieces[2] = new KlotskiPiece(2, "General 1", 'G', 1, 2, new int[]{0, 0});
                pieces[3] = new KlotskiPiece(3, "General 2", 'G', 1, 2, new int[]{0, 3});
                pieces[4] = new KlotskiPiece(4, "General 3", 'G', 1, 2, new int[]{2, 0});
                pieces[5] = new KlotskiPiece(5, "General 4", 'G', 1, 2, new int[]{-1, -1});
                // Soldiers (1x1)
                pieces[6] = new KlotskiPiece(6, "Soldier 1", 'S', 1, 1, new int[]{4, 0});
                pieces[7] = new KlotskiPiece(7, "Soldier 2", 'S', 1, 1, new int[]{4, 1});
                pieces[8] = new KlotskiPiece(8, "Soldier 3", 'S', 1, 1, new int[]{4, 2});
                pieces[9] = new KlotskiPiece(9, "Soldier 4", 'S', 1, 1, new int[]{4, 3});

                moveCount = 0;
                break;
            case 3:
                this.blockedId = 1;
                pieces = new KlotskiPiece[10];
                // Cao Cao (2x2) (row x col)
                // Cao Cao should always have id = 0
                pieces[0] = new KlotskiPiece(0, "Cao Cao", 'C', 2, 2, new int[]{1, 1});
                // Guan Yu (2x1)
                pieces[1] = new KlotskiPiece(1, "Guan Yu", 'Y', 2, 1, new int[]{0, 1});
                // Generals (1x2)
                pieces[2] = new KlotskiPiece(2, "General 1", 'G', 1, 2, new int[]{0, 0});
                pieces[3] = new KlotskiPiece(3, "General 2", 'G', 1, 2, new int[]{0, 3});
                pieces[4] = new KlotskiPiece(4, "General 3", 'G', 1, 2, new int[]{2, 0});
                pieces[5] = new KlotskiPiece(5, "General 4", 'G', 1, 2, new int[]{-1, -1});
                // Soldiers (1x1)
                pieces[6] = new KlotskiPiece(6, "Soldier 1", 'S', 1, 1, new int[]{4, 0});
                pieces[7] = new KlotskiPiece(7, "Soldier 2", 'S', 1, 1, new int[]{4, 1});
                pieces[8] = new KlotskiPiece(8, "Soldier 3", 'S', 1, 1, new int[]{4, 2});
                pieces[9] = new KlotskiPiece(9, "Soldier 4", 'S', 1, 1, new int[]{4, 3});

                moveCount = 0;
                break;
            case 4:
                this.blockedId = 3;
                pieces = new KlotskiPiece[10];
                // Cao Cao (2x2) (row x col)
                // Cao Cao should always have id = 0
                pieces[0] = new KlotskiPiece(0, "Cao Cao", 'C', 2, 2, new int[]{0, 1});
                // Guan Yu (2x1)
                pieces[1] = new KlotskiPiece(1, "Guan Yu", 'Y', 2, 1, new int[]{3, 1});
                // Generals (1x2)
                pieces[2] = new KlotskiPiece(2, "General 1", 'G', 1, 2, new int[]{0, 0});
                pieces[3] = new KlotskiPiece(3, "General 2", 'G', 1, 2, new int[]{0, 3});
                pieces[4] = new KlotskiPiece(4, "General 3", 'G', 1, 2, new int[]{2, 0});
                pieces[5] = new KlotskiPiece(5, "General 4", 'G', 1, 2, new int[]{-1, -1});
                // Soldiers (1x1)
                pieces[6] = new KlotskiPiece(6, "Soldier 1", 'S', 1, 1, new int[]{4, 0});
                pieces[7] = new KlotskiPiece(7, "Soldier 2", 'S', 1, 1, new int[]{4, 1});
                pieces[8] = new KlotskiPiece(8, "Soldier 3", 'S', 1, 1, new int[]{4, 2});
                pieces[9] = new KlotskiPiece(9, "Soldier 4", 'S', 1, 1, new int[]{4, 3});

                moveCount = 0;
                break;
            case 5:
                this.blockedId = 4;
                pieces = new KlotskiPiece[10];
                // Cao Cao (2x2) (row x col)
                // Cao Cao should always have id = 0
                pieces[0] = new KlotskiPiece(0, "Cao Cao", 'C', 2, 2, new int[]{0, 1});
                // Guan Yu (2x1)
                pieces[1] = new KlotskiPiece(1, "Guan Yu", 'Y', 2, 1, new int[]{3, 1});
                // Generals (1x2)
                pieces[2] = new KlotskiPiece(2, "General 1", 'G', 1, 2, new int[]{2, 3});
                pieces[3] = new KlotskiPiece(3, "General 2", 'G', 1, 2, new int[]{0, 3});
                pieces[4] = new KlotskiPiece(4, "General 3", 'G', 1, 2, new int[]{3, 0});
                pieces[5] = new KlotskiPiece(5, "General 4", 'G', 1, 2, new int[]{-1, -1});
                // Soldiers (1x1)
                pieces[6] = new KlotskiPiece(6, "Soldier 1", 'S', 1, 1, new int[]{0, 0});
                pieces[7] = new KlotskiPiece(7, "Soldier 2", 'S', 1, 1, new int[]{1, 0});
                pieces[8] = new KlotskiPiece(8, "Soldier 3", 'S', 1, 1, new int[]{4, 2});
                pieces[9] = new KlotskiPiece(9, "Soldier 4", 'S', 1, 1, new int[]{4, 3});

                moveCount = 0;
                break;
            default:
                // Default to level 1
                this.blockedId = 2;
                initialize();
                break;
        }
    }

    public EnhancedKlotskiGame(int blockedId) {
        super(blockedId);
    }

    /**
     * Initializes the game board for Klotski Level1 by creating and positioning all playable pieces.
     * Sets up a10-piece configuration consisting of:
     * -1x2x2 piece (Cao Cao with ID0, positioned at row0, column1)
     * -1x2x1 vertical piece (Guan Yu at row3, column1)
     * -4x1x2 horizontal pieces (Generals1-4: two at top corners, one at row2 column0, and one inactive at invalid coordinates)
     * -4x1x1 soldier pieces positioned in the bottom row and at row3 column3
     * Resets the move counter to0. All piece positions are specified as [row, column] coordinates
     * following game board conventions. This configuration represents the classic starting layout
     * for the first difficulty level of Klotski puzzles.
     */
    @Override
        public void initialize() {
            // == level 1 ==
            pieces = new KlotskiPiece[10];
            // Cao Cao (2x2) (row x col)
            // Cao Cao should always have id = 0
            pieces[0] = new KlotskiPiece(0, "Cao Cao", 'C', 2, 2, new int[]{0, 1});
            // Guan Yu (2x1)
            pieces[1] = new KlotskiPiece(1, "Guan Yu", 'Y', 2, 1, new int[]{3, 1});
            // Generals (1x2)
            pieces[2] = new KlotskiPiece(2, "General 1", 'G', 1, 2, new int[]{0, 0});
            pieces[3] = new KlotskiPiece(3, "General 2", 'G', 1, 2, new int[]{0, 3});
            pieces[4] = new KlotskiPiece(4, "General 3", 'G', 1, 2, new int[]{2, 0});
            pieces[5] = new KlotskiPiece(5, "General 4", 'G', 1, 2, new int[]{-1, -1});
            // Soldiers (1x1)
            pieces[6] = new KlotskiPiece(6, "Soldier 1", 'S', 1, 1, new int[]{4, 0});
            pieces[7] = new KlotskiPiece(7, "Soldier 2", 'S', 1, 1, new int[]{4, 1});
            pieces[8] = new KlotskiPiece(8, "Soldier 3", 'S', 1, 1, new int[]{4, 2});
            pieces[9] = new KlotskiPiece(9, "Soldier 4", 'S', 1, 1, new int[]{3, 3});
    
            moveCount = 0;
        }
}

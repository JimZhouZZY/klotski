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
 * @version 1.4
 * @since 2025-05-26
 * 
 * Change log:
 * 2025-05-27: implement levels for 'enhanced' game
 * 2025-05-27: Multilevel for blocked
 * 2025-05-27: fix: arrow key causes crash when selecting blocked pieces
 * 2025-05-27: implement blocked pieces
 */

package io.github.jimzhouzzy.klotski.logic;

public class EnhancedKlotskiGame extends KlotskiGame {
    public int level;
    
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

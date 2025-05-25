package io.github.jimzhouzzy.klotski.logic;

public class KlotskiNewGame extends KlotskiGame {

    @Override
    public void initialize() {
        pieces = new KlotskiPiece[8];

        // Cao Cao
        pieces[0] = new KlotskiPiece(0, "Cao Cao", 'C', 2, 2, new int[]{0, 1});
        // Guan Yu
        pieces[1] = new KlotskiPiece(1, "Guan Yu", 'Y', 2, 1, new int[]{3, 1});
        // Generals
        pieces[2] = new KlotskiPiece(2, "General 1", 'G', 1, 2, new int[]{0, 0});
        pieces[3] = new KlotskiPiece(3, "General 2", 'G', 1, 2, new int[]{0, 3});
        pieces[4] = new KlotskiPiece(4, "General 3", 'G', 1, 2, new int[]{2, 0});
        // Soldiers
        pieces[5] = new KlotskiPiece(5, "Soldier 1", 'S', 1, 1, new int[]{4, 0});
        pieces[6] = new KlotskiPiece(6, "Soldier 2", 'S', 1, 1, new int[]{4, 1});
        pieces[7] = new KlotskiPiece(7, "Soldier 3", 'S', 1, 1, new int[]{4, 2});

        moveCount = 0;
    }
}

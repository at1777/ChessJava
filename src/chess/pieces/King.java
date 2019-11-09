package chess.pieces;

import chess.ChessBoard;
import chess.ChessColor;

public class King extends Piece {

    public King(ChessBoard parent, ChessColor color, int startRow, int startCol) {
        super(parent, color, startRow, startCol);
    }

    @Override
    public boolean checkMove(int newRow, int newCol) {
        if (!super.checkMove(newRow, newCol))
            return false;

        return isAdjacent(newRow, newCol);
    }
}

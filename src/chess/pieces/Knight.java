package chess.pieces;

import chess.ChessBoard;
import chess.ChessColor;

public class Knight extends Piece {
    public Knight(ChessBoard parent, ChessColor color, int startRow, int startCol) {
        super(parent, color, startRow, startCol);
    }

    @Override
    public boolean checkMove(int newRow, int newCol) {
        if (!super.checkMove(newRow, newCol))
            return false;

        // All the possible moves a knight can make
        return     (getRow() + 2 == newRow && getCol() + 1 == newCol)
                || (getRow() + 2 == newRow && getCol() - 1 == newCol)
                || (getRow() - 2 == newRow && getCol() + 1 == newCol)
                || (getRow() - 2 == newRow && getCol() - 1 == newCol)
                || (getCol() + 2 == newCol && getRow() + 1 == newRow)
                || (getCol() + 2 == newCol && getRow() - 1 == newRow)
                || (getCol() - 2 == newCol && getRow() + 1 == newRow)
                || (getCol() - 2 == newCol && getRow() - 1 == newRow);
    }
}

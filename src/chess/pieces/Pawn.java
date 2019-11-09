package chess.pieces;

import chess.ChessBoard;
import chess.ChessColor;

public class Pawn extends Piece {
    private int startCol;

    public Pawn(ChessBoard parent, ChessColor color, int startRow, int startCol) {
        super(parent, color, startRow, startCol);
        this.startCol = startCol;
    }

    @Override
    public boolean checkMove(int newRow, int newCol) {
        if (!super.checkMove(newRow, newCol))
            return false;

        // Move forward one
        int forwardDr = getColor() == ChessColor.WHITE ? -1 : 1;

        if (newRow == getRow()) {
            if (getCol() == startCol && getCol() + forwardDr * 2 == newCol)
                return checkPiece(newRow, newCol, 0, forwardDr);
            return getCol() + forwardDr == newCol && parent.pieceAt(newRow, newCol) == null;
        }
        else if (getRow() + 1 == newRow || getRow() - 1 == newRow) {
            return getCol() + forwardDr == newCol
                    && parent.pieceAt(newRow, newCol) != null
                    && parent.pieceAt(newRow, newCol).getColor() != getColor();
        }

        return false;
    }
}

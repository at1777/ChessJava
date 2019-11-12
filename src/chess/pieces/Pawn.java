package chess.pieces;

import chess.ChessBoard;
import chess.ChessColor;
import server.PawnInterrupt;

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
                return getParent().pieceAt(newRow, getCol() + forwardDr) == null
                        && getParent().pieceAt(newRow, newCol) == null;
            return getCol() + forwardDr == newCol && getParent().pieceAt(newRow, newCol) == null;
        }
        else if (getRow() + 1 == newRow || getRow() - 1 == newRow) {
            return getCol() + forwardDr == newCol
                    && getParent().pieceAt(newRow, newCol) != null
                    && getParent().pieceAt(newRow, newCol).getColor() != getColor();
        }

        return false;
    }

    @Override
    public void move(int newRow, int newCol) throws PawnInterrupt {
        super.move(newRow, newCol);
        if (reachedEnd())
            throw new PawnInterrupt(this);
    }

    private boolean reachedEnd() {
        return getColor() == ChessColor.WHITE ? getCol() == 0 : getCol() == 7;
    }
}

package chess.pieces;

import chess.ChessBoard;
import chess.ChessColor;
import server.ChessException;

public class King extends Piece {

    public King(ChessBoard parent, ChessColor color, int startRow, int startCol) {
        super(parent, color, startRow, startCol);
    }

    @Override
    public boolean checkMove(int newRow, int newCol) {
        if (!super.checkMove(newRow, newCol))
            return false;

        /* Castling */
        if (!getMoved() && !this.getParent().check(getColor())) {
            if (newCol != getCol())
                return false;

            if (newRow == 6) {
                /* King side castling */
                Piece target_castle = getParent().pieceAt( 7, getCol());
                if (target_castle.getMoved())
                    return false;

                return getParent().pieceAt(5, getCol()) == null
                        && getParent().pieceAt(6, getCol()) == null;
            }
            else if (newRow == 2) {
                /* Queen-side castling */
                Piece target_castle = getParent().pieceAt( 0, getCol());
                if (target_castle.getMoved())
                    return false;

                return getParent().pieceAt(1, getCol()) == null
                        && getParent().pieceAt(2, getCol()) == null
                        && getParent().pieceAt(3, getCol()) == null;
            }
        }

        return isAdjacent(newRow, newCol);
    }

    @Override
    public void move(int row, int col) {
        if (!isAdjacent(row, col)) {
            /* We are castling */
            super.move(row, col);

            try {
                if (row == 6)
                    getParent().movePiece(getParent().pieceAt(7, col), 5, col); /* King-side */
                else if (row == 2)
                    getParent().movePiece(getParent().pieceAt(0, col), 3, col);
            } catch (ChessException e) {
                System.exit(1);
            }
        }
        else
            super.move(row, col);
    }
}

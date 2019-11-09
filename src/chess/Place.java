package chess;

import chess.pieces.Piece;

public class Place {
    private ChessColor color;
    private Piece currentPiece;

    Place(int color) {
        if (color == 0)
            this.color = ChessColor.BLACK;
        else
            this.color = ChessColor.WHITE;
        this.currentPiece = null;
    }

    public Piece getPiece() {
        return this.currentPiece;
    }

    public ChessColor getColor() {
        return this.color;
    }

    public Piece setPiece(Piece p) {
        Piece old = this.currentPiece;
        this.currentPiece = p;

        return old;
    }
}

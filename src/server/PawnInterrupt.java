package server;

import chess.pieces.Piece;

public class PawnInterrupt extends Exception {
    private Piece pawn;
    public PawnInterrupt(Piece p) {
        super("Reached end");
        this.pawn = p;
    }

    public Piece getPawn() {
        return pawn;
    }
}

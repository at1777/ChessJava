package chess;

import chess.pieces.*;
import gui.Observer;
import javafx.application.Platform;
import server.ChessException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ChessBoard {
    public enum Status {
        I_WON,
        I_LOST,
        TIE,
        ERROR;

        private String message = null;

        public void setMessage( String msg ) {
            this.message = msg;
        }

        @Override
        public String toString() {
            return '(' + this.message + ')';
        }
    }
    public enum PieceSet {
        PAWN_1,
        PAWN_2,
        PAWN_3,
        PAWN_4,
        PAWN_5,
        PAWN_6,
        PAWN_7,
        PAWN_8,
        CASTLE_1,
        CASTLE_2,
        BISHOP_1,
        BISHOP_2,
        KNIGHT_1,
        KNIGHT_2,
        QUEEN,
        KING
    }

    private Place[][] board;
    private ArrayList<Piece> takenBlack;
    private ArrayList<Piece> takenWhite;
    private boolean myTurn;
    private Status status;
    private boolean inCheck;

    private ArrayList<Piece> white;
    private ArrayList<Piece> black;
    private List<Observer<ChessBoard>> observers = new LinkedList<>();

    public ChessBoard() {
        board = new Place[8][8];
        takenBlack = new ArrayList<>();
        takenWhite = new ArrayList<>();
        white = new ArrayList<>();
        black = new ArrayList<>();
        myTurn = false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = new Place((row + col) % 2);
            }
        }
    }

    private void addPiece(Piece p) {
        board[p.getRow()][p.getCol()].setPiece(p);
        if (p.getColor() == ChessColor.BLACK)
            black.add(p);
        else
            white.add(p);
    }

    private void initColor(ChessColor color) {
        int pawnCol = color == ChessColor.BLACK ? 1 : 6;
        int majorCol = color == ChessColor.BLACK ? 0 : 7;

        /* Add a row of pawns */
        for (int i = 0; i < 8; i++) {
            addPiece(new Pawn(this, color, i, pawnCol));
        }

        /* Add the castles */
        addPiece(new Castle(this, color, 0, majorCol));
        addPiece(new Castle(this, color, 7, majorCol));

        addPiece(new Bishop(this, color, 2, majorCol));
        addPiece(new Bishop(this, color, 5, majorCol));

        addPiece(new Knight(this, color, 1, majorCol));
        addPiece(new Knight(this, color, 6, majorCol));

        addPiece(new Queen(this, color, 3, majorCol));
        addPiece(new King(this, color, 4, majorCol));
    }

    public void initBoard() {
        initColor(ChessColor.BLACK);
        initColor(ChessColor.WHITE);

        notifyObservers();
    }

    public Piece pieceAt(int row, int col) {
        return board[row][col].getPiece();
    }

    private void takePiece(Piece p) {
        if (p.getColor() == ChessColor.WHITE)
            takenWhite.add(p);
        else if (p.getColor() == ChessColor.BLACK)
            takenBlack.add(p);

        p.die();
    }

    public void movePiece(Piece p, int row, int col) throws ChessException {
        Piece toMove = board[p.getRow()][p.getCol()].setPiece(null);
        if (p != toMove)
            throw new ChessException("State of Piece not updated correctly");

        Piece taken = board[row][col].setPiece(toMove);
        if (taken != null) {
            takePiece(taken);
        }

        toMove.move(row, col);
    }

    public Piece getPiece(ChessColor color, PieceSet piece) {
        if (color == ChessColor.BLACK)
            return black.get(piece.ordinal());
        return white.get(piece.ordinal());
    }

    public boolean check(ChessColor c) {
        Piece king = getPiece(c, PieceSet.KING);
        ArrayList<Piece> otherTeam = c == ChessColor.BLACK ? white : black;
        for (Piece opponent : otherTeam) {
            if (opponent instanceof King)
                continue;
            if (!opponent.dead() && opponent.checkMove(king.getRow(), king.getCol()))
                return true;
        }

        return false;
    }

    public boolean gameOver() {
        return getPiece(ChessColor.BLACK, PieceSet.KING).dead() || getPiece(ChessColor.WHITE, PieceSet.KING).dead();
    }

    public ChessColor getWinner() {
        if (getPiece(ChessColor.BLACK, PieceSet.KING).dead())
            return ChessColor.WHITE;
        else if (getPiece(ChessColor.WHITE, PieceSet.KING).dead())
            return ChessColor.BLACK;
        return ChessColor.NONE;
    }

    /* View to Model */
    public void addObserver(Observer<ChessBoard> observer){
        observers.add(observer);
    }

    private void notifyObservers(){
        for (Observer<ChessBoard> observer: observers) {
            Platform.runLater(() -> observer.update( this ));
        }
    }

    public void moveMade(int startRow, int startCol, int row, int col) throws ChessException {
        Piece p = pieceAt(startRow, startCol);
        if (p == null)
            throw new ChessException(String.format("Piece at %d,%d", startRow, startCol));

        movePiece(p, row, col);
        myTurn = !myTurn;
        notifyObservers();
    }

    public void makeMove() {
        myTurn = true;
        notifyObservers();
    }

    /**
     * Can the local user make changes to the board?
     * @return true if the server has told this player it is its time to move
     */
    public boolean isMyTurn() {
        return this.myTurn;
    }

    /**
     * Called when the game has been won by this player.
     */
    public void gameWon() {
        this.status = Status.I_WON;
        notifyObservers();
    }

    /**
     * Called when the game has been won by the other player.
     */
    public void gameLost() {
        this.status = Status.I_LOST;
        notifyObservers();
    }

    /**
     * Called when the game has been tied.
     */
    public void gameTied() {
        this.status = Status.TIE;
        notifyObservers();
    }

    /**
     * Called when an error is sent from the server.
     *
     * @param arguments The error message sent from the server.
     */
    public void error( String arguments ) {
        this.status = Status.ERROR;
        this.status.setMessage( arguments );
        notifyObservers();
    }

    /**
     * Tell user s/he may close at any time.
     */
    public void close() {
        // Tell user s/he may close at any time?
        // Currently it will say win/lose/tie/error.
        notifyObservers();
    }

    public Place get(int row, int col) {
        return board[row][col];
    }

    public Status getStatus() {
        return status;
    }

    public void start() {
        this.myTurn = false;
        notifyObservers();
    }
}

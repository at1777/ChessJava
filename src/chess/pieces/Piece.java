package chess.pieces;

import chess.ChessBoard;
import chess.ChessColor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import server.PawnInterrupt;

public abstract class Piece {
    /* Package private */
    private ChessBoard parent;

    /* Private */
    private int row;
    private int col;
    private ChessColor color;
    private boolean isDead;
    private ImageView image;
    private boolean moved;

    public Piece(ChessBoard parent, ChessColor color, int startRow, int startCol) {
        this.parent = parent;
        this.color = color;
        this.row = startRow;
        this.col = startCol;
        this.isDead = false;
        this.moved = false;

        String imageName = String.format("%s_%s.png",
                getClass().getSimpleName().toLowerCase(),
                color.toString().toLowerCase());
        this.image = new ImageView(new Image(getClass().getResourceAsStream(imageName)));
    }

    public ImageView getImage() {
        return this.image;
    }

    public void die() {
        isDead = true;
    }

    public boolean dead() {
        return isDead;
    }

    public boolean checkMove(int newRow, int newCol) {
        Piece target_piece = parent.pieceAt(newRow, newCol);
        if (target_piece != null)
            return target_piece.getColor() != this.getColor();
        return true;
    }

    public ChessColor getColor() {
        return this.color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    boolean isAdjacent(int row, int col) {
        if (this.row == row && this.col == col)
            return false;

        boolean rowTrue = false;
        boolean colTrue = false;
        if (this.row - row == 1 || this.row - row == -1 || this.row == row)
            rowTrue = true;
        if (this.col - col == 1 || this.col - col == -1 || this.col == col)
            colTrue = true;

        return rowTrue && colTrue;
    }

    private boolean checkPiece(int row, int col, int dr, int dc) {
        int r = getRow() + dr;
        int c = getCol() + dc;
        for (; r < 8 && r >= 0 && c < 8 && c >= 0; r += dr, c += dc) {
            Piece p = this.parent.pieceAt(r, c);
            if (r == row && c == col) {
                if (p == null || p.getColor() != getColor())
                    return true;
            }

            if (p != null)
                return false;
        }

        return false;
    }

    boolean isForwardSide(int row, int col) {
        int[][] delta = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};

        for (int[] dyx : delta) {
            if (checkPiece(row, col, dyx[0], dyx[1]))
                return true;
        }
        return false;
    }

    boolean isDiagonal(int row, int col) {
        int[][] delta = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

        for (int[] dyx : delta) {
            if (checkPiece(row, col, dyx[0], dyx[1]))
                return true;
        }
        return false;
    }

    public void move(int row, int col) throws PawnInterrupt {
        this.row = row;
        this.col = col;
        this.moved = true;
    }

    boolean getMoved() {
        return this.moved;
    }

    ChessBoard getParent() {
        return this.parent;
    }

    public String getName() {
        return getClass().getSimpleName().toUpperCase();
    }

    public static Piece createPiece(ChessBoard parent, ChessColor color, String name, int row, int col) {
        Piece p = null;
        switch (name) {
            case "QUEEN":
                p = new Queen(parent, color, row, col);
                break;
            case "BISHOP":
                p = new Bishop(parent, color, row, col);
                break;
            case "KNIGHT":
                p = new Knight(parent, color, row, col);
                break;
            case "CASTLE":
                p = new Castle(parent, color, row, col);
                break;
            default:
                System.err.printf("Cannot create piece %s\n", name);
                System.exit(1);
        }

        return p;
    }
}

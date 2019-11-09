package gui;

import javafx.scene.control.Button;

public class ChessButton extends Button {
    private int row;
    private int col;

    public ChessButton (int row, int col) {
        super();

        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
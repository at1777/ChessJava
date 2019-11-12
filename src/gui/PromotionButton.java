package gui;

import chess.pieces.Piece;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;

class PromotionButton extends Button {
    private Piece targetPiece;
    private Main main;

    PromotionButton(Main main, Piece piece) {
        super();
        targetPiece = piece;
        this.main = main;

        setGraphic(targetPiece.getImage());
        setMinSize(70, 70);
        setMaxSize(70, 70);
        setOnAction(PromotionButton::handle);
    }

    private Piece getTargetPiece() {
        return targetPiece;
    }

    private Main getMain() {
        return main;
    }

    private static void handle(ActionEvent e) {
        PromotionButton target = (PromotionButton)e.getSource();
        target.getMain().getModel().chosePiece(target.getTargetPiece());
        target.getMain().getClient().chose(target.getTargetPiece().getName());
    }
}

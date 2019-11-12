package gui;

import chess.ChessBoard;
import chess.ChessColor;
import chess.Place;
import chess.pieces.*;
import javafx.application.Application;

import java.util.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Stage;
import server.ChessException;

/**
 * This application is the UI for Chess.
 *
 * @author Andrei Tumbar
 */
public class Main extends Application implements Observer<ChessBoard> {

    /**
     * Connection to network interface to server
     */
    private ChessClient serverConn;
    private ChessBoard model;

    private Label turnLabel;
    private Label checkLabel;
    private Stage mainStage;
    private Popup choosePieceWindow;

    private Background black = new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY));
    private Background white = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
    private Background green = new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY));

    private ChessButton[][] chessButtons;
    private Piece currentSelect;

    /**
     * Create the board model, create the network connection based on
     * command line parameters, and use the first message received to
     * allocate the board size the server is also using.
     */
    public void init() {
        // Get host info from command line
        List<String> args = getParameters().getRaw();

        // get host info and username from command line
        String host = args.get(0);
        int port = Integer.parseInt(args.get(1));

        model = new ChessBoard();

        try {
            serverConn = new ChessClient(host, port, model);
        } catch (ChessException e) {
            e.printStackTrace();
            System.err.println("Failed to initialize connection or board");
            System.exit(1);
        }
    }

    public void start( Stage __mainStage ) {
        VBox topBox = new VBox();
        HBox labelBox = new HBox();
        GridPane chessGrid = new GridPane();

        this.mainStage = __mainStage;
        choosePieceWindow = null;

        chessButtons = new ChessButton[8][8];
        model.initBoard();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                ChessButton but = new ChessButton(row, col);
                but.setOnAction(this::handleButton);
                but.setMinSize(70, 70);
                but.setMaxSize(70, 70);
                chessGrid.add(but, row, col);
                chessButtons[row][col] = but;

                setGraphic(model, but);
            }
        }

        turnLabel = new Label("Waiting for player connection");
        turnLabel.setFont(new Font("Arial", 30));

        checkLabel = new Label("");
        checkLabel.setFont(new Font("Arial", 30));

        labelBox.getChildren().addAll(turnLabel, checkLabel);

        labelBox.setSpacing(25);

        topBox.getChildren().add(chessGrid);
        topBox.getChildren().add(labelBox);
        Scene mainScene = new Scene(topBox);
        mainStage.setScene(mainScene);


        mainStage.setOnCloseRequest(t -> {
            Platform.exit();
            System.exit(0);
        });

        mainStage.show();

        // start the network listener as the last thing
        this.serverConn.startListener();
        this.model.addObserver(this);
    }

    public ChessColor getColor() {
        return serverConn.getPlayerColor();
    }
    
    private synchronized void handleButton(ActionEvent e) {
        ChessButton parentButton = (ChessButton)e.getSource();
        if (currentSelect != null
                && parentButton.getCol() == currentSelect.getCol()
                && parentButton.getRow() == currentSelect.getRow()) {
            // Disable this move (make a different one)
            for (int row = 0; row < 8; row++)
                for (int col = 0; col < 8; col++)
                    setGraphic(this.model, chessButtons[row][col]);
            currentSelect = null;
        }
        else if (currentSelect != null) {
            // No longer your turn (you made a move)
            disableBoard();
            this.serverConn.sendMove(
                    currentSelect.getRow(), currentSelect.getCol(), // Piece to move
                    parentButton.getRow(), parentButton.getCol());
            currentSelect = null;
        }
        else {
            currentSelect = model.pieceAt(parentButton.getRow(), parentButton.getCol());
            showMoves();
        }
    }

    ChessBoard getModel() {
        return model;
    }

    ChessClient getClient() {
        return serverConn;
    }

    /**
     * Launch the JavaFX GUI.
     *
     * @param args not used, here, but named arguments are passed to the GUI.
     *             <code>--host=<i>hostname</i> --port=<i>portnum</i></code>
     */
    public static void main( String[] args ) {
        if (args.length != 2) {
            System.out.println("Usage: java GUI_Client2 host port");
            System.exit(0);
        } else {
            Application.launch(args);
        }
    }

    private void disableBoard() {
        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                chessButtons[row][col].setDisable(true);
    }

    private void setGraphic(ChessBoard model, ChessButton b) {
        Place m = model.get(b.getRow(), b.getCol());

        b.setBackground(m.getColor() == ChessColor.BLACK ? black : white);
        Piece p = m.getPiece();
        if (p != null) {
            b.setGraphic(p.getImage());
            if (p.getColor() == getColor() && model.isMyTurn())
                b.setDisable(false);
            else
                b.setDisable(true);
        }
        else {
            b.setGraphic(null);
            b.setDisable(true);
        }
    }

    private void showMoves() {
        Piece p = this.model.pieceAt(currentSelect.getRow(), currentSelect.getCol());
        if (p == null)
            return;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                if (p.checkMove(row, col)) {
                    chessButtons[row][col].setDisable(false);
                    chessButtons[row][col].setBackground(green);
                }
                else
                    chessButtons[row][col].setDisable(true);
            }
        }

        /* Allow this move to be turned off */
        chessButtons[currentSelect.getRow()][currentSelect.getCol()].setDisable(false);
    }

    private Popup choosePiece(int row, int col) {
        final Popup popup = new Popup();
        popup.setX(350);
        popup.setY(260);

        HBox chooseBox = new HBox();

        Button castleButton = new PromotionButton(this, new Castle(model, getColor(), row, col));
        Button bishopButton = new PromotionButton(this, new Bishop(model, getColor(), row, col));
        Button knightButton = new PromotionButton(this, new Knight(model, getColor(), row, col));
        Button queenButton = new PromotionButton(this, new Queen(model, getColor(), row, col));

        chooseBox.getChildren().addAll(castleButton, bishopButton, knightButton, queenButton);

        VBox top = new VBox();
        top.getChildren().addAll(new Label("Choose a piece"), chooseBox);

        popup.getContent().add(top);

        popup.show(this.mainStage);

        return popup;
    }

    @Override
    public synchronized void update(ChessBoard board) {
        if (model.isMyTurn())
            this.turnLabel.setText("Your turn");
        else
            this.turnLabel.setText("Opponents turn");

        if (model.getStatus() == ChessBoard.Status.I_WON)
            this.turnLabel.setText("You won, Yay!");
        else if (model.getStatus() == ChessBoard.Status.TIE)
            this.turnLabel.setText("You tied, Meh.");
        else if (model.getStatus() == ChessBoard.Status.I_LOST)
            this.turnLabel.setText("You lost, Boo!");
        else if (model.getStatus() == ChessBoard.Status.ERROR)
            this.turnLabel.setText("Opponent disconnected, they suck");

        if (model.check(getColor()))
            this.checkLabel.setText("Check!!");
        else
            this.checkLabel.setText("");

        if (this.model.awaitingPromotion())
            choosePieceWindow = choosePiece(model.awaiting().getRow(), model.awaiting().getRow());
        else if (choosePieceWindow != null) {
            choosePieceWindow.hide();
            choosePieceWindow = null;
        }

        for (int row = 0; row < 8; row++)
            for (int col = 0; col < 8; col++)
                setGraphic(this.model, chessButtons[row][col]);
    }

}
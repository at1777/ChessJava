package gui;

import chess.ChessBoard;
import chess.ChessColor;
import chess.Place;
import chess.pieces.Piece;
import javafx.application.Application;

import java.util.*;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
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

    private Background black = new Background(new BackgroundFill(Color.GRAY, CornerRadii.EMPTY, Insets.EMPTY));
    private Background white = new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY));
    private Background green = new Background(new BackgroundFill(Color.LIGHTGREEN, CornerRadii.EMPTY, Insets.EMPTY));

    private ChessButton[][] chessButtons;

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

    public void start( Stage mainStage ) {
        VBox topBox = new VBox();
        GridPane chessGrid = new GridPane();

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

        topBox.getChildren().add(chessGrid);
        topBox.getChildren().add(turnLabel);
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
        update(this.model);
    }

    private boolean inMove = false;
    private int moveRow;
    private int moveCol;

    public void handleButton(ActionEvent e) {
        ChessButton parentButton = (ChessButton)e.getSource();
        if (inMove && parentButton.getCol() == moveCol && parentButton.getRow() == moveRow) {
            inMove = false;
            update(this.model);
            return;
        }

        if (inMove) {
            inMove = false;
            disableBoard();
            this.serverConn.sendMove(moveRow, moveCol, parentButton.getRow(), parentButton.getCol());
            return;
        }

        moveRow = parentButton.getRow();
        moveCol = parentButton.getCol();
        inMove = true;
        update(this.model);
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
            if (p.getColor() == serverConn.getPlayerColor() && model.isMyTurn())
                b.setDisable(false);
            else
                b.setDisable(true);
        }
        else {
            b.setGraphic(null);
            b.setDisable(true);
        }
    }

    @Override
    public void update(ChessBoard board) {
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


        if (!inMove) {
            for (int row = 0; row < 8; row++) {
                for (int col = 0; col < 8; col++) {
                    setGraphic(board, chessButtons[row][col]);
                }
            }
        }

        if (!model.isMyTurn()) {
            disableBoard();
            return;
        }

        if (inMove) {
            Piece p = board.pieceAt(moveRow, moveCol);
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
            chessButtons[moveRow][moveCol].setDisable(false);
        }
    }

}
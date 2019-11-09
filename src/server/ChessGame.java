package server;

import chess.ChessBoard;
import chess.ChessColor;
import chess.pieces.Piece;
import server.ChessConnection;

/**
 * Handles all the game operations given two connection
 * Runs in separate thread from main
 *
 * @author Andrei Tumbar
 */
public class ChessGame extends Thread {
    private ChessConnection[] clients;
    private ChessBoard board;
    private boolean error;

    /**
     * Create a new Game given two connections and board dimensions
     *
     * @param player_one first player
     * @param player_two second player
     */
    public ChessGame(ChessConnection player_one, ChessConnection player_two) {
        this.clients = new ChessConnection[2];
        this.error = false;

        this.clients[0] = player_one;
        this.clients[1] = player_two;
        this.board = new ChessBoard();
        this.board.initBoard();
    }

    /**
     * Run the reversi game
     */
    public void run() {
        System.out.println("Starting game!");
        this.clients[0].startgame();
        this.clients[1].startgame();

        this.clients[0].setError(this::error);
        this.clients[1].setError(this::error);

        for (int moveNum = 0; !this.board.gameOver(); moveNum = (moveNum + 1) % 2) {
            ChessConnection player = this.clients[moveNum];

            player.make_move();

            String[] move;
            try {
                move = player.parseCommand();
            }
            catch (NullPointerException e) {
                System.err.println("Could not read response from client");
                error();
                break;
            }

            if (!move[0].equals(ChessConnection.MOVE)) {
                if (error)
                    break;

                System.err.printf("Invalid Command from client: %s\n", move[0]);
                error();
                break;
            }

            int startRow = Integer.parseInt(move[1]);
            int startCol = Integer.parseInt(move[2]);
            int row = Integer.parseInt(move[3]);
            int col = Integer.parseInt(move[4]);

            try {
                Piece p = this.board.pieceAt(startRow, startCol);
                this.board.movePiece(p, row, col);
            } catch (Exception e) {
                e.printStackTrace();
                error();
                break;
            }

            /* No error */
            this.clients[0].move_made(startRow, startCol, row, col);
            this.clients[1].move_made(startRow, startCol, row, col);
        }

        if (!error) {
            /* Tell the client if they won or not */
            if (this.board.getWinner() == ChessColor.BLACK) {
                this.clients[0].game_won();
                this.clients[1].game_lost();
            } else if (this.board.getWinner() == ChessColor.WHITE) {
                this.clients[1].game_won();
                this.clients[0].game_lost();
            } else {
                this.clients[0].game_tied();
                this.clients[1].game_tied();
            }
        }

        /* parseCommand on each thread should exit because socket closed */
        this.clients[0].close();
        this.clients[1].close();
    }

    /**
     * Exit both clients with error status
     */
    private void error() {
        this.error = true;
        this.clients[0].error();
        this.clients[1].error();
    }
}
package server;

import chess.ChessColor;
import chess.pieces.Piece;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Wraps the Socket in a reader and writer.
 * Can send and parse ChessProtocol
 *
 * @author Andrei Tumbar
 */
public class ChessConnection implements ChessProtocol {
    private Socket clientSocket;
    private PrintWriter clientOut;
    private BufferedReader clientIn;
    private Runnable runOnError;
    private ChessColor color;

    /**
     * Create a new connection, wrap socket for reading and writing
     * @param clientSocket socket to wrap
     */
    ChessConnection(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.runOnError = null;
        color = null;

        try {
            clientIn = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            clientOut =
                    new PrintWriter(clientSocket.getOutputStream(), true);
        } catch (IOException e) {
            handle_error(e, "Failed to wrap client stream");
        }
    }

    /**
     * Tell handle error what to call to close the other connection
     * @param error function to run
     */
    void setError(Runnable error) {
        this.runOnError = error;
    }

    ChessColor getColor() {
        return color;
    }

    /**
     * Read a command from the client socket
     * @return entire command line as a string
     */
    private String readCommand() {
        try {
            String readCommand = this.clientIn.readLine();
            System.out.println(readCommand);
            return readCommand;
        } catch (IOException e) {
            handle_error(e, "Failed to read from client");
        }

        return "";
    }

    /**
     * Write a command to the socket
     * @param fmt string format
     * @param args string format arguments
     */
    private void writeCommand(String fmt, Object ... args) {
        this.clientOut.println(String.format(fmt, args));
    }

    void chose(Piece p) {
        writeCommand("%s %s %s %d %d", CHOSE, p.getName(), p.getColor().name(), p.getRow(), p.getCol());
    }

    /**
     * Close this socket
     */
    void close() {
        try {
            this.clientSocket.close();
        } catch (IOException e) {
            System.err.println("Close client message");
            e.getMessage();
        }
    }

    /**
     * Handle IOException, print the error and exit
     * @param e Exception that occurred
     * @param message message to print out
     */
    private void handle_error(Exception e, String message) {
        this.close();
        System.err.println(message);
        e.printStackTrace();

        if (runOnError != null)
            runOnError.run();
    }

    /**
     * Parse a command by splitting it by spaces
     * @return list of space delimited tokens
     */
    String[] parseCommand() {
        return this.readCommand().replace("\n", "").split(" ");
    }

    void choose(int row, int col) {
        this.writeCommand("%s %d %d", CHOOSE, row, col);
    }

    void startgame() {this.writeCommand(STARTGAME);}

    /**
     * Send the color info to the client
     */
    void connect(ChessColor color) {
        this.color = color;
        this.writeCommand("%s %s", CONNECT, color.name());
    }

    /**
     * Tell the client there was an error
     */
    void error() {
        this.writeCommand(ERROR);
    }

    /**
     * Tell the client to make a move
     */
    void make_move() {
        this.writeCommand(MAKE_MOVE);
    }

    /**
     * Tell the client the move that was made
     * @param row row that move was made
     * @param col col that move was made
     */
    void move_made(int startRow, int startCol, int row, int col) {
        this.writeCommand("%s %d %d %d %d", MOVE_MADE, startRow, startCol, row, col);
    }

    /**
     * Tell the client that they lost the game
     */
    void game_lost() {
        this.writeCommand(GAME_LOST);
    }

    /**
     * Tell the client that they won the game
     */
    void game_won() {
        this.writeCommand(GAME_WON);
    }

    /**
     * Tell the client that they tied the game
     */
    void game_tied() {
        this.writeCommand(GAME_TIED);
    }

    /**
     * Tell the server what move to make
     * @param row row on which to make move
     * @param col col on which to make move
     */
    public void move(int startRow, int startCol, int row, int col ) {
        this.writeCommand("%s %d %d", MOVE, startRow, startCol, row, col);
    }

    /**
     * Print the sockets toString
     * @return sockets toString
     */
    @Override
    public String toString() {
        return this.clientSocket.toString();
    }
}
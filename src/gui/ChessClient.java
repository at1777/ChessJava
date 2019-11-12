package gui;


import chess.ChessBoard;
import chess.ChessColor;
import chess.pieces.*;
import server.ChessException;
import server.ChessProtocol;
import server.PawnInterrupt;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

/**
 * The client side network interface to a Reversi game server.
 * Each of the two players in a game gets its own connection to the server.
 * This class represents the controller part of a model-view-controller
 * triumvirate, in that part of its purpose is to forward user actions
 * to the remote server.
 *
 * @author Robert St Jacques @ RIT SE
 * @author Sean Strout @ RIT CS
 * @author James Heliotis @ RIT CS
 */
public class ChessClient implements Runnable, ChessProtocol {

    /**
     * Turn on if standard output debug messages are desired.
     */
    private static final boolean DEBUG = false;

    private ChessColor playerColor;

    /**
     * Print method that does something only if DEBUG is true
     *
     * @param logMsg the message to log
     */
    private static void dPrint( Object logMsg ) {
        if ( ChessClient.DEBUG ) {
            System.out.println( logMsg );
        }
    }

    /**
     * The {@link Socket} used to communicate with the reversi server.
     */
    private Socket sock;

    /**
     * The {@link Scanner} used to read requests from the reversi server.
     */
    private Scanner networkIn;

    /**
     * The {@link PrintStream} used to write responses to the reversi server.
     */
    private PrintStream networkOut;

    /**
     * The {@link ChessBoard} used to keep track of the state of the game.
     */
    private ChessBoard game;

    /**
     * Sentinel used to control the main game loop.
     */
    private boolean go;

    /**
     * Accessor that takes multithreaded access into account
     *
     * @return whether it ok to continue or not
     */
    private synchronized boolean goodToGo() {
        return this.go;
    }

    /**
     * Multithread-safe mutator
     */
    private synchronized void stop() {
        this.go = false;
    }

    /**
     * Hook up with a Reversi game server already running and waiting for
     * two players to connect. Because of the nature of the server
     * protocol, this constructor actually blocks waiting for the first
     * message from the server that tells it how big the board will be.
     * Afterwards a thread that listens for server messages and forwards
     * them to the game object is started.
     *
     * @param hostname the name of the host running the server program
     * @param port     the port of the server socket on which the server is
     *                 listening
     * @param model    the local object holding the state of the game that
     *                 must be updated upon receiving server messages
     * @throws ChessException If there is a problem opening the connection
     */
    ChessClient( String hostname, int port, ChessBoard model )
            throws ChessException {
        try {
            this.sock = new Socket( hostname, port );
            this.networkIn = new Scanner( sock.getInputStream() );
            this.networkOut = new PrintStream( sock.getOutputStream() );
            this.game = model;
            this.go = true;

            // Block waiting for the CONNECT message from the server.
            String request = this.networkIn.next();
            String arguments = this.networkIn.nextLine();
            assert request.equals( ChessProtocol.CONNECT ) :
                    "CONNECT not 1st";
            ChessClient.dPrint( "Connected to server " + this.sock );
            this.connect( arguments );
        }
        catch( IOException e ) {
            throw new ChessException( e );
        }
    }

    void startListener() {
        // Run rest of client in separate thread.
        // This threads stops on its own at the end of the game and
        // does not need to rendezvous with other software components.
        Thread netThread = new Thread( this );
        netThread.start();
    }

    ChessColor getPlayerColor() {
        return playerColor;
    }


    /**
     * Called by the constructor to set up the game board for this player now
     * that the server has sent the board dimensions with the
     * {@link ChessProtocol#CONNECT} request.
     *
     * @param arguments string from the server's message that
     *                  contains the square dimension of the board
     */
    private void connect( String arguments ) {
        playerColor = arguments.trim().equals("WHITE") ? ChessColor.WHITE : ChessColor.BLACK;

        // Get the board state set up.
        this.game.initBoard(); // may throw exception
    }

    /**
     * Tell the local user to choose a move. How this is communicated to
     * the user is up to the View (UI).
     */
    private void makeMove() {
        this.game.makeMove();
    }

    /**
     * A move has been made by one of the players
     *
     * @param arguments string from the server's message that
     *                  contains the row, then column where the
     *                  player made the move
     */
    private void moveMade( String arguments ) {
        ChessClient.dPrint( '!' + MOVE_MADE + ',' + arguments );

        String[] fields = arguments.trim().split( " " );
        int startRow = parseInt( fields[ 0 ] );
        int startCol = parseInt( fields[ 1 ] );
        int row = parseInt( fields[ 2 ] );
        int column = parseInt( fields[ 3 ] );

        // Update the board model.
        try {
            this.game.moveMade(startRow, startCol, row, column);
        }
        catch (PawnInterrupt p) {
            if (p.getPawn().getColor() == getPlayerColor())
                this.game.choosePiece(this.game.pieceAt(row, column));
        }
        catch (ChessException e) {
            e.printStackTrace();
            error("Chess Exception");
        }
    }

    /**
     * Called when the server sends a message saying that the
     * game has been won by this player. Ends the game.
     */
    private void gameWon() {
        ChessClient.dPrint( '!' + GAME_WON );

        dPrint( "You won! Yay!" );
        this.game.gameWon();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game has been won by the other player. Ends the game.
     */
    private void gameLost() {
        ChessClient.dPrint( '!' + GAME_LOST );
        dPrint( "You lost! Boo!" );
        this.game.gameLost();
        this.stop();
    }

    /**
     * Called when the server sends a message saying that the
     * game is a tie. Ends the game.
     */
    private void gameTied() {
        ChessClient.dPrint( '!' + GAME_TIED );
        dPrint( "You tied! Meh!" );
        this.game.gameTied();
        this.stop();
    }

    private void choose(String arguments) {
        String[] args = arguments.split(" ");
        this.game.choosePiece(game.pieceAt(parseInt(args[0]), parseInt(args[1])));
    }

    void sendChose(Piece p) {
        System.out.printf("%s %s %s %d %d\n", CHOSE, p.getName(), p.getColor().name(), p.getRow(), p.getCol());
        this.networkOut.printf("%s %s %s %d %d\n", CHOSE, p.getName(), p.getColor().name(), p.getRow(), p.getCol());
    }

    private void chose(String arguments) {
        String[] args = arguments.split(" ");
        Piece p = Piece.createPiece(
                game,
                ChessColor.valueOf(args[2]),
                args[1],
                parseInt(args[3]),
                parseInt(args[4]));

        this.game.chosePiece(p);
    }

    /**
     * Called when the server sends a message saying that
     * gameplay is damaged. Ends the game.
     *
     * @param arguments The error message sent from the reversi.server.
     */
    private void error( String arguments ) {
        ChessClient.dPrint( '!' + ERROR + ',' + arguments );
        dPrint( "Fatal error: " + arguments );
        this.game.error( arguments );
        this.stop();
    }

    /**
     * This method should be called at the end of the game to
     * close the client connection.
     */
    private void close() {
        try {
            this.sock.close();
        }
        catch( IOException ioe ) {
            // squash
        }
        this.game.close();
    }

    /**
     * UI wants to send a new move to the server.
     *
     * @param row the row
     * @param col the column
     */
    void sendMove( int startRow, int startCol, int row, int col ) {
        this.networkOut.printf("%s %d %d %d %d\n", MOVE, startRow, startCol, row, col);
    }

    /**
     * Run the main client loop. Intended to be started as a separate
     * thread internally. This method is made private so that no one
     * outside will call it or try to start a thread on it.
     */
    public void run() {
        String startgame = this.networkIn.nextLine();
        if (startgame.equals(STARTGAME))
            game.start();

        while ( this.goodToGo() ) {
            try {
                String request = this.networkIn.next();
                String arguments = this.networkIn.nextLine().trim();
                ChessClient.dPrint( "Net message in = \"" + request + '"' );

                switch ( request ) {
                    case CONNECT:
                        // This should not happen because ChessClient
                        // waits for the CONNECT message in the constructor.
                        assert false : "CONNECT already happened?";
                        connect( arguments );
                        break;
                    case MAKE_MOVE:
                        makeMove();
                        break;
                    case CHOOSE:
                        choose(arguments);
                        break;
                    case CHOSE:
                        chose(arguments);
                        break;
                    case MOVE_MADE:
                        moveMade( arguments );
                        break;
                    case GAME_WON:
                        gameWon();
                        break;
                    case GAME_LOST:
                        gameLost();
                        break;
                    case GAME_TIED:
                        gameTied();
                        break;
                    case ERROR:
                        error( arguments );
                        break;
                    default:
                        System.err
                                .println( "Unrecognized request: " + request );
                        this.stop();
                        break;
                }
            }
            catch( NoSuchElementException nse ) {
                // Looks like the connection shut down.
                this.error( "Lost connection to server." );
                this.stop();
            }
            catch( Exception e ) {
                this.error( e.getMessage() + '?' );
                this.stop();
            }
        }
        this.close();
    }

}
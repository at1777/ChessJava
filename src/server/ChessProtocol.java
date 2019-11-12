package server;

/**
 * The {@link ChessProtocol} interface provides constants for all of the
 * messages that are communicated between the reversi.server and the
 * reversi.client's.
 *
 * @author Andrei Tumbar
 */
public interface ChessProtocol {
    /**
     * Request sent from the reversi.server to the client after the client initially
     * opens a {@link java.net.Socket} connection to the reversi.server. This is the
     * first part of the handshake used to establish that the client
     * understands the {@link ChessProtocol protocol}.  The square dimensions
     * of the board are sent in the request.<P>
     *
     *  For example for a 6x6 board: CONNECT BLACK\n
     */
    String CONNECT = "CONNECT";

    String STARTGAME = "STARTGAME";

    /* CHOOSE row col */
    String CHOOSE = "CHOOSE";

    /* CHOSE QUEEN/BISHOP/KNIGHT/CASTLE */
    String CHOSE = "CHOSE";

    /**
     * Request sent from the reversi.server to the client when it is the client's turn
     * to make a move.
     */
    String MAKE_MOVE = "MAKE_MOVE";

    /**
     * Response sent from the client to the reversi.server in response to a
     * {@link #MAKE_MOVE} request. The response should include the row and column
     * number into which the player would like to move.<P>
     *
     * For example (to move in (3,2)): MOVE 2 2 3 2\n
     */
    String MOVE = "MOVE";

    /**
     * Request sent from the reversi.server to the client when either player has moved.
     * The request will include the row and column in which the player moved.<P>
     *
     * For example (if a move was made in (3,2)): MOVE_MADE 2 2 3 2\n
     */
    String MOVE_MADE = "MOVE_MADE";

    /**
     * Request sent from the reversi.server to the client when the client has won the
     * game.
     */
    String GAME_WON = "GAME_WON";

    /**
     * Request sent from the reversi.server to the client when the client has lost the
     * game.
     */
    String GAME_LOST = "GAME_LOST";

    /**
     * Request sent from the reversi.server to the client when the client has tied the
     * game.
     */
    String GAME_TIED = "GAME_TIED";

    /**
     * Request sent from the reversi.server to the client when any kind of error has
     * resulted from a bad client response. No response is expected from the
     * client and the connection is terminated (as is the game).
     */
    String ERROR = "ERROR";
}
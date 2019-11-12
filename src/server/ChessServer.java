package server;
import chess.ChessBoard;
import chess.ChessColor;

import java.util.LinkedList;

/**
 * The ReversiServer waits for incoming client connections and
 * pairs them off to play the game.
 *
 * @author Andrei Tumbar
 */
public class ChessServer {
    /**
     * Starts a new ReversiServer. Simply creates the server and runs.
     *
     * @param args Used to specify the port on which the server should listen
     *             for incoming client connections.
     * @throws ChessException If there is an error starting the server.
     */
    public static void main(String[] args) throws ChessException {
        if (args.length != 1) {
            System.out.println("Usage: java ChessServer port");
            System.exit(1);
        }

        ChessServer server = new ChessServer(Integer.parseInt(args[0]));
    }

    private boolean keepAlive;
    private LinkedList<ChessGame> games;
    private ServerListener listen;

    /**
     * Create a new server with dimensions and port
     * @param port port to listen on
     */
    public ChessServer(int port) {
        this.games = new LinkedList<>();
        this.keepAlive = true;

        /* Start a threaded listener */
        this.listen = new ServerListener(port);
        this.listen.start();

        while (keepAlive) {
            ChessConnection[] clients = new ChessConnection[2];
            for (int clientNum = 0; clientNum < 2; clientNum++) {
                System.out.printf("Waiting for player %s...\n", clientNum == 0 ? "one" : "two");

                /* Waits for accept or returns if someone else connected previously */
                clients[clientNum] = this.listen.getConnection();
                System.out.printf("Player %s connected! %s\n", clientNum == 0 ? "one" : "two", clients[clientNum]);

                clients[clientNum].connect(clientNum == 0 ? ChessColor.BLACK : ChessColor.WHITE);
            }

            ChessGame currentGame = new ChessGame(clients[0], clients[1]);
            this.games.add(currentGame);
            currentGame.start();
        }

        /* Stop listening
         * Won't do anything in normal lab */
        this.kill();
    }

    /**
     * Stop listening on the server port
     */
    public void kill() {
        this.keepAlive = false;
        this.listen.close();

        /* Wait for all the games to finish */
        for (ChessGame g = this.games.poll(); g != null; g = this.games.poll()) {
            try {g.join();}
            catch (InterruptedException ignored) {}
        }
    }
}
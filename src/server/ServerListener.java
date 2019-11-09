package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Listens on port in a separate thread from main
 * Main thread will poll connections from this class
 * When main thread exits, listener is closed and accept stops
 *
 * Written so that a multi-threaded server is closeable
 *
 * @author Andrei Tumbar
 */
public class ServerListener extends Thread {
    private ServerSocket listen;
    private boolean keepAlive;
    private ConcurrentLinkedQueue<ChessConnection> connQueue;

    /**
     * Create a server socket on port
     *
     * @param port port to listen on
     */
    public ServerListener(int port) {
        try {
            this.listen = new ServerSocket(port);
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + port + " or listening for a connection");
            System.out.println(e.getMessage());
            System.exit(1);
        }

        connQueue = new ConcurrentLinkedQueue<>();
        keepAlive = true;
    }

    /**
     * Keep accepting connections until we kill this
     */
    public void run() {
        while (this.keepAlive)
            addConnection(accept());
    }

    /**
     * Add a connection to the queue
     *
     * @param conn connection to add
     */
    private synchronized void addConnection(ChessConnection conn) {
        if (conn != null)
            connQueue.add(conn);

        notifyAll();
    }

    /**
     * Called by the server thread
     * Wait for a notify by acceptConnection
     *
     * @return a connection one someone connection
     */
    public synchronized ChessConnection getConnection() {
        while (keepAlive && connQueue.peek() == null) {
            try {
                wait();
            } catch (InterruptedException ignored) {
            }
        }

        return connQueue.poll();
    }

    /**
     * Accept an incoming connection
     *
     * @return Connection instance that implements the protocol
     */
    private ChessConnection accept() {
        Socket out;
        try {
            out = this.listen.accept();
        } catch (IOException e) {
            /* This socket was closed */
            keepAlive = false;
            return null;
        }

        return new ChessConnection(out);
    }

    /**
     * Stop listening on this port
     */
    public void close() {
        try {
            this.keepAlive = false;
            this.listen.close();
        } catch (IOException e) {
            System.err.println("Failed to close server socket");
        }
    }
}
module ChessJava {
    requires transitive javafx.controls;
    requires kotlin.stdlib;
    exports chess.pieces;
    exports chess;
    exports gui;
    exports server;

}
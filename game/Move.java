package game;

import model.Colors;
import model.Position;
import model.pieces.Piece;

public class Move {
    private final Colors playerColor;
    private final Position from;
    private final Position to;
    private final Piece captured; // poate fi null

    public Move(Colors playerColor, Position from, Position to, Piece captured) {
        this.playerColor = playerColor;
        this.from = from;
        this.to = to;
        this.captured = captured;
    }

    public Colors getPlayerColor() {
        return playerColor;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }

    public Piece getCaptured() {
        return captured;
    }

    @Override
    public String toString() {
        return playerColor + ": " + from + "->" + to;
    }
}

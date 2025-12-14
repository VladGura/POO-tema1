package game;

import exceptions.InvalidMoveException;
import model.ChessPair;
import model.Colors;
import model.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class Player {

    private String name;
    private Colors color;
    private List<Piece> capturedPieces = new ArrayList<>();
    private int points;

    public Player(String name, Colors color) {
        this.name = name;
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Colors getColor() {
        return color;
    }

    public List<Piece> getCapturedPieces() {
        return capturedPieces;
    }

    public int getPoints() {
        return points;
    }

    public void addCapturedPiece(Piece piece) {
        if (piece != null) {
            capturedPieces.add(piece);
            // TODO: calcul punctaj
        }
    }

    public void addPoints(int value) {
        this.points += value;
    }

    public Move makeMove(Board board, Position from, Position to) throws InvalidMoveException {
        // TODO: validare + mutare reala
        // deocamdata doar stub
        return new Move(color, from, to, null);
    }
}

package model.pieces;

import model.Colors;
import model.Position;
import game.Board;

import java.util.List;

public abstract class Piece implements ChessPiece {
    protected Colors color;
    protected Position position;

    public Piece(Colors color, Position position) {
        this.color = color;
        this.position = position;
    }

    @Override
    public Colors getColor() { return color; }

    @Override
    public Position getPosition() { return position; }

    @Override
    public void setPosition(Position position) { this.position = position; }

    @Override
    public abstract List<Position> getPossibleMoves(Board board);

    @Override
    public abstract boolean checkForCheck(Board board, Position kingPosition);

    @Override
    public abstract char type();
}

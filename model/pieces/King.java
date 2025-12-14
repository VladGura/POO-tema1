package model.pieces;

import game.Board;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(model.Colors color, Position position) {
        super(color, position);
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        if (board == null || position == null) return moves;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;

                Position p = new Position((char) (position.getX() + dx), position.getY() + dy);
                if (!board.inBounds(p)) continue;

                Piece on = board.getPieceAt(p);
                if (on == null || on.getColor() != color) {
                    moves.add(p);
                }
            }
        }

        // TODO (optional): castling + verificari de sah
        return moves;
    }

    @Override
    public boolean checkForCheck(Board board, Position kingPosition) {
        return false;
    }

    @Override
    public char type() {
        return 'K';
    }
}

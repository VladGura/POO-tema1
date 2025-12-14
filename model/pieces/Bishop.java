package model.pieces;

import game.Board;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    public Bishop(model.Colors color, Position position) {
        super(color, position);
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        if (board == null || position == null) return moves;

        addRay(board, moves,  1,  1);
        addRay(board, moves,  1, -1);
        addRay(board, moves, -1,  1);
        addRay(board, moves, -1, -1);

        return moves;
    }

    private void addRay(Board board, List<Position> moves, int dx, int dy) {
        char x = position.getX();
        int y = position.getY();

        while (true) {
            x = (char) (x + dx);
            y = y + dy;

            Position p = new Position(x, y);
            if (!board.inBounds(p)) return;

            Piece on = board.getPieceAt(p);
            if (on == null) {
                moves.add(p);
            } else {
                if (on.getColor() != color) moves.add(p);
                return;
            }
        }
    }

    @Override
    public boolean checkForCheck(Board board, Position kingPosition) {
        return false;
    }

    @Override
    public char type() {
        return 'B';
    }
}

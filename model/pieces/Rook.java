package model.pieces;

import model.Colors;
import model.Position;
import game.Board;
import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(Colors color, Position position) {
        super(color, position);
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        if (board == null || position == null) return moves;

        addRay(board, moves,  1,  0); // right
        addRay(board, moves, -1,  0); // left
        addRay(board, moves,  0,  1); // up
        addRay(board, moves,  0, -1); // down

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
                if (on.getColor() != color) moves.add(p); // capture
                return; // stop on first piece
            }
        }
    }

    @Override
    public boolean checkForCheck(Board board, Position kingPosition) {
        return false;
    }

    @Override
    public char type() {
        return 'R';
    }
}

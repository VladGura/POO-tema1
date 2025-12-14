package model.pieces;

import game.Board;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {

    public Knight(model.Colors color, Position position) {
        super(color, position);
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        if (board == null || position == null) return moves;

        int[] dx = { 1, 2, 2, 1, -1, -2, -2, -1 };
        int[] dy = { 2, 1, -1, -2, -2, -1, 1, 2 };

        for (int i = 0; i < 8; i++) {
            Position p = new Position((char) (position.getX() + dx[i]), position.getY() + dy[i]);
            if (!board.inBounds(p)) continue;

            Piece on = board.getPieceAt(p);
            if (on == null || on.getColor() != color) {
                moves.add(p);
            }
        }

        return moves;
    }

    @Override
    public boolean checkForCheck(Board board, Position kingPosition) {
        return false;
    }

    @Override
    public char type() {
        return 'N';
    }
}

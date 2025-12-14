package model.pieces;

import model.Colors;
import model.Position;
import game.Board;
import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(Colors color, Position position) {
        super(color, position);
    }

    @Override
    public List<Position> getPossibleMoves(Board board) {
        List<Position> moves = new ArrayList<>();
        if (board == null || position == null) return moves;

        int dir = (color == Colors.WHITE) ? 1 : -1;
        int startRow = (color == Colors.WHITE) ? 2 : 7;

        // 1 step forward
        Position one = new Position(position.getX(), position.getY() + dir);
        if (board.inBounds(one) && board.getPieceAt(one) == null) {
            moves.add(one);

            // 2 steps forward from start
            Position two = new Position(position.getX(), position.getY() + 2 * dir);
            if (position.getY() == startRow && board.inBounds(two) && board.getPieceAt(two) == null) {
                moves.add(two);
            }
        }

        // captures diagonally
        char x = position.getX();
        int y = position.getY();

        Position diagL = new Position((char) (x - 1), y + dir);
        if (board.inBounds(diagL)) {
            Piece p = board.getPieceAt(diagL);
            if (p != null && p.getColor() != color) moves.add(diagL);
        }

        Position diagR = new Position((char) (x + 1), y + dir);
        if (board.inBounds(diagR)) {
            Piece p = board.getPieceAt(diagR);
            if (p != null && p.getColor() != color) moves.add(diagR);
        }

        return moves;
    }

    @Override
    public boolean checkForCheck(Board board, Position kingPosition) {
        // o facem dupa ce implementam sah-ul
        return false;
    }

    @Override
    public char type() {
        return 'P';
    }
}

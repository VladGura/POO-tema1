
package model.pieces;

import model.Colors;
import model.Position;
import game.Board;
import java.util.List;

public interface ChessPiece {
    Colors getColor();

    Position getPosition();

    void setPosition(Position position);

    List<Position> getPossibleMoves(Board board);

    boolean checkForCheck(Board board, Position kingPosition);

    char type();
}

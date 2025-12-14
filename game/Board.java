package game;

import exceptions.InvalidMoveException;
import model.ChessPair;
import model.Colors;
import model.Position;
import model.pieces.*;

import java.util.*;

public class Board {

    private final Set<ChessPair<Position, Piece>> pieces = new TreeSet<>();

    public Board() { }

    public Set<ChessPair<Position, Piece>> getPieces() { return pieces; }

    public void clear() { pieces.clear(); }

    // ✅ normalizeaza mereu pe A..H
    private Position norm(Position p) {
        if (p == null) return null;
        char x = Character.toUpperCase(p.getX());
        return new Position(x, p.getY());
    }

    public boolean inBounds(Position p) {
        p = norm(p);
        if (p == null) return false;
        char x = p.getX();
        int y = p.getY();
        return x >= 'A' && x <= 'H' && y >= 1 && y <= 8;
    }

    public Piece getPieceAt(Position position) {
        position = norm(position);
        if (position == null) return null;

        for (ChessPair<Position, Piece> pair : pieces) {
            if (pair == null || pair.getKey() == null) continue;
            Position k = norm(pair.getKey());
            if (position.equals(k)) return pair.getValue();
        }
        return null;
    }

    public void addPiece(Piece piece) {
        if (piece == null || piece.getPosition() == null) return;
        // ✅ fortam pozitia piesei pe A..H
        piece.setPosition(norm(piece.getPosition()));
        pieces.add(new ChessPair<>(piece.getPosition(), piece));
    }

    public void removeAt(Position position) {
        position = norm(position);
        if (position == null) return;

        ChessPair<Position, Piece> target = null;
        for (ChessPair<Position, Piece> pair : pieces) {
            if (pair == null || pair.getKey() == null) continue;
            if (position.equals(norm(pair.getKey()))) { target = pair; break; }
        }
        if (target != null) pieces.remove(target);
    }

    public void initialize() {
        clear();

        addPiece(new Rook(Colors.WHITE, new Position('A', 1)));
        addPiece(new Knight(Colors.WHITE, new Position('B', 1)));
        addPiece(new Bishop(Colors.WHITE, new Position('C', 1)));
        addPiece(new Queen(Colors.WHITE, new Position('D', 1)));
        addPiece(new King(Colors.WHITE, new Position('E', 1)));
        addPiece(new Bishop(Colors.WHITE, new Position('F', 1)));
        addPiece(new Knight(Colors.WHITE, new Position('G', 1)));
        addPiece(new Rook(Colors.WHITE, new Position('H', 1)));
        for (char c = 'A'; c <= 'H'; c++) addPiece(new Pawn(Colors.WHITE, new Position(c, 2)));

        addPiece(new Rook(Colors.BLACK, new Position('A', 8)));
        addPiece(new Knight(Colors.BLACK, new Position('B', 8)));
        addPiece(new Bishop(Colors.BLACK, new Position('C', 8)));
        addPiece(new Queen(Colors.BLACK, new Position('D', 8)));
        addPiece(new King(Colors.BLACK, new Position('E', 8)));
        addPiece(new Bishop(Colors.BLACK, new Position('F', 8)));
        addPiece(new Knight(Colors.BLACK, new Position('G', 8)));
        addPiece(new Rook(Colors.BLACK, new Position('H', 8)));
        for (char c = 'A'; c <= 'H'; c++) addPiece(new Pawn(Colors.BLACK, new Position(c, 7)));
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int y = 8; y >= 1; y--) {
            sb.append(y).append(" ");
            for (char x = 'A'; x <= 'H'; x++) {
                Piece p = getPieceAt(new Position(x, y));
                if (p == null) sb.append(".  ");
                else {
                    char t = p.type();
                    char c = (p.getColor() == Colors.WHITE) ? 'W' : 'B';
                    sb.append(t).append("-").append(c).append(" ");
                }
            }
            sb.append("\n");
        }
        sb.append("   A   B   C   D   E   F   G   H\n");
        return sb.toString();
    }

    private Position findKing(Colors color) {
        for (ChessPair<Position, Piece> pair : pieces) {
            Piece piece = (pair != null) ? pair.getValue() : null;
            if (piece instanceof King && piece.getColor() == color) return norm(pair.getKey());
        }
        return null;
    }

    public boolean isInCheck(Colors kingColor) {
        Position kingPos = findKing(kingColor);
        if (kingPos == null) return false;
        Colors attacker = (kingColor == Colors.WHITE) ? Colors.BLACK : Colors.WHITE;
        return isSquareAttacked(kingPos, attacker);
    }

    private boolean isSquareAttacked(Position square, Colors byColor) {
        square = norm(square);
        for (ChessPair<Position, Piece> pair : pieces) {
            Piece p = (pair != null) ? pair.getValue() : null;
            if (p == null || p.getColor() != byColor) continue;

            List<Position> moves = p.getPossibleMoves(this);
            if (moves == null) continue;

            for (Position m : moves) {
                if (m != null && norm(m).equals(square)) return true;
            }
        }
        return false;
    }

    private boolean wouldLeaveKingInCheck(Position from, Position to, Colors currentColor) {
        from = norm(from);
        to = norm(to);

        Piece moving = getPieceAt(from);
        Piece captured = getPieceAt(to);

        removeAt(from);
        if (captured != null) removeAt(to);

        moving.setPosition(to);
        addPiece(moving);

        boolean inCheck = isInCheck(currentColor);

        removeAt(to);
        moving.setPosition(from);
        addPiece(moving);
        if (captured != null) addPiece(captured);

        return inCheck;
    }

    public boolean isValidMove(Position from, Position to, Colors currentColor) {
        from = norm(from);
        to = norm(to);
        if (!inBounds(from) || !inBounds(to)) return false;

        Piece p = getPieceAt(from);
        if (p == null || p.getColor() != currentColor) return false;

        Piece dst = getPieceAt(to);
        if (dst != null && dst.getColor() == currentColor) return false;

        List<Position> moves = p.getPossibleMoves(this);
        if (moves == null) return false;

        boolean ok = false;
        for (Position pp : moves) {
            if (pp != null && norm(pp).equals(to)) { ok = true; break; }
        }
        if (!ok) return false;

        return !wouldLeaveKingInCheck(from, to, currentColor);
    }

    public boolean requiresPromotion(Position from, Position to) {
        from = norm(from);
        to = norm(to);
        Piece p = getPieceAt(from);
        if (!(p instanceof Pawn)) return false;
        if (p.getColor() == Colors.WHITE && to.getY() == 8) return true;
        if (p.getColor() == Colors.BLACK && to.getY() == 1) return true;
        return false;
    }

    public void movePiece(Position from, Position to) throws InvalidMoveException {
        from = norm(from);
        to = norm(to);
        Piece p = getPieceAt(from);
        if (p == null) throw new InvalidMoveException("No piece at " + from);
        movePiece(from, to, p.getColor(), 'Q');
    }

    // compat vechi
    public void movePiece(Position from, Position to, Colors currentColor) throws InvalidMoveException {
        movePiece(from, to, currentColor, 'Q');
    }

    // cu promotion
    public void movePiece(Position from, Position to, Colors currentColor, char promoteTo) throws InvalidMoveException {
        from = norm(from);
        to = norm(to);

        if (!inBounds(from) || !inBounds(to)) throw new InvalidMoveException("Out of board");

        Piece piece = getPieceAt(from);
        if (piece == null) throw new InvalidMoveException("No piece at " + from);
        if (piece.getColor() != currentColor) throw new InvalidMoveException("Not your piece");

        Piece dst = getPieceAt(to);
        if (dst != null && dst.getColor() == currentColor)
            throw new InvalidMoveException("Destination occupied");

        List<Position> moves = piece.getPossibleMoves(this);
        if (moves == null) throw new InvalidMoveException("No moves");

        boolean ok = false;
        for (Position p : moves) {
            if (p != null && norm(p).equals(to)) { ok = true; break; }
        }
        if (!ok) throw new InvalidMoveException("Illegal move");

        if (wouldLeaveKingInCheck(from, to, currentColor))
            throw new InvalidMoveException("Move leaves king in check");

        removeAt(from);
        if (dst != null) removeAt(to);

        piece.setPosition(to);

        if (piece instanceof Pawn && requiresPromotion(from, to)) {
            char t = Character.toUpperCase(promoteTo);
            if (t == 'R') piece = new Rook(currentColor, to);
            else if (t == 'B') piece = new Bishop(currentColor, to);
            else if (t == 'N') piece = new Knight(currentColor, to);
            else piece = new Queen(currentColor, to);
        }

        addPiece(piece);
    }
}

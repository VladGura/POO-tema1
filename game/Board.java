package game;

import exceptions.InvalidMoveException;
import model.ChessPair;
import model.Colors;
import model.Position;
import model.pieces.*;

import java.util.*;

public class Board {

    private final Set<ChessPair<Position, Piece>> pieces = new TreeSet<>();

    public Board() {
        initialize();
    }

    public void clear() {
        pieces.clear();
    }

    public void addPiece(Piece piece) {
        if (piece == null || piece.getPosition() == null) return;
        pieces.removeIf(p -> p.getKey().equals(piece.getPosition()));
        pieces.add(new ChessPair<>(piece.getPosition(), piece));
    }

    public Set<ChessPair<Position, Piece>> getPieces() {
        return pieces;
    }

    public Piece getPieceAt(Position position) {
        for (ChessPair<Position, Piece> pair : pieces) {
            if (pair.getKey().equals(position)) return pair.getValue();
        }
        return null;
    }

    public void removeAt(Position position) {
        if (position == null) return;
        pieces.removeIf(p -> p.getKey().equals(position));
    }

    public boolean inBounds(Position p) {
        if (p == null) return false;
        char x = p.getX();
        int y = p.getY();
        return x >= 'A' && x <= 'H' && y >= 1 && y <= 8;
    }

    public void initialize() {
        clear();

        // WHITE
        addPiece(new Rook(Colors.WHITE, new Position('A', 1)));
        addPiece(new Knight(Colors.WHITE, new Position('B', 1)));
        addPiece(new Bishop(Colors.WHITE, new Position('C', 1)));
        addPiece(new Queen(Colors.WHITE, new Position('D', 1)));
        addPiece(new King(Colors.WHITE, new Position('E', 1)));
        addPiece(new Bishop(Colors.WHITE, new Position('F', 1)));
        addPiece(new Knight(Colors.WHITE, new Position('G', 1)));
        addPiece(new Rook(Colors.WHITE, new Position('H', 1)));
        for (char x = 'A'; x <= 'H'; x++) addPiece(new Pawn(Colors.WHITE, new Position(x, 2)));

        // BLACK
        addPiece(new Rook(Colors.BLACK, new Position('A', 8)));
        addPiece(new Knight(Colors.BLACK, new Position('B', 8)));
        addPiece(new Bishop(Colors.BLACK, new Position('C', 8)));
        addPiece(new Queen(Colors.BLACK, new Position('D', 8)));
        addPiece(new King(Colors.BLACK, new Position('E', 8)));
        addPiece(new Bishop(Colors.BLACK, new Position('F', 8)));
        addPiece(new Knight(Colors.BLACK, new Position('G', 8)));
        addPiece(new Rook(Colors.BLACK, new Position('H', 8)));
        for (char x = 'A'; x <= 'H'; x++) addPiece(new Pawn(Colors.BLACK, new Position(x, 7)));
    }

    public String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("    A B C D E F G H\n");
        sb.append("   -----------------\n");
        for (int y = 8; y >= 1; y--) {
            sb.append(y).append(" | ");
            for (char x = 'A'; x <= 'H'; x++) {
                Piece p = getPieceAt(new Position(x, y));
                if (p == null) sb.append(". ");
                else {
                    char t = p.type();
                    if (p.getColor() == Colors.BLACK) t = Character.toLowerCase(t);
                    sb.append(t).append(' ');
                }
            }
            sb.append("| ").append(y).append('\n');
        }
        sb.append("   -----------------\n");
        sb.append("    A B C D E F G H\n");
        return sb.toString();
    }

    // ------------------- CHECK LOGIC -------------------

    private Position findKing(Colors color) {
        for (ChessPair<Position, Piece> pair : pieces) {
            Piece p = pair.getValue();
            if (p instanceof King && p.getColor() == color) return p.getPosition();
        }
        return null;
    }

    // true daca "target" e atacat de "attacker"
    private boolean isSquareAttacked(Position target, Colors attacker) {
        for (ChessPair<Position, Piece> pair : pieces) {
            Piece p = pair.getValue();
            if (p.getColor() != attacker) continue;

            if (p instanceof Pawn) {
                if (pawnAttacks((Pawn) p, target)) return true;
                continue;
            }

            if (p instanceof King) {
                if (kingAttacks((King) p, target)) return true;
                continue;
            }

            // pt rook/bishop/queen/knight: getPossibleMoves = squares attacked (in modelul nostru simplu)
            for (Position m : p.getPossibleMoves(this)) {
                if (m.equals(target)) return true;
            }
        }
        return false;
    }

    private boolean pawnAttacks(Pawn pawn, Position target) {
        if (pawn == null || pawn.getPosition() == null || target == null) return false;
        Position pos = pawn.getPosition();
        int dir = (pawn.getColor() == Colors.WHITE) ? 1 : -1;

        Position diagL = new Position((char) (pos.getX() - 1), pos.getY() + dir);
        Position diagR = new Position((char) (pos.getX() + 1), pos.getY() + dir);

        return (inBounds(diagL) && diagL.equals(target)) || (inBounds(diagR) && diagR.equals(target));
    }

    private boolean kingAttacks(King king, Position target) {
        if (king == null || king.getPosition() == null || target == null) return false;
        Position k = king.getPosition();
        int dx = Math.abs(k.getX() - target.getX());
        int dy = Math.abs(k.getY() - target.getY());
        return dx <= 1 && dy <= 1 && !(dx == 0 && dy == 0);
    }

    private boolean wouldLeaveKingInCheck(Position from, Position to, Colors mover) {
        Piece moving = getPieceAt(from);
        Piece captured = getPieceAt(to);

        // apply temporary
        removeAt(from);
        if (captured != null) removeAt(to);
        moving.setPosition(to);
        addPiece(moving);

        Position kingPos = findKing(mover);
        boolean inCheck = (kingPos != null) && isSquareAttacked(kingPos, (mover == Colors.WHITE) ? Colors.BLACK : Colors.WHITE);

        // undo
        removeAt(to);
        moving.setPosition(from);
        addPiece(moving);
        if (captured != null) addPiece(captured);

        return inCheck;
    }

    public boolean isInCheck(Colors color) {
        Position kingPos = findKing(color);
        if (kingPos == null) return false;
        Colors attacker = (color == Colors.WHITE) ? Colors.BLACK : Colors.WHITE;
        return isSquareAttacked(kingPos, attacker);
    }

    // ------------------- MOVE VALIDATION -------------------

    public void movePiece(Position from, Position to, Colors currentColor) throws InvalidMoveException {
        if (!inBounds(from) || !inBounds(to)) throw new InvalidMoveException("Out of board");

        Piece piece = getPieceAt(from);
        if (piece == null) throw new InvalidMoveException("No piece at " + from);
        if (piece.getColor() != currentColor) throw new InvalidMoveException("Not your piece");

        Piece dst = getPieceAt(to);
        if (dst != null && dst.getColor() == currentColor) {
            throw new InvalidMoveException("Destination occupied by your piece");
        }

        // legal move for that piece
        boolean ok = false;
        for (Position p : piece.getPossibleMoves(this)) {
            if (p.equals(to)) { ok = true; break; }
        }
        if (!ok) throw new InvalidMoveException("Illegal move for " + piece.type());

        // NEW: forbid moves that leave your king in check
        if (wouldLeaveKingInCheck(from, to, currentColor)) {
            throw new InvalidMoveException("Move leaves king in check");
        }

        // apply real move
        removeAt(from);
        if (dst != null) removeAt(to);
        piece.setPosition(to);
        addPiece(piece);
    }
}

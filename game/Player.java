package game;

import exceptions.InvalidMoveException;
import model.ChessPair;
import model.Colors;
import model.Position;
import model.pieces.*;

import java.util.*;

public class Player {

    private String name;
    private Colors color;

    private List<Piece> capturedPieces = new ArrayList<>();
    private TreeSet<ChessPair<Position, Piece>> ownedPieces = new TreeSet<>();

    private int points;

    public Player() { }

    public Player(String name, Colors color) {
        this.name = name;
        this.color = color;
    }

    public String getName() { return name; }
    public Colors getColor() { return color; }
    public void setName(String name) { this.name = name; }
    public void setColor(Colors color) { this.color = color; }

    public List<Piece> getCapturedPieces() { return capturedPieces; }
    public List<ChessPair<Position, Piece>> getOwnedPieces() { return new ArrayList<>(ownedPieces); }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public void addCapturedPiece(Piece piece) {
        if (piece != null) {
            capturedPieces.add(piece);
            points += piecePoints(piece);
        }
    }

    private int piecePoints(Piece p) {
        if (p instanceof Queen) return 90;
        if (p instanceof Rook) return 50;
        if (p instanceof Bishop) return 30;
        if (p instanceof Knight) return 30;
        if (p instanceof Pawn) return 10;
        return 0;
    }

    public Move makeMove(Board board, Position from, Position to) throws InvalidMoveException {
        if (board == null) throw new InvalidMoveException("No board");
        Piece cap = board.getPieceAt(to);

        board.movePiece(from, to, color);

        ownedPieces.clear();
        for (ChessPair<Position, Piece> pr : new ArrayList<>(board.getPieces())) {
            if (pr != null && pr.getValue() != null && pr.getValue().getColor() == color) ownedPieces.add(pr);
        }

        return new Move(color, from, to, cap);
    }

    // ✅ IMPORTANT: snapshot ca sa nu crape cand Board simuleaza mutari
    public Position[] pickRandomLegalMove(Board b) {
        if (b == null) return null;

        List<Position[]> all = new ArrayList<>();
        List<ChessPair<Position, Piece>> snapshot = new ArrayList<>(b.getPieces());

        for (ChessPair<Position, Piece> pr : snapshot) {
            if (pr == null || pr.getValue() == null) continue;
            Piece p = pr.getValue();
            if (p.getColor() != color) continue;

            Position from = pr.getKey();
            List<Position> pm = p.getPossibleMoves(b);
            if (pm == null) continue;

            for (Position to : pm) {
                try {
                    if (b.isValidMove(from, to, color)) all.add(new Position[]{from, to});
                } catch (Throwable ignore) { }
            }
        }

        if (all.isEmpty()) return null;
        return all.get(new Random().nextInt(all.size()));
    }
}

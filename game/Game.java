package game;

import exceptions.InvalidMoveException;
import model.ChessPair;
import model.Colors;
import model.Position;
import model.pieces.Piece;

import java.util.*;

public class Game {

    private int id;
    private Board board = new Board();

    private Player whitePlayer;
    private Player blackPlayer;

    private List<Move> moves = new ArrayList<>();

    private Colors currentTurn = Colors.WHITE;
    private int currentPlayerIndex = 0;

    private Colors winnerColor = null;
    private boolean draw = false;

    // ✅ pentru repetitie de pozitie
    private final List<String> positionSignatures = new ArrayList<>();

    public Game() { }

    public Game(int id, Player whitePlayer, Player blackPlayer) {
        this.id = id;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
    }

    public int getId() { return id; }
    public Board getBoard() { return board; }
    public Player getWhitePlayer() { return whitePlayer; }
    public Player getBlackPlayer() { return blackPlayer; }
    public List<Move> getMoves() { return moves; }
    public Colors getCurrentTurn() { return currentTurn; }
    public Colors getWinnerColor() { return winnerColor; }
    public boolean isDraw() { return draw; }

    public void setId(int id) { this.id = id; }
    public void setBoard(Board board) { this.board = board; }
    public void setWhitePlayer(Player p) { this.whitePlayer = p; }
    public void setBlackPlayer(Player p) { this.blackPlayer = p; }
    public void setMoves(List<Move> moves) { this.moves = moves; }
    public void setCurrentTurn(Colors t) { this.currentTurn = t; }

    public void start() {
        if (board == null) board = new Board();
        board.initialize();

        if (moves == null) moves = new ArrayList<>();
        moves.clear();

        currentTurn = Colors.WHITE;
        currentPlayerIndex = 0;

        winnerColor = null;
        draw = false;

        positionSignatures.clear();
        positionSignatures.add(makePositionSignature());
    }

    public void resume() {
        if (moves == null) moves = new ArrayList<>();
        if (currentTurn == null) currentTurn = Colors.WHITE;

        currentPlayerIndex = (currentTurn == Colors.WHITE) ? 0 : 1;

        winnerColor = null;
        draw = false;

        // reconstruieste istoric minim (ca sa nu fie gol)
        if (positionSignatures.isEmpty()) {
            positionSignatures.add(makePositionSignature());
        }
    }

    public void switchPlayer() {
        currentPlayerIndex = 1 - currentPlayerIndex;
        currentTurn = (currentTurn == Colors.WHITE) ? Colors.BLACK : Colors.WHITE;
    }

    // compat ConsoleUI
    public void switchTurn() { switchPlayer(); }

    public void addMove(Move move) {
        if (moves == null) moves = new ArrayList<>();
        moves.add(move);

        // dupa fiecare mutare, salveaza semnatura + verifica repetitie
        positionSignatures.add(makePositionSignature());
        checkDrawByPositionRepetition();
    }

    // ✅ semnatura determinista: turn + lista piese sortata
    private String makePositionSignature() {
        List<String> parts = new ArrayList<>();

        List<ChessPair<Position, Piece>> snapshot = new ArrayList<>(board.getPieces());
        for (ChessPair<Position, Piece> pr : snapshot) {
            if (pr == null || pr.getKey() == null || pr.getValue() == null) continue;

            Position pos = pr.getKey();
            Piece pc = pr.getValue();

            char x = Character.toUpperCase(pos.getX());
            int y = pos.getY();

            char type = pc.type(); // K Q R B N P
            char col = (pc.getColor() == Colors.WHITE) ? 'W' : 'B';

            parts.add("" + type + col + x + y); // ex: PW A2 -> "PWA2" (fara spatii)
        }

        Collections.sort(parts);

        StringBuilder sb = new StringBuilder();
        sb.append(currentTurn == Colors.WHITE ? "T=W|" : "T=B|");
        for (String s : parts) sb.append(s).append(",");
        return sb.toString();
    }

    // ✅ cerinta: aceeasi pozitie repetata de 3 ori consecutiv
    private void checkDrawByPositionRepetition() {
        if (positionSignatures.size() < 3) return;

        String s1 = positionSignatures.get(positionSignatures.size() - 1);
        String s2 = positionSignatures.get(positionSignatures.size() - 2);
        String s3 = positionSignatures.get(positionSignatures.size() - 3);

        if (s1.equals(s2) && s2.equals(s3)) {
            draw = true;
            // computer “resigns” la egalitate => user castiga
            winnerColor = (getComputerPlayer().getColor() == Colors.WHITE) ? Colors.BLACK : Colors.WHITE;
        }
    }

    public boolean checkForCheckMate() {
        Colors victim = currentTurn;
        if (!board.isInCheck(victim)) return false;

        List<ChessPair<Position, Piece>> snapshot = new ArrayList<>(board.getPieces());

        for (ChessPair<Position, Piece> pr : snapshot) {
            if (pr == null || pr.getValue() == null) continue;
            Piece p = pr.getValue();
            if (p.getColor() != victim) continue;

            Position from = pr.getKey();
            List<Position> pm = p.getPossibleMoves(board);
            if (pm == null) continue;

            for (Position to : pm) {
                try {
                    if (board.isValidMove(from, to, victim)) return false;
                } catch (Throwable ignore) { }
            }
        }

        winnerColor = (victim == Colors.WHITE) ? Colors.BLACK : Colors.WHITE;
        return true;
    }

    public void tryMove(Player p, Position from, Position to, char promoteTo) throws InvalidMoveException {
        if (p == null) throw new InvalidMoveException("No player");
        if (p.getColor() != currentTurn) throw new InvalidMoveException("Not your turn");

        Piece captured = board.getPieceAt(to);
        board.movePiece(from, to, p.getColor(), promoteTo);

        Move mv = new Move(p.getColor(), from, to, captured);
        addMove(mv);

        if (captured != null) p.addCapturedPiece(captured);

        switchPlayer();
    }

    public void tryMove(Player p, Position from, Position to) throws InvalidMoveException {
        tryMove(p, from, to, 'Q');
    }

    public void makeRandomMoveFor(Player cpu) throws InvalidMoveException {
        if (cpu == null) throw new InvalidMoveException("No cpu");
        if (cpu.getColor() != currentTurn) throw new InvalidMoveException("Not cpu turn");

        Position[] mv = cpu.pickRandomLegalMove(board);
        if (mv == null) throw new InvalidMoveException("No legal moves");

        Piece captured = board.getPieceAt(mv[1]);
        board.movePiece(mv[0], mv[1], cpu.getColor(), 'Q');

        Move m = new Move(cpu.getColor(), mv[0], mv[1], captured);
        addMove(m);

        if (captured != null) cpu.addCapturedPiece(captured);

        switchPlayer();
    }

    public Player getComputerPlayer() {
        if (whitePlayer != null && "computer".equalsIgnoreCase(whitePlayer.getName())) return whitePlayer;
        return blackPlayer;
    }
}

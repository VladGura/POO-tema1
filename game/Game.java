package game;

import exceptions.InvalidMoveException;
import model.ChessPair;
import model.Colors;
import model.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.List;

public class Game {

    private int id;
    private Board board = new Board();
    private Player whitePlayer;
    private Player blackPlayer;
    private Colors currentTurn = Colors.WHITE;
    private List<Move> moves = new ArrayList<>();

    public Game() {
        // pentru JSON
    }

    public Game(int id, Player whitePlayer, Player blackPlayer) {
        this.id = id;
        this.whitePlayer = whitePlayer;
        this.blackPlayer = blackPlayer;
        this.board = new Board();
        this.currentTurn = Colors.WHITE;
    }

    public int getId() { return id; }
    public Board getBoard() { return board; }
    public Player getWhitePlayer() { return whitePlayer; }
    public Player getBlackPlayer() { return blackPlayer; }
    public Colors getCurrentTurn() { return currentTurn; }
    public List<Move> getMoves() { return moves; }

    public void setId(int id) { this.id = id; }
    public void setBoard(Board board) { this.board = board; }
    public void setWhitePlayer(Player p) { this.whitePlayer = p; }
    public void setBlackPlayer(Player p) { this.blackPlayer = p; }
    public void setCurrentTurn(Colors turn) { this.currentTurn = turn; }
    public void setMoves(List<Move> moves) { this.moves = moves; }

    public void switchTurn() {
        currentTurn = (currentTurn == Colors.WHITE) ? Colors.BLACK : Colors.WHITE;
    }

    public void addMove(Move move) {
        moves.add(move);
    }
}

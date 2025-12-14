package io;

import game.Game;
import game.Move;
import game.Player;
import model.ChessPair;
import model.Colors;
import model.Position;
import model.pieces.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class GamesRepository {

    private final Path gamesPath;

    public GamesRepository(Path gamesPath) {
        this.gamesPath = gamesPath;
    }

    public Map<Integer, Game> loadGamesAsMap() throws IOException, ParseException {
        return JsonReaderUtil.readGamesAsMap(gamesPath);
    }

    public void saveOrUpdate(Game game) throws IOException, ParseException {
        if (game == null) return;

        JSONArray arr = readRootArraySafe(gamesPath);
        JSONObject gObj = toJson(game);

        boolean replaced = false;
        for (int i = 0; i < arr.size(); i++) {
            Object item = arr.get(i);
            if (!(item instanceof JSONObject)) continue;
            JSONObject o = (JSONObject) item;

            int id = asInt(o.get("id"), -1);
            if (id == game.getId()) {
                arr.set(i, gObj);
                replaced = true;
                break;
            }
        }

        if (!replaced) arr.add(gObj);

        writeRootArray(gamesPath, arr);
    }

    // ---------------- JSON BUILD ----------------

    @SuppressWarnings("unchecked")
    private JSONObject toJson(Game g) {
        JSONObject obj = new JSONObject();
        obj.put("id", g.getId());

        // players: [{email,color}]
        JSONArray players = new JSONArray();
        Player wp = g.getWhitePlayer();
        Player bp = g.getBlackPlayer();
        if (wp != null) players.add(playerObj(playerEmail(wp), Colors.WHITE));
        if (bp != null) players.add(playerObj(playerEmail(bp), Colors.BLACK));
        obj.put("players", players);

        // currentPlayerColor
        obj.put("currentPlayerColor", String.valueOf(g.getCurrentTurn()));

        // board: [{type,color,position}]
        JSONArray boardArr = new JSONArray();

        // IMPORTANT: la tine e Set<ChessPair<Position, Piece>>
        Set<ChessPair<Position, model.pieces.Piece>> pieces = g.getBoard().getPieces();

        if (pieces != null) {
            for (ChessPair<Position, model.pieces.Piece> pair : pieces) {
                if (pair == null) continue;

                // IMPORTANT: ChessPair la tine NU are getFirst/getSecond.
                // Cel mai probabil are getKey/getValue (Map.Entry style).
                Position pos = pair.getKey();
                model.pieces.Piece piece = pair.getValue();

                if (pos == null || piece == null) continue;

                JSONObject pObj = new JSONObject();
                pObj.put("type", pieceTypeShort(piece));          // exact ca in json-ul tau: "K,Q,R,B,N,P"
                pObj.put("color", String.valueOf(piece.getColor()));
                pObj.put("position", posToString(pos));
                boardArr.add(pObj);
            }
        }
        obj.put("board", boardArr);

        // moves: [{playerColor,from,to}] (+ captured optional)
        JSONArray movesArr = new JSONArray();
        List<Move> moves = g.getMoves();
        if (moves != null) {
            for (Move m : moves) {
                if (m == null || m.getFrom() == null || m.getTo() == null) continue;

                JSONObject mObj = new JSONObject();
                mObj.put("playerColor", String.valueOf(m.getPlayerColor()));
                mObj.put("from", posToString(m.getFrom()));
                mObj.put("to", posToString(m.getTo()));

                // daca Move are captured, il salvam optional (ca in exemplul tau din games.json)
                // daca tu NU ai getCaptured(), poti sterge blocul asta.
                try {
                    Object captured = m.getCaptured(); // poate e Piece sau null
                    if (captured instanceof model.pieces.Piece) {
                        model.pieces.Piece cap = (model.pieces.Piece) captured;
                        JSONObject capObj = new JSONObject();
                        capObj.put("type", pieceTypeShort(cap));
                        capObj.put("color", String.valueOf(cap.getColor()));
                        mObj.put("captured", capObj);
                    }
                } catch (Throwable ignore) {
                    // ok daca nu exista in implementarea ta
                }

                movesArr.add(mObj);
            }
        }
        obj.put("moves", movesArr);

        return obj;
    }

    // incearca sa scoata email-ul real daca exista, altfel fallback
    private String playerEmail(Player p) {
        if (p == null) return "";
        // daca ai getEmail(), foloseste-l:
        // return p.getEmail();

        // altfel, in JsonReaderUtil noi am construit Player(email, color) cu email ca "nume"
        // deci in 90% cazuri getName() exista si e email-ul
        try {
            return String.valueOf(p.getName());
        } catch (Throwable t) {
            return String.valueOf(p);
        }
    }

    @SuppressWarnings("unchecked")
    private JSONObject playerObj(String email, Colors c) {
        JSONObject o = new JSONObject();
        o.put("email", email == null ? "" : email);
        o.put("color", String.valueOf(c));
        return o;
    }

    // ---------------- file helpers ----------------

    private JSONArray readRootArraySafe(Path path) throws IOException, ParseException {
        if (path == null) return new JSONArray();
        if (!Files.exists(path)) return new JSONArray();

        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            Object root = new JSONParser().parse(reader);
            if (root instanceof JSONArray) return (JSONArray) root;
            return new JSONArray();
        }
    }

    private void writeRootArray(Path path, JSONArray arr) throws IOException {
        if (path == null) return;
        Path parent = path.getParent();
        if (parent != null && !Files.exists(parent)) Files.createDirectories(parent);

        try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            writer.write(arr.toJSONString());
        }
    }

    private int asInt(Object o, int def) {
        if (o instanceof Number) return ((Number) o).intValue();
        try { return o != null ? Integer.parseInt(String.valueOf(o)) : def; }
        catch (NumberFormatException e) { return def; }
    }

    private String posToString(Position p) {
        if (p == null) return "";
        return "" + Character.toUpperCase(p.getX()) + p.getY();
    }

    // exact ca in json-ul tau: K/Q/R/B/N/P
    private String pieceTypeShort(model.pieces.Piece p) {
        if (p instanceof King) return "K";
        if (p instanceof Queen) return "Q";
        if (p instanceof Rook) return "R";
        if (p instanceof Bishop) return "B";
        if (p instanceof Knight) return "N";
        if (p instanceof Pawn) return "P";
        return "P";
    }
}

package io;

import game.Board;
import game.Game;
import game.Move;
import game.Player;
import model.Colors;
import model.Position;
import model.pieces.Bishop;
import model.pieces.King;
import model.pieces.Knight;
import model.pieces.Pawn;
import model.pieces.Piece;
import model.pieces.Queen;
import model.pieces.Rook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import user.User;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public final class JsonReaderUtil {

    private JsonReaderUtil() {}

    // accounts.json: [{email,password,points,games:[id,id,...]}]
    public static List<User> readUsers(Path accountsPath) throws IOException, ParseException {
        if (accountsPath == null || !Files.exists(accountsPath)) return new ArrayList<>();

        try (Reader reader = Files.newBufferedReader(accountsPath, StandardCharsets.UTF_8)) {
            Object root = new JSONParser().parse(reader);
            JSONArray arr = asArray(root);
            if (arr == null) return new ArrayList<>();

            List<User> users = new ArrayList<>();
            for (Object item : arr) {
                JSONObject obj = asObject(item);
                if (obj == null) continue;

                String email = asString(obj.get("email"));
                String password = asString(obj.get("password"));

                User u = new User(email, password);

                int pts = asInt(obj.get("points"), 0);
                if (pts != 0) u.addPoints(pts);

                users.add(u);
            }
            return users;
        }
    }

    // games.json: [{id, players:[{email,color}], currentPlayerColor, board:[{type,color,position}], moves:[{playerColor,from,to}]}]
    public static Map<Integer, Game> readGamesAsMap(Path gamesPath) throws IOException, ParseException {
        Map<Integer, Game> map = new HashMap<>();
        if (gamesPath == null || !Files.exists(gamesPath)) return map;

        try (Reader reader = Files.newBufferedReader(gamesPath, StandardCharsets.UTF_8)) {
            Object root = new JSONParser().parse(reader);
            JSONArray arr = asArray(root);
            if (arr == null) return map;

            for (Object item : arr) {
                JSONObject obj = asObject(item);
                if (obj == null) continue;

                int id = asInt(obj.get("id"), -1);
                if (id < 0) continue;

                Game g = new Game();
                g.setId(id);

                // players
                Player white = null;
                Player black = null;

                JSONArray playersArr = asArray(obj.get("players"));
                if (playersArr != null) {
                    for (Object pItem : playersArr) {
                        JSONObject pObj = asObject(pItem);
                        if (pObj == null) continue;

                        String email = asString(pObj.get("email"));
                        Colors c = parseColor(asString(pObj.get("color")));
                        Player p = new Player(email, c);

                        if (c == Colors.WHITE) white = p;
                        else if (c == Colors.BLACK) black = p;
                    }
                }

                if (white == null) white = new Player("white@unknown", Colors.WHITE);
                if (black == null) black = new Player("black@unknown", Colors.BLACK);

                g.setWhitePlayer(white);
                g.setBlackPlayer(black);

                // current turn
                g.setCurrentTurn(parseColor(asString(obj.get("currentPlayerColor"))));

                // board
                Board b = new Board();
                b.clear();

                JSONArray boardArr = asArray(obj.get("board"));
                if (boardArr != null) {
                    for (Object bItem : boardArr) {
                        JSONObject bObj = asObject(bItem);
                        if (bObj == null) continue;

                        String type = asString(bObj.get("type"));
                        Colors color = parseColor(asString(bObj.get("color")));
                        Position pos = parsePos(asString(bObj.get("position")));

                        Piece piece = buildPiece(type, color, pos);
                        if (piece != null) b.addPiece(piece);
                    }
                }

                g.setBoard(b);

                // moves (optional)
                List<Move> moves = new ArrayList<>();
                JSONArray movesArr = asArray(obj.get("moves"));
                if (movesArr != null) {
                    for (Object mItem : movesArr) {
                        JSONObject mObj = asObject(mItem);
                        if (mObj == null) continue;

                        Colors pc = parseColor(asString(mObj.get("playerColor")));
                        Position from = parsePos(asString(mObj.get("from")));
                        Position to = parsePos(asString(mObj.get("to")));

                        moves.add(new Move(pc, from, to, null));
                    }
                }
                g.setMoves(moves);

                map.put(id, g);
            }
        }

        return map;
    }

    // leaga users cu game-urile dupa id-urile din accounts.json
    public static void attachUserGames(Path accountsPath, List<User> users, Map<Integer, Game> games)
            throws IOException, ParseException {

        if (accountsPath == null || !Files.exists(accountsPath)) return;

        Map<String, User> byEmail = new HashMap<>();
        for (User u : users) byEmail.put(u.getEmail(), u);

        try (Reader reader = Files.newBufferedReader(accountsPath, StandardCharsets.UTF_8)) {
            Object root = new JSONParser().parse(reader);
            JSONArray arr = asArray(root);
            if (arr == null) return;

            for (Object item : arr) {
                JSONObject obj = asObject(item);
                if (obj == null) continue;

                String email = asString(obj.get("email"));
                User u = byEmail.get(email);
                if (u == null) continue;

                JSONArray gids = asArray(obj.get("games"));
                if (gids == null) continue;

                for (Object gidObj : gids) {
                    int gid = asInt(gidObj, -1);
                    Game g = games.get(gid);
                    if (g != null) u.addGame(g);
                }
            }
        }
    }

    // ---------- HELPERS ----------

    private static JSONArray asArray(Object o) {
        return (o instanceof JSONArray) ? (JSONArray) o : null;
    }

    private static JSONObject asObject(Object o) {
        return (o instanceof JSONObject) ? (JSONObject) o : null;
    }

    private static String asString(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }

    private static int asInt(Object o, int def) {
        if (o instanceof Number) return ((Number) o).intValue();
        try {
            return (o != null) ? Integer.parseInt(String.valueOf(o)) : def;
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static Colors parseColor(String s) {
        if (s == null) return Colors.GRAY;
        String t = s.trim().toUpperCase(Locale.ROOT);
        if (t.equals("WHITE")) return Colors.WHITE;
        if (t.equals("BLACK")) return Colors.BLACK;
        if (t.equals("GRAY")) return Colors.GRAY;
        if (t.equals("W")) return Colors.WHITE;
        if (t.equals("B")) return Colors.BLACK;
        return Colors.GRAY;
    }

    private static Position parsePos(String s) {
        if (s == null || s.length() < 2) return null;
        char x = Character.toUpperCase(s.charAt(0));
        int y;
        try {
            y = Integer.parseInt(s.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
        return new Position(x, y);
    }

    private static Piece buildPiece(String type, Colors color, Position pos) {
        if (type == null || pos == null) return null;

        String t = type.trim().toUpperCase(Locale.ROOT);

        if (t.equals("K") || t.equals("KING")) return new King(color, pos);
        if (t.equals("Q") || t.equals("QUEEN")) return new Queen(color, pos);
        if (t.equals("R") || t.equals("ROOK")) return new Rook(color, pos);
        if (t.equals("B") || t.equals("BISHOP")) return new Bishop(color, pos);
        if (t.equals("N") || t.equals("KNIGHT")) return new Knight(color, pos);
        if (t.equals("P") || t.equals("PAWN")) return new Pawn(color, pos);

        return null;
    }
}

package ui;

import exceptions.InvalidCommandException;
import exceptions.InvalidMoveException;
import game.Game;
import game.Move;
import game.Player;
import io.AccountsRepository;
import io.GamesRepository;
import model.Colors;
import model.Position;
import org.json.simple.parser.ParseException;
import user.User;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class ConsoleUI {

    private final Scanner scanner = new Scanner(System.in);

    private Game game;

    // Loaded data (from JSON)
    private List<User> loadedUsers = new ArrayList<>();
    private Map<Integer, Game> loadedGames = new HashMap<>();
    private final Map<String, User> usersByEmail = new HashMap<>();

    // Default input paths (relative to project root)
    private final Path accountsPath = Path.of("input", "accounts.json");
    private final Path gamesPath = Path.of("input", "games.json");

    public void start() {
        System.out.println(" POO demo");
        printHelp();

        game = new Game(1,
                new Player("white", Colors.WHITE),
                new Player("black", Colors.BLACK));

        loop();
    }

    private void printHelp() {
        System.out.println("Commands:");
        System.out.println("  new            -> new game (fresh board)");
        System.out.println("  show           -> print board");
        System.out.println("  reset          -> reset board");
        System.out.println("  play           -> run automatic demo moves");
        System.out.println("  load           -> load users+games from JSON");
        System.out.println("  users          -> list loaded users");
        System.out.println("  games <email>  -> list games for a user");
        System.out.println("  open <gameId>  -> open a loaded game (with replay)");
        System.out.println("  save           -> save current game to input/games.json");
        System.out.println("  skip           -> switch turn (demo helper)");
        System.out.println("  E2-E4          -> move (also accepts: E2 E4)");
        System.out.println("  exit           -> quit");
        System.out.println();
    }

    private void loop() {
        while (true) {
            System.out.println(game.getBoard().render());
            System.out.println("Turn: " + game.getCurrentTurn());
            System.out.print("> ");

            String lineRaw = scanner.nextLine();
            if (lineRaw == null) return;
            String line = lineRaw.trim();
            if (line.isEmpty()) continue;

            // --- basic commands ---
            if (line.equalsIgnoreCase("exit")) {
                System.out.println("Bye.");
                return;
            }
            if (line.equalsIgnoreCase("help")) {
                printHelp();
                continue;
            }
            if (line.equalsIgnoreCase("show")) {
                continue;
            }
            if (line.equalsIgnoreCase("reset")) {
                game.getBoard().initialize();
                game.setCurrentTurn(Colors.WHITE);
                continue;
            }
            if (line.equalsIgnoreCase("new")) {
                game = new Game(1,
                        new Player("white", Colors.WHITE),
                        new Player("black", Colors.BLACK));
                continue;
            }
            if (line.equalsIgnoreCase("skip")) {
                game.switchTurn();
                continue;
            }
            if (line.equalsIgnoreCase("save")) {
                doSave();
                continue;
            }

            // --- FIX: usage for open / games when called without args ---
            if (line.equalsIgnoreCase("open")) {
                System.out.println("Usage: open <gameId>");
                continue;
            }
            if (line.equalsIgnoreCase("games")) {
                System.out.println("Usage: games <email>");
                continue;
            }

            // --- data commands ---
            if (line.equalsIgnoreCase("load")) {
                doLoad();
                continue;
            }
            if (line.equalsIgnoreCase("users")) {
                doUsers();
                continue;
            }
            if (line.toLowerCase(Locale.ROOT).startsWith("games ")) {
                doGames(line.substring(6).trim());
                continue;
            }
            if (line.toLowerCase(Locale.ROOT).startsWith("open ")) {
                doOpen(line.substring(5).trim());
                continue;
            }

            // --- scripted demo ---
            if (line.equalsIgnoreCase("play")) {
                runScript();
                continue;
            }

            // --- move command: accept E2-E4 or E2 E4 ---
            try {
                handleMove(line);
            } catch (InvalidCommandException e) {
                System.out.println("Invalid command: " + e.getMessage());
            } catch (InvalidMoveException e) {
                System.out.println("Invalid move: " + e.getMessage());
            }
        }
    }

    private void doLoad() {
        try {
            AccountsRepository accRepo = new AccountsRepository(accountsPath);
            GamesRepository gamesRepo = new GamesRepository(gamesPath);

            loadedUsers = accRepo.loadUsers();
            loadedGames = gamesRepo.loadGamesAsMap();

            // attach user -> games
            io.JsonReaderUtil.attachUserGames(accountsPath, loadedUsers, loadedGames);

            usersByEmail.clear();
            for (User u : loadedUsers) {
                usersByEmail.put(u.getEmail(), u);
            }

            System.out.println("Loaded users=" + loadedUsers.size() + ", games=" + loadedGames.size());
        } catch (IOException | ParseException e) {
            System.out.println("LOAD ERROR: " + e.getMessage());
        }
    }

    private void doUsers() {
        if (loadedUsers.isEmpty()) {
            System.out.println("No users loaded. Run: load");
            return;
        }
        for (User u : loadedUsers) {
            System.out.println("- " + u.getEmail() + " (points=" + u.getPoints() + ", games=" + u.getGames().size() + ")");
        }
    }

    private void doGames(String email) {
        if (email == null || email.isEmpty()) {
            System.out.println("Usage: games <email>");
            return;
        }
        if (loadedUsers.isEmpty()) {
            System.out.println("No users loaded. Run: load");
            return;
        }
        User u = usersByEmail.get(email);
        if (u == null) {
            System.out.println("User not found: " + email);
            return;
        }
        if (u.getGames().isEmpty()) {
            System.out.println("User has no games: " + email);
            return;
        }

        System.out.println("Games for " + email + ":");
        for (Game g : u.getGames()) {
            int moves = (g.getMoves() == null) ? 0 : g.getMoves().size();
            System.out.println("- id=" + g.getId()
                    + " turn=" + g.getCurrentTurn()
                    + " pieces=" + g.getBoard().getPieces().size()
                    + " moves=" + moves);
        }
        System.out.println("Open one with: open <gameId>");
    }

    private void doOpen(String idStr) {
        if (idStr == null || idStr.isEmpty()) {
            System.out.println("Usage: open <gameId>");
            return;
        }
        if (loadedGames.isEmpty()) {
            System.out.println("No games loaded. Run: load");
            return;
        }

        int gid;
        try {
            gid = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            System.out.println("Invalid gameId: " + idStr);
            return;
        }

        Game g = loadedGames.get(gid);
        if (g == null) {
            System.out.println("Game not found: " + gid);
            return;
        }

        boolean replayOk = replayMovesIntoGame(g);

        this.game = g;

        System.out.println("Opened game id=" + gid + (replayOk ? " (replayed)" : " (json board fallback)"));
        System.out.println("Turn=" + game.getCurrentTurn());

        if (game.getBoard().isInCheck(game.getCurrentTurn())) {
            System.out.println("CHECK (player to move is in check)!");
        }
    }

    private boolean replayMovesIntoGame(Game g) {
        List<Move> moves = g.getMoves();
        if (moves == null || moves.isEmpty()) {
            return true;
        }

        // fallback state = board from JSON + turn from JSON
        var jsonTurn = g.getCurrentTurn();
        var board = g.getBoard();

        try {
            System.out.println("Replaying moves: " + moves.size());

            board.initialize();

            for (Move m : moves) {
                if (m == null || m.getFrom() == null || m.getTo() == null) {
                    throw new InvalidMoveException("Invalid move entry in history");
                }
                board.movePiece(m.getFrom(), m.getTo(), m.getPlayerColor());
            }

            // authoritative: keep JSON turn
            g.setCurrentTurn(jsonTurn);

            Colors computed = (moves.size() % 2 == 0) ? Colors.WHITE : Colors.BLACK;
            if (computed != jsonTurn) {
                System.out.println("WARN: JSON currentTurn=" + jsonTurn + " but replay suggests " + computed);
            }

            return true;

        } catch (Exception ex) {
            // do NOT crash in interviu
            g.setCurrentTurn(jsonTurn);
            System.out.println("REPLAY ERROR (fallback to JSON board): " + ex.getMessage());
            return false;
        }
    }

    private void doSave() {
        try {
            GamesRepository repo = new GamesRepository(gamesPath);

            // update local cache too (if loaded)
            if (loadedGames != null) {
                loadedGames.put(game.getId(), game);
            }

            repo.saveOrUpdate(game);

            System.out.println("Saved game id=" + game.getId() + " to " + gamesPath);
        } catch (Exception e) {
            System.out.println("SAVE ERROR: " + e.getMessage());
        }
    }

    private void handleMove(String line) throws InvalidCommandException, InvalidMoveException {
        String norm = line.toUpperCase().trim().replace(" ", "-");
        if (!norm.matches("^[A-H][1-8]-[A-H][1-8]$")) {
            throw new InvalidCommandException("Invalid command format");
        }

        Position from = parsePos(norm.substring(0, 2));
        Position to = parsePos(norm.substring(3, 5));

        game.getBoard().movePiece(from, to, game.getCurrentTurn());
        game.switchTurn();

        if (game.getBoard().isInCheck(game.getCurrentTurn())) {
            System.out.println("CHECK!");
        }
    }

    private Position parsePos(String s) {
        char x = Character.toUpperCase(s.charAt(0));
        int y = Integer.parseInt(s.substring(1));
        return new Position(x, y);
    }

    private void runScript() {
        System.out.println("=== Running demo script (with checks) ===");

        game.getBoard().initialize();
        game.setCurrentTurn(Colors.WHITE);

        Object[][] script = {
                {"E2-E4", true, null},
                {"E7-E5", true, null},
                {"D1-H5", true, null},
                {"B8-C6", true, null},
                {"F1-C4", true, null},
                {"G8-F6", true, null},
                {"H5-F7", true, "CHECK"},
                {"A8-A6", false, "Illegal move"},
                {"B1 A3", false, "Not your piece"},
        };

        int pass = 0, fail = 0;

        for (Object[] step : script) {
            String cmd = (String) step[0];
            boolean expectedOk = (boolean) step[1];
            String expectedContains = (String) step[2];

            System.out.println();
            System.out.println(game.getBoard().render());
            System.out.println("Turn: " + game.getCurrentTurn());
            System.out.println("> " + cmd);

            boolean ok = true;
            String extra = "";

            try {
                String norm = cmd.toUpperCase().replace(" ", "-");
                if (!norm.matches("^[A-H][1-8]-[A-H][1-8]$")) {
                    throw new InvalidCommandException("Invalid command format");
                }

                Position from = parsePos(norm.substring(0, 2));
                Position to = parsePos(norm.substring(3, 5));

                game.getBoard().movePiece(from, to, game.getCurrentTurn());
                game.switchTurn();

                if (game.getBoard().isInCheck(game.getCurrentTurn())) {
                    extra = "CHECK";
                    System.out.println("CHECK!");
                }

            } catch (Exception e) {
                ok = false;
                extra = e.getMessage();
                System.out.println("ERROR: " + e.getMessage());
            }

            boolean matchesExpectation = (ok == expectedOk);
            if (matchesExpectation && expectedContains != null) {
                matchesExpectation = extra.toUpperCase().contains(expectedContains.toUpperCase());
            }

            if (matchesExpectation) {
                pass++;
                System.out.println("[PASS]");
            } else {
                fail++;
                System.out.println("[FAIL] expectedOk=" + expectedOk
                        + (expectedContains != null ? (" expectedContains=" + expectedContains) : ""));
            }
        }

        System.out.println();
        System.out.println("=== Script done ===");
        System.out.println("PASS=" + pass + " FAIL=" + fail);
        System.out.println(game.getBoard().render());
        System.out.println("Turn: " + game.getCurrentTurn());
    }
}

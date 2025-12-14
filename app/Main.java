package app;

import exceptions.InvalidCommandException;
import exceptions.InvalidMoveException;
import game.Game;
import game.Player;
import model.Colors;
import model.Position;
import user.User;
import io.AccountsRepository;
import io.GamesRepository;
import io.JsonReaderUtil;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Main {

    private List<User> users = new ArrayList<>();
    private Map<Integer, Game> games = new HashMap<>();
    private User currentUser;

    private final Path accountsPath = Path.of("input", "accounts.json");
    private final Path gamesPath = Path.of("input", "games.json");

    private AccountsRepository accountsRepo = new AccountsRepository(accountsPath);
    private GamesRepository gamesRepo = new GamesRepository(gamesPath);

    private final Scanner sc = new Scanner(System.in);

    public void read() {
        try { users = accountsRepo.loadUsers(); }
        catch (IOException | ParseException e) { users = new ArrayList<>(); }

        try { games = JsonReaderUtil.readGamesAsMap(gamesPath); }
        catch (IOException | ParseException e) { games = new HashMap<>(); }

        try { JsonReaderUtil.attachUserGames(accountsPath, users, games); }
        catch (IOException | ParseException e) { }
    }

    public void write() {
        try { accountsRepo.saveUsers(users); } catch (Exception e) { }
        try {
            if (currentUser != null) {
                for (Game g : currentUser.getActiveGames()) gamesRepo.saveOrUpdate(g);
            }
        } catch (Exception e) { }
    }

    public User login(String email, String password) {
        if (email == null || password == null) return null;
        for (User u : users) {
            if (u != null && email.equals(u.getEmail()) && password.equals(u.getPassword())) {
                currentUser = u;
                return u;
            }
        }
        return null;
    }

    public User newAccount(String email, String password) {
        if (email == null || email.isBlank() || password == null) return null;
        for (User u : users) {
            if (u != null && email.equals(u.getEmail())) { currentUser = u; return u; }
        }
        User u = new User(email, password);
        users.add(u);
        currentUser = u;
        return u;
    }

    public void run() {
        while (true) {
            if (currentUser == null) {
                authLoop();
                if (currentUser == null) return;
            }

            try {
                System.out.println("\n=== MAIN MENU ===");
                System.out.println("1) New game (vs computer)");
                System.out.println("2) View active games");
                System.out.println("3) Logout");
                System.out.print("> ");

                int opt = readMenuInt();
                if (opt == 1) doNewGame();
                else if (opt == 2) doActiveGames();
                else if (opt == 3) currentUser = null;
                else throw new InvalidCommandException("Invalid option");
            } catch (Exception e) {
                System.out.println("Invalid command");
            } finally {
                write();
            }
        }
    }

    private void authLoop() {
        while (currentUser == null) {
            System.out.println("\n=== AUTH ===");
            System.out.println("1) login");
            System.out.println("2) new account");
            System.out.println("3) exit");
            System.out.print("> ");

            int c = readMenuInt();
            if (c == 3) return;

            System.out.print("email: ");
            String e = readLineSafe();
            System.out.print("password: ");
            String p = readLineSafe();

            if (c == 1) {
                if (login(e, p) == null) System.out.println("Bad credentials. Try again.");
            } else if (c == 2) {
                if (newAccount(e, p) == null) System.out.println("Could not create.");
            } else {
                System.out.println("Invalid command");
            }
        }
    }

    private void doNewGame() {
        System.out.print("Alias (player name): ");
        String alias = readLineSafe();
        Colors col = pickColor();

        int id = nextGameId();
        Player human = new Player(alias, col);
        Player cpu = new Player("computer", (col == Colors.WHITE) ? Colors.BLACK : Colors.WHITE);

        Game g;
        if (col == Colors.WHITE) g = new Game(id, human, cpu);
        else g = new Game(id, cpu, human);

        g.start();
        currentUser.addGame(g);
        games.put(id, g);

        playLoop(g, human, cpu);
    }

    private void doActiveGames() {
        List<Game> list = currentUser.getActiveGames();
        if (list.isEmpty()) { System.out.println("No active games."); return; }

        System.out.println("\nActive games:");
        for (Game g : list) System.out.println("  id=" + g.getId() + " turn=" + g.getCurrentTurn());

        System.out.println("1) view details");
        System.out.println("2) continue");
        System.out.println("3) delete");
        System.out.print("> ");
        int opt = readMenuInt();

        System.out.print("game id: ");
        int gid = readMenuInt();

        Game g = null;
        for (Game gg : list) if (gg != null && gg.getId() == gid) { g = gg; break; }
        if (g == null) { System.out.println("No such game"); return; }

        if (opt == 1) {
            System.out.println(g.getBoard().render());
            System.out.println("Moves:");
            for (var m : g.getMoves()) System.out.println("  " + m);
        } else if (opt == 2) {
            g.resume();
            Player cpu = g.getComputerPlayer();
            Player human = (cpu == g.getWhitePlayer()) ? g.getBlackPlayer() : g.getWhitePlayer();
            playLoop(g, human, cpu);
        } else if (opt == 3) {
            currentUser.removeGame(g);
            System.out.println("Deleted.");
        } else {
            System.out.println("Invalid command");
        }
    }

    private void playLoop(Game g, Player human, Player cpu) {
        System.out.println("\nGame started. Commands: 'A2-A3', 'leave', 'resign'");

        while (true) {
            System.out.println(g.getBoard().render());
            System.out.println("Next move: " + g.getCurrentTurn());

            if (g.isDraw()) {
                System.out.println("Draw (computer resign). Game ended.");
                finalizePointsWinLikeResign(g, human);
                currentUser.removeGame(g);
                return;
            }

            if (g.checkForCheckMate()) {
                System.out.println("Checkmate. Game ended.");
                boolean humanWon = (g.getWinnerColor() == human.getColor());
                finalizePointsByMate(human, humanWon);
                currentUser.removeGame(g);
                return;
            }

            if (g.getCurrentTurn() == human.getColor()) {
                System.out.print("> ");
                String cmd = readLineSafe().trim();

                if (cmd.equalsIgnoreCase("leave")) {
                    // save game in user active list
                    return;
                }

                if (cmd.equalsIgnoreCase("resign")) {
                    System.out.println("You resigned.");
                    finalizePointsResign(human);
                    currentUser.removeGame(g);
                    return;
                }

                try {
                    Position[] mv = parseMove(cmd);
                    char promo = 'Q';
                    if (g.getBoard().requiresPromotion(mv[0], mv[1])) {
                        promo = askPromotion();
                    }
                    g.tryMove(human, mv[0], mv[1], promo);
                } catch (InvalidCommandException e) {
                    System.out.println("Invalid command");
                } catch (InvalidMoveException e) {
                    System.out.println("Invalid move: " + e.getMessage());
                } catch (Throwable t) {
                    System.out.println("Error: " + t.getMessage());
                }
            } else {
                try {
                    g.makeRandomMoveFor(cpu);
                } catch (Throwable t) {
                    // no legal move -> treat as draw -> computer resign
                    System.out.println("Computer cannot move. Draw.");
                    finalizePointsWinLikeResign(g, human);
                    currentUser.removeGame(g);
                    return;
                }
            }
        }
    }

    // --- scor cerinta ---
    // Xnou = X + Y - 150 (user resign)
    private void finalizePointsResign(Player human) {
        int X = currentUser.getPoints();
        int Y = human.getPoints();
        currentUser.setPoints(X + Y - 150);
    }

    // draw -> computer resign => Xnou = X + Y + 150
    private void finalizePointsWinLikeResign(Game g, Player human) {
        int X = currentUser.getPoints();
        int Y = human.getPoints();
        currentUser.setPoints(X + Y + 150);
    }

    // mate: Xnou = X + Y ± 300
    private void finalizePointsByMate(Player human, boolean humanWon) {
        int X = currentUser.getPoints();
        int Y = human.getPoints();
        currentUser.setPoints(X + Y + (humanWon ? 300 : -300));
    }

    // --- helpers ---
    private char askPromotion() {
        System.out.print("Promote pawn to (Q/R/B/N): ");
        String s = readLineSafe().trim().toUpperCase();
        if (s.isEmpty()) return 'Q';
        char c = s.charAt(0);
        if (c == 'R' || c == 'B' || c == 'N' || c == 'Q') return c;
        return 'Q';
    }

    private Colors pickColor() {
        System.out.println("Choose color: 1) WHITE  2) BLACK");
        System.out.print("> ");
        int v = readMenuInt();
        return (v == 2) ? Colors.BLACK : Colors.WHITE;
    }

    private int nextGameId() {
        int mx = 0;
        for (Integer k : games.keySet()) if (k != null && k > mx) mx = k;
        return mx + 1;
    }

    private int readMenuInt() {
        String s = readLineSafe().trim();
        if (s.equalsIgnoreCase("login")) return 1;
        if (s.equalsIgnoreCase("new")) return 2;
        if (s.equalsIgnoreCase("exit")) return 3;
        try { return Integer.parseInt(s); } catch (Exception e) { return -999; }
    }

    private String readLineSafe() {
        try { return sc.nextLine(); } catch (Exception e) { return ""; }
    }

    private Position[] parseMove(String s) throws InvalidCommandException {
        if (s == null) throw new InvalidCommandException("null");
        String t = s.trim().replace(" ", "");
        String[] parts = t.split("-");
        if (parts.length != 2) throw new InvalidCommandException("format");
        Position a = parsePos(parts[0]);
        Position b = parsePos(parts[1]);
        if (a == null || b == null) throw new InvalidCommandException("pos");
        return new Position[]{a, b};
    }

    private Position parsePos(String s) {
        if (s == null || s.length() < 2) return null;
        char x = Character.toUpperCase(s.charAt(0));
        int y;
        try { y = Integer.parseInt(s.substring(1)); } catch (Exception e) { return null; }
        return new Position(x, y);
    }

    public static void main(String[] args) {
        Main m = new Main();
        m.read();
        m.run();
    }
}

package user;

import game.Game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class User {
    private String email;
    private String password;
    private List<Game> games = new ArrayList<>();
    private int points;

    public User() { }

    public User(String email, String password) {
        this.email = email;
        this.password = password;
        this.points = 0;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }

    public void setEmail(String e) { this.email = e; }
    public void setPassword(String p) { this.password = p; }

    public void addGame(Game game) {
        if (game == null) return;
        if (!games.contains(game)) games.add(game);
    }

    public void removeGame(Game game) {
        if (game == null) return;
        games.remove(game);
    }

    public List<Game> getActiveGames() {
        return Collections.unmodifiableList(games);
    }

    public int getPoints() { return points; }

    public void setPoints(int points) {
        this.points = points;
    }

    // helper used by JsonReaderUtil (legacy)
    public void addPoints(int v) {
        this.points += v;
    }

    // helper, used by older code
    public List<Game> getGames() { return games; }
}

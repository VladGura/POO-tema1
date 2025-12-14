package user;

import game.Game;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String email;
    private String password;
    private List<Game> games = new ArrayList<>();
    private int points;

    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public List<Game> getGames() { return games; }
    public int getPoints() { return points; }

    public void addGame(Game game) { games.add(game); }
    public void addPoints(int value) { this.points += value; }
}

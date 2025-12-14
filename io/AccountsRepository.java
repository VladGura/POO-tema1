package io;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import user.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class AccountsRepository {

    private final Path accountsPath;

    public AccountsRepository(Path accountsPath) {
        this.accountsPath = accountsPath;
    }

    public List<User> loadUsers() throws IOException, ParseException {
        return JsonReaderUtil.readUsers(accountsPath);
    }

    // ✅ necesar pentru Main.write()
    public void saveUsers(List<User> users) throws IOException {
        JSONArray arr = new JSONArray();

        if (users != null) {
            for (User u : users) {
                if (u == null) continue;

                JSONObject o = new JSONObject();
                o.put("email", u.getEmail());
                o.put("password", u.getPassword());
                o.put("points", u.getPoints());

                JSONArray g = new JSONArray();
                // daca ai activeGames
                try {
                    u.getActiveGames().forEach(game -> {
                        if (game != null) g.add(game.getId());
                    });
                } catch (Throwable ignore) {
                    // fallback daca nu exista getActiveGames in runtime (nu ar trebui)
                }

                o.put("games", g);
                arr.add(o);
            }
        }

        Files.createDirectories(accountsPath.getParent());
        Files.writeString(accountsPath, arr.toJSONString(), StandardCharsets.UTF_8);
    }

    // optional (debug)
    public Path getAccountsPath() {
        return accountsPath;
    }
}

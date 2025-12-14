package io;

import org.json.simple.parser.ParseException;
import user.User;

import java.io.IOException;
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
}

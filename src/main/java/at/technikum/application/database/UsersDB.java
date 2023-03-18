package at.technikum.application.database;

public enum UsersDB {
    USERNAME("username"),
    PASSWORD("password"),
    COINS("coins"),
    NAME("name"),
    BIO("bio"),
    IMAGE("image");

    final String value;

    UsersDB(String username) {
        value = username;
    }

    @Override
    public String toString() {
        return value;
    }

}

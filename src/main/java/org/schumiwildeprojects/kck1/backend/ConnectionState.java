package org.schumiwildeprojects.kck1.backend;

public enum ConnectionState {
    USERNAME_EXISTS("Taki użytkownik już istnieje."),
    SUCCESSFUL(null),
    INVALID_NICKNAME("Nazwa użytkownika może zawierać tylko małe i wielkie litery, cyfry, oraz następujące znaki:\n- _ [ ] { } \\ ` |");

    private final String msg;

    ConnectionState(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}

package org.schumiwildeprojects.kck1.backend;

public enum ConnectionState {
    USERNAME_EXISTS("Taki użytkownik już istnieje."),
    SUCCESSFUL(null);

    private final String msg;

    ConnectionState(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}

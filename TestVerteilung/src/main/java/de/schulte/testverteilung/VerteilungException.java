package de.schulte.testverteilung;

public class VerteilungException extends Exception {

    VerteilungException(String fehler) {
        super(fehler);
    }

    VerteilungException(String fehler, Exception e) {
        super(fehler, e);
    }
}

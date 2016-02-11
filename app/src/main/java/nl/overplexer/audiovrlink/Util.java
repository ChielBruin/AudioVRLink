package nl.overplexer.audiovrlink;

/**
 * Util class
 * Contains useful methods
 */
public class Util {
    public static String getString(int id) {
        return MainActivity.getInstance().getString(id);
    }

    public static String getString(int id, Object... formatArgs) {
        return MainActivity.getInstance().getString(id, formatArgs);
    }
}

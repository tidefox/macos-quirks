package io.github.tidefox.clientquirks.quit;

/** What to do with a window close request (Cmd+Q, red button, Alt+F4). Pure logic for unit tests. */
public enum QuitDecision {
    /** Not a close request, or one we were told to swallow: keep running. */
    IGNORE,
    /** Let vanilla shut the game down. */
    QUIT,
    /** Swallow the request and show the confirm screen. */
    CONFIRM;

    public static QuitDecision decide(boolean closeRequested, boolean enabled, boolean mac,
                                      boolean confirmEverywhere, boolean worldLoaded,
                                      boolean confirmScreenOpen) {
        if (!closeRequested) return IGNORE;
        if (!enabled || (!mac && !confirmEverywhere)) return QUIT;
        if (!worldLoaded) return QUIT;
        // A second Cmd+Q while the prompt is up means "yes, really quit".
        if (confirmScreenOpen) return QUIT;
        return CONFIRM;
    }
}

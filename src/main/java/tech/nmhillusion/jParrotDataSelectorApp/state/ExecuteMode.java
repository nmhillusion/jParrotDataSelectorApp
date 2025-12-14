package tech.nmhillusion.jParrotDataSelectorApp.state;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-12-13
 */
public enum ExecuteMode {
    SELECT(1, "Mode: SELECT"),
    UPDATE(2, "Mode: UPDATE");

    private final int value;
    private final String displayText;

    ExecuteMode(int value, String displayText) {
        this.value = value;
        this.displayText = displayText;
    }

    public static ExecuteMode fromValue(int value) {
        for (ExecuteMode mode : values()) {
            if (mode.getValue() == value) {
                return mode;
            }
        }
        return null;
    }

    public int getValue() {
        return value;
    }

    public String getDisplayText() {
        return displayText;
    }

    public ExecuteMode nextMode() {
        final ExecuteMode[] values = values();
        final int nextVal = getValue() % values.length + 1;
        return fromValue(nextVal);
    }
}

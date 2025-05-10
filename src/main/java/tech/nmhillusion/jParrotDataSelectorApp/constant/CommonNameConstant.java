package tech.nmhillusion.jParrotDataSelectorApp.constant;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-05-10
 */
public enum CommonNameConstant {
    ENV__APP_HOME("APP_HOME"),
    FOLDER__REQUIRED_CHECK("config");

    private final String eName;

    CommonNameConstant(String value) {
        this.eName = value;
    }

    public String getEName() {
        return eName;
    }
}

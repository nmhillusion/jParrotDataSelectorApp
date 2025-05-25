package tech.nmhillusion.jParrotDataSelectorApp.constant;

/**
 * date: 2025-05-25
 * <p>
 * created-by: nmhillusion
 */
public enum DbNameType {
    ORACLE("oracle"),
    SQL_SERVER("sqlserver"),
    MY_SQL("mysql");

    private final String value;

    DbNameType(String value) {
        this.value = value;
    }

    public static DbNameType from(String nameType) {
        for (DbNameType type : DbNameType.values()) {
            if (type.getValue().equalsIgnoreCase(nameType)) {
                return type;
            }
        }

        return null;
    }

    public String getValue() {
        return value;
    }
}

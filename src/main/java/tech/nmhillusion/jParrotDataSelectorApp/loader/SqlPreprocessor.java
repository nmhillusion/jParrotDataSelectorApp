package tech.nmhillusion.jParrotDataSelectorApp.loader;

import org.apache.calcite.config.Lex;
import org.apache.calcite.sql.SqlDialect;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import tech.nmhillusion.jParrotDataSelectorApp.constant.DbNameType;
import tech.nmhillusion.n2mix.helper.log.LogHelper;
import tech.nmhillusion.n2mix.util.StringUtil;
import tech.nmhillusion.n2mix.validator.StringValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * created by: nmhillusion
 * <p>
 * created date: 2025-12-28
 */
public class SqlPreprocessor {
    private final static EnumMap<DbNameType, SqlPreprocessor> PREPROCESSOR_FACTORY = new EnumMap<>(DbNameType.class);
    private final DbNameType dbNameType;
    private final SqlParser.Config parserConfig;
    private final SqlDialect sqlDialect;

    private SqlPreprocessor(DbNameType dbNameType) {
        this.dbNameType = dbNameType;

        final Lex dbLex = switch (dbNameType) {
            case ORACLE -> Lex.ORACLE;
            case SQL_SERVER -> Lex.SQL_SERVER;
            case MY_SQL -> Lex.MYSQL;
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbNameType);
        };

        this.parserConfig = SqlParser.config()
                .withLex(dbLex)
        ;

        this.sqlDialect = switch (dbNameType) {
            case ORACLE -> SqlDialect.DatabaseProduct.ORACLE.getDialect();
            case SQL_SERVER -> SqlDialect.DatabaseProduct.MSSQL.getDialect();
            case MY_SQL -> SqlDialect.DatabaseProduct.MYSQL.getDialect();
            default -> throw new IllegalArgumentException("Unsupported database type: " + dbNameType);
        };
    }

    public static SqlPreprocessor of(DbNameType dbNameType) {
        return PREPROCESSOR_FACTORY.computeIfAbsent(dbNameType, SqlPreprocessor::new);
    }

    public String parseSql(String rawSql) throws SqlParseException {
        final SqlParser parser = SqlParser.create(rawSql, parserConfig);
        final SqlNode sqlNode = parser.parseQuery(rawSql);

        return sqlNode.toSqlString(sqlDialect).getSql();
    }

    private String normalizeForSqlBlock(String sqlBlock) {
        return Stream.of(sqlBlock
                        .split("\n")
                )
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.joining("\n"));
    }

    public List<String> parseSqlStatements(String rawSql) throws SqlParseException {
        final List<String> sqlBlocks = Arrays.stream(StringUtil.trimWithNull(
                        rawSql
                ).split(";"))
                .map(this::normalizeForSqlBlock)
                .filter(Predicate.not(StringValidator::isBlank))
                .toList();

        final List<String> resultList = new ArrayList<>();
        for (String filteredStatement : sqlBlocks) {
            final String parsedSql = parseSql(filteredStatement);
            if (StringValidator.isBlank(parsedSql)) {
                continue;
            }
            resultList.add(filteredStatement);

            LogHelper.getLogger(this).info("Parsed SQL: {} -> {}", filteredStatement, parsedSql);
        }
        return resultList;
    }
}

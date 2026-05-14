package ALDRIGKØRIGEN;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.sql.*;
import java.util.*;


public class FridaImporter {

    // ── Defaults (overridden by command-line args) ──────────────────────────
    private static final String DEFAULT_XLSX = "data/Frida_5.5_Dataset.xlsx";
    private static final String DEFAULT_DB   = "frida.db";

    // Sheets to import and their target SQL table names
    private static final Map<String, String> SHEET_TO_TABLE = new LinkedHashMap<>();
    static {
        SHEET_TO_TABLE.put("Data_Normalised", "data_normalised");
        SHEET_TO_TABLE.put("Food",            "food");
        SHEET_TO_TABLE.put("FoodGroup",       "food_group");
        SHEET_TO_TABLE.put("Parameter",       "parameter");
        SHEET_TO_TABLE.put("Source",          "source");
    }

    // How many rows to batch-insert before flushing to disk
    private static final int BATCH_SIZE = 500;

    // ───────────────────────────────────────────────────────────────────────
    public static void main(String[] args) throws Exception {
        String xlsxPath = args.length > 0 ? args[0] : DEFAULT_XLSX;
        String dbPath   = args.length > 1 ? args[1] : DEFAULT_DB;

        System.out.println("Source : " + xlsxPath);
        System.out.println("Target : " + dbPath);
        System.out.println();

        try (Workbook workbook = new XSSFWorkbook(new FileInputStream(xlsxPath));
             Connection conn   = DriverManager.getConnection("jdbc:sqlite:" + dbPath)) {

            // Run PRAGMAs FIRST, while auto-commit is still on (no active transaction)
            try (Statement st = conn.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL");
                st.execute("PRAGMA synchronous=NORMAL");
            }

            // THEN disable auto-commit to start batched transactions
            conn.setAutoCommit(false);

            for (Map.Entry<String, String> entry : SHEET_TO_TABLE.entrySet()) {
                String sheetName = entry.getKey();
                String tableName = entry.getValue();

                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    System.out.println("[SKIP] Sheet not found: " + sheetName);
                    continue;
                }

                importSheet(conn, sheet, tableName);
                conn.commit();
            }

            addForeignKeyIndexes(conn);
            conn.commit();

            System.out.println("\nDone. Database written to: " + dbPath);
        }
    }

    // ── Import a single sheet into a SQL table ──────────────────────────────
    private static void importSheet(Connection conn, Sheet sheet, String tableName)
            throws SQLException {

        System.out.printf("Importing %-20s → %s%n", sheet.getSheetName(), tableName);

        // ── 1. Read header row ──────────────────────────────────────────────
        Iterator<Row> rowIter = sheet.iterator();
        if (!rowIter.hasNext()) {
            System.out.println("  [WARN] Empty sheet, skipping.");
            return;
        }

        Row headerRow = rowIter.next();
        List<String> headers = extractHeaders(headerRow);

        if (headers.isEmpty()) {
            System.out.println("  [WARN] No usable headers, skipping.");
            return;
        }

        // ── 2. CREATE TABLE ─────────────────────────────────────────────────
        StringBuilder createSql = new StringBuilder("CREATE TABLE IF NOT EXISTS ")
                .append(tableName).append(" (");
        for (int i = 0; i < headers.size(); i++) {
            createSql.append("\"").append(headers.get(i)).append("\" TEXT");
            if (i < headers.size() - 1) createSql.append(", ");
        }
        createSql.append(")");

        try (Statement st = conn.createStatement()) {
            st.execute("DROP TABLE IF EXISTS " + tableName);
            st.execute(createSql.toString());
        }

        // ── 3. Prepare INSERT statement ─────────────────────────────────────
        StringJoiner cols  = new StringJoiner(", ", "(", ")");
        StringJoiner marks = new StringJoiner(", ", "(", ")");
        for (String h : headers) {
            cols.add("\"" + h + "\"");
            marks.add("?");
        }
        String insertSql = "INSERT INTO " + tableName + " " + cols + " VALUES " + marks;

        // ── 4. Batch insert data rows ───────────────────────────────────────
        int rowCount = 0;
        try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
            while (rowIter.hasNext()) {
                Row row = rowIter.next();

                for (int col = 0; col < headers.size(); col++) {
                    Cell cell = row.getCell(col, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    String val = getCellValue(cell);

                    // Convert the literal "NULL" string the file uses → actual SQL NULL
                    if (val == null || val.equalsIgnoreCase("NULL") || val.isBlank()) {
                        ps.setNull(col + 1, Types.VARCHAR);
                    } else {
                        ps.setString(col + 1, val);
                    }
                }

                ps.addBatch();
                rowCount++;

                if (rowCount % BATCH_SIZE == 0) {
                    ps.executeBatch();
                    conn.commit();                    // flush every BATCH_SIZE rows
                    System.out.printf("  %,d rows inserted...%n", rowCount);
                }
            }

            // Flush any remaining rows
            ps.executeBatch();
        }

        System.out.printf("  ✓ %,d rows imported into '%s'%n", rowCount, tableName);
    }

    // ── Extract non-null column headers from the first row ──────────────────
    private static List<String> extractHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        // Track last non-null position so trailing empty columns are dropped
        int lastNonNull = -1;
        List<String> raw = new ArrayList<>();

        for (Cell cell : headerRow) {
            String val = getCellValue(cell);
            raw.add(val);
            if (val != null && !val.isBlank()) {
                lastNonNull = raw.size() - 1;
            }
        }

        // Only keep columns up to (and including) the last non-null header
        for (int i = 0; i <= lastNonNull; i++) {
            String h = raw.get(i);
            if (h == null || h.isBlank()) {
                // Give anonymous columns a generated name
                headers.add("col_" + i);
            } else {
                headers.add(sanitize(h));
            }
        }
        return headers;
    }

    // ── Sanitize a header string into a safe SQL column name ────────────────
    private static String sanitize(String raw) {
        return raw
                // Danish / common special letters → ASCII equivalents
                .replace("æ", "ae").replace("Æ", "ae")
                .replace("ø", "oe").replace("Ø", "oe")
                .replace("å", "aa").replace("Å", "aa")
                .replace("ü", "ue").replace("ö", "oe").replace("ä", "ae")
                // Anything not alphanumeric → underscore
                .replaceAll("[^a-zA-Z0-9]", "_")
                // Collapse runs of underscores
                .replaceAll("_+", "_")
                // Strip leading/trailing underscores
                .replaceAll("^_|_$", "")
                .toLowerCase();
    }

    // ── Extract a cell's value as a plain String ─────────────────────────────
    private static String getCellValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();

            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                double d = cell.getNumericCellValue();
                if (d == Math.floor(d) && !Double.isInfinite(d)) {
                    return String.valueOf((long) d);
                }
                return String.valueOf(d);

            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());

            case FORMULA:
                try {
                    return String.valueOf(cell.getNumericCellValue());
                } catch (Exception e) {
                    return cell.getStringCellValue().trim();
                }

            default:
                return null;
        }
    }

    // ── Add indexes on foreign-key columns for fast JOIN queries ─────────────
    private static void addForeignKeyIndexes(Connection conn) throws SQLException {
        System.out.println("\nCreating indexes...");
        String[] indexes = {
                "CREATE INDEX IF NOT EXISTS idx_data_foodid      ON data_normalised(foodid)",
                "CREATE INDEX IF NOT EXISTS idx_data_paramid     ON data_normalised(parameterid)",
                "CREATE INDEX IF NOT EXISTS idx_data_source      ON data_normalised(source)",
                "CREATE INDEX IF NOT EXISTS idx_food_groupid     ON food(foodgroupid)",
                "CREATE INDEX IF NOT EXISTS idx_food_name        ON food(foodname)",
        };
        try (Statement st = conn.createStatement()) {
            for (String sql : indexes) {
                st.execute(sql);
                System.out.println("  ✓ " + sql.replaceAll("CREATE INDEX IF NOT EXISTS ", ""));
            }
        }
    }
}
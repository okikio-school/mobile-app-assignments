package com.example.assignment2

import android.content.ContentValues
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

private const val DATABASE_NAME = "MAP"
private const val LOCATIONS_TABLE = "LOCATIONS"

private const val HIDDEN_COL = "rowid"
private const val COL_ID = "ID"
private const val COL_ADDR = "ADDRESS"
private const val COL_LONGITUDE = "LONGITUDE"
private const val COL_LATITUDE = "LATITUDE"

// Used to identify if the database has been seeded
private const val FLAGS_TABLE = "FLAGS"
private const val FLAG_KEY_COLUMN = "FLAG"
private const val FLAG_VALUE_COLUMN = "VALUE"
private val ADDRS_ABBRIVIATIONS = mapOf(
    // Directional Abbreviations
    "n" to "North",
    "s" to "South",
    "e" to "East",
    "w" to "West",
    "ne" to "Northeast",
    "nw" to "Northwest",
    "se" to "Southeast",
    "sw" to "Southwest",

    // Street Type Abbreviations
    "rd" to "Road",
    "st" to "Street",
    "ave" to "Avenue",
    "blvd" to "Boulevard",
    "dr" to "Drive",
    "pl" to "Place",
    "ct" to "Court",
    "ln" to "Lane",
    "pkwy" to "Parkway",
    "cir" to "Circle",
    "trl" to "Trail",
    "ter" to "Terrace",
    "hwy" to "Highway",
    "expwy" to "Expressway",
    "sq" to "Square",
    "cres" to "Crescent",
    "bypass" to "Bypass",
    "way" to "Way",
    "alcove" to "Alcove",
    "bay" to "Bay",
    "close" to "Close",
    "cove" to "Cove",
    "gate" to "Gate",
    "green" to "Green",
    "hill" to "Hill",
    "mews" to "Mews",
    "rise" to "Rise",
    "row" to "Row",
    "path" to "Path",
    "ramp" to "Ramp",
    "trail" to "Trail",

    // Building and Unit Abbreviations
    "bldg" to "Building",
    "ste" to "Suite",
    "apt" to "Apartment",
    "fl" to "Floor",
    "unit" to "Unit",
    "dept" to "Department",
    "po" to "Post Office",
    "rm" to "Room",
    "bsmt" to "Basement",
    "lobby" to "Lobby",
    "ph" to "Penthouse",

    // General Place Abbreviations
    "ctr" to "Center",
    "sq" to "Square",
    "plz" to "Plaza",
    "pk" to "Park",
    "mall" to "Mall",
    "harbour" to "Harbour",
    "landing" to "Landing",
    "terminal" to "Terminal"
)

/**
 * Reads and parses a CSV file from the res/raw folder.
 *
 * @param context The Android context to access resources.
 * @param fileName The resource ID of the CSV file (e.g., R.raw.data).
 * @return A list of rows, where each row is a list of strings representing columns.
 */
private fun readCsvFromRaw(context: Context, fileName: Int): List<List<String>> {
    val result = mutableListOf<List<String>>()
    // Open the resource as an InputStream
    val inputStream = context.resources.openRawResource(fileName)
    BufferedReader(InputStreamReader(inputStream)).use { reader ->
        // Read each line from the CSV
        reader.forEachLine { line ->
            // Split by commas and trim spaces
            val row = line.split(",").map { it.trim() }
            result.add(row)
        }
    }
    return result
}

class DatabaseHelper(private val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        // Create a virtual table to store the data required for full-text search
        // Note: virtual tables only support TEXT data types for columns,
        // in addition to the `rowid` column which acts as an autoincrement primary key integer
        db.execSQL("CREATE VIRTUAL TABLE IF NOT EXISTS $LOCATIONS_TABLE USING fts5($COL_ADDR, $COL_LONGITUDE, $COL_LATITUDE)");

        // Create a flags table to track if the database has been seeded
        db.execSQL("CREATE TABLE IF NOT EXISTS $FLAGS_TABLE ($FLAG_KEY_COLUMN TEXT PRIMARY KEY, $FLAG_VALUE_COLUMN TEXT)");

        // Check if the database is already seeded
        if (!isDatabaseSeeded(db)) {
            // Seed the database from the CSV file
            seedDatabaseFromCsv(db, R.raw.gta_locations)

            // Mark database as seeded
            flagDatabaseAsSeeded(db)
        }
    }

    /**
     * Checks if the database has already been seeded.
     *
     * @param db The writable database instance.
     * @return True if the database is already seeded, false otherwise.
     */
    private fun isDatabaseSeeded(db: SQLiteDatabase): Boolean {
        val cursor = db.rawQuery("SELECT $FLAG_VALUE_COLUMN FROM $FLAGS_TABLE WHERE $FLAG_KEY_COLUMN = 'seeded'", null)
        val isSeeded = cursor.moveToFirst() && cursor.getString(0) == "true"
        cursor.close()
        return isSeeded
    }

    /**
     * Marks the database as seeded by inserting a flag into the metadata table.
     *
     * @param db The writable database instance.
     */
    private fun flagDatabaseAsSeeded(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(FLAG_KEY_COLUMN, "seeded")
            put(FLAG_VALUE_COLUMN, "true")
        }
        db.insert(FLAGS_TABLE, null, values)
    }

    /**
    * Seeds the database with data from a CSV file in the res/raw directory.
    *
    * @param db The writable database instance to insert data into.
    * @param csvResourceId The resource ID of the CSV file (e.g., R.raw.gta_locations).
    */
    private fun seedDatabaseFromCsv(db: SQLiteDatabase, csvResourceId: Int) {
        val csvData = readCsvFromRaw(context, csvResourceId)
        // Skip the header row
        for (i in 1 until csvData.size) {
            val row = csvData[i]
            // Ensure the row has enough data fields (address, longitude, latitude)
            if (row.size >= 3) {
                val contentValues = ContentValues().apply {
                    put(COL_ADDR, expandAddrsAbbriviations(row[0])) // Expand address abbreviations
                    put(COL_LONGITUDE, row[1])
                    put(COL_LATITUDE, row[2])
                }
                db.insert(LOCATIONS_TABLE, null, contentValues)
            }
        }
    }

    /**
     * Expands abbreviations in the address based on the ADDRS_ABBRIVIATIONS map.
     *
     * @param address The raw address string with abbreviations.
     * @return The expanded address string.
     */
    fun expandAddrsAbbriviations(address: String): String {
        // Split the address into individual words
        val words = address.split(" ")
        val expandedWords = mutableListOf<String>()

        // Loop through each word to check for abbreviations
        for (word in words) {
            // Convert the word to lowercase for case-insensitive matching
            val lower = word.lowercase()

            // If the word is in ADDRS_ABBRIVIATIONS, replace it; otherwise, keep it as is
            val expandedWord = if (ADDRS_ABBRIVIATIONS.containsKey(lower)) {
                ADDRS_ABBRIVIATIONS[lower]!!
            } else {
                word
            }
            expandedWords.add(expandedWord)
        }

        // Join all expanded words back into a single string with spaces
        val resultAddress = expandedWords.joinToString(" ")
        return resultAddress
    }

    override fun onUpgrade(db: SQLiteDatabase, old: Int, new: Int) {
        db.execSQL("DROP TABLE IF EXISTS $LOCATIONS_TABLE");
        db.execSQL("DROP TABLE IF EXISTS $FLAGS_TABLE");
        onCreate(db);
    }

    fun listData(): MutableList<Map<String, Any>> {
        return query("SELECT $HIDDEN_COL as $COL_ID, * FROM $LOCATIONS_TABLE")
    }

    fun getData(id: String): MutableList<Map<String, Any>> {
        return query("SELECT $HIDDEN_COL as $COL_ID, * FROM $LOCATIONS_TABLE WHERE $COL_ID = ?", arrayOf(id))
    }

    // Search query using the MATCH operator to perform full-text search
    fun searchData(queryString: String): MutableList<Map<String, Any>> {
        val unabbriviatedQuery = expandAddrsAbbriviations(queryString);

        // Expand the search query to include wildcard characters this should allow for better partial matching
        val expansiveSearch = "%" + queryString.replace(Regex("\\s+"), "%") + "%"
        val unabbriviatedSearch = "%" + unabbriviatedQuery.replace(Regex("\\s+"), "%") + "%"
        println("Search query: $queryString -> $expansiveSearch -> $unabbriviatedQuery -> $unabbriviatedSearch")

        return query("SELECT $HIDDEN_COL as $COL_ID, * FROM $LOCATIONS_TABLE WHERE $COL_ADDR LIKE ? OR $COL_ADDR LIKE ? OR $COL_LONGITUDE LIKE ? OR $COL_LATITUDE LIKE ?",
            arrayOf(expansiveSearch, unabbriviatedSearch, expansiveSearch, expansiveSearch))
    }

    private fun query(query: String, params: Array<String> = emptyArray()): MutableList<Map<String, Any>> {
        val locationList = mutableListOf<Map<String, Any>>()
        val db = writableDatabase

        val cursor = db.rawQuery(query, params)
        if (cursor.moveToFirst()) {
            do {
                val location = mapOf(
                    "id" to cursor.getInt(cursor.getColumnIndexOrThrow(COL_ID)),
                    "address" to cursor.getString(cursor.getColumnIndexOrThrow(COL_ADDR)),
                    "longitude" to cursor.getString(cursor.getColumnIndexOrThrow(COL_LONGITUDE)),
                    "latitude" to cursor.getString(cursor.getColumnIndexOrThrow(COL_LATITUDE)),
                )
                locationList.add(location)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return locationList
    }

    fun insertData(addr: String, longitude: String, latitude: String): Long {
        if (addr.isEmpty()) return -1L;

        val db = writableDatabase;
        val values = ContentValues();
        values.put(COL_ADDR, expandAddrsAbbriviations(addr));
        values.put(COL_LONGITUDE, longitude);
        values.put(COL_LATITUDE, latitude);

        val insertion = db.insert(LOCATIONS_TABLE, null, values);
        return insertion;
    }

    fun updateData(id: String, addr: String, longitude: String, latitude: String): Boolean {
        if (addr.isEmpty()) return false;

        val db = writableDatabase;
        val values = ContentValues();
        values.put(COL_ADDR, addr);
        values.put(COL_LONGITUDE, longitude);
        values.put(COL_LATITUDE, latitude);

        val insertion = db.update(LOCATIONS_TABLE, values, "$HIDDEN_COL = ?", arrayOf(id));
        return insertion > 0;
    }

    fun deleteData(id: String): Boolean {
        val db = writableDatabase;
        val deletion = db.delete(LOCATIONS_TABLE, "$HIDDEN_COL = ?", arrayOf(id));
        return deletion > 0;
    }

}
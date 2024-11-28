import 'dart:io';

import 'package:path/path.dart';
import 'package:sqflite/sqflite.dart';
import 'package:path/path.dart'; // For file path utilities

class DatabaseService {
  static final DatabaseService _instance = DatabaseService._constructor();
  static Database? _db;

  DatabaseService._constructor();

  static DatabaseService get instance => _instance;

  final String _scheduleTableName = "schedule";
  final String _scheduleId = "ID";
  final String _scheduleItemId = "ITEM_ID";
  final String _scheduleCost = "TARGET_COST_PER_DAY";
  final String _scheduleDate = "DATE";

  final String _foodItemsTableName = "food_items";
  final String _foodItemId = "ID";
  final String _foodItemTitle = "TITLE";
  final String _foodItemCost = "COST";

  Future<Database> get database async {
    if (_db != null) {
      return _db!;
    }
    _db = await getDatabase();
    return _db!;
  }

  Future<Database> getDatabase() async {
    final databaseDirPath = await getDatabasesPath();
    final databasePath = join(databaseDirPath, "notes_db.db");
    final database = await openDatabase(
      databasePath,
      version: 2,
      onCreate: (db, version) async {
        final String createScheduleTable = '''
            CREATE TABLE $_scheduleTableName (
              $_scheduleId INTEGER PRIMARY KEY AUTOINCREMENT,
              $_scheduleItemId INTEGER NOT NULL,
              $_scheduleCost TEXT,
              $_scheduleDate INTEGER,
              FOREIGN KEY ($_scheduleItemId) REFERENCES $_foodItemsTableName ($_foodItemId)
                ON DELETE CASCADE
                ON UPDATE CASCADE
            )
          ''';
        final String createFoodItemsTable = '''
            CREATE TABLE $_foodItemsTableName (
              $_foodItemId INTEGER PRIMARY KEY AUTOINCREMENT,
              $_foodItemTitle TEXT NOT NULL,
              $_foodItemCost TEXT,
            )
          ''';

        await db.execute(createFoodItemsTable); // Create the referenced table first
        await db.execute(createScheduleTable); // Create the referencing table next

        await importCsvToFoodItems('assets/food_items.csv');
      },
      onUpgrade: (db, oldVersion, newVersion) {
        if (oldVersion < 3) {
          // db.execute("ALTER TABLE notes ADD COLUMN IMAGE TEXT");
        }
      },
    );
    return database;
  }

  Future<void> importCsvToFoodItems(String csvFilePath) async {
    // Get database instance
    final db = await database;

    // Open the CSV file
    final file = File(csvFilePath);

    // Check if file exists
    if (!await file.exists()) {
      throw Exception("CSV file not found at $csvFilePath");
    }

    // Read the CSV file line by line
    final lines = await file.readAsLines();

    // Skip the header line and parse the remaining rows
    for (int i = 1; i < lines.length; i++) {
      final line = lines[i];
      final values = line.split(','); // Assumes CSV is comma-separated

      // Ensure each line has the correct number of columns
      if (values.length < 2) {
        throw Exception("Invalid data at line ${i + 1}: $line");
      }

      // Extract values
      final title = values[0].trim();
      final cost = values[1].trim();

      // Insert into the database
      await db.insert(
        _foodItemsTableName,
        {
          _foodItemTitle: title,
          _foodItemCost: cost,
        },
        conflictAlgorithm: ConflictAlgorithm.ignore, // Avoid duplicates
      );
    }
  }

  Future<void> addSchedule(
      int? itemId,
      String cost,
      int date,
  ) async {
    final db = await database;
    await db.insert(_scheduleTableName, {
      _scheduleItemId: itemId,
      _scheduleCost: cost,
      _scheduleDate: date
    });
  }

  Future<void> updateSchedule(
      int id,
      int? itemId,
      String cost,
      int date,
  ) async {
    final db = await database;
    await db.update(
      _scheduleTableName,
      {
        _scheduleItemId: itemId,
        _scheduleCost: cost,
        _scheduleDate: date
      },
      where: '$_scheduleId = ?',
      whereArgs: [id],
    );
  }

  Future<List<Map<String, dynamic>>> fetchAllScheduledMeals() async {
    final db = await database;
    // Query with a join to fetch the scheduled meals with their food item titles.
    return await db.rawQuery('''
      SELECT 
        $_scheduleTableName.$_scheduleId,
        $_scheduleTableName.$_scheduleCost,
        $_scheduleTableName.$_scheduleDate,
        $_foodItemsTableName.$_foodItemTitle
      FROM $_scheduleTableName
      INNER JOIN $_foodItemsTableName
      ON $_scheduleTableName.$_scheduleItemId = $_foodItemsTableName.$_foodItemId
    ''');
  }

  Future<List<Map<String, dynamic>>> fetchAllFoodItems() async {
    final db = await database;
    return await db.query(_foodItemsTableName);
  }

  Future<List<Map<String, dynamic>>> searchScheduleByDate(DateTime date) async {
    final db = await database;

    // Convert the input date to a Unix timestamp for comparison.
    final startOfDay = DateTime(date.year, date.month, date.day).millisecondsSinceEpoch;
    final endOfDay = DateTime(date.year, date.month, date.day, 23, 59, 59).millisecondsSinceEpoch;

    // Query with a join to fetch the scheduled meals with their food item titles.
    return await db.rawQuery('''
      SELECT 
        $_scheduleTableName.$_scheduleId,
        $_scheduleTableName.$_scheduleCost,
        $_scheduleTableName.$_scheduleDate,
        $_foodItemsTableName.$_foodItemTitle
      FROM $_scheduleTableName
      INNER JOIN $_foodItemsTableName
      ON $_scheduleTableName.$_scheduleItemId = $_foodItemsTableName.$_foodItemId
      WHERE $_scheduleTableName.$_scheduleDate BETWEEN ? AND ?
    ''', [startOfDay, endOfDay]);
  }

  Future<void> deleteScheduledMealById(int? id) async {
    if (id == null) return; // Do nothing if id is null
    final db = await database;
    await db.delete(
      _scheduleTableName,
      where: '$_scheduleId = ?',
      whereArgs: [id],
    );
  }
}

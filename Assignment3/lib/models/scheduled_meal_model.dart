class ScheduledMeal {
  final int? id;
  final int itemId;
  final String cost;
  final int date;
  final String? itemName;

  ScheduledMeal({
    this.id,
    required this.itemId,
    required this.cost,
    required this.date,
    this.itemName,
  });

  // fromMap method to convert a map to a Note object
  factory ScheduledMeal.fromMap(Map<String, dynamic> map) {
    return ScheduledMeal(
      id: map['ID'] as int?,
      itemId: map['ITEM_ID'] as int,
      itemName: map['TITLE'] as String,
      cost: map['TARGET_COST_PER_DAY'] as String,
      date: map['DATE'] as int,
    );
  }
}

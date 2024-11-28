import 'package:flutter/material.dart';
import 'package:note_me_v2/services/database_service.dart';

class FoodItemsDropdown extends StatefulWidget {
  final Function(DateTime) onItemSelected;
  const FoodItemsDropdown({
    super.key,
    required this.onItemSelected
  });

  @override
  FoodItemsDropdownState createState() => FoodItemsDropdownState();
}

class FoodItemsDropdownState extends State<FoodItemsDropdown> {
  List<Map<String, dynamic>> _foodItems = [];
  String? _selectedFoodItem; // To store the selected food item

  @override
  void initState() {
    super.initState();
    _loadFoodItems();
  }

  Future<void> _loadFoodItems() async {
    try {
      final items = await DatabaseService.instance.fetchAllFoodItems(); // Fetch food items
      setState(() {
        _foodItems = items;
      });
    } catch (e) {
      print("Error fetching food items: $e");
    }
  }

  @override
  Widget build(BuildContext context) {
    return DropdownButton<String>(
      hint: const Text("Select a food item"),
      value: _selectedFoodItem,
      onChanged: (String? newValue) {
        setState(() {
          // Food Item ID is stored in the value
          // technically this should be an integer but
          // since we are storing it as a string in the dropdown
          // it will be easier just to store it as a string here
          _selectedFoodItem = newValue;
        });
      },
      items: _foodItems.map<DropdownMenuItem<String>>((item) {
        return DropdownMenuItem<String>(
          value: item['ID'].toString(), // Store the ID as the value
          child: Text("${item['TITLE']} -- \$${item['COST']}"), // Display the title in the dropdown
        );
      }).toList(),
    );
  }
}
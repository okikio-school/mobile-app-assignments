import 'dart:io';
import 'package:flutter/material.dart';
import 'package:note_me_v2/screens/widgets/fooditems_view.dart';
import 'package:note_me_v2/services/database_service.dart';
import '../models/scheduled_meal_model.dart';

class ScheduledMealView extends StatefulWidget {
  const ScheduledMealView({
    super.key,
    required this.schedule,
    required this.onScheduleUpdated,
    required this.onScheduledDeleted,
  });

  final ScheduledMeal schedule;
  final Function(ScheduledMeal) onScheduleUpdated;
  final Function(ScheduledMeal) onScheduledDeleted;

  @override
  State<ScheduledMealView> createState() => _ScheduledMealViewState();
}

class _ScheduledMealViewState extends State<ScheduledMealView> {
  late int itemId;
  late TextEditingController dateController;
  late TextEditingController costPerDay;
  late String? foodItemTitle;

  @override
  void initState() {
    super.initState();
    itemId = widget.schedule.itemId;
    dateController = TextEditingController(text: widget.schedule.date);
    costPerDay = TextEditingController(text: widget.schedule.cost);
    foodItemTitle = widget.schedule.itemName ?? "Choose a food item";
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text(widget.schedule.itemName ?? "No food item selected"),
        actions: [
          IconButton(
              onPressed: () {
                showDialog(
                    context: context,
                    builder: (context) {
                      return AlertDialog(
                        title: const Text("Are you sure?"),
                        content:
                        Text("Scheduled Meal with food item '${widget.schedule.itemName ?? "Empty Food Item"}' will be deleted"),
                        actions: [
                          TextButton(
                              onPressed: () {
                                Navigator.of(context).pop();
                              },
                              child: const Text("Cancel")),
                          TextButton(
                              onPressed: () {
                                Navigator.of(context).pop();
                                DatabaseService.instance
                                    .deleteScheduledMealById(widget.schedule.id!);
                                widget.onScheduledDeleted(widget.schedule);
                                if (Navigator.of(context).canPop()) {
                                  Navigator.of(context).pop();
                                }
                              },
                              child: const Text("Delete")),
                        ],
                      );
                    });
              },
              icon: const Icon(Icons.delete))
        ],
      ),
      body: Container(
        child: Padding(
          padding: const EdgeInsets.all(15.0),
          child: Column(
            children: [
              FoodItemsDropdown(),
              TextFormField(
                controller: dateController,
                minLines: 5,
                maxLines: null,
                keyboardType: TextInputType.multiline,
                style: const TextStyle(fontSize: 18),
                decoration: const InputDecoration(
                    border: InputBorder.none, hintText: "Note"),
              ),
              const SizedBox(height: 20),
              Row(
                children: [
                ],
              ),
            ],
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
          onPressed: () {
            // Check if the title or content is empty
            if (dateController.text.isEmpty) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                    content: Text("Date must be provided.")),
              );
              return;
            }

            // Create the note object
            final note = ScheduledMeal(
              itemId: itemId,
              date: DateTime(dateController.text),
              cost: costPerDay.text,
            );

            // Save the note to the database and await the operation
            DatabaseService.instance.updateSchedule(
              widget.schedule.id!,
              itemId,
              costPerDay.text,
              DateTime(dateController.text),
            );

            // Notify HomeScreen and close the NewNotesScreen
            widget.onScheduleUpdated(note);
            if (Navigator.of(context).canPop()) {
              Navigator.of(context).pop();
            }
          },
          child: Icon(Icons.save)),
    );
  }
}

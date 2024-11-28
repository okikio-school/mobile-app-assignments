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
                        Text("Scheduled Meal with food item '${widget.schedule.foodItemTitle}' will be deleted"),
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
              FoodItemsDropdown(
                controller: itemId,
              ),
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
                  if (imagePath != null)
                    Image.file(
                      File(imagePath!),
                      width: 200,
                      height: 200,
                      fit: BoxFit.cover,
                    ),
                ],
              ),
            ],
          ),
        ),
      ),
      persistentFooterButtons: [
        Row(
          children: [
            Row(
              children: [
                const Icon(Icons.colorize),
                GestureDetector(
                  onTap: () async {
                    Color? color = await showDialog(
                      context: context,
                      builder: (context) => AlertDialog(
                        title: const Text('Select a Color'),
                        content: SingleChildScrollView(
                          child: Wrap(
                            spacing: 10,
                            runSpacing: 10,
                            children: [
                              Colors.red,
                              Colors.green,
                              Colors.blue,
                              Colors.yellow,
                              Colors.orange,
                              Colors.purple,
                              Colors.pink,
                              Colors.brown,
                              Colors.amber,
                              Colors.lightBlue,
                              Colors.lightGreen,
                              Colors.blueGrey,
                            ]
                                .map((color) => GestureDetector(
                              onTap: () =>
                                  Navigator.pop(context, color),
                              child: Container(
                                width: 25,
                                height: 25,
                                decoration: BoxDecoration(
                                  color: color,
                                  border: Border.all(),
                                  borderRadius:
                                  BorderRadius.circular(50),
                                ),
                              ),
                            ))
                                .toList(),
                          ),
                        ),
                      ),
                    );
                    if (color != null) {
                      setState(() => selectedColor = color);
                    }
                  },
                  child: Container(
                    width: 25,
                    height: 25,
                    decoration: BoxDecoration(
                        color: selectedColor,
                        border: Border.all(
                          color: Colors.white,
                          width: 2,
                        ),
                        borderRadius: BorderRadius.circular(50)),
                  ),
                ),
              ],
            ),
            IconButton(
              icon: const Icon(Icons.add_a_photo),
              onPressed: pickImage,
            ),
          ],
        )
      ],
      floatingActionButton: FloatingActionButton(
          onPressed: () {
            // Check if the title or content is empty
            if (itemId.text.isEmpty &&
                dateController.text.isEmpty) {
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(
                    content: Text("Title or content must be provided.")),
              );
              return;
            }

            // Create the note object
            final note = ScheduledMeal(
              title: itemId.text,
              content: dateController.text,
              color: selectedColor.value,
              image: imagePath,
            );

            // Save the note to the database and await the operation
            DatabaseService.instance.updateSchedule(
              widget.schedule.id!,
              itemId.text,
              dateController.text,
              selectedColor.value,
              imagePath,
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

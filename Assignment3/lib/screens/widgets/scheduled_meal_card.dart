
import 'dart:io';

import 'package:flutter/material.dart';

import '../../models/scheduled_meal_model.dart';
import '../scheduled_meal_view.dart';

class ScheduledMealCard extends StatelessWidget {
  const ScheduledMealCard(
      {super.key,
        required this.scheduledmeal,
        required this.index,
        required this.onScheduleUpdated,
        required this.onScheduleDeleted});

  final ScheduledMeal scheduledmeal;
  final int index;
  final Function(ScheduledMeal) onScheduleUpdated;
  final Function(ScheduledMeal) onScheduleDeleted;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: () {
        Navigator.of(context).push(MaterialPageRoute(
          builder: (context) => ScheduledMealView(
            schedule: scheduledmeal,
            onScheduleUpdated: (updatedNote) {
              onScheduleUpdated(updatedNote); // Pass the updated note back
              Navigator.of(context).pop();
            },
            onScheduledDeleted: (deletedNote) {
              onScheduleDeleted(deletedNote); // Pass the deleted note back
              Navigator.of(context).pop();
            },
          ),
        ));
      },
      child: Card(
        child: Padding(
          padding: const EdgeInsets.all(10),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                DateTime(scheduledmeal.date).toString(),
                style: const TextStyle(
                  fontSize: 20,
                ),
              ),
              const SizedBox(
                height: 10,
              ),
              Text(
                scheduledmeal.itemName ?? "Missing food item",
                style: const TextStyle(
                  fontSize: 16,
                ),
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
              ),
              Text(
                "\$${scheduledmeal.cost}",
                style: const TextStyle(
                  fontSize: 16,
                ),
                maxLines: 3,
                overflow: TextOverflow.ellipsis,
              ),
            ],
          ),
        ),
      ),
    );
  }
}

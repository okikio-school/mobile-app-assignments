import 'package:flutter/material.dart';

class DatePicker extends StatefulWidget {
  final Function(DateTime) onDateSelected;

  const DatePicker({
    super.key,
    required this.onDateSelected
  });

  @override
  DatePickerState createState() => DatePickerState();
}

class DatePickerState extends State<DatePicker> {
  DateTime? _selectedDate;

  Future<void> _pickDate(BuildContext context) async {
    // Open the date picker dialog
    final DateTime? pickedDate = await showDatePicker(
      context: context,
      initialDate: _selectedDate ?? DateTime.now(), // Default date
      firstDate: DateTime(2000), // Earliest selectable date
      lastDate: DateTime(2100), // Latest selectable date
    );

    // Update state with the selected date
    if (pickedDate != null && pickedDate != _selectedDate) {
      setState(() {
        _selectedDate = pickedDate;
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisAlignment: MainAxisAlignment.center,
      children: [
        // Display the selected date or a placeholder text
        Text(
          _selectedDate != null
              ? "Selected Date: ${_selectedDate!.toLocal().toString().split(' ')[0]}"
              : "No Date Selected",
          style: TextStyle(fontSize: 18),
        ),
        const SizedBox(height: 20),
        ElevatedButton(
          onPressed: () => _pickDate(context), // Open date picker
          child: const Text("Pick a Date"),
        ),
      ],
    );
  }
}

void main() {
  runApp(MaterialApp(
    home: DatePicker(),
  ));
}

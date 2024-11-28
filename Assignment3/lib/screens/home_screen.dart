import 'package:flutter/material.dart';
import 'package:note_me_v2/screens/widgets/scheduled_meal_card.dart';
import '../models/scheduled_meal_model.dart';
import '../services/database_service.dart';
import 'new_notes_screen.dart';

class HomeScreen extends StatefulWidget {
  const HomeScreen({super.key});

  @override
  State<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends State<HomeScreen> {
  final DatabaseService _databaseService = DatabaseService.instance;

  List<ScheduledMeal> schedules = [];
  String searchQuery = "";

  @override
  void initState() {
    super.initState();
    fetchNotes();
  }

  Future<void> fetchNotes() async {
    final data = await _databaseService.fetchAllScheduledMeals();
    setState(() {
      schedules = data.map((e) => ScheduledMeal.fromMap(e)).toList();
    });
  }

  Future<void> searchDates(DateTime query) async {
    final schedulesData = await _databaseService.searchScheduleByDate(query);
    setState(() {
      schedules = schedulesData.map((e) => ScheduledMeal.fromMap(e)).toList();
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Container(
          decoration: BoxDecoration(
              color: const Color.fromARGB(57, 0, 0, 0),
              borderRadius: BorderRadius.circular(50)),
          child: Padding(
            padding: EdgeInsets.fromLTRB(12, 2, 12, 2),
            child: TextField(
              decoration: InputDecoration(
                icon: Icon(Icons.search),
                hintText: "Search your notes",
                border: InputBorder.none,
              ),
              onChanged: (query) {
                setState(() {
                  searchQuery = query;
                });
                searchDates(query);
              },
            ),
          ),
        ),
      ),
      body: Center(
        child: schedules.isEmpty
            ? const Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              'Notes you add appear here',
            ),
          ],
        )
            : Padding(
          padding: const EdgeInsets.all(15.0),
          child: ListView.builder(
            itemCount: schedules.length,
            itemBuilder: (context, index) {
              return ScheduledMealCard(
                scheduledmeal: schedules[index],
                index: index,
                onScheduleUpdated: (updatedNote) async {
                  await fetchNotes();
                },
                onScheduleDeleted: (deletedNote) async {
                  await _databaseService.deleteScheduledMealById(deletedNote.id);
                  await fetchNotes();
                },
              );
            },
          ),
        ),
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.of(context).push(MaterialPageRoute(
              builder: (context) => NewNotesScreen(
                onNewNoteCreated: (note) async {
                  await fetchNotes();
                },
              )));
        },
        child: const Icon(Icons.add),
      ),
    );
  }

  void onNoteDeleted(ScheduledMeal deletedNote) {
    setState(() {
      schedules.removeWhere((note) => note.id == deletedNote.id);
    });
    _databaseService.deleteScheduledMealById(deletedNote.id!);
  }
}

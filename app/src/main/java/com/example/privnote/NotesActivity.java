package com.example.privnote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotes;
    private FloatingActionButton fabAdd;
    private NoteAdapter noteAdapter;
    private ArrayList<Note> notesList = new ArrayList<>();

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyNotesPrefs";
    private static final String NOTES_KEY = "notes";
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        findViews();
        loadNotes();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes(); // Refresh the list when returning from detail activity
    }

    private void findViews() {
        recyclerViewNotes = findViewById(R.id.recyclerViewNotes);
        fabAdd = findViewById(R.id.fabAdd);
    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddNoteDialog());
    }

    private void loadNotes() {
        notesList.clear();

        // Check if we have old format data (StringSet) and migrate it
        if (sharedPreferences.contains(NOTES_KEY)) {
            try {
                // Try to read as new format (JSON String)
                String notesJson = sharedPreferences.getString(NOTES_KEY, "[]");
                Type listType = new TypeToken<List<Note>>(){}.getType();
                List<Note> loadedNotes = gson.fromJson(notesJson, listType);

                if (loadedNotes != null) {
                    notesList.addAll(loadedNotes);
                }
            } catch (ClassCastException e) {
                // Old format detected - migrate from StringSet to new format
                Set<String> oldNotes = sharedPreferences.getStringSet(NOTES_KEY, new HashSet<>());
                for (String oldNote : oldNotes) {
                    notesList.add(new Note(oldNote));
                }
                saveNotes();
            }
        }

        if (noteAdapter == null) {
            noteAdapter = new NoteAdapter(this, notesList);
            recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewNotes.setAdapter(noteAdapter);
        } else {
            noteAdapter.notifyDataSetChanged();
        }
    }

    public void saveNotes() {
        String notesJson = gson.toJson(notesList);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NOTES_KEY, notesJson);
        editor.apply();

        if (noteAdapter != null) {
            noteAdapter.notifyDataSetChanged();
        }
    }


    private void showAddNoteDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("New Note")
                .setMessage("Enter note title:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String noteTitle = input.getText().toString().trim();
                    if (!noteTitle.isEmpty()) {
                        notesList.add(new Note(noteTitle));
                        saveNotes();
                    } else {
                        Toast.makeText(this, "Note title can't be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void deleteNote(int position) {
        if (position >= 0 && position < notesList.size()) {
            notesList.remove(position);
            saveNotes();
        }
    }
}
package com.example.privnote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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


public class NotesActivity extends AppCompatActivity {

    private RecyclerView recyclerViewNotes;
    private TextView textViewEmptyState;
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
        textViewEmptyState = findViewById(R.id.textViewEmptyState);

    }

    private void setupListeners() {
        fabAdd.setOnClickListener(v -> showAddNoteDialog());
    }

    public void loadNotes() {
        String encryptedJson = sharedPreferences.getString(NOTES_KEY, null);
        if (encryptedJson != null) {
            Type type = new TypeToken<ArrayList<String>>() {}.getType();
            ArrayList<String> encryptedNotes = gson.fromJson(encryptedJson, type);
            notesList.clear();

            for (String encryptedNote : encryptedNotes) {
                String decrypted = CryptoNotes.decrypt(encryptedNote);
                if (decrypted != null) {
                    try {
                        Note note = gson.fromJson(decrypted, Note.class);
                        if (note != null) {
                            notesList.add(note);
                        }
                    } catch (Exception e) {
                        e.printStackTrace(); // safe catch to prevent crash on bad data
                    }
                }
            }
        }

        if (noteAdapter == null) {
            noteAdapter = new NoteAdapter(this, notesList);
            recyclerViewNotes.setLayoutManager(new LinearLayoutManager(this));
            recyclerViewNotes.setAdapter(noteAdapter);
        } else {
            noteAdapter.notifyDataSetChanged();
        }

        updateEmptyState();
    }

    public void saveNotes() {
        ArrayList<String> encryptedNotes = new ArrayList<>();
        for (Note note : notesList) {
            String encrypted = CryptoNotes.encrypt(gson.toJson(note));
            if (encrypted != null) {
                encryptedNotes.add(encrypted);
            }
        }

        String encryptedJson = gson.toJson(encryptedNotes);
        sharedPreferences.edit().putString(NOTES_KEY, encryptedJson).apply();

        if (noteAdapter != null) {
            noteAdapter.notifyDataSetChanged();
        }

        updateEmptyState();
    }

    private void updateEmptyState() {
        if (notesList.isEmpty()) {
            textViewEmptyState.setVisibility(View.VISIBLE);
            recyclerViewNotes.setVisibility(View.GONE);
        } else {
            textViewEmptyState.setVisibility(View.GONE);
            recyclerViewNotes.setVisibility(View.VISIBLE);
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
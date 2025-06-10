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
import java.util.List;

public class NoteDetailActivity extends AppCompatActivity {

    private RecyclerView recyclerViewItems;
    private FloatingActionButton fabAddItem;
    private NoteItemAdapter itemAdapter;
    private Note currentNote;
    private int noteIndex;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyNotesPrefs";
    private static final String NOTES_KEY = "notes";
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();

        noteIndex = getIntent().getIntExtra("note_index", -1);
        if (noteIndex == -1) {
            finish();
            return;
        }

        loadNote();
        findViews();
        setupRecyclerView();
        setupListeners();
    }

    private void findViews() {
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        fabAddItem = findViewById(R.id.fabAddItem);
    }

    private void loadNote() {
        String notesJson = sharedPreferences.getString(NOTES_KEY, "[]");
        Type listType = new TypeToken<List<Note>>(){}.getType();
        List<Note> notesList = gson.fromJson(notesJson, listType);

        if (notesList != null && noteIndex < notesList.size()) {
            currentNote = notesList.get(noteIndex);
            setTitle(currentNote.getTitle());
        } else {
            finish();
        }
    }

    private void setupRecyclerView() {
        itemAdapter = new NoteItemAdapter(this, currentNote.getItems());
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItems.setAdapter(itemAdapter);
    }

    private void setupListeners() {
        fabAddItem.setOnClickListener(v -> showAddItemDialog());
    }

    private void showAddItemDialog() {
        final android.widget.EditText input = new android.widget.EditText(this);
        new AlertDialog.Builder(this)
                .setTitle("Add Item")
                .setMessage("Enter item content:")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String itemContent = input.getText().toString().trim();
                    if (!itemContent.isEmpty()) {
                        currentNote.addItem(itemContent);
                        itemAdapter.notifyDataSetChanged();
                        saveNote();
                    } else {
                        Toast.makeText(this, "Item content can't be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    public void deleteItem(int position) {
        currentNote.removeItem(position);
        itemAdapter.notifyItemRemoved(position);
        itemAdapter.notifyItemRangeChanged(position, currentNote.getItems().size());
        saveNote();
    }

    private void saveNote() {
        String notesJson = sharedPreferences.getString(NOTES_KEY, "[]");
        Type listType = new TypeToken<List<Note>>(){}.getType();
        List<Note> notesList = gson.fromJson(notesJson, listType);

        if (notesList != null && noteIndex < notesList.size()) {
            notesList.set(noteIndex, currentNote);
            String updatedJson = gson.toJson(notesList);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(NOTES_KEY, updatedJson);
            editor.apply();
        }
    }
}
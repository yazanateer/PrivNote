package com.example.privnote;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class NoteDetailActivity extends AppCompatActivity {

    private EditText editTextNoteContent;
    private Note currentNote;
    private int noteIndex;

    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "MyNotesPrefs";
    private static final String NOTES_KEY = "notes";
    private Gson gson;

    private Handler saveHandler = new Handler();
    private Runnable saveRunnable;

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
        setupListeners();
        displayNoteContent();


        ImageButton buttonBack = findViewById(R.id.buttonBack);
        buttonBack.setOnClickListener(v -> finish()); // Finish current activity to go back
    }

    private void findViews() {
        editTextNoteContent = findViewById(R.id.editTextNoteContent);
    }

//private void loadNote() {
//    String encryptedJson = sharedPreferences.getString(NOTES_KEY, null);
//    if (encryptedJson == null) {
//        finish();
//        return;
//    }
//
//    Type listType = new TypeToken<List<String>>() {}.getType();
//    List<String> encryptedNotes = gson.fromJson(encryptedJson, listType);
//
//    if (encryptedNotes != null && noteIndex < encryptedNotes.size()) {
//        String decrypted = CryptoNotes.decrypt(encryptedNotes.get(noteIndex));
//        if (decrypted != null) {
//            currentNote = gson.fromJson(decrypted, Note.class);
//            setTitle(currentNote.getTitle());
//        } else {
//            finish();
//        }
//    } else {
//        finish();
//    }
//}

    private void loadNote() {
        String encryptedJson = sharedPreferences.getString(NOTES_KEY, null);
        if (encryptedJson == null) {
            Log.e("PrivNote", "No notes found in SharedPreferences.");
            finish();
            return;
        }

        Log.d("PrivNote", "Full encrypted notes JSON:\n" + encryptedJson);

        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> encryptedNotes = gson.fromJson(encryptedJson, listType);

        if (encryptedNotes != null && noteIndex < encryptedNotes.size()) {
            String encryptedNote = encryptedNotes.get(noteIndex);
            Log.d("PrivNote", "Encrypted note at index " + noteIndex + ":\n" + encryptedNote);

            String decrypted = CryptoNotes.decrypt(encryptedNote);
            Log.d("PrivNote", "Decrypted JSON:\n" + decrypted);

            if (decrypted != null) {
                currentNote = gson.fromJson(decrypted, Note.class);
                Log.d("PrivNote", "Parsed Note Title: " + currentNote.getTitle());
                setTitle(currentNote.getTitle());
            } else {
                Log.e("PrivNote", "Decryption returned null");
                finish();
            }
        } else {
            Log.e("PrivNote", "Invalid noteIndex or empty encryptedNotes list.");
            finish();
        }
    }


    private void displayNoteContent() {
        // Convert the items list to a single text (each item on new line)
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < currentNote.getItems().size(); i++) {
            content.append(currentNote.getItems().get(i));
            if (i < currentNote.getItems().size() - 1) {
                content.append("\n");
            }
        }
        editTextNoteContent.setText(content.toString());

        // Set cursor to end
        editTextNoteContent.setSelection(editTextNoteContent.getText().length());
    }

    private void setupListeners() {
        // Auto-save with delay to avoid too frequent saves
        editTextNoteContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // Cancel previous save
                if (saveRunnable != null) {
                    saveHandler.removeCallbacks(saveRunnable);
                }

                // Schedule new save after 1 second delay
                saveRunnable = () -> saveNoteContent();
                saveHandler.postDelayed(saveRunnable, 1000);
            }
        });
    }

    private void saveNoteContent() {
        String content = editTextNoteContent.getText().toString();
        currentNote.setContent(content); // Syncs items

        Log.d("PrivNote", "Original content:\n" + content);

        String noteJson = gson.toJson(currentNote);
        Log.d("PrivNote", "Note as JSON:\n" + noteJson);

        String newEncrypted = CryptoNotes.encrypt(noteJson);
        Log.d("PrivNote", "Encrypted note:\n" + newEncrypted);

        String encryptedJson = sharedPreferences.getString(NOTES_KEY, null);
        if (encryptedJson == null) return;

        Type listType = new TypeToken<List<String>>() {}.getType();
        List<String> encryptedNotes = gson.fromJson(encryptedJson, listType);

        if (encryptedNotes != null && noteIndex < encryptedNotes.size()) {
            encryptedNotes.set(noteIndex, newEncrypted);
            String updatedJson = gson.toJson(encryptedNotes);
            sharedPreferences.edit().putString(NOTES_KEY, updatedJson).apply();
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        // Cancel any pending saves and save immediately
        if (saveRunnable != null) {
            saveHandler.removeCallbacks(saveRunnable);
        }
        saveNoteContent();
    }

    public void deleteItem(int position) {
        // Example logic to remove item from the list and notify the adapter
        // Optional: Update SharedPreferences or storage
    }
}

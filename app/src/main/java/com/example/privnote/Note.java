package com.example.privnote;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private String title;
    private List<String> items;

    public Note(String title) {
        this.title = title;
        this.items = new ArrayList<>();
    }

    public Note(String title, List<String> items) {
        this.title = title;
        this.items = items != null ? items : new ArrayList<>();
    }

    // Getters and setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) { this.items = items; }

    public void addItem(String item) {
        this.items.add(item);
    }

    public void removeItem(int index) {
        if (index >= 0 && index < items.size()) {
            this.items.remove(index);
        }
    }
}
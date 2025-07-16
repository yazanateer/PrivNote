package com.example.privnote;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private String title;
    private List<String> items;
    private String content;

    public Note(String title) {
        this.title = title;
        this.items = new ArrayList<>();
        this.content = "";
    }

    public Note(String title, List<String> items) {
        this.title = title;
        this.items = items != null ? items : new ArrayList<>();
        this.content = "";
    }

    public Note(String title, String content) {
        this.title = title;
        this.content = content != null ? content : "";
        this.items = new ArrayList<>();
        // Convert content to items (split by lines)
        if (!this.content.isEmpty()) {
            String[] lines = this.content.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    this.items.add(line);
                }
            }
        }
    }


    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public List<String> getItems() { return items; }
    public void setItems(List<String> items) {
        this.items = items;
        updateContentFromItems();
    }

    public String getContent() {
        updateContentFromItems();
        return content;
    }

    public void setContent(String content) {
        this.content = content != null ? content : "";
        updateItemsFromContent();
    }

    private void updateContentFromItems() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(items.get(i));
            if (i < items.size() - 1) {
                sb.append("\n");
            }
        }
        this.content = sb.toString();
    }

    private void updateItemsFromContent() {
        this.items.clear();
        if (content != null && !content.trim().isEmpty()) {
            String[] lines = content.split("\n");
            for (String line : lines) {
                if (!line.trim().isEmpty()) {
                    this.items.add(line);
                }
            }
        }
    }
}
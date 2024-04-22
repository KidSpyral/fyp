package com.example.fyp;


public class diaryEntry extends diaryEntryId
{
    private String description;
    private String title;
    private String sentiment;
    private String entryDate;
    private long timestamp;

    public diaryEntry(){
        this.timestamp = System.currentTimeMillis();

    }
    public diaryEntry(String description, String title, String sentiment, String entryDate) {
        this.description = description;
        this.title = title;
        this.sentiment = sentiment;
        this.entryDate = entryDate;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }

    public String getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(String entryDate) {
        this.entryDate = entryDate;
    }

    @Override
    public String toString() {
        return "DiaryEntry{id='" + diaryEntryId + "', description='" + description + "', title='" + title + "', sentiment='" + sentiment + "', entryDate='" + entryDate + "', timestamp='" + timestamp + "'}";
    }
}

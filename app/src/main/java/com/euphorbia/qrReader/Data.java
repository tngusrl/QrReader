package com.euphorbia.qrReader;

import java.io.Serializable;

public class Data implements Serializable {

    private int idx;
    private String date;
    private String title;
    private String content;

    public Data(int idx, String date, String title, String content) {
        this.idx = idx;
        this.date = date;
        this.title = title;
        this.content = content;
    }

    public int getIdx() {
        return idx;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}

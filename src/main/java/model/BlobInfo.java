package model;

// A simple class to encapsulate blob names and URLs
public class BlobInfo {
    private String name;
    private String url;

    public BlobInfo(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
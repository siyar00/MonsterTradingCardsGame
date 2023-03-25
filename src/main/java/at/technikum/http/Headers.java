package at.technikum.http;

public enum Headers {
    CONTENT_TYPE_JSON("Content-Type", "application/json"),
    CONTENT_TYPE_TEXT("Content-Type", "text/plain");

    private final String key;
    private final String value;

    Headers(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String toString(){
        return key+":"+value;
    }
}

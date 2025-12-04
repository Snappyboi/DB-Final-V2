package backend;

public class Media {
    private int mediaID;
    private String mediaIdRaw;
    private String title;
    private String genre;
    private String releaseDate;
    private String imdbLink;
    private String directors;
    private String cast;

    public Media(int mediaID, String title, String genre, String releaseDate) {
        this.mediaID = mediaID;
        this.title = title;
        this.genre = genre;
        this.releaseDate = releaseDate;
    }

    public int getMediaID() { return mediaID; }

    public String getMediaIdRaw() { return mediaIdRaw; }

    void setMediaIdRaw(String mediaIdRaw) { this.mediaIdRaw = mediaIdRaw; }

    public String getTitle() { return title; }

    public String getGenre() { return genre; }

    public String getReleaseDate() { return releaseDate; }

    public String getImdbLink() { return imdbLink; }

    public void setImdbLink(String imdbLink) { this.imdbLink = imdbLink; }

    public String getDirectors() { return directors; }

    public void setDirectors(String directors) { this.directors = directors; }

    public String getCast() { return cast; }

    public void setCast(String cast) { this.cast = cast; }

    @Override
    public String toString() { return title + " (" + genre + ", " + releaseDate + ")"; }
}

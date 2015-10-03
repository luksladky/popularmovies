package cz.lukassladky.popularmovies;

/**
 * Created by admin on 27.9.2015.
 */
public class Review {
    public String title;
    public String content;
    public String url;

    public Review(String title, String content, String url) {
        this.title = title;
        this.content = content;
        this.url = url;
    }
}
package cz.lukassladky.popularmovies;

/**
 * Created by admin on 27.9.2015.
 */
public class TitleContentContainer {
    public String title;
    public String content;
    private int type;

    public TitleContentContainer(int type, String title, String content) {
        this.type = type;
        this.title = title;
        this.content = content;

    }

    public int getType() {
        return type;
    }

}

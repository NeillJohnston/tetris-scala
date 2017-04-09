package io.github.neilljohnston.GUI;

import java.util.HashMap;

public class Overlay {
    private int width, height;
    private HashMap<String, Section> sections;

    public Overlay(int width, int height, Section... sections) {
        this.width = width;
        this.height = height;
        this.sections = new HashMap<String, Section>();

        for(Section s : sections)
            this.sections.put(s.name, s);
    }

    //

    /**
     * Update the overlay so that everything is positioned properly.
     * @param width     New width
     * @param height    New height
     */
    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
    }
}

package io.github.neilljohnston.GUI;

public class Section {
    public String name;

    public int width, height;
    public double propWidth, propHeight;
    public int sourceWidth, sourceHeight;

    public Section(String... properties) {
        for(String property : properties) {
            String key = property.split(":")[0];
            String value = property.split(":")[1];

            // Yes, this is how I get around not being able to switch with Strings.
            // This entire if...else... statement will be magic strings ad nauseam.
            // For some reason I'm okay with that.
            if(key.equals("name")) { name = value; }
        }
    }

    public void reposition() {

    }

    public void resize(int width, int height) {
        this.sourceWidth = width;
        this.sourceHeight = height;
        reposition();
    }
}

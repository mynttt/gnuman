package de.hshannover.inform.gnuman.app.mapeditor;

class NewMapDTO {
    int height, width;
    String name, author;

    NewMapDTO(int height, int width, String name, String author) {
        this.height = height;
        this.width = width;
        this.name = de.hshannover.inform.gnuman.app.util.Helper.onlyAscii(name).trim().length() == 0 ? "Unknown" : de.hshannover.inform.gnuman.app.util.Helper.onlyAscii(name);
        this.author = de.hshannover.inform.gnuman.app.util.Helper.onlyAscii(author).trim().length() == 0 ? "Unknown" : de.hshannover.inform.gnuman.app.util.Helper.onlyAscii(author);
    }
}

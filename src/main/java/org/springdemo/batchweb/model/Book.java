package org.springdemo.batchweb.model;

public record Book(String isbn, String title, String authors, Integer yearPublished) {
    public static BookBuilder builder() {
        return new BookBuilder();
    }

    public static class BookBuilder {
        private String isbn;
        private String title;
        private String authors;
        private Integer yearPublished;

        BookBuilder() {
        }

        public BookBuilder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public BookBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BookBuilder authors(String authors) {
            this.authors = authors;
            return this;
        }

        public BookBuilder yearPublished(Integer yearPublished) {
            this.yearPublished = yearPublished;
            return this;
        }

        public Book build() {
            return new Book(this.isbn, this.title, this.authors, this.yearPublished);
        }

        public String toString() {
            return "Book.BookBuilder(isbn=" + this.isbn + ", title=" + this.title + ", authors=" + this.authors + ", yearPublished=" + this.yearPublished + ")";
        }
    }
}

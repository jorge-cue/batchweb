package org.springdemo.batchweb.entity;

import jakarta.persistence.*;

@Entity
public class BookEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(nullable = false, unique = true, length = 13, updatable = false, insertable = true)
    String isbn;
    @Column(nullable = false)
    String title;
    @Column(nullable = false)
    String authors;
    @Column(nullable = true)
    Integer yearPublished;

    private BookEntity(Long id, String isbn, String title, String authors, Integer yearPublished) {
        this.id = id;
        this.isbn = isbn;
        this.title = title;
        this.authors = authors;
        this.yearPublished = yearPublished;
    }

    public BookEntity() {
    }

    public static BookEntityBuilder builder() {
        return new BookEntityBuilder();
    }

    public Long getId() {
        return this.id;
    }

    public String getIsbn() {
        return this.isbn;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAuthors() {
        return this.authors;
    }

    public Integer getYearPublished() {
        return this.yearPublished;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAuthors(String authors) {
        this.authors = authors;
    }

    public void setYearPublished(Integer yearPublished) {
        this.yearPublished = yearPublished;
    }

    public boolean equals(final Object o) {
        if (o == this) return true;
        if (!(o instanceof BookEntity)) return false;
        final BookEntity other = (BookEntity) o;
        if (!other.canEqual((Object) this)) return false;
        final Object this$id = this.getId();
        final Object other$id = other.getId();
        if (this$id == null ? other$id != null : !this$id.equals(other$id)) return false;
        final Object this$isbn = this.getIsbn();
        final Object other$isbn = other.getIsbn();
        if (this$isbn == null ? other$isbn != null : !this$isbn.equals(other$isbn)) return false;
        final Object this$title = this.getTitle();
        final Object other$title = other.getTitle();
        if (this$title == null ? other$title != null : !this$title.equals(other$title)) return false;
        final Object this$authors = this.getAuthors();
        final Object other$authors = other.getAuthors();
        if (this$authors == null ? other$authors != null : !this$authors.equals(other$authors)) return false;
        final Object this$yearPublished = this.getYearPublished();
        final Object other$yearPublished = other.getYearPublished();
        if (this$yearPublished == null ? other$yearPublished != null : !this$yearPublished.equals(other$yearPublished))
            return false;
        return true;
    }

    protected boolean canEqual(final Object other) {
        return other instanceof BookEntity;
    }

    public int hashCode() {
        final int PRIME = 59;
        int result = 1;
        final Object $id = this.getId();
        result = result * PRIME + ($id == null ? 43 : $id.hashCode());
        final Object $isbn = this.getIsbn();
        result = result * PRIME + ($isbn == null ? 43 : $isbn.hashCode());
        final Object $title = this.getTitle();
        result = result * PRIME + ($title == null ? 43 : $title.hashCode());
        final Object $authors = this.getAuthors();
        result = result * PRIME + ($authors == null ? 43 : $authors.hashCode());
        final Object $yearPublished = this.getYearPublished();
        result = result * PRIME + ($yearPublished == null ? 43 : $yearPublished.hashCode());
        return result;
    }

    public String toString() {
        return "BookEntity(id=" + this.getId() + ", isbn=" + this.getIsbn() + ", title=" + this.getTitle() + ", authors=" + this.getAuthors() + ", yearPublished=" + this.getYearPublished() + ")";
    }

    public static class BookEntityBuilder {
        private Long id;
        private String isbn;
        private String title;
        private String authors;
        private Integer yearPublished;

        BookEntityBuilder() {
        }

        public BookEntityBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public BookEntityBuilder isbn(String isbn) {
            this.isbn = isbn;
            return this;
        }

        public BookEntityBuilder title(String title) {
            this.title = title;
            return this;
        }

        public BookEntityBuilder authors(String authors) {
            this.authors = authors;
            return this;
        }

        public BookEntityBuilder yearPublished(Integer yearPublished) {
            this.yearPublished = yearPublished;
            return this;
        }

        public BookEntity build() {
            return new BookEntity(this.id, this.isbn, this.title, this.authors, this.yearPublished);
        }

        public String toString() {
            return "BookEntity.BookEntityBuilder(id=" + this.id + ", isbn=" + this.isbn + ", title=" + this.title + ", authors=" + this.authors + ", yearPublished=" + this.yearPublished + ")";
        }
    }
}

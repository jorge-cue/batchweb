package org.springdemo.batchweb.model;

import lombok.Builder;

@Builder
public record Book(Long id, String isbn, String title, String authors, Integer yearPublished) {
}

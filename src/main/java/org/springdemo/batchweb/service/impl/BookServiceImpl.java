package org.springdemo.batchweb.service.impl;

import org.springdemo.batchweb.mappers.BookMappers;
import org.springdemo.batchweb.model.Book;
import org.springdemo.batchweb.repository.BookRepository;
import org.springdemo.batchweb.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMappers bookMappers;

    @Autowired
    public BookServiceImpl(BookRepository bookRepository, BookMappers bookMappers) {
        this.bookRepository = bookRepository;
        this.bookMappers = bookMappers;
    }

    @Override
    public Collection<Book> findAll() {
        var books = bookRepository.findAll();
        return bookMappers.mapEntitiesToModels(books);
    }
}

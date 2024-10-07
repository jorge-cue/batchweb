package org.springdemo.batchweb.service;

import org.springdemo.batchweb.model.Book;

import java.util.Collection;

public interface BookService {

    Collection<Book> findAll();

}

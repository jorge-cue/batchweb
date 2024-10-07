package org.springdemo.batchweb.mappers;

import org.mapstruct.Mapper;
import org.springdemo.batchweb.entity.BookEntity;
import org.springdemo.batchweb.model.Book;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface BookMappers {

    Book mapEntityToModel(BookEntity entity);

    Collection<Book> mapEntitiesToModels(Collection<BookEntity> entities);

    BookEntity mapModelToEntity(Book model);

    Collection<BookEntity> mapModelsToEntities(Collection<Book> models);

}

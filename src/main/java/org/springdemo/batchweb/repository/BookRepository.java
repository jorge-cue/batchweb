package org.springdemo.batchweb.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<org.springdemo.batchweb.entity.BookEntity, Long> {
}

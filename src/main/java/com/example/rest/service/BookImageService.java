package com.example.rest.service;

import com.example.rest.entity.BookImage;
import com.example.rest.repository.BookImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BookImageService {
    @Autowired
    private BookImageRepository bookImageRepository;
    public BookImage save(BookImage bookImage) {
        return bookImageRepository.save(bookImage);
    }
}

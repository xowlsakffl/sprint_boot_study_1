package com.example.rest.restcontroller;

import com.example.rest.entity.Book;
import com.example.rest.entity.BookImage;
import com.example.rest.entity.BookPayloadDTO;
import com.example.rest.entity.BookViewDTO;
import com.example.rest.service.BookImageService;
import com.example.rest.service.BookService;
import com.example.rest.util.ImageUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@RestController
@RequestMapping("/api")
@Tag(name = "전체 Book API", description = "Book API 입니다.")
public class BookRestController {
    @Autowired
    private BookService bookService;

    @Autowired
    private BookImageService bookImageService;
    /*
    @GetMapping("/test")
    @Tag(name = "Book Test API")
    @Operation(summary = "Test 메서드", description = "Test 메서드 입니다")
    public String test() {
        return "Hello World";
    }
    */

    @PostMapping(value = "/books", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    /*
    @ApiResponse(responseCode = "400", description = "400에러")
    @ApiResponse(responseCode = "200", description = "200 성공")
    @Operation(summary = "Book 추가")
    */
    public ResponseEntity<BookViewDTO> addBook(@Valid @RequestBody BookPayloadDTO bookPayload) {
        try {
            Book book = new Book();
            book.setSubject(bookPayload.getSubject());
            book.setAuthor(bookPayload.getAuthor());
            book.setPage(bookPayload.getPage());
            book.setPrice(bookPayload.getPrice());
            book = bookService.save(book);

            BookViewDTO bookViewDTO = new BookViewDTO(
                    book.getId(),
                    book.getSubject(),
                    book.getPrice(),
                    book.getAuthor(),
                    book.getPage(),
                    book.getCreatedAt()
            );
            return ResponseEntity.ok(bookViewDTO);// 200+Json 데이터
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @GetMapping(value = "/books", produces = "application/json")
    public List<BookViewDTO> books() {
        List<BookViewDTO> books = new ArrayList<>();
        for (Book book : bookService.findAll()) {
            books.add(new BookViewDTO(
                    book.getId(),
                    book.getSubject(),
                    book.getPrice(),
                    book.getAuthor(),
                    book.getPage(),
                    book.getCreatedAt()
            ));
        }
        return books;
    }

    @GetMapping(value = "/book/{id}", produces = "application/json")
    public ResponseEntity<?> getBook(@PathVariable long id) {
        Optional<Book> optionalBook = bookService.findById(id);
        Book book;
        if(optionalBook.isPresent()) {
            book = optionalBook.get();
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        BookViewDTO bookViewDTO = new BookViewDTO(
                book.getId(),
                book.getSubject(),
                book.getPrice(),
                book.getAuthor(),
                book.getPage(),
                book.getCreatedAt()
        );
        return ResponseEntity.ok(bookViewDTO);
    }

    @PutMapping(value = "/books/{id}", consumes = "application/json", produces = "application/json")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<BookViewDTO> updateBook(@Valid @RequestBody BookPayloadDTO bookPayload, @PathVariable long id) {
        Optional<Book> optionalBook = bookService.findById(id);
        if(optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setSubject(bookPayload.getSubject());
            book.setAuthor(bookPayload.getAuthor());
            book.setPage(bookPayload.getPage());
            book.setPrice(bookPayload.getPrice());
            book = bookService.save(book);

            BookViewDTO bookViewDTO = new BookViewDTO(
                    book.getId(),
                    book.getSubject(),
                    book.getPage(),
                    book.getAuthor(),
                    book.getPrice(),
                    book.getCreatedAt()
                    );
            return ResponseEntity.ok(bookViewDTO);
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @DeleteMapping(value = "/books/{id}")
    public ResponseEntity<?> deleteBook(@PathVariable long id) {
        Optional<Book> optionalBook = bookService.findById(id);
        if(optionalBook.isPresent()) {
            Book book = optionalBook.get();
        }else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        bookService.delete(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(null);
    }

    @Value("${upload.path}")
    private String uploadPath;

    @PostMapping(value = "/book/upload/{book_id}/{type}", consumes = {"multipart/form-data"})
    public ResponseEntity<?> imageUpload(@RequestPart(required = true) MultipartFile[] files, @PathVariable long book_id, @PathVariable int type) {
        Optional<Book> optionalBook = bookService.findById(book_id);
        Book book;
        if(optionalBook.isPresent()) {
            book = optionalBook.get();
        }else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        List<String> succesImages = new ArrayList<>();
        List<String> failImages = new ArrayList<>();
        Arrays.stream(files).forEach(file->{
            String contentType = file.getContentType();
            if(contentType.equals("image/jpeg") || contentType.equals("image/jpg") || contentType.equals("image/png")) {
                succesImages.add(file.getOriginalFilename());
                try {
                    String fileName = file.getOriginalFilename();
                    String generatedString = RandomStringUtils.random(10, true, true);
                    String newFileName = generatedString + fileName;
                    if(type == 1){
                        newFileName = "thumb_" + newFileName;
                    }
                    String absoluteFileLocation = ImageUtil.makePath(uploadPath, newFileName, book_id);
                    System.out.println("absoluteFileLocation = " + absoluteFileLocation);
                    Path path = Paths.get(absoluteFileLocation);
                    if (type != 1){
                        Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                    }
                    BookImage bookImage = new BookImage();
                    bookImage.setOriginalFileName(fileName);
                    bookImage.setFileName(newFileName);
                    bookImage.setType(type);
                    bookImage.setBook(book);
                    bookImageService.save(bookImage);

                    if(type == 1){
                        BufferedImage thumbnail = ImageUtil.getThumbnail(file,300);
                        String thumbnailLocation = ImageUtil.makePath(uploadPath, newFileName, book_id);
                        ImageIO.write(thumbnail, file.getContentType().split("/")[1],new File(thumbnailLocation));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    failImages.add(file.getOriginalFilename());
                }
            }else {
                failImages.add(file.getOriginalFilename());
            }
        });
        HashMap<String, List<String>> result = new HashMap<>();
        result.put("Success", succesImages);
        result.put("fail", failImages);

        List<HashMap<String, List<String>>> response = new ArrayList<>();
        response.add(result);
        return ResponseEntity.ok(response);
    }
}

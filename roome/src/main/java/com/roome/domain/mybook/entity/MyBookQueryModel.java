package com.roome.domain.mybook.entity;

import com.roome.domain.book.entity.BookGenre;
import com.roome.domain.book.entity.Genre;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class MyBookQueryModel {

    Long id;
    String title;
    String author;
    String publisher;
    LocalDate publishedDate;
    String imageUrl;
    List<String> genreNames;
    Long page;

    public static MyBookQueryModel create(MyBook myBook) {
        MyBookQueryModel myBookQueryModel = new MyBookQueryModel();
        myBookQueryModel.id = myBook.getId();
        myBookQueryModel.title = myBook.getBook().getTitle();
        myBookQueryModel.author = myBook.getBook().getAuthor();
        myBookQueryModel.publisher = myBook.getBook().getPublisher();
        myBookQueryModel.publishedDate = myBook.getBook().getPublishedDate();
        myBookQueryModel.imageUrl = myBook.getBook().getImageUrl();
        myBookQueryModel.genreNames = myBook.getBook().getBookGenres().stream()
                .map(BookGenre::getGenre)
                .map(Genre::getName)
                .toList();
        myBookQueryModel.page = myBook.getBook().getPage();
        return myBookQueryModel;
    }
}

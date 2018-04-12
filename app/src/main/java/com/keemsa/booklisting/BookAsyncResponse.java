package com.keemsa.booklisting;

import java.util.List;

/**
 * Created by sebastian on 07/07/16.
 */
public interface BookAsyncResponse {

    int SERVER_ERROR = 0;
    int NO_BOOKS = 1;

    void processBooks(List<Book> books);

    void toggleProgressBar(int value);

    String noAuthor();

    void toggleMessage(int value);

    void setMessage(int message);
}

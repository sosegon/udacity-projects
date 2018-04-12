package com.keemsa.booklisting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by sebastian on 07/07/16.
 */
public class BookAdapter extends ArrayAdapter<Book> {

    public BookAdapter(Context context, List<Book> books) {
        super(context, 0, books);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View bookItemView = convertView;
        if (bookItemView == null) {
            bookItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_book, parent, false);
        }

        Book currentBook = getItem(position);

        TextView txt_author_name = (TextView) bookItemView.findViewById(R.id.txt_author_name);
        TextView txt_book_name = (TextView) bookItemView.findViewById(R.id.txt_book_name);

        txt_author_name.setText(currentBook.getAuthorName());
        txt_book_name.setText(currentBook.getBookTitle());

        return bookItemView;
    }
}

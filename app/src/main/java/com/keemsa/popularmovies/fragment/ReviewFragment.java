package com.keemsa.popularmovies.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.keemsa.popularmovies.R;
import com.keemsa.popularmovies.data.ReviewColumns;

/**
 * Created by sebastian on 10/13/16.
 */
public class ReviewFragment extends Fragment {

    private final String LOG_TAG = ReviewFragment.class.getSimpleName();

    private TextView txt_review_author, txt_review_content;

    public ReviewFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_review, container, false);

        txt_review_author = (TextView) view.findViewById(R.id.txt_review_author);
        txt_review_content = (TextView) view.findViewById(R.id.txt_review_content);

        Bundle args = this.getArguments();
        if(args.containsKey(ReviewColumns.AUTHOR)){
            String author = getString(R.string.review_author, args.getString(ReviewColumns.AUTHOR));
            txt_review_author.setText(author);
        }
        if(args.containsKey(ReviewColumns.CONTENT)){
            txt_review_content.setText(args.getString(ReviewColumns.CONTENT));
        }

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if(savedInstanceState != null){
            if(savedInstanceState.containsKey(MovieReviewsFragment.REVIEW_COLUMNS[1])){
                txt_review_author.setText(savedInstanceState.getString(MovieReviewsFragment.REVIEW_COLUMNS[1]));
            }
        }
        super.onCreate(savedInstanceState);
    }
}

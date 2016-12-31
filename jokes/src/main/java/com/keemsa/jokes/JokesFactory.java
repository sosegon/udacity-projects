package com.keemsa.jokes;

/**
 * Created by sebastian on 12/31/16.
 */

public class JokesFactory {

  private static JokesFactory factory;

  private String[] jokes = {
          "joke1",
          "joke2",
          "joke3",
          "joke4",
          "joke5",
          "joke6",
          "joke7",
          "joke8",
  };

  private JokesFactory(){

  }

  public static JokesFactory getInstance(){
    if (factory == null) {
      factory = new JokesFactory();
    }

    return factory;
  }

  public Joke throwJoke(){
    int jokeIndex = (int) Math.random() * jokes.length;
    return new Joke(jokes[jokeIndex]);
  }
}

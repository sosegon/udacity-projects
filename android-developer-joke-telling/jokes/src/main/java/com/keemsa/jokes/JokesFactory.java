package com.keemsa.jokes;

/**
 * Created by sebastian on 12/31/16.
 */

public class JokesFactory {

  private static JokesFactory factory;

  private String[] jokes = {
          "An Android app walks into a bar. Bartender asks, \"Can I get you a drink?\" The app looks disappointed and says, \"That wasn't my intent.\"",
          "A Gingerbread phone walks into a bar and starts screaming obscenities. An irate patron, having had enough of Gingerbread's nonsense, throws a direct punch... but somehow misses. Gingerbread erupts with laugher and says, \"Lololol! You have no idea where my touch area is!\"",
          "A fragment walks into a bar and says, \"I'm lost. Can I please speak to the FragmentManager?\"",
          "A pregnant fragment walks into a bar. The bartender says, \"Whoa! Whoa! We don't support nested fragments here!\"",
          "An iPhone enters the Android bar. The customers take him out and beat him up. Just before passing out, he says \"I swear, I thought this was an iPhone bar\".\n" +
                  "The angry Androids reply: \"sorry, you can't use that excuse anymore!\"",
          "A fragment walks into a bar, and the bartender asks for an ID. Fragment says, \"I don't have an ID.\" So the bartender says, \"Okay, I'll make a NullPointerException.\"",
          "A ChaCha walks into a bar. The bartender asks, \"What will it be?\". \"I'm sorry\", says the ChaCha, \"I'm already full\".",
          "A WebView walks into a bar. The bartender says, \"Sorry buddy, we only allow natives here.\" Disappointed, the WebView walks out of the bar.. and then gets ran over by a Mustang.",
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

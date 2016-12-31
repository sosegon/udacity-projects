/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.keemsa.jokes.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.keemsa.jokes.Joke;
import com.keemsa.jokes.JokesFactory;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "myApi",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.jokes.keemsa.com",
                ownerName = "backend.jokes.keemsa.com",
                packagePath = ""
        )
)
public class MyEndpoint {

  @ApiMethod(name = "throwJoke")
  public Joke throwJoke() {
    return JokesFactory.getInstance().throwJoke();
  }
}

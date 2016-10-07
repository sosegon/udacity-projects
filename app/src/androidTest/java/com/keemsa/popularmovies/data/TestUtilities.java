package com.keemsa.popularmovies.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import com.keemsa.popularmovies.Utility;
import com.keemsa.popularmovies.utils.PollingCheck;

import java.util.Map;
import java.util.Set;

/**
 * Created by sebastian on 10/4/16.
 */
public class TestUtilities extends AndroidTestCase {

    static ContentValues createMovieValues() {
        ContentValues movieValues = new ContentValues();
        movieValues.put(MovieColumns._ID, 333484);
        movieValues.put(MovieColumns.TITLE, "The Magnificent Seven");
        movieValues.put(MovieColumns.SYNOPSIS, "A big screen remake of John Sturges' classic western The Magnificent Seven, itself a remake of Akira Kurosawa's Seven Samurai. Seven gun men in the old west gradually come together to help a poor village against savage thieves.");
        movieValues.put(MovieColumns.POSTER_URL, "z6BP8yLwck8mN9dtdYKkZ4XGa3D.jpg");
        movieValues.put(MovieColumns.RELEASE_DATE, Utility.getDateInMilliSeconds("2016-09-14"));
        movieValues.put(MovieColumns.RATING, 4.7);

        return movieValues;
    }

    static ContentValues[] createArrayMovieValues() {
        ContentValues movieValues1 = new ContentValues();
        movieValues1.put(MovieColumns._ID, 333484);
        movieValues1.put(MovieColumns.TITLE, "The Magnificent Seven");
        movieValues1.put(MovieColumns.SYNOPSIS, "A big screen remake of John Sturges' classic western The Magnificent Seven, itself a remake of Akira Kurosawa's Seven Samurai. Seven gun men in the old west gradually come together to help a poor village against savage thieves.");
        movieValues1.put(MovieColumns.POSTER_URL, "z6BP8yLwck8mN9dtdYKkZ4XGa3D.jpg");
        movieValues1.put(MovieColumns.RELEASE_DATE, Utility.getDateInMilliSeconds("2016-09-14"));
        movieValues1.put(MovieColumns.RATING, 4.7);

        ContentValues movieValues2 = new ContentValues();
        movieValues2.put(MovieColumns._ID, 271110);
        movieValues2.put(MovieColumns.TITLE, "Captain America: Civil War");
        movieValues2.put(MovieColumns.SYNOPSIS, "Following the events of Age of Ultron, the collective governments of the world pass an act designed to regulate all superhuman activity. This polarizes opinion amongst the Avengers, causing two factions to side with Iron Man or Captain America, which causes an epic battle between former allies.");
        movieValues2.put(MovieColumns.POSTER_URL, "5N20rQURev5CNDcMjHVUZhpoCNC.jpg");
        movieValues2.put(MovieColumns.RELEASE_DATE, Utility.getDateInMilliSeconds("2016-04-27"));
        movieValues2.put(MovieColumns.RATING, 6.77);

        ContentValues movieValues3 = new ContentValues();
        movieValues3.put(MovieColumns._ID, 278924);
        movieValues3.put(MovieColumns.TITLE, "Mechanic: Resurrection");
        movieValues3.put(MovieColumns.SYNOPSIS, "Arthur Bishop thought he had put his murderous past behind him when his most formidable foe kidnaps the love of his life. Now he is forced to travel the globe to complete three impossible assassinations, and do what he does best, make them look like accidents.");
        movieValues3.put(MovieColumns.POSTER_URL, "tgfRDJs5PFW20Aoh1orEzuxW8cN.jpg");
        movieValues3.put(MovieColumns.RELEASE_DATE, Utility.getDateInMilliSeconds("2016-08-25"));
        movieValues3.put(MovieColumns.RATING, 4.34);

        return new ContentValues[]{movieValues2, movieValues3, movieValues1}; // ASC orderBy ID
    }

    static ContentValues createTrailerValues() {
        ContentValues trailerValues = new ContentValues();
        trailerValues.put(TrailerColumns._ID, "5797609fc3a36865ae0021cd");
        trailerValues.put(TrailerColumns.KEY, "q-RBA0xoaWU");
        trailerValues.put(TrailerColumns.SITE, "YouTube");
        trailerValues.put(TrailerColumns.NAME, "Official Trailer");
        trailerValues.put(TrailerColumns.TYPE, "Trailer");
        trailerValues.put(TrailerColumns.MOVIE_ID, 333484);

        return trailerValues;
    }

    static ContentValues[] createArrayTrailerValues() {
        ContentValues trailerValues1 = new ContentValues();
        trailerValues1.put(TrailerColumns._ID, "5797609fc3a36865ae0021cd");
        trailerValues1.put(TrailerColumns.KEY, "q-RBA0xoaWU");
        trailerValues1.put(TrailerColumns.SITE, "YouTube");
        trailerValues1.put(TrailerColumns.NAME, "Official Trailer");
        trailerValues1.put(TrailerColumns.TYPE, "Trailer");
        trailerValues1.put(TrailerColumns.MOVIE_ID, 333484);

        ContentValues trailerValues2 = new ContentValues();
        trailerValues2.put(TrailerColumns._ID, "57183f21c3a3687b8c002e3b");
        trailerValues2.put(TrailerColumns.KEY, "deSRpSn8Pyk");
        trailerValues2.put(TrailerColumns.SITE, "YouTube");
        trailerValues2.put(TrailerColumns.NAME, "Teaser Trailer");
        trailerValues2.put(TrailerColumns.TYPE, "Teaser");
        trailerValues2.put(TrailerColumns.MOVIE_ID, 333484);

        return new ContentValues[]{trailerValues2, trailerValues1}; // ASC orderBy ID
    }

    static ContentValues createReviewValues() {
        ContentValues reviewValues = new ContentValues();
        reviewValues.put(ReviewColumns._ID, "57e8d5e9c3a3687c180059c9");
        reviewValues.put(ReviewColumns.AUTHOR, "Frank Ochieng");
        reviewValues.put(ReviewColumns.CONTENT, "The western genre has gradually been making its way back into the cinematic fold. Recent dusty trail ditties such as David McKenzie's modern-day _Hell or High Water_ or Quentin Tarantino's _The Hateful Eight_ have proven to be recent saddle-tested gems that enthusiastically put cowpoke enthusiasts in movie theater seats. Furthermore, what would Hollywood do if it did not predictably invite yet another remake of a classic film into the moviegoers' mindset? Hence, director Antoine Fuqua takes a challenging stab at generating interest in his latest workman-like western shoot 'em up in the millennium-made version of **The Magnificent Seven**. \\r\\n\\r\\nNaturally, Fuqua's chaotic and calculating gun-toting actioner is an updated remake of director John Sturges's 1960 film that starred late iconic box office big stars such as Yul Brynner, Steve McQueen, Charles Bronson and Eli Wallach. Of course in return Sturges's borrowed his artistic gun-for-hire gumption from legendary Japanese auteur Akiro Kurosawa's vintage and influential _Seven Samurai_. Fuqua, whose gritty urban police drama _Training Day_ secured a Best Actor Oscar for his **The Magnificent Seven** leading man in two-time Academy Award winner Denzel Washington, takes on the retelling of his particular _Seven_ with feisty fury. One would not necessary anoint Fuqua's outlaw tale as a superior successor to Sturges's borrowed blueprint from Kurosawa. However, Fuqua's array of blazing bullets from his bunch of rag tag bad boys has its own distinctive sense of decorative dare and destruction that feels authentic.\\r\\n\\r\\nScreenwriters Nic Pizzolatto (\\\"True Detective\\\")and Richard Wenk deliver an unapologetic script that calls for high body counts, old-fashioned showdowns and a wild west waywardness that swaggers courtesy of Fuqua's corrosive crew. The popcorn entertainment in **The Magnificent Seven** is strictly in guilty pleasure territory so there is no need to tighten up your holsters for all you little buckaroos that are eager to wallow in Fuqua's cutthroat corral of gunslingers.   \\r\\n\\r\\nMustached bounty hunter Sam Chisolm (Washington) is the all-dressed-in-black avenger whose mission is to provide protection for the town of Rose Creek, New Mexico. In his ambitious bid to save the jeopardized Rose Creek he must assembled a group of skilled gunmen able to stand up to the diabolical powers-that-be that look to foster the on-going havoc that prevails. \\r\\n\\r\\nSpecifically, Rose Creek is under the dastardly control of the diabolical Bartholomew Bogue (Peter Sarsgaard) that rules the town with an iron fist. The opening scene demonstrates how nefarious Bogue is at heart because he has no qualms about seizing land from its vulnerable owners or quieting down his critics with intimidating force. Basically, Bogue and his hideous henchmen are not to be reckoned with at all--unless you are willing to match wicked-minded wits with the raw and rough Chisolm and his gun-wielding renegades.\\r\\n\\r\\nRose Creek resident Emma Cullen (Haley Bennett, \\\"Music in Lyrics\\\", \\\"The Girl on the Train\\\") steps up to the plate in her effort to confront the nasty Bogue the only best way she knows how--hiring the capable and crafty collection of the Seven to contain this intimidating menace.\\r\\n\\r\\nJoining Chisolm in his bloody quest to rescue Rose Creek from Bogue's disturbing clutches are explosives expert gambler Josh Farraday (Chris Pratt), and conflicted sharpshooter Goodnight Robicheaux (Ethan Hawke reuniting with his \\\"Training Day\\\" director and co-star Fuqua and Washington). The rest of the tag-a-longs include the outlandishly bearded Jack Horne (Vincent D'Onofrio from TV's \\\"Law & Order: Criminal Intent\\\"), Billy Rocks (Byung-hun Lee), Vasquez (Manuel Garcia-Rulfo) and Red Harvest (Martin Seinsmeier). Together, the anti-heroes known as the Magnificent Seven hope to meet the expectations of Emma's (and the town's) cause and eradicate the villainous Bogue by any means necessary.    \\r\\n\\r\\n**The Magnificent Seven** certainly does not have any pretensions about posing as a conscious-minded, revisionist western as it definitely does not have the prolific pedigree such as Clint Eastwood's _Unforgiven_ for instance. Nevertheless, the film does have a devilish impishness as its main function is to echo an exaggerated rustic feel to its throwback acknowledgement when westerns of yesteryear were just plain frivolous and furious without any particular rhyme or reason.\\r\\n\\r\\nSure, the characters have really no inside depth beyond their taste for roughshod recklessness and wild tumbleweed theatrics. This is not necessarily a bad thing to consider in Fuqua's **The Magnificent Seven** because the name of the game is serving up an escapist need for its giddy-up rush for the senses. Indeed, Washington and company will not make anybody forget the aforementioned Brynner and his squad from nearly six decades ago. Still, this particular _Seven_ has its own kind of favorable punch to savor. \\r\\n\\r\\nThe notable names in _Seven_ do rise to the occasion within the context of this otherwise basic story of the wannabe borderline good guys versus the bombastic bad guys. Washington's Chisolm is solidly smooth as charismatic as the leader of the pack. Pratt's Farraday is a charming hoot as the roguish gambling cad. Hawke's Goodnight does a decent job portraying the talented gun handler simply trying to get his groove back due to his shaken confidence from a prior incident (yes, the catchy movie moniker of Goodnight Robicheaux is a keeper to say the least). And D'Onofrio's amusing Horne is deliciously irreverent. The always adventurous Sarsgaard comes to life as the vile wonder whose presence inspires the Seven to tap into vengeance mode. \\r\\n\\r\\nAt the end of the roundup it is quite clear that **The Magnificent Seven** wants to lasso its penchant for resembling a showy Hollywood western even if it is at the expense of lifting its rowdy roots from the likes of its highly regarded predecessors from golden cinema's treasured past.    \\r\\n\\r\\n**The Magnificent Seven** (2016)\\r\\n\\r\\nSony Pictures\\r\\n\\r\\n2 hrs. 12 mins.\\r\\n\\r\\nStarring: Denzel Washington, Chris Pratt, Ethan Hawke, Peter Sarsgaard, Haley Bennett, Vincent D'Onofrio, Byung-hun Lee, Maunel Garcia Rulfo, Martin Sensmeier\\r\\n\\r\\nDirected by: Antoine Fuqua\\r\\n\\r\\nMPAA Rating: PG-13\\r\\n\\r\\nGenre: Western\\/Drama\\/Action and Adventure\\r\\n\\r\\nCritic's rating: ** 1\\/2 stars (out of 4 stars)\\r\\n\\r\\n(c) **Frank Ochieng** 2016");
        reviewValues.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57e8d5e9c3a3687c180059c9");
        reviewValues.put(ReviewColumns.MOVIE_ID, 333484);

        return reviewValues;
    }

    static ContentValues[] createArrayReviewValues() {
        ContentValues reviewValues1 = new ContentValues();
        reviewValues1.put(ReviewColumns._ID, "57e8d5e9c3a3687c180059c9");
        reviewValues1.put(ReviewColumns.AUTHOR, "Frank Ochieng");
        reviewValues1.put(ReviewColumns.CONTENT, "The western genre has gradually been making its way back into the cinematic fold. Recent dusty trail ditties such as David McKenzie's modern-day _Hell or High Water_ or Quentin Tarantino's _The Hateful Eight_ have proven to be recent saddle-tested gems that enthusiastically put cowpoke enthusiasts in movie theater seats. Furthermore, what would Hollywood do if it did not predictably invite yet another remake of a classic film into the moviegoers' mindset? Hence, director Antoine Fuqua takes a challenging stab at generating interest in his latest workman-like western shoot 'em up in the millennium-made version of **The Magnificent Seven**. \\r\\n\\r\\nNaturally, Fuqua's chaotic and calculating gun-toting actioner is an updated remake of director John Sturges's 1960 film that starred late iconic box office big stars such as Yul Brynner, Steve McQueen, Charles Bronson and Eli Wallach. Of course in return Sturges's borrowed his artistic gun-for-hire gumption from legendary Japanese auteur Akiro Kurosawa's vintage and influential _Seven Samurai_. Fuqua, whose gritty urban police drama _Training Day_ secured a Best Actor Oscar for his **The Magnificent Seven** leading man in two-time Academy Award winner Denzel Washington, takes on the retelling of his particular _Seven_ with feisty fury. One would not necessary anoint Fuqua's outlaw tale as a superior successor to Sturges's borrowed blueprint from Kurosawa. However, Fuqua's array of blazing bullets from his bunch of rag tag bad boys has its own distinctive sense of decorative dare and destruction that feels authentic.\\r\\n\\r\\nScreenwriters Nic Pizzolatto (\\\"True Detective\\\")and Richard Wenk deliver an unapologetic script that calls for high body counts, old-fashioned showdowns and a wild west waywardness that swaggers courtesy of Fuqua's corrosive crew. The popcorn entertainment in **The Magnificent Seven** is strictly in guilty pleasure territory so there is no need to tighten up your holsters for all you little buckaroos that are eager to wallow in Fuqua's cutthroat corral of gunslingers.   \\r\\n\\r\\nMustached bounty hunter Sam Chisolm (Washington) is the all-dressed-in-black avenger whose mission is to provide protection for the town of Rose Creek, New Mexico. In his ambitious bid to save the jeopardized Rose Creek he must assembled a group of skilled gunmen able to stand up to the diabolical powers-that-be that look to foster the on-going havoc that prevails. \\r\\n\\r\\nSpecifically, Rose Creek is under the dastardly control of the diabolical Bartholomew Bogue (Peter Sarsgaard) that rules the town with an iron fist. The opening scene demonstrates how nefarious Bogue is at heart because he has no qualms about seizing land from its vulnerable owners or quieting down his critics with intimidating force. Basically, Bogue and his hideous henchmen are not to be reckoned with at all--unless you are willing to match wicked-minded wits with the raw and rough Chisolm and his gun-wielding renegades.\\r\\n\\r\\nRose Creek resident Emma Cullen (Haley Bennett, \\\"Music in Lyrics\\\", \\\"The Girl on the Train\\\") steps up to the plate in her effort to confront the nasty Bogue the only best way she knows how--hiring the capable and crafty collection of the Seven to contain this intimidating menace.\\r\\n\\r\\nJoining Chisolm in his bloody quest to rescue Rose Creek from Bogue's disturbing clutches are explosives expert gambler Josh Farraday (Chris Pratt), and conflicted sharpshooter Goodnight Robicheaux (Ethan Hawke reuniting with his \\\"Training Day\\\" director and co-star Fuqua and Washington). The rest of the tag-a-longs include the outlandishly bearded Jack Horne (Vincent D'Onofrio from TV's \\\"Law & Order: Criminal Intent\\\"), Billy Rocks (Byung-hun Lee), Vasquez (Manuel Garcia-Rulfo) and Red Harvest (Martin Seinsmeier). Together, the anti-heroes known as the Magnificent Seven hope to meet the expectations of Emma's (and the town's) cause and eradicate the villainous Bogue by any means necessary.    \\r\\n\\r\\n**The Magnificent Seven** certainly does not have any pretensions about posing as a conscious-minded, revisionist western as it definitely does not have the prolific pedigree such as Clint Eastwood's _Unforgiven_ for instance. Nevertheless, the film does have a devilish impishness as its main function is to echo an exaggerated rustic feel to its throwback acknowledgement when westerns of yesteryear were just plain frivolous and furious without any particular rhyme or reason.\\r\\n\\r\\nSure, the characters have really no inside depth beyond their taste for roughshod recklessness and wild tumbleweed theatrics. This is not necessarily a bad thing to consider in Fuqua's **The Magnificent Seven** because the name of the game is serving up an escapist need for its giddy-up rush for the senses. Indeed, Washington and company will not make anybody forget the aforementioned Brynner and his squad from nearly six decades ago. Still, this particular _Seven_ has its own kind of favorable punch to savor. \\r\\n\\r\\nThe notable names in _Seven_ do rise to the occasion within the context of this otherwise basic story of the wannabe borderline good guys versus the bombastic bad guys. Washington's Chisolm is solidly smooth as charismatic as the leader of the pack. Pratt's Farraday is a charming hoot as the roguish gambling cad. Hawke's Goodnight does a decent job portraying the talented gun handler simply trying to get his groove back due to his shaken confidence from a prior incident (yes, the catchy movie moniker of Goodnight Robicheaux is a keeper to say the least). And D'Onofrio's amusing Horne is deliciously irreverent. The always adventurous Sarsgaard comes to life as the vile wonder whose presence inspires the Seven to tap into vengeance mode. \\r\\n\\r\\nAt the end of the roundup it is quite clear that **The Magnificent Seven** wants to lasso its penchant for resembling a showy Hollywood western even if it is at the expense of lifting its rowdy roots from the likes of its highly regarded predecessors from golden cinema's treasured past.    \\r\\n\\r\\n**The Magnificent Seven** (2016)\\r\\n\\r\\nSony Pictures\\r\\n\\r\\n2 hrs. 12 mins.\\r\\n\\r\\nStarring: Denzel Washington, Chris Pratt, Ethan Hawke, Peter Sarsgaard, Haley Bennett, Vincent D'Onofrio, Byung-hun Lee, Maunel Garcia Rulfo, Martin Sensmeier\\r\\n\\r\\nDirected by: Antoine Fuqua\\r\\n\\r\\nMPAA Rating: PG-13\\r\\n\\r\\nGenre: Western\\/Drama\\/Action and Adventure\\r\\n\\r\\nCritic's rating: ** 1\\/2 stars (out of 4 stars)\\r\\n\\r\\n(c) **Frank Ochieng** 2016");
        reviewValues1.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57e8d5e9c3a3687c180059c9");
        reviewValues1.put(ReviewColumns.MOVIE_ID, 333484);

        ContentValues reviewValues2 = new ContentValues();
        reviewValues2.put(ReviewColumns._ID, "57eb62e9c3a36836f10021c8");
        reviewValues2.put(ReviewColumns.AUTHOR, "Sebastian Brownlow");
        reviewValues2.put(ReviewColumns.CONTENT, "In 1879, degenerate industrialist Bartholomew Bogue (Peter Sarsgaard) blockades the mining town of Rose Creek, and butchers a gathering of local people drove by Matthew Cullen (Matt Bomer) when they endeavor to confront him. Matthew's better half, Emma Cullen (Haley Bennett), and her companion, Teddy Q (Luke Grimes), ride to the closest town looking for somebody who can help them and happen upon abundance seeker Sam Chisolm (Denzel Washington), who at first decays their proposition until he learns of Bogue's contribution. \\r\\n\\r\\nChisolm embarks to enlist a gathering of gunslingers who can help him, beginning with card shark Josh Faraday (Chris Pratt).Watch and download \\\"The Megneficent Seven\\\" here _**movies watch free**_ They are later joined by sharpshooter Goodnight Robicheaux (Ethan Hawke), blade employing professional killer Billy Rocks (Byung-hun Lee), gifted tracker Jack Horne, Comanche warrior Red Harvest and famous Mexican criminal Vasquez (Manuel Garcia-Rulfo). \\r\\n\\r\\nTouching base in Rose Creek, the seven take part in a gunfight with Bogue's implementer McCann (Cam Gigandet) and his men and push them away with a notice to allow Rose Creek to sit unbothered. Construing that Bogue and his strengths will return in seven days, the seven and Cullen train the sportspeople to guard their home and become attached to them. Watch and download \\\"The Megneficent Seven\\\" here **movies watch free** Robicheaux, frequented by the abhorrences of the Civil War and dreading the unavoidable executing he will be a piece of, forsakes the gathering and is supplanted by Cullen. \\r\\n\\r\\nBogue touches base with his strengths and assaults the city, however the desperados are trapped by the townspeople, prompting a shootout amid which Robicheaux rejoins the gathering, McCann is murdered by Vasquez, and Horne is executed by Bogue's Comanche professional killer Denali (Jonathan Joss), who is later slaughtered by Red Harvest. Watch and download \\\"The Megneficent Seven\\\" here _movies watch free_. Bogue then divulges his mystery weapon, a Gatling firearm, with which he executes various innocents. Acknowledging they're outgunned, the seven push the townspeople away and mount their last stand. \\r\\n\\r\\nRobicheaux and Rocks are killed by a second round of gunfire as Faraday penances himself to demolish the Gatling firearm and whatever is left of Bogue's men, riding up to them in a last charge and afterward exploding a stick of explosive right beside the weapon. Bogue escapes into town, where he is faced by Chisolm, who incapacitates and wounds Bogue. Watch and download \\\"The Megneficent Seven\\\" here <a href=\\\"https:\\/\\/internetmovies4free.blogspot.com\\/\\\">movies watch free<\\/a>. As Chisolm is choking Bogue, he uncovers that Bogue and his men assaulted and killed his mom and sister amid an attack quite a long while prior, in which he himself survived being hanged. Bogue is then lethally shot by Cullen while going after a shrouded weapon in his boot. \\r\\n\\r\\nIn the fallout, Faraday, Robicheaux, Rocks and Horne are covered around the local area and respected by the general population of Rose Creek as saints, while Chisolm, Vasquez and Red Harvest ride off, with Cullen commenting that their gallantry made them legends. Watch and download \\\"The Megneficent Seven\\\" here _movies watch free_.");
        reviewValues2.put(ReviewColumns.URL, "https://www.themoviedb.org/review/57eb62e9c3a36836f10021c8");
        reviewValues2.put(ReviewColumns.MOVIE_ID, 333484);

        return new ContentValues[]{reviewValues1, reviewValues2}; // ASC orderBy ID
    }

    // The next method comes from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/androidTest/java/com/example/android/sunshine/app/data/TestUtilities.java#L27
    static void validateCursor(String error, Cursor cursor, ContentValues expectedValues) {
        assertTrue("Empty cursor returned. " + error, cursor.moveToFirst());
        validateCurrentRecord(error, cursor, expectedValues);
        cursor.close();
    }

    // The next method comes from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/androidTest/java/com/example/android/sunshine/app/data/TestUtilities.java#L33
    static void validateCurrentRecord(String error, Cursor cursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valuesInRecord = expectedValues.valueSet();

        for (Map.Entry<String, Object> value : valuesInRecord) {
            String columnName = value.getKey();
            int columnIndex = cursor.getColumnIndex(columnName);
            assertFalse("Column " + columnName + " not found" + error, columnIndex == -1);

            String expectedValue = value.getValue().toString();
            String actualValue = cursor.getString(columnIndex);
            assertEquals(
                    "Value " + actualValue + " did not match expected value " + expectedValue + ". " + error,
                    expectedValue, actualValue);
        }
    }

    static void validateUriType(Context mContext, String error, Uri uri, String expectedUriType) {
        String type = mContext.getContentResolver().getType(uri);
        assertEquals(
                "Type " + type + " did not match expected type " + expectedUriType + ". " + error,
                expectedUriType,
                type
        );
    }

    // The next class comes from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/androidTest/java/com/example/android/sunshine/app/data/TestUtilities.java#L107
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        // On earlier versions of Android, this onChange method is called
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            // Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
            // It's useful to look at the Android CTS source for ideas on how to test your Android
            // applications.  The reason that PollingCheck works is that, by default, the JUnit
            // testing framework is not running on the main Android application thread.
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    // This method comes from
    // https://github.com/udacity/Sunshine-Version-2/blob/sunshine_master/app/src/androidTest/java/com/example/android/sunshine/app/data/TestUtilities.java#L148
    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }
}
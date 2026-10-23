/* Copyright (c) 2007-2016 MIT 6.005 course staff, all rights reserved.
 * Redistribution of original or derived work requires permission of course staff.
 */
package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Set;

import org.junit.Test;

public class ExtractTest {

	/*

	* Testing strategy:
	*
	* getTimespan():
	* Partition the input tweets by list size (one tweet or multiple tweets)
	* and by timestamp order (already ordered or not ordered).
	* Test a single tweet and multiple tweets whose timestamps are not
	* in chronological order.
	*
	* getMentionedUsers():
	* Partition tweets by whether they contain no mentions, one mention,
	* or multiple mentions. Also test repeated mentions and mentions
	* appearing alongside punctuation.
	  */

    
    private static final Instant d1 = Instant.parse("2016-02-17T10:00:00Z");
    private static final Instant d2 = Instant.parse("2016-02-17T11:00:00Z");
    
    private static final Tweet tweet1 = new Tweet(1, "alyssa", "is it reasonable to talk about rivest so much?", d1);
    private static final Tweet tweet2 = new Tweet(2, "bbitdiddle", "rivest talk in 30 minutes #hype", d2);
    
    @Test(expected=AssertionError.class)
    public void testAssertionsEnabled() {
        assert false; // make sure assertions are enabled with VM argument: -ea
    }
    
    @Test
    public void testGetTimespanTwoTweets() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1, tweet2));
        
        assertEquals("expected start", d1, timespan.getStart());
        assertEquals("expected end", d2, timespan.getEnd());
    }
    
    @Test
    public void testGetMentionedUsersNoMention() {
        Set<String> mentionedUsers = Extract.getMentionedUsers(Arrays.asList(tweet1));
        
        assertTrue("expected empty set", mentionedUsers.isEmpty());
    }

   
    @Test
    public void testGetTimespanOneTweet() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet1));

        assertEquals("start should be the tweet timestamp",
                d1, timespan.getStart());
        assertEquals("end should be the tweet timestamp",
                d1, timespan.getEnd());
    }

    @Test
    public void testGetTimespanReverseOrder() {
        Timespan timespan = Extract.getTimespan(Arrays.asList(tweet2, tweet1));

        assertEquals("start should be the earlier timestamp",
                d1, timespan.getStart());
        assertEquals("end should be the later timestamp",
                d2, timespan.getEnd());
    }

    @Test
    public void testGetMentionedUsersOneMention() {
        Tweet tweet = new Tweet(3, "alice", "hello @bob", d1);

        Set<String> mentionedUsers =
                Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("should contain one mentioned user",
                Set.of("bob"), mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersMultipleMentions() {
        Tweet tweet = new Tweet(4, "alice",
                "hello @bob and @charlie", d1);

        Set<String> mentionedUsers =
                Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("should contain both mentioned users",
                Set.of("bob", "charlie"), mentionedUsers);
    }

    @Test
    public void testGetMentionedUsersRepeatedMention() {
        Tweet tweet = new Tweet(5, "alice",
                "@bob hello @bob", d1);

        Set<String> mentionedUsers =
                Extract.getMentionedUsers(Arrays.asList(tweet));

        assertEquals("repeated mentions should produce one set entry",
                Set.of("bob"), mentionedUsers);
    }
   

    /*
     * Warning: all the tests you write here must be runnable against any
     * Extract class that follows the spec. It will be run against several staff
     * implementations of Extract, which will be done by overwriting
     * (temporarily) your version of Extract with the staff's version.
     * DO NOT strengthen the spec of Extract or its methods.
     * 
     * In particular, your test cases must not call helper methods of your own
     * that you have put in Extract, because that means you're testing a
     * stronger spec than Extract says. If you need such helper methods, define
     * them in a different class. If you only need them in this test class, then
     * keep them in this test class.
     */

}

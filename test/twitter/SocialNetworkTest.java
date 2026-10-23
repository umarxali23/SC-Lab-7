package twitter;

import static org.junit.Assert.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class SocialNetworkTest {

    /*
     * Testing strategy
     *
     * guessFollowsGraph():
     * Test empty input, no mentions, a single mention,
     * multiple mentions, repeated tweets, duplicate mentions,
     * self-mentions, and mixed-case usernames.
     *
     * Do not require one exact graph when the specification
     * permits multiple valid graphs.
     *
     * influencers():
     * Test empty graphs, users with no followers, a leading
     * influencer, different follower counts, tied counts,
     * and users who appear only as followed values.
     */

    private static long nextId = 1;

    private Tweet makeTweet(String author, String text) {
        return new Tweet(
                nextId++,
                author,
                text,
                Instant.now());
    }

    private Set<String> followsOf(
            Map<String, Set<String>> graph,
            String author) {

        Set<String> result = new HashSet<>();

        for (Map.Entry<String, Set<String>> entry
                : graph.entrySet()) {

            if (entry.getKey().equalsIgnoreCase(author)) {
                result.addAll(entry.getValue());
            }
        }

        return result;
    }

    private int followerCount(
            Map<String, Set<String>> graph,
            String username) {

        int count = 0;

        for (Set<String> followedUsers : graph.values()) {
            for (String followed : followedUsers) {
                if (followed.equalsIgnoreCase(username)) {
                    count++;
                }
            }
        }

        return count;
    }

    private void assertDescendingFollowerCounts(
            List<String> users,
            Map<String, Set<String>> graph) {

        for (int i = 0; i < users.size() - 1; i++) {
            assertTrue(
                    "Follower counts should be descending",
                    followerCount(graph, users.get(i))
                            >= followerCount(graph, users.get(i + 1)));
        }
    }

    // 1. Empty list of tweets

    @Test
    public void testGuessFollowsGraphEmpty() {

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(
                        new ArrayList<Tweet>());

        assertTrue(graph.isEmpty());
    }

    // 2. Tweets without mentions

    @Test
    public void testGuessFollowsGraphNoMentions() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("alice", "Hello everyone"),
                makeTweet("bob", "Today is a good day"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        // Any usernames in the result must come from the tweets.
        Set<String> validUsers =
                new HashSet<>(Arrays.asList("alice", "bob"));

        for (Map.Entry<String, Set<String>> entry
                : graph.entrySet()) {

            assertTrue(validUsers.contains(entry.getKey()));

            for (String followed : entry.getValue()) {
                assertTrue(validUsers.contains(followed));
            }
        }
    }

    // 3. Single mention

    @Test
    public void testGuessFollowsGraphSingleMention() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("ernie", "Hello @bert"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        assertTrue(followsOf(graph, "ernie").contains("bert"));
    }

    // 4. Multiple mentions in one tweet

    @Test
    public void testGuessFollowsGraphMultipleMentions() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("alice", "Hello @bob and @charlie"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        Set<String> follows = followsOf(graph, "alice");

        assertTrue(follows.contains("bob"));
        assertTrue(follows.contains("charlie"));
    }

    // 5. Multiple tweets from the same user

    @Test
    public void testGuessFollowsGraphMultipleTweets() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("ernie", "Hi @bert"),
                makeTweet("ernie", "Hello @elmo"),
                makeTweet("ernie", "Hi again @grover"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        Set<String> follows = followsOf(graph, "ernie");

        assertTrue(follows.contains("bert"));
        assertTrue(follows.contains("elmo"));
        assertTrue(follows.contains("grover"));
    }

    // 6. Duplicate mentions

    @Test
    public void testGuessFollowsGraphDuplicateMentions() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("ernie", "@bert @bert @bert"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        Set<String> follows = followsOf(graph, "ernie");

        assertTrue(follows.contains("bert"));
        assertEquals(1, countOccurrences(follows, "bert"));
    }

    private int countOccurrences(Set<String> users, String target) {

        int count = 0;

        for (String user : users) {
            if (user.equalsIgnoreCase(target)) {
                count++;
            }
        }

        return count;
    }

    // 7. A user cannot follow themselves

    @Test
    public void testGuessFollowsGraphSelfMention() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("ernie", "Hello @ernie and @bert"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        Set<String> follows = followsOf(graph, "ernie");

        assertFalse(follows.contains("ernie"));
        assertTrue(follows.contains("bert"));
    }

    // 8. Usernames are case-insensitive

    @Test
    public void testGuessFollowsGraphCaseInsensitive() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("Ernie", "Hello @BERT"),
                makeTweet("ERNIE", "Hello @Elmo"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        assertTrue(followsOf(graph, "ernie").contains("bert"));
        assertTrue(followsOf(graph, "ernie").contains("elmo"));

        int ernieKeys = 0;

        for (String key : graph.keySet()) {
            if (key.equalsIgnoreCase("ernie")) {
                ernieKeys++;
            }
        }

        assertEquals(1, ernieKeys);
    }

    // 9. Email addresses are not mentions

    @Test
    public void testEmailAddressIsNotMention() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("alice", "Contact me at alice@mit.edu"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        assertFalse(followsOf(graph, "alice").contains("mit"));
    }

    // 10. Empty graph for influencers

    @Test
    public void testInfluencersEmpty() {

        Map<String, Set<String>> graph = new HashMap<>();

        List<String> result =
                SocialNetwork.influencers(graph);

        assertTrue(result.isEmpty());
    }

    // 11. Single user without followers

    @Test
    public void testInfluencersSingleUser() {

        Map<String, Set<String>> graph = new HashMap<>();

        graph.put("alice", new HashSet<String>());

        List<String> result =
                SocialNetwork.influencers(graph);

        assertEquals(1, result.size());
        assertEquals("alice", result.get(0));
    }

    // 12. One clear influencer

    @Test
    public void testInfluencersSingleInfluencer() {

        Map<String, Set<String>> graph = new HashMap<>();

        graph.put("alice",
                new HashSet<>(Arrays.asList("bob")));

        graph.put("charlie",
                new HashSet<>(Arrays.asList("bob")));

        graph.put("david",
                new HashSet<>(Arrays.asList("bob")));

        graph.put("erin", new HashSet<String>());

        List<String> result =
                SocialNetwork.influencers(graph);

        assertEquals(5, result.size());
        assertEquals("bob", result.get(0));

        assertDescendingFollowerCounts(result, graph);
    }

    // 13. Multiple influencers with different follower counts

    @Test
    public void testInfluencersMultipleInfluencers() {

        Map<String, Set<String>> graph = new HashMap<>();

        graph.put("alice",
                new HashSet<>(Arrays.asList("bob", "charlie")));

        graph.put("david",
                new HashSet<>(Arrays.asList("bob", "charlie")));

        graph.put("erin",
                new HashSet<>(Arrays.asList("bob")));

        graph.put("frank",
                new HashSet<>(Arrays.asList("bob")));

        List<String> result =
                SocialNetwork.influencers(graph);

        assertEquals(6, result.size());

        assertEquals("bob", result.get(0));
        assertEquals("charlie", result.get(1));

        assertDescendingFollowerCounts(result, graph);
    }

    // 14. Tied follower counts

    @Test
    public void testInfluencersTied() {

        Map<String, Set<String>> graph = new HashMap<>();

        graph.put("alice",
                new HashSet<>(Arrays.asList(
                        "bob", "charlie", "david")));

        graph.put("erin",
                new HashSet<>(Arrays.asList("bob", "charlie")));

        List<String> result =
                SocialNetwork.influencers(graph);

        assertEquals(5, result.size());

        Set<String> expectedTopTwo =
                new HashSet<>(Arrays.asList("bob", "charlie"));

        Set<String> actualTopTwo =
                new HashSet<>(result.subList(0, 2));

        assertEquals(expectedTopTwo, actualTopTwo);
        assertEquals("david", result.get(2));

        assertDescendingFollowerCounts(result, graph);
    }

    // 15. User who appears only as a followed value

    @Test
    public void testInfluencersUserOnlyAsFollowedValue() {

        Map<String, Set<String>> graph = new HashMap<>();

        graph.put("alice",
                new HashSet<>(Arrays.asList("bob")));

        List<String> result =
                SocialNetwork.influencers(graph);

        assertEquals(2, result.size());
        assertEquals("bob", result.get(0));
        assertTrue(result.contains("alice"));
    }

    // 16. End-to-end test

    @Test
    public void testGuessThenInfluencers() {

        List<Tweet> tweets = Arrays.asList(
                makeTweet("alice", "Hi @bob"),
                makeTweet("charlie", "Hi @bob and @alice"));

        Map<String, Set<String>> graph =
                SocialNetwork.guessFollowsGraph(tweets);

        List<String> result =
                SocialNetwork.influencers(graph);

        assertTrue(followsOf(graph, "alice").contains("bob"));
        assertTrue(followsOf(graph, "charlie").contains("bob"));

        assertEquals(3, result.size());
        assertEquals("bob", result.get(0));
    }
}
package twitter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class SocialNetwork {

    /**
     * Infer follow relationships from tweets.
     *
     * If a tweet author mentions another user, the author
     * follows that user.
     *
     * @param tweets list of tweets, not modified
     * @return inferred social network
     */
    public static Map<String, Set<String>> guessFollowsGraph(
            List<Tweet> tweets) {

        Map<String, Set<String>> graph = new HashMap<>();

        for (Tweet tweet : tweets) {

            String author = tweet.getAuthor()
                    .toLowerCase(Locale.ROOT);

            Set<String> followedUsers =
                    graph.computeIfAbsent(
                            author, key -> new HashSet<>());

            Set<String> mentions =
                    Extract.getMentionedUsers(
                            Collections.singletonList(tweet));

            for (String username : mentions) {

                String mentionedUser = username
                        .toLowerCase(Locale.ROOT);

                if (!author.equals(mentionedUser)) {
                    followedUsers.add(mentionedUser);
                }
            }
        }

        return graph;
    }

    /**
     * Return all distinct users sorted by descending
     * follower count.
     *
     * @param followsGraph social network
     * @return users ordered by follower count
     */
    public static List<String> influencers(
            Map<String, Set<String>> followsGraph) {

        Map<String, Integer> followerCounts = new HashMap<>();

        for (Map.Entry<String, Set<String>> entry
                : followsGraph.entrySet()) {

            String author = entry.getKey()
                    .toLowerCase(Locale.ROOT);

            followerCounts.putIfAbsent(author, 0);

            for (String followedUser : entry.getValue()) {

                String followed = followedUser
                        .toLowerCase(Locale.ROOT);

                followerCounts.putIfAbsent(followed, 0);

                followerCounts.put(
                        followed,
                        followerCounts.get(followed) + 1);
            }
        }

        List<String> result =
                new ArrayList<>(followerCounts.keySet());

        result.sort(
                Comparator.comparingInt(
                        (String user) -> followerCounts.get(user))
                        .reversed()
                        .thenComparing(Comparator.naturalOrder()));

        return result;
    }
}
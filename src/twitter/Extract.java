package twitter;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extract consists of methods that extract information from a list of tweets.
 * 
 * DO NOT change the method signatures or specifications of these methods, but
 * you may implement their body as you see fit.
 */
public class Extract {

    /**
     * Get the time period spanned by all the tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return a minimum-length time interval that contains the timestamp of
     *         every tweet in the list.
     */
    public static Timespan getTimespan(List<Tweet> tweets) {
        if (tweets.isEmpty()) {
            throw new IllegalArgumentException("Tweets list cannot be empty");
        }
        
        Instant minTime = tweets.get(0).getTimestamp();
        Instant maxTime = minTime;
        
        for (Tweet tweet : tweets) {
            Instant t = tweet.getTimestamp();
            if (t.isBefore(minTime)) {
                minTime = t;
            }
            if (t.isAfter(maxTime)) {
                maxTime = t;
            }
        }
        
        return new Timespan(minTime, maxTime);
    }

    /**
     * Get usernames mentioned in a list of tweets.
     * 
     * @param tweets
     *            list of tweets with distinct ids, not modified by this method.
     * @return the set of usernames who are mentioned in the text of the tweets.
     *         A username-mention begins with '@' and is followed by zero or more
     *         letters, numbers, and underscores. The set is case-insensitive:
     *         if a tweet mentions "@Alice", the returned set may include
     *         "alice" or "Alice", but not both.
     */
    public static Set<String> getMentionedUsers(List<Tweet> tweets) {
        Set<String> mentionedUsers = new HashSet<>();
        // Matches @ followed by word characters
        Pattern pattern = Pattern.compile("(^|[^\\w])@([a-zA-Z0-9_]+)");
        
        for (Tweet tweet : tweets) {
            Matcher matcher = pattern.matcher(tweet.getText());
            while (matcher.find()) {
                mentionedUsers.add(matcher.group(2).toLowerCase());
            }
        }
        
        return mentionedUsers;
    }
}
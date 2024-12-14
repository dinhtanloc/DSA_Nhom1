package ueh.service;

import ueh.model.HtmlData;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Stack;
import ueh.util.Queue;


@Service
public class HtmlFilterService {


    public static Map<String, List<String>> extractContentWithoutTags(String html) {
        String regex = "<(/?\\w+)([^>]*?)>";
        Pattern tagPattern = Pattern.compile(regex);
        Matcher matcher = tagPattern.matcher(html);

        Stack<String> tagStack = new Stack<>();
        Queue<String> closingQueue = new Queue<>();
        Map<String, List<String>> contentMap = new HashMap<>();
        int lastIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                String content = html.substring(lastIndex, matcher.start()).trim();
                if (!content.isEmpty() && !tagStack.isEmpty()) {
                    String currentTag = tagStack.peek();
                    contentMap.computeIfAbsent(currentTag, k -> new ArrayList<>()).add(content);
                }
            }

            String tag = matcher.group(1).toLowerCase();

            if (matcher.group(2).endsWith("/")) {
                lastIndex = matcher.end();
                continue;
            }

            if (tag.equals("meta") || tag.equals("link")) {
                lastIndex = matcher.end();
                continue;
            }

            if (!tag.startsWith("/")) {
                tagStack.push(tag);
            } else {
                closingQueue.enqueue(tag.substring(1));
                if (tagStack.isEmpty() || !tagStack.peek().equals(closingQueue.peek())) {
                    throw new IllegalArgumentException("Mismatched closing tag: " + tag);
                }
                closingQueue.dequeue();
                tagStack.pop();
            }

            lastIndex = matcher.end();
        }
        if (lastIndex < html.length()) {
            String content = html.substring(lastIndex).trim();
            if (!content.isEmpty() && !tagStack.isEmpty()) {
                String currentTag = tagStack.peek();
                contentMap.computeIfAbsent(currentTag, k -> new ArrayList<>()).add(content);
            }
        }
        if (!tagStack.isEmpty()) {
            throw new IllegalArgumentException("Unclosed tags remain: " + tagStack);
        }

        return contentMap;
    }
}

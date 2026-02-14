package ru.practicum.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "comment")
public class CommentProperties {

    private boolean moderationEnabled = false;
    private int maxLength = 2000;
    private int minLength = 10;
    private int replyDepthMax = 1;
    private boolean allowMultipleComments = false;
}
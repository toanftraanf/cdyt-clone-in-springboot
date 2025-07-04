package com.cdyt.be.scheduler;

import com.cdyt.be.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountSyncScheduler {

    private final StringRedisTemplate redisTemplate;
    private final ArticleRepository articleRepository;

    private static final String VIEW_KEY_PREFIX = "article:view:";

    @Value("${viewcount.sync.limit:500}")
    private int batchLimit;

    /**
     * Chạy mỗi phút: quét Redis, flush viewCount về DB
     */
    @Scheduled(cron = "0 * * * * *")
    public void flushViewCounts() {
        Set<String> keys = redisTemplate.keys(VIEW_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty())
            return;
        int counter = 0;
        for (String key : keys) {
            try {
                Long diff = redisTemplate.opsForValue().getAndDelete(key) != null
                        ? Long.valueOf(redisTemplate.opsForValue().getAndDelete(key))
                        : null;
                if (diff == null || diff == 0)
                    continue;
                Long articleId = Long.valueOf(key.substring(VIEW_KEY_PREFIX.length()));
                articleRepository.incrementViewCount(articleId, diff);
                counter++;
                if (counter >= batchLimit)
                    break; // tránh update quá lớn 1 lần
            } catch (Exception e) {
                log.error("Failed to flush view count for key {}: {}", key, e.getMessage());
            }
        }
        if (counter > 0)
            log.info("Flushed {} view-count keys to DB", counter);
    }
}
package com.shop.chatbot.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

@Service
public class AiConversationMemoryService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AiConversationMemoryService.class
            );

    private static final String KEY_PREFIX =
            "bloodops:ai:conversation:";

    private static final String LAST_INTENT =
            "lastIntent";

    private static final String LAST_KEYWORD =
            "lastKeyword";

    private final StringRedisTemplate redisTemplate;
    private final Duration conversationTtl;

    public AiConversationMemoryService(
            StringRedisTemplate redisTemplate,
            @Value("${ai.conversation.ttl}")
            Duration conversationTtl
    ) {
        this.redisTemplate = redisTemplate;
        this.conversationTtl = conversationTtl;
    }

    public Optional<ConversationMemory> find(
            String conversationId
    ) {
        if (!isValidConversationId(conversationId)) {
            return Optional.empty();
        }

        String key = createKey(conversationId);

        try {
            Map<Object, Object> values =
                    redisTemplate
                            .opsForHash()
                            .entries(key);

            if (values.isEmpty()) {
                return Optional.empty();
            }

            String lastIntent =
                    toNullableString(
                            values.get(LAST_INTENT)
                    );

            String lastKeyword =
                    toNullableString(
                            values.get(LAST_KEYWORD)
                    );

            /*
             * 대화를 이어갈 때마다 TTL을 갱신하는
             * sliding expiration 방식입니다.
             */
            redisTemplate.expire(
                    key,
                    conversationTtl
            );

            return Optional.of(
                    new ConversationMemory(
                            lastIntent,
                            lastKeyword
                    )
            );

        } catch (DataAccessException exception) {
            log.warn(
                    "Redis 대화 문맥 조회 실패: conversationId={}",
                    conversationId,
                    exception
            );

            return Optional.empty();
        }
    }

    public void save(
            String conversationId,
            String lastIntent,
            String lastKeyword
    ) {
        if (
                !isValidConversationId(conversationId)
                        || lastIntent == null
                        || lastIntent.isBlank()
        ) {
            return;
        }

        String key = createKey(conversationId);

        try {
            redisTemplate
                    .opsForHash()
                    .put(
                            key,
                            LAST_INTENT,
                            lastIntent
                    );

            if (
                    lastKeyword == null
                            || lastKeyword.isBlank()
            ) {
                redisTemplate
                        .opsForHash()
                        .delete(
                                key,
                                LAST_KEYWORD
                        );
            } else {
                redisTemplate
                        .opsForHash()
                        .put(
                                key,
                                LAST_KEYWORD,
                                lastKeyword
                        );
            }

            redisTemplate.expire(
                    key,
                    conversationTtl
            );

        } catch (DataAccessException exception) {
            /*
             * Redis 장애가 챗봇 전체 장애로 번지지 않게
             * 저장 실패만 기록하고 일반 단일 질문 처리는 유지합니다.
             */
            log.warn(
                    "Redis 대화 문맥 저장 실패: conversationId={}",
                    conversationId,
                    exception
            );
        }
    }

    public void delete(
            String conversationId
    ) {
        if (!isValidConversationId(conversationId)) {
            return;
        }

        try {
            redisTemplate.delete(
                    createKey(conversationId)
            );

        } catch (DataAccessException exception) {
            log.warn(
                    "Redis 대화 문맥 삭제 실패: conversationId={}",
                    conversationId,
                    exception
            );
        }
    }

    private String createKey(
            String conversationId
    ) {
        return KEY_PREFIX + conversationId;
    }

    private boolean isValidConversationId(
            String conversationId
    ) {
        return conversationId != null
                && !conversationId.isBlank()
                && conversationId.length() <= 100
                && conversationId.matches(
                "[a-zA-Z0-9_-]+"
        );
    }

    private String toNullableString(
            Object value
    ) {
        if (value == null) {
            return null;
        }

        String text = value.toString();

        return text.isBlank()
                ? null
                : text;
    }

    public record ConversationMemory(
            String lastIntent,
            String lastKeyword
    ) {
    }
}
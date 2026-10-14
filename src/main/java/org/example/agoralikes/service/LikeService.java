package org.example.agoralikes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.agoralikes.dto.event.LikeCreatedEvent;
import org.example.agoralikes.dto.event.LikeDeletedEvent;
import org.example.agoralikes.entity.like.Like;
import org.example.agoralikes.exception.LikeAlreadyExistsException;
import org.example.agoralikes.repository.LikeRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LikeService {

    private final LikeRepository likeRepository;
     private final RabbitTemplate rabbitTemplate; // Для уведомлений

    @Value("${rabbitmq.exchange.likes}")
    private String likesExchange;

    @Value("${rabbitmq.routing.key.like-created}")
    private String likeCreatedRoutingKey;

    @Value("${rabbitmq.routing.key.like-deleted}")
    private String likeDeletedRoutingKey;

    @Transactional
    public void addLike(Long postId, Long userId, String whoLiked, String avatarUrl) {
        // 1. Проверка на дубликат (если база не успела отбить unique constraint)
        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            throw new LikeAlreadyExistsException("Вы уже лайкнули этот пост!");
        }

        // 2. Сохраняем лайк
        Like like = new Like();
        like.setPostId(postId);
        like.setUserId(userId);
        likeRepository.save(like);

        // 3. Отправляем событие в RabbitMQ
        // 3. Создаем событие с НАСТОЯЩИМ ИМЕНЕМ
        LikeCreatedEvent event = new LikeCreatedEvent(
                postId,
                whoLiked,
                avatarUrl,
                null,
                Instant.now()
        );

        // 4. Отправляем
        rabbitTemplate.convertAndSend(likesExchange, likeCreatedRoutingKey, event);
        log.info("📨 [RABBIT] Лайк отправлен от пользователя: {}", whoLiked);
    }

    @Transactional
    public void removeLike(Long postId, Long userId, String username) {

        Long deletedCount = likeRepository.deleteByPostIdAndUserId(postId, userId);

        // 3. Если удалили 0 записей, значит лайка и не было.
        // Просто выходим, не выбрасывая ошибку. Фронтенд получит 200 OK.
        if (deletedCount == 0) {
            log.warn("Попытка удалить отсутствующий лайк (userId={}, postId={}). Считаем операцию успешной.", userId, postId);
            return;
        }

        // 2. Создаем событие удаления
        LikeDeletedEvent event = new LikeDeletedEvent(
                postId,
                username,
                Instant.now()
        );

        // 3. Отправляем в RabbitMQ
        rabbitTemplate.convertAndSend(likesExchange, likeDeletedRoutingKey, event);
        log.info("🗑️ [RABBIT] Лайк удален пользователем: {}", username);
    }

    public List<Long> getLikedPostIds(Long userId, List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return likeRepository.findPostIdsByUserIdAndPostIds(userId, postIds);
    }

}

package org.example.agoralikes.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.agoralikes.dto.event.LikeCreatedEvent;
import org.example.agoralikes.dto.event.LikeDeletedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

// LikeService публикует события через ApplicationEventPublisher, а в Kafka они уходят
// только после успешного commit: если транзакция откатится, событие не будет отправлено.
// Без активной транзакции @TransactionalEventListener событие молча пропускает.
@Slf4j
@Component
@RequiredArgsConstructor
public class LikeEventKafkaPublisher {

    private static final String LIKES_TOPIC = "agora.likes";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeCreated(LikeCreatedEvent event) {
        send(event.postId(), event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLikeDeleted(LikeDeletedEvent event) {
        send(event.postId(), event);
    }

    // Лайк уже сохранён/удалён в БД, поэтому сбой Kafka не должен ронять HTTP-ответ.
    // Событие при сбое теряется — надёжная доставка потребует Transactional Outbox.
    private void send(Long postId, Object event) {
        String key = postId.toString(); // ключ = postId → created/deleted одного поста в одной партиции, по порядку
        String type = event.getClass().getSimpleName();

        try {
            kafkaTemplate.send(LIKES_TOPIC, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("❌ [KAFKA] Не удалось отправить {} (postId={}) в {}", type, postId, LIKES_TOPIC, ex);
                            return;
                        }
                        var metadata = result.getRecordMetadata();
                        log.info("📤 [KAFKA] {} отправлен: postId={}, partition={}, offset={}",
                                type, postId, metadata.partition(), metadata.offset());
                    });
        } catch (Exception ex) {
            // Синхронный сбой ещё до отправки: ошибка сериализации, таймаут получения метаданных топика
            log.error("❌ [KAFKA] Ошибка при подготовке {} (postId={})", type, postId, ex);
        }
    }
}

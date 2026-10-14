package org.example.agoralikes.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.agoralikes.configuration.swagger.doc.*;
import org.example.agoralikes.service.LikeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/likes")
@RequiredArgsConstructor
@Tag(name = "Likes API", description = "API для проставления и удаления лайков на публикации (Требует JWT токен)")
@ApiErrorResponse400
@ApiErrorResponse401
@ApiErrorResponse404
@ApiErrorResponse500
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{postId}")
    @Operation(summary = "Поставить лайк", description = "Поставить лайк к посту (Нужен ID поста)")
    @ApiResponse(responseCode = "204", description = "Лайк успешно поставлен")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void likePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId, // <--- Читаем ID из заголовка
            @RequestHeader(value = "X-User-Name", required = false) String username,
            @RequestHeader(value = "X-User-Avatar", required = false) String avatarUrl
    ) {
        likeService.addLike(postId, userId, username, avatarUrl);
    }

    @DeleteMapping("/{postId}")
    @Operation(summary = "Удалить лайк", description = "Удалить лайк с поста (Нужен ID поста)")
    @ApiResponse(responseCode = "204", description = "Лайк успешно удален")
    @ApiErrorResponse403
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikePost(
            @PathVariable Long postId,
            @RequestHeader("X-User-Id") Long userId,
            @RequestHeader(value = "X-User-Name", required = false) String username
    ) {
        likeService.removeLike(postId, userId, username);
    }

    @PostMapping("/check")
    @Operation(summary = "Проверка лайков", description = "Возвращает ID постов из списка, которые лайкнул пользователь")
    public List<Long> checkLikes(
            @RequestBody List<Long> postIds,
            @RequestHeader("X-User-Id") Long userId
    ) {
        return likeService.getLikedPostIds(userId, postIds);
    }

}

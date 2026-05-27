package org.cinos.core.posts.events;

import lombok.Builder;

@Builder
public record PostCreateEvent (Long userId, Long postId){
}

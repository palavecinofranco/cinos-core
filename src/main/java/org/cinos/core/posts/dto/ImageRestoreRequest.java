package org.cinos.core.posts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRestoreRequest {
    private String imageUrl;
    private int originalWidth;
    private int originalHeight;
}

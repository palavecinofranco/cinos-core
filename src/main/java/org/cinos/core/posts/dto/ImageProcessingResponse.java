package org.cinos.core.posts.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageProcessingResponse {
    private String originalUrl;
    private String mediumUrl;
    private String thumbnailUrl;
    private String smallUrl;
    private int width;
    private int height;
    private long size;
    private String format;
}

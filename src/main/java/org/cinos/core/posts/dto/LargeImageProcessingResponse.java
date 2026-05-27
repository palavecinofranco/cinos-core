package org.cinos.core.posts.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LargeImageProcessingResponse {
    private String originalUrl;
    private String mediumUrl;
    private String thumbnailUrl;
    private int originalWidth;
    private int originalHeight;
    private int processedWidth;
    private int processedHeight;
    private long originalSize;
    private long processedSize;
    private String format;
    private String compressionRatio;
}

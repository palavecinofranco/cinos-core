package org.cinos.core.posts.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageInfoResponse {
    private String url;
    private int width;
    private int height;
}

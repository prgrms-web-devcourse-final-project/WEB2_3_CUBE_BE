package com.roome.domain.mybookreview.service.request;

public record MyBookReviewUpdateRequest (
        String title,
        String quote,
        String takeaway,
        String freeFormText,
        String coverColor
) {
}

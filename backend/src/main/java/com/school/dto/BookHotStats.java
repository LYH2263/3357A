package com.school.dto;

import lombok.Data;

@Data
public class BookHotStats {
    private Integer bookId;
    private String title;
    private String author;
    private String category;
    private Integer borrowCount;
    private Integer totalCount;
    private Integer availableCount;
    private String coverImage;
}

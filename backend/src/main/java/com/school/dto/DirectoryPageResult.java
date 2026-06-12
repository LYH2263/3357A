package com.school.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class DirectoryPageResult<T> {
    private List<T> records;
    private long total;
    private long pageNum;
    private long pageSize;
    private long pages;
    private Map<String, List<T>> groupedRecords;
    private List<String> availableInitials;
    private List<DirectoryGroupStats> groupStats;

    @Data
    public static class DirectoryGroupStats {
        private String groupKey;
        private String groupName;
        private long count;
    }
}

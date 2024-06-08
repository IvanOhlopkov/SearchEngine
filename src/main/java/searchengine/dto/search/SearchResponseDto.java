package searchengine.dto.search;

import lombok.Data;

import java.util.List;

@Data
public class SearchResponseDto {
    private boolean result;
    private int count;
    private List<SearchDataDto> data;
    private String error;
}

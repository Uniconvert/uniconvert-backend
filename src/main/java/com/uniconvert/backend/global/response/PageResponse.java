//PageResponse
//지출 목록이 20개 있다고 했을 때, 이를 5개씩 보여준다거나.. 페이지 단위로 잘라서 보여줄 때 사용할 공통 규격
package com.uniconvert.backend.global.response;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        boolean hasNext
) {
    public static <T> PageResponse<T> of(List<T> content, int page, int size, boolean hasNext) {
        return new PageResponse<>(content, page, size, hasNext);
    }
}
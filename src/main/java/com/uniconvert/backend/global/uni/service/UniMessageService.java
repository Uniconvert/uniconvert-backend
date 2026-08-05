package com.uniconvert.backend.global.uni.service;

import com.uniconvert.backend.global.uni.dto.UniMessageBundleResponse;
import com.uniconvert.backend.global.uni.dto.UniMessageResponse;
import com.uniconvert.backend.global.uni.enums.UniSection;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UniMessageService {

    public UniMessageBundleResponse createBundle(
            UniSection section,
            List<UniMessageResponse> insights
    ) {
        List<UniMessageResponse> entryMessages =
                UniMessageCatalog.getEntryMessages(section);

        List<UniMessageResponse> randomMessages =
                new ArrayList<>();

        /*
         * 실제 데이터로 만든 인사이트를 우선 랜덤 풀에 포함합니다.
         */
        if (insights != null) {
            randomMessages.addAll(insights);
        }

        /*
         * 화면별 정적 랜덤 멘트 10개를 추가합니다.
         */
        randomMessages.addAll(
                UniMessageCatalog.getRandomMessages(section)
        );

        return new UniMessageBundleResponse(
                List.copyOf(entryMessages),
                List.copyOf(randomMessages)
        );
    }
}

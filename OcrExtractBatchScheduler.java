package com.refine.ocr.scheduler;

import com.refine.ocr.dao.OcrDAO;
import com.refine.ocr.service.OcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OcrExtractBatchScheduler {

    private static final Logger logger = LoggerFactory.getLogger(OcrExtractBatchScheduler.class);

    @Autowired
    private OcrDAO ocrDAO;

    @Autowired
    private OcrService ocrService;

    /**
     * 3분마다 실행 - 최근 30분 내 extract 미처리 OCR 결과를 찾아서 자동 처리
     */
    @Scheduled(fixedRate = 180000)
    public void processUnextractedOcrResults() {
        try {
            List<String> unprocessedList = ocrDAO.getUnprocessedOcrRsltNos();

            if (unprocessedList == null || unprocessedList.isEmpty()) {
                return;
            }

            logger.info("[OCR Extract 배치] 미처리 건수: {}", unprocessedList.size());

            int successCount = 0;
            int failCount = 0;

            for (String ocrRsltNo : unprocessedList) {
                try {
                    ocrService.saveOcrExtractData(ocrRsltNo);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    logger.error("[OCR Extract 배치] 처리 실패 - ocr_rslt_no: {}, error: {}", ocrRsltNo, e.getMessage());
                }
            }

            logger.info("[OCR Extract 배치] 완료 - 성공: {}, 실패: {}", successCount, failCount);

        } catch (Exception e) {
            logger.error("[OCR Extract 배치] 배치 실행 오류: {}", e.getMessage(), e);
        }
    }
}

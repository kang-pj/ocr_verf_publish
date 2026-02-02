package com.refine.ocr.dao.impl;

import java.util.List;
import java.util.Map;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.refine.ocr.dao.OcrDAO;
import com.refine.ocr.vo.OcrInfoVO;

/**
 * OCR 문서 관리 DAO 구현체
 */
@Repository
public class OcrDAOImpl implements OcrDAO {
    
    @Autowired
    private SqlSessionTemplate sqlSession;
    
    @Override
    public int getOcrDocumentCount(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.getOcrDocumentCount", params);
    }
    
    @Override
    public List<OcrInfoVO> getOcrDocumentList(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getOcrDocumentList", params);
    }
    
    @Override
    public OcrInfoVO getOcrDocumentDetail(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.getOcrDocumentDetail", params);
    }
    
    @Override
    public List<OcrInfoVO> getDocumentListByCtrlNo(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getDocumentListByCtrlNo", params);
    }
    
    @Override
    public List<OcrInfoVO> getAllFilesByCtrlNo(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getAllFilesByCtrlNo", params);
    }
    
    @Override
    public List<OcrInfoVO> getOcrResultText(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getOcrResultText", params);
    }
    
    @Override
    public List<String> getOcrDocNoList(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getOcrDocNoList", params);
    }
    
    @Override
    public int updateVerificationStatus(Map<String, Object> params) {
        return sqlSession.update("ocr.updateVerificationStatus", params);
    }
    
    @Override
    public int updateOcrStatus(Map<String, Object> params) {
        return sqlSession.update("ocr.updateOcrStatus", params);
    }
    
    @Override
    public String getDocumentName(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.getDocumentName", params);
    }
    
    @Override
    public List<Map<String, Object>> getDocumentNamesBatch(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getDocumentNamesBatch", params);
    }
    
    @Override
    public OcrInfoVO getImageInfo(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.getImageInfo", params);
    }
    
    @Override
    public String getSysClsCd(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.getSysClsCd", params);
    }
    
    @Override
    public String getMaxCtrlNo(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.getMaxCtrlNo", params);
    }
    
    @Override
    public int insertOcrDocument(Map<String, Object> params) {
        return sqlSession.insert("ocr.insertOcrDocument", params);
    }
    
    @Override
    public int deleteOcrResult(Map<String, Object> params) {
        return sqlSession.delete("ocr.deleteOcrResult", params);
    }
    
    @Override
    public int deleteOcrDocument(Map<String, Object> params) {
        return sqlSession.delete("ocr.deleteOcrDocument", params);
    }
    
    @Override
    public List<String> getOcrDocNoListByUserId(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getOcrDocNoListByUserId", params);
    }
    
    @Override
    public List<String> getUserTestFilePathList(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getUserTestFilePathList", params);
    }
    
    @Override
    public int deleteUserTestDocuments(Map<String, Object> params) {
        return sqlSession.delete("ocr.deleteUserTestDocuments", params);
    }
    
    @Override
    public List<com.refine.ocr.vo.OcrItemVO> getOcrItemList(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getOcrItemList", params);
    }
    
    @Override
    public int getOcrItemCount(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.getOcrItemCount", params);
    }
    
    @Override
    public int checkOcrItemExists(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.checkOcrItemExists", params);
    }
    
    @Override
    public int insertOcrItem(Map<String, Object> params) {
        return sqlSession.insert("ocr.insertOcrItem", params);
    }
    
    @Override
    public int updateOcrItem(Map<String, Object> params) {
        return sqlSession.update("ocr.updateOcrItem", params);
    }
    
    @Override
    public int deleteOcrItem(Map<String, Object> params) {
        return sqlSession.update("ocr.deleteOcrItem", params);
    }
    
    @Override
    public int activateOcrItem(Map<String, Object> params) {
        return sqlSession.update("ocr.activateOcrItem", params);
    }

    @Override
    public List<Map<String, Object>> getDocumentTypes(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getDocumentTypes", params);
    }

    @Override
    public OcrInfoVO getOcrByRsltNo(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.getOcrByRsltNo", params);
    }

    @Override
    public int insertOcrExtractDataBatch(Map<String, Object> params) {
        return sqlSession.insert("ocr.insertOcrExtractDataBatch", params);
    }

    @Override
    public int checkOcrExtractDataExists(Map<String, Object> params) {
        return sqlSession.selectOne("ocr.checkOcrExtractDataExists", params);
    }

    @Override
    public List<Map<String, Object>> getOcrExtractData(Map<String, Object> params) {
        return sqlSession.selectList("ocr.getOcrExtractData", params);
    }

    @Override
    public int updateOcrExtractFailType(Map<String, Object> params) {
        return sqlSession.update("ocr.updateOcrExtractFailType", params);
    }

    @Override
    public int deleteOcrExtractData(Map<String, Object> params) {
        return sqlSession.delete("ocr.deleteOcrExtractData", params);
    }
}

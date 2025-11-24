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
    
    private static final String NAMESPACE = "com.refine.ocr.mapper.OcrMapper";
    
    @Override
    public int getOcrDocumentCount(Map<String, Object> params) {
        return sqlSession.selectOne(NAMESPACE + ".getOcrDocumentCount", params);
    }
    
    @Override
    public List<OcrInfoVO> getOcrDocumentList(Map<String, Object> params) {
        return sqlSession.selectList(NAMESPACE + ".getOcrDocumentList", params);
    }
    
    @Override
    public OcrInfoVO getOcrDocumentDetail(Map<String, Object> params) {
        return sqlSession.selectOne(NAMESPACE + ".getOcrDocumentDetail", params);
    }
    
    @Override
    public List<OcrInfoVO> getDocumentListByCtrlNo(Map<String, Object> params) {
        return sqlSession.selectList(NAMESPACE + ".getDocumentListByCtrlNo", params);
    }
    
    @Override
    public List<OcrInfoVO> getOcrResultText(Map<String, Object> params) {
        return sqlSession.selectList(NAMESPACE + ".getOcrResultText", params);
    }
    
    @Override
    public List<String> getOcrDocNoList(Map<String, Object> params) {
        return sqlSession.selectList(NAMESPACE + ".getOcrDocNoList", params);
    }
    
    @Override
    public int updateVerificationStatus(Map<String, Object> params) {
        return sqlSession.update(NAMESPACE + ".updateVerificationStatus", params);
    }
    
    @Override
    public OcrInfoVO getImageInfo(Map<String, Object> params) {
        return sqlSession.selectOne(NAMESPACE + ".getImageInfo", params);
    }
    
    @Override
    public String getSysClsCd(Map<String, Object> params) {
        return sqlSession.selectOne(NAMESPACE + ".getSysClsCd", params);
    }
    
    @Override
    public String getMaxCtrlNo(Map<String, Object> params) {
        return sqlSession.selectOne(NAMESPACE + ".getMaxCtrlNo", params);
    }
    
    @Override
    public int insertOcrDocument(Map<String, Object> params) {
        return sqlSession.insert(NAMESPACE + ".insertOcrDocument", params);
    }
}

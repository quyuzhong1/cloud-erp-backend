package com.erp.server.bi.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiSubjectRefLayoutEntity;

import java.util.List;

/**
 * 专题与布局关系表(BiSubjectRefLayout)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:32:40
 */
public interface BiSubjectRefLayoutService  extends IService<BiSubjectRefLayoutEntity> {

  
    /**
     * 保存专题与布局关系表
     * @author yl
     * @date 2022-12-13 16:54
     * @param subjectId
     * @param layoutIds
     * @return void
     */
    void addSubjectRefLayout(String subjectId, List<String> layoutIds);

    void deleteBySubjectId(String subjectId);

    Boolean delete(String subjectId, String layoutId);
}

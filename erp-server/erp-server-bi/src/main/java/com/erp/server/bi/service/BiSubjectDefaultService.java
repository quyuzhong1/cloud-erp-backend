package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.entity.BiSubjectDefaultEntity;
import com.erp.model.bi.entity.BiSubjectEntity;

import java.util.List;

/**
 * @Classname BiSubjectDefaultService
 * @Description TODO
 * @Date 2022-12-09 14:19
 * @Created by yl
 */
public interface BiSubjectDefaultService extends IService<BiSubjectDefaultEntity> {
    List<BiSubjectDefaultEntity> getByUserId(String userId);

    Boolean setDefault(String id);

    
    /**
     * 根据专题id 删除默认信息
     * @author yl
     * @date 2022-12-13 12:00
     * @param subjectId
     * @return void
     */
    void deleteBySubjectId(String subjectId);


    List<BiSubjectEntity> getDefault(String userId);

}

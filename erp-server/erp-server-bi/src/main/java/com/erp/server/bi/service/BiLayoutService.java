package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.DeleteLayoutModuleDTO;
import com.erp.model.bi.dto.SubjectLayoutDTO;
import com.erp.model.bi.dto.SubjectLayoutDetailsDTO;
import com.erp.model.bi.entity.BiLayoutEntity;

/**
 * 布局表(BiLayout)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:28:27
 */
public interface BiLayoutService  extends IService<BiLayoutEntity> {


    /**
     * 添加专题与模块关系
     * @author yl
     * @date 2022-12-13 16:13
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addSubjectLayout(SubjectLayoutDTO dto);

    /**
     * 根据专题id 获取专题详情信息
     * @author yl
     * @date 2022-12-13 17:30
     * @param subjectId
     * @return com.erp.model.bi.dto.SubjectLayoutDetailsDTO
     */
    SubjectLayoutDetailsDTO subjectInfo(String subjectId);

    
    /**
     * 修改专题布局
     * @author yl
     * @date 2022-12-14 11:47
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateSubjectLayout(SubjectLayoutDetailsDTO dto);

    /**
     *
     * @author yl
     * @date 2022-12-14 12:23
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean deleteLayoutModule(DeleteLayoutModuleDTO dto);

    Boolean deleteLayout(DeleteLayoutModuleDTO dto);

    void copySubjectLayout(String newSubjectId, String subjectId);
}

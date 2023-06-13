package com.erp.server.bi.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.bi.dto.LayoutRefModuleDTO;
import com.erp.model.bi.entity.BiLayoutRefModuleEntity;

import java.util.List;

/**
 * 布局与模块关系表(BiLayoutRefModule)表服务接口
 *
 * @author yl
 * @since 2022-12-08 14:29:39
 */
public interface BiLayoutRefModuleService  extends IService<BiLayoutRefModuleEntity> {



    /**
     * 保存模块
     * @author yl
     * @date 2022-12-13 16:37
     * @param layoutId
     * @param blockNo
     * @param moduleIdList
     * @return void
     */
    void addLayoutRefModule(String  subjectId,String layoutId, String blockNo, List<LayoutRefModuleDTO> moduleIdList);

    
    /**
     * 根据布局id 查出 布局与模块关系
     * @author yl
     * @date 2022-12-14 9:24
     * @param layoutIdList
     * @return java.util.List<com.erp.model.bi.entity.BiLayoutRefModuleEntity>
     */
    List<BiLayoutRefModuleEntity> getByLayoutIds(List<String> layoutIdList);

    
    /**
     * 根据专题id 删除模块布局关系
     * @author yl
     * @date 2022-12-14 15:04
     * @param subjectId
     * @return void
     */
    void deleteBySubjectId(String subjectId);

    Boolean deleteLayoutModuleId(String layoutId, String moduleId);

    Boolean deleteLayout(String layoutId);

    void copyLayoutRefModule(String newSubjectId, String newLayoutId, String copyLayoutId);

    /**
     * 根据布局id 获取到对应的模块id
     * @author yl
     * @date 2023-06-13 10:46
     * @param layoutIdList
     * @return java.util.List<com.erp.model.bi.dto.LayoutRefModuleDTO.LayoutRefModuleInfoDTO>
     */
    List<LayoutRefModuleDTO.LayoutRefModuleInfoDTO> listByLayoutIds(List<String> layoutIdList);
}

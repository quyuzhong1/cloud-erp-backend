package com.erp.server.plm.service;
import com.erp.model.plm.entity.ApplicationCategoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ApplicationCategoryDTO;

import java.util.List;

/**
 * <p>
 * 产品应用分类 服务类
 * </p>
 *
 * @author liaohui
 * @since 2025-01-09
 */
public interface ApplicationCategoryService extends SuperService<ApplicationCategoryEntity> {

    /**
    * 新增
    * @author liaohui
    * @param dto 参数
    */
    BaseResultDTO.AddDTO add(ApplicationCategoryDTO.AddDTO dto);

    /**
    * 修改
    * @author liaohui
    * @param dto 参数
    */
    Boolean update(ApplicationCategoryDTO.UpdateDTO dto);


    /**
     * 删除
     * @param id id
     */
    void delete(String id);

    /**
     * 查询
     * @param searchKeyword 关键字
     */
    List<ApplicationCategoryDTO.ViewDTO> list(String searchKeyword);

    /**
     * 通过名字查询分类
     * @param applicationCategory 分类
     */
    ApplicationCategoryEntity getByName(String applicationCategory);

    /**
     * 更新金蝶同步状态
     */
    Boolean updateSyncKingdeeId(String id,String syncKingdeeId);
}

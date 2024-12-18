package com.erp.server.oms.service;

import com.common.business.service.SuperService;
import com.erp.model.oms.dto.SoB2cRefCategoryDTO;
import com.erp.model.oms.entity.SoB2cRefCategoryEntity;

import java.util.List;


/**
 * <p>
 * B2C销售订单分类表 服务类
 * </p>
 *
 * @author Will
 * @since 2023-08-18
 */
public interface SoB2cRefCategoryService extends SuperService<SoB2cRefCategoryEntity> {

    /**
     * @description: 根据主表ids查询
     * @author Will
     * @date: 2023/8/22 14:24
     * @param mainIds
     * @return List<SoB2cRefCategoryEntity>
     */
    List<SoB2cRefCategoryEntity> listByMainIds(List<String> mainIds);
    /**
     * @description: 根据主表id删除
     * @author Will
     * @date: 2023/8/22 14:32
     * @param mainIds
     * @return Boolean
     */
    Boolean deleteByMainIds(List<String> mainIds);
    /**
     * @description: 新增订单分类
     * @author Will
     * @date: 2023/8/30 18:20
     * @param addList
     * @param mainId
     * @return Boolean
     */
    Boolean add(List<SoB2cRefCategoryDTO.AddDTO> addList, String mainId);
    /**
     * @description: 更新订单分类
     * @author Will
     * @date: 2023/8/31 9:31
     * @param categoryIdList
     * @param mainId
     * @return Boolean
     */
    Boolean update(List<String> categoryIdList, String mainId);

    /**
     * 根据分类id 获取
     * @param categoryIdList
     * @return
     */
    List<SoB2cRefCategoryEntity>  listCategoryIdList(List<String> categoryIdList);

    /**
     * 根据分类id 删除
     * @author yl
     * @date 2023-11-07 11:51
     * @param categoryIds
     * @return void
     */
    void removeByCategoryIds(List<String> categoryIds);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/11/7 15:43
     * @param mainId
     * @param categoryIdList
     */
    void deleteByMainIdAndCategoryId(String mainId, List<String> categoryIdList);
    /**
     * @author jack
     * @date: 2024-12-10
     * @param soIds
     */
    List<SoB2cRefCategoryDTO.CategoryNamesDTO> listCategoryNamesBySoIds(List<String> soIds);
}

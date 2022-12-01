package com.erp.server.plm.service;

import com.erp.model.plm.dto.BasicCategoryDTO;
import com.erp.model.plm.dto.SaveBasicCategoryDTO;
import com.erp.model.plm.dto.UpdateBasicNameDTO;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.BasicCategoryEntity;

import java.util.List;

/**
 * <p>
 * 产品分类表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface BasicCategoryService extends IService<BasicCategoryEntity> {

    void addCategory(SaveBasicCategoryDTO dto);

    Boolean updateCategory(UpdateBasicNameDTO dto);

    List<BasicCategoryDTO> getTree();

    Boolean deleteById(String id);

    List<String> getPidList(String categoryId);

    /**
     * @Description 根据类别名称查询类别信息
     * @Author Luo_WG
     * @Date 2022/9/28 18:51
     * @param categoryName：类别名称
     * @return BasicCategoryEntity
     **/
    BasicCategoryEntity getCategoryByName(String categoryName);
    /**
     * @description: 查询最高级
     * @author Will
     * @date: 2022/11/22 13:59
     * @param id
     * @param bestEntity
     * @return void
     */
    void getBestEntity(String id,BasicCategoryEntity bestEntity);
}

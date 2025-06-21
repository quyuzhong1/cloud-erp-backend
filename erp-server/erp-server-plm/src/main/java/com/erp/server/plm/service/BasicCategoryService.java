package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BasicCategoryEntity;

import java.util.List;
import java.util.Map;

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
     * @param isMainCategory：是否是一级主类别
     * @return BasicCategoryEntity
     **/
    BasicCategoryEntity getCategoryByName(String categoryName, Boolean isMainCategory);
    /**
     * @description: 查询最高级
     * @author Will
     * @date: 2022/11/22 13:59
     * @param id
     * @param bestEntity
     * @return void
     */
    void getBestEntity(String id,BasicCategoryEntity bestEntity);

    /**
     * @description: 查询品类
     * @author Will
     * @date: 2022/12/26 11:50
     * @param params
     * @return BasicCategoryDTO
     */
    BasicCategoryDTO getCategoryByParam(Map<String, String> params);
    /**
     * @description: 根据品类id查询所有父级分类及本身分类
     * @author Will
     * @date: 2023/1/7 9:37
     * @param categoryId
     * @return List<BasicCategoryEntity>
     */
    List<BasicCategoryEntity> listParentEntity(String categoryId);

    List<BasicCategoryDTO> getListTree(String type);


    List<BasicCategoryTreeDTO> getDbTree();

    List<String> getChildrenCategoryIds(String categoryId);
    /**
     * 更新金蝶同步状态
     */
    Boolean updateSyncKingdeeId(String id,String syncKingdeeId);

    /**
     * 获取到父级分类
     * @author yl
     * @date 2023-09-15 9:42
     * @param
     * @return java.util.List<com.erp.model.plm.entity.BasicCategoryEntity>
     */
    List<BasicCategoryEntity> listParentCategory();

    /**
     * 通过子类id或名称获取到父级的分类
     */
    BasicCategoryDTO getParentCategoryByParam(Map<String, String> params);

    /**
     * 获取品类列表
     * @return
     */
    List<BasicCategoryEntity> getCategoryList();

    /**
     * 品类列表
     * @param grade
     * @return
     */
    List<CategoryControllerDTO.CategoryDropDownDTO> listCategoryDropDown(Integer grade);

    /**
     * 获取二级分类的列表 拼接一级名称
     * @return
     */
    List<BasicCategoryTreeDTO> categoryGradeDown();

    List<BasicCategoryDTO> getCategoryByPid(String pid);

    String getParentName(String categoryId);

    List<BasicCategoryDTO.DropdownDTO> getCategoryDropdown();
}

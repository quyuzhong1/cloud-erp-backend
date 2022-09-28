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
}

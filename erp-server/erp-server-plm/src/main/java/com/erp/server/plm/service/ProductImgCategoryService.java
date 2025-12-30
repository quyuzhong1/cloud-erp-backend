package com.erp.server.plm.service;
import com.erp.model.plm.entity.ProductImgCategoryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.plm.dto.ProductImgCategoryDTO;
import com.common.business.vo.PagingVO;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 图片分类表 服务类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-29
 */
public interface ProductImgCategoryService extends SuperService<ProductImgCategoryEntity> {

    /**
    * 新增
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(ProductImgCategoryDTO.AddDTO dto);

    /**
    * 修改
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return
    */
    Boolean update(ProductImgCategoryDTO.UpdateDTO dto);



    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-12-29
    * @param id
    * @return
    */
    ProductImgCategoryDTO.ViewDTO view(String id);


    /**
    * 删除
    * @author wuhaotian
    * @date: 2025-12-29
    * @param id
    * @return
    */
    Boolean delete(String id);

    /**
    * 列表查询（不分页，树结构）
    * @author wuhaotian
    * @date: 2025-12-29
    * @param paramDTO
    * @return List<ProductImgCategoryDTO.TreeDTO>
    */
    List<ProductImgCategoryDTO.TreeDTO> listTree(ProductImgCategoryDTO.ListTreeParamDTO paramDTO);
}

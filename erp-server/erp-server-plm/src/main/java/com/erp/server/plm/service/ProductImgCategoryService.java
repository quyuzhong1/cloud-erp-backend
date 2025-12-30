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
    * 分页列表查询
    * @author wuhaotian
    * @date: 2025-12-29
    * @param pagingParamDTO
    * @return PagingVO<ProductImgCategoryDTO.ListDTO>>
    */
    PagingVO<ProductImgCategoryDTO.ListDTO> paging(PagingDTO<ProductImgCategoryDTO.PagingParamDTO> pagingParamDTO);

    /**
    * 状态统计
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @return List<ProductImgCategoryDTO.TabListDTO>>
    */
    List<ProductImgCategoryDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
    * 详情
    * @author wuhaotian
    * @date: 2025-12-29
    * @param id
    * @return
    */
    ProductImgCategoryDTO.ViewDTO view(String id);


    /**
    * 导出Excel
    * @author wuhaotian
    * @date: 2025-12-29
    * @param dto
    * @param response
    * @return
    */
    void exportList(ProductImgCategoryDTO.ExportDTO dto, HttpServletResponse response);
}

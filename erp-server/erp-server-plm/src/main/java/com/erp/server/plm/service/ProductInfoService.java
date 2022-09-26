package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductInfoEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 产品信息表 服务类
 * </p>
 *
 * @author yl
 * @since 2022-09-13
 */
public interface ProductInfoService extends IService<ProductInfoEntity> {

    int countByCategoryId(String id);

    Boolean saveOrUpdateProduct(ProductDTO dto);

    Boolean updateCategory(MoveCategoryDTO dto);

    Boolean removeProduct(RemoveProductDTO dto);

    void exportTemplate(HttpServletRequest request, HttpServletResponse response);

    PagingVO paging(PagingDTO<ProductSearchDTO> dto);


    Boolean saveTemplate(SaveProductTemplateDTO dto);

    void updateProjectStatus(String productId, Integer state);
}

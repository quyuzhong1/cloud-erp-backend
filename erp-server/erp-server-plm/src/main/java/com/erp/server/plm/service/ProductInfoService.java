package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.ProductInfoEntity;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

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

    List<Map<String, Object>> getListObjs();

    ProductDTO info(String id);
    /**
     * @Description 无规格sku修改产品信息
     * @Author Luo_WG
     * @Date 2022/9/21 18:44
     * @param dto:产品基础信息请求参数
     * @return java.lang.Boolean
     **/
    Boolean updateSpec(ProductInfoDTO dto);

    void updateProduct(UpdateProductDTO dto);
}

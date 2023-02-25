package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
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

    String saveOrUpdateProduct(ProductDTO dto);

    Boolean updateCategory(MoveCategoryDTO dto);

    Boolean removeProduct(RemoveProductDTO dto);

    void exportTemplate(HttpServletRequest request, HttpServletResponse response);

    PagingVO<ProductShowDTO> paging(PagingDTO<ProductSearchDTO> dto);

    /**
     * @description: 查询产品列表数据（无分页）
     * @author Will
     * @date: 2023/2/10 14:35
     * @param dto
     * @return List<ProductShowDTO>
     */
    List<BasicDTO> listProductInfo(ProductSearchDTO dto);

    Boolean saveTemplate(SaveProductTemplateDTO dto);

    void updateProjectStatus(String productId, Integer state);

    List<Map<String, Object>> getListObjs();

    ProductDTO info(String id);
    /**
     * @Description 无规格sku修改产品信息
     * @Author Luo_WG
     * @Date 2022/9/21 18:44
     * @param dto:产品基础信息请求参数
     * @return java.lang.String
     **/
    String updateSpec(ProductInfoDTO dto);

    void updateProduct(UpdateProductDTO dto);

    void exportProductData(ExportProductDataDTO dto);


    List<CountDTO> getProductRelevanceList();


    List<ProductProjectDTO> getProductAndProjectList();


    ProductShowDTO getProductInfo(String productId);

    List<ProductShowDTO> getProductInfoByIds(List<String> productIds);

    void checkProduct(String productId);
    /**
     * @description: 根据spu参数查询
     * @author Will
     * @date: 2022/12/26 11:59
     * @param params
     * @return ProductInfoDTO
     */
    ProductInfoDTO getSpuByParam(Map<String, String> params);

    /**
     * 更改信息 基于sku 变更的
     * @author yl
     * @date 2023-02-07 19:58
     * @param productInfoDTO
     * @return void
     */
    void updateSpecByChangeSku(ProductInfoDTO productInfoDTO);
    /**
     * @description: 根据spu编码查询产品
     * @author Will
     * @date: 2023/2/8 14:49
     * @param spuNo
     * @return ProductInfoEntity
     */
    ProductInfoEntity getBySpuNo(String spuNo);

    /**
     * @description: 根据名称查询产品
     * @author Will
     * @date: 2023/2/22 19:27
     * @param name
     * @return ProductInfoEntity
     */
    ProductInfoEntity getByName(String name);

    Boolean setProgressStatus(SetProductProgressStatusDTO dto);

    Boolean setSchematicImageUrl(SetSchematicImageUrlDTO dto);
}

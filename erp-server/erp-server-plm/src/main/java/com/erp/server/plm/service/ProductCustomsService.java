package com.erp.server.plm.service;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.ProductCustomsDTO;
import com.erp.model.plm.dto.ProductCustomsSkuDTO;
import com.erp.model.plm.entity.ProductCustomsEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;


/**
 * <p>
 *  目的国海关编码服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-06-12
 */
public interface ProductCustomsService extends SuperService<ProductCustomsEntity> {


    /**
     * 根据产品id查询目的国海关编码
     * @Author Luo_WG
     * @Date 2023/6/15 16:51
     * @param productId
     * @return java.util.List<com.erp.model.plm.entity.ProductCustomsEntity>
     **/
    List<ProductCustomsEntity> listByProductId(String productId);

    /**
     * 根据产品id查询目的国海关编码
     * @Author Luo_WG
     * @Date 2023/6/15 16:51
     * @param skuId
     * @return java.util.List<com.erp.model.plm.entity.ProductCustomsEntity>
     **/
    List<ProductCustomsEntity> listBySkuId(String skuId);


    Boolean removeBySkuId(List<String> skuIds);


    /**
     * 临时接口-添加产品国外海关编码
     * @Author Luo_WG
     * @Date 2023/6/25 10:07
     * @return java.lang.Boolean
     **/
    Boolean addProductCustoms();

    /**
     * 获取sku定义的目的国申报海关编码
     * @param dto
     * @return
     */
    List<ProductCustomsEntity> listProductCustomsBySkuIds(ProductCustomsSkuDTO dto);

    /**
     * 新增sku默认海关编码记录
     * @param skuIds
     */
    void addDefaultCustoms(List<String> skuIds);

    /**
     * 根据skuIds获取目的国申报信息列表
     * @param skuIds
     * @return
     */
    List<ProductCustomsEntity> listBySkuIds(List<String> skuIds, String country);

    ProductCustomsEntity getBySkuIdAndCountry(String skuId, String country);

    /**
     * 分页查询
     *
     * @return
     */
    PagingVO<ProductCustomsDTO.ListDTO> paging(PagingDTO<ProductCustomsDTO.PagingParamDTO> dto);
    /**
     * 新增
     *
     * @param dto
     * @return
     */
    Boolean add(ProductCustomsDTO.AddListDTO dto);
    /**
     * 编辑
     *
     * @param dto
     * @return
     */
    Boolean update(ProductCustomsDTO.UpdateDTO dto);
    /**
     * 详情
     * @author jack
     * @date:  2025-06-21
     * @return ApiResult<SupplierCredentialDTO.ViewDTO>>
     */
    ProductCustomsDTO.ViewDTO view(String id);
    /**
     * 导出Excel数据
     * @author jack
     * @date:  2025-06-21
     * @param dto
     * @param response
     * @return
     */
    void exportList(ProductCustomsDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
     * 导入
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * 下载模板
     *
     * @return
     */
    void downloadTemplate(HttpServletResponse response);
}

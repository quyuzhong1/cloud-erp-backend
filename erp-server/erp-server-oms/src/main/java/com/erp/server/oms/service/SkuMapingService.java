package com.erp.server.oms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.SkuMapingDTO;
import com.erp.model.oms.entity.SkuMapingEntity;
import com.common.business.service.SuperService;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * sku 对照表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
public interface SkuMapingService extends SuperService<SkuMapingEntity> {

    void downloadTemplate(HttpServletResponse response);

    /**
     * 导入sku对照信息
     * @author yl
     * @date 2023-06-29 11:01
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 分页查询
     * @author yl
     * @date 2023-06-29 18:07
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.PagingViewDTO>
     */
    PagingVO<SkuMapingDTO.PagingViewDTO> paging(PagingDTO<SkuMapingDTO.PagingParamDTO> dto);

    /**
     * 导出sku 对照表
     * @author yl
     * @date 2023-06-30 9:35
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportSkuMaping(SkuMapingDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 获取tab 列表
     * @author yl
     * @date 2023-06-30 9:45
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SkuMapingDTO.TabListDTO>
     */
    List<SkuMapingDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 更改sku 对照表
     * @author yl
     * @date 2023-06-30 10:21
     * @param dto
     * @return java.lang.String
     */
    String updateSkuMaping(SkuMapingDTO.UpdateDTO dto);

    /**
     * 销售订单添加客户sku
     * @author yl
     * @date 2023-07-01 9:19
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.ProductSkuInfoDTO>
     */
    PagingVO<SkuMapingDTO.ProductSkuInfoDTO> listPaging(PagingDTO<SkuMapingDTO.ListParamDTO> dto);
}

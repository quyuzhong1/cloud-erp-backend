package com.erp.server.plm.service;/**
 * @author Lambda
 * @Classname LogisticsProductService
 * @Description TODO
 * @Date 2023-11-06 12:27
 * @Created by yl
 */

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description TODO
 * @Author yl
 * @Date 2023-11-06 12:27
 */
public interface LogisticsProductService  extends SuperService<ProductDetailEntity> {

    /**
     * 分页列表
     * @param dto
     * @return
     */
    PagingVO<LogisticsProductDTO.PagingVO> paging(PagingDTO<LogisticsProductDTO.PagingParamDTO> dto);

    /**
     * 物流产品详情
     * @param id
     * @return
     */
    LogisticsProductDTO.ViewDTO view(String id);

    /**
     * 编辑信息
     * @author yl
     * @date 2023-11-08 8:37
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean update(LogisticsProductDTO.UpdateDTO dto);

    /**
     * 导出物流产品信息
     * @author yl
     * @date 2023-11-08 10:54
     * @param dto
     * @param response
     * @return java.lang.Boolean
     */
    Boolean exportExcel(LogisticsProductDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 导入产品信息
     * @author yl
     * @date 2023-11-08 11:57
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);
}

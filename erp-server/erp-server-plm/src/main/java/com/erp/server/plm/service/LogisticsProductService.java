package com.erp.server.plm.service;/**
 * @author Lambda
 * @Classname LogisticsProductService
 * @Description TODO
 * @Date 2023-11-06 12:27
 * @Created by yl
 */

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.LogisticsProductDTO;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.entity.ProductLogisticsEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

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
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-11-08 10:54
     */
    Boolean exportExcel(LogisticsProductDTO.ExportDTO dto);

    /**
     * 导入产品信息
     * @author yl
     * @date 2023-11-08 11:57
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     */
    Boolean importExcel(MultipartFile excelFile, HttpServletResponse response);

    /**
     * 获取tab 页
     * @author yl
     * @date 2023-11-10 14:17
     * @param dto
     * @return java.util.List<com.erp.model.plm.dto.LogisticsProductDTO.TabListDTO>
     */
    List<LogisticsProductDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 更新分页
     * @author yl
     * @date 2023-11-10 14:45
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.plm.dto.LogisticsProductDTO.UpdatePagingDTO>
     */
    PagingVO<LogisticsProductDTO.UpdatePagingDTO> updatePaging(PagingDTO<LogisticsProductDTO.UpdatePagingParamDTO> dto);

    /**
     * 根据sku id list 获取到产品信息
     *@parms skuIdList
     *@return 
     *@author yl
     *@date 2023-11-27
     */
    List<LogisticsProductDTO.ProductDTO> listLogisticsProduct(List<String> skuIdList,List<String> skuNoList);
    /**
     * @description: 提审
     * @author Will
     * @date: 2024/3/18 16:45
     * @param id 
     * @param aTrue 
     * @return BatchResultDTO 
     */
    BatchResultDTO submit(String id, Boolean aTrue);
    /**
     * @description: 撤销流程
     * @author Will
     * @date: 2024/3/18 18:38
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO cancelProcess(String id);
    /**
     * @description: 审核
     * @author Will
     * @date: 2024/3/18 18:38
     * @param approveOneDTO
     * @return BatchResultDTO
     */
    BatchResultDTO approve(ApproveOneDTO approveOneDTO);
    /**
     * @description: 反审核
     * @author Will
     * @date: 2024/3/18 18:38
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO disApprove(String id);
    /**
     * @description: 结束审核
     * @author Will
     * @date: 2024/3/18 18:54
     * @param dto
     * @param entity
     * @return Boolean
     */
    Boolean approveEnd(ApproveOneDTO dto, ProductLogisticsEntity entity);
    /**
     * @description: 推送备案
     * @author Will
     * @date: 2024/3/19 14:22
     * @param dto
     */
    List<BatchResultDTO>  pushRegistration(LogisticsProductDTO.PushRegistrationDTO dto);

    PagingVO<LogisticsProductDTO.ExportInfoDTO> exportLogisticsProduct(PagingDTO<LogisticsProductDTO.ExportDTO> dto);
}

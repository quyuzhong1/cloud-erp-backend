package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchasePriceDTO;
import com.erp.model.scm.entity.PurchasePriceEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购价目表 服务类
 * </p>
 *
 * @author admin
 * @since 2023-03-15
 */
public interface PurchasePriceService extends SuperService<PurchasePriceEntity> {

    
    /**
     * 添加采购价目表
     * @author yl
     * @date 2023-03-24 12:22
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceEntity
     */
    PurchasePriceEntity add(PurchasePriceDTO.AddDTO dto);

    
    /**
     * 获取采购价目详情
     * @author yl
     * @date 2023-03-27 9:11
     * @param id
     * @return com.erp.model.scm.dto.PurchasePriceDTO.ViewDTO
     */
    PurchasePriceDTO.ViewDTO view(String id);

    /**
     * 修改采购价目
     * @author yl
     * @date 2023-03-27 10:52
     * @param dto
     * @return com.erp.model.scm.entity.PurchasePriceEntity
     */
    PurchasePriceEntity updatePurchasePrice(PurchasePriceDTO.ViewDTO dto);
    
    /**
     * 保存并提交审核 价目
     * @author yl
     * @date 2023-03-27 11:46
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean addAndSubmit(PurchasePriceDTO.AddDTO dto);

    /**
     * 修改并审核采购价目
     * @author yl
     * @date 2023-03-27 11:58
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateAndSubmit(PurchasePriceDTO.ViewDTO dto);

    
    
    /**
     * 批量删除采购价目信息
     * @author yl
     * @date 2023-03-27 12:04
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean deleteByIds(List<String> ids);

    /**
     * 采购价目表 提交审核
     * @author yl
     * @date 2023-03-27 12:11
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean submitApprove(List<String> ids);

    /**
     * 审核
     * @author yl
     * @date 2023-03-27 12:29
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean approve(BaseApproveParamDTO dto);

    /**
     * 取消流程
     * @author yl
     * @date 2023-03-27 14:04
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 采购信息分页
     * @author yl
     * @date 2023-03-27 14:40
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.scm.dto.PurchasePriceDTO.PagingViewDTO>
     */
    PagingVO<PurchasePriceDTO.PagingViewDTO> paging(PagingDTO<PurchasePriceDTO.PagingParamDTO> dto);

    
    /**
     * 采购价目表导出
     * @author yl
     * @date 2023-03-27 17:55
     * @param dto
     * @param response
     * @return void
     */
    void exportPurchasePrice(PurchasePriceDTO.PagingParamDTO dto, HttpServletResponse response);
}

package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购订单表 服务类
 * </p>
 *
 * @author will
 * @since 2023-03-16
 */
public interface PurchaseOrderService extends SuperService<PurchaseOrderEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/16 11:21
     * @param dto
     * @return PagingVO<PurchaseOrderDTO.listDTO>
     */
    PagingVO<PurchaseOrderDTO.ListDTO> paging(PagingDTO<PurchaseOrderDTO.SearchParamDTO> dto);
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/16 11:22
     * @param dto
     * @return Boolean
     */
    Boolean add(PurchaseOrderDTO.AddDTO dto);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/16 11:23
     * @param dto
     * @return Boolean
     */
    Boolean update(PurchaseOrderDTO.UpdateDTO dto);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/3/16 11:23
     * @param id
     * @return PurchaseOrderDTO.viewDTO
     */
    PurchaseOrderDTO.ViewDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/16 11:23
     * @param ids
     * @return Boolean
     */
    Boolean delete(List<String> ids);
    /**
     * @description: 批量审核
     * @author Will
     * @date: 2023/3/16 11:26
     * @param baseApproveParamDTO
     */
    void approve(BaseApproveParamDTO baseApproveParamDTO);
    /**
     * @description: 批量反审核
     * @author Will
     * @date: 2023/3/16 11:26
     * @param ids
     * @return Boolean
     */
    Boolean disApprove(List<String> ids);
    /**
     * @description: 取消流程
     * @author Will
     * @date: 2023/3/16 11:27
     * @param id
     * @return Boolean
     */
    Boolean cancelProcess(String id);
    /**
     * @description: 结束交货
     * @author Will
     * @date: 2023/3/16 11:35
     * @param id
     * @return Boolean
     */
    Boolean finishDelivery(String id);

    /**
     * @description: 采购变更
     * @author Will
     * @date: 2023/3/16 11:37
     * @param id
     * @return Boolean
     */
    Boolean purchaseChange(String id);
    /**
     * @description: 导出采购合同PDF
     * @author Will
     * @date: 2023/3/16 11:42
     * @param id
     * @return Boolean
     */
    Boolean exportPurchaseContractPdf(String id);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/3/16 11:58
     * @param excelFile
     * @param response
     * @return Boolean
     */
    Boolean importFile(MultipartFile excelFile, HttpServletResponse response);
    /**
     * @description: 导出
     * @author Will
     * @date: 2023/3/16 11:58
     * @param dto
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(PurchaseOrderDTO.SearchParamDTO dto, HttpServletResponse response);
    /**
     * @description: 提交
     * @author Will
     * @date: 2023/3/16 16:11
     * @param ids
     * @return Boolean
     */
    Boolean submit(List<String> ids);
    /**
     * @description: 新增并提交
     * @author Will
     * @date: 2023/3/17 12:59
     * @param dto
     * @return Boolean
     */
    Boolean addAndSubmit(PurchaseOrderDTO.AddDTO dto);
}

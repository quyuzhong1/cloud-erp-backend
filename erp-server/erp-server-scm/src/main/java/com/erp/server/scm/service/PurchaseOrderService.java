package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.ListStatusCountDTO;
import com.erp.model.scm.dto.PurchaseChangeDTO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderDetailDTO;
import com.erp.model.scm.entity.PurchaseOrderEntity;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
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
    String add(PurchaseOrderDTO.AddDTO dto);
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
    Boolean approve(BaseApproveParamDTO baseApproveParamDTO);
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
     * @param ids
     * @return Boolean
     */
    Boolean cancelProcess(List<String> ids);
    /**
     * @description: 结束交货
     * @author Will
     * @date: 2023/3/16 11:35
     * @param ids
     * @return Boolean
     */
    Boolean finishDelivery(List<String> ids);

    /**
     * @description: 导出采购合同PDF
     * @author Will
     * @date: 2023/3/16 11:42
     * @param id
     * @return PurchaseOrderDTO.ExportPdfDTO
     */
    PurchaseOrderDTO.ExportPdfDTO exportPurchaseContractPdf(String id);
    /**
     * @description: 导入
     * @author Will
     * @date: 2023/3/16 11:58
     * @param excelFile
     * @param response
     * @return PurchaseOrderDetailDTO.ImportDTO
     */
    PurchaseOrderDetailDTO.ImportDTO importFile(MultipartFile excelFile, List<String> skuIds,String supplierId, HttpServletResponse response);
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
    /**
     * @description: 
     * @author Will
     * @date: 2023/3/27 15:51
     * @param dto
     * @return Boolean 
     */
    Boolean updateAndSubmit(PurchaseOrderDTO.UpdateDTO dto);
    /**
     * @description: 查询列表数量
     * @author Will
     * @date: 2023/3/29 9:59
     * @return List<PurchaseOrderCountDTO>
     */
    List<ListStatusCountDTO.PurchaseOrderCountDTO> listCount(PermissionsDTO dto);
    /**
     * @description: 批量作废
     * @author Will
     * @date: 2023/3/29 10:28
     * @param ids
     * @param remark
     * @return Boolean
     */
    Boolean invalid(List<String> ids, String remark);
    /**
     * @description: 下推签收单弹框数据显示
     * @author Will
     * @date: 2023/3/29 15:55
     * @param ids
     * @return List<ViewGenerateReceiveDTO>
     */
    List<PurchaseOrderDTO.ViewGenerateReceiveDTO> viewGenerateReceive(List<String> ids);
    /**
     * @description: 下推签收单保存
     * @author Will
     * @date: 2023/3/29 16:46
     * @param dto
     * @return Boolean
     */
    Boolean generateReceive(PurchaseOrderDTO.ListGenerateReceiveDTO dto);
    /**
     * @description: 采购变更数据显示
     * @author Will
     * @date: 2023/3/31 14:29
     * @param id
     * @return ViewDTO
     */
    PurchaseChangeDTO.ViewDTO viewPurchaseChange(String id);
    /**
     * @description: 下推采购入库单显示
     * @author Will
     * @date: 2023/4/13 11:38
     * @param ids
     * @return List<ViewGenerateStockInDTO>
     */
    List<PurchaseOrderDTO.ViewGenerateStockInDTO> viewGenerateStockIn(List<String> ids);
    /**
     * @description: 根据id查询采购订单
     * @author Will
     * @date: 2023/4/17 9:20
     * @param id
     * @return UpdateDTO
     */
    PurchaseOrderDTO.GetOneDTO getPurchaseOrder(String id);

    /**
     * 根据采购订单id 获取对应产品信息
     * @author yl
     * @date 2023-04-17 18:27
     * @param purchaseOrderId
     * @return com.erp.model.scm.dto.PurchaseOrderDTO.GetQcProductDTO
     */
    PurchaseOrderDTO.GetQcProductDTO getQcProductInfo(String purchaseOrderId);
    /**
     * @description: 更新金蝶发送状态
     * @author Will
     * @date: 2023/4/21 10:04
     * @param ids
     * @param code
     * @param syncKingdeeId
     * @return Boolean
     */
    Boolean updateSyncKingdeeStatus(List<String> ids, String code, String syncKingdeeId);

    /**
     * 根据采购订单id 集合获取对应数量
     * @author yl
     * @date 2023-04-23 14:03
     * @param purchaseOrderIds
     * @return java.util.List<com.erp.model.scm.dto.PurchaseOrderDTO.GetOneDTO>
     */
    List<PurchaseOrderDTO.PurchaseOrderInfoDTO> getPurchaseOrderByOrderIds(List<String> purchaseOrderIds);

    
    /**
     * 采购订单 下推 退货数据显示
     * @author yl
     * @date 2023-04-25 9:39
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids);


}

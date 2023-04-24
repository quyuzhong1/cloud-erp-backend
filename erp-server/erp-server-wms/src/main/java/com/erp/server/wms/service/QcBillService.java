package com.erp.server.wms.service;

import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.dto.PurchaseStockInDTO;
import com.erp.model.wms.dto.QcBillDTO;
import com.erp.model.wms.dto.QcInfoDTO;
import com.erp.model.wms.entity.QcBillEntity;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 质检单表 服务类
 * </p>
 *
 * @author lambda
 * @since 2023-04-14
 */
public interface QcBillService extends SuperService<QcBillEntity> {


    /**
     * 暂存质检单
     * @param dto
     * @return
     */
    Boolean add(QcBillDTO.SaveOrUpdateDTO dto);
    /**
     * 根据采购id查询
     */
    List<QcBillEntity> listByPoIds(List<String> poIds);

    /**
     * 质检单详情
     * @author yl
     * @date 2023-04-19 11:53
     * @param id
     * @return com.erp.model.wms.dto.QcBillDTO.ViewDTO
     */
    QcBillDTO.ViewDTO view(String id);

    /**
     * 质检单分页信息
     * @author yl
     * @date 2023-04-19 15:25
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.QcBillDTO.PagingViewDTO>
     */
    PagingVO<QcBillDTO.PagingViewDTO> paging(PagingDTO<QcBillDTO.PagingParamDTO> dto);

    /**
     * 导出
     * @author yl
     * @date 2023-04-19 17:39
     * @param dto
     * @param response
     * @return void
     */
    void exportQcBill(QcBillDTO.ExportDTO dto, HttpServletResponse response);

    /**
     * 完成质检
     * @author yl
     * @date 2023-04-20 10:26
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean finish(QcBillDTO.SaveOrUpdateDTO dto);

    /**
     * 暂存
     * @author yl
     * @date 2023-04-20 14:00
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean draft(QcBillDTO.SaveOrUpdateDTO dto);

    
    /**
     * 免检
     * @author yl
     * @date 2023-04-20 15:29
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean exemption(QcBillDTO.SaveOrUpdateDTO dto);




    /**
     * 批量完成质检单
     * @author yl
     * @date 2023-04-20 15:37
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean batchFinish(List<String> ids);

    /**
     * 批量完成免检
     * @author yl
     * @date 2023-04-20 16:50
     * @return java.lang.Boolean
     */
    Boolean batchExemption(List<String> ids);

    /**
     * 批量取消 质检单
     * @author yl
     * @date 2023-04-20 17:13
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean batchCancel(List<String> ids);

    /**
     * 删除质检单
     * @author yl
     * @date 2023-04-20 17:21
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean delete(List<String> ids);
    
    /**
     * 撤销
     * @author yl
     * @date 2023-04-20 17:30
     * @param ids
     * @return java.lang.Boolean
     */
    Boolean cancelProcess(List<String> ids);

    /**
     * 分配质检员
     * @author yl
     * @date 2023-04-20 17:58
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean assign(QcBillDTO.AssignDTO dto);

    /**
     * 批量更新处理措施
     * @author yl
     * @date 2023-04-20 19:08
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean updateHandleMode(QcInfoDTO.UpdateHandleModeDTO dto);

    /**
     * 获取tab 类型数量
     * @author yl
     * @date 2023-04-21 16:48
     * @param
     * @return java.util.List<com.erp.model.wms.dto.QcBillDTO.TabListDTO>
     */
    List<QcBillDTO.TabListDTO> tabList();

    
    /**
     * 下推 退货单 显示
     * @author yl
     * @date 2023-04-23 12:07
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO>
     */
    List<PurchaseReturnOrderDTO.ViewGeneratePurchaseReturnOrderDTO> viewGeneratePurchaseReturnOrder(List<String> ids);

    
    /**
     * 下推退货单
     * @author yl
     * @date 2023-04-24 9:35
     * @param dto
     * @return java.lang.Boolean
     */
    Boolean generatePurchaseReturnOrder(PurchaseStockInDTO.ListGeneratePurchaseReturnOrderDTO dto);
}

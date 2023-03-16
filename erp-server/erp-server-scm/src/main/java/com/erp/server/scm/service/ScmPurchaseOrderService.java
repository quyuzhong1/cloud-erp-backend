package com.erp.server.scm.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.serveice.SuperService;
import com.erp.model.scm.dto.*;
import com.erp.model.scm.entity.ScmPurchaseOrderEntity;
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
public interface ScmPurchaseOrderService extends SuperService<ScmPurchaseOrderEntity> {
    /**
     * @description: 分页查询
     * @author Will
     * @date: 2023/3/16 11:21
     * @param dto
     * @return PagingVO<List<ScmPurchaseOrderPagingViewDTO>>
     */
    PagingVO<List<ScmPurchaseOrderPagingViewDTO>> paging(PagingDTO<ScmPurchaseOrderPagingParamDTO> dto);
    /**
     * @description: 获取编号
     * @author Will
     * @date: 2023/3/16 11:22
     * @return String
     */
    String getCode();
    /**
     * @description: 新增
     * @author Will
     * @date: 2023/3/16 11:22
     * @param scmPurchaseOrderDTO
     * @return Boolean
     */
    Boolean add(ScmPurchaseOrderDTO scmPurchaseOrderDTO);
    /**
     * @description: 修改
     * @author Will
     * @date: 2023/3/16 11:23
     * @param scmPurchaseOrderDTO
     * @return Boolean
     */
    Boolean update(ScmPurchaseOrderDTO scmPurchaseOrderDTO);
    /**
     * @description: 查询详情
     * @author Will
     * @date: 2023/3/16 11:23
     * @param id
     * @return ScmSalesDemandDTO
     */
    ScmPurchaseOrderDTO view(String id);
    /**
     * @description: 删除
     * @author Will
     * @date: 2023/3/16 11:23
     * @param id
     * @return Boolean
     */
    Boolean delete(String id);
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
    Boolean unApprove(List<String> ids);
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
     * @description: 再次购买
     * @author Will
     * @date: 2023/3/16 11:35
     * @param id
     * @return Boolean
     */
    Boolean copy(String id);
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
     * @description: 仓库签收单弹框数据显示
     * @author Will
     * @date: 2023/3/16 11:46
     * @param id
     * @return List<ScmPurchaseOrderViewDTO>
     */
    List<ScmPurchaseOrderViewDTO> viewForWarehouseReceive(String id);
    /**
     * @description: 下推签收保存
     * @author Will
     * @date: 2023/3/16 11:52
     * @param scmPurchaseOrderViewDTO
     * @return Boolean
     */
    Boolean generateWarehouseReceive(ScmPurchaseOrderViewDTO scmPurchaseOrderViewDTO);
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
     * @param scmPurchaseOrderPagingParamDTO
     * @param response
     * @return Boolean
     */
    Boolean exportExcel(ScmPurchaseOrderPagingParamDTO scmPurchaseOrderPagingParamDTO, HttpServletResponse response);
}

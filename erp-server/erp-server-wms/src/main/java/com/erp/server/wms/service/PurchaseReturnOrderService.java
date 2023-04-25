package com.erp.server.wms.service;

import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.PurchaseReturnOrderDTO;
import com.erp.model.wms.entity.PurchaseReturnOrderEntity;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 * 采购退货单 服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-07
 */
public interface PurchaseReturnOrderService extends SuperService<PurchaseReturnOrderEntity> {
    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.PurchaseReturnOrderDTO.PagingViewDTO>
     **/
    PagingVO<PurchaseReturnOrderDTO.PagingViewDTO> paging(PagingDTO<PurchaseReturnOrderDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    String add(PurchaseReturnOrderDTO.AddDTO dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(PurchaseReturnOrderDTO.UpdateDTO dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.PurchaseReturnOrderDTO.ViewDTO
     **/
    PurchaseReturnOrderDTO.ViewDTO view(String id);

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/4/14 10:04
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean submit(List<String> ids);

    /**
     * 新增提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean addAndSubmit(PurchaseReturnOrderDTO.AddDTO dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(PurchaseReturnOrderDTO.UpdateDTO dto);

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    Boolean approve(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param baseApproveParamDTO baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    Boolean disApprove(BaseApproveParamDTO baseApproveParamDTO);

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/4/13 18:58
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean cancelProcess(List<String> ids);

    /**
     * 批量作废
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @param remark remark
     * @return java.lang.Boolean
     **/
    Boolean invalid(List<String> ids, String remark);

    /**
     * 批量删除
     * @Author Luo_WG
     * @Date 2023/4/6 19:29
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean delete(List<String> ids);

    /**
     * 导出
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dto dto
     * @param response response
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean exportExcel(@RequestBody PurchaseReturnOrderDTO.PagingParamDTO dto, HttpServletResponse response);


    /**
     * 采购订单-关联的退货订单
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     **/
    List<PurchaseReturnOrderDTO.OrderRefReceiveDTO> purchaseOrderRefReturn(String purchaseOrderId);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:14
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>>
     **/
    List<PurchaseReturnOrderDTO.ReturnOrderCountDTO> listCount(@RequestBody PermissionsDTO dto);
    /**
     * @description: 根据来源id查询
     * @author Will
     * @date: 2023/4/19 9:27
     * @param sourceIds
     * @return List<PurchaseReturnOrderEntity>
     */
    List<PurchaseReturnOrderEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 获取退货数量
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     **/
    List<PurchaseReturnOrderDTO.GetReturnQtyDTO> getReturnQty(String purchaseOrderId);

    /**
     * 修改金蝶同步状态
     * @Author Luo_WG
     * @Date 2023/4/24 15:29
     * @param id
     * @param syncKingdeeStatus
     * @param syncKingdeeId
     * @return java.lang.Boolean
     **/
    Boolean updateSyncKingdeeStatus(String id ,String syncKingdeeStatus,String syncKingdeeId);

    
    /**
     * 批量生成退货单
     * @author yl
     * @date 2023-04-25 11:09
     * @param list
     * @return java.lang.Boolean
     */
    Boolean batchAdd(List<PurchaseReturnOrderDTO.AddDTO> list);
}

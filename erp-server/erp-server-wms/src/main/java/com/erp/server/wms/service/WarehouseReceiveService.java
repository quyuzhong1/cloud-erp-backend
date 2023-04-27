package com.erp.server.wms.service;


import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.wms.dto.WarehouseReceiveDTO;
import com.erp.model.wms.entity.WarehouseReceiveEntity;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * <p>
 *  采购收货服务类
 * </p>
 *
 * @author LUO_WG
 * @since 2023-04-06
 */
public interface WarehouseReceiveService extends SuperService<WarehouseReceiveEntity> {
    /**
     * 分页查询
     * @Author Luo_WG
     * @Date 2023/4/13 15:41
     * @param pagingParamDTO pagingParamDTO
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.WarehouseReceiveDTO.PagingViewDTO>
     **/
    PagingVO<WarehouseReceiveDTO.PagingViewDTO> paging(PagingDTO<WarehouseReceiveDTO.PagingParamDTO> pagingParamDTO);

    /**
     * 列表状态数量统计
     * @Author Luo_WG
     * @Date 2023/4/17 13:13
     * @param dto dto
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.WarehouseReceiveCountDTO>
     **/
    List<WarehouseReceiveDTO.WarehouseReceiveCountDTO> listCount(PermissionsDTO dto);

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/4/13 11:03
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    String add(WarehouseReceiveDTO.AddDTO dto);

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/13 14:51
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean update(WarehouseReceiveDTO.UpdateDTO dto);

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/4/13 17:10
     * @param id id
     * @return com.erp.model.wms.dto.WarehouseReceiveDTO.ViewDTO
     **/
    WarehouseReceiveDTO.ViewDTO view(String id);

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
    Boolean addAndSubmit(WarehouseReceiveDTO.AddDTO dto);

    /**
     * 修改提交
     * @Author Luo_WG
     * @Date 2023/4/6 18:52
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean updateAndSubmit(WarehouseReceiveDTO.UpdateDTO dto);

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
     * @param ids ids
     * @return java.lang.Boolean
     **/
    Boolean disApprove(List<String> ids);

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
    Boolean exportExcel(@RequestBody WarehouseReceiveDTO.PagingParamDTO dto, HttpServletResponse response);

    /**
     * 下推入库单列表查询
     * @Author Luo_WG
     * @Date 2023/4/14 14:24
     * @param ids ids
     * @return java.util.List<com.erp.model.wms.dto.WarehouseReceiveDTO.GenerateStockInViewDTO>
     **/
    List<WarehouseReceiveDTO.GenerateStockInViewDTO> generateStockInView(List<String> ids);

    /**
     * 下推入库单
     * @Author Luo_WG
     * @Date 2023/4/13 18:59
     * @param dtos dtos
     * @return java.lang.Boolean
     **/
    Boolean generateStockIn(List<WarehouseReceiveDTO.GenerateStockInDTO> dtos);

    /**
     * 采购订单-关联的收货单据
     * @Author Luo_WG
     * @Date 2023/4/13 18:47
     * @param purchaseOrderId purchaseOrderId
     * @return java.lang.Integer
     **/
    List<WarehouseReceiveDTO.OrderRefReceiveDTO> purchaseOrderRefReceive(String purchaseOrderId);

    /**
     * 根据采购订单ids 获取收获数据
     * @author yl
     * @date 2023-04-27 18:31
     * @param purchaseOrderIds
     * @return java.util.List<com.erp.model.wms.entity.WarehouseReceiveEntity>
     */
    List<WarehouseReceiveEntity> listByPurchaseOrderIds(List<String> purchaseOrderIds);

    /**
     * 采购订单-下推收货单保存按钮
     * @Author Luo_WG
     * @Date 2023/4/24 13:54
     * @param dto dto
     * @return java.lang.Boolean
     **/
    Boolean generateReceive(PurchaseOrderDTO.ListGenerateReceiveDTO dto);
}

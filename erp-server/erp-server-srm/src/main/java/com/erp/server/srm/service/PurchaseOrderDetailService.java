package com.erp.server.srm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.dto.PurchaseOrderDTO;
import com.erp.model.scm.dto.PurchaseOrderSrmDTO;
import com.erp.model.srm.entity.PurchaseOrderDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.PurchaseOrderDetailDTO;

import java.util.List;

/**
 * <p>
 * 采购订单明细表（已确认） 服务类
 * </p>
 *
 * @author zdy
 * @since 2024-01-27
 */
public interface PurchaseOrderDetailService extends SuperService<PurchaseOrderDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2024-01-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(PurchaseOrderDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2024-01-27
    * @param dto
    * @return
    */
    Boolean update(PurchaseOrderDetailDTO.UpdateDTO dto);

    /**
     * 根据记录同步
     * @param id
     * @param detailIds
     * @param executionStatus
     */
    void syncScmPurchaseOrderDetail(String id, List<String> detailIds, String executionStatus);

    /**
     * 根据srm采购订单id删除记录
     * @param ids
     */
    void removeBySrmOrderIds(List<String> ids);

    /**
     * 待发货 分页列表
     * @param dto
     * @return
     */
    PagingVO<PurchaseOrderDTO.ListDTO> srmWaitDeliveryPaging(PagingDTO<PurchaseOrderDTO.SrmSearchParamDTO> dto);

    /**
     * 待发货订单合计
     * @param dto
     * @return
     */
    PurchaseOrderDTO.ListDTO srmWaitDeliveryTotal(PurchaseOrderDTO.SrmSearchParamDTO dto);

    /**
     * 待发货 时间周期统计
     * @param dto
     * @return
     */
    PurchaseOrderSrmDTO.WaitDeliveryCountDTO srmWaitDeliveryCount(PurchaseOrderSrmDTO.WaitDeliveryParamDTO dto);
}

package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;

import java.util.List;

/**
 * <p>
 * b2c发货单 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-12-13
 */
public interface SoB2cDeliveryService extends SuperService<SoB2cDeliveryEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-12-13
    * @param dto
    * @return
    */
    Boolean add(SoB2cDeliveryDTO.AddDTO dto);

    /**
     * 获取状态统计
     * @Author Luo_WG
     * @Date 2023/12/13 18:53
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.TabListDTO>
     **/
    List<SoB2cDeliveryDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/12/13 19:14
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoB2cDeliveryDTO.ListDTO>
     **/
    PagingVO<SoB2cDeliveryDTO.ListDTO> paging(PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto);

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2023/12/13 19:19
     * @param id
     * @return com.erp.model.wms.dto.SoB2cDeliveryDTO.ViewDTO
     **/
    SoB2cDeliveryDTO.ViewDTO view(String id);

    /**
     * 手动发货
     * @Author Luo_WG
     * @Date 2023/12/13 19:26
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO manualDelivery(String id);

    /**
     * 虚假发货
     * @Author Luo_WG
     * @Date 2023/12/13 19:30
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO falseDelivery(String id);

    /**
     * 打印拣货单预览
     * @Author Luo_WG
     * @Date 2023/12/13 19:37
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.printPickingViewDTO>
     **/
    List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingView(List<String> ids);

    /**
     * 打印拣货单
     * @Author Luo_WG
     * @Date 2023/12/19 16:12
     * @param ids
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean printPicking(List<String> ids);

    /**
     * 取消打印拣货单
     * @Author Luo_WG
     * @Date 2023/12/19 16:20
     * @param ids
     * @return java.lang.Boolean
     **/
    Boolean printPickingCancel(List<String> ids);

    /**
     * 打印物流面单
     * @Author Luo_WG
     * @Date 2023/12/13 20:13
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>
     **/
    List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillView(List<String> ids);

    SoB2cDeliveryEntity getByBusinessCode(String businessCode);

    /**
     * 打印物流面单确认
     * @Author Luo_WG
     * @Date 2023/12/19 16:39
     * @param dto
     * @return void
     **/
    String printLogisticsBillConfirm(SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto);

    /**  销售订单标记发货失败后从新标记发货
     * @description
     * @param soB2cId
     * @author Lambda
     * @return
     * @create 2023-12-25 14:47
     */
    List<BatchResultDTO> retryFalseDelivery(String soB2cId);

    /**
     * 根据来源id查询发货单
     * @Author Luo_WG
     * @Date 2023/12/25 16:32
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryEntity>
     **/
    List<SoB2cDeliveryEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 回滚冻结的库存
     * @Author Luo_WG
     * @Date 2023/12/26 12:22
     * @param ids
     * @return void
     **/
    void rollbackInventory(List<String> ids);

    /**
     * 修改发货状态
     * @Author Luo_WG
     * @Date 2023/12/26 12:36
     * @param ids
     * @param status
     * @return java.lang.Boolean
     **/
    Boolean updateStatus(List<String> ids, String status);

    /** 发货
     * @description
     * @param
     * @author Lambda
     * @return 
     * @create 2023-12-29 10:37
     */
    BatchResultDTO delivery(String id, String deliveryType);

    /**
     * 生成销售出库单
     * @description
     * @param entity
     * @author Lambda
     * @return
     * @create 2024-01-26 10:17
     */
    void generateB2cSoOutstock(SoB2cDeliveryEntity entity);
}

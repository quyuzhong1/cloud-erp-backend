package com.erp.server.wms.service;

import com.common.business.dto.PlatformShipOrderDTO;
import com.common.business.dto.PrintWayBillPdfDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.sys.openapi.DimensionalWeightDTO;
import com.erp.model.wms.dto.SoB2cDeliveryDTO;
import com.erp.model.wms.dto.SoB2cDeliveryInterceptDTO;
import com.erp.model.wms.dto.pickingstrategy.LocationInventoryResultDTO;
import com.erp.model.wms.entity.SoB2cDeliveryDetailEntity;
import com.erp.model.wms.entity.SoB2cDeliveryEntity;
import com.erp.model.wms.enums.AbnormalCauseEnum;
import com.erp.model.wms.enums.ShipmentMarkTypeEnum;

import javax.servlet.http.HttpServletResponse;
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
     *
     * @param dto
     * @return
     * @author Luo_WG
     * @date: 2023-12-13
     */
    Boolean add(SoB2cDeliveryDTO.AddDTO dto);

    /**
     * 获取状态统计
     *
     * @param dto
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.TabListDTO>
     * @Author Luo_WG
     * @Date 2023/12/13 18:53
     **/
    List<SoB2cDeliveryDTO.TabListDTO> tabList(PermissionsDTO dto);

    /**
     * 列表查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.wms.dto.SoB2cDeliveryDTO.ListDTO>
     * @Author Luo_WG
     * @Date 2023/12/13 19:14
     **/
    PagingVO<SoB2cDeliveryDTO.ListDTO> paging(PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto);

    /**
     * 详情
     *
     * @param id
     * @return com.erp.model.wms.dto.SoB2cDeliveryDTO.ViewDTO
     * @Author Luo_WG
     * @Date 2023/12/13 19:19
     **/
    SoB2cDeliveryDTO.ViewDTO view(String id);

    /**
     * 手动发货
     *
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     * @Author Luo_WG
     * @Date 2023/12/13 19:26
     **/
    BatchResultDTO manualDelivery(String id);

    /**
     * 手动标发
     * @Author Luo_WG
     * @Date 2023/12/13 19:30
     **/
    BatchResultDTO falseDelivery(String id);

    /**
     * 打印拣货单预览
     *
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.printPickingViewDTO>
     * @Author Luo_WG
     * @Date 2023/12/13 19:37
     **/
    List<SoB2cDeliveryDTO.PrintPickingViewDTO> printPickingView(List<String> ids);

    /**
     * 打印拣货单
     *
     * @param ids
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/12/19 16:12
     **/
    Boolean printPicking(List<String> ids);

    /**
     * 取消打印拣货单
     *
     * @param id
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/19 16:20
     **/
    BatchResultDTO printPickingCancel(String id);

    /**
     * 打印物流面单
     *
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.SoB2cDeliveryDTO.PrintLogisticsWaybillDTO>
     * @Author Luo_WG
     * @Date 2023/12/13 20:13
     **/
    List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillView(List<String> ids);

    SoB2cDeliveryEntity getByBusinessCode(String businessCode);

    /**
     * 打印物流面单确认
     *
     * @param dto
     * @return void
     * @Author Luo_WG
     * @Date 2023/12/19 16:39
     **/
    void printLogisticsBillConfirm(SoB2cDeliveryDTO.PrintLogisticsBillConfirmDTO dto, HttpServletResponse response);

    /**
     * 根据来源id查询发货单
     *
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.SoB2cDeliveryEntity>
     * @Author Luo_WG
     * @Date 2023/12/25 16:32
     **/
    List<SoB2cDeliveryEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 根据状态查询
     * @author will
     * @date 2024/8/13 15:50
     * @param sourceIds
     * @param notStatus
     * @return List<SoB2cDeliveryEntity>
     */
    List<SoB2cDeliveryEntity> listBySourceIds(List<String> sourceIds, String notStatus);
    /**
     * 回滚冻结的库存
     *
     * @param ids
     * @return void
     * @Author Luo_WG
     * @Date 2023/12/26 12:22
     **/
    void rollbackInventory(List<String> ids);

    /**
     * 回滚库存
     *
     **/
    void rollbackPickingInventory(List<String> ids);

    /**
     * 修改发货状态
     *
     * @param ids
     * @param status
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/26 12:36
     **/
    Boolean updateStatus(List<String> ids, String status);

    /**
     * 发货
     *
     * @param
     * @return
     * @description
     * @author Lambda
     * @create 2023-12-29 10:37
     */
    BatchResultDTO delivery(String id, String deliveryType);

    /**
     * 扣减冻结库存（库存不够生成异常）
     * @author will
     * @date 2024/7/21 9:47
     * @param entity
     */
    Boolean generateOutFreezeError (SoB2cDeliveryEntity entity);

    /**
     * 清除扣减冻结异常
     * @author will
     * @date 2024/8/8 22:08
     * @param entity
     */
    void cleanErrorSignFreeze (SoB2cDeliveryEntity entity);

    /**
     * 扣减冻结库存
     * @author will
     * @date 2024/7/21 9:47
     * @param entity
     */
    Boolean outFreezeVirtualInventory (SoB2cDeliveryEntity entity);

    /**
     * 生成销售出库单
     *
     * @param entity
     * @return
     * @description
     * @author Lambda
     * @create 2024-01-26 10:17
     */
    Boolean generateB2cSoOutstock(SoB2cDeliveryEntity entity);

    Boolean updateB2cDeliveryWeightBySoId(SoB2cDeliveryDTO.UpdateWeightDTO dto);

    /**
     * @param id
     * @return BatchResultDTO
     * @description: 完成打印
     * @author Will
     * @date: 2024/4/17 10:48
     */
    BatchResultDTO finishPrint(String id);

    /**
     * 导出列表
     *
     * @param dto
     * @return
     */
    Boolean exportExcel(SoB2cDeliveryDTO.PagingParamDTO dto);

    /**
     * 手动标发(批量)
     * @param ids
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2024/4/22 18:06
     **/
    Boolean falseDeliveryBatch(List<String> ids);

    /**
     * 打印面单预览
     *
     * @param param
     * @return
     */
    List<SoB2cDeliveryDTO.PrintLogisticsWaybillDTO> printLogisticsWaybillPreview(SoB2cDeliveryDTO.PrintLogisticsBillConfirmParam param);

    /**
     * 检查自发货订单的发货单状态
     * true=无已发货的发货单
     * false=有已发货的发货单
     */
    boolean hasNotGenB2cSoOutStockAndLog(SoB2cEntity currentEntity);

    List<BatchResultDTO> logisticsIntercept(List<String> ids);


    /**
     * 查询不是取消发货的发货单
     *
     * @param soId
     * @return
     */
    SoB2cDeliveryEntity getNotCancelBySoId(String soId);

    Boolean shipOrder(PlatformShipOrderDTO platformShipOrderDTO);

    List<BaseResultDTO.AddDTO> generationWaves(SoB2cDeliveryDTO.GenerationWavesDTO dto);

    BatchResultDTO clearException(String id);

    SoB2cDeliveryDTO.CancelShipmentView cancelShipmentView(List<String> ids);

    /**
     * 查询待处理发货单
     *
     * @return List<SoB2cDeliveryEntity>
     * @author will
     * @date 2024/6/25 18:07
     */
    List<SoB2cDeliveryEntity> listWaitHandle();

    /**
     * @param base64List         base64
     * @param printWayBillPdfDTO pdf
     */
    void customDistribute(List<String> base64List, PrintWayBillPdfDTO printWayBillPdfDTO);

    /**
     * 流水线称重回填
     *
     * @param dto
     * @return String
     * @author will
     * @date 2024/6/28 15:48
     */
    ApiResult<String> dimensionalWeightPipeline(DimensionalWeightDTO dto);

    Boolean updateAbnormal(List<String> ids, AbnormalCauseEnum abnormalCauseEnum);

    /**
     * 根据发货单大于物流面单
     *
     * @param id
     * @param response
     * @author will
     * @date 2024/7/1 18:14
     */
    void printLogisticsBillConfirmById(String id, HttpServletResponse response);

    /**
     * 查询id集合
     *
     * @param type 发货标记类型
     */
    List<String> listIdsByShipmentMark(ShipmentMarkTypeEnum type);

    /**
     * 修改发货单标发类型
     *
     * @param ids  发货单id
     * @param code 类型
     */
    void updateShipmentMark(List<String> ids, String code);

    /**
     * 更新发货单状态
     *
     * @param deliveryIdList
     * @author will
     * @date 2024/7/4 9:31
     */
    void updateDeliveryStatus(List<String> deliveryIdList, String status);

    /**
     * B2C生成拣货单
     *
     * @param soB2cDeliveryEntity         发货单
     * @param soB2cDeliveryDetailEntities 发货单明细
     */
    List<String> generatePickingDetail(SoB2cDeliveryEntity soB2cDeliveryEntity, List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntities);

    /**
     * B2C生成拣货单 (规则前置执行)
     *
     * @param soB2cDeliveryEntity         发货单
     * @param soB2cDeliveryDetailEntities 发货单明细
     * @param results 前置规则返回的仓位
     */
    List<String> generatePickingDetail(SoB2cDeliveryEntity soB2cDeliveryEntity, List<SoB2cDeliveryDetailEntity> soB2cDeliveryDetailEntities, List<LocationInventoryResultDTO> results);
    /**
     * 取消发货
     */
    BatchResultDTO cancelShipment(String id, List<SoB2cDeliveryDTO.CancelShipmentDTO> detail);
    /**
     * 发货生成直接调拨单
     * @author will
     * @date 2024/7/11 10:42
     * @param entity
     * @return Boolean
     */
    Boolean pushTransferInfo(SoB2cDeliveryEntity entity);

    /**
     * 发货生成直接调拨单
     * @author will
     * @date 2024/7/11 10:42
     * @param entity
     * @return Boolean
     */
    Boolean pushTransferInfoError(SoB2cDeliveryEntity entity);

    /**
     * 重试生成直接调拨单
     * @author will
     * @date 2024/8/1 10:00
     * @param soId
     * @return Boolean
     */
    Boolean afreshPushTransferInfo(String soId);
    /**
     * 重新出库
     * @author will
     * @date 2024/7/12 15:47
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO retryOutstock(String id);
    /**
     * 库存数据修复
     * @author will
     * @date 2024/7/31 19:39
     * @param id
     * @return BatchResultDTO
     */
    BatchResultDTO handleErrorData(String id,Boolean isAddQty);
    /**
     * 重试发货虚拟仓库存扣减
     * @author will
     * @date 2024/8/1 10:37
     * @param soId
     * @return Boolean
     */
    Boolean afreshOutFreezeVirtualInventory(String soId);

    /**
     * 导出发货单
     * @param dto
     * @return
     */
    PagingVO<SoB2cDeliveryDTO.ListDTO> exportB2cDelivery(PagingDTO<SoB2cDeliveryDTO.PagingParamDTO> dto);

    /**
     * 回滚虚拟库存
     * @param deliveryEntityList
     */
    void addUsableVirtualInventory (List<SoB2cDeliveryEntity> deliveryEntityList);

    /**
     * 根据销售订单手动标发
     * @param id
     * @return
     */
    BatchResultDTO falseDeliveryBySoId(String id);
}

package com.erp.server.wms.service;

import cn.hutool.core.lang.Tuple;
import com.common.business.dto.PlatformFbaShipmentReceiveDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.ListingInfoWithSkuMappingDTO;
import com.erp.model.oms.entity.ShopInfoEntity;
import com.erp.model.wms.dto.FbaShipmentDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentReceiveEntity;
import com.erp.model.wms.entity.FirstMileDeliveryEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * FBI货件表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-10-30
 */
public interface FbaShipmentService extends SuperService<FbaShipmentEntity> {
    /**
     * 列表查询
     * @param dto
     * @return ApiResult<PagingVO<FbaDeliveryDTO.ListDTO>>
     */
    PagingVO<FbaShipmentDTO.ListDTO> paging(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto);

    /**
     * 拉取货件信息
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     */
    Boolean pullShipment(FbaShipmentDTO.PullShipmentDTO dto);

    /**
     * 查询发货记录
     * @Author Luo_WG
     * @Date 2023/10/30 17:38
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.DeliverRecordDTO>>
     **/
    List<FirstMileDeliveryDTO.DeliverRecordView> listDeliverRecord(String id);

    /**
     * 查询货件状态记录
     * @Author Luo_WG
     * @Date 2023/10/30 17:40
     * @param code
     * @return com.common.core.controller.vo.ApiResult<java.util.List<FbaShipmentDTO.ShipmentStatusRecordDTO>>
     **/
    List<FbaShipmentDTO.ShipmentStatusRecordView> listShipmentStatusRecord(String code);

    /**
     * 查询收货记录
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.ReceiveRecordView>>
     **/
    List<FbaShipmentDTO.ReceiveRecordView> listReceiveRecord(String id);

    /**
     * 查询详情
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.ReceiveRecordView>>
     **/
    FbaShipmentDTO.ViewDTO view(String id);

    /**
     * 完结货件
     * @param ids
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean finishShipment(List<String> ids);

    /**
     * 下推发货单列表查询
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateDeliverView>>
     **/
    List<FbaShipmentDTO.GenerateDeliverView> generateDeliverView(BaseIdsDTO.IdsDTO ids);

    /**
     * 单个下推发货单获取详情
     * @Author Luo_WG
     * @Date 2023/11/8 9:19
     * @param id
     * @return com.erp.model.wms.dto.FbaDeliveryDTO.ViewDTO
     **/
    FirstMileDeliveryDTO.ViewDTO getDeliverView(String id);

    /**
     * 下推发货单保存
     * @Author Luo_WG
     * @Date 2023/10/31 14:35
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateDeliverSave(List<FbaShipmentDTO.GenerateDeliverView> list);

    /**
     * 下推发货单保存并提交
     * @Author Luo_WG
     * @Date 2023/11/6 14:35
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateDeliverSaveAndSubmit(List<FbaShipmentDTO.GenerateDeliverView> list);

    /**
     * sku映射
     * @Author Luo_WG
     * @Date 2023/11/2 11:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean skuMapping(FbaShipmentDTO.SkuMappingParamDTO dto);

    /**
     * 批量更新sku映射
     * @Author Luo_WG
     * @Date 2023/11/6 11:29
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO skuMappingBatch(String id);

    /**
     * FBA货件相关保存
     *
     * @author Jim
     * @date 2023/11/10
     **/
    void checkAndSaveAll(FbaShipmentEntity entity, Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap, List<String> hasChildrenSkuIds, List<PlatformFbaShipmentReceiveDTO> receiveDTOList, List<PlatformFbaShipmentReceiveDTO> detailList);

    /**
     * FBA货件相关更新
     *
     * @author Jim
     * @date 2023/11/10
     **/
    void checkAndUpdateAll(FbaShipmentEntity oldEntity, FbaShipmentEntity entity, Map<String, ListingInfoWithSkuMappingDTO> listingInfoMap, List<String> hasChildrenSkuIds, List<PlatformFbaShipmentReceiveDTO> receiveDTOList, List<PlatformFbaShipmentReceiveDTO> detailList);

    void handlerWarehouse(FbaShipmentEntity entity, List<FbaShipmentReceiveEntity> saveReceiveList, LocalDate billDate, Map<String, LocalDate> closedDateMap);

    /**
     * 通过fbaShipmentId查询实体
     * @author  Jim
     * @date 2023/11/2
     */
    FbaShipmentEntity getByFbaShipmentId(String fbaShipmentId);

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2023/11/6 10:37
     * @param id
     * @return com.common.business.dto.base.BatchResultDTO
     **/
    BatchResultDTO delete(String id);

    /**
     * 下推要货申请列表查询
     * @Author Luo_WG
     * @Date 2023/11/17 11:19
     * @param ids
     * @return java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateRequisitionApplicationViewDTO>
     **/
    List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> generateRequisitionApplicationView(List<String> ids);

    /**
     * FBA货件下推要货申请保存
     * @Author Luo_WG
     * @Date 2023/11/17 11:22
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateRequisitionApplicationSave(List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> list);

    /**
     * FBA货件下推要货申请保存并提交
     * @Author Luo_WG
     * @Date 2023/11/17 11:22
     * @param list
     * @return java.lang.Boolean
     **/
    Boolean generateRequisitionApplicationSaveAndSubmit(List<FbaShipmentDTO.GenerateRequisitionApplicationViewDTO> list);

    /**
     * 发货更新状态和发货数量
     * @Author Luo_WG
     * @Date 2023/11/17 11:22
     * @param deliveryEntity
     * @return java.lang.Boolean
     **/
    Boolean deliveryStatus(FirstMileDeliveryEntity deliveryEntity);

    /**
     * 发货单反审核修改发货数量和状态
     * @Author Luo_WG
     * @Date 2023/11/17 11:22
     * @param deliveryEntity
     * @return java.lang.Boolean
     **/
    Boolean deliveryDisApprove(FirstMileDeliveryEntity deliveryEntity);

    /**
     * 生成直接调拨单
     * @Author Jim
     * @Date 2023/12/04
     */
    Tuple generateTransferOut(ShopInfoEntity shopEntity, FbaShipmentEntity shipmentEntity, List<FbaShipmentReceiveEntity> newReceiveEntityList, Boolean isToOnwayWarehouse, String remark, LocalDate billDate, String transferDirection);

    /**
     * 重新直接调拨单
     * @Author Jim
     * @Date 2024/01/15
     */
    BatchResultDTO regenerateTransferOut(String id);


    void generateTransfer(ShopInfoEntity shopInfoEntity, FbaShipmentEntity entity, List<FbaShipmentReceiveEntity> receiveList, Boolean isToOnwayWarehouse, String remark, LocalDate billDate, String transferDirection, Map<String, LocalDate> closedDateMap);

    void export(FbaShipmentDTO.PagingParamDTO dto);

    /**
     * 检查当前Fba货件是停止生成签收记录得日期
     *
     */
    LocalDate getStopGenReceivedDate(FbaShipmentEntity fbaShipmentEntity);

    PagingVO<FbaShipmentDTO.SearchResultDTO> search(PagingDTO<FbaShipmentDTO.SearchDTO> dto);

    FbaShipmentEntity getByCode(String fbaShipmentCode);

    List<FbaShipmentEntity> listByCodes(List<String> fbaShipmentCodeList);

    /**
     * 导出
     */
    PagingVO<FbaShipmentDTO.ExportDTO> exportFbaShipment(PagingDTO<FbaShipmentDTO.PagingParamDTO> dto);

    boolean updatePackingStatus(String id);

    PagingVO<FbaShipmentDTO.SearchResultDTO> searchByCodeWithRequisition(PagingDTO<FbaShipmentDTO.SearchDTO> dto);

    List<String> requisitionFbaQuickPaste(FbaShipmentDTO.QuickPasteDTO dto);
}

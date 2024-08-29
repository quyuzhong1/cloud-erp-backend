package com.erp.server.wms.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.common.business.dto.PlatformInboundDTO;
import com.common.business.dto.base.*;
import com.common.business.service.SuperService;
import com.common.business.vo.PagingVO;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.entity.FirstMileDeliveryDetailEntity;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;

/**
 * <p>
 * 海外仓入库单 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
public interface OverseasWarehouseInboundService extends SuperService<OverseasWarehouseInboundEntity> {

    /**
     * 新增
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-16
     */
    BaseResultDTO.AddDTO add(OverseasWarehouseInboundDTO.AddDTO dto);

    /**
     * 修改
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-16
     */
    Boolean update(OverseasWarehouseInboundDTO.UpdateDTO dto);


    OverseasWarehouseInboundEntity getByCode(String receivingCode, String notInStockStatus);

    /**
     * 分页查询
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-21
     */
    PagingVO<OverseasWarehouseInboundDTO.ListDTO> paging(PagingDTO<OverseasWarehouseInboundDTO.PagingParamDTO> dto);

    /**
     * 手动完结
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-24
     */
    BatchResultDTO manualFinish(OverseasWarehouseInboundDTO.FinishDTO dto);

    /**
     * 详情
     *
     * @param id
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    OverseasWarehouseInboundDTO.ViewDTO view(String id);

    /**
     * 详情列表
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    List<OverseasWarehouseInboundDetailDTO.ViewListDTO> viewList(OverseasWarehouseInboundDTO.ViewListReqDTO dto);

    /**
     * 状态数量统计
     *
     * @param dto
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    List<OverseasWarehouseInboundDTO.CountDTO> listCount(PermissionsDTO dto);

    /**
     * 取消
     *
     * @param id ID
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    BatchResultDTO cancel(String id);

    /**
     * 删除
     *
     * @param id ID
     * @return
     * @author Jim
     * @date: 2023-11-27
     */
    BatchResultDTO delete(String id);

    /**
     * d导出
     *
     * @param dto 条件
     * @return Boolean
     * @author Jim
     * @date: 2023-11-27
     */
    Boolean exportExcel(OverseasWarehouseInboundDTO.ExportDTO dto, HttpServletResponse response);

    List<String> getReceiptNumbersForStatus(List<String> statusList, String platform);

    /**
     * 根据来源id查询入库单
     *
     * @param sourceIds
     * @return java.util.List<com.erp.model.wms.entity.OverseasWarehouseInboundEntity>
     * @Author Luo_WG
     * @Date 2023/11/27 17:36
     **/
    List<OverseasWarehouseInboundEntity> listBySourceIds(List<String> sourceIds);

    /**
     * 通过source_id查询
     * @param notInStockStatus 指定不属于的入库状态
     */
    OverseasWarehouseInboundEntity getBySourceId(String sourceId, String notInStockStatus);

    /**
     * 修改入库单状态
     *
     * @param status
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/12/5 14:48
     **/
    Boolean updateInstockStatus(List<String> ids, String status);

    /**
     * 海外单生成直接调拨单
     *
     * @Author Jim
     * @Date 2023/12/5
     **/
    String generateTransferOut(OverseasWarehouseInboundEntity mainEntity,
                               List<OverseasWarehouseInboundDetailEntity> detailEntityList,
                               Map<String, Integer> receiverdMap);

    /**
     * 推动海外入库单到第三方
     *
     * @Author Jim
     * @Date 2023/12/6
     **/
    ApiResult<String> pullThirdOverseasPlatform(OverseasProviderEntity providerEntity,
                                                OverseasWarehouseInboundEntity mainEntity,
                                                List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList,
                                                String verityCode);

    /**
     * 推动海外入库单到第三方
     *
     * @Author Jim
     * @Date 2023/12/6
     **/
    ApiResult<String> pullThirdOverseasPlatformWithSkuMapping(
            OverseasProviderEntity providerEntity,
            OverseasWarehouseInboundEntity mainEntity,
            List<FirstMileDeliveryDetailEntity> deliveryDetailEntityList,
            String verityCode);

    ApiResult<?> handlePlatformMessage(PlatformInboundDTO dto);

    List<BaseDropDownDTO.CommonDTO> getLogisticByTransferWarehouseId(String transferWarehouseId);
}

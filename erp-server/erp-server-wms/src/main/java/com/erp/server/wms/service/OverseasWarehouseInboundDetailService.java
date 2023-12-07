package com.erp.server.wms.service;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;

import java.util.List;

/**
 * <p>
 * 海外仓入库单详情 服务类
 * </p>
 *
 * @author Jim
 * @since 2023-11-16
 */
public interface OverseasWarehouseInboundDetailService extends SuperService<OverseasWarehouseInboundDetailEntity> {

    /**
    * 新增
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasWarehouseInboundDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasWarehouseInboundDetailDTO.UpdateDTO dto);


    /**
     * 修改
     * @author Jim
     * @date: 2023-11-24
     * @param detailId
     * @return
     */
    List<OverseasWarehouseInboundDTO.ReceiveRecordView> listReceiveRecord(String detailId);

    List<OverseasWarehouseInboundDetailEntity> getByMainId(String mainId);

    /**
     * 动手签收
     * @author Jim
     * @date: 2023-11-24
     * @param dto 签收信息
     * @return 处理结果
     */
    BatchResultDTO manualReceived(OverseasWarehouseInboundDTO.ReceivedDTO dto);

    /**
     * 通过mainId列表查询
     * @author Jim
     * @date: 2023-11-27
     * @param mainIdList 主表ID
     * @return entityList
     */
    List<OverseasWarehouseInboundDetailEntity> getByMainIds(List<String> mainIdList);

    /**
     * 通过Ids列表查询
     * @author Jim
     * @date: 2023-11-27
     * @param idList 表ID
     * @return entityList
     */
    List<OverseasWarehouseInboundDetailEntity> getByIds(List<String> idList);


    /**
     * 通过mainId查询是否所有签收
     * @author Jim
     * @date: 2023-12-06
     */
    Boolean checkAllReceiveByMainId(String mainId);
}

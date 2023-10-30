package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.FbaShipmentDTO;

import java.util.List;

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
    Boolean pullShipment(FbaShipmentDTO.pullShipmentDTO dto);

    /**
     * 查询发货记录
     * @Author Luo_WG
     * @Date 2023/10/30 17:38
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.DeliverRecordDTO>>
     **/
    List<FbaShipmentDTO.DeliverRecordView> listDeliverRecord(String id);

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
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.ReceiveRecordView>>
     **/
    List<FbaShipmentDTO.ReceiveRecordView> listReceiveRecord(PagingDTO<FbaShipmentDTO.ReceiveRecordParam> dto);

    /**
     * 查询详情
     * @param id
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.ReceiveRecordView>>
     **/
    List<FbaShipmentDTO.ViewDTO> view(String id);

    /**
     * 完结货件
     * @param ids
     * @return com.common.core.controller.vo.ApiResult
     **/
    Boolean finishShipment(BaseIdsDTO.IdsDTO ids);

    /**
     * 下推发货单列表查询
     * @param ids
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.FbaShipmentDTO.GenerateDeliverView>>
     **/
    List<FbaShipmentDTO.GenerateDeliverView> generateDeliverView(BaseIdsDTO.IdsDTO ids);
}

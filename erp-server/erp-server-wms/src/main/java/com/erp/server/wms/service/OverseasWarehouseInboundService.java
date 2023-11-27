package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.dto.OverseasWarehouseInboundDetailDTO;
import com.erp.model.wms.entity.OverseasWarehouseInboundEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.OverseasWarehouseInboundDTO;

import java.util.List;

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
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(OverseasWarehouseInboundDTO.AddDTO dto);

    /**
    * 修改
    * @author Jim
    * @date: 2023-11-16
    * @param dto
    * @return
    */
    Boolean update(OverseasWarehouseInboundDTO.UpdateDTO dto);


    OverseasWarehouseInboundEntity getByCode(String receivingCode);

    /**
     * 分页查询
     * @author Jim
     * @date: 2023-11-21
     * @param dto
     * @return
     */
    PagingVO<OverseasWarehouseInboundDTO.ListDTO> paging(PagingDTO<OverseasWarehouseInboundDTO.PagingParamDTO> dto);

    /**
     * 手动完结
     * @author Jim
     * @date: 2023-11-24
     * @param dto
     * @return
     */
    BatchResultDTO manualFinish(OverseasWarehouseInboundDTO.FinishDTO dto);

    /**
     * 详情
     * @author Jim
     * @date: 2023-11-27
     * @param id
     * @return
     */
    OverseasWarehouseInboundDTO.ViewDTO view(String id);

    /**
     * 详情列表
     * @author Jim
     * @date: 2023-11-27
     * @param dto
     * @return
     */
    List<OverseasWarehouseInboundDetailDTO.ViewListDTO> viewList(OverseasWarehouseInboundDTO.ViewListReqDTO dto);
}

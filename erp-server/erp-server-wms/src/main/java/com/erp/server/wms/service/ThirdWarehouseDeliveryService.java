package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.dto.GenerateDeliveryAndOutStockDTO;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.wms.entity.ThirdWarehouseDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.ThirdWarehouseDeliveryDTO;

import java.util.List;

/**
 * <p>
 * 三方仓发货单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-10-17
 */
public interface ThirdWarehouseDeliveryService extends SuperService<ThirdWarehouseDeliveryEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-10-17
    * @return
    */
    ThirdWarehouseDeliveryEntity add(ThirdWarehouseDeliveryEntity entity);

    ThirdWarehouseDeliveryEntity getByCodeAndSoId(String outCode,String soId);

    ThirdWarehouseDeliveryEntity getLatestBySoId(String soId);

    ThirdWarehouseDeliveryEntity getLatestByCode(String code);

    void generateDeliveryAndOutStock(GenerateDeliveryAndOutStockDTO generateDeliveryAndOutStockDTO);

    PagingVO<ThirdWarehouseDeliveryDTO.PagingViewDTO> paging(PagingDTO<ThirdWarehouseDeliveryDTO.PagingParamDTO> dto);

    ThirdWarehouseDeliveryDTO.ViewDTO view(String id);

    void export(ThirdWarehouseDeliveryDTO.PagingParamDTO dto);

    List<ThirdWarehouseDeliveryDTO.TabListDTO> tabList();

    BatchResultDTO retryOutstock(String id);

    ThirdWarehouseDeliveryEntity generatePlatformDelivery(String soId);
}

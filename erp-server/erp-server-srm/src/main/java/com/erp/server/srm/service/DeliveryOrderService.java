package com.erp.server.srm.service;
import com.common.business.vo.PagingVO;
import com.erp.model.srm.entity.DeliveryOrderEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.srm.dto.DeliveryOrderDTO;

import java.util.List;

/**
 * <p>
 * 送货单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-01-12
 */
public interface DeliveryOrderService extends SuperService<DeliveryOrderEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-01-12
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DeliveryOrderDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-01-12
    * @param dto
    * @return
    */
    Boolean update(DeliveryOrderDTO.UpdateDTO dto);

    Integer countByPrint(String supplierId, boolean isPrint);

    Integer countByReceiveStatus(String supplierId, String code);

    PagingVO<DeliveryOrderDTO.ListDTO> paging(PagingDTO<DeliveryOrderDTO.ParamDTO> dto);

    List<DeliveryOrderDTO.TabListDTO> tabList();

    DeliveryOrderDTO.ViewDTO view(String id);
}

package com.erp.server.wms.service;
import com.common.business.vo.PagingVO;
import com.erp.model.wms.entity.B2bThirdDeliveryEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.wms.dto.B2bThirdDeliveryDTO;

import java.util.List;

/**
 * <p>
 * B2B三方发货单 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-11-26
 */
public interface B2bThirdDeliveryService extends SuperService<B2bThirdDeliveryEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-11-26
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(B2bThirdDeliveryDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-11-26
    * @param dto
    * @return
    */
    Boolean update(B2bThirdDeliveryDTO.UpdateDTO dto);


    List<B2bThirdDeliveryDTO.TabListDTO> tabList();

    PagingVO<B2bThirdDeliveryDTO.PagingViewDTO> paging(PagingDTO<B2bThirdDeliveryDTO.PagingParamDTO> dto);

    void export(B2bThirdDeliveryDTO.PagingParamDTO dto);

    B2bThirdDeliveryDTO.ViewDTO view(String id, String soId);
}

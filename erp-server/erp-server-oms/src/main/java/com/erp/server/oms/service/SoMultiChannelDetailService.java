package com.erp.server.oms.service;
import com.erp.model.oms.entity.SoMultiChannelDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.SoMultiChannelDetailDTO;
import com.erp.model.oms.entity.SoMultiChannelEntity;

import java.util.List;

/**
 * <p>
 * 多渠道订单明细 服务类
 * </p>
 *
 * @author zdy
 * @since 2025-08-20
 */
public interface SoMultiChannelDetailService extends SuperService<SoMultiChannelDetailEntity> {

    /**
    * 新增
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(SoMultiChannelDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author zdy
    * @date: 2025-08-20
    * @param dto
    * @return
    */
    Boolean update(SoMultiChannelDetailDTO.UpdateDTO dto);


    List<SoMultiChannelDetailEntity> addDetail(SoMultiChannelEntity soMultiChannelEntity, List<SoMultiChannelDetailDTO.AddDTO> detailList);
}

package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoReceiverEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoReceiverDTO;

/**
 * <p>
 * 中台销售订单收货人表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
public interface DmpSoReceiverService extends SuperService<DmpSoReceiverEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoReceiverDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-24
    * @param dto
    * @return
    */
    Boolean update(DmpSoReceiverDTO.UpdateDTO dto);


}

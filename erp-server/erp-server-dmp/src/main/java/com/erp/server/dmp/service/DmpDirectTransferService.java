package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpDirectTransferEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpDirectTransferDTO;

/**
 * <p>
 * 中台直接调拨单 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-02
 */
public interface DmpDirectTransferService extends SuperService<DmpDirectTransferEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpDirectTransferDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    Boolean update(DmpDirectTransferDTO.UpdateDTO dto);


}

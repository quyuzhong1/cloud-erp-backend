package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpDirectTransferDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpDirectTransferDetailDTO;

/**
 * <p>
 * 中台直接调拨单详情表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-07-02
 */
public interface DmpDirectTransferDetailService extends SuperService<DmpDirectTransferDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpDirectTransferDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-07-02
    * @param dto
    * @return
    */
    Boolean update(DmpDirectTransferDetailDTO.UpdateDTO dto);


}

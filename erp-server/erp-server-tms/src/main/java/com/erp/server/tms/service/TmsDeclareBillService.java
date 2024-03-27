package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsDeclareBillEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsDeclareBillDTO;

/**
 * <p>
 * 报关单 服务类
 * </p>
 *
 * @author lrp
 * @since 2024-03-27
 */
public interface TmsDeclareBillService extends SuperService<TmsDeclareBillEntity> {

    /**
    * 新增
    * @author lrp
    * @date: 2024-03-27
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsDeclareBillDTO.AddDTO dto);

    /**
    * 修改
    * @author lrp
    * @date: 2024-03-27
    * @param dto
    * @return
    */
    Boolean update(TmsDeclareBillDTO.UpdateDTO dto);


}

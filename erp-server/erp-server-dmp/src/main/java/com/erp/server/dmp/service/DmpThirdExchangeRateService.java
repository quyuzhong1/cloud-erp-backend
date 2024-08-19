package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpThirdExchangeRateEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpThirdExchangeRateDTO;

/**
 * <p>
 * 第三方汇率 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-08-19
 */
public interface DmpThirdExchangeRateService extends SuperService<DmpThirdExchangeRateEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-08-19
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpThirdExchangeRateDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-08-19
    * @param dto
    * @return
    */
    Boolean update(DmpThirdExchangeRateDTO.UpdateDTO dto);


}

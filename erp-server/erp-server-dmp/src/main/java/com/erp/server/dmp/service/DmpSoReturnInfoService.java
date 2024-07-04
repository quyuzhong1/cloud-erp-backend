package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoReturnInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoReturnInfoDTO;

/**
 * <p>
 * 销售退货订单主表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-30
 */
public interface DmpSoReturnInfoService extends SuperService<DmpSoReturnInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-30
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoReturnInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-30
    * @param dto
    * @return
    */
    Boolean update(DmpSoReturnInfoDTO.UpdateDTO dto);


}

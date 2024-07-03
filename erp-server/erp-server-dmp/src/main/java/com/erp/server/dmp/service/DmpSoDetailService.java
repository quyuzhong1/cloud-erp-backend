package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoDetailEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoDetailDTO;

/**
 * <p>
 * 中台销售订单详情表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
public interface DmpSoDetailService extends SuperService<DmpSoDetailEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoDetailDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-24
    * @param dto
    * @return
    */
    Boolean update(DmpSoDetailDTO.UpdateDTO dto);


}

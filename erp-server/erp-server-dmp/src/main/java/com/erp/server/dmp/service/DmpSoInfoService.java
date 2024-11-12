package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpSoInfoEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpSoInfoDTO;
import com.erp.model.wms.dto.ShudiyunB2cOrderDTO;

import java.util.List;

/**
 * <p>
 * 中台销售订单表 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-24
 */
public interface DmpSoInfoService extends SuperService<DmpSoInfoEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-24
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpSoInfoDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-24
    * @param dto
    * @return
    */
    Boolean update(DmpSoInfoDTO.UpdateDTO dto);

    /**
     * 数帝云线上字段映射
     */
    List<ShudiyunB2cOrderDTO> shudiyunFieldDmpOrderHandler(String thirdCode);
}

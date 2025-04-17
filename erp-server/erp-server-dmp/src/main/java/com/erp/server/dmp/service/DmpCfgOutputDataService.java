package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgOutputDataEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgOutputDataDTO;

/**
 * <p>
 * 输出数据获取配置 服务类
 * </p>
 *
 * @author shukai
 * @since 2025-02-10
 */
public interface DmpCfgOutputDataService extends SuperService<DmpCfgOutputDataEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2025-02-10
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgOutputDataDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2025-02-10
    * @param dto
    * @return
    */
    Boolean update(DmpCfgOutputDataDTO.UpdateDTO dto);


}

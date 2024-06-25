package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpCfgInputConvertEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpCfgInputConvertDTO;

/**
 * <p>
 * 外部系统接口转换内部数据 服务类
 * </p>
 *
 * @author shukai
 * @since 2024-06-11
 */
public interface DmpCfgInputConvertService extends SuperService<DmpCfgInputConvertEntity> {

    /**
    * 新增
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpCfgInputConvertDTO.AddDTO dto);

    /**
    * 修改
    * @author shukai
    * @date: 2024-06-11
    * @param dto
    * @return
    */
    Boolean update(DmpCfgInputConvertDTO.UpdateDTO dto);


}

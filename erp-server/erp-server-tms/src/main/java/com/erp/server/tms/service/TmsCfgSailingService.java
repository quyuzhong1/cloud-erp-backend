package com.erp.server.tms.service;
import com.erp.model.tms.entity.TmsCfgSailingEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.tms.dto.TmsCfgSailingDTO;

/**
 * <p>
 * 截单开船配置 服务类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
public interface TmsCfgSailingService extends SuperService<TmsCfgSailingEntity> {

    /**
    * 新增
    * @author will
    * @date: 2024-03-15
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(TmsCfgSailingDTO.AddDTO dto);

    /**
    * 修改
    * @author will
    * @date: 2024-03-15
    * @param dto
    * @return
    */
    Boolean update(TmsCfgSailingDTO.UpdateDTO dto);


}

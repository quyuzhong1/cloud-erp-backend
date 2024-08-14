package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.DmpLogisticsTrackEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.DmpLogisticsTrackDTO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2024-08-07
 */
public interface DmpLogisticsTrackService extends SuperService<DmpLogisticsTrackEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(DmpLogisticsTrackDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2024-08-07
    * @param dto
    * @return
    */
    Boolean update(DmpLogisticsTrackDTO.UpdateDTO dto);


}

package com.erp.server.oms.service;
import com.erp.model.oms.entity.KolCooperationPlatformEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.oms.dto.KolCooperationPlatformDTO;

/**
 * <p>
 * 达人合作平台信息 服务类
 * </p>
 *
 * @author jack
 * @since 2025-12-02
 */
public interface KolCooperationPlatformService extends SuperService<KolCooperationPlatformEntity> {

    /**
    * 新增
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return
    */
    BaseResultDTO.AddDTO add(KolCooperationPlatformDTO.AddDTO dto);

    /**
    * 修改
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return
    */
    Boolean update(KolCooperationPlatformDTO.UpdateDTO dto);


}

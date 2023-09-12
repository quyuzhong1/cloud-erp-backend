package com.erp.server.sys.service;
import com.erp.model.sys.entity.PdaUserSkipVersionEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.sys.dto.PdaUserSkipVersionDTO;

/**
 * <p>
 * PDA用户跳过版本升级记录表 服务类
 * </p>
 *
 * @author Luo_WG
 * @since 2023-09-12
 */
public interface PdaUserSkipVersionService extends SuperService<PdaUserSkipVersionEntity> {

    /**
    * 新增
    * @author Luo_WG
    * @date: 2023-09-12
    * @param dto
    * @return
    */
    String add(PdaUserSkipVersionDTO.AddDTO dto);

    /**
    * 修改
    * @author Luo_WG
    * @date: 2023-09-12
    * @param dto
    * @return
    */
    Boolean update(PdaUserSkipVersionDTO.UpdateDTO dto);


}

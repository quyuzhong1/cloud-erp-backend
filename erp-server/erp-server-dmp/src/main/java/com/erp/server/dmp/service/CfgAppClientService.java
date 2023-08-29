package com.erp.server.dmp.service;
import com.erp.model.dmp.entity.CfgAppClientEntity;
import com.common.business.service.SuperService;
import com.common.business.dto.base.*;
import com.erp.model.dmp.dto.CfgAppClientDTO;

/**
 * <p>
 * 第三方应用程序信息表 服务类
 * </p>
 *
 * @author Lambda
 * @since 2023-08-28
 */
public interface CfgAppClientService extends SuperService<CfgAppClientEntity> {

    /**
    * 新增
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    String add(CfgAppClientDTO.AddDTO dto);

    /**
    * 修改
    * @author Lambda
    * @date: 2023-08-28
    * @param dto
    * @return
    */
    Boolean update(CfgAppClientDTO.UpdateDTO dto);


    /**
     * 获取根据信息 获取到配置信息
     * @author yl
     * @date 2023-08-29 10:43
     * @param dto
     * @return com.erp.model.dmp.entity.CfgAppClientEntity
     */
    CfgAppClientEntity getCfgAppClient(CfgAppClientDTO.FindDTO dto);
}

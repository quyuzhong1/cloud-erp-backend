package com.erp.server.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.sys.dto.SysCodeDTO;
import com.erp.model.sys.entity.SysCodeEntity;

/**
 * @author Will
 * @version 1.0
 * @description: TODO
 * @date 2022/11/21 11:35
 */
public interface SysCodeService extends IService<SysCodeEntity> {

    /**
     * @description: 保存系统编码数据
     * @author Will
     * @date: 2022/11/21 12:08
     * @param dto
     * @return boolean
     */
    boolean saveSysCode(SysCodeDTO dto);
    /**
     * @description: 根据编码信息生成系统编码
     * @author Will
     * @date: 2022/11/21 12:15
     * @param dto
     * @return String
     */
    String getSysCode(SysCodeDTO dto);
}

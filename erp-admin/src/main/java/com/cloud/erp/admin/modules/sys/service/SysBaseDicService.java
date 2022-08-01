package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.entity.SysBaseDicEntity;
import com.erp.common.dto.base.BaseDicDTO;

import java.util.List;

/**
 * @Classname SysBaseDicService
 * @Description TODO
 * @Date 2022-07-20 16:58
 * @Created by yl
 */

public interface SysBaseDicService extends IService<SysBaseDicEntity> {
    List<SysBaseDicEntity> listByDicType(BaseDicDTO dto);
}

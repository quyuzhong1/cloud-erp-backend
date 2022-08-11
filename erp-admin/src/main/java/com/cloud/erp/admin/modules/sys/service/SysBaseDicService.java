package com.cloud.erp.admin.modules.sys.service;

import com.cloud.erp.admin.modules.sys.entity.SysBaseDicEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.dto.base.BaseDicDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author yl
 * @since 2022-08-09
 */
public interface SysBaseDicService extends IService<SysBaseDicEntity> {
    List<SysBaseDicEntity> listByDicType(BaseDicDTO dto);
}

package com.erp.server.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseDicDTO;
import com.erp.model.sys.entity.SysBaseDicEntity;

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

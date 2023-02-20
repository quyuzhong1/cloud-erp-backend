package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.business.dto.base.BaseSearchDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.BatchSavePostUserDTO;
import com.erp.model.sys.entity.SysPostUserEntity;

import java.util.List;

/**
 * @Classname SysPostUserService
 * @Description TODO
 * @Date 2022-07-12 17:09
 * @Created by yl
 */
public interface SysPostUserService  extends IService<SysPostUserEntity> {
    void removeByPostId(List<String> ids);

    List<SysUserDTO> findPostUser(BaseSearchDTO dto);

    boolean saveBatchPostUser(BatchSavePostUserDTO dto);
}

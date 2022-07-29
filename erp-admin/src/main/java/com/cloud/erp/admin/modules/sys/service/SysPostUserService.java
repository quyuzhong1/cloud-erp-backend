package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.entity.SysPostUserEntity;
import com.cloud.erp.admin.modules.sys.vo.SysUserVO;
import com.erp.common.dto.BaseSearchDTO;

import java.util.List;

/**
 * @Classname SysPostUserService
 * @Description TODO
 * @Date 2022-07-12 17:09
 * @Created by yl
 */
public interface SysPostUserService  extends IService<SysPostUserEntity> {
    void removeByPostId(List<String> ids);

    List<SysUserVO> findPostUser(BaseSearchDTO dto);
}

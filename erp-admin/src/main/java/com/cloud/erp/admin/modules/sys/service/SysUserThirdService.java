package com.cloud.erp.admin.modules.sys.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cloud.erp.admin.modules.sys.entity.SysUserThirdEntity;

/**
 * ${comments}
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
public interface SysUserThirdService extends IService<SysUserThirdEntity> {


    void bindingThirdParty(String uid, String flagId,String bindingPlatform);

    SysUserThirdEntity findByUnionId(String flagId);

    SysUserThirdEntity findByUserId(String uid);
}


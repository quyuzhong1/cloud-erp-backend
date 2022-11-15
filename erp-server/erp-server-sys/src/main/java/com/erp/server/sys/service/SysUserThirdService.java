package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.common.modules.sys.dto.FindUserByThirdDTO;
import com.erp.common.modules.third.dto.ThirdUnionDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;

import java.util.List;

/**
 * ${comments}
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
public interface SysUserThirdService extends IService<SysUserThirdEntity> {


    void bindingThirdParty(String uid, String flagId, String bindingPlatform);

    SysUserThirdEntity findByUnionId(String flagId);

    SysUserThirdEntity findByUserId(String uid);

    boolean checkIfBinding(String uid, String flagId, String bindingPlatform);

    boolean removeThirdParty(String bindingThird);

    SysUserInfoEntity getUserIdByThird(FindUserByThirdDTO thirdDTO);


    List<ThirdUnionDTO> getUnionByPlatform(String platform);
}


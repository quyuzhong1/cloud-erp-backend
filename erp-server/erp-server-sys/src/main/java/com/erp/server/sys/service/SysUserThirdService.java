package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.FindUserDTO;
import com.erp.model.sys.dto.FindUserByThirdDTO;
import com.erp.model.sys.vo.ThirdUnionDTO;
import com.erp.model.sys.entity.SysUserInfoEntity;
import com.erp.model.sys.entity.SysUserThirdEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * ${comments}
 *
 * @author yl
 * @email ylstrive@gmail.com
 * @date 2022-07-11 14:05:47
 */
public interface SysUserThirdService extends IService<SysUserThirdEntity> {


    void bindingThirdParty(String uid, String flagId,String thirdOpenId,String thirdUserId, String bindingPlatform);

    SysUserThirdEntity findByUnionId(String flagId);

    SysUserThirdEntity findByUserId(String uid);

    boolean checkIfBinding(String uid, String flagId, String bindingPlatform);

    boolean removeThirdParty(String bindingThird);

    SysUserInfoEntity getUserIdByThird(FindUserByThirdDTO thirdDTO);


    List<ThirdUnionDTO> getUnionByPlatform(String platform);

    /**
     * 根据用户id 删除绑定关系
     * @author yl
     * @date 2022-11-25 11:26
     * @param userIds
     * @return void
     */
    void deleteByUserIds(List<String> userIds);

    /**
     * 根据平台和用户id获取平台信息
     * @param platform
     * @param userIds
     * @return
     */
    List<ThirdUnionDTO> getUnionByPlatformAndUserIds(String platform, List<String> userIds);

    List<ThirdUnionDTO> getThirdByUserIds(String platform, List<String> userIds);

    SysUserThirdEntity getUserByThird(String platform, String thirdId);

    /**
     * 获取第三方绑定的用户
     * @author yl
     * @date 2023-06-19 20:00
     * @param
     * @return java.util.List<com.common.business.dto.FindUserDTO>
     */
    List<FindUserDTO> listThirdBindUser();

    /**
     * 批量获取获取第三方绑定的用户
     * @param platform
     * @param thirdIds
     * @return
     */
    List<SysUserThirdEntity> getUserByThirdIdList(String platform, ArrayList<String> thirdIds);
}


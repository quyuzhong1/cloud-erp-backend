package com.erp.server.sys.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.common.business.dto.base.BaseSearchDTO;
import com.erp.model.sys.dto.SysUserDTO;
import com.erp.model.sys.dto.BatchSavePostUserDTO;
import com.erp.model.sys.entity.SysPostUserEntity;

import java.util.List;

/**
 * @Classname SysPostUserService

 * @Date 2022-07-12 17:09
 * @Created by yl
 */
public interface SysPostUserService  extends IService<SysPostUserEntity> {
    void removeByPostId(List<String> ids);

    List<SysUserDTO> findPostUser(BaseSearchDTO dto);

    boolean saveBatchPostUser(BatchSavePostUserDTO dto);

    /**
     * 根据用户id查询岗位
     * @Author Luo_WG
     * @Date 2024/1/12 10:55
     * @param userId
     * @return java.util.List<com.erp.model.sys.entity.SysPostUserEntity>
     **/
    List<SysPostUserEntity> getByUserId(String userId);

    List<SysPostUserEntity> getUserIdByPostIds(List<String> ids);
}

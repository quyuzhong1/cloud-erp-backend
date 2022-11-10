package com.erp.server.plm.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.erp.model.plm.entity.UserCancelNoticeEntity;

import java.util.List;

/**
 * @Classname UserCancelNoticeService
 * @Description TODO
 * @Date 2022-11-10 15:09
 * @Created by yl
 */
public interface UserCancelNoticeService extends IService<UserCancelNoticeEntity> {
    List<String> getUserCancelNoticeIds(String userId);

    Boolean updateState(String userId, String id, Boolean state);
}

package com.erp.server.plm.service;

import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.LoginUser;

import java.util.List;

/**
 * @Classname CommonService
 * @Description TODO
 * @Date 2022-10-12 15:20
 * @Created by yl
 */
public interface CommonService {

    public LoginUser getUserInfo();

    String getNameByIds(List<String> userIds);

    String getNameById (String userId);

    public List<FindUserDTO> getAllUser();


    String getUidByUnionId(String fsPlatform, String fsUnionId);
}

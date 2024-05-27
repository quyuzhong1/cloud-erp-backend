package com.erp.server.plm.service;

import com.common.business.dto.FindUserDTO;

import java.util.List;

/**
 * @Classname CommonService

 * @Date 2022-10-12 15:20
 * @Created by yl
 */
public interface CommonService {

    String getNameByIds(List<String> userIds);

    String getNameById (String userId);

    public List<FindUserDTO> getAllUser();


    String getUidByUnionId(String fsPlatform, String fsUnionId);

    /**
     * @description: 获取当前审核人
     * @author Will
     * @date: 2024/3/18 19:26
     * @param businessKey
     * @return List<String>
     */
    List<String> listProcessCurBusinessIds (String businessKey);
}

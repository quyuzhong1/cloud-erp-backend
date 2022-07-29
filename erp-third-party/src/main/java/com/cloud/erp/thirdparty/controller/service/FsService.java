package com.cloud.erp.thirdparty.controller.service;


import com.erp.common.modules.sys.dto.FindThirdUserDTO;

import java.util.Map;

/**
 * @Classname FsService
 * @Description TODO
 * @Date 2022-07-20 18:24
 * @Created by yl
 */
public interface FsService {

    Map<String, Object> getFsUser(FindThirdUserDTO dto);
}

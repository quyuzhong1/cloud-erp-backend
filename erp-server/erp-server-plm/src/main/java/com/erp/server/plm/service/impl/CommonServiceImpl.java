package com.erp.server.plm.service.impl;

import com.erp.common.modules.sys.dto.FindUserDTO;
import com.erp.common.vo.LoginUser;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.plm.interceptor.PlmInterceptor;
import com.erp.server.plm.service.CommonService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @Classname CommonServiceImpl
 * @Description TODO
 * @Date 2022-10-12 15:20
 * @Created by yl
 */
@Service
public class CommonServiceImpl implements CommonService {

    @Autowired
    private SysUserFeign sysUserFeign;


    /**
     * 获取用户信息
     *
     * @return
     */
    @Override
    public LoginUser getUserInfo() {
        String userId = "";
        String userName = "";
        LoginUser loginUser = PlmInterceptor.threadLocal.get();
        if (Objects.isNull(loginUser)) {
            loginUser = new LoginUser();
            loginUser.setUid(userId);
            loginUser.setUserName(userName);
        }
        return loginUser;
    }


    /**
     * 获取用户名
     *
     * @param
     * @return java.util.List<java.lang.String>
     * @author yl
     * @date 2022-10-11 17:48
     */
    @Override
    public String getNameByIds(List<String> userIds) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        List<String> names = new ArrayList<>();
        for (String userId : userIds) {
            FindUserDTO findUser = userList.stream().filter(u -> userId.equals(u.getUserId())).findFirst().orElse(null);
            if (findUser != null) {
                names.add(findUser.getUserName());
            } else {
                names.add("");
            }
        }
        return StringUtils.join(names, ",");
    }

    @Override
    public String getNameById(String userId) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        FindUserDTO user = userList.stream().filter(u -> userId.equals(u.getUserId())).findFirst().orElse(null);
        if (!Objects.isNull(user)) {
            return user.getUserName();
        }
        return "";
    }


}

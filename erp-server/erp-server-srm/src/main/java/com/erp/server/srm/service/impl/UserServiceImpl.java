package com.erp.server.srm.service.impl;

import com.common.business.constant.UserStateConstants;
import com.common.business.interceptor.CommonInterceptor;
import com.common.business.vo.LoginUser;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.vo.SupplierUserInfoVO;
import com.erp.rpc.wms.feign.SupplierUserFeign;
import com.erp.server.srm.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author zdy
 * @ClassName UserServiceImpl
 * @description: 用户通用方法
 * @date 2024年01月09日
 * @version: 1.0
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private SupplierUserFeign supplierUserFeign;
    /**
     * 获取当前用户供应商id
     * @return
     */
    @Override
    public String getSupplierId(){
        LoginUser loginUser = CommonInterceptor.threadLocal.get();
        if (Objects.nonNull(loginUser)){
            String uid = loginUser.getUid();
            //获取用户关联供应商
            SupplierUserInfoVO info = supplierUserFeign.info(uid);
            if (Objects.nonNull(info)){
                //用户是否禁用
                if (Objects.isNull(info.getUserState()) || !info.getUserState()) {
                    throw new ServiceException(ApiError.ERROR_9016);
                }else {
                    return info.getSupplierId();
                }
            }else {
                //用户未关联供应商
                throw new ServiceException(ApiError.ERROR_USER_NOT_REL_SUPPLIER);
            }
        }else {
            throw new ServiceException(ApiError.ERROR_403);
        }
    }
}

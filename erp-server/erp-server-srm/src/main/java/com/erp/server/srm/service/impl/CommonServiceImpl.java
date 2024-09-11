package com.erp.server.srm.service.impl;

import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.rpc.scm.feign.SupplierFeign;
import com.erp.server.srm.service.CommonService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author yl
 * @Classname CommonServiceImpl

 * @Date 2023-03-15 11:50
 * @Created by yl
 */
@Service
public class CommonServiceImpl implements CommonService {

    @Resource
    private SupplierFeign supplierFeign;

    @Override
    public SupplierEntity getSupplierEntity(){
        LoginUser loginUser = UserContext.getLoginUser();
        if(Objects.isNull(loginUser)){
            throw new ServiceException(ApiError.ERROR_403);
        }
        //查询供应商信息
        SupplierEntity supplier = supplierFeign.getSupplierByUid(loginUser.getUid());
        if(Objects.isNull(supplier)){
            throw new ServiceException(ApiError.ERROR_96001);
        }
        return supplier;
    }
}

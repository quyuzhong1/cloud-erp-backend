package com.erp.server.wms.handler;

import cn.hutool.json.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.BaseController;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
public abstract class AbstractThirdWarehouseHandler extends BaseController implements ThirdWarehouseService {

    @Resource
    private OverseasProviderService overseasProviderService;


    /**
     * 获取授权信息 并设置threadlocal
     */
    public void handleAuthInfo(String id){
        //查询授权信息
        OverseasProviderEntity authEntity = overseasProviderService.getById(id);
        if(Objects.isNull(authEntity)){
            throw new ServiceException(ApiError.THIRD_OVERSEAS_WAREHOUSE_AUTH_ERROR);
        }
        //设置thread-local
        ThirdWarehouseContext.setAuthId(id);
        ThirdWarehouseContext.setAuthMap(authEntity.getAuthJson());
    }

    @Override
    public Boolean authorize(JSONObject authJsonObj){
        return true;
    }

}

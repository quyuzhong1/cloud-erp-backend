package com.erp.server.wms.handler;

import cn.hutool.json.JSONObject;
import com.common.business.threadlocal.ThirdWarehouseContext;
import com.common.core.controller.BaseController;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.erp.model.dmp.dto.ThirdWarehouseTaskDTO;
import com.erp.model.wms.dto.OverseasProviderDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.server.wms.service.OverseasProviderService;
import com.erp.server.wms.service.ThirdWarehouseService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Objects;

@Service
public abstract class AbstractThirdWarehouseHandler extends BaseController implements ThirdWarehouseService {

    @Resource
    private OverseasProviderService overseasProviderService;

    @Resource
    private DmpTaskFeign dmpTaskFeign;

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
        ThirdWarehouseContext.setAuthMap(authEntity.getAuthJson());
    }

    @Override
    public Boolean authorize(OverseasProviderDTO.AuthorizeParamDTO dto){
        boolean result;
        try {
            //调用获取仓库接口，有数据返回则授权成功
            ThirdWarehouseContext.setAuthMap(dto.getAuthJson());
            result = this.hasWarehouse();
            if(result){
                // 授权成功添加数据同步任务
                dmpTaskFeign.createThirdWarehouseTask(new ThirdWarehouseTaskDTO.AddDTO(dto.getId(),dto.getAuthJson(),getPlatForm().getCode()));
            }
        }finally {
            ThirdWarehouseContext.remove();
        }
        return result;
    }

    protected abstract Boolean hasWarehouse();
}

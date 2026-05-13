package com.erp.server.auth.controller.openapi;

import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignInvoke;
import com.common.core.controller.vo.ApiResult;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.scm.feign.ScmDictFeign;
import com.erp.rpc.wms.feign.WmsCommonFeign;
import com.erp.rpc.wms.feign.WmsFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;


/**
 * openapi，公共方法调用
 * @author Administrator
 *
 */
@OpenApi
public class CommonOpenApi {
    @Resource
    private WmsFeign wmsFeign;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private WmsCommonFeign wmsCommonFeign;
    @Resource
    private ScmDictFeign scmDictFeign;
    /**
     * 
     * @param feignInvoke
     * @return
     */
    @OpenApi("common")
    public ApiResult<?> common(@RequestBody FeignInvoke feignInvoke){
        return FeignBuilder.create(null).invokeFeign(feignInvoke);
    }

    /**
     * WMS字典列表
     * @param dictDTO
     * @return
     */
    @OpenApi("wmsDict")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> wmsDictList(@RequestBody BaseDTO.DictDTO dictDTO) {
        return wmsFeign.dictList(dictDTO.getKey());
    }

    /**
     * 枚举下拉框，供前端调用，不用每个枚举类都提供一个单独的接口（每个服务都有专属自己的）
     * @param key
     * @return
     */
    @OpenApi("wmsCommonEnumDropDown")
    public ApiResult<List<Map<String,Object>>> wmsCommonEnumDropDown(String key) {
        return wmsCommonFeign.enumSelect(key);
    }


    @OpenApi("scmDropDownSupplierAllList")
    public ApiResult<List<BaseDropDownDTO.RemarkDTO>> scmDropDownSupplierAllList(String key) {
        return scmDictFeign.listALLSupplierDropDown();
    }
}

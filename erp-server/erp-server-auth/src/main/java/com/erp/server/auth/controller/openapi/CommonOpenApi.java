package com.erp.server.auth.controller.openapi;

import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.wrapper.FeignBuilder;
import com.common.business.wrapper.FeignInvoke;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.scm.feign.ScmDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.wms.feign.WmsCommonFeign;
import com.erp.rpc.wms.feign.WmsFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.server.auth.config.OpenApi;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.Comparator;
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
    private SysUserFeign sysUserFeign;
    @Resource
    private WmsCommonFeign wmsCommonFeign;
    @Resource
    private ScmDictFeign scmDictFeign;
    @Resource
    private WmsTaskFeign wmsTaskFeign;
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
     * WMS字典列表  删除wmsDict调用方式统一使用wmsDictDropDown
     * @param key
     * @return
     */
    @OpenApi("wmsDictDropDown")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> wmsDictList(String key) {
        return wmsFeign.dictList(key);
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

    /**
     * 供应商下拉框，供前端调用，不用每个服务都提供一个单独的接口（每个服务都有专属自己的）
     * @return
     */
    @OpenApi("scmDropDownSupplierAllList")
    public ApiResult<List<BaseDropDownDTO.RemarkDTO>> scmDropDownSupplierAllList() {
        return scmDictFeign.listALLSupplierDropDown();
    }

    /**
     * 根据名称获取仓库列表供前端调用，不用每个服务都提供一个单独的接口（每个服务都有专属自己的）
     * @return
     */
    @OpenApi("wmsWarehouseListOrderByName")
    public ApiResult<List<WarehouseDTO.ListDTO>> wmsWarehouseListOrderByName() {
        List<WarehouseDTO.ListDTO> list = wmsTaskFeign.listApproveWarehouse();
        if (list == null) {
            return ApiResult.success(Collections.emptyList());
        }
        list.sort(Comparator.comparing(WarehouseDTO.ListDTO::getName, Comparator.nullsLast(Comparator.naturalOrder())));
        return ApiResult.success(list);
    }

    /**
     * 获取用户
     * @return
     */
    @OpenApi("sysUserFindList")
    public ApiResult<List<FindUserDTO>> sysUserFindList(BaseSearchDTO dto) {
        return sysUserFeign.userList(dto);
    }
}

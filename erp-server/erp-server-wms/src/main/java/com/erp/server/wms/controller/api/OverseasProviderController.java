package com.erp.server.wms.controller.api;


import com.common.business.vo.PagingVO;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.wms.service.OverseasProviderService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.wms.dto.OverseasProviderDTO;

import java.util.List;

/**
 * 海外物流商
 *
 * @author Luo_WG
 * @since 2023-11-16
 */
@Slf4j
@RestController
@LogSystemModule("海外物流商")
@RequestMapping("/overseasProvider")
public class OverseasProviderController extends BaseController {

    @Resource
    private OverseasProviderService overseasProviderService;

    /**
     * 新增
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "海外物流商新增")
    public ApiResult add(@RequestBody @Validated OverseasProviderDTO.AddDTO dto) {
        overseasProviderService.add(dto);
        return success();
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2023-11-16
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "海外物流商修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "wms:overseasProvider:update",
        serviceClass = OverseasProviderService.class,
        keyIdName = "id")
    public ApiResult update(@RequestBody @Validated OverseasProviderDTO.UpdateDTO dto) {
        overseasProviderService.update(dto);
        return success();
    }

    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/11/16 16:12
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<OverseasProviderDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "wms:overseasProvider:paging",
            tableAlias = "op"
    )
    public ApiResult<PagingVO<OverseasProviderDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<OverseasProviderDTO.PagingParamDTO> dto) {
        PagingVO<OverseasProviderDTO.ListDTO> result = overseasProviderService.paging(dto);
        return success(result);
    }

    /**
     * 详情
     * @author Luo_WG
     * @date:  2023-10-30
     * @param id
     * @return ApiResult<FbaDeliveryDTO.ViewDTO>>
     */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasProvider:view",
            serviceClass = OverseasProviderService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<OverseasProviderDTO.ViewDTO> view(@RequestParam("id") String id) {
        OverseasProviderDTO.ViewDTO result = overseasProviderService.view(id);
        return success(result);
    }

    /**
     * 服务商授权
     * @Author Luo_WG
     * @Date 2023/11/16 16:41
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/authorize")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasProvider:authorize",
            serviceClass = OverseasProviderService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult authorize(@RequestBody @Validated OverseasProviderDTO.AuthorizeParamDTO dto) {
        Boolean flag = overseasProviderService.authorize(dto);
        return flag ? success() : failure();
    }

    /**
     * 取消授权
     * @Author Luo_WG
     * @Date 2023/11/16 16:41
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/cancelAuthorize")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasProvider:cancelAuthorize",
            serviceClass = OverseasProviderService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult cancelAuthorize(@RequestBody @Validated BaseIdDTO dto) {
        Boolean flag = overseasProviderService.cancelAuthorize(dto.getId());
        return flag ? success() : failure();
    }

    /**
     * 根据ERP仓库id查询绑定的海外仓信息
     * @Author Luo_WG
     * @Date 2023/11/23 15:27
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @PostMapping("/listProviderWarehouseByIds")
    public ApiResult<List<OverseasProviderDTO.WarehouseDTO>> listProviderWarehouseByIds(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<OverseasProviderDTO.WarehouseDTO> list = overseasProviderService.listProviderWarehouseByIds(dto.getIds());
        return success(list);
    }

    /**
     * 授权查看
     **/
    @PostMapping("/authorizeView")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasProvider:authorize",
            serviceClass = OverseasProviderService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<OverseasProviderDTO.AuthorizeViewDTO> authorizeView(@RequestBody @Validated BaseIdDTO dto) {
        return success(overseasProviderService.authorizeView(dto));
    }


    /**
     * 三方仓修改
     */
    @PostMapping("/updateThirdWarehouse")
    @LogAction(value = LogActionEnum.UPDATE, desc = "三方仓修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasProvider:update",
            serviceClass = OverseasProviderService.class,
            keyIdName = "id")
    public ApiResult updateThirdWarehouse(@RequestBody @Validated OverseasProviderDTO.UpdateThirdWarehouseDTO dto) {
        overseasProviderService.updateThirdWarehouse(dto);
        return success();
    }


    /**
     * 三方仓修改
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "三方仓修改")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:overseasProvider:update",
            serviceClass = OverseasProviderService.class,
            keyIdName = "id")
    public ApiResult delete(@RequestBody @Validated BaseIdDTO dto) {
        overseasProviderService.delete(dto.getId());
        return success();
    }
}

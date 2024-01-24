package com.erp.server.tms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.dmp.dto.DmpPullTaskDTO;
import com.erp.model.oms.dto.CustomerDTO;
import com.erp.model.tms.dto.TransferDeclareDetailDTO;
import com.erp.model.wms.dto.FirstMileDeliveryDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.erp.server.tms.service.TransferDeclareService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.TransferDeclareDTO;

import java.util.List;

/**
 * 中转报关表
 *
 * @author Luo_WG
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("中转报关表")
@RequestMapping("/transferDeclare")
public class TransferDeclareController extends BaseController {

    @Resource
    private TransferDeclareService transferDeclareService;

    /**
     * 分页列表查询
     * @Author Luo_WG
     * @Date 2024/1/20 14:50
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.tms.dto.TransferDeclareDTO.ListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:transferDeclare:paging",
            tableAlias = "td"
    )
    public ApiResult<PagingVO<TransferDeclareDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<TransferDeclareDTO.PagingParamDTO> dto) {
        PagingVO<TransferDeclareDTO.ListDTO> pagingVO = transferDeclareService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 分页列表tab页
     * @Author Luo_WG
     * @Date 2024/1/20 16:54
     * @param dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.tms.dto.TransferDeclareDTO.TabListDTO>>
     **/
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:transferDeclare:paging",
            tableAlias = "td"
    )
    public ApiResult<List<TransferDeclareDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<TransferDeclareDTO.TabListDTO> tabList = transferDeclareService.tabList(dto);
        return success(tabList);
    }

    /**
    * 新增
    * @author Luo_WG
    * @date:  2024-01-19
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "中转报关表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated TransferDeclareDTO.AddDTO dto) {
        return success(transferDeclareService.add(dto));
    }

    /**
    * 修改
    * @author Luo_WG
    * @date:  2024-01-19
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "中转报关表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:transferDeclare:update",
        serviceClass = TransferDeclareService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated TransferDeclareDTO.UpdateDTO dto) {
        transferDeclareService.update(dto);
        return success();
    }

    /**
     * 详情
     * @Author Luo_WG
     * @Date 2024/1/23 18:07
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.tms.dto.TransferDeclareDTO.ViewDTO>
     **/
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:transferDeclare:view",
            serviceClass = TransferDeclareService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<TransferDeclareDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(transferDeclareService.view(id));
    }


    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:transferDeclare:view",
            serviceClass = TransferDeclareService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<TransferDeclareDetailDTO.ViewDTO> viewDetailList(@RequestBody @Validated PagingDTO<TransferDeclareDTO.ViewDetailParamDTO> dto) {
//        return success(transferDeclareService.viewDetailList(id));
        return null;
    }

}

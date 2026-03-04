package com.erp.server.dmp.controller.api;


import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;
import com.erp.server.dmp.service.CfgAfterPlatformShopService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.CfgAfterPlatformShopDTO;

/**
 * 
 *
 * @author wtr
 * @since 2026-03-03
 */
@Slf4j
@RestController
@LogSystemModule("售后平台店铺配置")
@RequestMapping("/cfgAfterPlatformShop")
public class CfgAfterPlatformShopController extends BaseController {

    @Resource
    private CfgAfterPlatformShopService cfgAfterPlatformShopService;

    /**
    * 新增
    * @author wtr
    * @date:  2026-03-03
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgAfterPlatformShopDTO.AddDTO dto) {
        return success(cfgAfterPlatformShopService.add(dto));
    }

    /**
    * 修改
    * @author wtr
    * @date:  2026-03-03
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:cfgAfterPlatformShop:update",
        serviceClass = CfgAfterPlatformShopService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated CfgAfterPlatformShopDTO.UpdateDTO dto) {
        cfgAfterPlatformShopService.update(dto);
        return success();
    }


    /**
    * 列表查询
    * @author wtr
    * @date: 2026-03-03
    * @param dto
    * @return ApiResult<PagingVO<CfgAfterPlatformShopDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "dmp:cfgAfterPlatformShop:paging",
            tableAlias = ""
    )
    public ApiResult<PagingVO<CfgAfterPlatformShopDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<CfgAfterPlatformShopDTO.PagingParamDTO> dto) {
        return success(cfgAfterPlatformShopService.paging(dto));
    }

}

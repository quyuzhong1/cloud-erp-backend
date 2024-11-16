package com.erp.server.oms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.dto.ShopSysUserAuthDTO;
import com.erp.server.oms.service.ShopSysUserAuthService;
import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 店铺权限设置表
 *
 * @author Will
 * @since 2023-09-01
 */
@Slf4j
@RestController
@RequestMapping("/shopSysUserAuth")
public class ShopSysUserAuthController extends BaseController {

    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;

    /**
    * 批量授权
    * @author Will
    * @date:  2023-09-01
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/batchAuth")
    public ApiResult batchAuth(@RequestBody @Validated ShopSysUserAuthDTO.BatchAuthDTO dto) {
        shopSysUserAuthService.batchAuth(dto);
        return success();
    }

    /**
     * 查看详情
     * @author Will
     * @date: 2023/9/1 14:48
     * @param dto
     * @return ApiResult<ViewDTO>
     */
    @PostMapping("/view")
    public ApiResult<ShopSysUserAuthDTO.ViewDTO> batchAuth(@RequestBody @Validated ShopSysUserAuthDTO.ViewParamDTO dto) {
        ShopSysUserAuthDTO.ViewDTO viewDTO = shopSysUserAuthService.view(dto);
        return success(viewDTO);
    }

}

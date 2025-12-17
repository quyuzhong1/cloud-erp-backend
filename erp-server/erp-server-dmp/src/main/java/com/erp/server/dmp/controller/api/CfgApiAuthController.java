package com.erp.server.dmp.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.CfgApiAuthDTO;
import com.erp.server.dmp.service.CfgApiAuthService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * API授权信息
 * @author Will
 * @version 1.0
 * @date 2023/1/11 11:47
 */
@RestController
@RequestMapping("cfgApiAuth")
@LogSystemModule("API授权信息")
public class CfgApiAuthController extends BaseController {

    @Resource
    private CfgApiAuthService cfgApiAuthService;

    /**
     * 新增
     * @author Will
     * @date: 2023/1/11 16:13
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult add(@RequestBody @Validated CfgApiAuthDTO.ParamDTO dto) {
        Boolean flag = this.cfgApiAuthService.insert(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 编辑
     * @author Will
     * @date: 2023/1/11 16:18
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "编辑")
    public ApiResult update(@RequestBody @Validated CfgApiAuthDTO.ParamDTO dto) {
        this.cfgApiAuthService.update(dto);
        return success();
    }

    @PostMapping("/mongo/save/test")
    public ApiResult saveTest(@RequestBody BaseIdDTO dto)  {
        try {
            if(Integer.valueOf(dto.getId()) < 100){
                cfgApiAuthService.saveMongoTest(dto.getId());
            }else {
                cfgApiAuthService.saveMongoTest(dto.getId(), dto.getId());
            }
            return success();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

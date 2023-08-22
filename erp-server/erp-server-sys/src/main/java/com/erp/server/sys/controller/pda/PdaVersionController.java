package com.erp.server.sys.controller.pda;

import com.erp.model.sys.dto.MessageDTO;
import com.erp.model.sys.dto.PdaVersionDTO;
import com.erp.model.sys.entity.PdaVersionEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.PdaVersionService;
import com.common.core.controller.vo.ApiResult;

import java.util.List;


/**
 * 系统版本控制
 * @author Luo_WG
 * @since 2023-08-14
 */
@RestController
@RequestMapping("/pdaVersion")
public class PdaVersionController extends BaseController {

    @Autowired
    private PdaVersionService pdaVersionService;

    /**
     * 获取pda最新版本
     * @Author Luo_WG
     * @Date 2023/8/14 16:27
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.entity.PdaVersionEntity>
     **/
    @GetMapping(value = "/getPdaVersion")
    public ApiResult<PdaVersionEntity> getPdaVersion() {
        PdaVersionEntity version = pdaVersionService.getPdaVersion();
        return success(version);
    }

    /**
     * 发版
     * @Author Luo_WG
     * @Date 2023/8/14 16:27
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.sys.entity.PdaVersionEntity>
     **/
    @PostMapping(value = "/release")
    public ApiResult release(@RequestBody PdaVersionDTO.AddDTO dto) {
        Boolean flag = pdaVersionService.release(dto);
        return flag == true ? success() : failure();
    }
}

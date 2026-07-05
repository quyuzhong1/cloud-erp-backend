package com.erp.server.dmp.controller.feign;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgFileParseOpenApiDTO;
import com.erp.server.dmp.service.CfgFileParseService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 月结文件解析配置 Feign 控制器。
 *
 * @author openai
 * @since 2026-07-05
 */
@RestController
@RequestMapping("/feign/cfgFileParse")
public class CfgFileParseFeignController extends BaseController {

    @Resource
    private CfgFileParseService cfgFileParseService;

    /**
     * 查询启用月结配置并生成文件夹路径。
     *
     * @param dto 查询参数
     * @return 月结配置文件夹生成结果
     */
    @PostMapping("/generateMonthlyFileParseFolders")
    public ApiResult<List<CfgFileParseOpenApiDTO.ConfigDTO>> generateMonthlyFileParseFolders(@RequestBody @Validated CfgFileParseOpenApiDTO.QueryDTO dto) {
        return success(cfgFileParseService.generateMonthlyFileParseFolders(dto));
    }
}

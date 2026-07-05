package com.erp.rpc.dmp.feign;

import com.common.business.config.FeignErrorDecoder;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.dmp.dto.CfgFileParseOpenApiDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 月结文件解析配置远程调用接口。
 *
 * @author openai
 * @since 2026-07-05
 */
@FeignClient(value = "erp-dmp", path = "/feign/cfgFileParse", contextId = "cfgFileParseFeign", configuration = {FeignErrorDecoder.class})
public interface CfgFileParseFeign {

    /**
     * 查询启用月结配置并生成文件夹路径。
     *
     * @param dto 查询参数
     * @return 月结配置文件夹生成结果
     */
    @PostMapping("/generateMonthlyFileParseFolders")
    ApiResult<List<CfgFileParseOpenApiDTO.ConfigDTO>> generateMonthlyFileParseFolders(@RequestBody CfgFileParseOpenApiDTO.QueryDTO dto);
}

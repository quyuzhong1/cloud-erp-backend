package com.erp.server.tms.controller.api;


import com.common.business.dto.base.BatchResultDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.tms.dto.LogisticsChannelConstraintDTO;
import com.erp.server.tms.service.LogisticsChannelConstraintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 物流渠道规则约束
 *
 * @author lrp
 * @since 2024-02-29
 */
@Slf4j
@RestController
@LogSystemModule("物流渠道规则约束")
@RequestMapping("/logisticsChannelConstraint")
public class LogisticsChannelConstraintController extends BaseController {

    @Resource
    private LogisticsChannelConstraintService logisticsChannelConstraintService;

    /**
     * 列表查询
     * @author lrp
     * @date:  2024-02-29
     * @return ApiResult<String>
     */
    @GetMapping("/list")
    public ApiResult<List<LogisticsChannelConstraintDTO.ListDTO>> list(@RequestParam(value = "channelId") String channelId) {
        return success(logisticsChannelConstraintService.getList(channelId));
    }

    /**
    * 保存
    * @author lrp
    * @date:  2024-02-29
    * @return ApiResult<String>
    */
    @PostMapping("/addAndUpdate")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流渠道规则约束新增")
    public ApiResult<List<BatchResultDTO>> addAndUpdate(@RequestBody @Validated LogisticsChannelConstraintDTO.AddOrUpdateDTO dtoList) {
        List<BatchResultDTO> batchResultDTOList = logisticsChannelConstraintService.addAndUpdate(dtoList);
        return batchResultDTOList.stream().allMatch(BatchResultDTO::getSuccess) ? success(batchResultDTOList) : failure(batchResultDTOList);
    }

    /**
     * 下载导入模板
     * @param response
     * @return
     */
    @GetMapping("/downloadTemplate")
    public ApiResult<Object>downloadTemplate(HttpServletResponse response) {
        logisticsChannelConstraintService.downloadTemplate(response);
        return success();
    }

    /**
     * 国家设置导入
     * @param excelFile
     * @param response
     * @return
     */
    @PostMapping("/import")
    @LogAction(value = LogActionEnum.IMPORT, desc = "国家设置导入")
    public ApiResult<Object>importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = logisticsChannelConstraintService.importExcel(excelFile, response);
        return result ? success() : failure();
    }
}

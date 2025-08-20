package com.erp.server.tms.controller.api;


import lombok.extern.slf4j.Slf4j;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StopWatch;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsTrackService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 物流轨迹表
 *
 * @author zdy
 * @since 2023-11-14
 */
@Slf4j
@RestController
@LogSystemModule("物流轨迹表")
@RequestMapping("/logisticsTrack")
public class LogisticsTrackController extends BaseController {

    @Resource
    private LogisticsTrackService logisticsTrackService;

    /**
    * 新增
    * @author zdy
    * @date:  2023-11-14
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流轨迹表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsTrackDTO.AddDTO dto) {
        return success(logisticsTrackService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2023-11-14
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流轨迹表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsTrack:update",
        serviceClass = LogisticsTrackService.class,
        keyIdName = "id")
    public ApiResult<Object>update(@RequestBody @Validated LogisticsTrackDTO.UpdateDTO dto) {
        logisticsTrackService.update(dto);
        return success();
    }


    /**
     * 接收track123物流轨迹同步数据
     * @return
     */
    @PostMapping("/webhookByTrack123")
    public ApiResult<Object>webhookByTrack123(@RequestBody LogisticsTrackDTO.TrackWebHookDTO dto){
        logisticsTrackService.webhookByTrack123(dto);
        return success();
    }

    /**
     *  异步导入
     * @author zdy
     * @date: 2025/07/18 16:19
     * @param dto
     * @return ApiResult
     */
    @PostMapping(value = "/importExcel")
    public ApiResult<Object> importExcel(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean flag = logisticsTrackService.importExcel(dto);
        return flag == true ? success() : failure();
    }
}

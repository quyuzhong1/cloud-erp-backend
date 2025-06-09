package com.erp.server.tms.controller.api;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.validator.ValidList;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.tms.dto.SettingForecastDTO;
import com.erp.server.tms.service.SettingForecastService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 预报设置
 *
 * @author Lambda
 * @since 2024-01-18
 */
@Slf4j
@RestController
@LogSystemModule("预报设置")
@RequestMapping("/forecastSetting")
public class SettingForecastController extends BaseController {


    @Resource
    private SettingForecastService settingForecastService;

    /**
     * 预报设置列表
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<SettingForecastDTO.ListDTO>> list() {
        List<SettingForecastDTO.ListDTO> list = settingForecastService.listAll();
        return success(list);
    }

    /**
     * 添加
     *
     * @return
     */
    @PostMapping("/add")
    public ApiResult<Object>batchAdd(@RequestBody @Validated ValidList<SettingForecastDTO.SaveOrUpdateDTO> list) {
        Boolean result = settingForecastService.addOrUpdate(list);
        return result ? success() : failure();
    }

    /**
     * 检查是否修改
     *
     * @return
     */
    @PostMapping("/checkIsUpdate")
    public ApiResult<Object>checkIsUpdate(@RequestBody  List<SettingForecastDTO.SaveOrUpdateDTO> list) {
        Boolean result = settingForecastService.checkIsUpdate(list);
        return success(result);
    }


    /**
     * 删除
     *
     * @param dto
     * @return
     */
    @PostMapping("/delete")
    public ApiResult<Object>delete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> idList = dto.getIds();
        Boolean result = settingForecastService.delete(idList);
        return result ? success() : failure();
    }

    /**
     * 物流商列表
     *
     * @return
     */
    @GetMapping("/listLogisticsSupplier")
    public ApiResult<List<BaseDropDownDTO.DisabledDTO>> listLogisticsSupplier() {
        List<BaseDropDownDTO.DisabledDTO> list = settingForecastService.listLogisticsSupplier();
        return success(list);
    }




}

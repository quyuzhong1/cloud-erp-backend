package com.erp.server.sys.controller.api;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.utils.HolidayUtils;
import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.entity.SysCalendarEntity;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.server.sys.service.SysCalendarService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统日历相关
 *
 * @author Cloud
 * @since 2023-02-24
 */
@RestController
@RequestMapping("/sys/calendar")
public class SysCalendarController extends BaseController {

    @Resource
    private SysCalendarService sysCalendarService;

    /**
     * 手动修改系统日期列表
     * @param dto
     * @return
     */
    @PostMapping("/list")
    public ApiResult<List<SysCalendarListVO>> listByDate(@RequestBody @Validated(SysCalendarDTO.SysSelectList.class) SysCalendarDTO.ListDTO dto){
        List<SysCalendarListVO> resultList = sysCalendarService.listByCondition(dto);
        return success(resultList);
    }

    /**
     * 新增或更新日期状态
     * @param dto
     * @return
     */
    @PostMapping("/save/or/update")
    public ApiResult<Boolean> saveOrUpdateBatchDate(@RequestBody @Validated SysCalendarDTO.SaveOrUpdateDTO dto){
        Boolean result = sysCalendarService.saveOrUpdateBatchDate(dto);
        return success(result);
    }


    @PostMapping("/save/year")
    public ApiResult saveYearHoliday(@RequestBody SysCalendarDTO.SaveYearDTO dto){
        if(null == dto.getYear()){
            dto.setYear(LocalDate.now().getYear());
        }
        if(null == dto.getMonth()){
            dto.setYear(LocalDate.now().getMonthValue());
        }
        List<JSONObject> dataList = HolidayUtils.JJRRemarkMap(dto.getYear(), dto.getMonth());

        List<SysCalendarEntity> list = dataList
                .stream()
                .map(str -> JSONUtil.toBean(str, SysCalendarEntity.class))
                .collect(Collectors.toList());
        if(StrUtil.isNotBlank(dto.getOrganization())){
            list.stream().forEach(x -> x.setOrganization(dto.getOrganization()));
        }
        Boolean result = sysCalendarService.initRemarkList(list);
        return success(result);
    }

}

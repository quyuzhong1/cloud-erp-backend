package com.erp.server.sys.controller.api;


import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.server.sys.service.SysCalendarService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;
import com.common.core.controller.BaseController;

import javax.annotation.Resource;
import java.util.List;

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

    @PostMapping("/list")
    public ApiResult<List<SysCalendarListVO>> listByDate(@RequestBody @Validated SysCalendarDTO.ListDTO dto){
        List<SysCalendarListVO> resultList = sysCalendarService.listByCondition(dto);
        return success(resultList);
    }

    @PostMapping("/save/or/update")
    public ApiResult<Boolean> saveOrUpdateBatchDate(@RequestBody @Validated SysCalendarDTO.SaveOrUpdateDTO dto){
        Boolean result = sysCalendarService.saveOrUpdateBatchDate(dto);
        return success(result);
    }


}

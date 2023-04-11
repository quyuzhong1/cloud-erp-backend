package com.erp.server.sys.controller.feign;

import com.erp.model.sys.dto.SysCalendarDTO;
import com.erp.model.sys.vo.SysCalendarListVO;
import com.erp.server.sys.service.SysCalendarService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * TODO
 *
 * @Author Cloud
 * @Date 2023/2/27 18:25
 **/

@RestController
@RequestMapping("feign/calendar")
public class SysCalendarFeignController {

    @Resource
    private SysCalendarService sysCalendarService;


    @PostMapping("/list")
    public List<SysCalendarListVO> listCalendar(@RequestBody SysCalendarDTO.ListDTO dto){
        return sysCalendarService.listByCondition(dto);
    }
}

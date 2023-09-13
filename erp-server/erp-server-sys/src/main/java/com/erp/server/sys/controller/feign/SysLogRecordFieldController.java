package com.erp.server.sys.controller.feign;


import com.erp.model.sys.dto.SysLogRecordFieldListDTO;
import com.erp.model.sys.entity.SysLogRecordFieldEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.SysLogRecordFieldService;
import com.erp.model.sys.dto.SysLogRecordFieldDTO;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SYS系统日志字段保存配置表
 *
 * @author Jim
 * @since 2023-08-29
 */
@Slf4j
@RestController
@RequestMapping("/feign/logRecordField")
public class SysLogRecordFieldController extends BaseController {

    @Resource
    private SysLogRecordFieldService sysLogRecordFieldService;

    /**
     * 列表
     *
     * @return ApiResult<String>
     * @author Jim
     * @date: 2023-08-29
     */
    @PostMapping("/list")
    public List<SysLogRecordFieldListDTO> listByDto(@RequestBody @Validated SysLogRecordFieldDTO.ListDTO dto) {
        return sysLogRecordFieldService.listByDto(dto);
    }
}

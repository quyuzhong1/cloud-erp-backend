package com.erp.server.plm.controller.api;


import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.ProjectTaskTimeRecordDTO;
import com.erp.model.plm.vo.ProjectTaskTimeRecordPageVO;
import com.erp.server.plm.service.ProjectTaskTimeRecordService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 任务工时记录
 *
 * @author Cloud
 * @since 2023-02-23
 */
@RestController
@LogSystemModule("工时统计")
@RequestMapping("task/time/record")
public class ProjectTaskTimeRecordController extends BaseController {

    @Resource
    private ProjectTaskTimeRecordService projectTaskTimeRecordService;

    /**
     * 任务工时分页列表
     * @param dto
     * @return
     */

    @PostMapping("/paging")
//    @DataPermission(operationType = DataAttributeEnum.LIST,
//            tableField = "charge_id",
//            menuCode = "plm:task:time:record:paging",
//            tableAlias = "pt")
    public ApiResult<PagingVO<ProjectTaskTimeRecordPageVO>> pageTaskTimeRecord(@RequestBody PagingDTO<ProjectTaskTimeRecordDTO.PageRecordDto> dto){
        PagingVO<ProjectTaskTimeRecordPageVO>  pageVO = projectTaskTimeRecordService.pageRecord(dto);
        return success(pageVO);
    }

    /**
     * 导出工时统计
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "导出工时统计")
    @PostMapping(value = "/export")
    public ApiResult<Object> exportTaskTime(@RequestBody ProjectTaskTimeRecordDTO.PageRecordDto dto) {
        Boolean flag = projectTaskTimeRecordService.exportTaskTimeList(dto);
        return flag == true ? success() : failure();
    }


}

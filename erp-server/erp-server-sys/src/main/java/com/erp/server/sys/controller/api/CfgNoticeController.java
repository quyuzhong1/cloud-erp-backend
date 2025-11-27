package com.erp.server.sys.controller.api;


import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.validator.ValidList;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.CfgNoticeDTO;
import com.erp.server.sys.service.CfgNoticeService;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Param;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知配置表
 *
 * @author will
 * @since 2025-02-13
 */
@Slf4j
@RestController
@LogSystemModule("通知配置表")
@RequestMapping("/cfgNotice")
public class CfgNoticeController extends BaseController {

    @Resource
    private CfgNoticeService cfgNoticeService;

    /**
     * 分页查询
     * @Auther will
     * @Date 2025/2/13 14:51
     * @param dto
     * @return ApiResult<PagingVO<CfgNoticeDTO.ListDTO>>
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<CfgNoticeDTO.ListDTO>> queryByPage(@RequestBody @Validated PagingDTO<CfgNoticeDTO.SearchParamDTO> dto) {
        PagingVO<CfgNoticeDTO.ListDTO> pagingVO = cfgNoticeService.paging(dto);
        return success(pagingVO);
    }

    /**
    * 新增
    * @author will
    * @date:  2025-02-13
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "通知配置表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated CfgNoticeDTO.AddDTO dto) {
        return success(cfgNoticeService.add(dto));
    }

    /**
    * 修改
    * @author will
    * @date:  2025-02-13
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "通知配置表修改")
    public ApiResult<?> update(@RequestBody @Validated CfgNoticeDTO.UpdateDTO dto) {
        cfgNoticeService.update(dto);
        return success();
    }

    /**
     * 查看详情
     * @Auther will
     * @Date 2025/2/13 15:00
     * @param id
     * @return ApiResult<CfgNoticeDTO.ViewDTO>
     */
    @LogViewService
    @GetMapping("/view")
    public ApiResult<CfgNoticeDTO.ViewDTO> view(@Param("id") String id) {
        throw new ServiceException(ApiError.ERROR_UNAUTHORIZED_ACCESS);
//        CfgNoticeDTO.ViewDTO dto = cfgNoticeService.view(id);
//        return success(dto);
    }

    /**
     * 更新启禁用
     * @Auther will
     * @Date 2025/2/13 15:13
     * @param dto
     * @return ApiResult<?>
     */
    @PostMapping("/updateDisabled")
    @LogAction(value = LogActionEnum.UPDATE, desc = "通知配置表启禁用")
    public ApiResult<?> updateDisabled(@RequestBody @Validated CfgNoticeDTO.UpdateDisabledDTO dto) {
        cfgNoticeService.updateDisabled(dto);
        return success();
    }

    /**
     * 查看发送通知时间
     * @Auther will
     * @Date 2025/2/13 15:13
     * @param paramList
     * @return ApiResult<?>
     */
    @PostMapping("/viewSendTime")
    public ApiResult<List<LocalDateTime>> viewSendTime(@RequestBody @Validated ValidList<CfgNoticeDTO.NoticeTimeDTO> paramList) {
        List<LocalDateTime> list = cfgNoticeService.viewSendTime(paramList);
        return success(list);
    }
}

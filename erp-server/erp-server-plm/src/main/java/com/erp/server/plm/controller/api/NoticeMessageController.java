package com.erp.server.plm.controller.api;

import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.NoticeMessageDTO;
import com.erp.model.plm.entity.NoticeMessageEntity;
import com.erp.model.plm.enums.NoticeItemPeopleEnum;
import com.erp.server.plm.service.NoticeMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 产品系统通用设置
 *
 * @Classname NoticeMessageController

 * @Date 2022-11-07 11:00
 * @Created by yl
 */
@RestController
@LogSystemModule("系统通用设置")
@RequestMapping("notice/message")
public class NoticeMessageController extends BaseController {


    @Autowired
    private NoticeMessageService noticeMessageService;

    /**
     * 通知管理-分页展示
     *
     * @param
     * @return
     */
    @PostMapping("/paging")
    public ApiResult<PagingVO<NoticeMessageDTO>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<NoticeMessageDTO> pagingVO = noticeMessageService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 通知管理-新增通知-获取项目人员
     *
     * @param
     * @return
     */
    @GetMapping("/getItemPeople")
    public ApiResult<Object> getItemPeople() {
        List<Map<String,String>> list = NoticeItemPeopleEnum.getAll();
        return success(list);
    }


    /**
     * 通知管理-新增通知-新增通知
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "新增通知")
    @PostMapping("/add")
    public ApiResult<Object> add(@RequestBody @Validated NoticeMessageDTO dto) {
        Boolean flag = noticeMessageService.add(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 通知管理-新增通知-更改通知
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "更改通知")
    @PostMapping("/update")
    public ApiResult<Object> update(@RequestBody @Validated NoticeMessageDTO dto) {
        Boolean flag = noticeMessageService.updateNotice(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 通知管理-新增通知-设置通知状态
     *
     * @param
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "设置通知状态:id={id},状态值={state}(true=禁用,false=启用)")
    @PostMapping("/updateState")
    public ApiResult<Object> updateState(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean flag = noticeMessageService.updateState(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 通知管理-通知详情
     */
    @LogViewService
    @GetMapping("/view")
    public ApiResult<NoticeMessageEntity> view(@RequestParam(value = "id") String id) {
        NoticeMessageEntity view = noticeMessageService.view(id);
        return success(view);
    }

}

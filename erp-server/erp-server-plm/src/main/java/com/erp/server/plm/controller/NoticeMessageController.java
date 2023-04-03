package com.erp.server.plm.controller;

import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.NoticeMessageDTO;
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
 * @Description TODO
 * @Date 2022-11-07 11:00
 * @Created by yl
 */
@RestController
@RequestMapping("/notice/message")
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
    public ApiResult<PagingVO<List<NoticeMessageDTO>>> paging(@RequestBody @Validated PagingDTO<BaseSearchDTO> dto) {
        PagingVO<List<NoticeMessageDTO>> pagingVO = noticeMessageService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 通知管理-新增通知-获取项目人员
     *
     * @param
     * @return
     */
    @GetMapping("/getItemPeople")
    public ApiResult getItemPeople() {
        List<Map> list = NoticeItemPeopleEnum.getAll();
        return success(list);
    }


    /**
     * 通知管理-新增通知-新增通知
     *
     * @param
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated NoticeMessageDTO dto) {
        Boolean flag = noticeMessageService.add(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 通知管理-新增通知-更改通知
     *
     * @param
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated NoticeMessageDTO dto) {
        Boolean flag = noticeMessageService.updateNotice(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 通知管理-新增通知-设置通知状态
     *
     * @param
     * @return
     */
    @PostMapping("/updateState")
    public ApiResult updateState(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean flag = noticeMessageService.updateState(dto);
        return flag == true ? success() : failure();
    }

}

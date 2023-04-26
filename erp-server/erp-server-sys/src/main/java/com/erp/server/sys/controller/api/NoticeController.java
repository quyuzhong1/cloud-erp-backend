package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.server.sys.service.NoticeInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 通知管理
 *
 * @author lambda
 * @since 2023-04-20
 */
@RestController
@RequestMapping("/notice")
public class NoticeController extends BaseController {

    @Resource
    private NoticeInfoService noticeInfoService;


    /**
     * 新增通知
     *
     * @param
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated NoticeDTO.AddDTO dto) {
        Boolean result = noticeInfoService.add(dto);
        return result ? success() : failure();
    }

    /**
     * 修改通知
     *
     * @param
     * @return
     */
    @PostMapping("/update")
    public ApiResult update(@RequestBody @Validated NoticeDTO.UpdateDTO dto) {
        Boolean result = noticeInfoService.edit(dto);
        return result ? success() : failure();
    }

}

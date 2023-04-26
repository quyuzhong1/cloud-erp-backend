package com.erp.server.sys.controller.api;


import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.UpdateStateDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.CfgNodeMemberDTO;
import com.erp.model.sys.dto.NoticeDTO;
import com.erp.server.sys.service.CfgNodeMemberService;
import com.erp.server.sys.service.NoticeInfoService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

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

    @Resource
    private CfgNodeMemberService cfgNodeMemberService;


    /**
     * 根据节点的 key 获取到对应 节点成员
     *
     * @param nodeKey
     * @return
     * @author yl
     * @date 2023-04-26 18:22
     */
    @GetMapping("/listByNodeKey")
    public ApiResult<List<CfgNodeMemberDTO.ListDTO>> listByNodeKey(@RequestParam(value = "nodeKey") String nodeKey) {
        List<CfgNodeMemberDTO.ListDTO> cfgNodeList = cfgNodeMemberService.listByNodeKey(nodeKey);
        return success(cfgNodeList);
    }


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

    /**
     * 通知详情
     *
     * @param dto
     * @return
     * @author yl
     * @date 2023-04-26 16:17
     */
    @PostMapping("/view")
    public ApiResult<NoticeDTO.ViewDTO> view(@RequestBody @Validated BaseIdDTO dto) {
        NoticeDTO.ViewDTO  view= noticeInfoService.view(dto.getId());
        return success(view);
    }



     /**
     * 启用 禁用 通知节点
     *
     * @param dto
     * @return
     */
    @PostMapping("/updateStatus")
    public ApiResult updateStatus(@RequestBody @Validated UpdateStateDTO dto) {
        Boolean result = noticeInfoService.updateStatus(dto);
        return result == true ? success() : failure();
    }


}

package com.erp.server.sys.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.business.dto.base.BasePagingSearchDTO;
import com.common.business.dto.base.BaseSearchDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.vo.PagingVO;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SysPostDTO;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.model.wms.enums.NoticeUserEnum;
import com.erp.server.sys.service.SysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname SysPostController

 * @Date 2022-07-12 9:54
 * @Created by yl
 */
@RestController
@LogSystemModule("岗位管理")
@RequestMapping("post")
public class SysPostController extends BaseController {

    @Autowired
    private SysPostService sysPostService;

    @LogAction(value = LogActionEnum.INSERT, desc = "保存岗位")
    @RequestMapping("/save")
    public ApiResult save(@RequestBody @Validated SysPostDTO postEntity) {
        boolean flag = sysPostService.savePost(postEntity);
        return flag == true ? success() : failure();
    }

    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新岗位:id={id},岗位名={postName}")
    @RequestMapping("/update")
    public ApiResult update(@RequestBody @Validated SysPostDTO postEntity) {
        boolean flag = sysPostService.updatePost(postEntity);
        return flag == true ? success() : failure();
    }

    @LogAction(value = LogActionEnum.DELETE, desc = "删除岗位")
    @RequestMapping("/remove")
    public ApiResult remove(@RequestBody List<String> ids) {
        boolean flag = sysPostService.removePostByIds(ids);
        return flag == true ? success() : failure();
    }

    @RequestMapping("/list")
    public ApiResult list(@RequestBody BaseSearchDTO dto) {
        List<SysPostEntity> list = sysPostService.findPost(dto);
        return success(list);
    }

    @RequestMapping("/listByCfgThirdNotice")
    public ApiResult listByCfgThirdNotice(@RequestBody BaseSearchDTO dto) {
        List<SysPostEntity> list = sysPostService.findPost(dto);
        list = list.stream()
                .filter(e -> !(e.getPostName().equals(NoticeUserEnum.CREATE_USER.getName()) || e.getPostName().equals(NoticeUserEnum.APPROVE_USER.getName()) || e.getPostName().equals(NoticeUserEnum.HANDLE_USER.getName())))
                .collect(Collectors.toList());
        return success(list);
    }

    @RequestMapping("/paging")
    public ApiResult paging(@RequestBody PagingDTO<BasePagingSearchDTO> dto) {
        PagingVO vo = sysPostService.paging(dto);
        return success(vo);
    }

    @GetMapping("/listByRequisitionSetting")
    public ApiResult listByRequisitionSetting() {
        List<SysPostEntity> list = sysPostService.listByRequisitionSetting();
        return success(list);
    }

    @GetMapping("/listByRequisitionChangeSetting")
    public ApiResult listByRequisitionChangeSetting() {
        List<SysPostEntity> list = sysPostService.listByRequisitionChangeSetting();
        return success(list);
    }

    @PostMapping("/listByNameList")
    public ApiResult listByNameList(@RequestBody List<String> nameList) {
        List<SysPostEntity> list = sysPostService.listByNameList(nameList);
        return success(list);
    }
}

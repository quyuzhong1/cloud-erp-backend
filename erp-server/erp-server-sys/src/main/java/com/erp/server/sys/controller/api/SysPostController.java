package com.erp.server.sys.controller.api;


import com.erp.common.controller.BaseController;
import com.erp.common.dto.base.ApiResult;
import com.erp.common.dto.base.BasePagingSearchDTO;
import com.erp.common.dto.base.BaseSearchDTO;
import com.erp.common.dto.base.PagingDTO;
import com.erp.common.vo.PagingVO;
import com.erp.model.sys.dto.SysPostDTO;
import com.erp.model.sys.entity.SysPostEntity;
import com.erp.server.sys.service.SysPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Classname SysPostController
 * @Description TODO
 * @Date 2022-07-12 9:54
 * @Created by yl
 */
@RestController
@RequestMapping("sys/post")
public class SysPostController extends BaseController {

    @Autowired
    private SysPostService sysPostService;

    @RequestMapping("/save")
    public ApiResult save(@RequestBody @Validated SysPostDTO postEntity) {
        boolean flag = sysPostService.savePost(postEntity);
        return flag == true ? success() : failure();
    }

    @RequestMapping("/update")
    public ApiResult update(@RequestBody @Validated SysPostDTO postEntity) {
        boolean flag = sysPostService.updatePost(postEntity);
        return flag == true ? success() : failure();
    }

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

    @RequestMapping("/paging")
    public ApiResult paging(@RequestBody PagingDTO<BasePagingSearchDTO> dto) {
        PagingVO vo = sysPostService.paging(dto);
        return success(vo);
    }
}

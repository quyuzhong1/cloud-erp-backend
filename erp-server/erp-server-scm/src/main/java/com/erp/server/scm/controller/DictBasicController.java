package com.erp.server.scm.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.server.scm.service.DictBasicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典管理
 *
 * @author Lambda
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/dict")
public class DictBasicController extends BaseController {


    @Resource
    private DictBasicService dictBasicService;


    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdateBatch")
    public ApiResult saveOrUpdate(@RequestBody @Validated List<DictBasicDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return result == true ? success() : failure();
    }


    /**
     * 获取对应字典数据
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult list(@RequestParam("key") String key) {
        List<DictBasicDTO> list = dictBasicService.getByKey(key);
        return success(list);
    }

}

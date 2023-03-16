package com.erp.server.scm.controller;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.DictBasicDTO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 字典管理
 * @author Lambda
 * @since 2023-03-16
 */
@RestController
@RequestMapping("/dict")
public class DictBasicController extends BaseController {





    /**
     * 保存或者修改字典信息
     *
     * @param dto
     * @return
     */
    @PostMapping("/saveOrUpdate")
    public ApiResult saveOrUpdate(@RequestBody @Validated DictBasicDTO dto) {
        return success();
    }


    /**
     * 获取对应字典数据
     * @return
     */
    @GetMapping("/lsit")
    public ApiResult list(@RequestParam("key") String key) {
        return success();
    }

}

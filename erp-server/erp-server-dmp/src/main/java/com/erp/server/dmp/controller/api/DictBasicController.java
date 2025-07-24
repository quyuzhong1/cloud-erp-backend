package com.erp.server.dmp.controller.api;


import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.dmp.dto.DictBasicDTO;
import com.erp.server.dmp.service.DictBasicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典表
 *
 * @author will
 * @since 2023-05-08
 */
@RestController
@RequestMapping("/dict")
@LogSystemModule("字典表")
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
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "保存或者修改字典信息")
    public ApiResult saveOrUpdate(@RequestBody @Validated List<DictBasicDTO.AddOrUpdateDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return result == true ? success() : failure();
    }

    /**
     * 获取字典数据 根据属性
     * @param type
     * @return
     */
    @GetMapping("/list")
    public ApiResult list(@RequestParam(value = "type") String type) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByKey(type);
        return  success(list);
    }


}

package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.server.sys.service.DictBasicService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 字典管理
 * @author lambda
 * @since 2023-04-26
 */
@RestController
@RequestMapping("/dictBasic")
public class DictBasicController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 添加字典
     * @param list
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated List<DictBasicDTO.AddOrUpdateDTO> list) {
        Boolean result = dictBasicService.addOrUpdate(list);
        return result ? success() : failure();
    }

    /**
     * 获取字典数据 根据属性
     * type=itemPeople (项目人员)
     * type=otherPeople(其它人员)
     * @param type
     * @return
     */
    @GetMapping("/list")
    public ApiResult list(@RequestParam(value = "type") String type) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.listByType(type);
        return  success(list);
    }


}

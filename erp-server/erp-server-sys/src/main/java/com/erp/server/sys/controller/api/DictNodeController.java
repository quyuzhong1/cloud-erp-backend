package com.erp.server.sys.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictNodeDTO;
import com.erp.server.sys.service.DictNodeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 通知管理
 * @author lambda
 * @since 2023-04-26
 */
@RestController
@RequestMapping("/dictNode")
public class DictNodeController extends BaseController {

    @Resource
    private DictNodeService dictNodeService;


    /**
     * 添加节点
     * @param dto
     * @return
     */
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated DictNodeDTO.AddDTO dto) {
        Boolean addResult = dictNodeService.add(dto);
        return addResult ? success() : failure();
    }


    /**
     * 通知节点列表
     * businessType=qcInfo (质检单)
     * @param module
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictNodeDTO.ViewDTO>> add(@RequestParam(value = "module") String module) {
        List<DictNodeDTO.ViewDTO> list = dictNodeService.listByModule(module);
        return  success(list);
    }

}

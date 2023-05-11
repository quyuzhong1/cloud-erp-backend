package com.erp.server.oms.controller.api;


import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.server.oms.service.DictBasicService;
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
    public ApiResult saveOrUpdate(@RequestBody @Validated List<DictBasicDTO.AddOrUpdateDTO> dto) {
        Boolean result = dictBasicService.saveOrUpdateDict(dto);
        return result == true ? success() : failure();
    }


    /**
     * 获取对应字典数据
     *  customerCompanyCategory  公司客户类别
     * settleMode 客户支付方式
     *
     * 最后取value值
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictBasicDTO.ViewDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByKey(key);
        return success(list);
    }
}

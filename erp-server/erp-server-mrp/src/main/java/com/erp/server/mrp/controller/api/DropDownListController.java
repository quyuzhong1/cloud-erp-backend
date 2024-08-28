package com.erp.server.mrp.controller.api;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.BillApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.mrp.dto.DictBasicDTO;
import com.erp.server.mrp.service.DictBasicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 下拉
 */
@RestController
@RequestMapping("/drop/down")
public class DropDownListController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;


    /**
     * 查询字典下拉
     * amazonPlatform 亚马逊平台
     * overseasPlatform 海外平台
     * internalPlatform 国内平台
     * b2bPlatform B2B平台
     * @return
     */
    @GetMapping("/dict/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByKey(key);
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }

    /**
     * 单据状态下拉
     *
     * @return
     */
    @GetMapping("/billStatus/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> listBillStatusDropDown() {
        List<BaseDropDownDTO.CommonDTO> result = Arrays.stream(BillApproveStatusEnum.values())
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getStatus(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }


}


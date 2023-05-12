package com.erp.server.oms.controller.api;


import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.DictBasicDTO;
import com.erp.server.oms.service.DictBasicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
     * 获取对应字典数据
     *  customerCompanyCategory  公司客户类别
     *  settleMode 客户结算方式
     *  collectionTerms 收款条件
     *  invoiceType 发票类型
     *
     * @return
     */
    @GetMapping("/dict/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> list(@RequestParam("key") String key) {
        List<DictBasicDTO.ViewDTO> list = dictBasicService.getByKey(key);
        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getValue(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }







}

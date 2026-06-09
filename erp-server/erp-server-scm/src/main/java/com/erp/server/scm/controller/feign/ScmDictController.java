package com.erp.server.scm.controller.feign;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.common.business.dto.DmpSyncMqDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.model.scm.entity.DictBasicEntity;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.server.scm.service.DictBasicService;
import com.erp.server.scm.service.SupplierService;
import com.erp.server.scm.service.SyncTaskService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @description: 字典数据
 * @author Will
 * @date: 2023/10/19 11:05
 */
@RestController
@RequestMapping("feign/dict")
public class ScmDictController  extends BaseController {

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private SupplierService supplierService;

    /**
     * 根据key查询字典数据
     * @author Will
     * @date: 2024/1/23 16:46
     * @param key
     * @return List<DictBasicDTO>
     */
    @PostMapping("/listDictByKey")
    public List<DictBasicEntity> listDictByKey(@RequestBody String key) {
        List<DictBasicEntity> list =  dictBasicService.getByKey(key);
        return list;
    }

    /**
     * 根据ids查询
     * @author Will
     * @date: 2024/1/25 10:21
     * @param idList
     * @return List<DictBasicDTO>
     */
    @PostMapping("/listDictByIdList")
    public List<DictBasicEntity> listDictByIdList(@RequestBody List<String> idList) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.EMPTY_LIST;
        }
        List<DictBasicEntity> list =  dictBasicService.listByIds(idList);
        return list;
    }

    /**
     * 所有供应商下拉列表
     *
     * @return
     */
    @PostMapping("/listALLSupplierDropDown")
    public ApiResult<List<BaseDropDownDTO.RemarkDTO>> listALLSupplierDropDown() {
        List<SupplierEntity> mapList = supplierService.list();
        if (CollectionUtils.isEmpty(mapList)) {
            return success(new ArrayList<>());
        }
        List<BaseDropDownDTO.RemarkDTO> result = mapList.stream()
                .map(x -> new BaseDropDownDTO.RemarkDTO(x.getId(), x.getName(),x.getPaymentCondition(),x.getDisabled()))
                .collect(Collectors.toList());
        return success(result);
    }
}

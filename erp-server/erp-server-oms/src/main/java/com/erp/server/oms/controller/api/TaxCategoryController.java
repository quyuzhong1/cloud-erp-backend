package com.erp.server.oms.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.oms.entity.TaxCategoryEntity;
import com.erp.server.oms.service.TaxCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 税种基础数据Controller
 *
 * @author system
 * @date 2025/01/XX
 */
@Slf4j
@RestController
@RequestMapping("/taxCategory")
public class TaxCategoryController extends BaseController {

    @Resource
    private TaxCategoryService taxCategoryService;

    /**
     * 获取税种下拉列表
     * 用于页面下拉选择框，返回税种ID和税种名称
     *
     * @return 税种下拉列表
     */
    @GetMapping("/dropDownList")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> getDropDownList() {
        List<TaxCategoryEntity> list = taxCategoryService.list(
                new LambdaQueryWrapper<TaxCategoryEntity>()
                        .eq(TaxCategoryEntity::getIsDeleted, false)
                        .eq(TaxCategoryEntity::getDisabled, false)
                        .orderByAsc(TaxCategoryEntity::getCategoryId)
        );

        List<BaseDropDownDTO.CommonDTO> result = list.stream()
                .map(entity -> new BaseDropDownDTO.CommonDTO(
                        entity.getCategoryId(),  // code: 税种ID
                        entity.getDescricao()     // value: 税种名称
                ))
                .collect(Collectors.toList());

        return success(result);
    }

    /**
     * 获取所有税种列表（包含禁用和已删除的）
     * 用于管理页面
     *
     * @return 税种列表
     */
    @GetMapping("/list")
    public ApiResult<List<TaxCategoryEntity>> getAllList() {
        List<TaxCategoryEntity> list = taxCategoryService.list(
                new LambdaQueryWrapper<TaxCategoryEntity>()
                        .eq(TaxCategoryEntity::getIsDeleted, false)
                        .orderByAsc(TaxCategoryEntity::getCategoryId)
        );
        return success(list);
    }

    /**
     * 初始化税种列表
     * 从第三方系统获取税种列表并初始化到cfg_tax_category表
     * 复用定时任务的逻辑
     *
     * @author: system
     * @date: 2025/01/XX
     * @return: 初始化结果信息
     */
    @PostMapping("/initTaxCategoryList")
    public ApiResult<Map<String, Object>> initTaxCategoryList() {
        Map<String, Object> result = taxCategoryService.initTaxCategoryList();
        return success(result);
    }
}

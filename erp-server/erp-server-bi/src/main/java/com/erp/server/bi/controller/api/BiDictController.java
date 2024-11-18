package com.erp.server.bi.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.bi.dto.DictDTO;
import com.erp.model.bi.entity.BiDictEntity;
import com.erp.server.bi.service.BiDictService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * bi系统字典表(BiDict)表控制层
 *
 * @author yl
 * @since 2022-12-08 14:24:02
 */
@RestController
@LogSystemModule("bi系统字典")
@RequestMapping("dict")
public class BiDictController extends BaseController {
    /**
     * 服务对象
     */
    @Resource
    private BiDictService biDictService;


    /**
     * 通过主键查询单条数据
     *
     * @param id 主键
     * @return 单条数据
     */
    @LogViewService
    @GetMapping("/get")
    public ApiResult<BiDictEntity> queryById(@PathVariable("id") String id) {
        return success(this.biDictService.queryById(id));
    }

    /**
     * 新增数据
     *
     * @param biDict 实体
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "bi系统字典新增数据")
    @PostMapping("/add")
    public ApiResult<Object> add(@RequestBody BiDictEntity biDict) {
        boolean flag = this.biDictService.insert(biDict);
        return flag ? success() : failure();
    }

    /**
     * 编辑数据
     *
     * @param biDict 实体
     * @return 编辑结果
     */
    @LogAction(value = LogActionEnum.UPDATE, desc = "bi系统字典编辑数据")
    @PostMapping("/update")
    public ApiResult<Object> edit(@RequestBody BiDictEntity biDict) {
        boolean flag = this.biDictService.update(biDict);
        return flag ? success() : failure();
    }

    /**
     * 删除数据
     *
     * @param
     *
     * @return 删除是否成功
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "bi系统字典删除数据")
    @PostMapping("/delete")
    public ApiResult<Object> deleteById(@RequestBody @Validated BaseIdDTO dto) {
        boolean flag = this.biDictService.deleteById(dto.getId());
        return flag ? success() : failure();
    }


    /**
     * 获取到专题的分类
     *
     * @param
     * @return 删除是否成功
     */
    @GetMapping("/list")
    public ApiResult<List<Map<String, Object>>> list(String type) {
        List<Map<String, Object>> list = this.biDictService.listByType(type);
        return success(list);
    }
    /**
     * 根据类型获取全量字典字段
     *
     * @param
     * @return 删除是否成功
     */
    @GetMapping("/listByType")
    public ApiResult<List<BiDictEntity>> listByType(String type) {
        List<BiDictEntity> list = this.biDictService.listEntityByType(type);
        return success(list);
    }
    /**
     * 根据分类查询值和编码
     *
     * @param type
     * @return ApiResult
     * @author Will
     * @date: 2023/9/18 16:43
     */
    @GetMapping("/listValue")
    public ApiResult<List<Map<String, Object>>> listValue(String type) {
        List<Map<String, Object>> list = this.biDictService.listValueByType(type);
        return success(list);
    }


    /**
     * 批量新增字典
     *
     * @param dictEntities 实体
     * @return 新增结果
     */
    @LogAction(value = LogActionEnum.CUSTOM_BATCH_INSERT, desc = "bi系统字典批量新增:字典名称={name}")
    @PostMapping("/batchAdd")
    public ApiResult<Object> batchAdd(@RequestBody @Validated List<DictDTO> dictEntities) {
        boolean flag = this.biDictService.batchAdd(dictEntities);
        return flag ? success() : failure();
    }
}


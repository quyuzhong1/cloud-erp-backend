package com.erp.server.plm.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.plm.dto.BasicLabelDTO;
import com.erp.model.plm.vo.LabelBasicVO;
import com.erp.model.plm.vo.LabelLevelTreeVO;
import com.erp.server.plm.service.BasicLabelService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 基础标签表
 *
 * @author Lambda
 * @since 2023-09-13
 */
@Slf4j
@RestController
@LogSystemModule("基础标签")
@RequestMapping("/basicLabel")
public class BasicLabelController extends BaseController {

    @Resource
    private BasicLabelService basicLabelService;

    /**
     * 标签列表
     *
     * @param dto
     * @return com.common.core.vo.ApiResult
     * @author zdy
     * @date 2023-09-16 14:34
     */
    @PostMapping("/list")
    public ApiResult<List<LabelBasicVO>> list(@RequestBody BasicLabelDTO.SearchDTO dto) {
        List<LabelBasicVO> list = basicLabelService.listByCondition(dto);
        return success(list);
    }

    /**
     * 标签列表下拉框
     *
     * @return com.common.core.vo.ApiResult
     * @author zdy
     * @date 2023-09-16 14:34
     */
    @PostMapping("/tree")
    public ApiResult<List<LabelLevelTreeVO>> tree() {
        List<LabelLevelTreeVO> list = basicLabelService.listByTree();
        return success(list);
    }
    /**
     * 新增基础标签
     *
     * @param dto
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增基础标签")
    public ApiResult<String> add(@RequestBody @Validated BasicLabelDTO.AddDTO dto) {
        return success(basicLabelService.add(dto));
    }

    /**
     * 批量新增基础标签
     *
     * @param dtos
     * @return ApiResult<String>
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/batchAdd")
    @LogAction(value = LogActionEnum.INSERT, desc = "批量新增基础标签")
    public ApiResult<Boolean> batchAdd(@RequestBody @Validated List<BasicLabelDTO.AddDTO> dtos) {
        return success(basicLabelService.batchAdd(dtos));
    }

    /**
     * 更新基础标签
     *
     * @param dto
     * @return ApiResult
     * @author Lambda
     * @date: 2023-09-13
     */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "更新基础标签")
    public ApiResult update(@RequestBody @Validated BasicLabelDTO.UpdateDTO dto) {
        basicLabelService.update(dto);
        return success();
    }

    /**
     * 删除基础标签
     */
    @PostMapping("/remove")
    @LogAction(value = LogActionEnum.DELETE, desc = "删除基础标签")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:basicLabel:remove",
            serviceClass = BasicLabelService.class,
            keyIdName = "id")
    public ApiResult delete(@RequestBody @Validated BasicLabelDTO.DeleteDTO dto) {
        basicLabelService.removeBasicLabelById(dto.getId());
        return success();
    }

}

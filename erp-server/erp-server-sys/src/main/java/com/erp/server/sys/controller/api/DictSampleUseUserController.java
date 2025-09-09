package com.erp.server.sys.controller.api;

import com.common.business.dto.base.BaseIdDTO;
import com.common.business.validator.ValidList;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.sys.dto.SampleUseUserDTO;
import com.erp.server.sys.service.DictSampleUseUserService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 样品公司外部使用人-字典表
 *
 * @author Lambda
 * @since 2025-01-27
 */
@RestController
@LogSystemModule("样品公司外部使用人")
@RequestMapping("sampleUseUser")
public class DictSampleUseUserController extends BaseController {

    @Resource
    private DictSampleUseUserService sampleUseUserService;

    @LogAction(value = LogActionEnum.INSERT, desc = "新增或更新示例用户")
    @PostMapping("/saveOrUpdate")
    public ApiResult add(@RequestBody @Valid ValidList<SampleUseUserDTO.AddOrUpdateDTO> userList) {
        Boolean result = sampleUseUserService.saveOrUpdateBatchUser(userList);
        return result == true ? success() : failure();
    }

    /**
     * 删除示例用户
     *
     * @param ids
     * @return com.common.core.controller.vo.ApiResult
     * @author Lambda
     * @date 2025-01-27 16:29
     */
    @LogAction(value = LogActionEnum.DELETE, desc = "删除示例用户")
    @PostMapping("/delete")
    public ApiResult delete(@RequestBody @Validated List<String> ids) {
        Boolean result = sampleUseUserService.removeByIds(ids);
        return result == true ? success() : failure();
    }

    /**
     * 获取示例用户列表
     *
     * @return com.common.core.controller.vo.ApiResult
     * @author Lambda
     * @date 2025-01-27 16:29
     */
    @GetMapping("/list")
    public ApiResult<List<SampleUseUserDTO.ViewDTO>> list() {
        List<SampleUseUserDTO.ViewDTO> list = sampleUseUserService.getList();
        return success(list);
    }

    /**
     * 根据条件模糊查询示例用户列表
     *
     * @param queryDTO 查询条件
     * @return com.common.core.controller.vo.ApiResult
     * @author Lambda
     * @date 2025-01-27 16:29
     */
    @PostMapping("/listByCondition")
    public ApiResult<List<SampleUseUserDTO.ViewDTO>> listByCondition(@RequestBody SampleUseUserDTO.QueryDTO queryDTO) {
        List<SampleUseUserDTO.ViewDTO> list = sampleUseUserService.getListByCondition(queryDTO);
        return success(list);
    }

    /**
     * 根据名称列表批量查询示例用户
     *
     * @param nameList 名称列表
     * @return com.common.core.controller.vo.ApiResult
     * @author Lambda
     * @date 2025-01-27 16:29
     */
    @PostMapping("/feign/getListByNameList")
    public ApiResult<List<SampleUseUserDTO.ViewDTO>> getListByNameList(@RequestBody List<String> nameList) {
        List<SampleUseUserDTO.ViewDTO> list = sampleUseUserService.getListByNameList(nameList);
        return success(list);
    }


}

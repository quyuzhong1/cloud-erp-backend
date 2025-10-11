package com.erp.server.sys.controller.feign;

import com.common.business.dto.base.BaseIdDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.SampleUseUserDTO;
import com.erp.server.sys.service.DictSampleUseUserService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author Lambda
 * @Classname SampleUseUserFeignController
 * @Date 2025-01-27 18:18
 * @Created by Lambda
 */
@RestController
@RequestMapping("feign/sampleUseUser")
public class SampleUseUserFeignController extends BaseController {

    @Resource
    private DictSampleUseUserService sampleUseUserService;

    @PostMapping("/getByIds")
    public List<BaseIdDTO> getByIds(@RequestBody List<String> ids) {
        List<BaseIdDTO> list = sampleUseUserService.getByIds(ids);
        return list;
    }
    /**
     * 根据名称列表批量查询示例用户
     *
     * @param nameList 名称列表
     * @return com.common.core.controller.vo.ApiResult
     * @author Lambda
     * @date 2025-01-27 16:29
     */
    @PostMapping("/getListByNameList")
    public ApiResult<List<SampleUseUserDTO.ViewDTO>> getListByNameList(@RequestBody List<String> nameList) {
        List<SampleUseUserDTO.ViewDTO> list = sampleUseUserService.getListByNameList(nameList);
        return success(list);
    }

    /**
     * 获取示例用户列表
     *
     * @return com.common.core.controller.vo.ApiResult
     * @author Lambda
     * @date 2025-01-27 16:29
     */
    @GetMapping("/list")
    public List<SampleUseUserDTO.ViewDTO> list() {
        List<SampleUseUserDTO.ViewDTO> list = sampleUseUserService.getList();
        return list;
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
    public List<SampleUseUserDTO.ViewDTO> listByCondition(@RequestBody SampleUseUserDTO.QueryDTO queryDTO) {
        List<SampleUseUserDTO.ViewDTO> list = sampleUseUserService.getListByCondition(queryDTO);
        return list;
    }

}

package com.erp.server.scm.controller.feign;

import com.common.business.dto.base.BaseDropDownDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.scm.dto.DictBasicDTO;
import com.erp.server.scm.service.DictBasicService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * SCM 服务 Feign 控制器
 * @author wuhaotian
 * @since 2025-09-24
 */
@RestController
@RequestMapping("/feign")
public class ScmFeignController extends BaseController {

    @Resource
    private DictBasicService dictBasicService;

    /**
     * 审批状态下拉列表
     */
    @GetMapping("/dropDown/approveStatus/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> approveStatusList() {
        List<BaseDropDownDTO.CommonDTO> result = Arrays.stream(ApproveStatusEnum.values())
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getStatus(), x.getName()))
                .collect(Collectors.toList());
        return success(result);
    }
}

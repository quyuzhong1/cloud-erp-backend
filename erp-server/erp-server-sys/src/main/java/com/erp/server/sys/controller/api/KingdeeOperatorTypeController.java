package com.erp.server.sys.controller.api;


import com.erp.model.sys.entity.KingdeeDepartmentEntity;
import com.erp.model.sys.entity.KingdeeOperatorTypeEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.sys.service.KingdeeOperatorTypeService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.sys.dto.KingdeeOperatorTypeDTO;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 金蝶架构管理-业务员类型
 *
 * @author Lambda
 * @since 2024-03-11
 */
@Slf4j
@RestController
@LogSystemModule("业务员类型")
@RequestMapping("/kingdeeOperatorType")
public class KingdeeOperatorTypeController extends BaseController {

    @Resource
    private KingdeeOperatorTypeService kingdeeOperatorTypeService;



    /**
     * 下拉
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<BaseDropDownDTO.CommonDTO>> list(){
        List<KingdeeOperatorTypeEntity> list=kingdeeOperatorTypeService.list();
        List<BaseDropDownDTO.CommonDTO> result = list.stream().filter(o-> StringUtils.isNotBlank(o.getCode()))
                .map(x -> new BaseDropDownDTO.CommonDTO(x.getCode(), x.getName()))
                .collect(Collectors.toList());
        return success(result);

    }


    /**
    * 新增
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "新增")
    public ApiResult add(@RequestBody @Validated KingdeeOperatorTypeDTO.AddDTO dto) {
        Boolean result = kingdeeOperatorTypeService.add(dto);
        return result ? success() : failure();
    }

    /**
    * 修改
    * @author Lambda
    * @date:  2024-03-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改")
    public ApiResult update(@RequestBody @Validated KingdeeOperatorTypeDTO.UpdateDTO dto) {
        kingdeeOperatorTypeService.update(dto);
        return success();
    }



}

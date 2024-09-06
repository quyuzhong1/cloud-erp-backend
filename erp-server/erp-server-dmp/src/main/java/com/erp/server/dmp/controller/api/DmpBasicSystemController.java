package com.erp.server.dmp.controller.api;


import com.erp.model.plm.dto.DictControllerDTO;
import com.erp.model.plm.enums.BasicDictTypeEnum;
import lombok.extern.slf4j.Slf4j;
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
import com.erp.server.dmp.service.DmpBasicSystemService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpBasicSystemDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * 外部系统
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("外部系统")
@RequestMapping("/dmpBasicSystem")
public class DmpBasicSystemController extends BaseController {

    @Resource
    private DmpBasicSystemService dmpBasicSystemService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "外部系统新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpBasicSystemDTO.AddDTO dto) {
        return success(dmpBasicSystemService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "外部系统修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpBasicSystem:update",
        serviceClass = DmpBasicSystemService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpBasicSystemDTO.UpdateDTO dto) {
        dmpBasicSystemService.update(dto);
        return success();
    }

    /**
     * 系统下拉
     * @Author Luo_WG
     * @Date 2024/9/5 18:42
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.plm.dto.DictControllerDTO.DictDropDownDTO>>
     **/
    @GetMapping("/listDmpBasicSystem")
    public ApiResult<List<DictControllerDTO.DictDropDownDTO>> listDmpBasicSystem(){
        List<DictControllerDTO.DictDropDownDTO> result = dmpBasicSystemService.listDmpBasicSystem();
        return success(result);
    }


}

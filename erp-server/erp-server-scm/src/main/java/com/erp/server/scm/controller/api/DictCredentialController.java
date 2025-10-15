package com.erp.server.scm.controller.api;


import com.erp.model.scm.dto.SupplierCredentialDTO;
import com.erp.model.scm.entity.DictCredentialEntity;
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
import com.erp.server.scm.service.DictCredentialService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.scm.dto.DictCredentialDTO;

import java.util.List;

/**
 * 供应商资质字典表
 *
 * @author jack
 * @since 2025-10-15
 */
@Slf4j
@RestController
@LogSystemModule("供应商资质字典表")
@RequestMapping("/dictCredential")
public class DictCredentialController extends BaseController {

    @Resource
    private DictCredentialService dictCredentialService;

    /**
    * 新增
    * @author jack
    * @date:  2025-10-15
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "供应商资质字典表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DictCredentialDTO.AddDTO dto) {
        return success(dictCredentialService.add(dto));
    }

    /**
     *
     * @return
     */
    @GetMapping("/list")
    public ApiResult<List<DictCredentialEntity>> list() {
        List<DictCredentialEntity> list = dictCredentialService.lambdaQuery()
                .eq(DictCredentialEntity::getDisabled, false)
                .orderByAsc(DictCredentialEntity::getSort)
                .orderByDesc(DictCredentialEntity::getCreateTime)
                .list();
        return success(list);
    }

}

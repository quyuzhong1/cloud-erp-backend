package com.erp.server.tms.controller.api;


import com.erp.model.sys.dto.DepartmentDTO;
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
import com.erp.server.tms.service.CfgLogisticsCostImportFieldService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.CfgLogisticsCostImportFieldDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.tms.entity.CfgLogisticsCostImportFieldEntity;

/**
 * 费用项配置字段基础表
 *
 * @author jack
 * @since 2026-01-20
 */
@Slf4j
@RestController
@LogSystemModule("费用项配置字段基础表")
@RequestMapping("/cfgLogisticsCostImportField")
public class CfgLogisticsCostImportFieldController extends BaseController {

    @Resource
    private CfgLogisticsCostImportFieldService cfgLogisticsCostImportFieldService;
    /**
    *
    * @return
    */
    @GetMapping("/listByBusinessType")
    public ApiResult<List<CfgLogisticsCostImportFieldDTO.ListDTO>> listByBusinessType(@RequestParam(value = "businessType",required = true) String businessType) {
       return success(cfgLogisticsCostImportFieldService.listByBusinessType(businessType));
    }


    /**
     * 分页列表
     */
    @RequestMapping("/tree")
    public ApiResult tree(@RequestParam(value = "businessType",required = true) String businessType) {
        List<CfgLogisticsCostImportFieldDTO.TreeDTO> treeVO=cfgLogisticsCostImportFieldService.tree(businessType);
        return success(treeVO);
    }


}

package com.erp.server.plm.controller.api;


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
import com.erp.server.plm.service.BasicProductBuService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.plm.dto.BasicProductBuDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.plm.entity.BasicProductBuEntity;

/**
 * 产品BU信息
 *
 * @author lrp
 * @since 2026-01-16
 */
@Slf4j
@RestController
@LogSystemModule("产品BU信息")
@RequestMapping("/basicProductBu")
public class BasicProductBuController extends BaseController {

    @Resource
    private BasicProductBuService basicProductBuService;

    /**
     * 新增或修改
     * @author lrp
     * @date:  2026-01-16
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/addOrUpdate")
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "产品BU信息修改")
    public ApiResult<?> addOrUpdate(@RequestBody @Validated List<BasicProductBuDTO.DropDownDTO> dto) {
        basicProductBuService.addOrUpdate(dto);
        return success();
    }

    /**
     * 删除
     * @author lrp
     * @date:  2026-01-16
     * @param dto
     * @return ApiResult
     */
    @PostMapping("/delete")
    @LogAction(value = LogActionEnum.DELETE, desc = "产品BU信息删除")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "plm:basicProductBu:delete",
            serviceClass = BasicProductBuService.class,
            keyIdName = "id")
    public ApiResult<?> delete(@RequestBody @Validated BaseIdDTO dto) {
        basicProductBuService.delete(dto.getId());
        return success();
    }


    /**
    * 列表
    * @return
    */
    @GetMapping("/list")
    public ApiResult<List<BasicProductBuDTO.DropDownDTO>> list() {
       return success(basicProductBuService.dropDown());
    }

}

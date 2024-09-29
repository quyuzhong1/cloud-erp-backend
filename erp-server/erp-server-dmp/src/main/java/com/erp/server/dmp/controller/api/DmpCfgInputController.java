package com.erp.server.dmp.controller.api;


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
import com.erp.server.dmp.service.DmpCfgInputService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.dmp.dto.DmpCfgInputDTO;

import java.util.List;

/**
 * 输入信息
 *
 * @author shukai
 * @since 2024-06-11
 */
@Slf4j
@RestController
@LogSystemModule("输入信息")
@RequestMapping("/dmpCfgInput")
public class DmpCfgInputController extends BaseController {

    @Resource
    private DmpCfgInputService dmpCfgInputService;

    /**
    * 新增
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "输入信息新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated DmpCfgInputDTO.AddDTO dto) {
        return success(dmpCfgInputService.add(dto));
    }

    /**
    * 修改
    * @author shukai
    * @date:  2024-06-11
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "输入信息修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "dmp:dmpCfgInput:update",
        serviceClass = DmpCfgInputService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated DmpCfgInputDTO.UpdateDTO dto) {
        dmpCfgInputService.update(dto);
        return success();
    }

    /**
     * 查询系统单据
     * @Author Luo_WG
     * @Date 2024/9/5 19:28
     * @param id 系统id
     * @return com.common.core.controller.vo.ApiResult<?>
     **/
    @GetMapping("/listDmpCfgInput")
    public ApiResult<List<DmpCfgInputDTO.ListDmpCfgInputDTO>> listDmpCfgInput(@RequestParam("id") String id) {
        List<DmpCfgInputDTO.ListDmpCfgInputDTO> resultList = dmpCfgInputService.listDmpCfgInput(id);
        return success(resultList);
    }

    /**
     * 查询所有输入配置
     * @Author Luo_WG
     * @Date 2024/9/5 19:28
     * @return com.common.core.controller.vo.ApiResult<?>
     **/
    @GetMapping("/allDmpCfgInput")
    public ApiResult<List<DmpCfgInputDTO.ListDmpCfgInputDTO>> allDmpCfgInput() {
        List<DmpCfgInputDTO.ListDmpCfgInputDTO> resultList = dmpCfgInputService.allDmpCfgInput();
        return success(resultList);
    }


}

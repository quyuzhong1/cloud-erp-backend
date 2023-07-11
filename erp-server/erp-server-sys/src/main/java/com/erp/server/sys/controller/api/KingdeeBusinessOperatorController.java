package com.erp.server.sys.controller.api;


import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDropDownDTO;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.sys.dto.KingdeeBusinessOperatorDTO;
import com.erp.server.sys.service.KingdeeBusinessOperatorService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * 金蝶信息
 *
 * @author Lambda
 * @since 2023-07-07
 */
@RestController
@RequestMapping("/kingdeeBusinessOperator")
public class KingdeeBusinessOperatorController extends BaseController {

    @Resource
    private KingdeeBusinessOperatorService kingdeeBusinessOperatorService;


    /**
     * 导入
     */
    @PostMapping("/import")
    public ApiResult importExcel(@RequestParam(value = "excelFile") MultipartFile excelFile, HttpServletResponse response) {
        Boolean result = kingdeeBusinessOperatorService.importFile(excelFile, response);
        return result ? success() : failure();
    }


    /**
     * 业务员列表
     */
    @PostMapping("/list")
    public ApiResult<List<FindUserDTO>> list (@RequestBody KingdeeBusinessOperatorDTO.ListBusinessOperatorDTO dto) {
        List<FindUserDTO> list = kingdeeBusinessOperatorService.listInfo(dto);
        return success(list);
    }

}

package com.erp.server.tms.controller.api;


import com.common.business.vo.PagingVO;
import com.erp.model.tms.dto.LogisticsBillDTO;
import com.erp.model.tms.entity.LogisticsCarrierEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.enums.LogActionEnum;
import com.common.business.dto.base.*;
import org.springframework.web.bind.annotation.RestController;

import com.common.core.controller.BaseController;
import com.erp.server.tms.service.LogisticsCarrierService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.tms.dto.LogisticsCarrierDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 物流快递/海运/空运公司列表
 *
 * @author zdy
 * @since 2024-05-08
 */
@Slf4j
@RestController
@LogSystemModule("物流快递/海运/空运公司列表")
@RequestMapping("/logisticsCarrier")
public class LogisticsCarrierController extends BaseController {

    @Resource
    private LogisticsCarrierService logisticsCarrierService;

    /**
    * 新增
    * @author zdy
    * @date:  2024-05-08
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "物流快递/海运/空运公司列表新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated LogisticsCarrierDTO.AddDTO dto) {
        return success(logisticsCarrierService.add(dto));
    }

    /**
    * 修改
    * @author zdy
    * @date:  2024-05-08
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "物流快递/海运/空运公司列表修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "tms:logisticsCarrier:update",
        serviceClass = LogisticsCarrierService.class,
        keyIdName = "id")
    public ApiResult<Object> update(@RequestBody @Validated LogisticsCarrierDTO.UpdateDTO dto) {
        logisticsCarrierService.update(dto);
        return success();
    }

    /**
     * 导入
     * @author zdy
     * @date: 2023/8/17 14:06
     * @param excelFile
     * @param response
     * @return ApiResult
     */
    @PostMapping("/importFile")
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入配置")
    public ApiResult<Object>importFile(@RequestParam(value = "excelFile") MultipartFile excelFile,@RequestParam(value = "logisticsType") String logisticsType, HttpServletResponse response) {
        Boolean flag = logisticsCarrierService.importFile(excelFile,logisticsType,response);
        return flag == true ? success() : failure();
    }

    /**
     * 公司下拉值
     * @param searchDTO
     * @return
     */
    @PostMapping("/drop/down")
    public ApiResult<PagingVO<LogisticsCarrierDTO.PagingVO>> dropDown(@RequestBody @Validated PagingDTO<LogisticsCarrierDTO.SearchDTO> searchDTO){
        PagingVO<LogisticsCarrierDTO.PagingVO> listPagingVO = logisticsCarrierService.dropDown(searchDTO);
        return success(listPagingVO);
    }
}

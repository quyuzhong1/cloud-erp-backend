package com.erp.server.oms.controller.api;


import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.enums.ClientTypeEnum;
import com.common.core.utils.ExcelUtil;
import com.erp.model.wms.dto.SampleBorrowInfoDTO;
import com.erp.model.wms.entity.SampleBorrowInfoEntity;
import com.erp.server.oms.query.KolPartnerInfoQueryHandler;
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
import com.erp.server.oms.service.KolPartnerInfoService;
import com.common.core.controller.vo.ApiResult;
import com.common.business.vo.PagingVO;
import com.common.business.dto.base.*;
import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.enums.DataAttributeEnum;
import com.erp.model.oms.dto.AddressParseDTO;
import com.erp.model.oms.dto.KolPartnerInfoDTO;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;
import com.erp.model.oms.entity.KolPartnerInfoEntity;

/**
 * 企业达人库
 *
 * @author jack
 * @since 2025-12-02
 */
@Slf4j
@RestController
@LogSystemModule("企业达人库")
@RequestMapping("/kolPartnerInfo")
public class KolPartnerInfoController extends BaseController {

    @Resource
    private KolPartnerInfoService kolPartnerInfoService;

    /**
    * 新增
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<String>
    */
    @PostMapping("/add")
    @LogAction(value = LogActionEnum.INSERT, desc = "企业达人库新增")
    public ApiResult<BaseResultDTO.AddDTO> add(@RequestBody @Validated KolPartnerInfoDTO.AddDTO dto) {
        return success(kolPartnerInfoService.add(dto));
    }

    /**
    * 修改
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult
    */
    @PostMapping("/update")
    @LogAction(value = LogActionEnum.UPDATE, desc = "企业达人库修改")
        @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
        tableField = "create_user_id",
        menuCode = "oms:kolPartnerInfo:update",
        serviceClass = KolPartnerInfoService.class,
        keyIdName = "id")
    public ApiResult<?> update(@RequestBody @Validated KolPartnerInfoDTO.UpdateDTO dto) {
        kolPartnerInfoService.update(dto);
        return success();
    }

    /**
    * 获取状态统计
    * @return
    */
    @PostMapping("/tabList")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:paging",
            tableAlias = "kpi"
    )
    public ApiResult<List<KolPartnerInfoDTO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
       return success(kolPartnerInfoService.tabList(dto));
    }

    /**
    * 列表查询
    * @author jack
    * @date: 2025-12-02
    * @param dto
    * @return ApiResult<PagingVO<KolPartnerInfoDTO.ListDTO>>
    */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:paging",
            tableAlias = "kpi"
    )
    @WebAdvanceQuery(handler = KolPartnerInfoQueryHandler.class)
    public ApiResult<PagingVO<KolPartnerInfoDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<KolPartnerInfoDTO.PagingParamDTO> dto) {
        return success(kolPartnerInfoService.paging(dto));
    }


    /**
    * 删除
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @return ApiResult<List<BatchResultDTO>>
    */
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:delete",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.DELETE, desc = "企业达人库删除")
    public ApiResult<List<BatchResultDTO>> batchDelete(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
		List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
		List<KolPartnerInfoEntity> list = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, ids).list();
		Map<String, KolPartnerInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO deleteResult;
            try {
                deleteResult = kolPartnerInfoService.delete(id);
            }catch (Exception e){
                log.error("企业达人库删除失败",e);
                KolPartnerInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    deleteResult = BatchResultDTO.fail(id, id, "企业达人库不存在, 删除失败");
                    resultDTOS.add(deleteResult);
                    continue;
                }
                deleteResult = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(deleteResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }



    /**
     * 批量启用、禁用
     * @author jack
     * @date:  2025-08-20
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/disabled")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:enable",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "ids")
    @LogAction(value = LogActionEnum.UPDATE, desc = "企业达人库启用、禁用")
    public ApiResult<List<BatchResultDTO>> batchDisabled(@RequestBody @Validated KolPartnerInfoDTO.IdsDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(ids.size());
        List<KolPartnerInfoEntity> list = kolPartnerInfoService.lambdaQuery().in(KolPartnerInfoEntity::getId, ids).list();
        Map<String, KolPartnerInfoEntity> idEntityMap = list.stream().collect(Collectors.toMap(KolPartnerInfoEntity::getId, w -> w));
        for (String id : dto.getIds()) {
            BatchResultDTO result;
            try {
                result = kolPartnerInfoService.disabled(id,dto.getDisabled());
            }catch (Exception e){
                KolPartnerInfoEntity entity = idEntityMap.get(id);
                if (ObjectUtil.isEmpty(entity)) {
                    result = BatchResultDTO.fail(id, id, "企业达人库不存在, 操作失败");
                    resultDTOS.add(result);
                    continue;
                }
                result = BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage());
            }
            resultDTOS.add(result);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
    * 详情
    * @author jack
    * @date:  2025-12-02
    * @param id
    * @return ApiResult<KolPartnerInfoDTO.ViewDTO>>
    */
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:view",
            serviceClass = KolPartnerInfoService.class,
            keyIdName = "id")
    @LogViewService
    public ApiResult<KolPartnerInfoDTO.ViewDTO> view(@RequestParam("id") String id) {
        return success(kolPartnerInfoService.view(id));
    }

    /**
    * 导出Excel数据
    * @author jack
    * @date:  2025-12-02
    * @param dto
    * @param response
    * @return
    */
    @PostMapping("/export")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "oms:kolPartnerInfo:export",
            tableAlias = "kpi"
    )
    @LogAction(value = LogActionEnum.EXPORT, desc = "企业达人库导出Excel数据")
    @WebAdvanceQuery(handler = KolPartnerInfoQueryHandler.class)
    public  ApiResult<Object> exportList(@RequestBody @Validated KolPartnerInfoDTO.PagingParamDTO dto, HttpServletResponse response) {
        kolPartnerInfoService.exportList(dto, response);
        return success();
    }


    /**
     * 异步导入
     * @author jack
     * @date:  2025-12-02
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.IMPORT, desc = "导入企业达人库")
    @PostMapping("/import")
    public ApiResult importFile(@RequestBody BaseDTO.ImportDTO dto) {
        Boolean result = kolPartnerInfoService.importFile(dto);
        return result ? success() : failure();
    }

    /**
     * 下载模板
     * @author jack
     * @date:  2025-12-02
     * @param response
     * @return
     */
    @LogAction(value = LogActionEnum.EXPORT, desc = "企业达人库下载模板")
    @GetMapping("/downloadTemplate")
    public ApiResult downloadTemplate(HttpServletResponse response) {
        String standardPath = "excel/kolPartnerInfoTemplate.xlsx";
        String standardExcelName = "kolPartnerInfoTemplate.xlsx";
        ExcelUtil.downloadTemplate(standardPath, standardExcelName, response);
        return success();
    }



    /**
     * 达人下拉接口 (disabled = ture的需要置灰不能选择)
     * @author jack
     * @date:  2025-12-02
     * @param dto
     * @return
     */
    @PostMapping("/drop/down")
    public ApiResult<List<KolPartnerInfoDTO.DropDownDTO>> dropDown(@RequestBody KolPartnerInfoDTO.SelectDTO dto) {
        return success(kolPartnerInfoService.dropDown(dto));
    }

    /**
     * 达人地址模糊查询
     * @author jack
     * @date:  2025-12-02
     * @param dto
     * @return
     */
    @PostMapping("/partnerAddressList")
    public ApiResult<List<KolPartnerInfoDTO.PartnerAddressDTO>> partnerAddressList(@RequestBody @Validated KolPartnerInfoDTO.AddressSelectDTO dto) {
        return success(kolPartnerInfoService.partnerAddressList(dto));
    }

    /**
     * 地址解析
     */
    @PostMapping("/addressParse")
    public ApiResult<AddressParseDTO.ParseResultDTO> addressParse(@RequestBody @Validated AddressParseDTO.ParseRequestDTO dto) {
        return success(kolPartnerInfoService.addressParse(dto));
    }

    /**
     * 批量地址解析
     */
    @PostMapping("/batchAddressParse")
    public ApiResult<List<AddressParseDTO.BatchParseResultDTO>> batchAddressParse(@RequestBody @Validated List<AddressParseDTO.BatchParseRequestDTO> dtoList) {
        return success(kolPartnerInfoService.batchAddressParse(dtoList));
    }


}

package com.erp.server.srm.controller.api;


import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.srm.dto.PoReconciliationDetailDTO;
import com.erp.model.srm.entity.PoReconciliationDetailEntity;
import com.erp.server.srm.query.PoReconciliationDetailScmQueryHandler;
import com.erp.server.srm.service.PoReconciliationDetailScmService;
import com.erp.server.srm.service.PoReconciliationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 采购对账单明细【scm】
 *
 * @author will
 * @since 2024-01-19
 */
@Slf4j
@RestController
@LogSystemModule("采购对账单明细")
@RequestMapping("/poReconciliationDetail/scm")
public class PoReconciliationDetailScmController extends BaseController {

    @Resource
    private PoReconciliationDetailScmService poReconciliationDetailScmService;


    /**
     * 分页查询
     * @author Will
     * @date: 2024/1/20 11:31
     * @param dto
     * @return ApiResult<PagingVO<ListDTO>>
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliationDetail:scm:paging",
            tableAlias = "prd"
    )
    @WebAdvanceQuery(handler = PoReconciliationDetailScmQueryHandler.class)
    public ApiResult<PagingVO<PoReconciliationDetailDTO.ListDTO>> paging(@RequestBody @Validated PagingDTO<PoReconciliationDetailDTO.PagingParamDTO> dto) {
        return success(poReconciliationDetailScmService.paging(dto));
    }


    /**
     * 导出Excel
     * @author Will
     * @date: 2024/1/20 12:03
     * @param dto
     */
    @PostMapping("/export")
    @LogAction(value = LogActionEnum.EXPORT, desc = "采购对账单导出Excel数据")
    public ApiResult<Boolean> exportList(@RequestBody @Validated PoReconciliationDetailDTO.PagingParamDTO dto) {
        poReconciliationDetailScmService.exportList(dto);
        return success(true);
    }


    /**
     * 生成对账单
     * @author Will
     * @date: 2024/1/23 18:00
     * @param dto
     */
    @PostMapping("/generatePoReconciliation")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliationDetail:scm:generatePoReconciliation",
            tableAlias = "prd"
    )
    public ApiResult<Object> generatePoReconciliation(@RequestBody @Validated PoReconciliationDetailDTO.GeneratePoReconciliationDTO dto) {
        Boolean flag = poReconciliationDetailScmService.generatePoReconciliation(dto);
        return flag == true ? success() : failure();
    }


    /**
     * 手动生成
     * @author will
     * @date 2025/6/11 15:48
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping("/manualGenerate")
    @LogAction(value = LogActionEnum.INSERT, desc = "手动生成")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliationDetail:scm:manualGenerate",
            serviceClass = PoReconciliationService.class,
            keyIdName = "ids")
    public ApiResult<Object> manualGenerate(@RequestBody @Validated PoReconciliationDetailDTO.CodeDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getCodeList().size());
        for (String code : dto.getCodeList()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationDetailScmService.manualGenerate(new PoReconciliationDetailDTO.GenerateParamDTO(code));
            }catch (Exception e){
                log.error("对账单明细 单据手动生成失败",e);
                resultDTO = BatchResultDTO.fail(code,code, e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 状态更新
     * @author will
     * @date 2025/6/11 15:48
     * @param dto
     * @return ApiResult<Object>
     */
    @PostMapping("/updateStatus")
    @LogAction(value = LogActionEnum.UPDATE_STATUS, desc = "状态更新")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliationDetail:scm:updateStatus",
            serviceClass = PoReconciliationService.class,
            keyIdName = "ids")
    public ApiResult<Object> updateStatus(@RequestBody @Validated BaseIdsDTO.StatusDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO resultDTO;
            try {
                resultDTO = poReconciliationDetailScmService.updateStatus(id,dto.getStatus());
            }catch (Exception e){
                log.error("对账单明细 单据状态更新失败",e);
                PoReconciliationDetailEntity entity = poReconciliationDetailScmService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    resultDTO = BatchResultDTO.fail(id, id, "对账单明细不存在, 单据状态更新失败");
                    resultDTOS.add(resultDTO);
                    continue;
                }
                resultDTO = BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage());
            }
            resultDTOS.add(resultDTO);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 对账设置
     * @author will
     * @date 2025/6/11 15:48
     * @return ApiResult<Object>
     */
    @PostMapping("/addSetting")
    @LogAction(value = LogActionEnum.INSERT, desc = "对账设置")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "srm:poReconciliationDetail:scm:addSetting",
            serviceClass = PoReconciliationService.class,
            keyIdName = "ids")
    public ApiResult<Object> addSetting(@RequestBody @Validated PoReconciliationDetailDTO.AddSettingDTO addSettingDTO) {
        Boolean flag = poReconciliationDetailScmService.addSetting(addSettingDTO);
        return flag ? success() : failure();
    }

}

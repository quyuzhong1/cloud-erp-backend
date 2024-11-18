package com.erp.server.wms.controller.pda;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.business.annotation.DataPermission;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.anno.LogViewService;
import com.common.core.controller.BaseController;
import com.common.core.enums.LogActionEnum;
import com.common.core.controller.vo.ApiResult;
import com.erp.model.wms.dto.PoInstockDTO;
import com.erp.model.wms.entity.PoInstockEntity;
import com.erp.model.wms.entity.PoReturnEntity;
import com.erp.model.wms.entity.SubcontractIssueEntity;
import com.erp.server.wms.service.PoInstockService;
import com.erp.server.wms.service.PoReturnService;
import com.erp.server.wms.service.SubcontractIssueService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * PDA:采购入库单
 * @Author Luo_WG
 * @Date 2023/8/15 10:23
 **/
@Slf4j
@RestController
@LogSystemModule("PDA采购入库单")
@RequestMapping("/pdaPoInStock")
public class PdaPoInStockController extends BaseController {
    @Resource
    private PoInstockService poInstockService;
    @Resource
    private PoReturnService poReturnService;
    @Resource
    private SubcontractIssueService subcontractIssueService;
    /**
     * 列表查询
     * @Author Luo_WG
     * @Date 2023/8/16 14:50
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.PoInstockDTO.PdaListDTO>>
     **/
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<PagingVO<PoInstockDTO.PdaPagingView>> paging(@RequestBody @Validated PagingDTO<PoInstockDTO.PdaSearchParamDTO> dto) {
        PagingVO<PoInstockDTO.PdaPagingView> pagingVO = poInstockService.PdaPaging(dto);
        return success(pagingVO);
    }

    /**
     * 列表数量
     * @Author Luo_WG
     * @Date 2023/8/16 18:00
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.PoInstockDTO.PdaPoInStockCountDTO>>
     **/
    @PostMapping("/listCount")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:paging",
            tableAlias = "psi"
    )
    public ApiResult<List<PoInstockDTO.PdaPoInStockCountDTO>> listCount(@RequestBody PermissionsDTO dto) {
        List<PoInstockDTO.PdaPoInStockCountDTO> list = poInstockService.pdaListCount(dto);
        return success(list);
    }

    /**
     * 新增
     * @Author Luo_WG
     * @Date 2023/8/11 10:19
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INSERT, desc = "新增采购入库单")
    @PostMapping("/add")
    public ApiResult add(@RequestBody @Validated PoInstockDTO.AddDTO dto) {
        String id = poInstockService.pdaAdd(dto, Boolean.FALSE);
        return CharSequenceUtil.isNotBlank(id) == true ? success() : failure();
    }

    /**
     * 修改
     * @Author Luo_WG
     * @Date 2023/4/6 18:46
     * @param dto dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE, desc = "修改采购入库单")
    @PostMapping("/update")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:update",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult update(@RequestBody @Validated PoInstockDTO.UpdateDTO dto) {
        Boolean flag = poInstockService.pdaUpdate(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 查询详情
     * @Author Luo_WG
     * @Date 2023/8/17 9:15
     * @param id
     * @return com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.PoInstockDTO.ViewDTO>
     **/
    @LogViewService
    @GetMapping("/view")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:pdaPoInStock:view",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult<PoInstockDTO.ViewDTO> view(@RequestParam("id") String id) {
        PoInstockDTO.ViewDTO dto = poInstockService.pdaView(id);
        return success(dto);
    }

    /**
     * 新增并提交
     * @Author Luo_WG
     * @Date 2023/8/17 10:03
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.ADD_AND_SUBMIT, desc = "新增并提交采购入库单")
    @PostMapping("/addAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:add",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult addAndSubmit(@RequestBody @Validated PoInstockDTO.AddDTO dto) {
        String id = poInstockService.pdaAddAndSubmit(dto);
        return CharSequenceUtil.isNotBlank(id) ? success() : failure();
    }

    /**
     * 修改并提交
     * @Author Luo_WG
     * @Date 2023/8/17 10:03
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.UPDATE_AND_SUBMIT, desc = "修改并提交采购入库单")
    @PostMapping("/updateAndSubmit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:update",
            serviceClass = PoInstockService.class,
            keyIdName = "id")
    public ApiResult updateAndSubmit(@RequestBody @Validated PoInstockDTO.UpdateDTO dto) {
        Boolean flag = poInstockService.pdaUpdateAndSubmit(dto);
        return flag == true ? success() : failure();
    }

    /**
     * 提交
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交采购入库单")
    @PostMapping("/submit")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:submit",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult submit(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poInstockService.submit(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 删除
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DELETE, desc = "删除采购入库单")
    @PostMapping("/delete")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:delete",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult delete(@RequestBody @Valid BaseIdsDTO.IdsDTO dto) {
        Boolean flag = poInstockService.delete(dto.getIds());
        return flag == true ? success() : failure();
    }

    /**
     * 作废
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.INVALID, desc = "作废采购入库单")
    @PostMapping("/invalid")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:invalid",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult invalid(@RequestBody @Validated BaseIdsDTO.RemarkDTO dto) {
        Boolean flag = poInstockService.invalid(dto.getIds(),dto.getRemark());
        return flag == true ? success() : failure();
    }

    /**
     * 批量审核
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核采购入库单")
    @PostMapping("/approve")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:approve",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PoInstockEntity> entityList = poInstockService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购收货单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(poInstockService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("采购收货单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 批量反审核
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核采购入库单")
    @PostMapping("/disApprove")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:disApprove",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PoInstockEntity> entityList = poInstockService.listByIds(dto.getIds());
        List<PoReturnEntity> purchaseReturnOrderList = poReturnService.listBySourceIds(dto.getIds());
        List<SubcontractIssueEntity> subcontractIssueList = subcontractIssueService.listBySourceIdList(dto.getIds());
        for (String id : dto.getIds()) {
            PoInstockEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购入库单记录不存在"));
                continue;
            }
            List<PoReturnEntity> returnEntityList = purchaseReturnOrderList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getSourceId()) && e.getSourceId().equals(id)).collect(Collectors.toList());
            List<SubcontractIssueEntity> issueEntityList = subcontractIssueList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getSourceId()) && e.getSourceId().equals(id)).collect(Collectors.toList());
            try {
                resultDTOS.add(poInstockService.disApprove(entity,returnEntityList, issueEntityList));
            }catch (Exception e){
                log.error("采购入库单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 取消流程
     * @Author Luo_WG
     * @Date 2023/8/17 10:04
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     **/
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销采购入库单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "purchase_user_id,stock_in_user_id",
            menuCode = "wms:pdaPoInStock:cancelProcess",
            serviceClass = PoInstockService.class,
            keyIdName = "ids")
    public ApiResult cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        Boolean result = poInstockService.cancelProcess(dto.getIds());
        return result == true ? success() : failure();
    }
}

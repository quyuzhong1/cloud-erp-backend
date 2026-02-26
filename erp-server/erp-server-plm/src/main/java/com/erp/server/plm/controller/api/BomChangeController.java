package com.erp.server.plm.controller.api;

import cn.hutool.core.util.ObjectUtil;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.ApproveDTO;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.anno.LogSystemModule;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.enums.LogActionEnum;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.*;
import com.erp.model.plm.entity.BomChangeEntity;
import com.erp.model.plm.vo.BomChangePagingVO;
import com.erp.model.workflow.vo.ApproveNodeRecordVO;
import com.erp.server.plm.constant.BomConstant;
import com.erp.server.plm.query.BomChangeHandler;
import com.erp.server.plm.service.BomInfoService;
import com.erp.server.plm.service.BomChangeService;
import com.erp.server.plm.service.ProductDetailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 变更管理
 *
 * @author yl
 * @since 2023-01-11 14:05:03
 */
@RestController
@LogSystemModule("BOM管理")
@RequestMapping("change")
@RefreshScope(proxyMode = ScopedProxyMode.DEFAULT)
@Slf4j
public class BomChangeController extends BaseController {


    @Resource
    private BomChangeService bomChangeService;

    @Resource
    private BomInfoService bomInfoService;

    @Resource
    private ProductDetailService productDetailService;

    /**
     * 添加变更
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INSERT, desc = "添加变更")
    @PostMapping("/add")
    public ApiResult<Object> add(@RequestBody @Validated AddChangeDTO dto) {
        Boolean result = bomChangeService.add(dto);
        return result == true ? success() : failure();
    }


    /**
     * 编辑变更
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.CUSTOM_UPDATE, desc = "更新变更:id={id},数据源id={源数据id},变更类型={type}")
    @PostMapping("/update")
    public ApiResult<Object> update(@RequestBody @Validated UpdateChangeDTO dto) {
        Boolean result = bomChangeService.edit(dto);
        return result == true ? success() : failure();
    }


    /**
     * 变更分页展示
     *
     * @param dto
     * @return
     */
    @PostMapping("/paging")
    @WebAdvanceQuery(handler = BomChangeHandler.class)
    public ApiResult<PagingVO<List<BomChangePagingVO>>> queryByPage(@RequestBody @Validated PagingDTO<SearchPagingDTO> dto) {
        PagingVO<List<BomChangePagingVO>> pagingVO = bomChangeService.paging(dto);
        return success(pagingVO);
    }

    /**
     * 获取 tab列表
     * @author Will
     * @date: 2023/10/13 11:49
     * @param dto
     * @return ApiResult<List<TabListDTO>>
     */
    @PostMapping("/tabList")
    public ApiResult<List<BomChangePagingVO.TabListDTO>> tabList(@RequestBody PermissionsDTO dto) {
        List<BomChangePagingVO.TabListDTO> tabList = bomChangeService.tabList(dto);
        return success(tabList);
    }


    /**
     * 变更详情
     *
     * @param dto
     * @return
     */
    @PostMapping("/view")
    public ApiResult<Object> details(@RequestBody @Validated BaseIdDTO dto) {

        //获取到变更信息
        BomChangeEntity changeEntity = bomChangeService.getById(dto.getId());
        if (Objects.isNull(changeEntity)) {
            throw new ServiceException(ApiError.COMMON_CHANGE_INFO_REQUIRED);
        }
        Object object = null;
        String type = changeEntity.getType();
        //对应就是bom
        if (BomConstant.CHANGE_BOM.equals(type)) {
            ProductBomChangeDTO bomChange = bomChangeService.getBomDetails(changeEntity);
            object = bomChange;
        }
        //对应sku
        if (BomConstant.CHANGE_SKU.equals(type)) {
            BomChangeDTO skuChange = bomChangeService.skuDetails(changeEntity);
            object = skuChange;
        }
        if (object != null) {
            return success(object);
        }
        return failure();
    }

    /**
     * 作废
     *
     * @param dto
     * @return
     */
    @LogAction(value = LogActionEnum.INVALID, desc = "作废变更")
    @PostMapping("/cancellation")
    public ApiResult<Object> cancellation(@RequestBody @Validated BaseIdDTO dto) {
        Boolean result = bomChangeService.cancellation(dto.getId());
        return result == true ? success() : failure();
    }

    /**
     * 获取变更的信息
     *
     * @param
     * @return
     */
    @PostMapping("/list")
    public ApiResult<List<ChangeInfoDTO>> list(@RequestBody @Validated BomChangeListSearchDTO dto) {
        List<ChangeInfoDTO> list = bomChangeService.getChangeByType(dto.getType(), dto.getSearchKeyword());
        return success(list);
    }

    /**
     * 提交审核
     * @author will
     * @date:  2024-01-08
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/submit")
    @LogAction(value = LogActionEnum.SUBMIT, desc = "产品变更单提交审核")
    public ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO submit;
            try {
                submit = bomChangeService.submit(id,Boolean.TRUE);
            }catch (Exception e){
                log.error("产品变更单 提交审核失败",e);
                BomChangeEntity entity = bomChangeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    submit = BatchResultDTO.fail(id, id, "产品变更单不存在, 提交失败");
                    resultDTOS.add(submit);
                    continue;
                }
                submit = BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage());
            }
            resultDTOS.add(submit);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 审核
     * @author will
     * @date 2025/5/16 15:00
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/approve")
    @LogAction(value = LogActionEnum.APPROVE, desc = "变更单审核")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<String> ids = dto.getIds();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : ids) {
            BatchResultDTO approveResult;
            try {
                approveResult = bomChangeService.approve(new ApproveOneDTO(id, dto.getType(),dto.getComment()));
            }catch (Exception e){
                log.error("变更单审核失败",e);
                BomChangeEntity entity = bomChangeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    approveResult = BatchResultDTO.fail(id, id, "变更单不存在, 审核失败");
                    resultDTOS.add(approveResult);
                    continue;
                }
                approveResult = BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage());
            }
            resultDTOS.add(approveResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销流程
     * @author will
     * @date:  2025-05-16
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @PostMapping("/cancelProcess")
    @LogAction(value = LogActionEnum.CANCEL, desc = "变更撤销")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        for (String id : dto.getIds()) {
            BatchResultDTO cancelResult;
            try {
                cancelResult = bomChangeService.cancelProcess(new ApproveDTO.CancelProcessDTO(id));
            }catch (Exception e){
                log.error("变更信息流程失败",e);
                BomChangeEntity entity = bomChangeService.getById(id);
                if (ObjectUtil.isEmpty(entity)) {
                    cancelResult = BatchResultDTO.fail(id, id, "变更信息不存在, 撤回流程失败");
                    resultDTOS.add(cancelResult);
                    continue;
                }
                cancelResult = BatchResultDTO.fail(entity.getId(), entity.getSourceCode(), e.getMessage());
            }
            resultDTOS.add(cancelResult);
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 查询变更字段
     *
     * @param dto
     * @return
     */
    @PostMapping("/listChangeField")
    public ApiResult<List<String>> listChangeField(@RequestBody @Validated BaseIdDTO dto) {
        List<String> list = bomChangeService.listChangeField(dto.getId());
        return success(list);
    }


    /**
     * bom 审核情况
     *
     * @return
     */
    @PostMapping("/auditInfo")
    public ApiResult<List<ApproveNodeRecordVO>> auditInfo(@RequestBody @Validated BaseIdDTO dto) {
        List<ApproveNodeRecordVO> list= bomChangeService.auditInfo(dto.getId());
        return success(list);
    }

}


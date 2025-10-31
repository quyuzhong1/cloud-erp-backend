package com.erp.server.oms.controller.api;


import com.common.business.annotation.DataPermission;
import com.common.business.annotation.WebAdvanceQuery;
import com.common.business.dto.base.*;
import com.common.business.enums.DataAttributeEnum;
import com.common.business.vo.PagingVO;
import com.common.core.anno.LogAction;
import com.common.core.controller.BaseController;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.LogActionEnum;
import com.erp.model.oms.dto.SoB2cRefundDTO;
import com.erp.model.oms.entity.SoB2cRefundEntity;
import com.erp.server.oms.service.SoB2cRefundService;
import lombok.extern.slf4j.Slf4j;
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
 * <p>
 * 退款订单 前端控制器
 * </p>
 *
 * @author Lambda
 * @since 2024-09-12
 */
@Slf4j
@RestController
@RequestMapping("/refundOrder")
public class SoB2cRefundController extends BaseController {


    @Resource
    private SoB2cRefundService soB2cRefundService;

    /**
     * 退款订单分页
     *
     * @return
     */
    @PostMapping("/paging")
    @DataPermission(operationType = DataAttributeEnum.LIST,
            shopTableField = "ro.shop_id",
            menuCode = "oms:refundOrder:paging"
    )
    @WebAdvanceQuery
    public ApiResult<PagingVO<SoB2cRefundDTO.PagingViewDTO>> queryByPage(@RequestBody @Validated PagingDTO<SoB2cRefundDTO.PagingParamDTO> dto) {
        PagingVO<SoB2cRefundDTO.PagingViewDTO> pagingVO = soB2cRefundService.paging(dto);
        return success(pagingVO);
    }


    /**
     * 退款订单导出
     *
     * @return
     */
    @PostMapping("/export")
    @WebAdvanceQuery
    public ApiResult<Object> export(@RequestBody @Validated SoB2cRefundDTO.PagingParamDTO dto) {
        soB2cRefundService.exportExcel(dto);
        return success();
    }
    

    /**
     * 提交
     * @author will 
     * @date 2025/10/24 11:51
     * @param dto 
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.SUBMIT, desc = "提交售后订单")
    @PostMapping("/submit")
    public  ApiResult<List<BatchResultDTO>> submit(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoB2cRefundEntity> entityList = soB2cRefundService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoB2cRefundEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"售后订单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soB2cRefundService.submit(entity,Boolean.TRUE));
            }catch (Exception e){
                log.error("售后订单提交失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 撤销售后单
     * @author will
     * @date 2025/10/24 11:57
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.CANCEL, desc = "撤销售后单")
    @PostMapping("/cancelProcess")
    @DataPermission(operationType = DataAttributeEnum.CHECK_BY_ID,
            tableField = "create_user_id",
            menuCode = "wms:refundOrder:cancelProcess",
            serviceClass = SoB2cRefundService.class,
            keyIdName = "ids")
    public ApiResult<List<BatchResultDTO>> cancelProcess(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoB2cRefundEntity> entityList = soB2cRefundService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoB2cRefundEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"售后订单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soB2cRefundService.cancelProcess(entity));
            }catch (Exception e){
                log.error("售后订单取消流程失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }


    /**
     * 审核
     * @author will
     * @date 2025/10/24 11:54
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.APPROVE, desc = "审核售后订单")
    @PostMapping("/approve")
    public ApiResult<List<BatchResultDTO>> approve(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoB2cRefundEntity> entityList = soB2cRefundService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoB2cRefundEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"售后订单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soB2cRefundService.approve(entity,new ApproveOneDTO(id,dto.getType(),dto.getComment())));
            }catch (Exception e){
                log.error("售后订单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

    /**
     * 售后订单反审核
     * @author will
     * @date 2025/10/24 11:55
     * @param dto
     * @return ApiResult<List<BatchResultDTO>>
     */
    @LogAction(value = LogActionEnum.DISAPPROVE, desc = "反审核售后订单")
    @PostMapping("/disApprove")
    public ApiResult<List<BatchResultDTO>> disApprove(@RequestBody @Validated BaseIdsDTO.IdsDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoB2cRefundEntity> entityList = soB2cRefundService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoB2cRefundEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"售后订单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soB2cRefundService.disApprove(entity));
            }catch (Exception e){
                log.error("售后订单反审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS.stream().allMatch(BatchResultDTO::getSuccess) ? success(resultDTOS) : failure(resultDTOS);
    }

}

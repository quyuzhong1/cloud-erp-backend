package com.erp.server.wms.controller.feign;

import com.common.business.dto.base.ApproveOneDTO;
import com.common.business.dto.base.BaseApproveParamDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.core.utils.BeanMapper;
import com.erp.model.wms.entity.*;
import com.erp.model.workflow.dto.WorkOptionDTO;
import com.erp.server.wms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 采购订单feign
 *
 * @Author Luo_WG
 * @Date 2023/4/13 11:11
 **/
@Slf4j
@RestController
@RequestMapping("feign/wmsWorkOption")
public class WmsWorkOptionFeignController {
    @Resource
    private WorkOptionService workOptionService;

    @Resource
    private WarehouseReceiveService warehouseReceiveService;
    @Resource
    private WarehouseReceiveDetailService warehouseReceiveDetailService;

    @Resource
    private PoInstockService poInstockService;

    @Resource
    private PoReturnService poReturnService;
    @Resource
    private PoReturnDetailService poReturnDetailService;
    @Resource
    private TransferApplicationService transferApplicationService;

    @Resource
    private StocktakingTaskService stocktakingTaskService;

    @Resource
    private FirstMileDeliveryService firstMileDeliveryService;

    @Resource
    private WmsDeliveryPlanService wmsDeliveryPlanService;

    @Resource
    private TransferInfoService transferInfoService;

    @Resource
    private SoDeliveryNoticeChangeService soDeliveryNoticeChangeService;

    @Resource
    private RequisitionApplicationChangeService requisitionApplicationChangeService;

    /**
     * 根据入参查询单据数量
     *
     * @Author Luo_WG
     * @Date 2023/4/21 15:34
     **/
    @PostMapping("/getTableNum")
    public List<WorkOptionDTO.MyWorkOptionDTO> getTableNum(@RequestBody List<WorkOptionDTO.MyWorkOptionDTO> myWorkOptionDTOList) {
        return workOptionService.getTableNum(myWorkOptionDTOList);
    }

    /**
     * 采购收货审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @PostMapping("/warehouseReceiveApprove")
    public List<BatchResultDTO> warehouseReceiveApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<WarehouseReceiveEntity> entityList = warehouseReceiveService.listByIds(dto.getIds());
        List<WarehouseReceiveDetailEntity> receiveDetailList = warehouseReceiveDetailService.listDetailByMainIds(dto.getIds());
        for (String id : dto.getIds()) {
            WarehouseReceiveEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购收货单记录不存在"));
                continue;
            }
            List<WarehouseReceiveDetailEntity> detailEntityList = receiveDetailList.stream().filter(e -> e.getMainId().equals(id)).collect(Collectors.toList());
            try {
                resultDTOS.add(warehouseReceiveService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),detailEntityList));
            }catch (Exception e){
                log.error("采购收货单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

    /**
     * 采购入库审核
     *
     * @param dto
     * @return ApiResult
     * @author Will
     * @date: 2023/4/11 20:11
     */
    @PostMapping("/poInstockApprove")
    public List<BatchResultDTO> poInstockApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
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
        return resultDTOS;
    }

    /**
     * 采购退货审核
     *
     * @param dto
     * @return com.common.core.controller.vo.ApiResult
     * @Author Luo_WG
     * @Date 2023/4/6 19:06
     **/
    @PostMapping("/purchaseReturnOrderApprove")
    public List<BatchResultDTO> purchaseReturnOrderApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<PoReturnEntity> entityList = poReturnService.listByIds(dto.getIds());
        List<PoReturnDetailEntity> poReturnDetailList = poReturnDetailService.listByMainIds(dto.getIds());
        for (String id : dto.getIds()) {
            PoReturnEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"采购退货单记录不存在"));
                continue;
            }
            List<PoReturnDetailEntity> detailEntityList = poReturnDetailList.stream().filter(e -> e.getMainId().equals(id)).collect(Collectors.toList());
            try {
                resultDTOS.add(poReturnService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),detailEntityList));
            }catch (Exception e){
                log.error("采购退货单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

    /**
     * 调拨申请单审核
     *
     * @param dto
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/8/3 16:38
     **/
    @PostMapping("/transferApplicationApprove")
    public List<BatchResultDTO> transferApplicationApprove(@RequestBody @Validated BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferApplicationEntity> entityList = transferApplicationService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferApplicationEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"调拨申请单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferApplicationService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess()));
            }catch (Exception e){
                log.error("调拨申请单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }

    /**
     * 盘底任务审核
     *
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     * @Author Luo_WG
     * @Date 2023/8/3 16:38
     **/
    @PostMapping("/stocktakingTaskApprove")
    public Boolean stocktakingTaskApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        ApproveOneDTO oneDto = new ApproveOneDTO();
        BeanMapper.copy(baseApproveParamDTO,oneDto);
        String id= CollectionUtils.isNotEmpty(baseApproveParamDTO.getIds())?baseApproveParamDTO.getIds().get(0):"";
        oneDto.setId(id);
        stocktakingTaskService.approve(id, oneDto);
        return Boolean.TRUE;
    }

    /**
     * 头程发货单
     * @Author Luo_WG
     * @Date 2023/11/15 18:01
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("/fbaDeliveryApprove")
    public Boolean fbaDeliveryApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        ApproveOneDTO oneDto = new ApproveOneDTO();
        BeanMapper.copy(baseApproveParamDTO,oneDto);
        String id= CollectionUtils.isNotEmpty(baseApproveParamDTO.getIds())?baseApproveParamDTO.getIds().get(0):"";
        oneDto.setId(id);
        firstMileDeliveryService.approve(oneDto);
        return Boolean.TRUE;
    }

    /**
     * 海外发货计划
     * @Author Luo_WG
     * @Date 2023/11/17 16:16
     * @param baseApproveParamDTO
     * @return java.lang.Boolean
     **/
    @PostMapping("/overseasDeliveryPlanApprove")
    public Boolean deliveryPlanApprove(@RequestBody @Validated BaseApproveParamDTO baseApproveParamDTO) {
        ApproveOneDTO oneDto = new ApproveOneDTO();
        BeanMapper.copy(baseApproveParamDTO,oneDto);
        String id= CollectionUtils.isNotEmpty(baseApproveParamDTO.getIds())?baseApproveParamDTO.getIds().get(0):"";
        oneDto.setId(id);
        wmsDeliveryPlanService.approve(oneDto);
        return Boolean.TRUE;
    }

    /**
     * 直接调拨单审核
     * @param dto
     * @return
     */
    @PostMapping("/transferInfoApprove")
    public List<BatchResultDTO> transferInfoApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<TransferInfoEntity> entityList = transferInfoService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            TransferInfoEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"直接调拨单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(transferInfoService.approve(entity,dto.getType(),dto.getComment(),dto.getIsNeedProcess(),Boolean.TRUE,Boolean.TRUE));
            }catch (Exception e){
                log.error("直接调拨单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }


    /**
     * 发货通知变更单审核
     * @param dto
     * @return
     */
    @PostMapping("/noticeChangeApprove")
    public List<BatchResultDTO> noticeChangeApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<SoDeliveryNoticeChangeEntity> entityList = soDeliveryNoticeChangeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            SoDeliveryNoticeChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"发货通知变更单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(soDeliveryNoticeChangeService.approve(new ApproveOneDTO(entity.getId(),dto.getType(),dto.getComment())));
            }catch (Exception e){
                log.error("发货通知变更单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }


    /**
     * 要货申请变更单审核
     * @param dto
     * @return
     */
    @PostMapping("/requisitionChangeApprove")
    public List<BatchResultDTO> requisitionChangeApprove(@RequestBody BaseApproveParamDTO dto) {
        List<BatchResultDTO> resultDTOS = new ArrayList<>(dto.getIds().size());
        List<RequisitionApplicationChangeEntity> entityList = requisitionApplicationChangeService.listByIds(dto.getIds());
        for (String id : dto.getIds()) {
            RequisitionApplicationChangeEntity entity = entityList.stream().filter(v->v.getId().equals(id)).findFirst().orElse(null);
            if(Objects.isNull(entity)){
                resultDTOS.add(BatchResultDTO.fail(id,id,"发货通知变更单记录不存在"));
                continue;
            }
            try {
                resultDTOS.add(requisitionApplicationChangeService.approve(entity.getId(),new ArrayList<>(), dto.getType()));
            }catch (Exception e){
                log.error("发货通知变更单审核失败",e);
                resultDTOS.add(BatchResultDTO.fail(entity.getId(), entity.getCode(), e.getMessage()));
            }
        }
        return resultDTOS;
    }
}

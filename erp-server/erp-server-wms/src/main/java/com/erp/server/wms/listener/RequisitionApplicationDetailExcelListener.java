package com.erp.server.wms.listener;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.wms.dto.RequisitionApplicationDTO;
import com.erp.model.wms.dto.excel.RequisitionApplicationDetailExcelDTO;
import com.erp.model.wms.entity.FbaShipmentEntity;
import com.erp.model.wms.entity.FbaShipmentPackingEntity;
import com.erp.model.wms.entity.RequisitionApplicationEntity;
import com.erp.server.wms.service.FbaShipmentPackingService;
import com.erp.server.wms.service.FbaShipmentService;
import com.erp.server.wms.service.RequisitionApplicationService;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 要货申请下推发货单明细
 */
public class RequisitionApplicationDetailExcelListener extends AnalysisEventListener<RequisitionApplicationDetailExcelDTO> {

    private final FbaShipmentPackingService fbaShipmentPackingService = SpringUtil.getBean(FbaShipmentPackingService.class);
    private final FbaShipmentService fbaShipmentService = SpringUtil.getBean(FbaShipmentService.class);
    private final RequisitionApplicationService requisitionApplicationService = SpringUtil.getBean(RequisitionApplicationService.class);

    /**
     * 明细数据
     */
    private List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> fbaBindShipmentViewDTOS;

    /**
     * 成功的数据
     */
    private List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> successList = new ArrayList<>();


    /**
     * 导入错误数据
     */
    private List<RequisitionApplicationDetailExcelDTO> errorList = new ArrayList<>();


    public RequisitionApplicationDetailExcelListener(List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> fbaBindShipmentViewDTOS) {
        if (CollectionUtils.isNotEmpty(fbaBindShipmentViewDTOS)){
            fbaBindShipmentViewDTOS.forEach(e -> {
                e.setId(StrUtil.isNotBlank(e.getId()) && !Objects.equals(e.getId(),"null") ? e.getId() : "");
                e.setTaskId(StrUtil.isNotBlank(e.getTaskId()) && !Objects.equals(e.getTaskId(),"null") ? e.getTaskId() : "");
                e.setCartonId(StrUtil.isNotBlank(e.getCartonId()) && !Objects.equals(e.getCartonId(),"null") ? e.getCartonId() : "");
                e.setBoxNo(StrUtil.isNotBlank(e.getBoxNo()) && !Objects.equals(e.getBoxNo(),"null") ? e.getBoxNo() : "");
                e.setPackingSku(StrUtil.isNotBlank(e.getPackingSku()) && !Objects.equals(e.getPackingSku(),"null") ? e.getPackingSku() : "");
                e.setPackingFnSku(StrUtil.isNotBlank(e.getPackingFnSku()) && !Objects.equals(e.getPackingFnSku(),"null") ? e.getPackingFnSku() : "");
                e.setFbaShipmentId(StrUtil.isNotBlank(e.getFbaShipmentId()) && !Objects.equals(e.getFbaShipmentId(),"null") ? e.getFbaShipmentId() : "");
                e.setFbaShipmentCode(StrUtil.isNotBlank(e.getFbaShipmentCode()) && !Objects.equals(e.getFbaShipmentCode(),"null") ? e.getFbaShipmentCode() : "");
                e.setFbaBoxNo(StrUtil.isNotBlank(e.getFbaBoxNo()) && !Objects.equals(e.getFbaBoxNo(),"null") ? e.getFbaBoxNo() : "");
                e.setFbaPackingSku(StrUtil.isNotBlank(e.getFbaPackingSku()) && !Objects.equals(e.getFbaPackingSku(),"null") ? e.getFbaPackingSku() : "");
                e.setFbaPackingFnSku(StrUtil.isNotBlank(e.getFbaPackingFnSku()) && !Objects.equals(e.getFbaPackingFnSku(),"null") ? e.getFbaPackingFnSku() : "");
                e.setDeliveryCode(StrUtil.isNotBlank(e.getDeliveryCode()) && !Objects.equals(e.getDeliveryCode(),"null") ? e.getDeliveryCode() : "");
            });
        }
        this.fbaBindShipmentViewDTOS  = CollectionUtils.isEmpty(fbaBindShipmentViewDTOS) ? Collections.emptyList() : fbaBindShipmentViewDTOS;
    }

    /**
     * 解析每一行回调
     * @param RequisitionApplicationDetailExcelDTO
     * @param analysisContext
     */
    @Override
    public void invoke(RequisitionApplicationDetailExcelDTO RequisitionApplicationDetailExcelDTO, AnalysisContext analysisContext) {
        //注解验证信息
        List<String> errorMsgList = new ArrayList<>();
        List<String> msgList = FieldValidUtil.fieldValid(RequisitionApplicationDetailExcelDTO);
        if (CollectionUtils.isNotEmpty(msgList)) {
            errorMsgList.addAll(msgList);
        }
        List<FbaShipmentPackingEntity> fbaShipmentPackingEntities = fbaShipmentPackingService.listByFbaCodes(Collections.singletonList(RequisitionApplicationDetailExcelDTO.getFbaShipmentCode()));
        if (CollectionUtils.isNotEmpty(fbaShipmentPackingEntities)){
            errorMsgList.add(StrUtil.format("{}已绑定下推发货单，无法重复下推",RequisitionApplicationDetailExcelDTO.getFbaShipmentCode()));
        }
        //校验店铺是否一致
        String id = fbaBindShipmentViewDTOS.stream().map(RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO::getId).filter(StrUtil::isNotBlank).findFirst().orElse(null);
        RequisitionApplicationEntity requisitionApplication = null;
        if (StrUtil.isBlank(id)){
            errorMsgList.add("要货申请记录id不能为空");
        }else {
            requisitionApplication = requisitionApplicationService.getById(id);
        }
        if (Objects.isNull(requisitionApplication)){
            errorMsgList.add("要货申请记录不存在");
        }
        FbaShipmentEntity shipmentEntity = null;
        if (StrUtil.isNotBlank(RequisitionApplicationDetailExcelDTO.getFbaShipmentCode())){
            shipmentEntity = fbaShipmentService.getByCode(RequisitionApplicationDetailExcelDTO.getFbaShipmentCode());
        }
        if (Objects.isNull(shipmentEntity)){
            errorMsgList.add("FBI货件表记录不存在");
        }
        if (Objects.nonNull(shipmentEntity) && Objects.nonNull(requisitionApplication) && !Objects.equals(shipmentEntity.getShopId(), requisitionApplication.getChannelId())){
            errorMsgList.add("货件与要货申请的店铺不一致");
        }
        //货件号和箱号必须同时存在，且货件每个货件号下的货件箱号必须从1开始且连续
//        RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO shipmentViewDetailDTO = fbaBindShipmentViewDTOS.get(0);
//        if (Objects.nonNull(shipmentViewDetailDTO) && Objects.equals(shipmentViewDetailDTO.getBoxNo(), RequisitionApplicationDetailExcelDTO.getBoxNo())
//                && StrUtil.isNotBlank(shipmentViewDetailDTO.getFbaBoxNo()) && Objects.equals("1",shipmentViewDetailDTO.getFbaBoxNo())){
//            errorMsgList.add("货件箱号必须从1开始且连续");
//        }
        //存在错误数据则直接返回
        if (errorMsgList.size() > 0) {
            RequisitionApplicationDetailExcelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
            errorList.add(RequisitionApplicationDetailExcelDTO);
            return;
        }
        //先匹配到对应的记录 然后赋值
        FbaShipmentEntity finalShipmentEntity = shipmentEntity;
        fbaBindShipmentViewDTOS.forEach(e -> {
            //数据已存在就不能覆盖
            if (RequisitionApplicationDetailExcelDTO.getBoxNo().equals(e.getBoxNo()) && StrUtil.isBlank(e.getFbaBoxNo()) && StrUtil.isBlank(e.getFbaShipmentCode())){
                e.setFbaBoxNo(RequisitionApplicationDetailExcelDTO.getFbaBoxNo());
                e.setFbaShipmentCode(finalShipmentEntity.getCode());
                e.setFbaShipmentId(finalShipmentEntity.getId());
            }
        });
    }


    /**
     * 数据全部解析完成调用
     */
    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {

    }
    public List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> getExcelDateList(){
        return fbaBindShipmentViewDTOS;
    }

    public List<RequisitionApplicationDetailExcelDTO> getErrorList() {
        return errorList;
    }


    public List<RequisitionApplicationDTO.FbaBindShipmentViewDetailDTO> getSuccessList() {
        return successList;
    }
}

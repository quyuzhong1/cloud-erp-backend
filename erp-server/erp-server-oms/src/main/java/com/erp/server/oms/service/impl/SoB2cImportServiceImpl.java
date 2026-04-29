package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.enums.ApproveStatusEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.wrapper.FeignQuery;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.FieldValidUtil;
import com.erp.model.oms.dto.SoB2cDTO;
import com.erp.model.oms.dto.excel.B2CManualDeliveryImportExcelDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;
import com.erp.model.oms.entity.SoB2cLogisticsEntity;
import com.erp.model.oms.entity.SoB2cReceiverEntity;
import com.erp.model.plm.dto.ProductChangeDTO;
import com.erp.model.plm.dto.ProductChangeDetailDTO;
import com.erp.model.plm.dto.excel.ProductChangeImportExcelDTO;
import com.erp.model.plm.entity.ProductChangeEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.enums.ProductChangeFieldEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.InvalidStatusEnum;
import com.erp.model.tms.dto.LogisticsChannelDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.entity.WarehouseEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.WmsOverseasWarehouseFeign;
import com.erp.rpc.wms.feign.WmsTaskFeign;
import com.erp.rpc.wms.feign.WmsWarehouseFeign;
import com.erp.server.oms.listener.B2CManualDeliveryExcelListener;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.service.*;
import io.seata.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * B2C销售订单表 拆分操作服务类
 * </p>
 *
 */
@Service
@Slf4j
public class SoB2cImportServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements SoB2cImportService {

    @Resource
    private SoB2cService soB2cService;

    @Resource
    private SoB2cLogisticsService soB2cLogisticsService;

    @Resource
    private SoB2cReceiverService soB2cReceiverService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private SoB2cDetailService soB2cDetailService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private FileFeign fileFeign;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;

    @Resource
    private LogisticsFeign logisticsFeign;


    @Override
    public void importManualDelivery(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("手动发货导入", FileTaskEventEnum.IMPORT_OMS_B2C_MANUAL_DELIVERY.getCode(), dto);
    }

    @Override
    public void importB2cManualDelivery(BaseDTO.ImportDTO dto) {
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream().filter(e -> org.apache.commons.lang3.StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId())).findFirst().orElse(null);
        if(Objects.nonNull(findUserDTO)){
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        B2CManualDeliveryExcelListener excelListenerUtil = new B2CManualDeliveryExcelListener(dto.getTaskId(),dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), B2CManualDeliveryImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<B2CManualDeliveryImportExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "B2C手动发货错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, B2CManualDeliveryImportExcelDTO.class);
            if (!file.isDirectory()) {
                url = FastDFSClientUtil.uploadFile(file, fileName);
            }
        }
        importResultDTO.setRemark("处理完成，失败" + errorList.size() + "条");
        importResultDTO.setErrorUrl(url);
        importResultDTO.setFinishTime(LocalDateTime.now());
        importResultDTO.setStatus(FileTaskStatusEnum.FINISH.getCode());
        downloadTaskFeign.updateTask(importResultDTO);

    }

    @Override
    public void downloadManualDeliveryTemplate(HttpServletResponse response) {
        String path = "excel/soB2cManualDelivery.xlsx";
        String excelName = "b2c手动发货.xlsx";

        com.common.core.utils.ExcelUtil.downloadTemplate(path, excelName, response);
    }

    @Override
    public void handleManualDeliveryImportSuccessList(List<B2CManualDeliveryImportExcelDTO> successList, List<String> errorNoList, List<B2CManualDeliveryImportExcelDTO> errorList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        List<String> codeList = successList.stream().map(B2CManualDeliveryImportExcelDTO::getCode).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<SoB2cEntity> soB2cEntityList = this.lambdaQuery().in(SoB2cEntity::getCode, codeList).list();
        if(CollectionUtils.isEmpty(soB2cEntityList)){
            successList.forEach(e->e.setErrorMsg("订单编号在系统中不存在"));
            errorList.addAll(successList);
            return;
        }
        List<String> soIds = soB2cEntityList.stream().map(SoB2cEntity::getId).collect(Collectors.toList());
        List<SoB2cDetailEntity> soB2cDetailEntityList = soB2cDetailService.listByMainIds(soIds);
        List<SoB2cLogisticsEntity> soB2cLogisticsEntityList = soB2cLogisticsService.listByMainIds(soIds);
        List<SoB2cReceiverEntity> soB2cReceiverEntityList = soB2cReceiverService.listByMainIds(soIds);
        List<String> noInventorySkuIdList = plmTaskFeign.getNoInventorySku()
                .stream()
                .map(SkuVO::getSkuId).distinct().collect(Collectors.toList());

        List<String> warehouseNameList = successList.stream().map(B2CManualDeliveryImportExcelDTO::getWarehouseName).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<WarehouseEntity> warehouseEntityList = FeignQuery.create(WarehouseEntity.class).in(WarehouseEntity::getName, warehouseNameList).list();
        if(CollectionUtils.isEmpty(warehouseEntityList)){
            successList.forEach(e->e.setErrorMsg("仓库在系统中不存在"));
            errorList.addAll(successList);
            return;
        }
        List<String> channelNameList = successList.stream().map(B2CManualDeliveryImportExcelDTO::getChannelName).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<LogisticsChannelDTO.BaseDTO> channelEntities = logisticsFeign.listChannelInfoByName(channelNameList);
        Set<String> handleCodes = new HashSet<>();
        List<String> warehouseIds = warehouseEntityList.stream().map(WarehouseEntity::getId).collect(Collectors.toList());

        List<WarehouseDTO.UpdateDTO> updateDTOS = wmsTaskFeign.listWarehouseByIds(warehouseIds);
        List<OverseasProviderWarehouseDTO.ViewDTO> overseasWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIds);
        for (B2CManualDeliveryImportExcelDTO dto : successList) {
            List<String> errorMsgList = new ArrayList<>();

            SoB2cEntity soB2cEntity = soB2cEntityList.stream().filter(e -> StringUtils.isNotBlank(dto.getCode()) && Objects.equals(e.getCode(), dto.getCode())).findFirst().orElse(null);
            if(Objects.isNull(soB2cEntity)){
                errorMsgList.add("订单编号在系统中不存在");
            }
            WarehouseEntity warehouseEntity = warehouseEntityList.stream().filter(e -> StringUtils.isNotBlank(dto.getWarehouseName()) && Objects.equals(e.getName(), dto.getWarehouseName())).findFirst().orElse(null);
            if(Objects.isNull(warehouseEntity)){
                errorMsgList.add("仓库名称在系统中不存在");
            } else if(Boolean.TRUE.equals(warehouseEntity.getDisabled())){
                errorMsgList.add("仓库未启用");
            }
            LogisticsChannelDTO.BaseDTO baseDTO = channelEntities.stream().filter(e -> StringUtils.isNotBlank(dto.getChannelName()) && Objects.equals(e.getName(), dto.getChannelName())).findFirst().orElse(null);
            if(Objects.isNull(baseDTO)){
                errorMsgList.add("物流渠道在系统中不存在");
            } else if(Boolean.TRUE.equals(baseDTO.getDisabled())){
                errorMsgList.add("物流渠道未启用");
            }
            if(CollectionUtils.isNotEmpty(errorMsgList)){
                List<String> itemErrorList = errorMsgList.stream().distinct().collect(Collectors.toList());
                dto.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                errorList.add(dto);
                continue;
            }
            if(Boolean.TRUE.equals(soB2cEntity.getInvalidStatus())){
                errorMsgList.add("订单已作废");
            }
            if(CollectionUtils.isNotEmpty(errorMsgList)){
                List<String> itemErrorList = errorMsgList.stream().distinct().collect(Collectors.toList());
                dto.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                errorList.add(dto);
                continue;
            }
            if(handleCodes.contains(dto.getCode())) {
                errorMsgList.add("订单编号存在重复");
            }
            handleCodes.add(dto.getCode());
            if(CollectionUtils.isNotEmpty(errorMsgList)){
                List<String> itemErrorList = errorMsgList.stream().distinct().collect(Collectors.toList());
                dto.setErrorMsg(FieldValidUtil.getMsgSort(itemErrorList));
                errorList.add(dto);
                continue;
            }
            List<SoB2cDetailEntity> details = soB2cDetailEntityList.stream().filter(e -> Objects.equals(e.getMainId(), soB2cEntity.getId())).collect(Collectors.toList());
            SoB2cLogisticsEntity soB2cLogisticsEntity = soB2cLogisticsEntityList.stream().filter(e -> Objects.equals(e.getMainId(), soB2cEntity.getId())) .findFirst().orElse(null);
            SoB2cReceiverEntity soB2cReceiverEntity = soB2cReceiverEntityList.stream().filter(e -> Objects.equals(e.getMainId(), soB2cEntity.getId())) .findFirst().orElse(null);
            WarehouseDTO.UpdateDTO updateDTO = updateDTOS.stream().filter(e -> Objects.equals(e.getId(), warehouseEntity.getId())).findFirst().orElse(null);
            OverseasProviderWarehouseDTO.ViewDTO overseasWarehouse = overseasWarehouseList.stream().filter(v -> v.getWarehouseId().equals(warehouseEntity.getId())).findFirst().orElse(null);

            //校验通过
            SoB2cDTO.DeliveryWithNotOutboundDTO delivery = SoB2cDTO.DeliveryWithNotOutboundDTO.builder()
                    .id(soB2cEntity.getId())
                    .warehouseId(warehouseEntity.getId())
                    .logisticsChannelId(baseDTO.getId())
                    .deliveryTime(dto.getDeliveryTime())
                    .trackNo(dto.getTransportNo())
                    .actualDeliveryCode(dto.getActualDeliveryCode())
                    .platformShipFlag(!Objects.isNull(dto.getPlatformShipFlag()) && (dto.getPlatformShipFlag().equals("是")))
                    .build();
            try {
                List<BatchResultDTO> resultDTOList = new ArrayList<>();
                soB2cService.lockDeliveryWithNotOutbound(delivery,soB2cEntity,soB2cLogisticsEntity,baseDTO,soB2cReceiverEntity,updateDTO,details,noInventorySkuIdList,overseasWarehouse,resultDTOList);
                if(!resultDTOList.isEmpty() && !resultDTOList.get(0).getSuccess()){
                    String errorMsg = resultDTOList.stream().map(BatchResultDTO::getMsg).filter(StringUtils::isNotBlank).distinct().collect(Collectors.joining(","));
                    dto.setErrorMsg(errorMsg);
                    errorList.add(dto);
                }
            }catch (Exception e){
                log.error("{}导入B2C发货单失败",dto.getCode(), e);
                dto.setErrorMsg("手动出库："+e.getMessage());
                errorList.add(dto);
            }
        }
    }

}

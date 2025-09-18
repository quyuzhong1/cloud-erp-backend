package com.erp.server.tms.service.impl;


import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.json.JSONObject;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.common.business.dto.base.BaseDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.LogisticsPlatformEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.controller.vo.ApiResult;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.tms.dto.LogisticsBillDetailQueryDTO;
import com.erp.model.tms.dto.LogisticsTrackDTO;
import com.erp.model.tms.dto.excel.LogisticsBillCostExcelDTO;
import com.erp.model.tms.dto.excel.LogisticsTrackInfoExcelDTO;
import com.erp.model.tms.entity.LogisticsTrackEntity;
import com.erp.model.tms.enums.FmLogisticTrackStatusEnum;
import com.erp.model.tms.enums.LogisticTrackStatusEnum;
import com.erp.model.tms.vo.request.LogisticsRegisterVO;
import com.erp.model.tms.vo.request.RegisterTrackVO;
import com.erp.model.tms.vo.response.RegisterResponseVO;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.server.tms.convert.TrackDataConverter;
import com.erp.server.tms.handler.LogisticsRegistry;
import com.erp.server.tms.listener.LogisticsBillCostExcelListener;
import com.erp.server.tms.listener.LogisticsLastMileCostExcelListener;
import com.erp.server.tms.listener.LogisticsTrackInfoExcelListener;
import com.erp.server.tms.mapper.LogisticsTrackMapper;
import com.erp.server.tms.service.LogisticsBillDetailService;
import com.erp.server.tms.service.LogisticsService;
import com.erp.server.tms.service.LogisticsTrackService;
import com.erp.server.tms.service.OperateLogService;
import com.google.common.collect.Lists;
import com.sdk.tms.track123.dto.PlatformTrackDTO;
import io.seata.common.util.StringUtils;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.IMPORT_TMS_LOGISTICS_TRACK_INFO;

/**
 * <p>
 * 物流轨迹表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2023-11-14
 */
@Slf4j
@Service
public class LogisticsTrackServiceImpl extends SuperServiceImpl<LogisticsTrackMapper, LogisticsTrackEntity> implements LogisticsTrackService {
    @Resource
    private OperateLogService operateLogService;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private LogisticsRegistry logisticsRegistry;
    private final static DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsTrackDTO.AddDTO addDTO) {
        LogisticsTrackEntity logisticsTrackEntity = new LogisticsTrackEntity();
        BeanMapperUtils.copy(addDTO, logisticsTrackEntity);
        if (CharSequenceUtil.isBlank(logisticsTrackEntity.getTrackNo())){
            return new BaseResultDTO.AddDTO();
        }

        // 数据处理
        handleData(logisticsTrackEntity);

        log.info("开始新增物流轨迹单");
        boolean save = super.save(logisticsTrackEntity);
        if (!save) {
            throw new ServiceException("物流轨迹单保存失败");
        }

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "物流轨迹单", logisticsTrackEntity.getId());
        
        operateLogService.addModuleOperateLog(msg, null, logisticsTrackEntity.getId(), "新增操作");
        

        return new BaseResultDTO.AddDTO(logisticsTrackEntity.getId(), logisticsTrackEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsTrackDTO.UpdateDTO updateDTO) {
        LogisticsTrackEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(() -> new ServiceException(ApiError.NOT_EXIST_BILL, "物流轨迹单"));
        LogisticsTrackEntity logisticsTrackEntity = BeanMapperUtils.map(LogisticsTrackEntity.class, updateDTO);

        // 数据处理
        handleData(logisticsTrackEntity);
        log.info("编辑 开始修改物流轨迹单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsTrackEntity);
        if (!save) {
            throw new ServiceException("物流轨迹单保存失败");
        }
        

        // 记录主单操作日志
        log.info("编辑 开始记录物流轨迹单日志数据，id：【{}】", logisticsTrackEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), logisticsTrackEntity.getId(), "物流轨迹单");
        
        operateLogService.addModuleOperateLogByObj(old, logisticsTrackEntity, null, logisticsTrackEntity.getId(), msg);
        return Boolean.TRUE;
    }


    /**
     * 根据运输单号查询数据
     *
     * @param trackNoList
     * @return
     * @author yl
     * @date 2023-11-16 15:50
     */
    @Override
    public List<LogisticsTrackEntity> listByTrackNoList(List<String> trackNoList) {
        if (CollectionUtils.isEmpty(trackNoList)) {
            return Collections.emptyList();
        }
        return this.lambdaQuery().in(LogisticsTrackEntity::getTrackNo, trackNoList).orderByDesc(LogisticsTrackEntity::getTrackTime).list();
    }

    @Override
    public LogisticsTrackDTO.ViewDTO listByTrackNo(String trackNo) {
        if(StringUtils.isBlank(trackNo)){
            return new LogisticsTrackDTO.ViewDTO();
        }
        LogisticsTrackDTO.ViewDTO viewDTO = new LogisticsTrackDTO.ViewDTO();
        viewDTO.setTrackNo(trackNo);

        List<LogisticsTrackEntity> list = this.listByTrackNoList(Arrays.asList(trackNo));
        List<LogisticsTrackDTO.ListDTO> resultList = BeanMapperUtils.copyList(LogisticsTrackDTO.ListDTO.class, list);
        int size = resultList.size();
        for (int i = 0; i < size; i++) {
            LogisticsTrackDTO.ListDTO item = resultList.get(i);
            if (i == 0) {
                item.setIsLatest(Boolean.TRUE);
            } else {
                item.setIsLatest(Boolean.FALSE);

            }
            String status = item.getStatus();
            String statusName = LogisticTrackStatusEnum.getName(status);
            if (CharSequenceUtil.isBlank(statusName)){
                FmLogisticTrackStatusEnum fmLogisticTrackStatusEnum = FmLogisticTrackStatusEnum.getNameByCode(status);
                statusName = Objects.nonNull(fmLogisticTrackStatusEnum) ?fmLogisticTrackStatusEnum.getName() :CharSequenceUtil.EMPTY;
            }
            item.setStatusName(statusName);
        }
        viewDTO.setList(resultList);
        return viewDTO;
    }

    @Override
    @Async("tmsExecutor")
    public void processTrackData(PlatformTrackDTO dto){
        log.info(CharSequenceUtil.format("-------记录【{}】物流轨迹开始------", dto.getTrackNo()));
        if (CharSequenceUtil.isBlank(dto.getTrackNo()) || CollectionUtils.isEmpty(dto.getDetails())){
            return;
        }
        List<LogisticsTrackEntity> newList = TrackDataConverter.INSTANCE.platformToTrack(dto.getDetails());
        //设置唯一值
        newList.forEach(e-> {e.setTrackNo(dto.getTrackNo());e.setMd5(getDataMd5(e));});
        //增量数据库记录
        this.saveIncrementTrackData(dto.getTrackNo(), newList);
        //获取最新记录
        LogisticsTrackEntity maxTrack = newList.stream().max(Comparator.comparing(LogisticsTrackEntity::getTrackTime)).orElse(null);
        //根据跟踪号进行更新操作
        logisticsBillDetailService.updateLogisticsBillDetailByTrackNo(maxTrack);
        log.info(CharSequenceUtil.format("-------记录【{}】物流轨迹结束------", dto.getTrackNo()));

    }

    @Override
    public void updateBeforeThreeMonthTrackNo(LogisticsBillDetailQueryDTO query) {
        List<LogisticsTrackDTO.UpdateTrackDTO> dtoList = baseMapper.listBeforeThreeMonthTrack(query);
        if (CollectionUtils.isEmpty(dtoList)){
            return;
        }
        List<String> trackNoList = dtoList.stream().map(LogisticsTrackDTO.UpdateTrackDTO::getTrackNo).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(trackNoList)){
            return;
        }
        String code = LogisticTrackStatusEnum.SYSTEM_COMPLETE.getCode();
        //集合分区
        List<List<String>> partition = Lists.partition(trackNoList, MathUtil.NUMBER_100);
        partition.forEach(e -> logisticsBillDetailService.batchUpdateTrackStatus(e,code,null, null));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void webhookByTrack123(LogisticsTrackDTO.TrackWebHookDTO dto) {
        if (Objects.isNull(dto.getData()) || CharSequenceUtil.isBlank(dto.getData().getTrackNo()) || Objects.isNull(dto.getData().getLocalLogisticsInfo())
                || CollectionUtils.isEmpty(dto.getData().getLocalLogisticsInfo().getTrackingDetails())){
            log.info("webhook接收到数据格式无数据记录：{}", dto);
            return;
        }
        String trackNo = dto.getData().getTrackNo();
        String transitStatus = dto.getData().getTransitStatus();
        String trackStatus = convertTrackStatus(transitStatus);
        //轨迹明细
        List<LogisticsTrackDTO.TrackingDetail> trackingDetails = dto.getData().getLocalLogisticsInfo().getTrackingDetails();
        //数据转换
        List<LogisticsTrackEntity> newList = TrackDataConverter.INSTANCE.convertWebHookToEntity(trackingDetails);
        //设置唯一值
        newList.forEach(e-> {e.setTrackNo(trackNo);e.setMd5(getDataMd5(e));});
        //获取最新记录
        LogisticsTrackEntity maxTrack = newList.stream().max(Comparator.comparing(LogisticsTrackEntity::getTrackTime)).orElse(null);
        //增量数据库记录
        this.saveIncrementTrackData(trackNo, newList);
        //根据跟踪号进行更新操作
        if (Objects.nonNull(maxTrack)){
            maxTrack.setOrderStatus(trackStatus);
            logisticsBillDetailService.updateLogisticsBillDetailByTrackNo(maxTrack);
        }
    }

    @Override
    public void saveIncrementTrackData(String trackNo, List<LogisticsTrackEntity> newList) {
        if (CharSequenceUtil.isBlank(trackNo) || CollectionUtils.isEmpty(newList)){
            return;
        }
        List<LogisticsTrackEntity> oldList = this.listByTrackNoList(Collections.singletonList(trackNo));
        if (CollectionUtils.isEmpty(oldList)){
            this.saveBatch(newList);
        }else {
            List<String> md5List = oldList.stream().map(LogisticsTrackEntity::getMd5).filter(StrUtil::isNotBlank).distinct().collect(Collectors.toList());
            List<LogisticsTrackEntity> noExistList = newList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getMd5()) && !md5List.contains(e.getMd5())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(noExistList)){
                this.saveBatch(noExistList);
            }
        }
    }

    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        downloadTaskFeign.saveImportTask("导入物流单信息", IMPORT_TMS_LOGISTICS_TRACK_INFO.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public void importLogisticsTrackInfo(BaseDTO.ImportDTO dto) {
        LogisticsTrackInfoExcelListener excelListenerUtil = new LogisticsTrackInfoExcelListener(dto.getTaskId(),dto.getImportType(),dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), LogisticsTrackInfoExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        //导出错误数据
        List<LogisticsTrackInfoExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "物流轨迹错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, LogisticsTrackInfoExcelDTO.class);
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
    public void handleImportSuccessList(List<LogisticsTrackInfoExcelDTO> successList, List<LogisticsTrackInfoExcelDTO> errorList, String importType) {
        if (CollectionUtils.isEmpty(successList)){
            return;
        }
        LogisticsService service = logisticsRegistry.getHandler(LogisticsPlatformEnum.TRACK123.getCode());
        List<Map<String, String>> mapList = service.getLogisticsAuthConfigByPlatform(LogisticsPlatformEnum.TRACK123.getCode());
        List<LogisticsRegisterVO> logisticsRegisterVOS = new ArrayList<>();
        successList.forEach(e -> {
            logisticsRegisterVOS.add(LogisticsRegisterVO.builder()
                    .trackNo(e.getTrackNo())
                    .phoneSuffix(e.getMobile())
                    .courierCode(e.getChannelName())
                    .build());
        });
        RegisterTrackVO registerTrackVO = RegisterTrackVO.builder().authMap(mapList.get(0)).logisticsRegisterVOS(logisticsRegisterVOS).build();
        service.updateTrack(registerTrackVO);
    }

    /**
     * 获取唯一值
     * @param trackingDetail
     * @return
     */
    private String getDataMd5(LogisticsTrackEntity trackingDetail) {
        String trackTime = trackingDetail.getTrackTime().format(TIME_FORMAT);
        return DigestUtil.md5Hex(trackingDetail.getTrackNo() + "-" + trackingDetail.getContent() + "-" + trackTime);
    }


    /**
     * 新增修改处理数据
     */
    private void handleData(LogisticsTrackEntity logisticsTrackEntity) {
        logisticsTrackEntity.setMd5(getDataMd5(logisticsTrackEntity));

    }
    private String convertTrackStatus(String transitSubStatus) {
        if (StringUtils.isBlank(transitSubStatus)) {//待查询
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        } else if (transitSubStatus.contains("INIT")) {//待查询  单号正在查询中，请等待
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        } else if (transitSubStatus.contains("NO_RECORD")) {//暂无信息 包裹无法查询到物流轨迹信息
            return LogisticTrackStatusEnum.NOT_FIND.getCode();
        } else if (transitSubStatus.contains("INFO_RECEIVED")) {//已接收 物流公司已经收到寄运订单，正在准备揽收包裹
            return LogisticTrackStatusEnum.WAIT_COLLECT.getCode();
        } else if (transitSubStatus.contains("IN_TRANSIT")) {//运输中 包裹正在运输途中
            return LogisticTrackStatusEnum.TRACK_ING.getCode();
        } else if (transitSubStatus.contains("WAITING_DELIVERY")) {//派送中 包裹正在派送或已到达代收点等待收件人自提
            return LogisticTrackStatusEnum.DELIVERY_ING.getCode();
        } else if (transitSubStatus.contains("DELIVERY_FAILED")) {//投递失败 包裹尝试派送，但由于地址问题、收件人联系不上等原因导致派送失败
            return LogisticTrackStatusEnum.DELIVERY_FAIL.getCode();
        } else if (transitSubStatus.contains("ABNORMAL")) {//异常 包裹出现破损、退件、海关扣留等异常情况
            return LogisticTrackStatusEnum.MAYBE_EXCEPTION.getCode();
        } else if (transitSubStatus.contains("DELIVERED")) {//已成功 包裹投递成功
            return LogisticTrackStatusEnum.SIGN.getCode();
        } else if (transitSubStatus.contains("EXPIRED")) {//已过期 包裹在最近的30天没有任何物流更新
            return LogisticTrackStatusEnum.TRANSPORT_LONG.getCode();
        }
        return LogisticTrackStatusEnum.NOT_FIND.getCode();
    }
}

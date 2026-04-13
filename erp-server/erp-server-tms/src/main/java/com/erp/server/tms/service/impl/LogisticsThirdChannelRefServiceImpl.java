package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.PlatformDictEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDetailDTO;
import com.erp.model.tms.dto.excel.ImportLogisticsThirdChannelRefExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.LogisticsThirdChannelRefPushTypeEnum;
import com.erp.model.tms.enums.TrackPlatformTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.convert.LogisticsThirdChannelRefConverter;
import com.erp.server.tms.listener.LogisticsThirdChannelRefListener;
import com.erp.server.tms.mapper.LogisticsThirdChannelRefMapper;
import com.erp.server.tms.service.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.core.exception.ServiceException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.tms.dto.LogisticsThirdChannelRefDTO;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

import static com.common.business.threadlocal.UserContext.getDefaultLoginUser;

/**
 * <p>
 * 物流-第三方渠道关系表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2025-05-29
 */
@Slf4j
@Service
public class LogisticsThirdChannelRefServiceImpl extends SuperServiceImpl<LogisticsThirdChannelRefMapper, LogisticsThirdChannelRefEntity> implements LogisticsThirdChannelRefService {
    @Autowired
    private OperateLogService operateLogService;
    @Resource
    private LogisticsThirdChannelRefDetailService logisticsThirdChannelRefDetailService;
    @Resource
    private LogisticsSupplierService logisticsSupplierService;
    @Resource
    private LogisticsChannelService logisticsChannelService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Resource
    private LogisticsBillDetailService logisticsBillDetailService;
    @Resource
    private BasicQueryLogisticsProviderService basicQueryLogisticsProviderService;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(LogisticsThirdChannelRefDTO.AddDTO addDTO) {
        LogisticsThirdChannelRefEntity logisticsThirdChannelRefEntity = new LogisticsThirdChannelRefEntity();
        BeanMapperUtils.copy(addDTO, logisticsThirdChannelRefEntity);
        List<LogisticsThirdChannelRefDetailEntity> detailList = new ArrayList<>();
        if (CollUtil.isNotEmpty(addDTO.getDetailList())){
            detailList = BeanMapperUtils.copyList(LogisticsThirdChannelRefDetailEntity.class,addDTO.getDetailList());
        }
        // 数据处理
        handleData(logisticsThirdChannelRefEntity,detailList);

        log.info("开始新增物流-第三方渠道关系单");
        boolean save = super.save(logisticsThirdChannelRefEntity);
        if(!save) {
            throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_SAVE_FAILED);
        }

        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据我司渠道为【{}】", getDefaultLoginUser().getUserName(), "物流-第三方渠道关系单" , logisticsThirdChannelRefEntity.getLogisticsChannelName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_THIRD_CHANNEL_REF.getCode(), logisticsThirdChannelRefEntity.getId(), "新增操作");
        // 新增明细（如果有明细的话）
        logisticsThirdChannelRefDetailService.updateDetail(logisticsThirdChannelRefEntity,detailList);
        return new BaseResultDTO.AddDTO(logisticsThirdChannelRefEntity.getId(), logisticsThirdChannelRefEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(LogisticsThirdChannelRefDTO.UpdateDTO addOrUpdateDTO) {
        LogisticsThirdChannelRefEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "物流-第三方渠道关系单"));
        LogisticsThirdChannelRefEntity logisticsThirdChannelRefEntity =  BeanMapperUtils.map(LogisticsThirdChannelRefEntity.class, addOrUpdateDTO);
        List<LogisticsThirdChannelRefDetailEntity> detailList = new ArrayList<>();
        if (CollUtil.isNotEmpty(addOrUpdateDTO.getDetailList())){
            detailList = BeanMapperUtils.copyList(LogisticsThirdChannelRefDetailEntity.class,addOrUpdateDTO.getDetailList());
        }
        // 数据处理
        handleData(logisticsThirdChannelRefEntity, detailList);
        log.info("编辑 开始修改物流-第三方渠道关系单数据，id：【{}】", old.getId());
        boolean save = super.updateById(logisticsThirdChannelRefEntity);
        if(!save) {
            throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_SAVE_FAILED);
        }
        //修改明细数据（包含增删改）（如果有明细的话）
        logisticsThirdChannelRefDetailService.updateDetail(logisticsThirdChannelRefEntity,detailList);
        // 记录主单操作日志
        log.info("编辑 开始记录物流-第三方渠道关系单日志数据，id：【{}】", logisticsThirdChannelRefEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑我司渠道为【{}】的【{}】单据 ", getDefaultLoginUser().getUserName(), logisticsThirdChannelRefEntity.getLogisticsChannelName(), "物流-第三方渠道关系单");
        operateLogService.addModuleOperateLogByObj(old, logisticsThirdChannelRefEntity, ModuleTypeEnum.LOGISTICS_THIRD_CHANNEL_REF.getCode(), logisticsThirdChannelRefEntity.getId(), msg);
        return Boolean.TRUE;
    }

    @Override
    public List<LogisticsThirdChannelRefEntity> listByChannelIds(List<String> channelIds) {
        if (CollUtil.isEmpty(channelIds)){
            return Collections.emptyList();
        }
        return this.lambdaQuery()
                .in(LogisticsThirdChannelRefEntity::getLogisticsChannelId, channelIds).list();
    }

    @Override
    public PagingVO<LogisticsThirdChannelRefDTO.PagingVO> paging(PagingDTO<LogisticsThirdChannelRefDTO.PagingParamDTO> dto) {
        Page<Object> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        LogisticsThirdChannelRefDTO.PagingParamDTO params = dto.getParams();
        IPage<LogisticsThirdChannelRefDTO.PagingVO> pageData = baseMapper.paging(query, params);
        fillData(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    private void fillData(List<LogisticsThirdChannelRefDTO.PagingVO> records) {
        if (CollUtil.isEmpty(records)){
            return;
        }
        records.forEach(e ->{
            e.setPlatformTypeName(TrackPlatformTypeEnum.getName(e.getPlatformType()));
            e.setPushMobileName(e.getIsPushMobile() ? "是" : "否");
            e.setPushTypeName(LogisticsThirdChannelRefPushTypeEnum.getName(e.getPushType()));
            e.setDisabledName(e.getDisabled() ? "是" : "否");
            e.setDictPlatformName(PlatformDictEnum.getNameByCode(e.getDictPlatform()));
            e.setMainDictPlatformName(PlatformDictEnum.getNameByCode(e.getMainDictPlatform()));
            e.setPlatformShopName(CharSequenceUtil.isNotBlank(e.getDictPlatformName()) ? e.getDictPlatformName() : "" + (CharSequenceUtil.isNotBlank(e.getShopName()) ? e.getShopName():""));
        });
    }

    @Override
    public LogisticsThirdChannelRefDTO.ViewDTO view(String id) {
        LogisticsThirdChannelRefEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_NOT_FOUND));
        LogisticsThirdChannelRefDTO.ViewDTO data = BeanMapperUtils.map(LogisticsThirdChannelRefDTO.ViewDTO.class, entity);
        data.setPlatformTypeName(TrackPlatformTypeEnum.getName(entity.getPlatformType()));
        data.setPushTypeName(LogisticsThirdChannelRefPushTypeEnum.getName(entity.getPushType()));
        data.setDictPlatformName(PlatformDictEnum.getNameByCode(entity.getDictPlatform()));
        //查询实际明细
        List<LogisticsThirdChannelRefDetailEntity> detailEntityList = logisticsThirdChannelRefDetailService.listByMainIdList(Collections.singletonList(data.getId()));
        if (CollectionUtils.isNotEmpty(detailEntityList)) {
            List<LogisticsThirdChannelRefDetailDTO.ViewDTO> viewDTOS = BeanMapperUtils.copyList(LogisticsThirdChannelRefDetailDTO.ViewDTO.class, detailEntityList);
            viewDTOS.forEach(viewDTO -> {
                viewDTO.setDictPlatformName(PlatformDictEnum.getNameByCode(viewDTO.getDictPlatform()));
            });
            data.setDetailList(viewDTOS);
        }
        return data;
    }

    @Override
    public Boolean export(LogisticsThirdChannelRefDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("物流-第三方渠道关系表导出", FileTaskEventEnum.EXPORT_TMS_LOGISTICS_THIRD_CHANNEL_REF.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(LogisticsThirdChannelRefEntity entity) {
        //检查是否引用
        Integer count = logisticsBillDetailService.countByThirdRefId(entity.getId());
        if (count > 0){
            return BatchResultDTO.fail(entity.getLogisticsChannelName(), entity.getLogisticsSupplierName(),ApiError.LOGISTICS_THIRD_CHANNEL_IN_USE_DELETE_FORBIDDEN.getMsg());
        }
        //主表
        this.removeById(entity.getId());
        //明细
        logisticsThirdChannelRefDetailService.removeByMainId(entity.getId());
        return BatchResultDTO.success(entity.getLogisticsChannelName(), entity.getLogisticsSupplierName(),"操作成功");
    }

    @Override
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        LogisticsThirdChannelRefEntity entity = this.getById(id);
        if (Objects.isNull(entity)){
            return BatchResultDTO.fail(id, id, ApiError.LOGISTICS_THIRD_CHANNEL_NOT_FOUND.getMsg());
        }
        if (entity.getDisabled().equals(disabled)){
            return BatchResultDTO.fail(id, entity.getLogisticsSupplierName(), ApiError.LOGISTICS_THIRD_CHANNEL_STATUS_UNCHANGED.getMsg());
        }
        this.lambdaUpdate().set(LogisticsThirdChannelRefEntity::getDisabled, disabled).eq(LogisticsThirdChannelRefEntity::getId, id).update();
        String msg = CharSequenceUtil.format("用户【{}】变更我司渠道为【{}】的【{}】单据状态为【{}】", UserContext.getDefaultLoginUser().getUserName(), entity.getLogisticsChannelName(), "物流-第三方渠道关系单", disabled ? "停用" : "启用");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LOGISTICS_THIRD_CHANNEL_REF.getCode(), entity.getId(), "启用/停用");
        return BatchResultDTO.success(entity.getLogisticsChannelName(),entity.getLogisticsSupplierName(), "操作成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        List<LogisticsSupplierEntity> logisticsSupplierEntities = logisticsSupplierService.lambdaQuery().list();
        Map<String, LogisticsChannelEntity> logisticsChannelMap = logisticsChannelService.list().stream().collect(Collectors.toMap(LogisticsChannelEntity::getName, Function.identity(), (o1, o2) -> o1));
        Map<String, BasicQueryLogisticsProviderEntity> queryLogisticsProviderMap = basicQueryLogisticsProviderService.list().stream().collect(Collectors.toMap(BasicQueryLogisticsProviderEntity::getLogisticsNameCn, Function.identity(), (o1, o2) -> o1));
        LogisticsThirdChannelRefListener excelListener = new LogisticsThirdChannelRefListener(logisticsSupplierEntities,logisticsChannelMap,queryLogisticsProviderMap);
        try {
            EasyExcel.read(excelFile.getInputStream(), ImportLogisticsThirdChannelRefExcelDTO.class, excelListener).sheet(0).doRead();
        } catch (Exception e) {
            log.error("物流-第三方渠道关系单导入错误", e);
            return Boolean.FALSE;
        }
        //导入数据处理
        List<ImportLogisticsThirdChannelRefExcelDTO> successList = excelListener.getSuccessList();
        //导出错误数据
        List<ImportLogisticsThirdChannelRefExcelDTO> errorList = excelListener.getErrorList();
        //处理校验导入成功数据
        handleImportFile(successList, errorList);

        if (!errorList.isEmpty()) {
            String fileName = "物流-第三方渠道关系单导入错误信息";
            ExcelUtil.export(fileName, "导入异常", errorList, ImportLogisticsThirdChannelRefExcelDTO.class, response);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleImportFile(List<ImportLogisticsThirdChannelRefExcelDTO> successList, List<ImportLogisticsThirdChannelRefExcelDTO> errorList) {
        //组装入库数据
        Map<String, List<ImportLogisticsThirdChannelRefExcelDTO>> groupMap = successList.stream().collect(Collectors.groupingBy(ImportLogisticsThirdChannelRefExcelDTO::getSerialNumber));
        for (Map.Entry<String, List<ImportLogisticsThirdChannelRefExcelDTO>> entry : groupMap.entrySet()) {
            List<ImportLogisticsThirdChannelRefExcelDTO> list = entry.getValue();
            ImportLogisticsThirdChannelRefExcelDTO importLogisticsThirdChannelRefExcelDTO = list.get(0);
            LogisticsThirdChannelRefDTO.AddDTO dto = LogisticsThirdChannelRefConverter.INSTANCE.convertImportToAddDTO(importLogisticsThirdChannelRefExcelDTO);
            List<LogisticsThirdChannelRefDetailDTO.AddDTO> detailList = LogisticsThirdChannelRefConverter.INSTANCE.convertImportToAddDetailDTO(list);
            if (LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode().equals(dto.getPushType())|| LogisticsThirdChannelRefPushTypeEnum.PLATFORM_SENDER.getCode().equals(dto.getPushType())){
                dto.setDetailList(detailList);
            }else if (LogisticsThirdChannelRefPushTypeEnum.ORDER_RECEIVER.getCode().equals(dto.getPushType())){
                dto.setDetailList(Collections.emptyList());
            }else {
                LogisticsThirdChannelRefDetailDTO.AddDTO addDTO = detailList.get(0);
                addDTO.setMobile(importLogisticsThirdChannelRefExcelDTO.getShopPhone());
                dto.setDetailList(Collections.singletonList(addDTO));
            }
            try {
                this.add(dto);
            }catch (Exception e){
                list.forEach(f -> f.setErrorMsg(e.getMessage()));
                errorList.addAll(list);
            }
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/logisticsThirdChannelRefTemplate.xlsx";
        String excelName = "第三方渠道关系导入.xlsx";
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        try {
            InputStream inputStream = resourceLoader.getResource(path).getInputStream();
            XSSFWorkbook wb = new XSSFWorkbook(inputStream);
            // 输出Excel文件
            OutputStream output = response.getOutputStream();
            response.reset();
            // 设置文件头
            response.setHeader("Content-Disposition",
                    "attchement;filename=" + new String(excelName.getBytes("gb2312"), StandardCharsets.ISO_8859_1));
            response.setContentType("application/msexcel");
            wb.write(output);
            wb.close();
        } catch (Exception e) {
            throw new ServiceException(ApiError.HTTP_UNKNOWN);
        }
    }

    @Override
    public List<LogisticsThirdChannelRefDTO.PagingVO> listByPlatform(String platformType) {
        return baseMapper.listByPlatform(platformType);
    }

    @Override
    public List<LogisticsThirdChannelRefDTO.PagingVO> listByChannelId(String channelId) {
        return baseMapper.listByChannelId(channelId);
    }
    /**
    * 新增修改处理数据
    */
    private void handleData(LogisticsThirdChannelRefEntity logisticsThirdChannelRefEntity, List<LogisticsThirdChannelRefDetailEntity> detailList) {
        //同一个查询服务商下我司物流商+渠道，查询物流商+渠道仅可创建一条
        Integer count = this.lambdaQuery().eq(LogisticsThirdChannelRefEntity::getPlatformType, logisticsThirdChannelRefEntity.getPlatformType())
                .eq(LogisticsThirdChannelRefEntity::getLogisticsSupplierId, logisticsThirdChannelRefEntity.getLogisticsSupplierId())
                .eq(LogisticsThirdChannelRefEntity::getLogisticsChannelId, logisticsThirdChannelRefEntity.getLogisticsChannelId())
                .eq(LogisticsThirdChannelRefEntity::getDictPlatform, logisticsThirdChannelRefEntity.getDictPlatform())
                .ne(CharSequenceUtil.isNotBlank(logisticsThirdChannelRefEntity.getId()), LogisticsThirdChannelRefEntity::getId, logisticsThirdChannelRefEntity.getId())
                .count();
        if (count > 0) {
            throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_DUPLICATE,
                    logisticsThirdChannelRefEntity.getLogisticsSupplierName(), logisticsThirdChannelRefEntity.getLogisticsChannelName());
        }

        if(Objects.equals(logisticsThirdChannelRefEntity.getLogisticsSupplierId() ,"all")){
            logisticsThirdChannelRefEntity.setLogisticsSupplierName("所有");
        }else {
            LogisticsSupplierEntity supplierEntity = logisticsSupplierService.getById(logisticsThirdChannelRefEntity.getLogisticsSupplierId());
            if (Objects.nonNull(supplierEntity)) {
                logisticsThirdChannelRefEntity.setLogisticsSupplierName(supplierEntity.getShortName());
            } else {
                throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_SUPPLIER_NOT_FOUND);
            }
        }

        if(Objects.equals(logisticsThirdChannelRefEntity.getLogisticsChannelId() ,"all")){
            logisticsThirdChannelRefEntity.setLogisticsChannelCode("all");
            logisticsThirdChannelRefEntity.setLogisticsChannelName("所有");
        }else {
            LogisticsChannelEntity logisticsChannelEntity = logisticsChannelService.getById(logisticsThirdChannelRefEntity.getLogisticsChannelId());
            if (Objects.nonNull(logisticsChannelEntity) && Objects.equals(logisticsChannelEntity.getMainId(), logisticsThirdChannelRefEntity.getLogisticsSupplierId())) {
                logisticsThirdChannelRefEntity.setLogisticsChannelCode(logisticsChannelEntity.getCode());
                logisticsThirdChannelRefEntity.setLogisticsChannelName(logisticsChannelEntity.getName());
            } else {
                throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_CHANNEL_NOT_FOUND);
            }
        }

        String thirdSupplierName = logisticsThirdChannelRefEntity.getThirdSupplierName();
        BasicQueryLogisticsProviderEntity basicQueryLogisticsProviderEntity = basicQueryLogisticsProviderService.lambdaQuery().eq(BasicQueryLogisticsProviderEntity::getLogisticsNameCn, thirdSupplierName).last(" limit 1 ").one();
        if(Objects.isNull(basicQueryLogisticsProviderEntity)){
            throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_QUERY_PROVIDER_NOT_FOUND, thirdSupplierName);
        }else {
            logisticsThirdChannelRefEntity.setThirdChannelName(basicQueryLogisticsProviderEntity.getCompanyCode());
            logisticsThirdChannelRefEntity.setThirdSupplierCode(basicQueryLogisticsProviderEntity.getLogisticsNameEn());

            Boolean isRegisterPhone = basicQueryLogisticsProviderEntity.getIsRegisterPhone();
            Boolean isPushMobile = logisticsThirdChannelRefEntity.getIsPushMobile();
            String pushType = logisticsThirdChannelRefEntity.getPushType();

            // 校验推送手机号配置规则
            validatePushMobileConfig(isRegisterPhone, isPushMobile, pushType);
        }

        // 验证数据 & 数据赋值
        if (logisticsThirdChannelRefEntity.getPushType().equals(LogisticsThirdChannelRefPushTypeEnum.SENDER.getCode()) || logisticsThirdChannelRefEntity.getPushType().equals(LogisticsThirdChannelRefPushTypeEnum.RECEIVER.getCode())) {
            if (logisticsThirdChannelRefEntity.getIsPushMobile()) {
                //手机号必填
                detailList.stream().filter(detail -> StrUtil.isBlank(detail.getMobile())).forEach(detail -> {
                    throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_MOBILE_REQUIRED);
                });
            }
        } else if (logisticsThirdChannelRefEntity.getPushType().equals(LogisticsThirdChannelRefPushTypeEnum.SHOP_SENDER.getCode())) {
            if (logisticsThirdChannelRefEntity.getIsPushMobile()) {
                //店铺Id必填
                detailList.stream().filter(detail -> StrUtil.isBlank(detail.getShopId())).forEach(detail -> {
                    throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_SHOP_ID_REQUIRED);
                });
            }

        } else if (logisticsThirdChannelRefEntity.getPushType().equals(LogisticsThirdChannelRefPushTypeEnum.PLATFORM_SENDER.getCode())) {
            if (logisticsThirdChannelRefEntity.getIsPushMobile()) {
                //平台必填
                detailList.stream().filter(detail -> StrUtil.isBlank(detail.getDictPlatform())).forEach(detail -> {
                    throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_PLATFORM_REQUIRED);
                });
            }
        } else if (logisticsThirdChannelRefEntity.getPushType().equals(LogisticsThirdChannelRefPushTypeEnum.ORDER_RECEIVER.getCode())) {
            if (CollUtil.isNotEmpty(detailList)) {
                throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_DETAIL_NOT_REQUIRED);
            }
        }
    }

    /**
     * 校验推送手机号配置规则
     * 业务规则:
     * 1. 如果物流商要求注册手机号(isRegisterPhone=true),则必须开启推送手机号,且推送类型必填
     * 2. 如果物流商不要求注册手机号(isRegisterPhone=false),但开启了推送手机号,则推送类型必填
     *
     * @param isRegisterPhone 物流商是否要求注册手机号
     * @param isPushMobile 是否开启推送手机号
     * @param pushType 推送类型
     */
    private void validatePushMobileConfig(Boolean isRegisterPhone, Boolean isPushMobile, String pushType) {
        // 规则1: 物流商要求注册手机号时,必须开启推送
        if (Boolean.TRUE.equals(isRegisterPhone) && !Boolean.TRUE.equals(isPushMobile)) {
            throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_PUSH_MOBILE_IMMUTABLE);
        }

        // 规则2: 开启推送手机号时,推送类型必填
        if (Boolean.TRUE.equals(isPushMobile) && StringUtils.isBlank(pushType)) {
            throw new ServiceException(ApiError.LOGISTICS_THIRD_CHANNEL_PUSH_TYPE_REQUIRED);
        }
    }


    @Override
    public Boolean existRefBySalePlatform(String salePlatform, String channelId, String logisticsSupplierId) {
        Boolean flag = false;
        List<LogisticsThirdChannelRefEntity> logisticsThirdChannelRefEntities = baseMapper.existRefBySalePlatform(salePlatform, channelId, logisticsSupplierId);
        if(CollUtil.isNotEmpty(logisticsThirdChannelRefEntities)){
            flag = true;
        }
        return flag;
    }

    @Override
    public List<LogisticsThirdChannelRefDTO.ListByTrackNosDTO> listByTrackNos(List<String> trackNos) {
        if (CollUtil.isEmpty(trackNos)) {
            return Collections.emptyList();
        }
        return baseMapper.listByTrackNos(trackNos);
    }
}

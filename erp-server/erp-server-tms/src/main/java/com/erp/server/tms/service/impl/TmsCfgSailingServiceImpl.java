package com.erp.server.tms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapper;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.dto.excel.SupplierVisitImportExcelDTO;
import com.erp.model.scm.entity.SupplierEntity;
import com.erp.model.tms.dto.TmsCfgSailingDTO;
import com.erp.model.tms.dto.excel.TmsCfgSailingExcelDTO;
import com.erp.model.tms.entity.DictBasicEntity;
import com.erp.model.tms.entity.LogisticsChannelEntity;
import com.erp.model.tms.entity.LogisticsSupplierEntity;
import com.erp.model.tms.entity.TmsCfgSailingEntity;
import com.erp.model.tms.enums.DictBasicEnum;
import com.erp.model.tms.enums.TmsCfgSailingDateTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.listener.TmsCfgSailingExcelListener;
import com.erp.server.tms.mapper.TmsCfgSailingMapper;
import com.erp.server.tms.service.DictBasicService;
import com.erp.server.tms.service.LogisticsChannelService;
import com.erp.server.tms.service.LogisticsSupplierService;
import com.erp.server.tms.service.TmsCfgSailingService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_CFG_SAILING;

/**
 * <p>
 * 截单开船配置 服务实现类
 * </p>
 *
 * @author will
 * @since 2024-03-15
 */
@Slf4j
@Service
public class TmsCfgSailingServiceImpl extends SuperServiceImpl<TmsCfgSailingMapper, TmsCfgSailingEntity> implements TmsCfgSailingService {

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private LogisticsSupplierService logisticsSupplierService;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(TmsCfgSailingDTO.AddDTO addDTO) {
        TmsCfgSailingEntity tmsCfgSailingEntity = new TmsCfgSailingEntity();
        BeanMapperUtils.copy(addDTO, tmsCfgSailingEntity);

        // 数据处理
        List<TmsCfgSailingEntity> resultList =   handleData(tmsCfgSailingEntity,addDTO.getLogisticsChannelIdList(),Boolean.TRUE);

        log.info("开始新增截单开船配置");
        boolean save = super.saveBatch(resultList);
        if(!save) {
            throw new ServiceException("截单开船配置保存失败");
        }

        return new BaseResultDTO.AddDTO(tmsCfgSailingEntity.getId(), tmsCfgSailingEntity.getId());
    }

    /**
    * 修改
    */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(TmsCfgSailingDTO.UpdateDTO updateDTO) {
        TmsCfgSailingEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "截单开船配置"));
        TmsCfgSailingEntity tmsCfgSailingEntity =  BeanMapperUtils.map(TmsCfgSailingEntity.class, updateDTO);

        log.info("编辑 开始修改截单开船配置数据，id：【{}】", old.getId());
        boolean save = super.updateById(tmsCfgSailingEntity);
        if(!save) {
            throw new ServiceException("截单开船配置保存失败");
        }
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<TmsCfgSailingDTO.ListDTO> paging(PagingDTO<TmsCfgSailingDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page query = new Page(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<TmsCfgSailingDTO.ListDTO> pageData = this.baseMapper.paging(query, pagingParamDTO.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    public TmsCfgSailingDTO.ViewDTO view(String id) {
        TmsCfgSailingEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到截单发船数据"));
        TmsCfgSailingDTO.ViewDTO data = BeanMapperUtils.map(TmsCfgSailingDTO.ViewDTO.class, entity);
        data.setLogisticsChannelIdList(Arrays.asList(entity.getLogisticsChannelId()));
        return data;
    }

    @Override
    public BatchResultDTO delete(String id) {
        TmsCfgSailingEntity entity = super.getByIdOpt(id).orElseThrow(()->new ServiceException("未找到截单发船数据"));
        // 删除主单数据
        log.info("删除 开始删除截单发船数据，id：【{}】", id);
        this.removeById(id);
        return BatchResultDTO.success(entity.getId(), entity.getId(), OperationTypeEnum.DELETE);
    }

    @Override
    public  List<TmsCfgSailingEntity> getByLogisticsChannelIdList (List<String> logisticsChannelIdList) {
        List<TmsCfgSailingEntity> list = lambdaQuery()
                .in(TmsCfgSailingEntity::getLogisticsChannelId, logisticsChannelIdList)
                .list();
        return list;
    }

    @Override
    public LocalDateTime calculateShipTime(String logisticsChannelId, LocalDateTime orderTime) {
        if(StringUtils.isBlank(logisticsChannelId) || orderTime == null){
            return null;
        }
        List<TmsCfgSailingEntity> list = getByLogisticsChannelIdList(Arrays.asList(logisticsChannelId));
        if(CollectionUtil.isEmpty(list)) {
            return null;
        }
        TmsCfgSailingEntity entity = list.get(0);
        //生效时间大于下单时间，开船时间为空
        if(entity.getEffectiveDate().atStartOfDay().isAfter(orderTime)) {
            return null;
        }
        return calculateShipTime(entity.getEffectiveDate().atStartOfDay(), entity.getDateValue(), entity.getDateType(), entity.getStartDate(), entity.getStartTime(), orderTime,entity.getEndDate(),entity.getEndTime(),true,true);
    }

    /**
     * 获取目标时间下一个开船日
     * 逻辑：获取起始时间开始后的第一个开船日，判断下单日如果在开船日之前并且在接单日之后，则舍弃这个开船日，返回下个开船日，否则返回这个开船日
     * @return
     */
    private  LocalDateTime calculateShipTime(LocalDateTime effectiveTime,Integer sailingInterval, String dateType, Integer startDate, LocalTime startTime, LocalDateTime orderTime, Integer endDate,LocalTime endTime,Boolean isFirst,Boolean isNext) {
        LocalDateTime nextTargetDay;
        if(dateType.equals(TmsCfgSailingDateTypeEnum.WEEK.getCode())){
            if(effectiveTime.isAfter(orderTime)){
                LocalDateTime endDay = effectiveTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.of(endDate))).with(endTime);
                if(orderTime.isAfter(endDay) && isNext){
                    return this.calculateShipTime(effectiveTime.plusWeeks(sailingInterval),sailingInterval,dateType,startDate,startTime,orderTime,  endDate,endTime,  false,false);
                }else{
                    return effectiveTime;
                }
            }
            //判断有效日期的周几与开船周几，如果小于， 则开船日期等于本周的开船日期，如果等于，则开船日期为当天 + 开船时间 如果大于，则下一个开船日期等于本周的开船日期+开船间隔
            // 获取当前时间是星期几
            DayOfWeek dayOfWeek = effectiveTime.getDayOfWeek();
            // 获取星期几的数值表示，1 表示星期一，7 表示星期日
            int dayOfWeekValue = dayOfWeek.getValue();
            if(dayOfWeekValue < startDate){
                nextTargetDay = effectiveTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(startDate)));
            }else {
                if(isFirst){
                    nextTargetDay = effectiveTime.with(TemporalAdjusters.nextOrSame(DayOfWeek.of(startDate)));
                }else{
                    nextTargetDay = effectiveTime.with(TemporalAdjusters.previousOrSame(DayOfWeek.of(startDate))).plusWeeks(sailingInterval);
                }
            }
            //如果下单日期在截单日和开船日之间，则从下一个开船日开始计算
            nextTargetDay = nextTargetDay.with(startTime);
            if(nextTargetDay.isAfter(orderTime)){
                LocalDateTime endDay = nextTargetDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.of(endDate))).with(endTime);
                if(orderTime.isAfter(endDay) && isNext){
                    return this.calculateShipTime(nextTargetDay.plusWeeks(sailingInterval),sailingInterval,dateType,startDate,startTime,orderTime,  endDate,endTime,  false,false);
                }else{
                    return nextTargetDay;
                }
            }else{
                return this.calculateShipTime(nextTargetDay.plusWeeks(sailingInterval),sailingInterval,dateType,startDate,startTime,orderTime,  endDate,endTime,  false,true);
            }
        }else if (dateType.equals(TmsCfgSailingDateTypeEnum.MONTH.getCode())){
            if(effectiveTime.isAfter(orderTime)){
                LocalDateTime endDay = effectiveTime.withDayOfMonth(endDate).with(endTime);
                if(orderTime.isAfter(endDay) && isNext){
                    return this.calculateShipTime(effectiveTime.plusWeeks(sailingInterval),sailingInterval,dateType,startDate,startTime,orderTime,  endDate,endTime,  false,false);
                }else{
                    return effectiveTime;
                }
            }
            //逻辑与上面相似
            int dayOfMonth = effectiveTime.getDayOfMonth();
            if (dayOfMonth < startDate) {
                nextTargetDay = effectiveTime.withDayOfMonth(startDate);
            } else {
                if(isFirst){
                    nextTargetDay = effectiveTime.withDayOfMonth(startDate);
                }else{
                    nextTargetDay = effectiveTime.plusMonths(sailingInterval).withDayOfMonth(startDate);
                }
            }
            nextTargetDay = nextTargetDay.with(startTime);
            if (nextTargetDay.isAfter(orderTime)) {
                LocalDateTime endDay = nextTargetDay.withDayOfMonth(endDate).with(endTime);
                if(orderTime.isAfter(endDay) && isNext){
                    return this.calculateShipTime(nextTargetDay.plusMonths(sailingInterval), sailingInterval, dateType, startDate, startTime, orderTime,  endDate,endTime,  false,false);
                }else{
                    return nextTargetDay;
                }
            } else {
                return this.calculateShipTime(nextTargetDay.plusMonths(sailingInterval), sailingInterval, dateType, startDate, startTime, orderTime,  endDate,endTime,  false,true);
            }
        }
        return null;
    }
    /**
    * 新增修改处理数据
    */
    private List<TmsCfgSailingEntity> handleData(TmsCfgSailingEntity tmsCfgSailingEntity,List<String> logisticsChannelIdList,Boolean isAdd) {
        List<TmsCfgSailingEntity> resultList = new ArrayList<>();
        List<LogisticsChannelEntity> logisticsChannelList = logisticsChannelService.listByIds(logisticsChannelIdList);

        //查询原信息
        List<TmsCfgSailingEntity> oldList = getByLogisticsChannelIdList(logisticsChannelIdList);

        for (String  logisticsChannelId : logisticsChannelIdList) {
            TmsCfgSailingEntity entity = new TmsCfgSailingEntity();
            BeanMapperUtils.copy(tmsCfgSailingEntity,entity);
            LogisticsChannelEntity channelEntity = logisticsChannelList.stream().filter(obj -> CharSequenceUtil.equals(logisticsChannelId, obj.getId())).findFirst().orElse(new LogisticsChannelEntity());
            entity.setLogisticsSupplierId(channelEntity.getMainId());
            entity.setLogisticsChannelId(logisticsChannelId);
            //新增校验是否重复
            TmsCfgSailingEntity old = oldList.stream().filter(obj -> CharSequenceUtil.equals(obj.getLogisticsChannelId(), logisticsChannelId)).findFirst().orElse(null);
            if (isAdd && ObjectUtil.isNotEmpty(old)) {
                throw new ServiceException(ApiError.ERROR_CFG_SAILING_EXIST,channelEntity.getName());
            }
            resultList.add(entity);
        }
        return resultList;
    }

    /**
     * 分页查询处理数据
     */
    private void fillList(List<TmsCfgSailingDTO.ListDTO> list) {
       if (CollectionUtil.isEmpty(list)) {
           return;
       }
       //物流商信息
       List<String> logisticsSupplierIdList = list.stream().map(TmsCfgSailingDTO.ListDTO::getLogisticsSupplierId).collect(Collectors.toList());
        List<LogisticsSupplierEntity> logisticsSupplierList = logisticsSupplierService.listByIds(logisticsSupplierIdList);
        //渠道信息
       List<String> logisticsChannelIdList = list.stream().map(TmsCfgSailingDTO.ListDTO::getLogisticsChannelId).collect(Collectors.toList());
       List<LogisticsChannelEntity> logisticsChannelList = logisticsChannelService.listByIds(logisticsChannelIdList);

        List<DictBasicEntity> dictList  = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.WEEK.getType(), DictBasicEnum.MONTH.getType(),"dateType"));

        DateTimeFormatter dataForamt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (TmsCfgSailingDTO.ListDTO listDTO : list) {

           //物流商名称
           String supplierName = logisticsSupplierList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getLogisticsSupplierId()))
                   .findFirst().flatMap(obj -> Optional.ofNullable(obj.getSupplierName())).orElse("");
           listDTO.setLogisticsSupplierName(supplierName);
           //物流渠道
           String logisticsChannelName = logisticsChannelList.stream().filter(obj -> CharSequenceUtil.equals(obj.getId(), listDTO.getLogisticsChannelId()))
                   .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
           listDTO.setLogisticsChannelName(logisticsChannelName);

           //开船日期
            String startDateName = dictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), listDTO.getDateType()) && CharSequenceUtil.equals(obj.getCode(), listDTO.getStartDate().toString()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setStartDateName(startDateName);
            //截单日期
            String endDateName = dictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), listDTO.getDateType()) && CharSequenceUtil.equals(obj.getCode(), listDTO.getEndDate().toString()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setEndDateName(endDateName);

            //日期类型
            String dateTypeName = dictList.stream().filter(obj -> CharSequenceUtil.equals(obj.getType(), "dateType") && CharSequenceUtil.equals(obj.getCode(), listDTO.getDateType()))
                    .findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            listDTO.setDateTypeName(dateTypeName);

            if(Objects.nonNull(listDTO.getEffectiveDate())){
                String effectiveDateStr = listDTO.getEffectiveDate().format(dataForamt);
                listDTO.setEffectiveDateStr(effectiveDateStr);
            }
       }

    }

    @Override
    public void exportList(TmsCfgSailingDTO.PagingParamDTO param, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("截单开船导出", EXPORT_TMS_CFG_SAILING.getCode(), param);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importFile(MultipartFile excelFile, HttpServletResponse response) {
        //物流商信息
        List<LogisticsSupplierEntity> logisticsSupplierList = logisticsSupplierService.list();
        //渠道信息
        List<LogisticsChannelEntity> logisticsChannelList = logisticsChannelService.list();

        List<DictBasicEntity> dictList  = dictBasicService.getByKeyList(Arrays.asList(DictBasicEnum.WEEK.getType(), DictBasicEnum.MONTH.getType(),"dateType"));

        TmsCfgSailingExcelListener excelListenerUtil = new TmsCfgSailingExcelListener(logisticsSupplierList,logisticsChannelList,dictList);
        try {
            EasyExcel.read(excelFile.getInputStream(), TmsCfgSailingExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (Exception e) {
            log.error("导入截单开船错误！", e);
            return Boolean.FALSE;
        }
        List<TmsCfgSailingExcelDTO> errorList = excelListenerUtil.getErrorList();
        if (errorList.size() > 0) {
            String excelPath = "excel/tmsCfgSailingError.xlsx";
            String name = "tmsCfgSailingError";
            try {
                new ExcelPrintUtils().patchExport(errorList,
                        response,
                        StrUtil.builder().append(DateUtil.nowExcelFileFormat()).append(name).toString(),
                        excelPath);
            } catch (IOException e) {
                throw new ServiceException(ApiError.ERROR_95125);
            }
        }

        List<TmsCfgSailingEntity> successList = excelListenerUtil.getSuccessList();
        if(CollUtil.isNotEmpty(successList)){
            List<String> logisticsSupplierIds = successList.stream().map(TmsCfgSailingEntity::getLogisticsSupplierId).distinct().collect(Collectors.toList());

            //查询原信息
            List<TmsCfgSailingEntity> oldList = lambdaQuery()
                    .in(TmsCfgSailingEntity::getLogisticsSupplierId, logisticsSupplierIds)
                    .list();

            for (TmsCfgSailingEntity tmsCfgSailingEntity : successList) {
                TmsCfgSailingEntity oldEntity = oldList.stream()
                        .filter(e -> Objects.equals(e.getLogisticsSupplierId(), tmsCfgSailingEntity.getLogisticsSupplierId()) && Objects.equals(e.getLogisticsChannelId(), tmsCfgSailingEntity.getLogisticsChannelId()))
                        .findFirst()
                        .orElse(null);
                if(Objects.isNull(oldEntity)){ //新增
                    save(tmsCfgSailingEntity);
                }else { //更新
                    tmsCfgSailingEntity.setId(oldEntity.getId());
                    BeanMapper.copy(tmsCfgSailingEntity,oldEntity);
                    updateById(oldEntity);
                }
            }
        }
        return Boolean.TRUE;
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/tmsCfgSailingTemplate.xlsx";
        String excelName = "template.xlsx";
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
            log.error("warehouse downloadTemplate  出错了 e==", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }


}

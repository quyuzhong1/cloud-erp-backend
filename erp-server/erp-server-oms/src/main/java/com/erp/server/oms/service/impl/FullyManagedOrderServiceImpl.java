package com.erp.server.oms.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.date.DateUtil;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.*;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.CfgSettingEnum;
import com.erp.model.oms.enums.DictBasicTypeEnum;
import com.erp.model.oms.enums.FullyManagedTabEnum;
import com.erp.server.oms.convert.B2cOrderConverter;
import com.erp.server.oms.listener.*;
import com.erp.server.oms.mapper.SoB2cMapper;
import com.erp.server.oms.query.FullyManagedQueryHandler;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Qualifier;
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
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * @author zdy
 * @ClassName FullyManagedOrderServiceImpl
 * @description: 全托管订单服务
 * @date 2025年03月25日
 * @version: 1.0
 */
@Slf4j
@Service
public class FullyManagedOrderServiceImpl extends SuperServiceImpl<SoB2cMapper, SoB2cEntity> implements FullyManagedOrderService {
    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;
    @Resource
    @Qualifier("soB2cTabExecutorPool")
    private ExecutorService soB2cTabExecutorPool;
    @Resource
    private FullyManagedQueryHandler fullyManagedQueryHandler;
    @Resource
    private CfgSettingService cfgSettingService;
    @Resource
    private DictBasicService dictBasicService;
    @Resource
    private SoB2cService soB2cService;
    
    @Override
    public List<SoB2cDTO.TabListDTO> fullyManagedTabList(PermissionsDTO param) {
        FullyManagedTabEnum[] values = FullyManagedTabEnum.values();
        List<Future<SoB2cDTO.TabListDTO>> futureList = new ArrayList<>();
        List<SoB2cDTO.TabListDTO> list = new ArrayList<>();
        SoB2cDTO.ShopAuthResultDTO shopAuthResultDTO = handleShopSysUserAuth();
        for (FullyManagedTabEnum item : values) {
            Future<SoB2cDTO.TabListDTO> submit = soB2cTabExecutorPool.submit(() -> {
                SoB2cDTO.PagingParamDTO searchParamDTO = new SoB2cDTO.PagingParamDTO();
                searchParamDTO.setPermissionSql(param.getPermissionSql());
                SoB2cDTO.TabListDTO resultDTO = new SoB2cDTO.TabListDTO();
                String tabSql = fullyManagedQueryHandler.getTabSql(item.getCode());
                HashMap<String,String> map = new HashMap<>();
                map.put("default",tabSql);
                searchParamDTO.setSqlMap(map);
                //查询店铺设置权限
                Integer count;
                if (ObjectUtil.isEmpty(shopAuthResultDTO)) {
                    count = MathUtil.ZERO;
                } else {
                    count = this.baseMapper.listFullManagedCount(searchParamDTO, shopAuthResultDTO);
                }
                resultDTO.setCount(ObjectUtils.isEmpty(count) ? MathUtil.ZERO : count);
                resultDTO.setTabFlag(item.getCode());
                resultDTO.setTabFlagName(item.getName());
                return resultDTO;
            });
            futureList.add(submit);
        }
        for(Future<SoB2cDTO.TabListDTO> f : futureList) {
            try {
                list.add(f.get());
            } catch (InterruptedException e) {
                // 恢复线程的中断状态，确保中断标志不会被忽略
                Thread.currentThread().interrupt();
                log.error("线程被中断", e);
                throw new ServiceException("线程被中断", e);
            } catch (ExecutionException e) {
                log.error("线程任务执行异常", e);
                throw new ServiceException("线程任务执行异常", e.getCause());
            } catch (ThreadDeath td) {
                log.error("捕获到 ThreadDeath，线程终止", td);
                throw td; // 重新抛出以允许线程正常终止
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void timeOutConfig(CfgSettingDTO.TimeOutSettingDTO timeOutSettingDTO) {
        //检查预警设置是否存在 更新配置
        CfgSettingEntity setting = cfgSettingService.getSettingByKey(CfgSettingEnum.TIME_OUT_CONFIG.getCode());
        if (Objects.isNull(setting)) {
            setting = new CfgSettingEntity();
        }
        setting.setKey(CfgSettingEnum.TIME_OUT_CONFIG.getCode());
        setting.setValue(JSONUtil.parseObj(timeOutSettingDTO).toString());
        cfgSettingService.saveOrUpdate(setting);
        //修改全托管订单的预警时间
        List<DictBasicDTO.ViewDTO> dtoList = dictBasicService.getByKey(DictBasicTypeEnum.FULLY_MANAGED.getType());
        List<String> platformList = dtoList.stream().map(DictBasicDTO.ViewDTO::getValue).filter(CharSequenceUtil::isNotBlank).collect(Collectors.toList());
        //更新全托管订单的预警时间
        this.baseMapper.updateTimeOutConfig(platformList,timeOutSettingDTO.getWarningTime().multiply(new BigDecimal(60)).intValue());
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        String path = "classpath:excel/fullyManagedOrderTemplate.xlsx";
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
            throw new ServiceException(ApiError.DEFAULT);
        }
    }

    @Override
    public Boolean importExcel(MultipartFile excelFile, HttpServletResponse response) {
        FullyManagedImportExcelListener excelListenerUtil = new FullyManagedImportExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), FullyManagedImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            //错误的
            List<FullyManagedImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            //数据验证
            List<FullyManagedImportExcelDTO> successList = excelListenerUtil.getSuccessList();
            //处理验证成功数据
            handleImportSuccessList(successList, errorList);
            if (errorList.size() > 0) {
                StringBuffer sb = new StringBuffer();
                String excelPath = "excel/fullyManagedOrderError.xlsx";
                String name = "fullyManaged";
                String date = DateUtil.conversionDate(new Date(), DateUtil.DATE_PATTERN_SHORT_YEAR_NO_SP);
                sb.append(date);
                sb.append(name);
                try {
                    new ExcelPrintUtils().patchExport(errorList, response, sb.toString(), excelPath);
                } catch (IOException e) {
                    throw new ServiceException(ApiError.ERROR_95125);
                }
                return Boolean.FALSE;
            }
        } catch (SocketTimeoutException e) {
            log.error("导入超时错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_IMPORT_TIMEOUT);
        } catch (IOException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入错误！>>>{}", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        return Boolean.TRUE;

    }

    private void handleImportSuccessList(List<FullyManagedImportExcelDTO> successList, List<FullyManagedImportExcelDTO> errorList) {
        //上游已经将数据处理完成 现在开始执行导入
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //根据平台单号进行分组
        Map<String, List<FullyManagedImportExcelDTO>> collect = successList.stream().collect(Collectors.groupingBy(e ->e.getPlatformCode() + e.getDictPlatform()));
        //遍历分组数据
        collect.forEach((key, value) -> {
            SoB2cDTO.AddDTO addDTO = buildAddDTO(key,value,errorList);
            if (Objects.nonNull(addDTO)){
                try {
                    SoB2cEntity soB2cEntity = soB2cService.add(addDTO, null);
                    //匹配订单规则
                    SoB2cDTO.RuleResultDTO orderRuleResult = soB2cService.orderRule(soB2cEntity.getId());
                    //匹配成功
                    if (orderRuleResult.getIsRuleMatch() && orderRuleResult.getIsPass()) {
                        //仓库规则
                        SoB2cDTO.RuleResultDTO warehouseRuleResult = soB2cService.warehouseRule(orderRuleResult.getId(), orderRuleResult.getSoB2cDetailList(), orderRuleResult.getMap());
                        Boolean warehouseRuleMatch = warehouseRuleResult.getIsRuleMatch();
                        if (warehouseRuleMatch) {
                            SoB2cDTO.RuleResultDTO logisticsRuleResult = soB2cService.logisticsRule(soB2cEntity.getId(), new HashMap<>(), false);
                            SoB2cEntity entity = soB2cService.getById(soB2cEntity.getId());
                            if ((Objects.nonNull(logisticsRuleResult.getAutoGetTrackNo()) && Boolean.TRUE.equals(logisticsRuleResult.getAutoGetTrackNo()))
                                    || (Boolean.FALSE.equals(entity.getIsOutOfRangeDelivery()) && Objects.nonNull(logisticsRuleResult.getAutoGetTrackNotOfRangeDelivery()) && Boolean.TRUE.equals(logisticsRuleResult.getAutoGetTrackNotOfRangeDelivery()))) {
                                soB2cService.getLogisticsCode(soB2cEntity.getId(),  Boolean.TRUE);
                            }
                        }
                    }
                    //自动计算预估运费到订单的预估运费字段
                    soB2cService.autoCalcEstimatedShippingCost(Collections.singletonList(soB2cEntity.getId()));
                }catch (Exception e){
                    log.error("导入失败！>>>{}", e);
                    value.forEach(f -> f.setErrorMsg(CharSequenceUtil.format("平台【{}】平台订单号【{}】新增失败：【{}】",f.getDictPlatformName(), f.getPlatformCode(), e.getMessage())));
                    errorList.addAll(value);
                }
            }
        });
    }

    /**
     * 构建导入数据
     * @param key = platformCode + dictPlatform
     * @param value
     * @param errorList
     * @return
     */
    private SoB2cDTO.AddDTO buildAddDTO(String key, List<FullyManagedImportExcelDTO> value, List<FullyManagedImportExcelDTO> errorList) {
        //平台列表
        SoB2cEntity entity = this.lambdaQuery().eq(SoB2cEntity::getPlatformCode, value.get(0).getPlatformCode()).eq(SoB2cEntity::getDictPlatform, value.get(0).getDictPlatform()).one();
        if (ObjectUtil.isNotEmpty(entity)) {
            value.forEach(e -> e.setErrorMsg(CharSequenceUtil.format("平台【{}】平台订单号【{}】销售订单已存在【{}】",e.getDictPlatformName(), e.getPlatformCode(), entity.getCode())));
            errorList.addAll(value);
            return null;
        }
        if (value.size() > 1){
            //判断每个导入列中字段值是否一致
            boolean flag = value.stream()
                    .allMatch(e -> e.getPrice().compareTo(value.get(0).getPrice()) == 0
                            && e.getCurrencyCode().equals(value.get(0).getCurrencyCode())
                            && e.getPayTime().equals(value.get(0).getPayTime())
                            && e.getOrderSourceType().equals(value.get(0).getOrderSourceType())
                    );
            if (!flag){
                value.forEach(e -> e.setErrorMsg(CharSequenceUtil.format("平台【{}】平台订单号【{}】中订单金额/币别/下单时间/平台来源需要一致",e.getDictPlatformName(), e.getPlatformCode(), entity.getCode())));
                errorList.addAll(value);
                return null;
            }
        }
        SoB2cDTO.AddDTO addDTO = B2cOrderConverter.INSTANCE.convertFullyManagedExcelDTO(value.get(0));
        addDTO.setExtendDTO(B2cOrderConverter.INSTANCE.convertFullyManagedExtendDTO(value.get(0)));
        addDTO.setDetailList(B2cOrderConverter.INSTANCE.convertFullyManagedDetailDTO(value));
        addDTO.setLogisticsDTO(B2cOrderConverter.INSTANCE.convertFullyManagedLogisticsDTO(value.get(0)));
        return addDTO;
    }

    /**
     * 查询店铺权限设置
     */
    private SoB2cDTO.ShopAuthResultDTO handleShopSysUserAuth() {
        SoB2cDTO.ShopAuthResultDTO resultDTO = new SoB2cDTO.ShopAuthResultDTO();
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        if (ObjectUtil.isEmpty(userInfo) || StringUtils.isBlank(userInfo.getUid())) {
            return null;
        }
        List<ShopSysUserAuthDTO.ViewDTO> list = shopSysUserAuthService.listShopSysUserAuthByUserIdList(Arrays.asList(userInfo.getUid()));
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        resultDTO.setUserId(userInfo.getUid());
        resultDTO.setAuthType(list.get(0).getAuthType());
        return resultDTO;
    }
}

package com.erp.server.tms.service.impl;


import cn.hutool.core.lang.Pair;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.dto.base.PermissionsDTO;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.excel.ExcelPrintUtils;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.FieldValidUtil;
import com.common.core.utils.date.DateUtil;
import com.common.core.utils.date.LocalDateUtil;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.CurrencyDTO;
import com.erp.model.sys.entity.DictCountryEntity;
import com.erp.model.tms.dto.*;
import com.erp.model.tms.dto.excel.ShippingTemplateCityExcelDTO;
import com.erp.model.tms.dto.excel.ShippingTemplateExcelDTO;
import com.erp.model.tms.entity.*;
import com.erp.model.tms.enums.*;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.sys.feign.SysDictFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.tms.listener.ShippingTemplateCityExcelListener;
import com.erp.server.tms.listener.ShippingTemplateExcelListener;
import com.erp.server.tms.mapper.ShippingTemplateMapper;
import com.erp.server.tms.mapper.ShippingTemplateRuleMapper;
import com.erp.server.tms.service.*;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_SHIPPING_TEMPLATE;

/**
 * <p>
 * 运费模板 服务实现类
 * </p>
 *
 * @author Will
 * @since 2023-11-03
 */
@Slf4j
@Service
public class ShippingTemplateServiceImpl extends SuperServiceImpl<ShippingTemplateMapper, ShippingTemplateEntity> implements ShippingTemplateService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private ShippingTemplateRefChannelService shippingTemplateRefChannelService;

    @Resource
    private ShippingTemplateRuleService shippingTemplateRuleService;

    @Resource
    private ShippingTemplateRuleMapper shippingTemplateRuleMapper;

    @Resource
    private ShippingTemplateOtherCostService shippingTemplateOtherCostService;

    @Resource
    private ShippingRegionCityService shippingRegionCityService;

    @Resource
    private ShippingTemplateCostSettingService shippingTemplateCostSettingService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private LogisticsChannelService logisticsChannelService;

    @Resource
    private SysUserFeign sysUserFeign;

    @Resource
    private SysDictFeign sysDictFeign;

    @Resource
    private ShippingCalculationService shippingCalculationService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;
    @Override
    public List<ShippingTemplateDTO.TabListDTO> tabList(PermissionsDTO dto) {
        List<ShippingTemplateDTO.TabListDTO> dbList = baseMapper.tabList(dto.getPermissionSql());
        List<ShippingTemplateDTO.TabListDTO> resultList = new LinkedList<>();
        // 全部
        resultList.add(new ShippingTemplateDTO.TabListDTO("all", dbList.size(), "全部"));
        // 启用
        long trueCount = dbList.stream()
                .filter(e -> "f".equalsIgnoreCase(e.getTabFlag()))
                .map(ShippingTemplateDTO.TabListDTO::getCount)
                .findFirst()
                .orElse(0);
        resultList.add(new ShippingTemplateDTO.TabListDTO("false", (int) trueCount, "启用"));
        // 停用
        long falseCount = dbList.stream()
                .filter(e -> "t".equalsIgnoreCase(e.getTabFlag()))
                .map(ShippingTemplateDTO.TabListDTO::getCount)
                .findFirst()
                .orElse(0);
        resultList.add(new ShippingTemplateDTO.TabListDTO("true", (int) falseCount, "停用"));
        return resultList;
    }

    @Override
    public PagingVO<ShippingTemplateDTO.ListDTO> paging(PagingDTO<ShippingTemplateDTO.PagingParamDTO> pagingDTO) {
        ShippingTemplateDTO.PagingParamDTO params = pagingDTO.getParams();
        params.setPermissionSql(pagingDTO.getPermissionSql());
        Page query = new Page(pagingDTO.getCurrPage(), pagingDTO.getPageSize());
        IPage<ShippingTemplateDTO.ListDTO> pageData = this.baseMapper.paging(query, params);
        List<ShippingTemplateDTO.ListDTO> records = pageData.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return new PagingVO(pageData);
        }
        //数据赋值处理
        doOpHandleShippingTemplate(records);
        return new PagingVO(pageData);
    }


    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(ShippingTemplateDTO.AddDTO addDTO) {
        ShippingTemplateEntity shippingTemplateEntity = new ShippingTemplateEntity();
        BeanMapperUtils.copy(addDTO, shippingTemplateEntity);

        // 数据处理
        handleData(shippingTemplateEntity);

        log.info("开始新增运费模板");
        boolean save = super.save(shippingTemplateEntity);
        if(!save) {
            throw new ServiceException("运费模板保存失败");
        }

        //新增运费规则
        shippingTemplateRuleService.add(addDTO.getDetailList(),shippingTemplateEntity.getId());
        //新增其他费用
        shippingTemplateOtherCostService.add(addDTO.getOtherCostList(),shippingTemplateEntity.getId());

        // 操作日志
        String msg = CharSequenceUtil.format("用户【{}】新增【{}】单据名称为【{}】", UserContext.getDefaultLoginUser().getUserName(), "运费模板" , shippingTemplateEntity.getName());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), shippingTemplateEntity.getId(), "新增操作");

        return new BaseResultDTO.AddDTO(shippingTemplateEntity.getId(), shippingTemplateEntity.getId());
    }

    /**
     * 修改
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(ShippingTemplateDTO.UpdateDTO updateDTO) {
        ShippingTemplateEntity old = super.getById(updateDTO.getId());
        Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));
        ShippingTemplateEntity shippingTemplateEntity =  BeanMapperUtils.map(ShippingTemplateEntity.class, updateDTO);

        // 数据处理
        handleData(shippingTemplateEntity);
        log.info("编辑 开始修改运费模板数据，id：【{}】", old.getId());
        boolean save = super.updateById(shippingTemplateEntity);
        if(!save) {
            throw new ServiceException("运费模板保存失败");
        }
        //修改运费规则
        shippingTemplateRuleService.update(updateDTO.getDetailList(),shippingTemplateEntity.getId());
        //修改其他费用
        shippingTemplateOtherCostService.update(updateDTO.getOtherCostList(),shippingTemplateEntity.getId());

        // 记录主单操作日志
        log.info("编辑 开始记录运费模板日志数据，id：【{}】", shippingTemplateEntity.getId());
        String msg = CharSequenceUtil.format("用户【{}】编辑名称为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), shippingTemplateEntity.getName(), "运费模板");

        operateLogService.addModuleOperateLogByObj(old, shippingTemplateEntity, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), shippingTemplateEntity.getId(), msg);
        return Boolean.TRUE;
    }


    @Override
    public BigDecimal trialCalculation(ShippingTemplateDTO.TrialCalculationParamDTO dto) {
        ShippingTemplateEntity entity = super.getById(dto.getId());
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));
        ShippingTemplateTypeEnum typeEnum = ShippingTemplateTypeEnum.getEnumByCode(entity.getType());
        switch (typeEnum) {
            case ENUM_COUNTRY:
                //按国家
                return calculationByCountry(dto,entity);
            case ENUM_REGION:
                //按分区
                return calculationByRegion(dto,entity);
            case ENUM_WAREHOUSE:
                //按仓库
                return calculationByWarehouse(dto,entity);
            default:
                return BigDecimal.ZERO;
        }
    }

    @Override
    public ShippingTemplateDTO.ViewDTO view(String id) {
        ShippingTemplateEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));
        ShippingTemplateDTO.ViewDTO viewDTO = BeanMapperUtils.map(ShippingTemplateDTO.ViewDTO.class, entity);

        //运费规则
        List<ShippingTemplateRuleEntity> ruleList = shippingTemplateRuleService.listByMainId(id);
        List<ShippingTemplateRuleDTO.ViewDTO> detailList = BeanMapperUtils.copyList(ShippingTemplateRuleDTO.ViewDTO.class, ruleList);
        //格式化运费规则数据
        handleRuleView(detailList,entity);
        viewDTO.setDetailList(detailList);

        //其他费用
        List<ShippingTemplateOtherCostEntity> costList = shippingTemplateOtherCostService.listByMainId(id);
        List<ShippingTemplateOtherCostDTO.ViewDTO> otherCostList = BeanMapperUtils.copyList(ShippingTemplateOtherCostDTO.ViewDTO.class, costList);
        handleOtherCost(otherCostList);
        viewDTO.setOtherCostList(otherCostList);
        return viewDTO;
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateChannel(ShippingTemplateDTO.ChannelParamDTO dto) {
        ShippingTemplateEntity entity = super.getById(dto.getId());
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));
        List<ShippingTemplateRefChannelDTO.AddDTO> addList = new ArrayList<>();
        for (String logisticsChannelId : dto.getLogisticsChannelIdList()) {
            ShippingTemplateRefChannelDTO.AddDTO addDTO = new ShippingTemplateRefChannelDTO.AddDTO();
            addDTO.setMainId(dto.getId());
            addDTO.setLogisticsChannelId(logisticsChannelId);
            addList.add(addDTO);
        }
        //添加渠道关联关系
        shippingTemplateRefChannelService.add(addList,dto.getId());

        //渠道信息
        String channelNames = "";
        if (CollectionUtils.isNotEmpty(dto.getLogisticsChannelIdList())) {
            List<LogisticsChannelEntity> channelList = logisticsChannelService.listByIds(dto.getLogisticsChannelIdList());
            channelNames = channelList.stream().map(LogisticsChannelEntity::getName).collect(Collectors.joining(","));
        }
        //添加日志
        String msg = CharSequenceUtil.format("用户【{}】应用渠道【{}】", UserContext.getDefaultLoginUser().getUserName(),  channelNames);
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), entity.getId(), "应用渠道操作");
        return Boolean.TRUE;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id,Boolean disabled) {
        ShippingTemplateEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));

        List<ShippingTemplateRefChannelDTO.ViewDTO> refList = shippingTemplateRefChannelService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(refList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_DISABLED);
        }

        lambdaUpdate().eq(ShippingTemplateEntity::getId, id)
                .set(ShippingTemplateEntity::getDisabled, disabled)
                .update();

        // 启用/停用日志数据
        log.info("启用/停用 开始启用/停用运费模板单日志数据，id集合：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】运费模板【{}】的【{}】单据{}操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getName(), "运费模板",disabled ? "停用" : "启用");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), entity.getId(), "启用/停用");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DISABLED);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        ShippingTemplateEntity entity = super.getById(id);
        Optional.ofNullable(entity).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "运费模板"));

        List<ShippingTemplateRefChannelDTO.ViewDTO> refList = shippingTemplateRefChannelService.listByMainIds(Arrays.asList(id));
        if (CollectionUtils.isNotEmpty(refList)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_DELETE);
        }

        //删除规则
        shippingTemplateRuleService.deleteByMainId(id);
        //删除其他费用
        shippingTemplateOtherCostService.deleteByMainId(id);
        // 删除主单数据
        removeById(id);
        // 删除日志数据
        log.info("删除 开始删除运费模板单日志数据，id集合：【{}】", id);
        String msg = CharSequenceUtil.format("用户【{}】名称【{}】的【{}】单据删除操作 ", UserContext.getDefaultLoginUser().getUserName(), entity.getName(), "运费模板");
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SHIPPING_TEMPLATE.getCode(), entity.getId(), "删除运费模板单数据");
        return BatchResultDTO.success(entity.getId(), entity.getName(), OperationTypeEnum.DELETE);
    }

    @Override
    public void downloadTemplate(HttpServletResponse response,String billingMethod,String type) {
        String path = getTemplatePath( billingMethod,  type);

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
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean importFile(String billingMethod, String type, MultipartFile excelFile, HttpServletResponse response) {
        List<Pair<Integer,List<?>>> pairList = new ArrayList<>();
        //获取第一页数据
        ShippingTemplateExcelListener excelListenerUtil = listenerFirstSheet(excelFile);
        //获取第二页数据
        ShippingTemplateCityExcelListener cityExcelListenerUtil = listenerSecondSheet(type, excelFile);
        List<ShippingTemplateCityExcelDTO> cityErrorList = new ArrayList<>();
        List<ShippingTemplateCityExcelDTO> citySuccessList = new ArrayList<>();
        if (ObjectUtil.isNotEmpty(cityExcelListenerUtil)) {
            List<ShippingTemplateCityExcelDTO> excelDateList = cityExcelListenerUtil.getExcelDateList();
            if (CollectionUtils.isEmpty(excelDateList)) {
                throw new ServiceException(ApiError.ERROR_IMPORT_DATA_NOT_NULL,"分区城市");
            }
            cityErrorList = cityExcelListenerUtil.getErrorList();
            citySuccessList = cityExcelListenerUtil.getSuccessList();
        }
        List<ShippingTemplateExcelDTO> excelDateList = excelListenerUtil.getExcelDateList();
        if (CollectionUtils.isEmpty(excelDateList)) {
            throw new ServiceException(ApiError.ERROR_95123,"基础数据");
        }
        List<ShippingTemplateExcelDTO > errorList = excelListenerUtil.getErrorList();

        List<ShippingTemplateExcelDTO> successList = excelListenerUtil.getSuccessList();
        //处理验证成功数据
        handleImportSuccessList(billingMethod,type,successList, errorList,cityErrorList,citySuccessList);
        //根据传入参数获取模板地址
        String excelPath =  getExportErrorExcelPath(billingMethod,type);

        pairList.add(new Pair<>(0,errorList));
        pairList.add(new Pair<>(1,cityErrorList));
        if (errorList.size() > 0) {
            StringBuffer sb = new StringBuffer();
            String name = "ShippingTemplateError";
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
        return Boolean.TRUE;
    }


    @Override
    public List<ShippingTemplateOtherCostDTO.ViewDTO> viewOtherCost() {

        ShippingCostNameEnum[] values = ShippingCostNameEnum.values();
        //折扣下拉
        List<DictBasicDTO.ViewDTO> discountList = dictBasicService.getByKey(ShippingCostNameEnum.DISCOUNT_RATE.getCode());
        //燃油下拉
        List<DictBasicDTO.ViewDTO> fuelSurchargeList = dictBasicService.getByKey(ShippingCostNameEnum.FUEL_SURCHARGE_RATE.getCode());
        //边长下拉
        List<DictBasicDTO.ViewDTO> sideList = dictBasicService.getByKey(ShippingCalculationMethodEnum.ENUM_SIDE.getCode());
        //票下拉
        List<DictBasicDTO.ViewDTO> voteList = dictBasicService.getByKey(ShippingCalculationMethodEnum.ENUM_VOTE.getCode());

        List<ShippingTemplateOtherCostDTO.ViewDTO> list = new ArrayList<>();
        for (ShippingCostNameEnum shippingCostNameEnum : values) {
            ShippingTemplateOtherCostDTO.ViewDTO viewDTO = new ShippingTemplateOtherCostDTO.ViewDTO();
            viewDTO.setDictCode(shippingCostNameEnum.getCode());
            viewDTO.setDictName(shippingCostNameEnum.getName());
            viewDTO.setCalculationMethod(shippingCostNameEnum.getType());
            viewDTO.setCalculationUnit(shippingCostNameEnum.getUnit());
            if (ShippingCostNameEnum.DISCOUNT_RATE.equals(shippingCostNameEnum)) {
                List<ShippingTemplateCostSettingDTO.ViewDTO> costSettingList = discountList.stream().map(obj -> new ShippingTemplateCostSettingDTO.ViewDTO(obj.getCode(), obj.getName(), shippingCostNameEnum.getType())).collect(Collectors.toList());
                viewDTO.setCostSettingList(costSettingList);
            }
            if (ShippingCostNameEnum.OVERSIZE_SURCHARGE_COST.equals(shippingCostNameEnum)) {
                List<ShippingTemplateCostSettingDTO.ViewDTO> costSettingList = sideList.stream().map(obj -> new ShippingTemplateCostSettingDTO.ViewDTO(obj.getCode(), obj.getName(), shippingCostNameEnum.getType())).collect(Collectors.toList());
                viewDTO.setCostSettingList(costSettingList);
            }
            if (ShippingCostNameEnum.SIGNATURE_COST.equals(shippingCostNameEnum) || ShippingCostNameEnum.PREMIUM_COST.equals(shippingCostNameEnum)) {
                List<ShippingTemplateCostSettingDTO.ViewDTO> costSettingList = voteList.stream().map(obj -> new ShippingTemplateCostSettingDTO.ViewDTO(obj.getCode(), obj.getName(), shippingCostNameEnum.getType())).collect(Collectors.toList());
                viewDTO.setCostSettingList(costSettingList);
            }
            if (ShippingCostNameEnum.FUEL_SURCHARGE_RATE.equals(shippingCostNameEnum)) {
                List<ShippingTemplateCostSettingDTO.ViewDTO> costSettingList = fuelSurchargeList.stream().map(obj -> new ShippingTemplateCostSettingDTO.ViewDTO(obj.getCode(), obj.getName(), shippingCostNameEnum.getType())).collect(Collectors.toList());
                viewDTO.setCostSettingList(costSettingList);
            }
            list.add(viewDTO);
        }
        return list;
    }

    @Override
    public List<String> listWarehouseName() {
        return  shippingTemplateRuleMapper.listWarehouseName();
    }

    @Override
    public List<String> listRegionName() {
        return  shippingTemplateRuleMapper.listRegionName();
    }

    @Override
    public List<ShippingTemplateDTO.SelectDTO> listShippingTemplate() {
        return baseMapper.listShippingTemplate();
    }

    @Override
    public ShippingTemplateEntity getByChannelId(String channelId) {
        return baseMapper.getByChannelId(channelId);
    }

    /**
     * @description: 导入数据处理
     * @author Will
     * @date: 2023/11/8 18:19
     * @param billingMethod
     * @param type
     * @param successList
     * @param errorList

     */
    private void handleImportSuccessList (String billingMethod, String type,List<ShippingTemplateExcelDTO> successList,List<ShippingTemplateExcelDTO > errorList
            ,List<ShippingTemplateCityExcelDTO> cityErrorList,List<ShippingTemplateCityExcelDTO> citySuccessList) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }
        //根据模板名称查询是否存在相同模板
        List<String> nameList = successList.stream().map(ShippingTemplateExcelDTO::getName).distinct().collect(Collectors.toList());
        List<ShippingTemplateEntity> shippingTemplateList = listByNames(nameList);

        //查询国家数据
        List<String> countryNameList = successList.stream().flatMap(obj -> Stream.of(obj.getFromCountry(), obj.getToCountry())).distinct().collect(Collectors.toList());
        List<DictCountryEntity> dictCountryList = sysDictFeign.listCountryByNamesOrIds(countryNameList);

        Map<String, List<ShippingTemplateExcelDTO>> map = successList.stream().collect(Collectors.groupingBy(ShippingTemplateExcelDTO::getName));

        for (Map.Entry<String, List<ShippingTemplateExcelDTO>> entry :  map.entrySet()) {
            List<ShippingTemplateExcelDTO> value = entry.getValue();
            ShippingTemplateExcelDTO excelValueDTO = value.get(0);
            ShippingTemplateDTO.AddDTO addDTO = new ShippingTemplateDTO.AddDTO();
            addDTO.setBillingMethod(billingMethod);
            addDTO.setType(type);
            addDTO.setName(excelValueDTO.getName());
            addDTO.setCurrency(excelValueDTO.getCurrency());
            addDTO.setEffectiveDate(LocalDateUtil.stringToLocalDateTime(excelValueDTO.getEffectiveDate()).toLocalDate());
            addDTO.setExpireDate(StringUtils.isBlank(excelValueDTO.getExpireDate()) ? null : LocalDateUtil.stringToLocalDateTime(excelValueDTO.getExpireDate()).toLocalDate());
            addDTO.setVolumeSetting(Integer.valueOf(excelValueDTO.getVolumeSetting()));
            addDTO.setPriceBinary(PriceBinaryEnum.getCode(excelValueDTO.getPriceBinary()));
            addDTO.setWeightUnit(excelValueDTO.getWeightUnit());
            List<ShippingTemplateRuleDTO.AddDTO> detailList = new ArrayList<>();
            Boolean isError = Boolean.FALSE;
            for (ShippingTemplateExcelDTO excelDTO : value) {
                //验证数据
                List<String> errorMsgList = checkImportData(billingMethod, type, excelDTO, shippingTemplateList,citySuccessList,dictCountryList);
                if (CollectionUtils.isNotEmpty(errorMsgList)) {
                    isError = Boolean.TRUE;
                    excelDTO.setErrorMsg(FieldValidUtil.getMsgSort(errorMsgList));
                    continue;
                }
                ShippingTemplateRuleDTO.AddDTO ruleAddDTO = new ShippingTemplateRuleDTO.AddDTO();
                //起始国
                String fromCountry = dictCountryList.stream().filter(obj -> obj.getId().equals(excelDTO.getFromCountry()) || obj.getNameCn().equals(excelDTO.getFromCountry())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
                ruleAddDTO.setFromCountry(fromCountry);
                //目的国
                if (CharSequenceUtil.isNotBlank(excelDTO.getToCountry())) {
                    String toCountry = dictCountryList.stream().filter(obj -> obj.getId().equals(excelDTO.getToCountry()) || obj.getNameCn().equals(excelDTO.getToCountry())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
                    ruleAddDTO.setToCountry(toCountry);
                }
                ruleAddDTO.setRegion(excelDTO.getRegion());
                DictCountryEntity toCountry = dictCountryList.stream().filter(obj -> Objects.equals(excelDTO.getToCountry(), obj.getNameCn()) || Objects.equals(excelDTO.getToCountry(), obj.getId())).findFirst().orElse(null);
                String toCountryName = Objects.nonNull(toCountry) ? toCountry.getNameCn() : CharSequenceUtil.EMPTY;
                String toCountryId = Objects.nonNull(toCountry) ? toCountry.getId() : CharSequenceUtil.EMPTY;
                List<String> cityList = citySuccessList.stream().filter(obj -> (toCountryId.equals(obj.getCountry()) || toCountryName.equals(obj.getCountry())) && obj.getRegion().equals(excelDTO.getRegion())).map(ShippingTemplateCityExcelDTO::getCity).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(cityList)) {
                    ruleAddDTO.setCityList(cityList);
                }
                ruleAddDTO.setToWarehouseName(excelDTO.getToWarehouseName());
                ruleAddDTO.setStartWeight(new BigDecimal(excelDTO.getStartWeight()));
                ruleAddDTO.setEndWeight(new BigDecimal(excelDTO.getEndWeight()));
                ruleAddDTO.setFirstWeight(ObjectUtil.isEmpty(excelDTO.getFirstWeight()) ? null : new BigDecimal(excelDTO.getFirstWeight()));
                ruleAddDTO.setFirstWeight(ObjectUtil.isEmpty(excelDTO.getFirstWeight()) ? null : new BigDecimal(excelDTO.getFirstWeight()));
                ruleAddDTO.setFirstWeightShippingCost(ObjectUtil.isEmpty(excelDTO.getFirstWeightShippingCost()) ? null : new BigDecimal(excelDTO.getFirstWeightShippingCost()));
                ruleAddDTO.setAdditionalUnitWeight(ObjectUtil.isEmpty(excelDTO.getAdditionalUnitWeight()) ? null : new BigDecimal(excelDTO.getAdditionalUnitWeight()));
                ruleAddDTO.setAdditionalPrice(ObjectUtil.isEmpty(excelDTO.getAdditionalPrice()) ? null : new BigDecimal(excelDTO.getAdditionalPrice()));
                ruleAddDTO.setShippingPrice(ObjectUtil.isEmpty(excelDTO.getShippingPrice()) ? null : new BigDecimal(excelDTO.getShippingPrice()));
                ruleAddDTO.setRegistrationCost(ObjectUtil.isEmpty(excelDTO.getRegistrationCost()) ? null : new BigDecimal(excelDTO.getRegistrationCost()));
                ruleAddDTO.setOperatingCost(ObjectUtil.isEmpty(excelDTO.getOperatingCost()) ? null : new BigDecimal(excelDTO.getOperatingCost()));
                ruleAddDTO.setMinCost(ObjectUtil.isEmpty(excelDTO.getMinCost()) ? null : new BigDecimal(excelDTO.getMinCost()));
                detailList.add(ruleAddDTO);
            }
            //更新错误数据
            if (isError) {
                errorList.addAll(value);
                continue;
            }
            addDTO.setDetailList(detailList);
            //其他费用
            List<ShippingTemplateOtherCostDTO.AddDTO> otherCostList = addShippingTemplateOtherCost(excelValueDTO);
            addDTO.setOtherCostList(otherCostList);
            this.add(addDTO);
        }
        //添加城市错误信息
        for (ShippingTemplateExcelDTO excelDTO :errorList) {
            DictCountryEntity toCountry = dictCountryList.stream().filter(obj -> Objects.equals(excelDTO.getToCountry(), obj.getNameCn()) || Objects.equals(excelDTO.getToCountry(), obj.getId())).findFirst().orElse(null);
            String toCountryName = Objects.nonNull(toCountry) ? toCountry.getNameCn() : CharSequenceUtil.EMPTY;
            String toCountryId = Objects.nonNull(toCountry) ? toCountry.getId() : CharSequenceUtil.EMPTY;
            //城市信息
            List<ShippingTemplateCityExcelDTO> cityExcelList = citySuccessList.stream().filter(obj -> (toCountryId.equals(obj.getCountry()) || toCountryName.equals(obj.getCountry())) && obj.getRegion().equals(excelDTO.getRegion())).distinct().collect(Collectors.toList());
            cityErrorList.addAll(cityExcelList);
        }
    }


    @Override
    public Boolean exportExcel(ShippingTemplateDTO.ExportExcelParamDTO params) {
        downloadTaskFeign.saveDownloadTask("运费模板列表", EXPORT_TMS_SHIPPING_TEMPLATE.getCode(), params);
        return Boolean.TRUE;
    }



    /**
     * @description: 处理分区城市
     * @author Will
     * @date: 2023/11/8 11:44
     * @param detailList
     */
    private void handleRuleView (List<ShippingTemplateRuleDTO.ViewDTO> detailList,ShippingTemplateEntity entity) {
        //国家谢谢
        List<String> countryIdList = detailList.stream().flatMap(obj -> Stream.of(obj.getToCountry(), obj.getFromCountry()))
                .distinct().collect(Collectors.toList());
        List<DictCountryEntity> countryList = sysDictFeign.listCountryByIds(countryIdList);

        for (ShippingTemplateRuleDTO.ViewDTO viewDTO :detailList) {
            //起始地
            String fromCountryName = countryList.stream().filter(obj -> obj.getId().equals(viewDTO.getFromCountry())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            viewDTO.setFromCountryName(fromCountryName);
            //目的地
            String toCountryName = countryList.stream().filter(obj -> obj.getId().equals(viewDTO.getToCountry())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getNameCn())).orElse("");
            viewDTO.setToCountryName(toCountryName);

            if (ShippingTemplateTypeEnum.ENUM_REGION.getCode().equals(entity.getType())) {
                List<ShippingRegionCityEntity> list = shippingRegionCityService.listByRuleId(viewDTO.getId());
                if (CollectionUtils.isEmpty(list)) {
                    continue;
                }
                List<String> cityList = list.stream().map(ShippingRegionCityEntity::getCity).collect(Collectors.toList());
                viewDTO.setCityList(cityList);
            }
        }

    }

    /**
     * @description: 处理其他费用
     * @author Will
     * @date: 2023/11/8 12:08
     * @param otherCostList
     */
    private void handleOtherCost(List<ShippingTemplateOtherCostDTO.ViewDTO> otherCostList) {
        //计算方式
        List<String> otherCostIdList = otherCostList.stream().map(ShippingTemplateOtherCostDTO.ViewDTO::getId).collect(Collectors.toList());
        List<ShippingTemplateCostSettingEntity> list = shippingTemplateCostSettingService.listByOtherCostIds(otherCostIdList);

        for (ShippingTemplateOtherCostDTO.ViewDTO viewDTO : otherCostList) {

            List<ShippingTemplateCostSettingEntity> costSettingEntityList = list.stream().filter(obj -> obj.getOtherCostId().equals(viewDTO.getId())).collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(costSettingEntityList)) {
                List<ShippingTemplateCostSettingDTO.ViewDTO> costSettingList = BeanMapperUtils.copyList(ShippingTemplateCostSettingDTO.ViewDTO.class, costSettingEntityList);
                //计算方式字典
                List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(viewDTO.getCalculationMethod());
                for (ShippingTemplateCostSettingDTO.ViewDTO costSettingDTO : costSettingList) {
                    String name = dictList.stream().filter(obj -> obj.getCode().equals(costSettingDTO.getCode())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
                    costSettingDTO.setName(name);
                }
                //编码
                List<String> settingList = costSettingList.stream().map(ShippingTemplateCostSettingDTO.ViewDTO::getCode).collect(Collectors.toList());
                viewDTO.setSettingList(settingList);
                //名称
                List<String> settingNameList = costSettingList.stream().map(ShippingTemplateCostSettingDTO.ViewDTO::getName).collect(Collectors.toList());
                viewDTO.setSettingNameList(settingNameList);
            }
            //数值设置
            if (StringUtils.isNotBlank(viewDTO.getExtendJson())) {
                ExtendJsonDTO.CommonDTO jsonDTO = JSONUtil.toBean(viewDTO.getExtendJson(), ExtendJsonDTO.CommonDTO.class);
                viewDTO.setExtendJsonDto(jsonDTO);
            } else {
                viewDTO.setExtendJsonDto(new ExtendJsonDTO.CommonDTO());
            }
        }
    }

    /**
     * @description: 根据名称集合查询
     * @author Will
     * @date: 2023/11/8 18:37
     * @param nameList
     * @return List<ShippingTemplateEntity>
     */
    private List<ShippingTemplateEntity> listByNames (List<String> nameList) {
        if (CollectionUtils.isEmpty(nameList)) {
            return Collections.EMPTY_LIST;
        }
       return lambdaQuery().in(ShippingTemplateEntity::getName,nameList).list();
    }

    /**
     * @description: 根据名称查询
     * @author Will
     * @date: 2023/11/8 18:37
     * @param name
     * @return List<ShippingTemplateEntity>
     */
    private ShippingTemplateEntity listByName (String name) {
        return lambdaQuery().eq(ShippingTemplateEntity::getName,name).one();
    }

    /**
     * @description: 分页查询数据处理
     * @author Will
     * @date: 2023/11/6 16:43
     * @param records
     */
    private void doOpHandleShippingTemplate (List<ShippingTemplateDTO.ListDTO> records) {

        //渠道
        List<String> idList = records.stream().map(ShippingTemplateDTO.ListDTO::getId).distinct().collect(Collectors.toList());
        List<ShippingTemplateRefChannelDTO.ViewDTO> refList = shippingTemplateRefChannelService.listByMainIds(idList);

        List<String> currencyList = records.stream().map(ShippingTemplateDTO.ListDTO::getCurrency).collect(Collectors.toList());
        List<CurrencyDTO.ViewDTO> currencyViewList = sysUserFeign.listByCurrency(currencyList);


        for (ShippingTemplateDTO.ListDTO listDTO : records) {
            //模板类型名称
            listDTO.setTypeName(ShippingTemplateTypeEnum.getName(listDTO.getType()));

            //物流商id
            List<String> logisticsSupplierIdList = refList.stream().filter(obj -> obj.getMainId().equals(listDTO.getId())).map(ShippingTemplateRefChannelDTO.ViewDTO::getLogisticsSupplierId).distinct().collect(Collectors.toList());
            listDTO.setLogisticsSupplierIdList(logisticsSupplierIdList);
            //渠道id
            List<String> logisticsChannelIdList = refList.stream().filter(obj -> obj.getMainId().equals(listDTO.getId())).map(ShippingTemplateRefChannelDTO.ViewDTO::getLogisticsChannelId).collect(Collectors.toList());
            listDTO.setChannelIdList(logisticsChannelIdList);

            //渠道名称
            List<String> channelNameList = refList.stream().filter(obj -> obj.getMainId().equals(listDTO.getId())).map(ShippingTemplateRefChannelDTO.ViewDTO::getLogisticsChannelName).collect(Collectors.toList());
            listDTO.setChannelNameList(channelNameList);
            listDTO.setChannelNames(StrUtil.join(",",channelNameList));
            //是否禁用
            listDTO.setDisabledName(listDTO.getDisabled() ? "停用" : "启用");
            //币别符号
            String currencySymbol = currencyViewList.stream().filter(obj -> obj.getId().equals(listDTO.getCurrency())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getSymbol())).orElse("");
            listDTO.setCurrencySymbol(currencySymbol);
        }
    }

    /**
     * sheet0监听
     */
    private ShippingTemplateExcelListener listenerFirstSheet (MultipartFile excelFile) {

        ShippingTemplateExcelListener excelListenerUtil = new ShippingTemplateExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), ShippingTemplateExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        return excelListenerUtil;
    }
    /**
     * sheet1监听
     */
    private ShippingTemplateCityExcelListener listenerSecondSheet (String type,MultipartFile excelFile) {
        if (!ShippingTemplateTypeEnum.ENUM_REGION.getCode().equals(type)) {
            return null;
        }
        ShippingTemplateCityExcelListener cityExcelListenerUtil = new ShippingTemplateCityExcelListener();
        try {
            EasyExcel.read(excelFile.getInputStream(), ShippingTemplateCityExcelDTO.class, cityExcelListenerUtil).sheet(1).doRead();
        } catch (IOException e) {
            log.error("导入错误！", e);
            throw new ServiceException(ApiError.ERROR_95124);
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.ERROR_1016);
        }
        return cityExcelListenerUtil;
    }




    /**
     * @description: 导入模板名称
     * @author Will
     * @date: 2023/11/8 15:06
     * @param billingMethod
     * @param type
     * @return String
     */
    private String getExportErrorExcelPath (String billingMethod,String type) {

        if (ShippingTemplateTypeEnum.ENUM_COUNTRY.getCode().equals(type)) {
            if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(billingMethod)) {
                return "excel/shippingTemplateError_country1.xlsx";
            }
            if (ShippingBillingMethodEnum.ENUM_WEIGHT_SEGMENT.getCode().equals(billingMethod)) {
                return "excel/shippingTemplateError_country2.xlsx";
            }
        }
        if (ShippingTemplateTypeEnum.ENUM_REGION.getCode().equals(type)) {
            if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(billingMethod)) {
                return "excel/shippingTemplateError_region1.xlsx";
            }
            if (ShippingBillingMethodEnum.ENUM_WEIGHT_SEGMENT.getCode().equals(billingMethod)) {
                return "excel/shippingTemplateError_region2.xlsx";
            }
        }
        if (ShippingTemplateTypeEnum.ENUM_WAREHOUSE.getCode().equals(type)) {
            if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(billingMethod)) {
                return "excel/shippingTemplateError_warehouse1.xlsx";
            }
            if (ShippingBillingMethodEnum.ENUM_WEIGHT_SEGMENT.getCode().equals(billingMethod)) {
                return "excel/shippingTemplateError_warehouse2.xlsx";
            }
        }
        throw new ServiceException(ApiError.ERROR_95131);
    }

    /**
     * @description: 导入模板名称
     * @author Will
     * @date: 2023/11/8 15:06
     * @param billingMethod
     * @param type
     * @return String
     */
    private String getTemplatePath (String billingMethod,String type) {

        if (ShippingTemplateTypeEnum.ENUM_COUNTRY.getCode().equals(type)) {
            if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(billingMethod)) {
                return "classpath:excel/shippingTemplate_country1.xlsx";
            }
            if (ShippingBillingMethodEnum.ENUM_WEIGHT_SEGMENT.getCode().equals(billingMethod)) {
                return "classpath:excel/shippingTemplate_country2.xlsx";
            }
        }
        if (ShippingTemplateTypeEnum.ENUM_REGION.getCode().equals(type)) {
            if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(billingMethod)) {
                return "classpath:excel/shippingTemplate_region1.xlsx";
            }
            if (ShippingBillingMethodEnum.ENUM_WEIGHT_SEGMENT.getCode().equals(billingMethod)) {
                return "classpath:excel/shippingTemplate_region2.xlsx";
            }
        }
        if (ShippingTemplateTypeEnum.ENUM_WAREHOUSE.getCode().equals(type)) {
            if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(billingMethod)) {
                return "classpath:excel/shippingTemplate_warehouse1.xlsx";
            }
            if (ShippingBillingMethodEnum.ENUM_WEIGHT_SEGMENT.getCode().equals(billingMethod)) {
                return "classpath:excel/shippingTemplate_warehouse2.xlsx";
            }
        }
        throw new ServiceException(ApiError.ERROR_95131);
    }

    /**
     *  导入格式化其他费用信息
     */
    private List<ShippingTemplateOtherCostDTO.AddDTO> addShippingTemplateOtherCost (ShippingTemplateExcelDTO excelValueDTO) {
        List<ShippingTemplateOtherCostDTO.AddDTO> otherCostList = new ArrayList<>();
        ShippingCostNameEnum[] values = ShippingCostNameEnum.values();
        for (ShippingCostNameEnum shippingCostNameEnum : values) {
            ShippingTemplateOtherCostDTO.AddDTO addDTO = new ShippingTemplateOtherCostDTO.AddDTO();
            addDTO.setDictCode(shippingCostNameEnum.getCode());
            addDTO.setCalculationMethod(shippingCostNameEnum.getType());
            JSONObject jsonObject = JSONUtil.parseObj(excelValueDTO);
            addDTO.setCostSettingValue(ObjectUtil.isEmpty(jsonObject.get(shippingCostNameEnum.getCode())) ? null : new BigDecimal(jsonObject.get(shippingCostNameEnum.getCode()).toString()));
            addDTO.setCalculationUnit(shippingCostNameEnum.getUnit());
            if (ShippingCostNameEnum.PREMIUM_COST.equals(shippingCostNameEnum)) {
                addDTO.setSettingList(Arrays.asList(ShippingCostNameEnum.PREMIUM_COST.getType()));
            }
            if (ShippingCostNameEnum.SIGNATURE_COST.equals(shippingCostNameEnum)) {
                addDTO.setSettingList(Arrays.asList(ShippingCostNameEnum.SIGNATURE_COST.getType()));
            }
            otherCostList.add(addDTO);
        }
        return otherCostList;
    }

    /**
     * 导入验证数据
     */
    private List<String> checkImportData (String billingMethod, String type,ShippingTemplateExcelDTO addDTO
            ,List<ShippingTemplateEntity> shippingTemplateList,List<ShippingTemplateCityExcelDTO> citySuccessList
            ,List<DictCountryEntity> dictCountryList) {
        List<String> errorMsgList = new ArrayList<>();

        long count = shippingTemplateList.stream().filter(obj -> obj.getName().equals(addDTO.getName())).count();
        if (count > 0) {
            errorMsgList.add("已存在相同模板");
        }

        String fromCountry = dictCountryList.stream().filter(obj -> Objects.equals(addDTO.getFromCountry(), obj.getNameCn()) || Objects.equals(addDTO.getFromCountry(), obj.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
        if (CharSequenceUtil.isBlank(fromCountry)) {
            errorMsgList.add("系统中未找起始国家");
        }

        if (ShippingTemplateTypeEnum.ENUM_COUNTRY.getCode().equals(type)) {
            if (ObjectUtil.isEmpty(addDTO.getToCountry())) {
                errorMsgList.add("目的地不能为空");
            }
            if (CharSequenceUtil.isNotBlank(addDTO.getToCountry())) {
                String toCountry = dictCountryList.stream().filter(obj -> Objects.equals(addDTO.getToCountry(), obj.getNameCn()) || Objects.equals(addDTO.getToCountry(), obj.getId())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getId())).orElse("");
                if (CharSequenceUtil.isBlank(toCountry)) {
                    errorMsgList.add("系统中未找目的地");
                }
            }
        }
        if (ShippingTemplateTypeEnum.ENUM_REGION.getCode().equals(type)) {
            if (ObjectUtil.isEmpty(addDTO.getToCountry())) {
                errorMsgList.add("目的地不能为空");
            }
            if (ObjectUtil.isEmpty(addDTO.getRegion())) {
                errorMsgList.add("城市分区不能为空");
            }
            DictCountryEntity toCountry = dictCountryList.stream().filter(obj -> Objects.equals(addDTO.getToCountry(), obj.getNameCn()) || Objects.equals(addDTO.getToCountry(), obj.getId())).findFirst().orElse(null);
            String toCountryName = Objects.nonNull(toCountry) ? toCountry.getNameCn() : CharSequenceUtil.EMPTY;
            String toCountryId = Objects.nonNull(toCountry) ? toCountry.getId() : CharSequenceUtil.EMPTY;
            List<ShippingTemplateCityExcelDTO> cityExcelList = citySuccessList.stream().filter(obj -> (toCountryId.equals(obj.getCountry()) || toCountryName.equals(obj.getCountry())) && obj.getRegion().equals(addDTO.getRegion())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(cityExcelList)) {
                errorMsgList.add("未找到城市分区下城市信息");
            }
        }
        if (ShippingTemplateTypeEnum.ENUM_WAREHOUSE.getCode().equals(type)) {
            if (ObjectUtil.isEmpty(addDTO.getToWarehouseName())) {
                errorMsgList.add("目的仓不能为空");
            }
        }
        if (ShippingBillingMethodEnum.ENUM_SEVERAL_WEIGHT.getCode().equals(billingMethod)) {
            if (ObjectUtil.isEmpty(addDTO.getFirstWeight())) {
                errorMsgList.add("首重不能为空");
            }
            if (ObjectUtil.isEmpty(addDTO.getFirstWeightShippingCost())) {
                errorMsgList.add("首重运费不能为空");
            }
            if (ObjectUtil.isEmpty(addDTO.getAdditionalUnitWeight())) {
                errorMsgList.add("续重单位重量不能为空");
            }
            if (ObjectUtil.isEmpty(addDTO.getAdditionalPrice())) {
                errorMsgList.add("续重单价不能为空");
            }
        }
        if (ShippingBillingMethodEnum.ENUM_WEIGHT_SEGMENT.getCode().equals(billingMethod)) {
            if (ObjectUtil.isEmpty(addDTO.getShippingPrice())) {
                errorMsgList.add("运费单价不能为空");
            }
        }
        return errorMsgList;
    }


    /**
     * 按国家计算
     */
    private BigDecimal calculationByCountry (ShippingTemplateDTO.TrialCalculationParamDTO dto,ShippingTemplateEntity entity) {
        if (StringUtils.isEmpty(dto.getToCountry())) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_TO_COUNTRY_NOT_NUll);
        }
        ShippingTemplateRuleDTO.ViewParamDTO viewParamDTO = new ShippingTemplateRuleDTO.ViewParamDTO();
        viewParamDTO.setMainId(entity.getId());
        viewParamDTO.setFromCountry(dto.getFromCountry());
        viewParamDTO.setToCountry(dto.getToCountry());
        viewParamDTO.setWeight(dto.getWeight());
        ShippingTemplateRuleEntity shippingTemplateRule = shippingTemplateRuleService.getShippingTemplateRule(viewParamDTO);
        if (ObjectUtil.isEmpty(shippingTemplateRule)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_RULE_NOT_EXIST);
        }
        //计算最终运费
        ShippingCalculationDTO.ViewDTO shippingCalculationDTO = shippingCalculationService.calculationFinalShippingCost(entity, shippingTemplateRule, dto.getWeight());
        return shippingCalculationDTO.getTotalTrialShippingCost();
    }

    /**
     * 按分区计算
     */
    private BigDecimal calculationByRegion (ShippingTemplateDTO.TrialCalculationParamDTO dto,ShippingTemplateEntity entity) {
        if (StringUtils.isEmpty(dto.getRegion())) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_REGION_NOT_NULL);
        }
        ShippingTemplateRuleDTO.ViewParamDTO viewParamDTO = new ShippingTemplateRuleDTO.ViewParamDTO();
        viewParamDTO.setMainId(entity.getId());
        viewParamDTO.setFromCountry(dto.getFromCountry());
        viewParamDTO.setToCountry(dto.getToCountry());
        viewParamDTO.setRegion(dto.getRegion());
        viewParamDTO.setWeight(dto.getWeight());
        ShippingTemplateRuleEntity shippingTemplateRule = shippingTemplateRuleService.getShippingTemplateRule(viewParamDTO);
        if (ObjectUtil.isEmpty(shippingTemplateRule)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_RULE_NOT_EXIST);
        }
        //计算最终运费
        ShippingCalculationDTO.ViewDTO shippingCalculationDTO = shippingCalculationService.calculationFinalShippingCost(entity, shippingTemplateRule, dto.getWeight());
        return shippingCalculationDTO.getTotalTrialShippingCost();
    }

    /**
     * 按仓库计算
     */
    private BigDecimal calculationByWarehouse (ShippingTemplateDTO.TrialCalculationParamDTO dto,ShippingTemplateEntity entity) {
        if (StringUtils.isEmpty(dto.getToWarehouseName())) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_WAREHOUSE_NOT_NULL);
        }
        ShippingTemplateRuleDTO.ViewParamDTO viewParamDTO = new ShippingTemplateRuleDTO.ViewParamDTO();
        viewParamDTO.setMainId(entity.getId());
        viewParamDTO.setFromCountry(dto.getFromCountry());
        viewParamDTO.setToWarehouseName(dto.getToWarehouseName());
        viewParamDTO.setWeight(dto.getWeight());
        ShippingTemplateRuleEntity shippingTemplateRule = shippingTemplateRuleService.getShippingTemplateRule(viewParamDTO);
        if (ObjectUtil.isEmpty(shippingTemplateRule)) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_RULE_NOT_EXIST);
        }
        //计算最终运费
        ShippingCalculationDTO.ViewDTO shippingCalculationDTO = shippingCalculationService.calculationFinalShippingCost(entity, shippingTemplateRule, dto.getWeight());
        return shippingCalculationDTO.getTotalTrialShippingCost();
    }



    /**
    * 新增修改处理数据
    */
    private void handleData(ShippingTemplateEntity shippingTemplateEntity) {

        ShippingTemplateEntity entity = this.listByName(shippingTemplateEntity.getName());
        if (ObjectUtil.isNotEmpty(entity) && !CharSequenceUtil.equals(entity.getId(),shippingTemplateEntity.getId())) {
            throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_EXIST);
        }
        //验证是否能停用
        if (StringUtils.isNotBlank(shippingTemplateEntity.getId())) {
            List<ShippingTemplateRefChannelDTO.ViewDTO> refList = shippingTemplateRefChannelService.listByMainIds(Arrays.asList(shippingTemplateEntity.getId()));
            if (CollectionUtils.isNotEmpty(refList) && shippingTemplateEntity.getDisabled()) {
                throw new ServiceException(ApiError.ERROR_SHIPPING_TEMPLATE_DISABLED);
            }
        }
    }

    @Override
    public List<ShippingTemplateEntity> getByChannelIds(List<String> channelIds) {
        return baseMapper.getByChannelIds(channelIds);
    }

    @Override
    public PagingVO<ShippingTemplateDTO.ListDTO> exportShippingTemplate(PagingDTO<ShippingTemplateDTO.ExportExcelParamDTO> dto) {
        Page<ShippingTemplateDTO.ListDTO> page = baseMapper.listByExportExcel(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            doOpHandleShippingTemplate(page.getRecords());
        }
        return new PagingVO<>(page);
    }

}

package com.erp.server.oms.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.constant.FileTemplateConstant;
import com.common.business.dto.AdvanceQueryContainer;
import com.common.business.dto.base.BaseIdDTO;
import com.common.business.dto.base.BaseIdsDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.*;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.utils.JasperHelperUtil;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.controller.vo.ApiResult;
import com.common.core.entity.BaseEntity;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.common.core.utils.LengthConverterUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.dmp.constant.DmpOutputConstant;
import com.erp.model.dmp.dto.DmpInoutDTO;
import com.erp.model.dmp.entity.DmpOutputTaskRecordEntity;
import com.erp.model.dmp.enums.DmpBasicSystemCodeEnum;
import com.erp.model.dmp.enums.DmpInputTaskTaskTypeEnum;
import com.erp.model.dmp.enums.DmpOutputTaskRecordStatusEnum;
import com.erp.model.oms.dto.*;
import com.erp.model.oms.dto.excel.SkuMappingCustomerImportExcelDTO;
import com.erp.model.oms.dto.excel.SkuMappingImportExcelDTO;
import com.erp.model.oms.dto.excel.SkuMappingWarehouseImportExcelDTO;
import com.erp.model.oms.entity.*;
import com.erp.model.oms.enums.*;
import com.erp.model.plm.dto.BomChildrenSkuDTO;
import com.erp.model.plm.entity.ProductUnitEntity;
import com.erp.model.plm.enums.BomTypeEnum;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.model.sys.dto.FileTemplateDTO;
import com.erp.model.sys.entity.FileTemplateEntity;
import com.erp.model.tms.dto.InventorySkuCostDTO;
import com.erp.model.wms.dto.OverseasProviderWarehouseDTO;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.WarehouseDTO;
import com.erp.model.wms.dto.inventory.InventoryQtyDTO;
import com.erp.model.wms.entity.OverseasProviderEntity;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.rpc.dmp.feign.DmpInoutTaskFeign;
import com.erp.rpc.dmp.feign.DmpTaskFeign;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.BomSkuFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.AuthDataFeign;
import com.erp.rpc.sys.feign.FileTemplateFeign;
import com.erp.rpc.tms.feign.LogisticsFeign;
import com.erp.rpc.wms.feign.*;
import com.erp.server.oms.listener.SkuMappingCustomerExcelListener;
import com.erp.server.oms.listener.SkuMappingExcelListener;
import com.erp.server.oms.listener.SkuMappingWarehouseExcelListener;
import com.erp.server.oms.mapper.SkuMappingMapper;
import com.erp.server.oms.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.*;

/**
 * <p>
 * sku 对照表 服务实现类
 * </p>
 *
 * @author Lambda
 * @since 2023-06-28
 */
@Service
@Slf4j
public class SkuMappingServiceImpl extends SuperServiceImpl<SkuMappingMapper, SkuMappingEntity> implements SkuMappingService {

    @Resource
    private OperateLogService operateLogService;

    @Resource
    private PlmTaskFeign plmTaskFeign;

    @Resource
    private ShopInfoService shopInfoService;

    @Resource
    private OmsPushMsgService omsPushMsgService;

    @Resource
    private DictBasicService dictBasicService;

    @Resource
    private ListingInfoService listingInfoService;

    @Resource
    private WmsTaskFeign wmsTaskFeign;

    @Resource
    private WmsWarehouseFeign wmsWarehouseFeign;

    @Resource
    private WmsOverseasWarehouseFeign wmsOverseasWarehouseFeign;

    @Resource
    private OverseasProviderFeign overseasProviderFeign;

    @Resource
    private SkuMappingExtendService skuMappingExtendService;
    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private ShopSysUserAuthService shopSysUserAuthService;

    @Resource
    private DmpInoutTaskFeign dmpInoutTaskFeign;
    @Resource
    private LogisticsFeign logisticsFeign;
    @Resource
    private CustomerInfoService customerInfoService;
    @Resource
    private DmpTaskFeign dmpTaskFeign;

    @Resource
    private BomSkuFeign bomSkuFeign;
    @Resource
    private SoInfoService soInfoService;

    @Resource
    private VirtualInventoryFeign virtualInventoryFeign;
    @Resource
    private InventoryFeign inventoryFeign;
    @Resource
    private AuthDataFeign authDataFeign;

    @Resource
    private InvoiceTaxService invoiceTaxService;
    @Resource
    private FileFeign fileFeign;
    @Resource
    private FileTemplateFeign fileTemplateFeign;

    @Override
    public void downloadTemplate(String type, HttpServletResponse response) {
        if (StringUtils.isBlank(type)) {
            throw new ServiceException("下载模板类型不能为空");
        }
        //平台
        String platform = RuleTypeEnum.PLATFORM.getCode();
        //库存
        String warehouse = RuleTypeEnum.WAREHOUSE.getCode();
        String customer = RuleTypeEnum.CUSTOMER.getCode();
        List<String> typeList = Arrays.asList(platform, warehouse,customer);
        if (!typeList.contains(type)) {
            throw new ServiceException("下载模板类型有误");
        }
        String path = "excel/skuMappingTemplate.xlsx";
        if (warehouse.equals(type)) {
            path = "excel/skuMappingWarehouseTemplate.xlsx";
        }
        if (customer.equals(type)) {
            path = "excel/skuMappingCustomerTemplate.xlsx";
        }
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
            log.error("SkuMaping downloadTemplate  出错了 e>>>>>>>{}", e);
            throw new ServiceException(ApiError.ERROR_95131);
        }
    }


    /**
     * 导入sku对照信息
     *
     * @param excelFile
     * @param response
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-29 11:01
     */
    @Override
    public Boolean importExcel(MultipartFile excelFile, String type, HttpServletResponse response) {
        if (StringUtils.isBlank(type)) {
            throw new ServiceException("导入类型不能为空");
        }
        //平台
        String platform = RuleTypeEnum.PLATFORM.getCode();
        //库存
        String warehouse = RuleTypeEnum.WAREHOUSE.getCode();
        //客户
        String customer = RuleTypeEnum.CUSTOMER.getCode();
        List<String> typeList = Arrays.asList(platform, warehouse,customer);
        if (!typeList.contains(type)) {
            throw new ServiceException("导入类型有误");
        }
        if (platform.equals(type)) {
            List<SkuMappingEntity> skuMappingList = this.listEffectiveList();
            List<SkuVO> skuList = plmTaskFeign.listApproveSku();
            List<ListingInfoEntity> list = listingInfoService.list();
            String key = DictBasicTypeEnum.SALES_PLATFORM.getType();
            List<DictBasicDTO.ViewDTO> dictBasicList = dictBasicService.getByKey(key);
            //单位
            List<ProductUnitEntity> unitList = FeignQuery.create(ProductUnitEntity.class).list();
            //原产地
            String originKey = DictBasicTypeEnum.INVOICE_TAX_NFE_ORIGIN.getType();
            List<DictBasicDTO.ViewDTO> originList = dictBasicService.getByKey(originKey);

            SkuMappingExcelListener excelListenerUtil = new SkuMappingExcelListener(this,unitList,originList, skuList, shopInfoService, skuMappingList, dictBasicList, list, listingInfoService,operateLogService,invoiceTaxService);
            try {
                EasyExcel.read(excelFile.getInputStream(), SkuMappingImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            } catch (Exception e) {
                log.error("sku 对照表导入错误！>>>>{}", e);
                return Boolean.FALSE;
            }
            List<SkuMappingImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            if (errorList.size() > 0) {
                String fileName = "sku对照错误信息";
                ExcelUtil.export(fileName, "error", errorList, SkuMappingImportExcelDTO.class, response);
                return Boolean.FALSE;
            }
        }
        //仓库sku 对照
        if (warehouse.equals(type)) {
            // 查询仓库关联服务商
            List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listApproveWarehouse();
            warehouseList = warehouseList.stream().filter(w -> !w.getDisabled()).collect(Collectors.toList());
            List<String> warehouseIds = warehouseList.stream().map(WarehouseDTO.UpdateDTO::getId).collect(Collectors.toList());
            // 海外仓库
            List<WarehouseDTO.ListDTO> overseasWarehouseList = wmsWarehouseFeign.listByIds(warehouseIds);
            Map<String, WarehouseDTO.ListDTO> overseasWarehouseMap = new HashMap<>();
            if (CollectionUtils.isNotEmpty(overseasWarehouseList)) {
                overseasWarehouseMap = overseasWarehouseList.stream().collect(Collectors.toMap(WarehouseDTO.ListDTO::getId, Function.identity()));
            }
            SkuMappingWarehouseExcelListener excelListenerUtil = new SkuMappingWarehouseExcelListener(overseasWarehouseMap);
            try {
                EasyExcel.read(excelFile.getInputStream(), SkuMappingWarehouseImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            } catch (Exception e) {
                log.error("sku 对照表导入错误！>>>>{}", e);
                return Boolean.FALSE;
            }
            List<SkuMappingWarehouseImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            if (errorList.size() > 0) {
                String fileName = "sku对照错误信息";
                ExcelUtil.export(fileName, "error", errorList, SkuMappingWarehouseImportExcelDTO.class, response);
                return Boolean.FALSE;
            }
        }

        //客户sku 对照
        if (customer.equals(type)) {
            SkuMappingCustomerExcelListener excelListenerUtil = new SkuMappingCustomerExcelListener();
            try {
                EasyExcel.read(excelFile.getInputStream(), SkuMappingCustomerImportExcelDTO.class, excelListenerUtil).sheet(0).doRead();
            } catch (Exception e) {
                log.error("sku 对照表导入错误！>>>>{}", e);
                return Boolean.FALSE;
            }
            List<SkuMappingCustomerImportExcelDTO> errorList = excelListenerUtil.getErrorList();
            if (errorList.size() > 0) {
                String fileName = "sku对照错误信息";
                ExcelUtil.export(fileName, "error", errorList, SkuMappingCustomerImportExcelDTO.class, response);
                return Boolean.FALSE;
            }
        }

        return Boolean.TRUE;

    }

    /**
     * 平台sku对照表分页查询
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.PagingViewDTO>
     * @author yl
     * @date 2023-06-29 18:07
     */
    @Override
    public PagingVO<SkuMappingDTO.PagingViewDTO> paging(PagingDTO<SkuMappingDTO.PagingParamDTO> dto) {
        SkuMappingDTO.PagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        params.setType(RuleTypeEnum.PLATFORM.getCode());
        IPage pageData = baseMapper.paging(query, params);
        List<SkuMappingDTO.PagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        fillDb(list);
        return new PagingVO<>(pageData);

    }

    /**
     * 导出sku 对照表
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-06-30 9:35
     */
    @Override
    public Boolean exportPlatformSku(SkuMappingDTO.ExportDTO dto) {
        downloadTaskFeign.saveDownloadTask("sku对照列表", EXPORT_OMS_PLATFORM_SKU.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 获取tab 列表
     *
     * @param dto
     * @return java.util.List<com.erp.model.oms.dto.SkuMapingDTO.TabListDTO>
     * @author yl
     * @date 2023-06-30 9:45
     */
    @Override
    public List<SkuMappingDTO.TabListDTO> tabList(SkuMappingDTO.FindTabDTO dto) {
        List<SkuMappingDTO.TabListDTO> resultList = new ArrayList<>(3);
        if("warehouse".equals(dto.getType())){
            //重置权限
            dto.setPermissionSql(getWarehousePermissionSql());
        }else if ("customer".equals(dto.getType())){
            //重置权限
            dto.setPermissionSql("");
        }
        List<SkuMappingDTO.MatchCountDTO> matchCountList = baseMapper.listMatchCount(dto);
        //所有
        SkuMappingDTO.TabListDTO all = new SkuMappingDTO.TabListDTO();
        int allCount = matchCountList.stream().mapToInt(SkuMappingDTO.MatchCountDTO::getCount).sum();
        all.setCount(allCount);
        all.setTabFlag("");
        all.setTabFlagName("全部");
        resultList.add(all);
        //未匹配
        SkuMappingDTO.TabListDTO not = new SkuMappingDTO.TabListDTO();
        int notCount = matchCountList.stream().filter(m -> ListingMatchResultEnum.FALSE.getCode().equals(m.getMatchResult())).findFirst().
                map(SkuMappingDTO.MatchCountDTO::getCount).orElse(0);
        not.setCount(notCount);
        not.setTabFlag(ListingMatchResultEnum.FALSE.getCode());
        not.setTabFlagName(ListingMatchResultEnum.FALSE.getName());
        resultList.add(not);

        //已匹配
        SkuMappingDTO.TabListDTO already = new SkuMappingDTO.TabListDTO();
        int alreadyCount = matchCountList.stream().filter(m -> ListingMatchResultEnum.TRUE.getCode().equals(m.getMatchResult())).findFirst().
                map(SkuMappingDTO.MatchCountDTO::getCount).orElse(0);
        already.setCount(alreadyCount);
        already.setTabFlag(ListingMatchResultEnum.TRUE.getCode());
        already.setTabFlagName(ListingMatchResultEnum.TRUE.getName());
        resultList.add(already);

        //无需匹配
        SkuMappingDTO.TabListDTO notNeed = new SkuMappingDTO.TabListDTO();
        int notNeedCount = matchCountList.stream().filter(m -> ListingMatchResultEnum.NOT.getCode().equals(m.getMatchResult())).findFirst().
                map(SkuMappingDTO.MatchCountDTO::getCount).orElse(0);
        notNeed.setCount(notNeedCount);
        notNeed.setTabFlag(ListingMatchResultEnum.NOT.getCode());
        notNeed.setTabFlagName(ListingMatchResultEnum.NOT.getName());
        resultList.add(notNeed);

        return resultList;
    }

    /**
     * 更改sku 对照表
     *
     * @param dto
     * @param skuMapping
     * @param listing
     * @param shopId
     * @return java.lang.String
     * @author yl
     * @date 2023-06-30 10:21
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updatePlatformSku(SkuMappingDTO.UpdatePlatformDTO dto, SkuMappingEntity skuMapping, ListingInfoEntity listing, String shopId) {
        //step1 参数校验
        String id = dto.getId();
        if (Objects.isNull(skuMapping)) {
            throw new ServiceException(ApiError.ERROR_92051);
        }
        //启用日期不能大于上个映射关系的开始时间
        if (dto.getEffectiveTime().isBefore(skuMapping.getEffectiveTime())){
            throw new ServiceException(ApiError.ERROR_92151,skuMapping.getEffectiveTime());
        }
        // 历史skuId
        String historyProductSkuId = skuMapping.getProductSkuId();
        // 当前skuId
        String productSkuId = dto.getProductSkuId();
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(Arrays.asList(productSkuId));
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        String platformDict = dto.getDictPlatform();
        DictBasicEntity dictBasic = dictBasicService.getByTypeAndValue(DictBasicTypeEnum.SALES_PLATFORM.getType(), platformDict);
        if (Objects.isNull(dictBasic)) {
            throw new ServiceException(ApiError.ERROR_92053);
        }
        String platformSkuNo = dto.getPlatformSkuNo();
        if (null == listing) {
            throw new ServiceException("listing记录不存在");
        }
        //检查映射关系是否存在
        checkExist(id, listing.getId(), shopId);
        //检查历史映射关系是否存在
        if(!historyProductSkuId.equals(productSkuId)){
            checkHistory(id, listing.getId(), shopId, dto.getProductSkuId());
        }

        // 平台sku校验
        if (PlatformDictEnum.hasConnectionPlatform().contains(platformSkuNo)) {
            // 已对接api的平台
            if (!platformSkuNo.equalsIgnoreCase(listing.getPlatformSkuNo())) {
                throw new ServiceException("平台sku不存在");
            }
        } else {
            // 未对接api的平台
            listing.setPlatformSkuNo(platformSkuNo);
            listing.setPlatformSkuName(dto.getPlatformProductName());
        }
        // listing 更新匹配关系
        listing.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
        listing.setRemark("");
        listing.setPlatformSpuNo(dto.getPlatformSpuNo());
        if (!listingInfoService.updateById(listing)) {
            throw new ServiceException("[listing] 更新失败");
        }
        //更新发票税务信息
        if (ObjectUtil.isNotEmpty(dto.getTaxCodeDTO())) {
            dto.getTaxCodeDTO().setListingId(listing.getId());
            InvoiceTaxDTO.UpdateDTO updateDTO = BeanUtil.toBean(dto.getTaxCodeDTO(), InvoiceTaxDTO.UpdateDTO.class);
            invoiceTaxService.addOrUpdate(updateDTO);
        }
        // 无修改
        if (skuMapping.getProductSkuId().equalsIgnoreCase(productSkuId) && dto.getEffectiveTime().equals(skuMapping.getEffectiveTime())) {
            // 检查仓库发货配置
            skuMappingExtendService.checkAndSave(skuMapping, dto.getExtendList());
            if(PlatformDictEnum.ALI_EXPRESS.getCode().equals(skuMapping.getDictPlatform())){
                //更新发货设置信息
                List<ListingInfoEntity> listingInfoEntities = listingInfoService.lambdaQuery()
                        .eq(ListingInfoEntity::getPlatformSkuNo,listing.getPlatformSkuNo())
                        .eq(ListingInfoEntity :: getPlatformSpuNo,listing.getPlatformSpuNo())
                        .eq(ListingInfoEntity::getPlatform,listing.getPlatform())
                        .ne(ListingInfoEntity::getId,listing.getId())
                        .list();
                if(CollectionUtils.isNotEmpty(listingInfoEntities)){
                    List<String> listingIds = listingInfoEntities.stream().map(ListingInfoEntity::getId).collect(Collectors.toList());
                    List<SkuMappingEntity> skuMappingEntityList = this.listByListingIds(listingIds);
                    skuMappingEntityList = skuMappingEntityList.stream().filter(v->skuMapping.getShopId().equals(v.getShopId())).collect(Collectors.toList());
                    skuMappingExtendService.copyBySkuMapping(skuMapping,skuMappingEntityList);
                }
            }
            return BatchResultDTO.success(skuMapping.getId(), skuMapping.getId(), "更改sku对照表成功");
        }
        // 历史SkuId为设置过期
        boolean update = lambdaUpdate()
                .set(SkuMappingEntity::getIsExpire, true)
                .set(SkuMappingEntity::getExpireTime, dto.getEffectiveTime())
                //       // 历史SkuId为空逻辑删除
                .set(StringUtils.isBlank(historyProductSkuId), BaseEntity::getIsDeleted, true)
                .eq(BaseEntity::getId, skuMapping.getId())
                .update();
        if (!update){
            throw new ServiceException("[SkuMapping] 历史映射修改失败");
        }

        SkuMappingEntity addSkuMaping = new SkuMappingEntity();
        addSkuMaping.setShopId(shopId);
        addSkuMaping.setDictPlatform(skuMapping.getDictPlatform());
        addSkuMaping.setPlatformName(skuMapping.getPlatformName());
        addSkuMaping.setProductSkuNo(skuVOList.get(0).getSkuNo());
        addSkuMaping.setProductSkuId(productSkuId);
        addSkuMaping.setListingId(listing.getId());
        addSkuMaping.setType(RuleTypeEnum.PLATFORM);
        addSkuMaping.setIsExpire(Boolean.FALSE);
        addSkuMaping.setEffectiveTime(dto.getEffectiveTime());
        addSkuMaping.setExpireTime(dto.getEffectiveTime().plusYears(100));
        if (!this.save(addSkuMaping)) {
            throw new ServiceException("[SkuMapping] 映射修改新增失败");
        }
        // 检查仓库发货配置
        skuMappingExtendService.checkAndSave(addSkuMaping, dto.getExtendList());
        //速卖通相同店铺，skuNo,平台产品ID 有多个listingInfo, 需要同步映射关系
        if(PlatformDictEnum.ALI_EXPRESS.getCode().equals(skuMapping.getDictPlatform())){
            listingInfoService.handleAliExpress(addSkuMaping,skuVOList.get(0),listing);
        }

        operateLogService.addModuleOperateLogByObj(skuMapping, addSkuMaping, ModuleTypeEnum.LISTING_INFO.getCode(), addSkuMaping.getListingId(),  CharSequenceUtil.format("用户【{}】编辑sku映射表",UserContext.getDefaultLoginUser().getUserName()));
        return BatchResultDTO.success(addSkuMaping.getId(), addSkuMaping.getId(), "更改sku对照表成功");
    }


    /**
     * 销售订单添加客户sku
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMapingDTO.ProductSkuInfoDTO>
     * @author yl
     * @date 2023-07-01 9:19
     */
    @Override
    public PagingVO<SkuMappingDTO.ProductSkuInfoDTO> listPaging(PagingDTO<SkuMappingDTO.ListParamDTO> dto) {
        SkuMappingDTO.ListParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage pageData = baseMapper.listPaging(query, params);
        List<SkuMappingDTO.ProductSkuInfoDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        List<String> skuIdList = list.stream().map(SkuMappingDTO.ProductSkuInfoDTO::getSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (SkuMappingDTO.ProductSkuInfoDTO item : list) {
            String skuId = item.getSkuId();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().map(SkuVO::getSkuName).orElse("");
            item.setSkuName(skuName);
        }
        return new PagingVO<>(pageData);
    }


    /**
     * 添加库存sku 对照信息
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-08-18 16:32
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addWarehouseSku(SkuMappingDTO.AddWarehouseSkuDTO dto) {
        if(StringUtils.isBlank(dto.getWarehouseId()) && StringUtils.isBlank(dto.getAuthId())){
            throw new ServiceException("仓库和三方仓账号不能同时为空");
        }
        String skuId = dto.getProductSkuId();
        String warehouseSkuNo = dto.getWarehouseSkuNo();
        String warehouseId = dto.getWarehouseId();
        String thirdBarcode = dto.getThirdBarcode();
        String warehouseProductName = dto.getWarehouseProductName();
        LocalDateTime effectiveTime = dto.getEffectiveTime();
//        List<WarehouseDTO.UpdateDTO> warehouseList = wmsTaskFeign.listWarehouseByIds(Arrays.asList(warehouseId));
        // 查询当前仓库的平台类型
        List<WarehouseDTO.ListDTO> warehouseList = wmsWarehouseFeign.listByIds(Collections.singletonList(dto.getWarehouseId()));
        if (CollectionUtils.isEmpty(warehouseList) && StringUtils.isBlank(dto.getAuthId())) {
            throw new ServiceException("仓库不存在");
        }
        String platform = "";
        if(StringUtils.isBlank(dto.getAuthId())){
            WarehouseDTO.ListDTO currenWareHouse = warehouseList.stream().findFirst().orElse(null);
            OmsPlatformEnum platformEnum = OmsPlatformEnum.getByCode(currenWareHouse.getDictPlatform());
            if (null != platformEnum) {
                throw new ServiceException(platformEnum.getName() + "服务商仓库不允许新增");
            }
        }else{
            OverseasProviderEntity overseasProviderEntity = FeignQuery.getById(OverseasProviderEntity.class,dto.getAuthId());
            if (Objects.isNull(overseasProviderEntity)) {
                throw new ServiceException("三方仓账号不存在");
            }
            if(!overseasProviderEntity.getIsProductSync()){
                throw new ServiceException("该服务商未开启API推送，请开启后操作");
            }
            platform = overseasProviderEntity.getCode();
        }

        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(Arrays.asList(skuId));
        if (CollectionUtils.isEmpty(skuList)) {
            throw new ServiceException("sku不存在");
        }
        ListingInfoEntity existEntity = listingInfoService.getByPlatformSkuNo("",warehouseSkuNo, "");
        String listingId;
        if(null == existEntity){
            listingId = listingInfoService.addWarehouseSku(warehouseSkuNo, warehouseProductName,thirdBarcode, dto.getAuthId(), platform);
        }else{
            listingId = existEntity.getId();
        }
        if (StringUtils.isBlank(listingId)) {
            throw new ServiceException(warehouseSkuNo + "未找到");
        }
        checkWarehouseSkuExist("", listingId, warehouseId, skuId);
        SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
        skuMappingEntity.setWarehouseId(warehouseId);
        skuMappingEntity.setWarehouseName(CollectionUtils.isNotEmpty(warehouseList)?warehouseList.get(0).getName():"");
        skuMappingEntity.setType(RuleTypeEnum.WAREHOUSE);
        skuMappingEntity.setProductSkuId(skuId);
        skuMappingEntity.setProductSkuNo(skuList.get(0).getSkuNo());
        skuMappingEntity.setProductName(skuList.get(0).getSkuName());
        skuMappingEntity.setListingId(listingId);
        skuMappingEntity.setDictPlatform(platform);
        skuMappingEntity.setPlatformName(OmsPlatformEnum.getName(platform));
        skuMappingEntity.setHasMappingAll(true);
//        LocalDateTime now = LocalDateTime.now();
        //生效时间
        skuMappingEntity.setEffectiveTime(effectiveTime);
        skuMappingEntity.setExpireTime(effectiveTime.plusYears(MathUtil.NUMBER_100));
        if (this.save(skuMappingEntity)) {
            // 操作日志
            String msg =  CharSequenceUtil.format("用户【{}】新增【{}】为【{}】", UserContext.getDefaultLoginUser().getUserName(), "sku映射表", skuMappingEntity.getProductSkuNo());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), listingId, "新增操作");
            return skuMappingEntity.getId();
        }
        return "";
    }


    /**
     * 导出库存sku 对照表
     *
     * @param dto
     * @return java.lang.Boolean
     * @author yl
     * @date 2023-08-21 10:22
     */
    @Override
    public Boolean exportWarehouseSku(SkuMappingDTO.ExportWarehouseSkuDTO dto) {
        downloadTaskFeign.saveDownloadTask("sku对照列表", EXPORT_OMS_WAREHOUSE_SKU.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 更改库存sku 对照
     *
     * @param dto
     * @return java.lang.String
     * @author yl
     * @date 2023-08-21 11:40
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateWarehouseSku(SkuMappingDTO.UpdateWarehouseSkuDTO dto) {
        String id = dto.getId();
        SkuMappingEntity skuMapping = this.getById(id);
        if (Objects.isNull(skuMapping)) {
            throw new ServiceException(ApiError.ERROR_92051);
        }
        //启用日期不能大于上个映射关系的开始时间
        if (dto.getEffectiveTime().isBefore(skuMapping.getEffectiveTime())){
            throw new ServiceException(ApiError.ERROR_92151,skuMapping.getEffectiveTime());
        }
        String productSkuId = dto.getProductSkuId();
        List<SkuVO> skuVOList = plmTaskFeign.listSkuProductByIds(Arrays.asList(productSkuId));
        if (CollectionUtils.isEmpty(skuVOList)) {
            throw new ServiceException(ApiError.ERROR_95107);
        }
        String thirdBarcode = dto.getThirdBarcode();
        String warehouseSkuNo = dto.getWarehouseSkuNo();
        String warehouseId = dto.getWarehouseId();
        List<WarehouseDTO.UpdateDTO> warehouseList = new ArrayList<>();
        if (StringUtils.isBlank(skuMapping.getDictPlatform()) || !dto.getHasMappingAll() || StringUtils.isNotBlank(warehouseId)) {
            if (StringUtils.isBlank(warehouseId)) {
                throw new ServiceException("仓库不能为空");
            }
            warehouseList = wmsTaskFeign.listWarehouseByIds(Collections.singletonList(warehouseId));
            if (CollectionUtils.isEmpty(warehouseList)) {
                throw new ServiceException("仓库不存在");
            }
        }
        if(StringUtils.isBlank(skuMapping.getDictPlatform()) && StringUtils.isNotBlank(warehouseId)){
            // 查询当前仓库的平台类型
            List<WarehouseDTO.ListDTO> checkWarehouseList = wmsWarehouseFeign.listByIds(Collections.singletonList(dto.getWarehouseId()));
            if (CollectionUtils.isEmpty(checkWarehouseList)) {
                throw new ServiceException("仓库不存在");
            }
            WarehouseDTO.ListDTO currenWareHouse = checkWarehouseList.stream().findFirst().orElse(null);
            OmsPlatformEnum platformEnum = OmsPlatformEnum.getByCode(currenWareHouse.getDictPlatform());
            OmsPlatformEnum oldEnum = OmsPlatformEnum.getByCode(skuMapping.getDictPlatform());
            if (null != platformEnum) {
                if(null == oldEnum || !oldEnum.equals(platformEnum)){
                    throw new ServiceException(platformEnum.getName() + "服务商仓库不允许更新");
                }
            }
        }

        String platform = "";
        if(StringUtils.isNotBlank(dto.getAuthId())){
            OverseasProviderEntity overseasProviderEntity = FeignQuery.getById(OverseasProviderEntity.class,dto.getAuthId());
            if (Objects.isNull(overseasProviderEntity)) {
                throw new ServiceException("三方仓账号不存在");
            }
            if(!overseasProviderEntity.getIsProductSync() &&  overseasProviderEntity.getCode().equals(OmsPlatformEnum.CAI_NIAO.getCode())){
                throw new ServiceException("该服务商未开启API推送，请开启后操作");
            }
            platform = overseasProviderEntity.getCode();
        }

        ListingInfoEntity listingInfo = listingInfoService.getById(skuMapping.getListingId());
        String listingId = "";
        String authId = "";
        if (Objects.nonNull(listingInfo)) {
            listingId = listingInfo.getId();
            // listing 更新匹配关系
            listingInfo.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
            listingInfo.setRemark("");
            listingInfo.setThirdBarcode(thirdBarcode);
            if(StringUtils.isNotBlank(dto.getAuthId())){
                listingInfo.setAuthId(dto.getAuthId());
                listingInfo.setPlatform(platform);
            }
            listingInfo.setAuthId(dto.getAuthId());
            if (!listingInfoService.updateById(listingInfo)) {
                throw new ServiceException("[listing] 更新失败");
            }
        } else {
            String warehouseProductName = dto.getWarehouseProductName();
            listingId = listingInfoService.addWarehouseSku(warehouseSkuNo, warehouseProductName, thirdBarcode, dto.getAuthId(), platform);
        }
        if (StringUtils.isBlank(listingId)) {
            throw new ServiceException(warehouseSkuNo + "未找到");
        }

        SkuMappingEntity existEntity = this.getWarehouseMapping(listingId,dto.getWarehouseId(),dto.getProductSkuId(),RuleTypeEnum.WAREHOUSE,dto.getEffectiveTime(),skuMapping.getId());
        if(Objects.nonNull(existEntity)){
            throw new ServiceException("该仓库下已存在该sku");
        }
        //更改原有的
//        LocalDateTime now = LocalDateTime.now();
        skuMapping.setExpireTime(dto.getEffectiveTime());
        skuMapping.setIsExpire(Boolean.TRUE);
//        skuMapping.setIsDeleted(true);
        boolean updateResult = this.updateById(skuMapping);
        if (!updateResult) {
            throw new ServiceException("更新失败");
        }
        if (!this.removeById(skuMapping.getId())) {
            throw new ServiceException("[SkuMapping] 原数据删除失败");
        }
//        checkWarehouseSkuExist(id, listingId, warehouseId, productSkuId);
        //校验数据是否存在相同服务商不同listing 有关联多个sku
        checkSameWarehouseSkuExist(listingId, authId, productSkuId);

        SkuMappingEntity addSkuMapping = new SkuMappingEntity();
        addSkuMapping.setWarehouseId(StringUtils.isBlank(warehouseId) ? "" : warehouseId);
        addSkuMapping.setWarehouseName(CollectionUtils.isEmpty(warehouseList) ? "" : warehouseList.get(0).getName());
        addSkuMapping.setType(RuleTypeEnum.WAREHOUSE);
        addSkuMapping.setProductSkuId(productSkuId);
        addSkuMapping.setProductSkuNo(skuVOList.get(0).getSkuNo());
        addSkuMapping.setListingId(listingId);
        addSkuMapping.setDictPlatform(skuMapping.getDictPlatform());
        addSkuMapping.setPlatformName(skuMapping.getPlatformName());
        addSkuMapping.setHasMappingAll(!StringUtils.isBlank(skuMapping.getDictPlatform()) && dto.checkAndGetHasMappingAll());
        //生效时间
        addSkuMapping.setEffectiveTime(dto.getEffectiveTime());
        addSkuMapping.setExpireTime(dto.getEffectiveTime().plusYears(MathUtil.NUMBER_100));
        if (!this.save(addSkuMapping)) {
            throw new ServiceException("[SkuMapping] 数据新增失败");
        }
        // 操作日志
//        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "sku映射表", addSkuMapping.getId());
//        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.SKU_MAPPING.getCode(), addSkuMapping.getId(), "新增操作");
        operateLogService.addModuleOperateLogByObj(skuMapping, addSkuMapping, ModuleTypeEnum.LISTING_INFO.getCode(), addSkuMapping.getListingId(), "编辑sku映射表");
        return addSkuMapping.getId();

    }

    private void checkSameWarehouseSkuExist(String listingId, String authId, String productSkuId) {
        if(StringUtils.isBlank(authId) || StringUtils.isBlank(productSkuId) || StringUtils.isBlank(listingId)){
            return;
        }
        boolean existFlag = this.baseMapper.existOtherListing(listingId,authId,productSkuId);
        if(existFlag){
            throw new ServiceException("该服务商下已存在该映射关系");
        }
    }

    private SkuMappingEntity getWarehouseMapping(String listingId,String warehouseId ,String skuId,RuleTypeEnum ruleTypeEnum,LocalDateTime effectiveTime,String mappingId){
        return lambdaQuery()
                .eq(SkuMappingEntity::getListingId, listingId)
                .eq(SkuMappingEntity::getWarehouseId, warehouseId)
                .eq(SkuMappingEntity::getType, ruleTypeEnum)
                .eq(SkuMappingEntity::getEffectiveTime, effectiveTime)
                .eq(SkuMappingEntity::getProductSkuId, skuId)
                .eq(SkuMappingEntity::getIsExpire, false)
                .ne(SkuMappingEntity::getId,mappingId)
                .last(" LIMIT 1")
                .one();
    }

    @Override
    public List<SkuMappingDTO.ListSkuDTO> listBySkuNoList(List<SkuMappingDTO.ListSkuParamDTO> dataList) {
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.EMPTY_LIST;
        }
        List<String> skuNoList = dataList.stream().map(SkuMappingDTO.ListSkuParamDTO::getSkuNo).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        if (CollectionUtils.isEmpty(skuList)) {
            return Collections.EMPTY_LIST;
        }
        //重置sku含税成本
        resetSkuVo(skuList,dataList);
        //子sku
        List<String> skuIds = skuList.stream().map(SkuVO::getSkuId).collect(Collectors.toList());
        List<BomChildrenSkuDTO> allBomChildrenSkuDTOList = plmTaskFeign.listBomChildBySkuIds(skuIds);
        allBomChildrenSkuDTOList = allBomChildrenSkuDTOList.stream().filter(v->BomTypeEnum.COMBINATION.getType().equals(v.getType())).collect(Collectors.toList());
        //获取到skumappping 的对应关系
        List<SkuMappingEntity> list = lambdaQuery().in(SkuMappingEntity::getProductSkuNo, skuNoList).eq(SkuMappingEntity::getIsExpire, Boolean.FALSE).list();
        List<ListingInfoEntity> listingList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(list)) {
            List<String> listingIds = list.stream().map(SkuMappingEntity::getListingId).collect(Collectors.toList());
            listingList = listingInfoService.listByIds(listingIds);
        }

        //根据ERP仓库查询绑定的海外仓
        List<String> warehouseIds = dataList.stream().map(SkuMappingDTO.ListSkuParamDTO::getWarehouseId).filter(StringUtils::isNotBlank).distinct().collect(Collectors.toList());
        List<OverseasProviderWarehouseDTO.ViewDTO> providerWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(warehouseIds);

        //库存
        RuleTypeEnum warehouseType = RuleTypeEnum.WAREHOUSE;
        List<SkuMappingDTO.ListSkuDTO> resultList = new ArrayList<>();
        for (SkuMappingDTO.ListSkuParamDTO listSkuParamDTO : dataList) {
            SkuVO skuVO = skuList.stream().filter(obj -> obj.getSkuNo().equals(listSkuParamDTO.getSkuNo())).findFirst().orElse(new SkuVO());
            SkuMappingDTO.ListSkuDTO listSkuDTO = new SkuMappingDTO.ListSkuDTO();
            listSkuDTO.setCostSource(CharSequenceUtil.isBlank(skuVO.getCostSource()) ? "采购平均成本" : skuVO.getCostSource());
            listSkuDTO.setProductCost(skuVO.getProductCost());
            listSkuDTO.setFirstMileShippingCost(skuVO.getFirstMileShippingCost());
            listSkuDTO.setClearanceCustomsTax(skuVO.getClearanceCustomsTax());
            listSkuDTO.setProductSkuId(StringUtils.isBlank(skuVO.getSkuId()) ? "" : skuVO.getSkuId());
            listSkuDTO.setProductSkuNo(listSkuParamDTO.getSkuNo());
            listSkuDTO.setProductName(skuVO.getSkuName());
            listSkuDTO.setAdvicePrice(skuVO.getRetailPrice());
            listSkuDTO.setImageUrl(skuVO.getSkuImagesUrl());
            listSkuDTO.setTaxCost(MathUtil.compareTo(skuVO.getActualTaxCost(), MathUtil.ZERO) == MathUtil.ZERO ? skuVO.getTargetTaxCost() : skuVO.getActualTaxCost());
            listSkuDTO.setWarehouseId(listSkuParamDTO.getWarehouseId());
            listSkuDTO.setDictPlatform(listSkuParamDTO.getDictPlatform());
            //组合品的话根据子件计算长宽高重量
            List<BomChildrenSkuDTO> bomChildrenSkuDTOList = allBomChildrenSkuDTOList.stream().filter(v->v.getParentSkuId().equals(skuVO.getSkuId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(bomChildrenSkuDTOList)){
                listSkuDTO.setProductHeight(LengthConverterUtil.mmToCm(skuVO.getProductHeight()));
                listSkuDTO.setProductLength(LengthConverterUtil.mmToCm(skuVO.getProductLength()));
                listSkuDTO.setProductWidth(LengthConverterUtil.mmToCm(skuVO.getProductWidth()));
                listSkuDTO.setGrossWeight(skuVO.getGrossWeight());
                listSkuDTO.setNetWeight(skuVO.getNetWeight());
            }else{
                BigDecimal maxLength = bomChildrenSkuDTOList.stream().map(BomChildrenSkuDTO::getLength).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal maxWidth = bomChildrenSkuDTOList.stream().map(BomChildrenSkuDTO::getWidth).filter(Objects::nonNull).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO);
                BigDecimal totalHeight = bomChildrenSkuDTOList.stream().filter(e->Objects.nonNull(e.getHeight())).map(e -> e.getHeight().multiply(new BigDecimal(e.getQuantity()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal totalGrossWeight = bomChildrenSkuDTOList.stream().filter(e->Objects.nonNull(e.getGrossWeight())).map(e -> e.getGrossWeight().multiply(new BigDecimal(e.getQuantity()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                BigDecimal totalNetWeight = bomChildrenSkuDTOList.stream().filter(e->Objects.nonNull(e.getNetWeight())).map(e -> e.getNetWeight().multiply(new BigDecimal(e.getQuantity()))).reduce(BigDecimal::add).orElse(BigDecimal.ZERO);
                listSkuDTO.setProductHeight(LengthConverterUtil.mmToCm(totalHeight));
                listSkuDTO.setProductLength(LengthConverterUtil.mmToCm(maxLength));
                listSkuDTO.setProductWidth(LengthConverterUtil.mmToCm(maxWidth));
                listSkuDTO.setGrossWeight(totalGrossWeight);
                listSkuDTO.setNetWeight(totalNetWeight);
            }

            //查询库存sku映射
            SkuMappingEntity warehouseSkuMapping = list.stream().filter(
                    obj -> obj.getProductSkuId().equals(listSkuDTO.getProductSkuId()) &&
                            obj.getWarehouseId().equals(listSkuParamDTO.getWarehouseId()) &&
                            warehouseType.equals(obj.getType())
            ).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(warehouseSkuMapping)) {
                //库存sku信息
                SkuMappingEntity skuMappingEntity = warehouseSkuMapping;
                ListingInfoEntity warehouseListing = listingList.stream().filter(obj -> obj.getId().equals(skuMappingEntity.getListingId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(warehouseListing)) {
                    listSkuDTO.setWarehouseSkuNo(warehouseListing.getPlatformSkuNo());
                    listSkuDTO.setWarehouseProductName(warehouseListing.getPlatformSkuName());

                }
            } else {
                OverseasProviderWarehouseDTO.ViewDTO viewDTO = providerWarehouseList.stream().filter(req -> req.getWarehouseId().equals(listSkuParamDTO.getWarehouseId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(viewDTO)) {
                    //查询库存sku映射
                    warehouseSkuMapping = list.stream().filter(
                            obj -> obj.getProductSkuId().equals(listSkuDTO.getProductSkuId()) &&
                                    obj.getDictPlatform().equals(viewDTO.getProviderCode()) &&
                                    warehouseType.equals(obj.getType())
                    ).findFirst().orElse(null);
                    if (ObjectUtils.isNotEmpty(warehouseSkuMapping)) {
                        //库存sku信息
                        SkuMappingEntity finalWarehouseSkuMapping = warehouseSkuMapping;
                        ListingInfoEntity warehouseListing = listingList.stream().filter(obj -> obj.getId().equals(finalWarehouseSkuMapping.getListingId())).findFirst().orElse(null);
                        if (ObjectUtils.isNotEmpty(warehouseListing)) {
                            listSkuDTO.setWarehouseId(viewDTO.getWarehouseId());
                            listSkuDTO.setWarehouseSkuNo(warehouseListing.getPlatformSkuNo());
                            listSkuDTO.setWarehouseProductName(warehouseListing.getPlatformSkuName());
                        }
                    }
                }
            }
            //查询平台sku信息
            SkuMappingEntity platformSkuMapping = list.stream()
                    .filter(obj -> obj.getProductSkuId().equals(listSkuDTO.getProductSkuId())
                            && StringUtils.isEmpty(obj.getWarehouseId())
                            && obj.getDictPlatform().equals(listSkuParamDTO.getDictPlatform())
                            && (StringUtils.isBlank(listSkuParamDTO.getShopId()) || obj.getShopId().equals(listSkuParamDTO.getShopId()))
                            && !warehouseType.equals(obj.getType())
                    ).findFirst().orElse(null);
            if (ObjectUtils.isNotEmpty(platformSkuMapping)) {
                //平台sku信息
                ListingInfoEntity platformListing = listingList.stream().filter(obj -> obj.getId().equals(platformSkuMapping.getListingId())).findFirst().orElse(null);
                if (ObjectUtils.isNotEmpty(platformListing)) {
                    listSkuDTO.setShopId(platformSkuMapping.getShopId());
                    listSkuDTO.setPlatformSkuNo(platformListing.getPlatformSkuNo());
                    listSkuDTO.setPlatformProductName(platformListing.getPlatformSkuName());
                    listSkuDTO.setPlatformSpuNo(platformListing.getPlatformSpuNo());
                    listSkuDTO.setVariantProperty(platformListing.getProductSpec());
                }
            }
            resultList.add(listSkuDTO);
        }
        return resultList;
    }

    /**
     * 重置sku含税成本
     * @param skuList
     * @param dataList
     */
    private void resetSkuVo(List<SkuVO> skuList, List<SkuMappingDTO.ListSkuParamDTO> dataList) {
        if (CollUtil.isEmpty(dataList) || CollUtil.isEmpty(skuList)){
            return;
        }
        //整理查询数据
        InventorySkuCostDTO.QueryB2CDTO queryB2CDTO = buildQueryB2CDTO(dataList,skuList);
        if (Objects.isNull(queryB2CDTO)){
            return;
        }
        List<InventorySkuCostDTO.SkuCostDTO> skuCostDTOS = logisticsFeign.listSkuCostByDetail(queryB2CDTO);
        for(SkuVO skuVO : skuList){
            skuVO.setProductCost(skuVO.getNotTaxCostPrice());
            SkuMappingDTO.ListSkuParamDTO paramDTO = dataList.stream().filter(e -> CharSequenceUtil.isNotBlank(e.getSkuNo()) && e.getSkuNo().equals(skuVO.getSkuNo())).findFirst().orElse(null);
            if (Objects.isNull(paramDTO) || CharSequenceUtil.isBlank(paramDTO.getWarehouseId()) || CharSequenceUtil.isBlank(paramDTO.getShopId()) || Objects.isNull(paramDTO.getBillDate())){
                continue;
            }
            InventorySkuCostDTO.SkuCostDTO skuCostDTO = skuCostDTOS.stream().filter(e -> e.getSkuId().equals(skuVO.getSkuId())).findFirst().orElse(null);
            if (Objects.isNull(skuCostDTO)){
                continue;
            }
            BigDecimal rate = dmpTaskFeign.getRate(paramDTO.getBillDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), skuCostDTO.getCurrency());
            if (Objects.isNull(rate)){
                continue;
            }
            BigDecimal taxRate = Objects.nonNull(skuVO.getTaxRate()) ? skuVO.getTaxRate() : BigDecimal.ZERO;
            BigDecimal percentRate = MathUtil.divide(taxRate, MathUtil.BigDecimal_100);
            skuVO.setNotTaxCostPrice(MathUtil.multiplyWithTwo(skuCostDTO.getProductCost(),rate,4));
            BigDecimal actualTaxCost = MathUtil.add(skuCostDTO.getProductCost(), skuCostDTO.getFirstMileShippingCost()).add(skuCostDTO.getClearanceCustomsTax());
            skuVO.setActualTaxCost(MathUtil.multiplyWithTwo(MathUtil.multiplyWithTwo(actualTaxCost,rate,4), MathUtil.add(BigDecimal.valueOf(1), percentRate),4));
            skuVO.setProductCost(MathUtil.multiplyWithTwo(skuCostDTO.getProductCost(),rate,4));
            skuVO.setFirstMileShippingCost(MathUtil.multiplyWithTwo(skuCostDTO.getFirstMileShippingCost(),rate,4));
            skuVO.setClearanceCustomsTax(MathUtil.multiplyWithTwo(skuCostDTO.getClearanceCustomsTax(),rate,4));
            skuVO.setCostSource(skuCostDTO.getAllocatedMonth().format(DateTimeFormatter.ofPattern("yyyy-MM")) + "财务导入成本");
        }
    }

    private InventorySkuCostDTO.QueryB2CDTO buildQueryB2CDTO(List<SkuMappingDTO.ListSkuParamDTO> dataList, List<SkuVO> skuList) {
        if (CollUtil.isEmpty(dataList)){
            return null;
        }
        String shopId = dataList.stream().map(SkuMappingDTO.ListSkuParamDTO::getShopId).filter(CharSequenceUtil::isNotBlank).findFirst().orElse(CharSequenceUtil.EMPTY);
        LocalDateTime billDate = dataList.stream().map(SkuMappingDTO.ListSkuParamDTO::getBillDate).filter(Objects::nonNull).findFirst().orElse(null);
        if (CharSequenceUtil.isBlank(shopId) || Objects.isNull(billDate)){
            return null;
        }
        ShopInfoEntity shopInfo = shopInfoService.getById(shopId);
        if (Objects.isNull(shopInfo)){
            return null;
        }
        String salesOrgId = shopInfo.getSalesOrgId();
        //数据整理
        List<InventorySkuCostDTO.QueryB2CDetailDTO> detailDTOS = new ArrayList<>();
        for (SkuMappingDTO.ListSkuParamDTO skuParamDTO : dataList){
            if (!CharSequenceUtil.isAllNotBlank(skuParamDTO.getSkuNo(),skuParamDTO.getWarehouseId(),skuParamDTO.getShopId())){
                continue;
            }
            SkuVO skuVO = skuList.stream().filter(e -> e.getSkuNo().equals(skuParamDTO.getSkuNo())).findFirst().orElse(null);
            if (Objects.isNull(skuVO)){
                continue;
            }
            InventorySkuCostDTO.QueryB2CDetailDTO queryB2CDetailDTO = new InventorySkuCostDTO.QueryB2CDetailDTO();
            queryB2CDetailDTO.setSkuId(skuVO.getSkuId());
            queryB2CDetailDTO.setWarehouseId(skuParamDTO.getWarehouseId());
            detailDTOS.add(queryB2CDetailDTO);
        }
        if (CollUtil.isEmpty(detailDTOS)){
            return null;
        }
        InventorySkuCostDTO.QueryB2CDTO queryB2CDTO = new InventorySkuCostDTO.QueryB2CDTO();
        queryB2CDTO.setSalesOrgId(salesOrgId);
        queryB2CDTO.setDetailDTOS(detailDTOS);
        queryB2CDTO.setBillDate(billDate.toLocalDate());
        return queryB2CDTO;
    }

    /**
     * 根据平台sku noList 获取对应的数据
     *
     * @param platformSkuNoList
     * @return java.util.List<com.erp.model.oms.dto.SkuMappingDTO.ListSkuDTO>
     * @author yl
     * @date 2023-09-04 17:11
     */
    @Override
    public List<SkuMappingDTO.SkuDTO> listByPlatformSkuNoList(List<String> platformSkuNoList) {
        if (CollectionUtils.isEmpty(platformSkuNoList)) {
            return Collections.emptyList();
        }
        List<SkuMappingDTO.SkuDTO> list = baseMapper.listByPlatformSkuNoList(platformSkuNoList);
        String platformCode = RuleTypeEnum.PLATFORM.getCode();
        for (SkuMappingDTO.SkuDTO item : list) {
            String type = item.getType();
            String platformSkuNo = item.getPlatformSkuNo();
            String platformProductName = item.getPlatformProductName();
            if (!platformCode.equals(type)) {
                item.setFlagSkuNo(platformSkuNo);
                item.setFlagProductName(platformProductName);
                item.setPlatformSkuNo("");
                item.setPlatformProductName("");
            }
        }
        return list;
    }

    @Override
    public List<SkuMappingDTO.MappingSkuViewDTO> listByPlatformSkuNoAndPlatform(ListingInfoParamDTO listingInfoParamDTO) {
        return baseMapper.listByPlatformSkuNoAndPlatform(listingInfoParamDTO);
    }

    @Override
    public List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIds(List<String> productSkuIdList) {
        if (CollectionUtils.isEmpty(productSkuIdList)) {
            return Collections.emptyList();
        }
        List<SkuMappingDTO.ListStockSkuNoByProductSkuIdView> listStockSkuNoByProductSkuIdViews = baseMapper.listStockSkuNoByProductSkuIds(productSkuIdList);

        List<String> skuIds = listStockSkuNoByProductSkuIdViews.stream().map(req -> req.getProductSkuId()).distinct().collect(Collectors.toList());
        List<SkuVO> skuInfoByIds = plmTaskFeign.listSkuProductByIds(skuIds);

        for (SkuMappingDTO.ListStockSkuNoByProductSkuIdView view : listStockSkuNoByProductSkuIdViews) {
            SkuVO skuVO = skuInfoByIds.stream().filter(req -> req.getSkuId().equals(view.getProductSkuId())).findFirst().orElse(null);
            if (ObjectUtil.isNotEmpty(skuVO)) {
                view.setProductSkuName(skuVO.getSkuName());
            }
        }
        return listStockSkuNoByProductSkuIdViews;
    }

    /**
     * 库存sku 对照表分页
     *
     * @param dto
     * @return com.common.business.vo.PagingVO<com.erp.model.oms.dto.SkuMappingDTO.WarehousePagingViewDTO>
     * @author yl
     * @date 2023-08-21 9:56
     */
    @Override
    public PagingVO<SkuMappingDTO.WarehousePagingViewDTO> warehousePaging(PagingDTO<SkuMappingDTO.WarehousePagingParamDTO> dto) {
        SkuMappingDTO.WarehousePagingParamDTO params = dto.getParams();
        params.setPermissionSql(getWarehousePermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        params.setType(RuleTypeEnum.WAREHOUSE.getCode());
        IPage pageData = baseMapper.warehousePaging(query, params);
        List<SkuMappingDTO.WarehousePagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        fillWarehouseDb(list);
        return new PagingVO<>(pageData);
    }

    /**
     * 如果没有仓库时，默认所有人可以查看
     * @return
     */
    private String getWarehousePermissionSql() {
        String warehousePermissionSql = authDataFeign.getWarehousePermissionSql("sm.warehouse_id");
        if (StringUtils.isBlank(warehousePermissionSql)) {
            return "";
        }
        return " AND ( (sm.warehouse_id = '') OR " + " (1=1 " +warehousePermissionSql+ " ) ) ";
    }


    /**
     * 填充库存sku
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-08-21 10:15
     */
    private void fillWarehouseDb(List<SkuMappingDTO.WarehousePagingViewDTO> list) {
        List<String> skuIdList = list.stream().map(SkuMappingDTO.WarehousePagingViewDTO::getProductSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        for (SkuMappingDTO.WarehousePagingViewDTO item : list) {
            String skuId = item.getProductSkuId();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().map(SkuVO::getSkuName).orElse("");
            item.setProductName(skuName);
            item.setMatchResultStr(ListingMatchResultEnum.getName(item.getMatchResult()));
            item.setHasMappingAllStr(item.getHasMappingAll() ? "是" : "否");
        }

    }

    private void fillCustomerDb(List<SkuMappingDTO.CustomerPagingViewDTO> list,PagingDTO<SkuMappingDTO.CustomerPagingParamDTO> dto) {
        //平台信息
        String type = DictBasicTypeEnum.SALES_PLATFORM.getType();
        List<DictBasicDTO.ViewDTO> dictList = dictBasicService.getByKey(type);
        for (SkuMappingDTO.CustomerPagingViewDTO item : list) {
            item.setMatchResultStr(ListingMatchResultEnum.getName(item.getMatchResult()));
            String platformTypeName = dictList.stream().filter(obj -> obj.getValue().equals(item.getPlatformName())).findFirst().flatMap(obj -> Optional.ofNullable(obj.getName())).orElse("");
            item.setPlatformName(platformTypeName);
            if(StringUtils.isNotBlank(item.getProductImageUrl()) && dto.getParams().isExport()){
                item.setImageByte(FastDFSClientUtil.getFileByte(item.getProductImageUrl()));
            }
            if (CharSequenceUtil.isNotBlank(item.getLabelUrl()) && LabelSourceTypeEnum.CUSTOMER.getCode().equals(item.getLabelSourceType())){
                item.setIsUploadLabel(Boolean.TRUE);
                item.setUploadLabelStr("是");
                item.setLabelUrlStr(FastDFSClientUtil.publicUrl + "/" + item.getLabelUrl());
            }else {
                item.setIsUploadLabel(Boolean.FALSE);
                item.setUploadLabelStr("否");
                if (CharSequenceUtil.isNotBlank(item.getLabelUrl())){
                    item.setLabelUrlStr(FastDFSClientUtil.publicUrl + "/" + item.getLabelUrl());
                }
            }
        }

    }

    /**
     * 检查库存sku 是否存在
     *
     * @param id
     * @param listingId
     * @param warehouseId
     */
    private void checkWarehouseSkuExist(String id, String listingId, String warehouseId, String skuId) {
        LambdaQueryWrapper<SkuMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuMappingEntity::getListingId, listingId);
        queryWrapper.eq(SkuMappingEntity::getIsExpire, Boolean.FALSE);
        queryWrapper.eq(SkuMappingEntity::getType, RuleTypeEnum.WAREHOUSE);
        queryWrapper.eq(SkuMappingEntity::getWarehouseId, warehouseId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SkuMappingEntity::getId, id);
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException("同仓库库存SKU只能对应一个产品SKU");
        }
    }

    private void checkExist(String id, String listingId, String shopId) {
        LambdaQueryWrapper<SkuMappingEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SkuMappingEntity::getListingId, listingId);
        queryWrapper.eq(SkuMappingEntity::getIsExpire, Boolean.FALSE);
        queryWrapper.eq(SkuMappingEntity::getShopId, shopId);
        if (StringUtils.isNotBlank(id)) {
            queryWrapper.ne(SkuMappingEntity::getId, id);
        }
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw new ServiceException(ApiError.ERROR_92052);
        }


    }

    @Override
    public void checkHistory(String id, String listingId, String shopId, String productSkuId) {
        List<SkuMappingEntity> oldEntities = this.baseMapper.findHistory(id, listingId, shopId, productSkuId);
        if(CollectionUtils.isEmpty(oldEntities)){
            return;
        }
        throw new ServiceException(ApiError.SKU_MAPPING_NOT_ALLOW_HISTORY, oldEntities.get(0).getExpireTime().toString());
    }

    /**
     * 填充数据
     *
     * @param list
     * @return void
     * @author yl
     * @date 2023-06-29 19:15
     */
    private void fillDb(List<SkuMappingDTO.PagingViewDTO> list) {
        List<String> mainIds = list.stream().map(SkuMappingDTO.PagingViewDTO::getId).collect(Collectors.toList());
        Map<String, List<SkuMappingExtendDTO.ListDTO>> extendMap =  skuMappingExtendService.mapByMainIds(mainIds, false);

        List<String> skuIdList = list.stream().map(SkuMappingDTO.PagingViewDTO::getProductSkuId).collect(Collectors.toList());
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(skuIdList);
        //子件信息
        List<BomChildrenSkuDTO> bomChildrenSkuList = plmTaskFeign.listBomChildBySkuIds(skuIdList);

        //原产地名称
        List<DictBasicDTO.ViewDTO> originList = dictBasicService.getByKey(DictBasicTypeEnum.INVOICE_TAX_NFE_ORIGIN.getType());
        Map<String, String> originMap = originList.stream().collect(Collectors.toMap(DictBasicDTO.ViewDTO::getValue, DictBasicDTO.ViewDTO::getName));

        for (SkuMappingDTO.PagingViewDTO item : list) {
            String skuId = item.getProductSkuId();
            String skuName = skuList.stream().filter(s -> s.getSkuId().equals(skuId)).
                    findFirst().map(SkuVO::getSkuName).orElse("");
            item.setProductName(skuName);
            item.setMatchResultStr(ListingMatchResultEnum.getName(item.getMatchResult()));
            //查询sku是否存在子SKU
            List<BomChildrenSkuDTO> sonSkuList = bomChildrenSkuList.stream()
                    .filter(req -> req.getParentSkuId().equals(item.getProductSkuId()) && BomTypeEnum.COMBINATION.getType().equalsIgnoreCase(req.getType()))
                    .collect(Collectors.toList());
            if (CollectionUtils.isNotEmpty(sonSkuList)) {
                item.setIsCombination(Boolean.TRUE);
            } else {
                item.setIsCombination(Boolean.FALSE);
            }
            if (item.getIsCombination()){
                List<SkuMappingExtendDTO.ListDTO> listDTO = extendMap.getOrDefault(item.getId(), Collections.emptyList());
                item.setExtendList(listDTO);
            }
            //原产地名称
            item.setDictOriginName(originMap.get(item.getDictOrigin()));
        }
    }


    /**
     * 获取到有效的sku 对照表
     *
     * @param
     * @return java.util.List<com.erp.model.oms.entity.SkuMapingEntity>
     * @author yl
     * @date 2023-06-29 11:29
     */
    private List<SkuMappingEntity> listEffectiveList() {
        List<SkuMappingEntity> resultList = this.lambdaQuery().eq(SkuMappingEntity::getIsExpire, Boolean.FALSE).list();
        return resultList;
    }

    @Override
    public List<SkuMappingEntity> listByListingIds(List<String> listingIds) {
        if(CollectionUtils.isEmpty(listingIds)){
            return new ArrayList<>();
        }
        return lambdaQuery()
                .in(SkuMappingEntity::getListingId, listingIds)
                .eq(SkuMappingEntity::getIsExpire, false)
                .list();
    }

    @Override
    public SkuMappingEntity getByAttribute(String productSkuId, String warehouseId, RuleTypeEnum typeEnum) {
        return lambdaQuery()
                .eq(SkuMappingEntity::getProductSkuId, productSkuId)
                .eq(SkuMappingEntity::getWarehouseId, warehouseId)
                .eq(SkuMappingEntity::getType, typeEnum)
                .eq(SkuMappingEntity::getIsExpire, false)
                .last(" LIMIT 1")
                .one();
    }

    @Override
    public Boolean updateSkuMapping(SkuMappingDTO.UpdateSkuMappingDTO dto) {
        LocalDateTime now = LocalDateTime.now();
        return this.lambdaUpdate().eq(SkuMappingEntity::getListingId,dto.getListingId()).
                set(SkuMappingEntity::getProductSkuId,dto.getProductSkuId()).
                set(SkuMappingEntity::getProductName,dto.getProductName()).
                set(SkuMappingEntity::getIsExpire,dto.getIsExpire()).
                set(SkuMappingEntity::getEffectiveTime,now).
                set(SkuMappingEntity::getExpireTime,now.plusYears(MathUtil.NUMBER_100)).
                set(SkuMappingEntity::getProductSkuNo,dto.getProductSkuNo()).
                update(new SkuMappingEntity());
    }

    @Override
    public List<ListingInfoWithSkuMappingDTO> findListDto(ListingInfoParamDTO dto) {

        // 指定过期时间匹配小于或等于过期时间
        List<ListingInfoWithSkuMappingDTO> list = baseMapper.listByParams(dto);
        Map<String, List<ListingInfoWithSkuMappingDTO>> groupMap = list.stream().collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getListingId));
        // 过滤取指定过期时间匹配小于或等于过期时间/空=最新匹配
        if (null != dto.getLastExpireDate()){
            list = groupMap.values()
                    .stream()
                    .map(listingInfoWithSkuMappingDTOS -> ListingInfoWithSkuMappingDTO.getActiveOne(listingInfoWithSkuMappingDTOS, dto.getLastExpireDate()))
                    .collect(Collectors.toList());
        }
        if(CollectionUtils.isNotEmpty(dto.getWarehouseIdList())){
            List<OverseasProviderWarehouseDTO.ViewDTO> providerWarehouseList = wmsOverseasWarehouseFeign.listByWarehouseIdList(dto.getWarehouseIdList());
            List<String> authIdList = providerWarehouseList.stream().map(OverseasProviderWarehouseDTO.ViewDTO::getMainId).distinct().collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(authIdList)){
                list = list.stream().filter(v->StringUtils.isBlank(v.getAuthId()) || authIdList.contains(v.getAuthId())).collect(Collectors.toList());
            }
        }

        if (CollectionUtils.isEmpty(list) || RuleTypeEnum.WAREHOUSE.getCode().equalsIgnoreCase(dto.getType())){
            return list;
        }
        List<String> mainIds = list.stream().map(ListingInfoWithSkuMappingDTO::getTableId).collect(Collectors.toList());
        Map<String, List<SkuMappingExtendDTO.ListDTO>> entendMap = skuMappingExtendService.mapByMainIds(mainIds, false);
        for (ListingInfoWithSkuMappingDTO mappingDTO : list) {
            List<SkuMappingExtendDTO.ListDTO> extendList = entendMap.get(mappingDTO.getTableId());
            if (CollectionUtils.isEmpty(extendList)){
                continue;
            }
            Map<String, String> extendMap = extendList.stream().collect(Collectors.toMap(SkuMappingExtendDTO.ListDTO::getWarehouseManageType, SkuMappingExtendDTO.ListDTO::getWarehouseDeliveryType));
            mappingDTO.setExtendMap(extendMap);
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        SkuMappingEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("SKU映射关系不存在"));
        ListingInfoEntity listingInfoEntity = listingInfoService.getById(entity.getListingId());
        // 客户sku
        if (RuleTypeEnum.CUSTOMER == entity.getType()) {
            this.deleteCustomer(entity, listingInfoEntity);
            return BatchResultDTO.success(entity.getId(), entity.getProductName(), OperationTypeEnum.DELETE);
        }
        // 无平台
        if (StringUtils.isBlank(entity.getDictPlatform())) {
            this.deleteAll(id, listingInfoEntity);
            return BatchResultDTO.success(entity.getId(), entity.getProductName(), OperationTypeEnum.DELETE);
        }
        // 销售平台
        if (RuleTypeEnum.PLATFORM == entity.getType() && !PlatformDictEnum.hasConnectionPlatform().contains(entity.getDictPlatform())) {
            this.deleteAll(id, listingInfoEntity);
            return BatchResultDTO.success(entity.getId(), entity.getProductName(), OperationTypeEnum.DELETE);
        }
        // 仓库平台
        if (RuleTypeEnum.WAREHOUSE == entity.getType() && null == OmsPlatformEnum.getByCode(entity.getDictPlatform())) {
            this.deleteAll(id, listingInfoEntity);
            return BatchResultDTO.success(entity.getId(), entity.getProductName(), OperationTypeEnum.DELETE);
        }

        // 有平台
        throw new ServiceException("API接口新增的平台SKU和库存SKU不允许删除");
    }

    private void deleteCustomer(SkuMappingEntity entity, ListingInfoEntity listingInfoEntity) {
        if(soInfoService.existsByCustomerAndSku(listingInfoEntity.getAuthId(),listingInfoEntity.getPlatformSkuNo())){
            throw new ServiceException("客户SKU:{}已被订单引用,不允许删除",listingInfoEntity.getPlatformSkuNo());
        }
        //有订单引用不允许删除
        if (!this.removeById(entity.getId())) {
            throw new ServiceException("删除映射失败,请重试");
        }
        if (!listingInfoService.removeById(listingInfoEntity.getId())) {
            throw new ServiceException("删除listing失败");
        }

        String msg =  CharSequenceUtil.format("用户【{}】删除客户sku【{}】", UserContext.getDefaultLoginUser().getUserName(), listingInfoEntity.getPlatformSkuNo());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), listingInfoEntity.getId(), "删除操作");
    }

    @Override
    public List<SkuMappingDTO.ListSkuResultDTO> listBySkuList(List<SkuMappingDTO.ListingSkuParamDTO> listSkuParamList, String dictPlatform, String type, String warehouseId) {
        //查询仓库对应的海外仓授权
        OverseasProviderEntity overseasProviderEntity = overseasProviderFeign.getByWarehouseId(warehouseId);
        if(Objects.isNull(overseasProviderEntity)){
            throw new ServiceException("海外仓库授权不存在");
        }
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByAuthIds(Collections.singletonList(overseasProviderEntity.getId()));
        if(CollectionUtils.isEmpty(listingInfoEntityList)){
            throw new ServiceException("海外仓库授权下没有对应的listing");
        }
        List<String> skuIdList = listSkuParamList.stream().map(SkuMappingDTO.ListingSkuParamDTO::getSkuId).distinct().collect(Collectors.toList());
        List<String> warehouseIdList = listSkuParamList.stream().map(SkuMappingDTO.ListingSkuParamDTO::getWarehouseId).distinct().collect(Collectors.toList());
        List<String> listingIds = listingInfoEntityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<SkuMappingEntity> skuMappingList = this.listByInfo(skuIdList, dictPlatform, type,listingIds);
        List<SkuMappingEntity> wantSkuMappingList = new ArrayList<>(skuMappingList.size());
        for (SkuMappingEntity skuMappingEntity : skuMappingList) {
            Boolean hasMappingAll = skuMappingEntity.getHasMappingAll();
            if (hasMappingAll) {
                wantSkuMappingList.add(skuMappingEntity);
            } else {
                if (warehouseIdList.contains(skuMappingEntity.getWarehouseId())) {
                    wantSkuMappingList.add(skuMappingEntity);
                }
            }
        }

        List<SkuMappingDTO.ListSkuResultDTO> resultList = new ArrayList<>(wantSkuMappingList.size());
        for (SkuMappingEntity item : wantSkuMappingList) {
            SkuMappingDTO.ListSkuResultDTO resultDTO = new SkuMappingDTO.ListSkuResultDTO();
            String listingId = item.getListingId();
            resultDTO.setSkuId(item.getProductSkuId());
            resultDTO.setSkuNo(item.getProductSkuNo());
            resultDTO.setListingId(listingId);
            ListingInfoEntity listingEntity = listingInfoEntityList.stream().filter(l -> l.getId().equals(listingId)).findFirst().orElse(null);
            if (Objects.nonNull(listingEntity)) {
                resultDTO.setType(listingEntity.getType());
                resultDTO.setPlatformSkuNo(listingEntity.getPlatformSkuNo());
                resultDTO.setPlatformSkuName(listingEntity.getPlatformSkuName());
                resultDTO.setPlatformSpuNo(listingEntity.getPlatformSpuNo());
                resultDTO.setPlatformSpuName(listingEntity.getPlatformSpuName());
                resultDTO.setDictPlatform(dictPlatform);
                resultDTO.setPlatformSkuId(listingEntity.getPlatformSkuId());
                resultList.add(resultDTO);
            }
        }

        return resultList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAll(String id, ListingInfoEntity listingInfoEntity) {
        SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
        skuMappingEntity.setId(id);
        skuMappingEntity.setIsExpire(true);
        this.updateById(skuMappingEntity);
        if (!this.removeById(id)) {
            throw new ServiceException("删除映射失败,请重试");
        }
    }

    @Override
    public PagingVO<OperateLogDTO.ListDTO> getLog(PagingDTO<BaseIdDTO.SearchDTO> dto) {
        SkuMappingEntity skuMappingEntity = this.getById(dto.getParams().getId());
        if (Objects.isNull(skuMappingEntity)) {
            throw new ServiceException("sku对照表为空");
        }
        //查询Listing日志
        PagingVO<OperateLogDTO.ListDTO> listDTOPagingVO = new PagingVO<>();
        PagingDTO<OperateLogDTO.SearchDTO> logDTO = new PagingDTO<>();
        OperateLogDTO.SearchDTO logSearchDTO = new OperateLogDTO.SearchDTO();
        logDTO.setCurrPage(dto.getCurrPage());
        logDTO.setPageSize(dto.getPageSize());
        logDTO.setParams(logSearchDTO);
        if (StringUtils.isNotBlank(skuMappingEntity.getListingId())) {
            logSearchDTO.setBusinessId(skuMappingEntity.getListingId());
            logSearchDTO.setModuleType(ModuleTypeEnum.LISTING_INFO.getCode());
            logSearchDTO.setSearchKey(dto.getParams().getSearchKey());
            listDTOPagingVO = operateLogService.paging(logDTO);
        }

        //查询sku对照表日志
        if (listDTOPagingVO.getCurrPage() >= listDTOPagingVO.getTotalPage()) {
            List<OperateLogDTO.ListDTO> allList = (List<OperateLogDTO.ListDTO>) listDTOPagingVO.getList();
            if(allList.size() < listDTOPagingVO.getPageSize()){
                logSearchDTO.setBusinessId(skuMappingEntity.getId());
                logSearchDTO.setModuleType(ModuleTypeEnum.SKU_MAPPING.getCode());
                logSearchDTO.setSearchKey(dto.getParams().getSearchKey());
                logDTO.setCurrPage(1);
                PagingVO<OperateLogDTO.ListDTO> skuMappingPagingVO = operateLogService.paging(logDTO);
                List<OperateLogDTO.ListDTO> skuLogList = (List<OperateLogDTO.ListDTO>) skuMappingPagingVO.getList();
                allList = CollectionUtils.isEmpty(allList)? new ArrayList<>():allList;
                allList.addAll(skuLogList);
                listDTOPagingVO.setList(allList);
                listDTOPagingVO.setTotalCount(listDTOPagingVO.getTotalCount()+skuLogList.size());
                if(listDTOPagingVO.getTotalPage() == 0){
                    listDTOPagingVO.setTotalPage(1);
                }
            }
        }
        return listDTOPagingVO;
    }

    @Override
    public List<ListingInfoWithSkuMappingDTO> listByErpSkuIdAndType(List<String> erpSkuIdList,String provideCode,String warehouseId,String shopId) {
        if(CollectionUtils.isEmpty(erpSkuIdList) && StringUtils.isBlank(provideCode) && StringUtils.isBlank(warehouseId) &&StringUtils.isBlank(shopId)){
            return new ArrayList<>();
        }
        return baseMapper.listByErpSkuIdAndType(erpSkuIdList,provideCode, warehouseId,shopId);
    }

    @Override
    public List<ListingAdvanceQueryDTO> advanceQuerySku(AdvanceQueryContainer advanceQueryContainer) {
        return baseMapper.advanceQuerySku(advanceQueryContainer);
    }

    private List<SkuMappingEntity> listByInfo(List<String> skuIdList, String dictPlatform, String type,List<String> listingIds) {
        LocalDateTime now = LocalDateTime.now();
        return this.lambdaQuery().
                ge(SkuMappingEntity::getExpireTime, now).
                le(SkuMappingEntity::getEffectiveTime, now).
                in(CollectionUtils.isNotEmpty(skuIdList), SkuMappingEntity::getProductSkuId, skuIdList).
                in(CollectionUtils.isNotEmpty(listingIds), SkuMappingEntity::getListingId, listingIds).
                eq(SkuMappingEntity::getDictPlatform, dictPlatform).
                eq(SkuMappingEntity::getType, type).
                list();
    }

    @Override
    public Map<String, List<ListingInfoWithSkuMappingDTO>> mapListingByPlatformSkuNo(List<String> platformSkuList, List<String> platformSpuList, String dictPlatform, String shopId, LocalDateTime platformOrderCreateTime, Boolean isExpire) {
        if (CollectionUtils.isEmpty(platformSkuList) && !PlatformDictEnum.SHOPEE.getCode().equals(dictPlatform)) {
            return Collections.emptyMap();
        }
        // 构建请求参数
        ListingInfoParamDTO paramDTO = constructDto(platformSkuList, platformSpuList, dictPlatform, Collections.singletonList(shopId), platformOrderCreateTime, isExpire);
        // 查询ListingInfo和skuMapping的关系
        List<ListingInfoWithSkuMappingDTO> listDto = this.findListDto(paramDTO);
        if (CollectionUtils.isEmpty(listDto)){
            return Collections.emptyMap();
        }
        if (PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(dictPlatform)){
            return listDto.stream()
                    .collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getPlatformSpuNo));
        }else{
            return listDto.stream()
                    .collect(Collectors.groupingBy(ListingInfoWithSkuMappingDTO::getPlatformSkuNo));
        }
    }


    /**
     * 检查或获取映射关系
     */
    @Override
    public ListingInfoWithSkuMappingDTO checkAndMappingDTO(List<ListingInfoWithSkuMappingDTO> mappingDTOList, String platformSpuNo, String dictPlatform, String platformSkuNo) {
        if (CollectionUtils.isEmpty(mappingDTOList)) {
            return null;
        }
        if (1 == mappingDTOList.size()){
            return mappingDTOList.get(0);
        }
        // 美客多同店铺存在相同SkuNo需要配合平台产ID/SPU查询
        if (StringUtils.isBlank(platformSpuNo) && (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.TE_MU.getCode().equalsIgnoreCase(dictPlatform)
        )){
            throw new ServiceException("来源平台SPU为空");
        }
        if(PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(dictPlatform)){
            // 美客多根据订单中的店铺+平台产品ID+平台SKU匹配对照表映射，没找到listing数据时，再根据店铺+平台产品ID匹配映射
            return mappingDTOList.stream()
                    .filter(v -> v.getPlatformSkuNo().equals(platformSkuNo))
                    .findFirst()
                    .orElse(mappingDTOList.isEmpty() ? null : mappingDTOList.get(0));
        }
        // 查询相同SPU记录
        return mappingDTOList.stream()
                .filter(e->e.getPlatformSpuNo().equalsIgnoreCase(platformSpuNo))
                .findFirst()
                .orElse(null);
    }

    /**
     * 根据平台sku记录获取变更历史记录
     * @param id
     * @return
     */
    @Override
    public List<SkuMappingEntity> listHistoryByListingId(String id) {
        if (StringUtils.isBlank(id)){
            return Collections.emptyList();
        }
        List<SkuMappingEntity> list = baseMapper.listHistoryByListingId(id);
        //修改最后一条数据
        if (!list.isEmpty()){
            SkuMappingEntity skuMappingEntity = list.get(list.size() - 1);
            skuMappingEntity.setExpireTime(null);
            list.set(list.size() - 1, skuMappingEntity);
        }
        return list;
    }

    @Override
    public List<SkuMappingDTO.WarehouseSkuDTO> listByWarehouseAndPlatformSku(String warehouseId, List<String> platformSkuNoList) {
        if(StringUtils.isBlank(warehouseId) || CollectionUtils.isEmpty(platformSkuNoList)){
            return new ArrayList<>();
        }
        return baseMapper.listByWarehouseAndPlatformSku(warehouseId,platformSkuNoList);
    }

    @Override
    public PagingVO<SkuMappingDTO.PagingViewDTO> exportPlatformSku(PagingDTO<SkuMappingDTO.ExportDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        dto.getParams().setType(RuleTypeEnum.PLATFORM.getCode());
        Page<SkuMappingDTO.PagingViewDTO> page = baseMapper.listExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());

        fillDb(page.getRecords());
        return new PagingVO<>(page);
    }

    @Override
    public PagingVO<SkuMappingDTO.WarehousePagingViewDTO> exportWarehouseSku(PagingDTO<SkuMappingDTO.ExportWarehouseSkuDTO> dto) {
        dto.getParams().setType(RuleTypeEnum.WAREHOUSE.getCode());
        dto.getParams().setPermissionSql(getWarehousePermissionSql());
        Page<SkuMappingDTO.WarehousePagingViewDTO> page = baseMapper.listWarehouseExport(new Page<>(dto.getCurrPage(), dto.getPageSize()), dto.getParams());
        fillWarehouseDb(page.getRecords());
        return new PagingVO<>(page);
    }
    @Override
    public ListingInfoParamDTO constructDto(List<String> platformSkuList,
                                            List<String> platformSpuList,
                                            String dictPlatform,
                                            List<String> shopIdList,
                                            LocalDateTime platformOrderCreateTime,
                                            Boolean isExpire
    ) {
        ListingInfoParamDTO paramDTO = new ListingInfoParamDTO();
        paramDTO.setPlatform(dictPlatform);
        paramDTO.setShopIdList(shopIdList);
        paramDTO.setType(RuleTypeEnum.PLATFORM.getCode());

        // 亚马逊订单来源spu为空
        if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(dictPlatform)){
            // 过滤空spu
            platformSpuList = platformSpuList.stream()
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        }

        // 校验平台sku和平台spu必须其一不为空
        if (CollectionUtils.isEmpty(platformSkuList) && CollectionUtils.isEmpty(platformSpuList)){
            ServiceException.runError("参数异常:平台sku和平台spu都为空");
        }

        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dictPlatform)
        ){
            // 速卖通/Shopify/Tiktok订单SKU为空的情况只根据PlatformSkuNo匹配
            if (CollectionUtils.isNotEmpty(platformSkuList) && platformSkuList.stream().allMatch(StringUtils::isNotBlank)){
                paramDTO.setPlatformSkuNoList(platformSkuList);
            }
            // 美客多根据订单中的店铺+平台产品ID+平台SKU匹配对照表映射，没找到listing数据时，再根据店铺+平台产品ID匹配映射
            if(PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dictPlatform)
                    || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(dictPlatform)){
                paramDTO.setPlatformSkuNoList(new ArrayList<>());
            }
            // 存在空SKU忽略PlatformSkuNo查询
        } else {
            // 其他平台正常通过平台SKU查询
            paramDTO.setPlatformSkuNoList(platformSkuList);
        }

        // 速卖通同店铺存在相同SkuNo需要配合平台产ID/SPU查询
        if (PlatformDictEnum.ALI_EXPRESS.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.MERCADOLIBRE.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.MERCADOLIBRE_LOCAL.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.TIK_TOK.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.TE_MU.getCode().equalsIgnoreCase(dictPlatform)
                || PlatformDictEnum.SHOPIFY.getCode().equalsIgnoreCase(dictPlatform)
        ){
            paramDTO.setPlatformSpuNoList(platformSpuList);
        }
        paramDTO.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
        paramDTO.setLastExpireDate(platformOrderCreateTime);
        paramDTO.setIsExpire(isExpire);
        return paramDTO;
    }

	@Override
	public List<OmsPushMsgEntity> syncDataToSdy(LocalDateTime startTime, LocalDateTime endTime) {
		List<OmsPushMsgEntity> omsPushMsgEntityList = new ArrayList<>();
		List<SkuMappingEntity> skuMappingEntityList = lambdaQuery().ge(SkuMappingEntity::getUpdateTime, startTime).le(SkuMappingEntity::getUpdateTime, endTime).list();
		if(CollUtil.isNotEmpty(skuMappingEntityList)) {
			for(SkuMappingEntity skuMappingEntity : skuMappingEntityList) {
				if(StringUtils.isBlank(skuMappingEntity.getProductSkuNo())) {
					log.warn("sku映射产品为空，不推送：{}" , skuMappingEntity.getId());
					continue;
				}
				if(RuleTypeEnum.PLATFORM != skuMappingEntity.getType()) {
					log.warn("sku映射类型不为平台，不推送：{}" , skuMappingEntity.getId());
					continue;
				}
				String operate = SyncOperateEnum.OPERATE_APPROVE.getCode();
				if(skuMappingEntity.getIsExpire()) {
					operate = SyncOperateEnum.OPERATE_DELETE.getCode();
				}
				OmsPushMsgEntity omsPushMsgEntity = new OmsPushMsgEntity();
		        omsPushMsgEntity.setSourceId(skuMappingEntity.getId());
		        Map<String, Object> newSyncDataToSdy = this.newSyncDataToSdy(skuMappingEntity, operate);
		        omsPushMsgEntity.setSourceCode(newSyncDataToSdy.get("map_product_code").toString());
		        omsPushMsgEntity.setSourceType(SourceTypeEnum.SDY_SKU_MAPPING.getCode());
				omsPushMsgEntity.setPushData(JSON.toJSONString(newSyncDataToSdy));
		        omsPushMsgEntity.setTargetPlatform(DmpBasicSystemCodeEnum.SDY.getCode());
		        omsPushMsgEntity.setSyncOperate(operate);
		        omsPushMsgEntityList.add(omsPushMsgEntity);
			}
			if(CollUtil.isNotEmpty(omsPushMsgEntityList)) {
				omsPushMsgService.saveBatch(omsPushMsgEntityList);
			}
		}
		return omsPushMsgEntityList;
	}


	@Override
	public Map<String, Object> newSyncDataToSdy(SkuMappingEntity entity, String operate) {
		List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(Arrays.asList(entity.getProductSkuId()));
		String productName = "";
		if(CollUtil.isNotEmpty(skuList)) {
			productName = skuList.get(0).getSkuName();
		}
		ListingInfoEntity listingInfoEntity = listingInfoService.getById(entity.getListingId());
		Map<String, Object> resultMap = new HashMap<>();
		resultMap.put("biz_uni_key", entity.getId());
		resultMap.put("mapping_system_attribute", "主数据来源");
		resultMap.put("mapping_system_mdm_type", "msku");
		String platformSkuNo = listingInfoEntity.getPlatformSkuNo();
		if(StringUtils.isBlank(platformSkuNo)) {
			platformSkuNo = entity.getProductSkuNo();
		}
		resultMap.put("map_product_code", platformSkuNo);
		String platformSkuName = listingInfoEntity.getPlatformSkuName();
		if(StringUtils.isBlank(platformSkuName)) {
			platformSkuName = productName;
		}
		if(StringUtils.isBlank(productName)) {
			productName = platformSkuName;
		}
		resultMap.put("map_product_name", platformSkuName);
		resultMap.put("mdm_system", "SDC");
		resultMap.put("product_code", entity.getProductSkuNo());
		resultMap.put("product_name", productName);

		if(SyncOperateEnum.OPERATE_DELETE.getCode().equals(operate)) {
			resultMap.put("status", "未匹配");
		}else {
			resultMap.put("status", "已匹配");
		}
		return resultMap;
	}
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateNotMatch(SkuMappingDTO.UpdateNotMatchDTO dto) {
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByIds(dto.getListingIds());
        if (CollectionUtils.isEmpty(listingInfoEntityList)){
            throw new ServiceException("listing不存在");
        }
        if(listingInfoEntityList.stream().anyMatch(v->ListingMatchResultEnum.TRUE.getCode().equals(v.getMatchResult()))
        || listingInfoEntityList.stream().anyMatch(v->ListingMatchResultEnum.NOT.getCode().equals(v.getMatchResult()))){
            throw new ServiceException("只有未匹配的数据可以操作无需匹配");
        }
        listingInfoEntityList.forEach(v->{
            v.setMatchResult(ListingMatchResultEnum.NOT.getCode());
            v.setRemark(dto.getRemark());
            String msg =  CharSequenceUtil.format("用户【{}】更新状态为无需匹配", UserContext.getDefaultLoginUser().getUserName());
            operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), v.getId(), "状态变更");
        });
        listingInfoService.updateBatchById(listingInfoEntityList);
    }

    @Override
    public PagingVO<SkuMappingDTO.SyncPlatformProductView> syncPlatformProductView(PagingDTO<AdvanceQueryContainer> advanceQueryDTO) {
        LoginUser userInfo = UserContext.getDefaultLoginUser();
        List<ShopSysUserAuthDTO.ViewDTO> shopSysUserAuthList = shopSysUserAuthService.listShopSysUserAuthByUserIdList(Arrays.asList(userInfo.getUid()));
        if (CollectionUtils.isEmpty(shopSysUserAuthList)) {
            return new PagingVO<>();
        }
        List<ShopSysUserAuthDTO.ViewShopDTO> detailList = shopSysUserAuthList.get(0).getDetailList();
        detailList = detailList.stream().filter(e -> !ShopTypeEnum.INTERNAL.getCode().equals(e.getType())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(detailList)) {
            return new PagingVO<>();
        }
        List<String> shopIds = detailList.stream().map(ShopSysUserAuthDTO.ViewShopDTO::getShopId).collect(Collectors.toList());
        return shopInfoService.pageAuthShop(advanceQueryDTO,shopIds);
    }

    @Override
    public PagingVO<SkuMappingDTO.SyncWarehouseProductView> syncWarehouseProductView(PagingDTO<AdvanceQueryContainer> advanceQueryDTO) {
        return wmsOverseasWarehouseFeign.pageWarehouseProduct(advanceQueryDTO);
    }
    @Override
    public void syncPlatformProduct(List<String> ids) {
        List<ShopInfoEntity> shopInfoEntityList = shopInfoService.listByIds(ids);
        if(CollectionUtils.isEmpty(shopInfoEntityList)){
            throw new ServiceException("店铺不存在");
        }
        if(shopInfoEntityList.stream().anyMatch(v->!AuthStatusEnum.ALREADY.getCode().equals(v.getAuthStatus()))){
            throw new ServiceException("只有已授权店铺可以同步");
        }
        if(shopInfoEntityList.stream().anyMatch(ShopInfoEntity::getDisabled)){
            throw new ServiceException("已禁用店铺无法同步");
        }
        List<DmpInoutDTO.CreateInputDTO> createDTOList = new ArrayList<>();
        for (ShopInfoEntity shopInfoEntity : shopInfoEntityList) {
            DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
            dto.setSystemCode(shopInfoEntity.getDictPlatform());
            dto.setBillType(BusinessTypeEnum.PRODUCT.getCode());
            dto.setNextLevelId(shopInfoEntity.getId());
            // 亚马逊指定正常任务类型兼容限流重试
            if (PlatformDictEnum.AMAZON.getCode().equalsIgnoreCase(shopInfoEntity.getDictPlatform())){
                dto.setTaskType(DmpInputTaskTaskTypeEnum.NORMAL.getCode());
                // 手动指定创建报告
                Map<String, Boolean> map = Collections.singletonMap("hasCreateReport",true);
                dto.setDetailExtendJson(JSON.toJSONString(map));
            }
            createDTOList.add(dto);
        }
        try {
            dmpInoutTaskFeign.doInputTask(createDTOList);
        }catch (Exception e){
            if (e.getMessage().contains("任务不存在")){
                throw new ServiceException("店铺授权异常，请检查店铺授权");
            }
            throw new ServiceException(e.getMessage());
        }

    }

    @Override
    public void syncWarehouseProduct(List<String> ids) {
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.getByIds(OverseasProviderEntity.class,ids);
        if(CollectionUtils.isEmpty(overseasProviderEntityList)){
            throw new ServiceException("三方仓不存在");
        }
        if(overseasProviderEntityList.stream().anyMatch(v->!AuthStatusEnum.ALREADY.getCode().equals(v.getAuthStatus()))){
            throw new ServiceException("只有已授权三方仓可以同步");
        }
        List<DmpInoutDTO.CreateInputDTO> createDTOList = new ArrayList<>();
        for (OverseasProviderEntity overseasProviderEntity : overseasProviderEntityList) {
            DmpInoutDTO.CreateInputDTO dto = new DmpInoutDTO.CreateInputDTO();
            dto.setSystemCode(overseasProviderEntity.getCode());
            dto.setBillType(BusinessTypeEnum.PRODUCT.getCode());
            dto.setNextLevelId(overseasProviderEntity.getId());
            createDTOList.add(dto);
        }
        Boolean result = dmpInoutTaskFeign.doInputTask(createDTOList);
    }

    @Override
    public List<SkuMappingDTO.SkuMappingViewDTO> listSkuMappingByParams(ListingInfoDTO.QueryDTO queryDTO) {
        if (Objects.isNull(queryDTO)){
            return Collections.emptyList();
        }
        if (CollUtil.isEmpty(queryDTO.getPlatformSkuIdList()) && CollUtil.isEmpty(queryDTO.getPlatformSkuNoList()) && StringUtils.isBlank(queryDTO.getAuthId())){
            return Collections.emptyList();
        }
        return baseMapper.listSkuMappingByParams(queryDTO);
    }


//    @Override
//    public  List<BomChildrenSkuDTO> checkBomByPlatformSkuNos(SkuMappingDTO.SkuParamDTO skuParamDTO) {
//        if(StringUtils.isBlank(skuParamDTO.getCutomerId()) || CollectionUtils.isEmpty(skuParamDTO.getPlatformSkuNoList())){
//            return Collections.emptyList();
//        }
//        //平台sku匹配系统sku
//        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoDTOList = this.baseMapper.listSkuBySkuNos(skuParamDTO);
//        List<String> skuNos = productSkuInfoDTOList.stream().map(SkuMappingDTO.ProductSkuInfoDTO::getSkuNo).filter(StringUtils::isNotBlank).collect(Collectors.toList());
//        if(CollectionUtils.isEmpty(skuNos)){
//            return Collections.emptyList();
//        }
//        //系统sku 获取子件
//        return bomSkuFeign.checkExistAndListCombinationSku(skuNos);
//    }
    @Override
    public PagingVO<SkuMappingDTO.CustomerPagingViewDTO> customerPaging(PagingDTO<SkuMappingDTO.CustomerPagingParamDTO> dto) {
        SkuMappingDTO.CustomerPagingParamDTO params = dto.getParams();
        params.setPermissionSql(dto.getPermissionSql());
        Page<T> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        params.setType(RuleTypeEnum.CUSTOMER.getCode());
        IPage<SkuMappingDTO.CustomerPagingViewDTO> pageData = baseMapper.customerPaging(query, params);
        List<SkuMappingDTO.CustomerPagingViewDTO> list = pageData.getRecords();
        if (CollectionUtils.isEmpty(list)) {
            return new PagingVO<>(pageData);
        }
        fillCustomerDb(list,dto);
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String addCustomer(SkuMappingDTO.AddCustomerRequest request) {
        SkuMappingDTO.AddCustomerDTO dto = request.getDto();
        LocalDateTime effectiveTime = dto.getEffectiveTime();
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(Collections.singletonList(dto.getSkuId()));
        if(CollectionUtils.isEmpty(skuList)){
            throw new ServiceException("未查询到sku信息");
        }
        SkuVO skuVO = skuList.get(0);
        CustomerInfoEntity customerInfo = customerInfoService.getById(dto.getCustomerId());
        if(Objects.isNull(customerInfo)){
            throw new ServiceException("未查询到客户信息");
        }
        MultipartFile file = request.getFile();
        String url = "";
        if(Objects.nonNull(file) && StringUtils.isNotBlank(file.getOriginalFilename())){
            url = fileFeign.uploadFile(file);
        }
        ListingInfoEntity existsEntity = listingInfoService.getByPlatformSkuNo("",dto.getPlatformSkuNo(),dto.getCustomerId());
        if(Objects.nonNull(existsEntity)){
            if (CharSequenceUtil.isBlank(dto.getLabelUrl())){
                //根据模板生成pdf文件
                existsEntity.setLabelUrl(getLabelUrl(skuVO.getSkuNo(),dto.getPlatformSkuNo()));
                existsEntity.setLabelFileName(FileTemplateConstant.CUSTOMER_SKU_LABEL + ".pdf");
                existsEntity.setLabelSourceType(LabelSourceTypeEnum.SYSTEM.getCode());
            } else if (!Objects.equals(existsEntity.getLabelUrl(), dto.getLabelUrl())){
                existsEntity.setLabelUrl(dto.getLabelUrl());
                existsEntity.setLabelFileName(dto.getLabelFileName());
                existsEntity.setLabelSourceType(LabelSourceTypeEnum.CUSTOMER.getCode());
                String msg =  CharSequenceUtil.format("用户【{}】新增【{}】为【{}】产品标签【{}】链接【{}】", UserContext.getDefaultLoginUser().getUserName(), "客户sku", existsEntity.getPlatformSkuNo(),dto.getLabelFileName(),dto.getLabelUrl());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), existsEntity.getId(), "新增操作");
            }
            //存在客户sku，将历史映射关系失效，新增映射关系
            List<SkuMappingEntity> skuMappingEntityList = this.listByListingIds(Collections.singletonList(existsEntity.getId()));
            if(CollectionUtils.isNotEmpty(skuMappingEntityList)){
                SkuMappingEntity skuMappingEntity = skuMappingEntityList.get(0);
                if(skuMappingEntity.getProductSkuId().equals(dto.getSkuId())){
                    List<SkuMappingEntity> historyList = this.listExpiredByListing(existsEntity.getId());
                    if(CollectionUtils.isNotEmpty(historyList)){
                        SkuMappingEntity skuMapping = historyList.get(0);
                        if (skuMapping.getEffectiveTime().equals(effectiveTime) || effectiveTime.isBefore(skuMapping.getEffectiveTime())){
                            throw new ServiceException("启用时间不可早于历史启用启用时间【{}】",skuMapping.getEffectiveTime());
                        }
                    }
                    skuMappingEntity.setEffectiveTime(effectiveTime);
                    skuMappingEntity.setExpireTime(effectiveTime.plusYears(MathUtil.NUMBER_100));
                    this.updateById(skuMappingEntity);
                    existsEntity.setProductImageUrl(url);
                    existsEntity.setPlatformSkuName(dto.getPlatformSkuName());
                    listingInfoService.updateById(existsEntity);
                }else if (skuMappingEntity.getEffectiveTime().equals(effectiveTime) || effectiveTime.isBefore(skuMappingEntity.getEffectiveTime())){
                    throw new ServiceException("启用日期不能早于上个映射关系的开始时间【{}】",skuMappingEntity.getEffectiveTime());
                }else{
                    skuMappingEntity.setIsExpire(true);
                    this.updateById(skuMappingEntity);
                    existsEntity.setProductImageUrl(url);
                    existsEntity.setPlatformSkuName(dto.getPlatformSkuName());
                    listingInfoService.updateById(existsEntity);

                    SkuMappingEntity addSkuMapping = new SkuMappingEntity();
                    addSkuMapping.setType(RuleTypeEnum.CUSTOMER);
                    addSkuMapping.setProductSkuId(dto.getSkuId());
                    addSkuMapping.setProductSkuNo(skuVO.getSkuNo());
                    addSkuMapping.setProductName(skuVO.getSkuName());
                    addSkuMapping.setListingId(existsEntity.getId());
                    //生效时间
                    addSkuMapping.setEffectiveTime(effectiveTime);
                    addSkuMapping.setExpireTime(effectiveTime.plusYears(MathUtil.NUMBER_100));
                    if (this.save(addSkuMapping)) {
                        // 操作日志
                        String msg =  CharSequenceUtil.format("用户【{}】新增【{}】为【{}】", UserContext.getDefaultLoginUser().getUserName(), "客户sku", existsEntity.getPlatformSkuNo());
                        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), existsEntity.getId(), "新增操作");
                        return skuMappingEntity.getId();
                    }
                }
            }
        }else{
            ListingInfoEntity listingInfoEntity = new ListingInfoEntity();
            listingInfoEntity.setPlatformSkuNo(dto.getPlatformSkuNo());
            listingInfoEntity.setPlatformSkuName(dto.getPlatformSkuName());
            listingInfoEntity.setProductImageUrl(url);
            listingInfoEntity.setType(RuleTypeEnum.CUSTOMER.getCode());
            listingInfoEntity.setAuthId(dto.getCustomerId());
            listingInfoEntity.setMatchResult(ListingMatchResultEnum.TRUE.getCode());
            if (CharSequenceUtil.isBlank(dto.getLabelUrl())){
                //根据模板生成pdf文件
                listingInfoEntity.setLabelUrl(getLabelUrl(skuVO.getSkuNo(),dto.getPlatformSkuNo()));
                listingInfoEntity.setLabelFileName(FileTemplateConstant.CUSTOMER_SKU_LABEL + ".pdf");
                listingInfoEntity.setLabelSourceType(LabelSourceTypeEnum.SYSTEM.getCode());
            } else{
                listingInfoEntity.setLabelUrl(dto.getLabelUrl());
                listingInfoEntity.setLabelFileName(dto.getLabelFileName());
                listingInfoEntity.setLabelSourceType(LabelSourceTypeEnum.CUSTOMER.getCode());
            }
            listingInfoService.save(listingInfoEntity);

            SkuMappingEntity skuMappingEntity = new SkuMappingEntity();
            skuMappingEntity.setType(RuleTypeEnum.CUSTOMER);
            skuMappingEntity.setProductSkuId(dto.getSkuId());
            skuMappingEntity.setProductSkuNo(skuVO.getSkuNo());
            skuMappingEntity.setProductName(skuVO.getSkuName());
            skuMappingEntity.setListingId(listingInfoEntity.getId());
            //生效时间
            skuMappingEntity.setEffectiveTime(effectiveTime);
            skuMappingEntity.setExpireTime(effectiveTime.plusYears(MathUtil.NUMBER_100));
            if (this.save(skuMappingEntity)) {
                // 操作日志
                String msg =  CharSequenceUtil.format("用户【{}】新增【{}】为【{}】", UserContext.getDefaultLoginUser().getUserName(), "客户sku", listingInfoEntity.getPlatformSkuNo());
                operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), listingInfoEntity.getId(), "新增操作");
                return skuMappingEntity.getId();
            }
        }
        return "";
    }

    @Override
    public String getLabelUrl(String skuNo, String platformSkuNo) {
        if (CharSequenceUtil.isBlank(skuNo) || CharSequenceUtil.isBlank(platformSkuNo)){
            return "";
        }
        //模板查询
        FileTemplateDTO.GetOneDTO getOneDTO = new FileTemplateDTO.GetOneDTO();
        getOneDTO.setName(FileTemplateConstant.CUSTOMER_SKU_LABEL);
        getOneDTO.setFileType(FileTypeEnum.JASPER.getCode());
        getOneDTO.setSourceType(SourceTypeEnum.SO_DELIVERY_NOTICE.getCode());
        FileTemplateEntity fileTemplateEntity = fileTemplateFeign.getByFileTemplate(getOneDTO);
        //获取fastdfs文件
        byte[] content = null;
        try {
            content = FastDFSClientUtil.getStorageClient().download_file1(fileTemplateEntity.getUrl());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("skuNo", skuNo);
        map.put("platformSkuNo", platformSkuNo);
        map.put("dateStr", "");
        InputStream inputStream = new ByteArrayInputStream(content);
        byte[] bytes = JasperHelperUtil.exportToPdfStream(inputStream, map);
        return FastDFSClientUtil.uploadFile(bytes, FileTemplateConstant.CUSTOMER_SKU_LABEL + ".pdf",null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateCustomer(SkuMappingDTO.AddCustomerRequest request) {
        SkuMappingDTO.AddCustomerDTO dto = request.getDto();
        LocalDateTime effectiveTime = dto.getEffectiveTime();
        List<SkuVO> skuList = plmTaskFeign.listSkuProductByIds(Collections.singletonList(dto.getSkuId()));
        if(CollectionUtils.isEmpty(skuList)){
            throw new ServiceException("未查询到sku信息");
        }
        SkuVO skuVO = skuList.get(0);
        CustomerInfoEntity customerInfo = customerInfoService.getById(dto.getCustomerId());
        if(Objects.isNull(customerInfo)){
            throw new ServiceException("未查询到客户信息");
        }
        SkuMappingEntity skuMappingEntity = this.getById(dto.getSkuMappingId());
        if(Objects.isNull(skuMappingEntity)){
            throw new ServiceException("mapping为空");
        }
        ListingInfoEntity existsEntity = listingInfoService.getById(skuMappingEntity.getListingId());
        if(Objects.isNull(existsEntity)){
            throw new ServiceException("listing为空");
        }
        if(!dto.getPlatformSkuNo().equals(existsEntity.getPlatformSkuNo())){
            ListingInfoEntity otherExistsEntity = listingInfoService.getByPlatformSkuNo("",dto.getPlatformSkuNo(),dto.getCustomerId());
            if(Objects.nonNull(otherExistsEntity)){
                throw new ServiceException("已存在相同客户sku");
            }
        }

        MultipartFile file = request.getFile();
        String url = "";
        if(StringUtils.isBlank(dto.getProductImageUrl())){
            if(Objects.nonNull(file) && StringUtils.isNotBlank(file.getOriginalFilename())){
                url = fileFeign.uploadFile(file);
            }
        }else{
            url = dto.getProductImageUrl();
        }
        String msg =  CharSequenceUtil.format("用户【{}】编辑：客户sku【{}】修改为【{}】,客户sku名称【{}】修改为【{}】,产品sku【{}】修改为【{}】", UserContext.getDefaultLoginUser().getUserName(), existsEntity.getPlatformSkuNo(),dto.getPlatformSkuNo(),existsEntity.getPlatformSkuName(),dto.getPlatformSkuName(),skuMappingEntity.getProductSkuNo(),skuVO.getSkuNo());
        existsEntity.setPlatformSkuNo(dto.getPlatformSkuNo());
        existsEntity.setPlatformSkuName(dto.getPlatformSkuName());
        existsEntity.setProductImageUrl(url);
        if (CharSequenceUtil.isBlank(dto.getLabelUrl())){
            //根据模板生成pdf文件
            existsEntity.setLabelUrl(getLabelUrl(skuVO.getSkuNo(),dto.getPlatformSkuNo()));
            existsEntity.setLabelFileName(FileTemplateConstant.CUSTOMER_SKU_LABEL + ".pdf");
            existsEntity.setLabelSourceType(LabelSourceTypeEnum.SYSTEM.getCode());
        } else if (!Objects.equals(existsEntity.getLabelUrl(), dto.getLabelUrl())){
            existsEntity.setLabelUrl(dto.getLabelUrl());
            existsEntity.setLabelFileName(dto.getLabelFileName());
            existsEntity.setLabelSourceType(LabelSourceTypeEnum.CUSTOMER.getCode());
            String msg1 =  CharSequenceUtil.format("用户【{}】编辑【{}】为【{}】产品标签【{}】链接【{}】", UserContext.getDefaultLoginUser().getUserName(), "客户sku", existsEntity.getPlatformSkuNo(),dto.getLabelFileName(),dto.getLabelUrl());
            operateLogService.addModuleOperateLog(msg1, ModuleTypeEnum.LISTING_INFO.getCode(), existsEntity.getId(), "编辑操作");
        }
        listingInfoService.updateById(existsEntity);

        if(skuMappingEntity.getProductSkuId().equals(dto.getSkuId())){
            List<SkuMappingEntity> historyList = this.listExpiredByListing(existsEntity.getId());
            if(CollectionUtils.isNotEmpty(historyList)){
                SkuMappingEntity skuMapping = historyList.get(0);
                if (skuMapping.getEffectiveTime().equals(effectiveTime) || effectiveTime.isBefore(skuMapping.getEffectiveTime())){
                    throw new ServiceException("启用时间不可早于历史启用启用时间【{}】",skuMapping.getEffectiveTime());
                }
            }
            skuMappingEntity.setEffectiveTime(effectiveTime);
            skuMappingEntity.setExpireTime(effectiveTime.plusYears(MathUtil.NUMBER_100));
            this.updateById(skuMappingEntity);
        }else if (skuMappingEntity.getEffectiveTime().equals(effectiveTime) || effectiveTime.isBefore(skuMappingEntity.getEffectiveTime())){
            throw new ServiceException("启用日期不能早于上个映射关系的开始时间【{}",skuMappingEntity.getEffectiveTime());
        }else{
            skuMappingEntity.setIsExpire(true);
            this.updateById(skuMappingEntity);

            SkuMappingEntity addSkuMapping = new SkuMappingEntity();
            addSkuMapping.setType(RuleTypeEnum.CUSTOMER);
            addSkuMapping.setProductSkuId(dto.getSkuId());
            addSkuMapping.setProductSkuNo(skuVO.getSkuNo());
            addSkuMapping.setProductName(skuVO.getSkuName());
            addSkuMapping.setListingId(existsEntity.getId());
            //生效时间
            addSkuMapping.setEffectiveTime(effectiveTime);
            addSkuMapping.setExpireTime(effectiveTime.plusYears(MathUtil.NUMBER_100));
            this.save(addSkuMapping);
        }
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.LISTING_INFO.getCode(), existsEntity.getId(), "编辑操作");
        return skuMappingEntity.getId();
    }

    @Override
    public Boolean exportCustomerSku(SkuMappingDTO.CustomerPagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("sku对照列表", EXPORT_OMS_CUSTOMER_SKU.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public List<SkuMappingDTO.PagingViewDTO> listByAccountAndDictPlatform(ListingInfoDTO.QueryPlatformDTO params) {
        return baseMapper.listByAccountAndDictPlatform(params);
    }

    @Override
    public List<SkuMappingDTO.ProductSkuInfoDTO> listSkuBySkuNos(SkuMappingDTO.SkuParamDTO skuParamDTO) {
        if(null ==  skuParamDTO || StringUtils.isBlank(skuParamDTO.getCutomerId())){
            throw new ServiceException("客户id不能为空");
        }
        return this.baseMapper.listSkuBySkuNos(skuParamDTO);
    }

    @Override
    public List<SkuMappingDTO.CustomerInventorySkuInfoDTO> getErpSkuByCustomerSku(SkuMappingDTO.CustomerInventorySkuParamDTO skuParamDTO) {
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByAuth(RuleTypeEnum.CUSTOMER.getCode(), skuParamDTO.getPlatformSkuNoList(),Collections.singletonList(skuParamDTO.getCustomerId()));
        if(CollectionUtils.isEmpty(listingInfoEntityList)){
            return Collections.emptyList();
        }
        List<String> listingIdList = listingInfoEntityList.stream().map(ListingInfoEntity::getId).collect(Collectors.toList());
        List<SkuMappingEntity> allSkuMappingEntityList = this.lambdaQuery()
                .in(SkuMappingEntity::getListingId, listingIdList)
                .orderByDesc(SkuMappingEntity::getEffectiveTime)
                .list();

        if(CollectionUtils.isEmpty(allSkuMappingEntityList)){
            return Collections.emptyList();
        }

        List<String> skuIdList = allSkuMappingEntityList.stream().map(SkuMappingEntity::getProductSkuId).collect(Collectors.toList());
        //虚拟库存
        VirtualInventoryDTO.VirtualInventoryParamDTO paramDTO = new VirtualInventoryDTO.VirtualInventoryParamDTO();
        paramDTO.setWarehouseIdList(Collections.singletonList(skuParamDTO.getWarehouseId()));
        paramDTO.setVirtualWarehouseIdList(Collections.singletonList(skuParamDTO.getVirtualWarehouseId()));
        paramDTO.setDictInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));
        paramDTO.setSkuIdList(skuIdList);
        List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryQtyDTOList = virtualInventoryFeign.listInventoryQty(paramDTO);
        //实体库存
        InventoryQtyDTO.SkuInventoryStatusParamDTO skuInventoryDTO = new InventoryQtyDTO.SkuInventoryStatusParamDTO();
        skuInventoryDTO.setInventoryStatusList(Arrays.asList(InventoryStatusEnum.USABLE.getCode(),InventoryStatusEnum.FROZEN.getCode()));
        skuInventoryDTO.setWarehouseIdList(Collections.singletonList(skuParamDTO.getWarehouseId()));
        skuInventoryDTO.setSkuIdList(skuIdList);
        List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> inventoryList = inventoryFeign.listSkuInventoryStatusByParam(skuInventoryDTO);
        List<SkuMappingDTO.CustomerInventorySkuInfoDTO> innerCustomerInventorySkuInfoDTOS = new ArrayList<>();
        for (String platformSkuNo : skuParamDTO.getPlatformSkuNoList()) {
            SkuMappingDTO.CustomerInventorySkuInfoDTO customerInventorySkuInfoDTO = new SkuMappingDTO.CustomerInventorySkuInfoDTO();
            customerInventorySkuInfoDTO.setPlatformSkuNo(platformSkuNo);
            innerCustomerInventorySkuInfoDTOS.add(customerInventorySkuInfoDTO);
            ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream().filter(v->v.getPlatformSkuNo().equals(platformSkuNo)).findFirst().orElse(null);
            if(Objects.isNull(listingInfoEntity)){
                continue;
            }
            List<SkuMappingEntity> skuMappingEntityList = allSkuMappingEntityList.stream().filter(v->v.getListingId().equals(listingInfoEntity.getId())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(skuMappingEntityList)){
                continue;
            }
            List<SkuMappingDTO.InnerCustomerInventorySkuInfoDTO> innerCustomerInventorySkuInfoDTOSList = new ArrayList<>();
            for (SkuMappingEntity skuMappingEntity : skuMappingEntityList) {
                SkuMappingDTO.InnerCustomerInventorySkuInfoDTO innerCustomerInventorySkuInfoDTO = new SkuMappingDTO.InnerCustomerInventorySkuInfoDTO();
                innerCustomerInventorySkuInfoDTO.setId(skuMappingEntity.getId());
                innerCustomerInventorySkuInfoDTO.setSkuNo(skuMappingEntity.getProductSkuNo());
                innerCustomerInventorySkuInfoDTO.setSkuId(skuMappingEntity.getProductSkuId());
                innerCustomerInventorySkuInfoDTO.setEffectiveTime(skuMappingEntity.getEffectiveTime());
                innerCustomerInventorySkuInfoDTO.setIsEffective(!skuMappingEntity.getIsExpire());
                List<InventoryQtyDTO.SkuInventoryStatusTotalDTO> skuInventoryStatusTotalDTOList = inventoryList.stream().filter(v->v.getSkuId().equals(skuMappingEntity.getProductSkuId())).collect(Collectors.toList());
                Integer totalQty = skuInventoryStatusTotalDTOList.stream().map(InventoryQtyDTO.SkuInventoryStatusTotalDTO::getInventoryTotal).reduce(0,Integer::sum);
                innerCustomerInventorySkuInfoDTO.setActualQty(totalQty);
                List<VirtualInventoryDTO.VirtualInventoryQtyDTO> virtualInventoryQtyDTOList1 = virtualInventoryQtyDTOList.stream().filter(v->v.getDictInventoryStatus().equals(InventoryStatusEnum.FROZEN.getCode()) && v.getSkuId().equals(skuMappingEntity.getProductSkuId())).collect(Collectors.toList());
                Integer frozenQty = virtualInventoryQtyDTOList1.stream().map(VirtualInventoryDTO.VirtualInventoryQtyDTO::getInventoryQty).reduce(0,Integer::sum);
                innerCustomerInventorySkuInfoDTO.setVirtualFrozenQty(frozenQty);
                innerCustomerInventorySkuInfoDTO.setStock(totalQty - frozenQty);
                innerCustomerInventorySkuInfoDTO.setProductName(skuMappingEntity.getProductName());
                innerCustomerInventorySkuInfoDTOSList.add(innerCustomerInventorySkuInfoDTO);
            }
            customerInventorySkuInfoDTO.setInnerCustomerInventorySkuInfoDTOS(innerCustomerInventorySkuInfoDTOSList);
        }
        return innerCustomerInventorySkuInfoDTOS;
    }

    @Override
    public BatchResultDTO updateCustomerLabel(SkuMappingDTO.CustomerLabelDTO dto) {
        String fileName = dto.getPlatformSkuNo();
        int dotIndex = fileName.lastIndexOf(".pdf");
        String platformSkuNo = (dotIndex == -1) ? fileName : fileName.substring(0, dotIndex);
        List<ListingInfoEntity> entityList = listingInfoService.listByParam(RuleTypeEnum.CUSTOMER.getCode(), null, Collections.singletonList(platformSkuNo));
        if (CollUtil.isEmpty(entityList)){
            throw new ServiceException("文件名匹配不到客户SKU");
        }else  if (entityList.size() > 1){
            throw new ServiceException("存在相同的客户SKU，请手动单个上传");
        }
        ListingInfoEntity listingInfoEntity = entityList.get(0);
        listingInfoService.updateLabelInfo(listingInfoEntity.getId(), dto.getLabelUrl(),LabelSourceTypeEnum.CUSTOMER.getCode(),CharSequenceUtil.isNotBlank(dto.getLabelFileName())? dto.getLabelFileName() : fileName);
        return BatchResultDTO.success(dto.getPlatformSkuNo(),dto.getPlatformSkuNo(),"更新客户SKU标签成功");
    }

    @Override
    public BatchResultDTO generateCustomerLabel(ListingInfoEntity entity, SkuMappingEntity skuMapping) {
        String labelUrl = getLabelUrl(skuMapping.getProductSkuNo(),entity.getPlatformSkuNo());
        //更新记录
        listingInfoService.updateLabelInfo(entity.getId(), labelUrl,LabelSourceTypeEnum.SYSTEM.getCode(), FileTemplateConstant.CUSTOMER_SKU_LABEL);
        return BatchResultDTO.success();
    }

    @Override
    public List<BatchResultDTO> pushProduct(List<String> ids) {
        List<BatchResultDTO> batchResultDTOList = new ArrayList<>();
        List<ListingInfoEntity> listingInfoEntityList = listingInfoService.listByIds(ids);
        List<String> authIds = listingInfoEntityList.stream()
                .map(ListingInfoEntity::getAuthId)
                .distinct()
                .collect(Collectors.toList());
        List<OverseasProviderEntity> overseasProviderEntityList = FeignQuery.getByIds(OverseasProviderEntity.class, authIds);
        List<ListingInfoEntity> syncList = new ArrayList<>();
        for (String id : ids) {
            ListingInfoEntity listingInfoEntity = listingInfoEntityList.stream()
                    .filter(v -> v.getId().equals(id))
                    .findFirst()
                    .orElse(null);
            if(Objects.isNull(listingInfoEntity)){
                batchResultDTOList.add(BatchResultDTO.fail(id,id,"listing不存在"));
                continue;
            }
            OverseasProviderEntity overseasProviderEntity = overseasProviderEntityList.stream()
                    .filter(v -> v.getId().equals(listingInfoEntity.getAuthId()))
                    .findFirst()
                    .orElse(null);
            if(Objects.isNull(overseasProviderEntity)){
                batchResultDTOList.add(BatchResultDTO.fail(id,listingInfoEntity.getPlatformSkuNo(),"三方仓不存在"));
                continue;
            }
            if(!overseasProviderEntity.getAuthStatus().equals(AuthStatusEnum.ALREADY.getCode())){
                batchResultDTOList.add(BatchResultDTO.fail(id,listingInfoEntity.getPlatformSkuNo(),"三方仓未授权"));
                continue;
            }
            if(!overseasProviderEntity.getIsProductSync()){
                batchResultDTOList.add(BatchResultDTO.fail(id,listingInfoEntity.getPlatformSkuNo(),"三方仓未开启产品同步"));
                continue;
            }
            syncList.add(listingInfoEntity);
        }
        if(CollectionUtils.isNotEmpty(syncList)){
            this.syncProductToWarehouse(syncList);
        }
        return batchResultDTOList;
    }

    private void syncProductToWarehouse(List<ListingInfoEntity> entityList) {
        List<String> listingIds = entityList.stream().map(BaseEntity::getId).collect(Collectors.toList());
        List<DmpOutputTaskRecordEntity> dmpOutputTaskRecordEntityList = dmpTaskFeign.getOutputTaskByIdAndType(listingIds,SourceTypeEnum.CAINIAO_LISTING.getCode());
        List<DmpOutputTaskRecordEntity> syncList = new ArrayList<>();
        List<OmsPushMsgEntity> msgList = new ArrayList<>();
        for (ListingInfoEntity listingInfoEntity : entityList) {
            DmpOutputTaskRecordEntity dmpOutputTaskRecordEntity = dmpOutputTaskRecordEntityList.stream().filter(v->v.getSourceId().equals(listingInfoEntity.getId())).findFirst().orElse(null);
            if(Objects.isNull(dmpOutputTaskRecordEntity)){
                msgList.add(this.createOmsPushMsgEntity(listingInfoEntity));
            }else{
                syncList.add(dmpOutputTaskRecordEntity);
            }
        }
        if(CollectionUtils.isNotEmpty(msgList)){
            boolean save = omsPushMsgService.saveBatch(msgList);
            if (!save){
                ServiceException.runError("保存本地消息失败:{}", JSONUtil.toJsonStr(msgList));
            }
        }
        if(CollectionUtils.isNotEmpty(syncList)){
            syncList.forEach(v->v.setStatus(DmpOutputTaskRecordStatusEnum.ERROR.getCode()));
            List<String> syncIdList = syncList.stream().map(BaseEntity::getId).collect(Collectors.toList());
            dmpInoutTaskFeign.updateDmpOutputTaskRecordEntity(syncList);
            ApiResult<?> result = dmpInoutTaskFeign.querySyncIds(new BaseIdsDTO.IdsDTO(syncIdList));
            if(!result.isSuccess()){
                ServiceException.runError("重新推送消息失败:{}", result.getMsg());
            }
        }

    }

    private OmsPushMsgEntity createOmsPushMsgEntity(ListingInfoEntity listingInfoEntity) {
        OmsPushMsgEntity omsPushEntity = new OmsPushMsgEntity();
        omsPushEntity.setTargetPlatform(listingInfoEntity.getPlatform());
        if(listingInfoEntity.getPlatform().equals(OmsPlatformEnum.CAI_NIAO.getCode())){
            omsPushEntity.setSourceType(SourceTypeEnum.CAINIAO_LISTING.getCode());
        }else{
            throw new ServiceException("不支持的推送平台:{}", listingInfoEntity.getPlatform());
        }
        omsPushEntity.setSourceId(listingInfoEntity.getId());
        omsPushEntity.setSourceCode(listingInfoEntity.getPlatformSkuNo());
        omsPushEntity.setSyncOperate(SyncOperateEnum.OPERATE_APPROVE.getCode());
        omsPushEntity.setPushData(JSON.toJSONString(DmpOutputConstant.getQuerySyncMap()));
        return omsPushEntity;
    }

//    @Override
//    public  List<BomChildrenSkuDTO> checkBomByPlatformSkuNos(SkuMappingDTO.SkuParamDTO skuParamDTO) {
//        if(StringUtils.isBlank(skuParamDTO.getCutomerId()) || CollectionUtils.isEmpty(skuParamDTO.getPlatformSkuNoList())){
//            return Collections.emptyList();
//        }
//        //平台sku匹配系统sku
//        List<SkuMappingDTO.ProductSkuInfoDTO> productSkuInfoDTOList = this.baseMapper.listSkuBySkuNos(skuParamDTO);
//        List<String> skuNos = productSkuInfoDTOList.stream().map(SkuMappingDTO.ProductSkuInfoDTO::getSkuNo).filter(StringUtils::isNotBlank).collect(Collectors.toList());
//        if(CollectionUtils.isEmpty(skuNos)){
//            return Collections.emptyList();
//        }
//        //系统sku 获取子件
//        return bomSkuFeign.checkExistAndListCombinationSku(skuNos);
//    }

    private List<SkuMappingEntity> listExpiredByListing(String listingId) {
        return this.lambdaQuery()
                .in(SkuMappingEntity::getListingId, listingId)
                .eq(SkuMappingEntity::getIsExpire,true)
                .orderByDesc(SkuMappingEntity::getEffectiveTime)
                .list();
    }
}

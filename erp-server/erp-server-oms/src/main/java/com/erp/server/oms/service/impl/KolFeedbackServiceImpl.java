package com.erp.server.oms.service.impl;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.annotation.DistributeLocker;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.FindUserDTO;
import com.common.business.dto.base.*;
import com.common.business.enums.BusinessNoTypeEnum;
import com.common.business.enums.FileTaskStatusEnum;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.common.business.vo.LoginUser;
import com.common.business.vo.PagingVO;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.BeanMapperUtils;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import com.erp.model.oms.dto.KolFeedbackDTO;
import com.erp.model.oms.dto.excel.KolFeedbackExcelDTO;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.erp.model.oms.entity.KolPartnerInfoEntity;
import com.erp.model.oms.entity.KolSocialMediaEntity;
import com.erp.model.oms.enums.FeedbackStatusEnum;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.plm.vo.ProductVO;
import com.erp.model.plm.vo.SkuVO;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.erp.server.oms.listener.KolFeedbackExcelListener;
import com.erp.server.oms.mapper.KolFeedbackMapper;
import com.erp.server.oms.service.KolFeedbackService;
import com.erp.server.oms.service.KolPartnerInfoService;
import com.erp.server.oms.service.KolSampleCostFeedbackUrlService;
import com.erp.server.oms.service.KolSocialMediaService;
import com.erp.server.oms.service.OperateLogService;
import com.google.common.collect.Lists;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_OMS_KOL_FEEDBACK;
import static com.common.business.enums.FileTaskEventEnum.IMPORT_OMS_KOL_FEEDBACK;

/**
 * <p>
 * KOL回片列表 服务实现类
 * </p>
 *
 * @author wuhaotian
 * @since 2025-12-01
 */
@Slf4j
@Service
public class KolFeedbackServiceImpl extends SuperServiceImpl<KolFeedbackMapper, KolFeedbackEntity> implements KolFeedbackService {
    @Autowired
    private OperateLogService operateLogService;

    @Autowired
    private PlmTaskFeign plmTaskFeign;

    @Autowired
    private DocNoGenHelper docNoGenHelper;

    @Autowired
    private DownloadTaskFeign downloadTaskFeign;

    @Autowired
    private FileFeign fileFeign;

    @Autowired
    private SysUserFeign sysUserFeign;

    @Autowired
    private com.erp.server.oms.service.DictBasicService dictBasicService;

    @Autowired
    private KolPartnerInfoService kolPartnerInfoService;

    @Autowired
    private KolSocialMediaService kolSocialMediaService;

    @Autowired
    private KolSampleCostFeedbackUrlService kolSampleCostFeedbackUrlService;

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolFeedbackDTO.AddDTO addDTO) {
        KolFeedbackEntity kolFeedbackEntity = new KolFeedbackEntity();
        BeanMapperUtils.copy(addDTO, kolFeedbackEntity);

        // 数据处理
        handleData(kolFeedbackEntity);

//        // 校验唯一性：source_code + sku_no + url_hash
//        checkUnique(kolFeedbackEntity, null);

        log.info("开始新增KOL回片列单");
        boolean save = super.save(kolFeedbackEntity);
        if(!save) {
            throw new ServiceException("KOL回片列单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "KOL回片列单" , kolFeedbackEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_FEEDBACK.getCode(), kolFeedbackEntity.getId(), "新增操作");
        kolSampleCostFeedbackUrlService.syncByFeedback(kolFeedbackEntity);

        return new BaseResultDTO.AddDTO(kolFeedbackEntity.getId(), kolFeedbackEntity.getId());
    }

    /**
    * 批量新增
    */
    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> batchAdd(KolFeedbackDTO.BatchAddDTO dto) {
        List<KolFeedbackDTO.AddDTO> list = dto.getList();
        List<BatchResultDTO> resultDTOS = new ArrayList<>(list.size());

        for (KolFeedbackDTO.AddDTO addDTO : list) {
            BatchResultDTO addResult;
            try {
                BaseResultDTO.AddDTO result = add(addDTO);
                addResult = BatchResultDTO.success(result.getId(), result.getCode(), "新增成功");
            } catch (Exception e) {
                log.error("KOL回片列表批量新增失败", e);
                String sourceCode = addDTO.getSourceCode() != null ? addDTO.getSourceCode() : "";
                addResult = BatchResultDTO.fail("", sourceCode, e.getMessage());
            }
            resultDTOS.add(addResult);
        }

        return resultDTOS;
    }

    /**
    * 修改
    */
    @DistributeLocker(keyName = "addOrUpdateDTO.getId()")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Boolean update(KolFeedbackDTO.UpdateDTO addOrUpdateDTO) {
        KolFeedbackEntity old = super.getById(addOrUpdateDTO.getId());
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.BILL_NOT_EXIST_WITH_TYPE, "KOL回片列单"));
        
        // 如果已回片，不允许编辑
        if (FeedbackStatusEnum.COMPLETED.getCode().equals(old.getFeedbackStatus())) {
            throw new ServiceException("已回片状态不允许编辑");
        }
        
        KolFeedbackEntity kolFeedbackEntity =  BeanMapperUtils.map(KolFeedbackEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolFeedbackEntity);
        
//        // 校验唯一性：source_code + sku_no + url_hash
//        checkUnique(kolFeedbackEntity, old.getId());
        
        log.info("编辑 开始修改KOL回片列单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolFeedbackEntity);
        if(!save) {
            throw new ServiceException("KOL回片列单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录KOL回片列单日志数据，id：【{}】", kolFeedbackEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolFeedbackEntity.getId(), "KOL回片列单");
        operateLogService.addModuleOperateLogByObj(old, kolFeedbackEntity,  ModuleTypeEnum.KOL_FEEDBACK.getCode(), kolFeedbackEntity.getId(), msg);
        if (!sameFeedbackUrlKey(old, kolFeedbackEntity)) {
            kolSampleCostFeedbackUrlService.removeByFeedback(old, hasSameActiveFeedback(old, Collections.singleton(old.getId())));
        }
        kolSampleCostFeedbackUrlService.syncByFeedback(kolFeedbackEntity);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<KolFeedbackDTO.ListDTO> paging(PagingDTO<KolFeedbackDTO.ParamDTO> dto) {
        dto.getParams().setPermissionSql(dto.getPermissionSql());
        Page<KolFeedbackDTO.ListDTO> query = new Page<>(dto.getCurrPage(), dto.getPageSize());
        IPage<KolFeedbackDTO.ListDTO> pageData = this.baseMapper.paging(query, dto.getParams());
        if(CollUtil.isEmpty(pageData.getRecords())) {
           return new PagingVO<>(pageData);
        }
        // 数据处理
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(BaseIdsDTO.IdsDTO dto) {
        if (CollUtil.isEmpty(dto.getIds())) {
            throw new ServiceException("删除ID列表不能为空");
        }
        
        // 检查是否有已回片状态的数据，已回片不允许删除
        List<KolFeedbackEntity> list = super.listByIds(dto.getIds());
        if (CollUtil.isEmpty(list)) {
            throw new ServiceException("未找到要删除的数据");
        }
        
        for (KolFeedbackEntity entity : list) {
            if (FeedbackStatusEnum.COMPLETED.getCode().equals(entity.getFeedbackStatus())) {
                throw new ServiceException("已回片状态不允许删除");
            }
        }
        
        Set<String> deleteIdSet = new HashSet<>(dto.getIds());
        Map<String, Boolean> sameFeedbackMap = buildSameActiveFeedbackMap(list, deleteIdSet);

        // 执行批量删除
        boolean remove = super.removeByIds(dto.getIds());
        if (!remove) {
            throw new ServiceException("批量删除失败");
        }
        list.forEach(entity -> kolSampleCostFeedbackUrlService.removeByFeedback(entity, sameFeedbackMap.getOrDefault(entity.getId(), false)));
    }

    @Override
    public BatchResultDTO delete(String id) {
        KolFeedbackEntity entity = super.getById(id);
        if (entity == null) {
            throw new ServiceException("KOL回片列表不存在");
        }
        
        // 检查状态，已回片不允许删除
        if (FeedbackStatusEnum.COMPLETED.getCode().equals(entity.getFeedbackStatus())) {
            throw new ServiceException("已回片状态不允许删除");
        }
        
        boolean hasSameActiveFeedback = hasSameActiveFeedback(entity, Collections.singleton(id));

        // 执行删除
        boolean remove = super.removeById(id);
        if (!remove) {
            throw new ServiceException("删除失败");
        }
        kolSampleCostFeedbackUrlService.removeByFeedback(entity, hasSameActiveFeedback);
        
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode());
    }

    @Override
    public Boolean export(KolFeedbackDTO.ParamDTO dto, HttpServletResponse response) {
        downloadTaskFeign.saveDownloadTask("KOL回片列表导出", EXPORT_OMS_KOL_FEEDBACK.getCode(), dto);
        return true;
    }

    /**
     * 异步导入
     */
    @Override
    public Boolean importExcel(BaseDTO.ImportDTO dto) {
        dto.setUserId(UserContext.getDefaultLoginUser().getUid());
        downloadTaskFeign.saveImportTask("KOL回片列表导入", IMPORT_OMS_KOL_FEEDBACK.getCode(), dto);
        return Boolean.TRUE;
    }

    /**
     * 导入KOL回片列表
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void importKolFeedback(BaseDTO.ImportDTO dto) {
        // 用户
        List<FindUserDTO> userList = sysUserFeign.getUserList();
        //设置操作人
        FindUserDTO findUserDTO = userList.stream()
                .filter(e -> StringUtils.isNotBlank(dto.getUserId()) && Objects.equals(e.getUserId(), dto.getUserId()))
                .findFirst()
                .orElse(null);
        if (Objects.nonNull(findUserDTO)) {
            LoginUser user = new LoginUser();
            user.setUid(findUserDTO.getUserId());
            user.setUserName(findUserDTO.getUserName());
            user.setRealName(findUserDTO.getRealName());
            user.setUserAccount(findUserDTO.getMobile());
            user.setMobile(findUserDTO.getMobile());
            UserContext.setLoginUser(user);
        }

        KolFeedbackExcelListener excelListenerUtil = new KolFeedbackExcelListener(dto.getTaskId(), dto.getImportType(), dto.getImportCount());
        try {
            byte[] bytes = fileFeign.downloadFile(dto.getFileUrl());
            EasyExcel.read(new ByteArrayInputStream(bytes), KolFeedbackExcelDTO.class, excelListenerUtil).sheet(0).doRead();
        } catch (ExcelCommonException e) {
            log.error("导入格式错误！", e);
            throw new ServiceException(ApiError.FILE_IMPORT_FORMAT_INVALID_XLSX);
        }

        BaseDTO.ImportResultDTO importResultDTO = new BaseDTO.ImportResultDTO();
        importResultDTO.setTaskId(dto.getTaskId());
        importResultDTO.setCount(excelListenerUtil.getCount());
        List<KolFeedbackExcelDTO> errorList = excelListenerUtil.getErrorList();
        String url = "";
        if (CollectionUtils.isNotEmpty(errorList)) {
            String fileName = "KOL回片列表错误信息.xlsx";
            File file = ExcelUtil.exportFile(fileName, "error", errorList, KolFeedbackExcelDTO.class);
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

    /**
     * 处理导入成功的数据
     */
    @Transactional(rollbackFor = Exception.class, propagation = org.springframework.transaction.annotation.Propagation.NESTED)
    @Override
    public void handleImportSuccessList(List<KolFeedbackExcelDTO> successList, List<String> errorNoList, List<KolFeedbackExcelDTO> errorList2, String importType) {
        if (CollectionUtils.isEmpty(successList)) {
            return;
        }

        // 批量查询SKU信息
        List<String> skuNoList = successList.stream()
                .map(KolFeedbackExcelDTO::getSkuNo)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        List<SkuVO> skuList = new ArrayList<>();
        if (CollUtil.isNotEmpty(skuNoList)) {
            skuList = plmTaskFeign.listBySkuNoList(skuNoList);
        }
        Map<String, SkuVO> skuMap = skuList.stream()
                .collect(Collectors.toMap(SkuVO::getSkuNo, sku -> sku, (k1, k2) -> k1));

        // 批量查询SKU ID对应的产品名称
        List<String> skuIdList = skuList.stream()
                .map(SkuVO::getSkuId)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        Map<String, String> skuIdToProductNameMap = new HashMap<>();
        if (CollUtil.isNotEmpty(skuIdList)) {
            List<ProductVO.ProductPackVO> productPackList = 
                    plmTaskFeign.getProductPackBySkuIds(skuIdList);
            if (CollUtil.isNotEmpty(productPackList)) {
                skuIdToProductNameMap = productPackList.stream()
                        .filter(p -> StringUtils.isNotBlank(p.getSkuId()) && StringUtils.isNotBlank(p.getProductName()))
                        .collect(Collectors.toMap(
                                ProductVO.ProductPackVO::getSkuId, 
                                ProductVO.ProductPackVO::getProductName,
                                (k1, k2) -> k1
                        ));
            }
        }

        // 批量查询达人信息
        List<String> partnerNicknameList = successList.stream()
                .map(KolFeedbackExcelDTO::getPartnerNickname)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
        Map<String, String> partnerNicknameToIdMap = new HashMap<>();
        if (CollUtil.isNotEmpty(partnerNicknameList)) {
            List<KolPartnerInfoEntity> partnerList = kolPartnerInfoService.lambdaQuery()
                    .in(KolPartnerInfoEntity::getNickname, partnerNicknameList)
                    .eq(KolPartnerInfoEntity::getIsDeleted, false)
                    .list();
            partnerNicknameToIdMap = partnerList.stream()
                    .collect(Collectors.toMap(
                            KolPartnerInfoEntity::getNickname, 
                            KolPartnerInfoEntity::getId,
                            (k1, k2) -> k1
                    ));
        }

        // 查询发布形式字典数据
        List<com.erp.model.oms.dto.DictBasicDTO.ViewDTO> publishTypeDictList = dictBasicService.getByKey("publishType");
        Map<String, String> publishTypeNameToValueMap = publishTypeDictList.stream()
                .collect(Collectors.toMap(
                        com.erp.model.oms.dto.DictBasicDTO.ViewDTO::getName,
                        com.erp.model.oms.dto.DictBasicDTO.ViewDTO::getValue,
                        (k1, k2) -> k1
                ));

        // 遍历数据进行校验和保存
        for (KolFeedbackExcelDTO excelDTO : successList) {
            List<String> errorMsgList = new ArrayList<>();
            
            try {
                // 数据校验和ID解析
                // 验证SKU是否存在并解析SKU ID和产品名称
                String skuNo = excelDTO.getSkuNo();
                SkuVO skuVO = skuMap.get(skuNo);
                if (skuVO == null || StrUtil.isBlank(skuVO.getSkuId())) {
                    errorMsgList.add("SKU【" + skuNo + "】不存在");
                } else {
                    excelDTO.setSkuId(skuVO.getSkuId());
                    // 获取产品名称
                    String productName = skuIdToProductNameMap.get(skuVO.getSkuId());
                    if (StrUtil.isNotBlank(productName)) {
                        excelDTO.setProductName(productName);
                    }
                }

                // 解析数量
                if (StringUtils.isNotBlank(excelDTO.getQtyStr())) {
                    try {
                        Integer qty = Integer.valueOf(excelDTO.getQtyStr());
                        if (qty <= 0) {
                            errorMsgList.add("数量必须大于0");
                        } else {
                            excelDTO.setQty(qty);
                        }
                    } catch (NumberFormatException e) {
                        errorMsgList.add("数量格式错误：" + excelDTO.getQtyStr() + "，必须为整数");
                    }
                } else {
                    errorMsgList.add("数量不能为空");
                }

                // 验证达人昵称是否存在并解析达人ID
                String partnerNickname = excelDTO.getPartnerNickname();
                String partnerId = partnerNicknameToIdMap.get(partnerNickname);
                if (StrUtil.isBlank(partnerId)) {
                    errorMsgList.add("达人昵称【" + partnerNickname + "】不存在");
                } else {
                    excelDTO.setPartnerId(partnerId);
                }

                // 验证发布形式（非必填）
                if (StrUtil.isNotBlank(excelDTO.getPublishTypeName())) {
                    String publishType = publishTypeNameToValueMap.get(excelDTO.getPublishTypeName());
                    if (StrUtil.isBlank(publishType)) {
                        errorMsgList.add("发布形式【" + excelDTO.getPublishTypeName() + "】不存在");
                    } else {
                        excelDTO.setPublishType(publishType);
                    }
                }

                excelDTO.setUrl(StrUtil.trim(excelDTO.getUrl()));

                // URL哈希值计算
                if (StrUtil.isNotBlank(excelDTO.getUrl())) {
                    String urlHash = DigestUtil.md5Hex(excelDTO.getUrl());
                    excelDTO.setUrlHash(urlHash);
                }

                // 如果有校验错误，添加到错误列表
                if (CollUtil.isNotEmpty(errorMsgList)) {
                    excelDTO.setErrorMsg(com.common.core.utils.FieldValidUtil.getMsgSort(errorMsgList));
                    errorList2.add(excelDTO);
                    continue;
                }

                // 复制数据并保存
                KolFeedbackEntity entity = new KolFeedbackEntity();
                BeanMapperUtils.copy(excelDTO, entity);
                
                // 数据处理
                handleData(entity);
                
                // 保存数据
                boolean save = super.save(entity);
                if (!save) {
                    excelDTO.setErrorMsg("保存失败");
                    errorList2.add(excelDTO);
                } else {
                    kolSampleCostFeedbackUrlService.syncByFeedback(entity);
                }
            } catch (Exception e) {
                log.error("导入KOL回片列表数据失败", e);
                excelDTO.setErrorMsg(e.getMessage().length() > 50 ? e.getMessage().substring(0, 50) : e.getMessage());
                errorList2.add(excelDTO);
            }
        }
    }

    @Override
    public void downloadTemplate(HttpServletResponse response) {
        // 下载KOL回片列表导入模板
        String path = "classpath:excel/kolFeedbackTemplate.xlsx";
        String excelName = "KOL回片列表导入模板.xlsx";
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
            log.info("开始下载KOL回片列表导入模板");
        } catch (Exception e) {
            log.error("KOL回片列表导入模板下载失败", e);
            throw new ServiceException("下载模板失败：" + e.getMessage());
        }
    }

    @Override
    public List<KolFeedbackDTO.FeedbackQtyDTO> listFeedbackQtyBySourceDetailIdList(List<String> sourceDetailIdList) {
        if (CollUtil.isEmpty(sourceDetailIdList)) {
            return Collections.emptyList();
        }
        return baseMapper.listFeedbackQtyBySourceDetailIdList(sourceDetailIdList, SourceTypeEnum.KOL_B2B_APPLICATION.getCode());
    }

    @Override
    public List<KolFeedbackEntity> listBySourceId(String sourceId) {
        return lambdaQuery().eq(KolFeedbackEntity::getSourceId,sourceId).list();
    }

    /**
     * 状态统计
     * @param param
     * @return
     */
    @Override
    public List<KolFeedbackDTO.TabListDTO> tabList(PermissionsDTO param) {
        KolFeedbackDTO.ParamDTO searchParam = new KolFeedbackDTO.ParamDTO();
        searchParam.setPermissionSql(param.getPermissionSql());
        
        // 使用一个SQL查询获取所有状态的统计数量
        List<KolFeedbackDTO.TabListDTO> list = baseMapper.tabList(searchParam);
        
        // 将查询结果转换为Map，方便查找
        Map<String, Integer> statusCountMap = list.stream()
                .collect(Collectors.toMap(
                        KolFeedbackDTO.TabListDTO::getTabFlag,
                        KolFeedbackDTO.TabListDTO::getCount,
                        (k1, k2) -> k1
                ));
        
        // 确保所有枚举状态都存在，如果不存在则添加数量为0的标签
        List<KolFeedbackDTO.TabListDTO> resultList = new ArrayList<>();
        for (FeedbackStatusEnum statusEnum : FeedbackStatusEnum.values()) {
            String code = statusEnum.getCode();
            Integer count = statusCountMap.getOrDefault(code, 0);
            KolFeedbackDTO.TabListDTO tabDTO = new KolFeedbackDTO.TabListDTO(code, statusEnum.getName(), count);
            resultList.add(tabDTO);
        }
        
        // 计算合计数量并添加"全部"标签
        int totalCount = resultList.stream().mapToInt(KolFeedbackDTO.TabListDTO::getCount).sum();
        KolFeedbackDTO.TabListDTO allTab = new KolFeedbackDTO.TabListDTO("all", "全部", totalCount);
        resultList.add(0, allTab); // 添加到第一位
        
        // 排序：全部 -> 待回片 -> 已回片
        resultList.sort((a, b) -> {
            // "all" 排在最前面
            if ("all".equals(a.getTabFlag())) {
                return -1;
            }
            if ("all".equals(b.getTabFlag())) {
                return 1;
            }
            // 其他状态按照枚举顺序排序：pending -> completed
            FeedbackStatusEnum enumA = FeedbackStatusEnum.getByCode(a.getTabFlag());
            FeedbackStatusEnum enumB = FeedbackStatusEnum.getByCode(b.getTabFlag());
            if (enumA != null && enumB != null) {
                return enumA.ordinal() - enumB.ordinal();
            }
            // 如果找不到枚举，保持原顺序
            return 0;
        });
        
        return resultList;
    }

    /**
     * 填充列表数据（枚举值转换）
     */
    private void fillList(List<KolFeedbackDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        // 查询发布形式字典
        List<com.erp.model.oms.dto.DictBasicDTO.ViewDTO> publishTypeDictList = dictBasicService.getByKey("publishType");
        Map<String, String> publishTypeNameMap = publishTypeDictList.stream()
                .collect(Collectors.toMap(com.erp.model.oms.dto.DictBasicDTO.ViewDTO::getValue, 
                        com.erp.model.oms.dto.DictBasicDTO.ViewDTO::getName, (v1, v2) -> v1));

        // 属性赋值
        for (KolFeedbackDTO.ListDTO data : list) {
            // 来源单据枚举转换
            if (StringUtils.isNotBlank(data.getSourceType())) {
                SourceTypeEnum sourceTypeEnum = SourceTypeEnum.getByCode(data.getSourceType());
                if (sourceTypeEnum != null) {
                    data.setSourceTypeName(sourceTypeEnum.getName());
                }
            }

            // 回片状态枚举转换
            if (StringUtils.isNotBlank(data.getFeedbackStatus())) {
                FeedbackStatusEnum feedbackStatusEnum = FeedbackStatusEnum.getByCode(data.getFeedbackStatus());
                if (feedbackStatusEnum != null) {
                    data.setFeedbackStatusName(feedbackStatusEnum.getName());
                }
            }

            // 发布形式字典转换
            if (StringUtils.isNotBlank(data.getPublishType())) {
                String publishTypeName = publishTypeNameMap.getOrDefault(data.getPublishType(), "");
                data.setPublishTypeName(publishTypeName);
            }

        }
    }

    /**
    * 新增修改处理数据
    */
    private void handleData(KolFeedbackEntity kolFeedbackEntity) {
        // 如果 sourceType 为空，默认设置为手工新增
        if (StrUtil.isBlank(kolFeedbackEntity.getSourceType())) {
            kolFeedbackEntity.setSourceType(SourceTypeEnum.SELF_ADD.getCode());
        }

        // 如果 sourceCode 为空，使用 docNoGenHelper.generateCode() 生成
        if (StrUtil.isBlank(kolFeedbackEntity.getSourceCode())) {
            String code = docNoGenHelper.generateCode(BusinessNoTypeEnum.CODE_HP);
            kolFeedbackEntity.setSourceCode(code);
        }

        // skuNo 和 productName 通过 skuId 查询
        if (StrUtil.isNotBlank(kolFeedbackEntity.getSkuId()) && 
            (StrUtil.isBlank(kolFeedbackEntity.getSkuNo()) || StrUtil.isBlank(kolFeedbackEntity.getProductName()))) {
            try {
                List<ProductDetailEntity> skuList = plmTaskFeign.getByIdList(Collections.singletonList(kolFeedbackEntity.getSkuId()));
                if (CollUtil.isNotEmpty(skuList) && !skuList.isEmpty()) {
                    ProductDetailEntity skuInfo = skuList.get(0);
                    kolFeedbackEntity.setSkuNo(skuInfo.getSkuNo());
                    kolFeedbackEntity.setProductName(skuInfo.getName());
                }
            } catch (Exception e) {
                log.warn("获取SKU信息失败，skuId: {}", kolFeedbackEntity.getSkuId(), e);
            }
        }

        // 达人昵称
         if (StrUtil.isNotBlank(kolFeedbackEntity.getPartnerId()) && StrUtil.isBlank(kolFeedbackEntity.getPartnerNickname())) {
             KolPartnerInfoEntity kolPartnerInfoEntity = kolPartnerInfoService.getById(kolFeedbackEntity.getPartnerId());
             if (kolPartnerInfoEntity!=null&& StringUtils.isNotBlank(kolPartnerInfoEntity.getNickname())){
                 kolFeedbackEntity.setPartnerNickname(kolPartnerInfoEntity.getNickname());
             }
         }

        kolFeedbackEntity.setUrl(StrUtil.trim(kolFeedbackEntity.getUrl()));

        // urlHash 用 hutool hash 工具（如果 url 不为空）
        if (StrUtil.isNotBlank(kolFeedbackEntity.getUrl())) {
            String urlHash = DigestUtil.md5Hex(kolFeedbackEntity.getUrl());
            kolFeedbackEntity.setUrlHash(urlHash);
        }

        // feedbackStatus 回片状态：如果 urlHash 不为空，查询达人社媒数据，如果有数据则设置为已回片，否则为待回片
        if (StrUtil.isBlank(kolFeedbackEntity.getFeedbackStatus())) {
            if (StrUtil.isNotBlank(kolFeedbackEntity.getUrlHash())) {
                // 根据 urlHash 查询达人社媒数据是否存在（使用 count 避免 getOne 在多条记录时抛异常）
                LambdaQueryWrapper<KolSocialMediaEntity> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(KolSocialMediaEntity::getUrlHash, kolFeedbackEntity.getUrlHash())
                        .eq(KolSocialMediaEntity::getIsDeleted, false);
                long count = kolSocialMediaService.count(queryWrapper);
                // 如果查询到社媒数据，设置为已回片，否则设置为待回片
                if (count > 0) {
                    kolFeedbackEntity.setFeedbackStatus(FeedbackStatusEnum.COMPLETED.getCode());
                } else {
                    kolFeedbackEntity.setFeedbackStatus(FeedbackStatusEnum.PENDING.getCode());
                }
            } else {
                // 如果没有 urlHash，默认设置为待回片
                kolFeedbackEntity.setFeedbackStatus(FeedbackStatusEnum.PENDING.getCode());
            }
        }
    }

    private boolean sameFeedbackUrlKey(KolFeedbackEntity oldEntity, KolFeedbackEntity newEntity) {
        return Objects.equals(oldEntity.getSourceType(), newEntity.getSourceType())
                && Objects.equals(oldEntity.getSourceDetailId(), newEntity.getSourceDetailId())
                && Objects.equals(oldEntity.getUrlHash(), newEntity.getUrlHash());
    }

    private boolean hasSameActiveFeedback(KolFeedbackEntity entity, Collection<String> excludeIds) {
        if (entity == null
                || StrUtil.isBlank(entity.getSourceType())
                || StrUtil.isBlank(entity.getSourceDetailId())
                || StrUtil.isBlank(entity.getUrlHash())) {
            return false;
        }
        return lambdaQuery()
                .eq(KolFeedbackEntity::getSourceType, entity.getSourceType())
                .eq(KolFeedbackEntity::getSourceDetailId, entity.getSourceDetailId())
                .eq(KolFeedbackEntity::getUrlHash, entity.getUrlHash())
                .eq(KolFeedbackEntity::getIsDeleted, false)
                .notIn(CollUtil.isNotEmpty(excludeIds), KolFeedbackEntity::getId, excludeIds)
                .count() > 0;
    }

    private Map<String, Boolean> buildSameActiveFeedbackMap(List<KolFeedbackEntity> deleteList, Set<String> deleteIdSet) {
        Map<String, Boolean> result = new HashMap<>();
        if (CollUtil.isEmpty(deleteList)) {
            return result;
        }
        deleteList.stream()
                .filter(item -> StrUtil.isNotBlank(item.getId()))
                .forEach(item -> result.put(item.getId(), false));
        List<KolFeedbackEntity> validDeleteList = deleteList.stream()
                .filter(item -> StrUtil.isNotBlank(item.getId()))
                .filter(item -> StrUtil.isNotBlank(item.getSourceType()))
                .filter(item -> StrUtil.isNotBlank(item.getSourceDetailId()))
                .filter(item -> StrUtil.isNotBlank(item.getUrlHash()))
                .collect(Collectors.toList());
        if (CollUtil.isEmpty(validDeleteList)) {
            return result;
        }
        Set<String> activeFeedbackKeySet = new HashSet<>();
        for (List<KolFeedbackEntity> partitionList : Lists.partition(validDeleteList, 100)) {
            LambdaQueryWrapper<KolFeedbackEntity> wrapper = new LambdaQueryWrapper<KolFeedbackEntity>()
                    .eq(KolFeedbackEntity::getIsDeleted, false)
                    .notIn(CollUtil.isNotEmpty(deleteIdSet), KolFeedbackEntity::getId, deleteIdSet)
                    .and(nested -> {
                        appendFeedbackUrlCondition(nested, partitionList.get(0));
                        for (int i = 1; i < partitionList.size(); i++) {
                            KolFeedbackEntity item = partitionList.get(i);
                            nested.or(orWrapper -> appendFeedbackUrlCondition(orWrapper, item));
                        }
                    });
            List<KolFeedbackEntity> activeFeedbackList = super.list(wrapper);
            if (CollUtil.isNotEmpty(activeFeedbackList)) {
                activeFeedbackKeySet.addAll(activeFeedbackList.stream().map(this::buildFeedbackUrlKey).collect(Collectors.toSet()));
            }
        }
        validDeleteList.forEach(item -> result.put(item.getId(), activeFeedbackKeySet.contains(buildFeedbackUrlKey(item))));
        return result;
    }

    private void appendFeedbackUrlCondition(LambdaQueryWrapper<KolFeedbackEntity> wrapper, KolFeedbackEntity entity) {
        wrapper.eq(KolFeedbackEntity::getSourceType, entity.getSourceType())
                .eq(KolFeedbackEntity::getSourceDetailId, entity.getSourceDetailId())
                .eq(KolFeedbackEntity::getUrlHash, entity.getUrlHash());
    }

    private String buildFeedbackUrlKey(KolFeedbackEntity entity) {
        return StrUtil.format("{}#{}#{}", entity.getSourceType(), entity.getSourceDetailId(), entity.getUrlHash());
    }

    /**
     * 校验唯一性：source_code + sku_no + url_hash
     * @param kolFeedbackEntity 当前实体
     * @param excludeId 排除的ID（修改时使用，排除当前记录）
     */
    private void checkUnique(KolFeedbackEntity kolFeedbackEntity, String excludeId) {
        if (StrUtil.isBlank(kolFeedbackEntity.getSourceCode()) || 
            StrUtil.isBlank(kolFeedbackEntity.getSkuNo()) || 
            StrUtil.isBlank(kolFeedbackEntity.getUrlHash())) {
            return;
        }

        // 查询是否存在相同的source_code、sku_no和url_hash的记录
        KolFeedbackEntity existEntity = lambdaQuery()
                .eq(KolFeedbackEntity::getSourceCode, kolFeedbackEntity.getSourceCode())
                .eq(KolFeedbackEntity::getSkuNo, kolFeedbackEntity.getSkuNo())
                .eq(KolFeedbackEntity::getUrlHash, kolFeedbackEntity.getUrlHash())
                .eq(KolFeedbackEntity::getIsDeleted, false)
                .ne(excludeId != null, KolFeedbackEntity::getId, excludeId)
                .one();

        if (existEntity != null) {
            throw new ServiceException("该来源单号、SKU编码和回片链接的组合已存在，不能重复添加");
        }
    }
}

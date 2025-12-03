package com.erp.server.oms.service.impl;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.config.DocNoGenHelper;
import com.common.business.dto.base.*;
import com.common.business.vo.PagingVO;
import com.erp.model.oms.entity.KolPartnerInfoEntity;
import com.erp.model.plm.entity.ProductDetailEntity;
import com.erp.model.scm.enums.ModuleTypeEnum;
import com.erp.rpc.plm.feign.PlmTaskFeign;
import com.erp.server.oms.service.KolPartnerInfoService;
import io.seata.spring.annotation.GlobalTransactional;
import com.common.business.annotation.DistributeLocker;
import com.erp.model.oms.entity.KolFeedbackEntity;
import com.erp.server.oms.mapper.KolFeedbackMapper;
import com.erp.server.oms.service.KolFeedbackService;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.threadlocal.UserContext;
import com.erp.server.oms.service.OperateLogService;
import com.erp.server.oms.service.CommonService;
import com.common.core.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import com.erp.model.oms.dto.KolFeedbackDTO;
import java.util.*;
import java.util.stream.Collectors;
import com.common.core.utils.*;
import com.common.core.enums.ApiError;
import org.springframework.web.multipart.MultipartFile;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.common.business.enums.SourceTypeEnum;
import com.common.business.enums.BusinessNoTypeEnum;
import com.erp.model.oms.enums.FeedbackStatusEnum;
import org.springframework.beans.BeanUtils;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.rpc.file.feign.FileFeign;
import com.erp.rpc.sys.feign.SysUserFeign;
import com.common.business.dto.FindUserDTO;
import com.common.business.vo.LoginUser;
import com.common.business.enums.FileTaskStatusEnum;
import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.exception.ExcelCommonException;
import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import java.util.Objects;
import com.erp.model.oms.dto.excel.KolFeedbackExcelDTO;
import com.erp.server.oms.listener.KolFeedbackExcelListener;
import com.common.core.utils.ExcelUtil;
import com.common.core.utils.FastDFSClientUtil;
import java.io.File;

import javax.servlet.http.HttpServletResponse;
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

    @GlobalTransactional(rollbackFor = Exception.class, timeoutMills = 120000)
    @Transactional(rollbackFor = Exception.class)
    @Override
    public BaseResultDTO.AddDTO add(KolFeedbackDTO.AddDTO addDTO) {
        KolFeedbackEntity kolFeedbackEntity = new KolFeedbackEntity();
        BeanMapperUtils.copy(addDTO, kolFeedbackEntity);

        // 数据处理
        handleData(kolFeedbackEntity);

        // 校验唯一性：source_code + sku_no + url_hash
        checkUnique(kolFeedbackEntity, null);

        log.info("开始新增KOL回片列单");
        boolean save = super.save(kolFeedbackEntity);
        if(!save) {
            throw new ServiceException("KOL回片列单保存失败");
        }
        // 操作日志
        String msg = StrUtil.format("用户【{}】新增【{}】单据id为【{}】", UserContext.getDefaultLoginUser().getUserName(), "KOL回片列单" , kolFeedbackEntity.getId());
        operateLogService.addModuleOperateLog(msg, ModuleTypeEnum.KOL_FEEDBACK.getCode(), kolFeedbackEntity.getId(), "新增操作");

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
        old = Optional.ofNullable(old).orElseThrow(()->new ServiceException(ApiError.NOT_EXIST_BILL, "KOL回片列单"));
        
        // 如果已回片，不允许编辑
        if (FeedbackStatusEnum.COMPLETED.getCode().equals(old.getFeedbackStatus())) {
            throw new ServiceException("已回片状态不允许编辑");
        }
        
        KolFeedbackEntity kolFeedbackEntity =  BeanMapperUtils.map(KolFeedbackEntity.class, addOrUpdateDTO);

        // 数据处理
        handleData(kolFeedbackEntity);
        
        // 校验唯一性：source_code + sku_no + url_hash
        checkUnique(kolFeedbackEntity, old.getId());
        
        log.info("编辑 开始修改KOL回片列单数据，id：【{}】", old.getId());
        boolean save = super.updateById(kolFeedbackEntity);
        if(!save) {
            throw new ServiceException("KOL回片列单保存失败");
        }
        // 记录主单操作日志
        log.info("编辑 开始记录KOL回片列单日志数据，id：【{}】", kolFeedbackEntity.getId());
        String msg = StrUtil.format("用户【{}】编辑id为【{}】的【{}】单据 ", UserContext.getDefaultLoginUser().getUserName(), kolFeedbackEntity.getId(), "KOL回片列单");
        operateLogService.addModuleOperateLogByObj(old, kolFeedbackEntity,  ModuleTypeEnum.KOL_FEEDBACK.getCode(), kolFeedbackEntity.getId(), msg);
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
        
        // 执行批量删除
        boolean remove = super.removeByIds(dto.getIds());
        if (!remove) {
            throw new ServiceException("批量删除失败");
        }
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
        
        // 执行删除
        boolean remove = super.removeById(id);
        if (!remove) {
            throw new ServiceException("删除失败");
        }
        
        return BatchResultDTO.success(entity.getId(), entity.getSourceCode());
    }

    @Override
    public Boolean export(PagingDTO<KolFeedbackDTO.ParamDTO> dto) {
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
            throw new ServiceException(ApiError.ERROR_1016);
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

        // 批量保存数据
        for (KolFeedbackExcelDTO excelDTO : successList) {
            try {
                KolFeedbackEntity entity = new KolFeedbackEntity();
                BeanMapperUtils.copy(excelDTO, entity);
                
                // 数据处理
                handleData(entity);
                
                // 保存数据
                boolean save = super.save(entity);
                if (!save) {
                    excelDTO.setErrorMsg("保存失败");
                    errorList2.add(excelDTO);
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

        // urlHash 用 hutool hash 工具（如果 url 不为空）
        if (StrUtil.isNotBlank(kolFeedbackEntity.getUrl())) {
            String urlHash = DigestUtil.md5Hex(kolFeedbackEntity.getUrl());
            kolFeedbackEntity.setUrlHash(urlHash);
        }

        // feedbackStatus 回片状态默认 "待回片"
        if (StrUtil.isBlank(kolFeedbackEntity.getFeedbackStatus())) {
            kolFeedbackEntity.setFeedbackStatus(FeedbackStatusEnum.PENDING.getCode());
        }
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

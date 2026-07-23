package com.erp.server.plm.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BaseResultDTO;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.DisabledEnum;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.enums.OperationTypeEnum;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.core.exception.ServiceException;
import com.erp.model.plm.dto.CfgProductForbiddenWordDTO;
import com.erp.model.plm.dto.excel.CfgProductForbiddenWordExportExcelDTO;
import com.erp.model.plm.entity.CfgProductForbiddenWordEntity;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.plm.mapper.CfgProductForbiddenWordMapper;
import com.erp.server.plm.service.CfgProductForbiddenWordService;
import com.erp.server.plm.service.OperateLogService;
import com.erp.server.plm.support.PlmPagingSortSupport;
import com.erp.server.plm.support.ProductForbiddenWordMatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p>
 * 产品违禁词库 服务实现类
 * </p>
 */
@Slf4j
@Service
public class CfgProductForbiddenWordServiceImpl extends SuperServiceImpl<CfgProductForbiddenWordMapper, CfgProductForbiddenWordEntity> implements CfgProductForbiddenWordService {

    private static final int FORBIDDEN_WORD_MAX_LENGTH = 200;

    @Resource
    private ProductForbiddenWordMatcher productForbiddenWordMatcher;

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private OperateLogService operateLogService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResultDTO.AddDTO add(CfgProductForbiddenWordDTO.AddDTO dto) {
        List<String> words = normalizeAndValidateAddWords(dto.getForbiddenWords());
        String firstId = "";
        for (String word : words) {
            CfgProductForbiddenWordEntity old = baseMapper.getByNormalizedWord(word);
            if (Objects.nonNull(old)) {
                // 已存在的未删除词仅刷新更新审计字段，不改变原词文本和状态。
                super.updateById(old);
                if (StringUtils.isBlank(firstId)) {
                    firstId = old.getId();
                }
                continue;
            }
            CfgProductForbiddenWordEntity entity = new CfgProductForbiddenWordEntity();
            entity.setForbiddenWord(word);
            entity.setDisabled(DisabledEnum.ENABLE.getCode());
            super.save(entity);
            addOperateLog(entity.getId(), StrUtil.format("新增违禁词【{}】", word), "新增信息");
            if (StringUtils.isBlank(firstId)) {
                firstId = entity.getId();
            }
        }
        productForbiddenWordMatcher.refreshNow();
        return new BaseResultDTO.AddDTO(firstId, firstId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean update(CfgProductForbiddenWordDTO.UpdateDTO dto) {
        CfgProductForbiddenWordEntity old = super.getByIdOpt(dto.getId()).orElseThrow(() -> new ServiceException("未找到违禁词数据"));
        String word = normalizeAndValidateWord(dto.getForbiddenWord());
        Integer count = baseMapper.countByNormalizedWord(word, dto.getId());
        if (Objects.nonNull(count) && count > 0) {
            throw new ServiceException(StrUtil.format("【{}】已添加，请勿重复添加", word));
        }
        CfgProductForbiddenWordEntity entity = new CfgProductForbiddenWordEntity();
        entity.setId(old.getId());
        entity.setVersion(dto.getVersion());
        entity.setForbiddenWord(word);
        boolean update = super.updateById(entity);
        if (!update) {
            throw new ServiceException("违禁词保存失败");
        }
        addOperateLog(old.getId(),
                StrUtil.format("编辑违禁词：由【{}】变更为【{}】", old.getForbiddenWord(), word),
                "编辑信息");
        productForbiddenWordMatcher.refreshNow();
        return Boolean.TRUE;
    }

    @Override
    public List<CfgProductForbiddenWordDTO.TabListDTO> tabList() {
        List<CfgProductForbiddenWordDTO.TabListDTO> list = baseMapper.tabList();
        int total = list.stream().mapToInt(CfgProductForbiddenWordDTO.TabListDTO::getCount).sum();

        List<CfgProductForbiddenWordDTO.TabListDTO> result = new ArrayList<>(3);
        result.add(new CfgProductForbiddenWordDTO.TabListDTO("all", "全部", total));

        CfgProductForbiddenWordDTO.TabListDTO enabledTab = list.stream()
                .filter(item -> "f".equals(item.getTabFlag()))
                .findFirst()
                .orElse(new CfgProductForbiddenWordDTO.TabListDTO("f", "启用", 0));
        enabledTab.setTabFlagName("启用");
        result.add(enabledTab);

        CfgProductForbiddenWordDTO.TabListDTO disabledTab = list.stream()
                .filter(item -> "t".equals(item.getTabFlag()))
                .findFirst()
                .orElse(new CfgProductForbiddenWordDTO.TabListDTO("t", "禁用", 0));
        disabledTab.setTabFlagName("禁用");
        result.add(disabledTab);
        return result;
    }

    @Override
    public PagingVO<CfgProductForbiddenWordDTO.ListDTO> paging(PagingDTO<CfgProductForbiddenWordDTO.PagingParamDTO> dto) {
        PlmPagingSortSupport.sanitizeForbiddenWordSort(dto.getParams().getSortList());
        Page query = new Page(dto.getCurrPage(), dto.getPageSize());
        IPage<CfgProductForbiddenWordDTO.ListDTO> pageData = baseMapper.paging(query, dto.getParams());
        fillList(pageData.getRecords());
        return new PagingVO(pageData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO delete(String id) {
        CfgProductForbiddenWordEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到违禁词数据"));
        super.removeById(id);
        addOperateLog(entity.getId(), StrUtil.format("删除违禁词【{}】", entity.getForbiddenWord()), "删除信息");
        productForbiddenWordMatcher.refreshNow();
        return BatchResultDTO.success(entity.getId(), entity.getForbiddenWord(), OperationTypeEnum.DELETE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchResultDTO updateStatus(String id, Boolean disabled) {
        CfgProductForbiddenWordEntity entity = super.getByIdOpt(id).orElseThrow(() -> new ServiceException("未找到违禁词数据"));
        CfgProductForbiddenWordEntity updateEntity = new CfgProductForbiddenWordEntity();
        updateEntity.setId(id);
        updateEntity.setVersion(entity.getVersion());
        updateEntity.setDisabled(disabled);
        boolean updated = super.updateById(updateEntity);
        if (!updated) {
            throw new ServiceException("违禁词状态更新失败，数据已被修改");
        }
        addOperateLog(entity.getId(),
                StrUtil.format("违禁词【{}】状态由【{}】变更为【{}】",
                        entity.getForbiddenWord(), DisabledEnum.getName(entity.getDisabled()), DisabledEnum.getName(disabled)),
                "状态变更");
        productForbiddenWordMatcher.refreshNow();
        return BatchResultDTO.success(entity.getId(), entity.getForbiddenWord(), OperationTypeEnum.DISABLED);
    }

    @Override
    public Boolean exportList(CfgProductForbiddenWordDTO.PagingParamDTO dto) {
        downloadTaskFeign.saveDownloadTask("违禁词库", FileTaskEventEnum.EXPORT_PLM_PRODUCT_FORBIDDEN_WORD.getCode(), dto);
        return Boolean.TRUE;
    }

    @Override
    public PagingVO<CfgProductForbiddenWordExportExcelDTO> exportPaging(PagingDTO<CfgProductForbiddenWordDTO.PagingParamDTO> dto) {
        PagingVO<CfgProductForbiddenWordDTO.ListDTO> pageData = paging(dto);
        List<CfgProductForbiddenWordExportExcelDTO> exportList = pageData.getList().stream()
                .map(this::convertExportExcel)
                .collect(Collectors.toList());
        return new PagingVO<>(exportList, pageData.getTotalCount(), dto.getPageSize(), dto.getCurrPage());
    }

    @Override
    public List<String> listEnabledWords() {
        return baseMapper.listEnabledWords();
    }

    @Override
    public List<String> matchEnabledWords(String productName) {
        return productForbiddenWordMatcher.match(productName);
    }

    @Override
    public List<String> matchWords(String productName, List<String> enabledWords) {
        return productForbiddenWordMatcher.match(productName, enabledWords);
    }

    @Override
    public void validateProductName(String productName) {
        List<String> hitWords = matchEnabledWords(productName);
        if (CollectionUtils.isEmpty(hitWords)) {
            return;
        }
        String messageWords = hitWords.stream()
                .map(word -> StrUtil.format("【{}】", word))
                .collect(Collectors.joining(","));
        throw new ServiceException(StrUtil.format("产品品名包含启用违禁词{}，请修改", messageWords));
    }

    private void fillList(List<CfgProductForbiddenWordDTO.ListDTO> list) {
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        for (CfgProductForbiddenWordDTO.ListDTO item : list) {
            if (Objects.nonNull(item.getDisabled())) {
                item.setDisabledName(DisabledEnum.getName(item.getDisabled()));
            }
        }
    }

    private CfgProductForbiddenWordExportExcelDTO convertExportExcel(CfgProductForbiddenWordDTO.ListDTO item) {
        CfgProductForbiddenWordExportExcelDTO excelDTO = new CfgProductForbiddenWordExportExcelDTO();
        excelDTO.setForbiddenWord(item.getForbiddenWord());
        excelDTO.setDisabledName(item.getDisabledName());
        excelDTO.setCreateUserName(item.getCreateUserName());
        excelDTO.setCreateTime(item.getCreateTime());
        return excelDTO;
    }

    private List<String> normalizeAndValidateAddWords(List<String> inputWords) {
        if (CollectionUtils.isEmpty(inputWords)) {
            throw new ServiceException("违禁词不能为空");
        }
        Set<String> normalizedSet = new LinkedHashSet<>();
        List<String> words = new ArrayList<>(inputWords.size());
        for (String inputWord : inputWords) {
            String word = normalizeAndValidateWord(inputWord);
            String normalized = normalizeKey(word);
            if (!normalizedSet.add(normalized)) {
                throw new ServiceException(StrUtil.format("【{}】已添加，请勿重复添加", word));
            }
            words.add(word);
        }
        return words;
    }

    private String normalizeAndValidateWord(String inputWord) {
        String word = StringUtils.trim(inputWord);
        if (StringUtils.isBlank(word)) {
            throw new ServiceException("违禁词不能为空");
        }
        if (word.length() > FORBIDDEN_WORD_MAX_LENGTH) {
            throw new ServiceException("违禁词最大长度不能超过200位");
        }
        return word;
    }

    private String normalizeKey(String word) {
        return word.trim().toLowerCase(java.util.Locale.ROOT);
    }

    // 页面“查看日志”查询 PLM operate_log；Controller 的全局 @LogAction 不会写入该表。
    private void addOperateLog(String businessId, String content, String operation) {
        operateLogService.addSysLogBySave(content, String.valueOf(CfgProductForbiddenWordEntity.class), businessId, "", operation);
    }
}

package com.erp.server.sys.service.impl;

import com.common.business.mask.core.MaskClassDescriptor;
import com.common.business.mask.core.MaskClassDescriptorRegistry;
import com.common.business.mask.core.MaskFieldDescriptor;
import com.common.business.mask.handler.RegexSafetyGuard;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.dto.MaskDiagnoseDTO;
import com.erp.server.sys.service.MaskDiagnoseService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import lombok.extern.slf4j.Slf4j;

/**
 * 脱敏诊断服务实现：纯只读，无任何配置变更副作用
 *
 * @author cloud-erp
 */
@Slf4j
@Service
public class MaskDiagnoseServiceImpl implements MaskDiagnoseService {

    /** 脱敏后预览截断长度（避免大文本回写 HTTP body） */
    private static final int PREVIEW_LIMIT = 256;

    @Resource
    private MaskClassDescriptorRegistry registry;

    @Override
    public MaskDiagnoseDTO.RegexTestVO testRegex(MaskDiagnoseDTO.RegexTestDTO dto) {
        MaskDiagnoseDTO.RegexTestVO vo = new MaskDiagnoseDTO.RegexTestVO();
        vo.setRepeat(dto.getRepeat());
        vo.setInputLen(dto.getSampleText().length());

        // ① 静态分析：直接复用 CUSTOM 策略入库时的同一道闸门
        String reason = RegexSafetyGuard.check(dto.getRegex());
        if (reason != null) {
            vo.setSafe(false);
            vo.setReason(reason);
            vo.setCompiled(false);
            return vo;
        }
        vo.setSafe(true);

        // ② 编译可行性
        Pattern pattern;
        try {
            pattern = Pattern.compile(dto.getRegex());
            vo.setCompiled(true);
        } catch (Exception e) {
            vo.setCompiled(false);
            vo.setReason("正则编译失败：" + e.getMessage());
            return vo;
        }

        // ③ 单次匹配命中数（独立计算，避免被微基准影响）
        Matcher counter = pattern.matcher(dto.getSampleText());
        int matchCount = 0;
        while (counter.find()) {
            matchCount++;
        }
        vo.setMatchCount(matchCount);

        // ④ 运行时长度护栏：超长直接拒绝，让运维知道线上也会被拒
        if (dto.getSampleText().length() > RegexSafetyGuard.MAX_INPUT_LEN) {
            vo.setReason("sampleText 长度 " + dto.getSampleText().length()
                    + " 超过 CUSTOM 策略运行时上限 " + RegexSafetyGuard.MAX_INPUT_LEN + "，线上会被跳过");
            vo.setTotalCostUs(0L);
            vo.setAvgCostUs(0L);
            vo.setMaskedPreview(truncate(dto.getSampleText()));
            return vo;
        }

        // ⑤ 重复 N 次 replaceAll 测耗时（System.nanoTime 精度足够）
        int repeat = dto.getRepeat() == null ? 100 : dto.getRepeat();
        String replacement = dto.getReplacement() == null ? "***" : dto.getReplacement();
        String masked = dto.getSampleText();
        long start = System.nanoTime();
        for (int i = 0; i < repeat; i++) {
            masked = pattern.matcher(dto.getSampleText()).replaceAll(replacement);
        }
        long costNs = System.nanoTime() - start;
        long totalUs = costNs / 1_000L;

        vo.setTotalCostUs(totalUs);
        vo.setAvgCostUs(totalUs / Math.max(repeat, 1));
        vo.setMaskedPreview(truncate(masked));
        return vo;
    }

    @Override
    public MaskDiagnoseDTO.DescriptorVO describe(MaskDiagnoseDTO.DescriptorSearchDTO dto) {
        if (StringUtils.isBlank(dto.getClassPath())) {
            throw new ServiceException("classPath 不能为空");
        }
        Class<?> clazz;
        try {
            clazz = Class.forName(dto.getClassPath());
        } catch (ClassNotFoundException e) {
            throw new ServiceException("找不到类：" + dto.getClassPath()
                    + "（可能是 ClassLoader 隔离、模块未引入或类名拼错）");
        }

        MaskClassDescriptor desc = registry.of(clazz);
        MaskDiagnoseDTO.DescriptorVO vo = new MaskDiagnoseDTO.DescriptorVO();
        vo.setClassPath(dto.getClassPath());
        if (desc == MaskClassDescriptor.NO_MASK) {
            vo.setNoMask(true);
            vo.setTotalFields(0);
            vo.setFields(Collections.emptyList());
            return vo;
        }
        vo.setNoMask(false);
        List<MaskFieldDescriptor> fds = desc.getFields();
        vo.setTotalFields(fds.size());
        vo.setFields(fds.stream().map(this::toFieldVO).collect(Collectors.toList()));
        return vo;
    }

    @Override
    public MaskDiagnoseDTO.RegistryStatsVO stats() {
        MaskDiagnoseDTO.RegistryStatsVO vo = new MaskDiagnoseDTO.RegistryStatsVO();
        vo.setScannedClasses(registry.size());
        List<String> names = new ArrayList<>();
        for (Class<?> c : registry.snapshotKeys()) {
            names.add(c.getName());
        }
        Collections.sort(names);
        vo.setClassNames(names);
        return vo;
    }

    private MaskDiagnoseDTO.FieldVO toFieldVO(MaskFieldDescriptor fd) {
        MaskDiagnoseDTO.FieldVO v = new MaskDiagnoseDTO.FieldVO();
        v.setFieldName(fd.getField().getName());
        v.setFieldType(fd.getField().getType().getName());
        v.setStrategy(fd.getStrategy() == null ? null : fd.getStrategy().name());
        v.setRegex(fd.getRegex());
        v.setReplacement(fd.getReplacement());
        v.setPermission(fd.getPermission());
        v.setKeepEmpty(fd.isKeepEmpty());
        v.setRecursive(fd.isRecursive());
        v.setHideWhenMasked(fd.isHideWhenMasked());
        v.setContainer(fd.isContainer());
        return v;
    }

    private String truncate(String s) {
        if (s == null) {
            return null;
        }
        return s.length() <= PREVIEW_LIMIT ? s : s.substring(0, PREVIEW_LIMIT) + "...(truncated)";
    }
}

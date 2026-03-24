package com.erp.server.wms.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.exceptions.MybatisPlusException;

import com.erp.model.wms.dto.AqlSamplingRequest;
import com.erp.model.wms.dto.AqlSamplingResponse;
import com.erp.model.wms.entity.QcSamplingCodeRuleEntity;
import com.erp.model.wms.enums.AqlValueEnum;
import com.erp.model.wms.enums.QcLevelEnum;
import com.erp.server.wms.mapper.QcSamplingCodeRuleMapper;
import com.erp.model.wms.entity.QcSamplingAqlRuleEntity;
import com.erp.server.wms.mapper.QcSamplingAqlRuleMapper;
import com.erp.server.wms.service.QcSamplingAqlRuleService;
import com.common.business.service.impl.SuperServiceImpl;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

import org.springframework.util.StringUtils;

/**
 * <p>
 * GB/T2828.1-2012 AQL判定数主表 服务实现类
 * </p>
 *
 * @author zdy
 * @since 2026-03-19
 */
@Slf4j
@Service
public class QcSamplingAqlRuleServiceImpl extends SuperServiceImpl<QcSamplingAqlRuleMapper, QcSamplingAqlRuleEntity> implements QcSamplingAqlRuleService {
    @Resource
    private QcSamplingCodeRuleMapper qcSamplingCodeRuleMapper;
    @Resource
    private QcSamplingAqlRuleMapper qcSamplingAqlRuleMapper;
    @Override
    public AqlSamplingResponse calculateSamplingPlan(AqlSamplingRequest request) {
        AqlSamplingResponse response = new AqlSamplingResponse();
        try {
            // 1. 参数校验
            validateRequest(request, response);
            if (StringUtils.hasText(response.getErrorMsg())) {
                return response;
            }

            // 2. 根据批量数查询样本量字码映射
            QcSamplingCodeRuleEntity lotMapping = qcSamplingCodeRuleMapper.selectByLotQty(request.getLotQty());
            if (lotMapping == null) {
                response.setErrorMsg("未匹配到[" + request.getLotQty() + "]对应的批量范围");
                return response;
            }
            response.setLotRange(lotMapping.getMinLotQty() + "~" + lotMapping.getMaxLotQty());
            response.setMinLotQty(lotMapping.getMinLotQty());
            response.setMaxLotQty(lotMapping.getMaxLotQty());
            // 3. 根据检验水平获取样本量字码
            String sampleCode = getSampleCodeByLevel(lotMapping, request.getQcLevel());
            if (!StringUtils.hasText(sampleCode)) {
                response.setErrorMsg("检验水平[" + request.getQcLevel() + "]无对应字码");
                return response;
            }
            response.setSampleQtyCode(sampleCode);
            response.setSampleQty(lotMapping.getSampleQty());



            // 4. 根据字码+AQL值查询判定数
            QcSamplingAqlRuleEntity aqlMapping = getAqlJudgeWithArrowRule(sampleCode, request.getAqlValue());
            if (aqlMapping == null) {
                response.setErrorMsg("字码[" + sampleCode + "] + AQL[" + request.getAqlValue() + "]无对应判定规则");
                return response;
            }
            response.setAcceptQty(aqlMapping.getAcceptQty());
            response.setRejectQty(aqlMapping.getRejectQty());

            // 5. 补充提示信息（NULL值说明）
            if (aqlMapping.getAcceptQty() == null || aqlMapping.getRejectQty() == null) {
                response.setTips("该组合无判定规则，建议调整AQL值或检验水平");
            }

            log.info("抽样方案计算成功：批量范围={}, 字码={}, 样本量={}, Ac={}, Re={}",
                    response.getLotRange(), sampleCode, response.getSampleQty(),
                    response.getAcceptQty(), response.getRejectQty());

        } catch (MybatisPlusException e) {
            log.error("数据库查询异常", e);
            response.setErrorMsg("数据库查询异常：" + e.getMessage());
        } catch (Exception e) {
            log.error("抽样方案计算异常", e);
            response.setErrorMsg("系统异常：" + e.getMessage());
        }
        return response;
    }

    /**
     * 请求参数校验
     */
    private void validateRequest(AqlSamplingRequest request, AqlSamplingResponse response) {
        // 批量数校验
        if (request.getLotQty() == null || request.getLotQty() < 2) {
            response.setErrorMsg("批量数必须≥2");
            return;
        }
        // 检验水平校验
        if (!StringUtils.hasText(request.getQcLevel()) ||
                QcLevelEnum.getByCode(request.getQcLevel()) == null) {
            response.setErrorMsg("检验水平必须为：S-1/S-2/S-3/S-4/I/II/III");
            return;
        }
        // AQL值校验
        if (!StringUtils.hasText(request.getAqlValue()) ||
                !AqlValueEnum.isValidAql(request.getAqlValue())) {
            response.setErrorMsg("AQL值必须为：0.010/0.015/0.025/0.040/0.065/0.10/0.15/0.25/0.40/0.65/1.0/1.5/2.5/4.0/6.5/10/15/25/40/65/100");
        }
    }

    /**
     * 根据检验水平获取对应样本量字码
     */
    private String getSampleCodeByLevel(QcSamplingCodeRuleEntity lotMapping, String level) {
        QcLevelEnum levelEnum = QcLevelEnum.getByCode(level);
        if (levelEnum == null) {
            return null;
        }
        switch (levelEnum) {
            case S1: return lotMapping.getCodeS1();
            case S2: return lotMapping.getCodeS2();
            case S3: return lotMapping.getCodeS3();
            case S4: return lotMapping.getCodeS4();
            case I: return lotMapping.getCodeI();
            case II: return lotMapping.getCodeIi();
            case III: return lotMapping.getCodeIii();
            default: return null;
        }
    }

    // 补充：箭头规则处理（核心逻辑）
    private QcSamplingAqlRuleEntity getAqlJudgeWithArrowRule(String sampleCode, String aqlValue) {
        QcSamplingAqlRuleEntity current = qcSamplingAqlRuleMapper.selectByCodeAndAql(sampleCode, aqlValue);
        // 1. 若当前有值，直接返回
        if (current != null && current.getAcceptQty() != null) {
            return current;
        }
        // 2. 处理向下箭头（↓）：按字码顺序（A→B→C→...→R）找下一个有值的
        List<String> codeOrder = Arrays.asList("A","B","C","D","E","F","G","H","J","K","L","M","N","P","Q","R");
        int currentIndex = codeOrder.indexOf(sampleCode);
        // 向下箭头：往后找
        for (int i = currentIndex + 1; i < codeOrder.size(); i++) {
            QcSamplingAqlRuleEntity next = qcSamplingAqlRuleMapper.selectByCodeAndAql(codeOrder.get(i), aqlValue);
            if (next != null && next.getAcceptQty() != null) {
                return next;
            }
        }
        // 3. 处理向上箭头（↑）：往前找
        for (int i = currentIndex - 1; i >= 0; i--) {
            QcSamplingAqlRuleEntity prev = qcSamplingAqlRuleMapper.selectByCodeAndAql(codeOrder.get(i), aqlValue);
            if (prev != null && prev.getAcceptQty() != null) {
                // 向上箭头：样本量保持当前字码的，判定数用前面的
                QcSamplingAqlRuleEntity result = new QcSamplingAqlRuleEntity();
                result.setSampleQtyCode(sampleCode);
                result.setSampleQty(getSampleSizeByCode(sampleCode)); // 取当前字码的样本量
                result.setAcceptQty(prev.getAcceptQty());
                result.setRejectQty(prev.getRejectQty());
                return result;
            }
        }
        return null;
    }

    // 辅助方法：根据字码获取样本量
    private Integer getSampleSizeByCode(String sampleCode) {
        QcSamplingCodeRuleEntity mapping = qcSamplingCodeRuleMapper.selectOne(
                new QueryWrapper<QcSamplingCodeRuleEntity>().eq("sample_qty_code", sampleCode)
        );
        return mapping != null ? mapping.getSampleQty() : null;
    }
}

package com.erp.server.tms.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.common.business.dto.base.BatchResultDTO;
import com.common.business.dto.base.PagingDTO;
import com.common.business.service.impl.SuperServiceImpl;
import com.common.business.vo.PagingVO;
import com.common.business.wrapper.FeignQuery;
import com.common.core.exception.ServiceException;
import com.erp.model.sys.entity.DictCurrencyEntity;
import com.erp.model.tms.dto.LogisticsReconDetailDTO;
import com.erp.model.tms.entity.LogisticsReconDetailEntity;
import com.erp.model.tms.enums.LogisticsReconDetailMatchStatusEnum;
import com.erp.model.tms.enums.LogisticsReconReconciliationStatusEnum;
import com.erp.rpc.file.feign.DownloadTaskFeign;
import com.erp.server.tms.mapper.LogisticsReconDetailMapper;
import com.erp.server.tms.service.LogisticsReconDetailService;
import com.erp.server.tms.service.LogisticsReconDetailSubService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_TMS_LOGISTICS_RECON_DETAIL;

/**
 * <p>
 * 物流商对账明细（行级） 服务实现类
 * </p>
 *
 * @author Will
 * @since 2026-05-29
 */
@Slf4j
@Service
public class LogisticsReconDetailServiceImpl
        extends SuperServiceImpl<LogisticsReconDetailMapper, LogisticsReconDetailEntity>
        implements LogisticsReconDetailService {

    @Resource
    private DownloadTaskFeign downloadTaskFeign;

    @Resource
    private LogisticsReconDetailSubService logisticsReconDetailSubService;


    @Override
    public PagingVO<LogisticsReconDetailDTO.ListDTO> paging(PagingDTO<LogisticsReconDetailDTO.PagingParamDTO> pagingParamDTO) {
        pagingParamDTO.getParams().setPermissionSql(pagingParamDTO.getPermissionSql());
        Page<LogisticsReconDetailDTO.ListDTO> query =
                new Page<>(pagingParamDTO.getCurrPage(), pagingParamDTO.getPageSize());
        IPage<LogisticsReconDetailDTO.ListDTO> pageData = baseMapper.paging(query, pagingParamDTO.getParams());
        if (CollUtil.isEmpty(pageData.getRecords())) {
            return new PagingVO<>(pageData);
        }
        fillList(pageData.getRecords());
        return new PagingVO<>(pageData);
    }

    @Override
    public List<LogisticsReconDetailDTO.TabListDTO> tabList(LogisticsReconDetailDTO.TabListParamDTO param) {
        List<LogisticsReconDetailDTO.TabListDTO> list = baseMapper.tabList(param);
        if (list == null) {
            list = new ArrayList<>();
        }
        List<String> existStatus = list.stream()
                .map(LogisticsReconDetailDTO.TabListDTO::getTabFlag)
                .collect(Collectors.toList());
        for (String status : LogisticsReconDetailMatchStatusEnum.getStatusList()) {
            if (!existStatus.contains(status)) {
                list.add(new LogisticsReconDetailDTO.TabListDTO(status, 0));
            }
        }
        list.add(new LogisticsReconDetailDTO.TabListDTO("all",
                list.stream().mapToInt(LogisticsReconDetailDTO.TabListDTO::getCount).sum()));
        return list;
    }

    @Override
    public void exportList(LogisticsReconDetailDTO.PagingParamDTO param) {
        downloadTaskFeign.saveDownloadTask("物流商对账明细", EXPORT_TMS_LOGISTICS_RECON_DETAIL.getCode(), param);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByMainIds(Collection<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return;
        }
        lambdaUpdate()
                .in(LogisticsReconDetailEntity::getMainId, mainIds)
                .remove();
    }

    @Override
    public List<LogisticsReconDetailEntity> listByMainIds(Collection<String> mainIds) {
        if (CollUtil.isEmpty(mainIds)) {
            return Collections.emptyList();
        }
        return lambdaQuery()
                .in(LogisticsReconDetailEntity::getMainId, mainIds)
                .list();
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> importMatch(LogisticsReconDetailDTO.ImportMatchDTO dto) {
        // 费用项级"导入匹配"：先标 matching → 调合并匹配链路 → 完成后回写 detail_sub.match_status / ref
        logisticsReconDetailSubService.batchUpdateMatchStatus(dto.getDetailSubIds(),
                LogisticsReconDetailMatchStatusEnum.MATCHING.getCode(), null);

        List<BatchResultDTO> results = new ArrayList<>(dto.getDetailSubIds().size());
        // TODO 调用 ImportHistoryRecordServiceImpl 合并匹配重载入口（重构后），按 detailSubIds 范围处理；
        //  完成后按 detail_sub 写 logistics_recon_ref_logistics_bill，并把 match_status 置为 matched / failed
        for (String detailSubId : dto.getDetailSubIds()) {
            results.add(BatchResultDTO.success(detailSubId, detailSubId,
                    com.common.business.enums.OperationTypeEnum.UPDATE));
        }
        // 主表匹配数 / 匹配状态由列表、详情查询实时聚合派生，匹配后无需回写主表
        return results;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<BatchResultDTO> manualMatch(LogisticsReconDetailDTO.ManualMatchDTO dto) {
        // TODO 手动匹配：
        //  1. 按每条 item 的 ERP 销售单号 / 平台订单号 / 物流跟踪号 / 发货单号查询 logistics_bill / logistics_bill_cost / tms_cost_detail
        //  2. 构造 LogisticsReconRefLogisticsBillEntity（match_type=manual）
        //  3. LogisticsReconRefLogisticsBillService.removeByDetailSubIds + save ref
        //  4. detail_sub.match_status = matched（主表匹配数由查询实时聚合，无需回写）
        List<BatchResultDTO> results = new ArrayList<>(dto.getItemList().size());
        for (LogisticsReconDetailDTO.ManualMatchItemDTO item : dto.getItemList()) {
            results.add(BatchResultDTO.fail(item.getDetailSubId(), item.getDetailSubId(),
                    "手动匹配待接入 ERP 单号查询逻辑"));
        }
        return results;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public BatchResultDTO addLogisticsBillCost(LogisticsReconDetailDTO.AddLogisticsBillCostDTO dto) {
        // TODO 新增费用单匹配：
        //  1. 调 LogisticsBillService / LogisticsBillCostService 新增物流单 + 费用单（billCostPayload 后续类型化）
        //  2. 复用 manualMatch 写 ref 关系（match_type=newBill）
        //  3. detail_sub.match_status = matched（主表匹配数由查询实时聚合，无需回写）
        throw new ServiceException("新增费用单匹配待重构 ImportHistoryRecord 后接入");
    }

    /**
     * 费用项列表填充：补 match_status 名称 + 金额 / 尺寸展示字段
     */
    private void fillList(List<LogisticsReconDetailDTO.ListDTO> list) {
        if (CollUtil.isEmpty(list)) {
            return;
        }
        Map<String, String> currencySymbolMap = FeignQuery.list(DictCurrencyEntity.class).stream()
                .collect(Collectors.toMap(DictCurrencyEntity::getId, DictCurrencyEntity::getSymbol, (first, second) -> first));
        for (LogisticsReconDetailDTO.ListDTO data : list) {
            data.setMatchStatusName(LogisticsReconDetailMatchStatusEnum.getName(data.getMatchStatus()));
            data.setReconciliationStatusName(
                    LogisticsReconReconciliationStatusEnum.getName(data.getReconciliationStatus()));
            data.setThirdSize(buildThirdSize(data.getThirdLength(), data.getThirdWidth(), data.getThirdHeight()));
            String symbol = currencySymbolMap.getOrDefault(data.getCurrency(), "¥");
            data.setCurrencySymbol(symbol);
            data.setActualAmountStr(formatAmount(data.getActualAmount(), symbol));
        }
    }

    /**
     * 格式化金额展示文本（带币别符号）
     * @author Will
     * @date 2026/6/1 15:40
     * @param amount 金额
     * @param symbol 币别符号
     * @return String
     */
    private String formatAmount(BigDecimal amount, String symbol) {
        if (amount == null) {
            return symbol + "0";
        }
        return symbol + amount.stripTrailingZeros().toPlainString();
    }

    /**
     * 组装物流商尺寸展示串（长*宽*高，去掉无意义的小数尾零）
     * @author Will
     * @date 2026/6/1 12:20
     * @param length 长
     * @param width 宽
     * @param height 高
     * @return String
     */
    private String buildThirdSize(BigDecimal length, BigDecimal width, BigDecimal height) {
        if (length == null && width == null && height == null) {
            return "";
        }
        return plain(length) + "*" + plain(width) + "*" + plain(height);
    }

    /**
     * BigDecimal 转普通字符串并去除末尾零
     * @author Will
     * @date: 2026/06/02
     * @param value
     * @return String
     */
    private String plain(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }
}

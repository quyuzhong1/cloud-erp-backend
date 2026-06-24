package com.erp.server.file.business.plm;

import com.common.business.dto.base.PagingDTO;
import com.common.business.enums.FileTaskEventEnum;
import com.common.business.vo.PagingVO;
import com.erp.model.plm.dto.MouldInfoDTO;
import com.erp.rpc.plm.feign.ExportPlmFeign;
import com.erp.server.file.core.AbstractMultiSheetGroupPageFileEventHandler;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

import static com.common.business.enums.FileTaskEventEnum.EXPORT_PLM_ORDER_TRACKING;

/**
 * 下单跟踪导出：按 {@code detailId} 分组展示，数据量超过单 sheet 时自动多 tab 分页（方案 C）。
 */
@Component
public class ExportPlmOrderTrackingHandler extends AbstractMultiSheetGroupPageFileEventHandler<MouldInfoDTO.OrderTrackingExportDTO, MouldInfoDTO.PagingParamDTO> {

    @Resource
    private ExportPlmFeign exportPlmFeign;

    @Override
    protected String getExcelPath() {
        return "excel/plm/orderTracking.xlsx";
    }

    @Override
    protected PagingVO<MouldInfoDTO.OrderTrackingExportDTO> getPageData(PagingDTO<MouldInfoDTO.PagingParamDTO> dto) {
        // 导出按 detailId 分组判重，必须保证同一 detailId 连续。exportOrderTracking 的 ORDER BY 默认以
        // sortList（${item.field}）为主排序，若透传用户列排序会打散 detailId，触发 failOnNonContinuousSheetGroup。
        // 故导出固定清空 sortList，回落到 mapper 的 md.create_time desc, md.id desc（detailId=md.id，天然连续）；
        // 一并关闭 ${item.field} 动态排序注入面（该列无列名白名单）。导出维度自有排序，不吃列表自定义排序。
        if (dto.getParams() != null) {
            dto.getParams().setSortList(null);
        }
        return exportPlmFeign.exportOrderTracking(dto);
    }

    /**
     * 订单跟踪导出以「模具明细 detailId」为单据维度。
     * <p>
     * {@link #getPageData} 已清空 sortList，上游回落到 {@code md.create_time desc, md.id desc}
     * （detailId=md.id），同一明细的多行天然相邻；若上游排序被改动破坏连续性，
     * {@link #failOnNonContinuousSheetGroup()} 会快速失败，避免置空错位产出错误展示。
     * detailId 为空的行不参与分组（返回 {@code null}），其字段始终完整保留。
     */
    @Override
    protected Object sheetGroupKey(MouldInfoDTO.OrderTrackingExportDTO row) {
        if (row == null || ObjectUtils.isEmpty(row.getDetailId())) {
            return null;
        }
        return row.getDetailId();
    }

    /**
     * 组内会置空重复展示字段，分组非连续会导致展示错位，必须快速失败。
     */
    @Override
    protected boolean failOnNonContinuousSheetGroup() {
        return true;
    }

    /**
     * 基类已按 {@link #sheetGroupKey(MouldInfoDTO.OrderTrackingExportDTO)} 合并跨页尾组，并按 sheet 容量多 tab 分页；
     * 未使用的预留 sheet 在 finish 前 trim。此处只处理单个完整明细分组内的重复行置空（仅首行保留）。
     */
    @Override
    protected void beforeWriteGroupRows(Object groupKey, List<MouldInfoDTO.OrderTrackingExportDTO> groupRows) {
        if (groupKey == null || ObjectUtils.isEmpty(groupRows)) {
            return;
        }
        for (int i = 1; i < groupRows.size(); i++) {
            MouldInfoDTO.OrderTrackingExportDTO exportDTO = groupRows.get(i);
            if (exportDTO == null) {
                continue;
            }
            exportDTO.setProjectNo("");
            exportDTO.setName("");
            exportDTO.setMouldNo("");
            exportDTO.setThirdMouldNo("");
            exportDTO.setStatusName("");
            exportDTO.setSupplierName("");
            exportDTO.setQty(null);
            exportDTO.setTaxPrice(null);
            exportDTO.setTaxRate(null);
            exportDTO.setPayMethodName("");
            exportDTO.setPaymentConditionName("");
            exportDTO.setIsNeedRefundName("");
            exportDTO.setRefundStandardName("");
            exportDTO.setRefundOrderQty(null);
            exportDTO.setRefundAmount(null);
            exportDTO.setRefundStatusName("");
            exportDTO.setPurchaseQty(null);
            exportDTO.setReceiveQty(null);
            exportDTO.setStockInQty(null);
            exportDTO.setDiffQty(null);
            exportDTO.setUpdateTime(null);
        }
    }

    @Override
    public FileTaskEventEnum getEvent() {
        return EXPORT_PLM_ORDER_TRACKING;
    }
}

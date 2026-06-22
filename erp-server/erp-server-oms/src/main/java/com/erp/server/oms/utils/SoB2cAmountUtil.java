package com.erp.server.oms.utils;

import cn.hutool.core.collection.CollUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.dto.SoB2cDetailDTO;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * B2C 销售订单金额计算工具
 *
 * <p>统一处理：
 * <ul>
 *   <li>主表 paid_amount = amount - total_discount（实付总额/订单付款总额）</li>
 *   <li>明细 sale_amount = price × qty（销售金额，折前）</li>
 *   <li>明细 paid_amount = sale_amount / main.amount × main.paid_amount，最后一行吃精度差</li>
 *   <li>明细 discount_amount = sale_amount - paid_amount</li>
 * </ul>
 *
 * <p>主表 totalDiscount（订单折扣总额）落库口径：sum(明细.discountAmount)；平台未推送主表折扣时同样按明细汇总回填。
 * 调用后明细的 saleAmount/paidAmount/discountAmount 与主表 paidAmount/totalDiscount 都会被覆盖写入。
 */
public final class SoB2cAmountUtil {

    private static final int SCALE = 4;

    private SoB2cAmountUtil() {
    }

    /**
     * 忽略入参中的实付总额（订单付款总额）。
     */
    public static void ignoreRequestMainPaidAmount(SoB2cEntity main) {
        if (main != null) {
            main.setPaidAmount(null);
        }
    }

    /**
     * 忽略入参中的订单折扣总额（手工新增/编辑不落库入参值，明细保存后按明细折扣汇总）。
     */
    public static void ignoreRequestMainTotalDiscount(SoB2cEntity main) {
        if (main != null) {
            main.setTotalDiscount(null);
        }
    }

    /**
     * 主表是否未推送/未设置折扣总额（null 或 0 视为缺失）。
     */
    public static boolean isMainTotalDiscountMissing(SoB2cEntity main) {
        if (main == null || main.getTotalDiscount() == null) {
            return true;
        }
        return main.getTotalDiscount().compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * 明细折扣额合计。
     */
    public static BigDecimal sumDetailDiscountAmount(List<SoB2cDetailEntity> details) {
        if (CollUtil.isEmpty(details)) {
            return BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
        }
        return details.stream()
                .map(SoB2cDetailEntity::getDiscountAmount)
                .map(MathUtil::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 主表折扣总额 = sum(明细.discountAmount)，并重算实付总额。
     *
     * @param onlyIfMissing true 时仅平台未推送主表折扣才回填；false 时强制以明细汇总为准（手工单）
     */
    public static void syncMainTotalDiscountFromDetails(SoB2cEntity main, List<SoB2cDetailEntity> details, boolean onlyIfMissing) {
        if (main == null) {
            return;
        }
        if (onlyIfMissing && !isMainTotalDiscountMissing(main)) {
            applyMainPaidAmount(main);
            return;
        }
        main.setTotalDiscount(sumDetailDiscountAmount(details));
        applyMainPaidAmount(main);
    }

    /**
     * 明细金额分摊前准备主表实付：有折扣总额按 amount-discount；否则平台付款金额；否则等于订单销售总额。
     */
    public static void prepareMainPaidAmountForDetailCalc(SoB2cEntity main) {
        if (main == null) {
            return;
        }
        BigDecimal amount = MathUtil.getValue(main.getAmount());
        if (!isMainTotalDiscountMissing(main)) {
            applyMainPaidAmount(main);
            return;
        }
        BigDecimal payAmount = main.getPayAmount();
        if (payAmount != null && payAmount.compareTo(BigDecimal.ZERO) > 0 && payAmount.compareTo(amount) <= 0) {
            main.setPaidAmount(payAmount.setScale(SCALE, RoundingMode.HALF_UP));
            return;
        }
        main.setPaidAmount(amount.setScale(SCALE, RoundingMode.HALF_UP));
    }

    /**
     * 忽略入参中的明细销售金额/实付金额/折扣额。
     */
    public static void ignoreRequestDetailAmountFields(List<SoB2cDetailEntity> details) {
        if (CollUtil.isEmpty(details)) {
            return;
        }
        for (SoB2cDetailEntity detail : details) {
            detail.setSaleAmount(null);
            detail.setPaidAmount(null);
            detail.setDiscountAmount(null);
        }
    }

    /**
     * 销售金额 = 单价 × 数量。
     */
    public static BigDecimal calcSaleAmount(BigDecimal price, Integer qty) {
        BigDecimal qtyDecimal = qty == null ? BigDecimal.ZERO : new BigDecimal(qty);
        return MathUtil.getValue(price).multiply(qtyDecimal).setScale(SCALE, RoundingMode.HALF_UP);
    }

    /**
     * 手工新增/编辑：主表订单销售总额由明细单价×数量汇总；折扣总额在明细保存后汇总回填。
     */
    public static void applyMainAmountsFromDetailAddDtos(SoB2cEntity main, List<SoB2cDetailDTO.AddDTO> details) {
        applyMainSaleAmountFromDetailAddDtos(main, details);
    }

    /**
     * 手工编辑：主表订单销售总额由明细单价×数量汇总；折扣总额在明细保存后汇总回填。
     */
    public static void applyMainAmountsFromDetailUpdateDtos(SoB2cEntity main, List<SoB2cDetailDTO.UpdateDTO> details) {
        applyMainSaleAmountFromDetailUpdateDtos(main, details);
    }

    private static void applyMainSaleAmountFromDetailAddDtos(SoB2cEntity main, List<SoB2cDetailDTO.AddDTO> details) {
        if (main == null || CollUtil.isEmpty(details)) {
            return;
        }
        BigDecimal totalSale = BigDecimal.ZERO;
        for (SoB2cDetailDTO.AddDTO detail : details) {
            totalSale = totalSale.add(calcSaleAmount(detail.getPrice(), detail.getQty()));
        }
        main.setAmount(totalSale.setScale(SCALE, RoundingMode.HALF_UP));
    }

    private static void applyMainSaleAmountFromDetailUpdateDtos(SoB2cEntity main, List<SoB2cDetailDTO.UpdateDTO> details) {
        if (main == null || CollUtil.isEmpty(details)) {
            return;
        }
        BigDecimal totalSale = BigDecimal.ZERO;
        for (SoB2cDetailDTO.UpdateDTO detail : details) {
            totalSale = totalSale.add(calcSaleAmount(detail.getPrice(), detail.getQty()));
        }
        main.setAmount(totalSale.setScale(SCALE, RoundingMode.HALF_UP));
    }

    /**
     * 手工新增/编辑明细：先汇总主表销售总额，再重算主表实付总额与明细三段金额。
     */
    public static void applyAllForManualDetailSave(SoB2cEntity main, List<SoB2cDetailEntity> details) {
        if (main == null || CollUtil.isEmpty(details)) {
            return;
        }
        ignoreRequestDetailAmountFields(details);
        for (SoB2cDetailEntity detail : details) {
            detail.setSaleAmount(calcSaleAmount(detail.getPrice(), detail.getQty()));
        }
        BigDecimal totalSale = details.stream()
                .map(SoB2cDetailEntity::getSaleAmount)
                .map(MathUtil::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        main.setAmount(totalSale.setScale(SCALE, RoundingMode.HALF_UP));
        prepareMainPaidAmountForDetailCalc(main);
        applyDetailPaidAndDiscountAmounts(main, details);
        syncMainTotalDiscountFromDetails(main, details, false);
    }

    /**
     * 主表实付总额：amount - totalDiscount。
     */
    public static void applyMainPaidAmount(SoB2cEntity main) {
        if (main == null) {
            return;
        }
        BigDecimal amount = MathUtil.getValue(main.getAmount());
        BigDecimal discount = MathUtil.getValue(main.getTotalDiscount());
        main.setPaidAmount(amount.subtract(discount).setScale(SCALE, RoundingMode.HALF_UP));
    }

    /**
     * 明细销售金额、实付金额、折扣额：
     * <ul>
     *   <li>saleAmount = price × qty</li>
     *   <li>paidAmount 按 saleAmount 占 main.amount 的比例分摊 main.paidAmount，最后一行 = paidAmount - sum(前面)</li>
     *   <li>discountAmount = saleAmount - paidAmount</li>
     * </ul>
     * 必须先调用 {@link #applyMainPaidAmount(SoB2cEntity)}。
     */
    public static void applyDetailAmounts(SoB2cEntity main, List<SoB2cDetailEntity> details) {
        applyDetailAmounts(main, details, true);
    }

    /**
     * 计算明细三段金额，并按需回填主表折扣总额。
     *
     * @param syncMainTotalDiscountOnlyIfMissing 平台 true：仅主表未推送折扣时 sum(明细)；手工 false：强制 sum(明细)
     */
    public static void applyDetailAmounts(SoB2cEntity main, List<SoB2cDetailEntity> details, boolean syncMainTotalDiscountOnlyIfMissing) {
        if (main == null || CollUtil.isEmpty(details)) {
            return;
        }
        for (SoB2cDetailEntity d : details) {
            d.setSaleAmount(calcSaleAmount(d.getPrice(), d.getQty()));
        }
        prepareMainPaidAmountForDetailCalc(main);
        applyDetailPaidAndDiscountAmounts(main, details);
        syncMainTotalDiscountFromDetails(main, details, syncMainTotalDiscountOnlyIfMissing);
    }

    private static void applyDetailPaidAndDiscountAmounts(SoB2cEntity main, List<SoB2cDetailEntity> details) {
        BigDecimal mainAmount = MathUtil.getValue(main.getAmount());
        BigDecimal paidTotal = MathUtil.getValue(main.getPaidAmount());

        if (mainAmount.compareTo(BigDecimal.ZERO) == 0) {
            for (SoB2cDetailEntity d : details) {
                d.setPaidAmount(BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP));
                d.setDiscountAmount(MathUtil.getValue(d.getSaleAmount())
                        .setScale(SCALE, RoundingMode.HALF_UP));
            }
            return;
        }

        BigDecimal allocated = BigDecimal.ZERO;
        int lastIdx = details.size() - 1;
        for (int i = 0; i < details.size(); i++) {
            SoB2cDetailEntity d = details.get(i);
            BigDecimal sale = MathUtil.getValue(d.getSaleAmount());
            BigDecimal paid;
            if (i == lastIdx) {
                paid = paidTotal.subtract(allocated).setScale(SCALE, RoundingMode.HALF_UP);
            } else {
                paid = sale.multiply(paidTotal)
                        .divide(mainAmount, SCALE, RoundingMode.HALF_UP);
                allocated = allocated.add(paid);
            }
            d.setPaidAmount(paid);
            d.setDiscountAmount(sale.subtract(paid).setScale(SCALE, RoundingMode.HALF_UP));
        }
    }

    /**
     * 一站式计算：主表 paidAmount + 明细 saleAmount/paidAmount/discountAmount。
     */
    public static void applyAll(SoB2cEntity main, List<SoB2cDetailEntity> details) {
        applyDetailAmounts(main, details, true);
    }

    /**
     * 下游同步取值兜底：优先用新字段（折后），未回填或仍为 0 时回退到旧字段。
     * 适用场景：金蝶/数帝云 等下游接口要"实付/成交"语义，但数据可能尚未跑回填脚本。
     */
    public static BigDecimal preferPositive(BigDecimal preferred, BigDecimal fallback) {
        if (preferred != null && preferred.compareTo(BigDecimal.ZERO) > 0) {
            return preferred;
        }
        return fallback;
    }

    /**
     * 子集重分摊：BOM 套装明细被拆成多条子件时使用。
     *
     * <p>区别于 {@link #applyDetailAmounts}：本方法把"父明细的实付金额"等比例分摊到 children 内部，
     * 不动订单其他未拆明细，避免 last-eats-diff 误把整单 paidAmount 灌进最后一条子件。
     *
     * <p>计算口径：
     * <ul>
     *   <li>每个 child.saleAmount 取 child.amount（拆分代码已先设置好）</li>
     *   <li>parentSaleAmount/parentPaidAmount 取自原父明细，老数据回退用 parent.amount</li>
     *   <li>child.paidAmount 按 child.saleAmount 占 sum(child.saleAmount) 的比例分摊 parentPaidAmount，
     *       最后一条吃精度差</li>
     *   <li>child.discountAmount = child.saleAmount - child.paidAmount</li>
     * </ul>
     */
    public static void redistributeChildAmounts(SoB2cDetailEntity parent, List<SoB2cDetailEntity> children) {
        if (parent == null || CollUtil.isEmpty(children)) {
            return;
        }
        BigDecimal parentSale = MathUtil.getValue(parent.getSaleAmount());
        BigDecimal parentPaid = MathUtil.getValue(parent.getPaidAmount());
        // 老数据兜底：父明细未回填新字段时，按"无折扣"对齐到 amount
        if (parentSale.compareTo(BigDecimal.ZERO) == 0) {
            parentSale = MathUtil.getValue(parent.getAmount());
            if (parentPaid.compareTo(BigDecimal.ZERO) == 0) {
                parentPaid = parentSale;
            }
        }
        BigDecimal childrenSaleSum = BigDecimal.ZERO;
        for (SoB2cDetailEntity c : children) {
            BigDecimal sale = MathUtil.getValue(c.getAmount());
            c.setSaleAmount(sale.setScale(SCALE, RoundingMode.HALF_UP));
            childrenSaleSum = childrenSaleSum.add(sale);
        }

        BigDecimal allocated = BigDecimal.ZERO;
        int lastIdx = children.size() - 1;
        for (int i = 0; i < children.size(); i++) {
            SoB2cDetailEntity c = children.get(i);
            BigDecimal sale = MathUtil.getValue(c.getSaleAmount());
            BigDecimal paid;
            if (childrenSaleSum.compareTo(BigDecimal.ZERO) == 0) {
                paid = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_UP);
            } else if (i == lastIdx) {
                paid = parentPaid.subtract(allocated).setScale(SCALE, RoundingMode.HALF_UP);
            } else {
                paid = sale.multiply(parentPaid)
                        .divide(childrenSaleSum, SCALE, RoundingMode.HALF_UP);
                allocated = allocated.add(paid);
            }
            c.setPaidAmount(paid);
            c.setDiscountAmount(sale.subtract(paid).setScale(SCALE, RoundingMode.HALF_UP));
        }
    }

    /**
     * 列表/详情展示：补全明细 saleAmount/paidAmount/discountAmount（原币），未落库时按主表比例分摊兜底。
     */
    public static void fillDetailListDisplayAmounts(List<SoB2cDetailDTO.ListDTO> details, BigDecimal mainAmount, BigDecimal mainPaidAmount) {
        if (CollUtil.isEmpty(details)) {
            return;
        }
        mainAmount = MathUtil.getValue(mainAmount);
        mainPaidAmount = MathUtil.getValue(preferPositive(mainPaidAmount, mainAmount));

        boolean anyDetailHasNewAmount = details.stream().anyMatch(d ->
                (d.getSaleAmount() != null && d.getSaleAmount().compareTo(BigDecimal.ZERO) > 0)
                        || (d.getPaidAmount() != null && d.getPaidAmount().compareTo(BigDecimal.ZERO) > 0)
                        || (d.getDiscountAmount() != null && d.getDiscountAmount().compareTo(BigDecimal.ZERO) != 0));

        if (!anyDetailHasNewAmount && mainAmount.compareTo(BigDecimal.ZERO) > 0) {
            allocateDetailDisplayAmountsFromMain(details, mainAmount, mainPaidAmount);
            return;
        }
        fillPartialDetailDisplayAmounts(details, mainAmount, mainPaidAmount);
    }

    /**
     * 详情展示：与 {@link #fillDetailListDisplayAmounts} 逻辑一致，作用于 ViewDTO。
     */
    public static void fillDetailViewDisplayAmounts(List<SoB2cDetailDTO.ViewDTO> details, BigDecimal mainAmount, BigDecimal mainPaidAmount) {
        if (CollUtil.isEmpty(details)) {
            return;
        }
        List<SoB2cDetailDTO.ListDTO> listDTOs = new ArrayList<>(details.size());
        for (SoB2cDetailDTO.ViewDTO detail : details) {
            SoB2cDetailDTO.ListDTO listDTO = new SoB2cDetailDTO.ListDTO();
            listDTO.setAmount(detail.getAmount());
            listDTO.setSaleAmount(detail.getSaleAmount());
            listDTO.setPaidAmount(detail.getPaidAmount());
            listDTO.setDiscountAmount(detail.getDiscountAmount());
            listDTOs.add(listDTO);
        }
        fillDetailListDisplayAmounts(listDTOs, mainAmount, mainPaidAmount);
        for (int i = 0; i < details.size(); i++) {
            SoB2cDetailDTO.ViewDTO viewDTO = details.get(i);
            SoB2cDetailDTO.ListDTO listDTO = listDTOs.get(i);
            viewDTO.setSaleAmount(listDTO.getSaleAmount());
            viewDTO.setPaidAmount(listDTO.getPaidAmount());
            viewDTO.setDiscountAmount(listDTO.getDiscountAmount());
        }
    }

    private static void allocateDetailDisplayAmountsFromMain(List<SoB2cDetailDTO.ListDTO> details, BigDecimal mainAmount, BigDecimal mainPaidAmount) {
        BigDecimal allocated = BigDecimal.ZERO;
        int lastIdx = details.size() - 1;
        for (int i = 0; i < details.size(); i++) {
            SoB2cDetailDTO.ListDTO detail = details.get(i);
            BigDecimal saleAmount = MathUtil.getValue(preferPositive(detail.getSaleAmount(), detail.getAmount()));
            detail.setSaleAmount(saleAmount.setScale(SCALE, RoundingMode.HALF_UP));
            BigDecimal paid;
            if (i == lastIdx) {
                paid = mainPaidAmount.subtract(allocated).setScale(SCALE, RoundingMode.HALF_UP);
            } else {
                paid = saleAmount.multiply(mainPaidAmount).divide(mainAmount, SCALE, RoundingMode.HALF_UP);
                allocated = allocated.add(paid);
            }
            detail.setPaidAmount(paid);
            detail.setDiscountAmount(saleAmount.subtract(paid).setScale(SCALE, RoundingMode.HALF_UP));
        }
    }

    /**
     * 部分明细已有新金额字段：保留已落库 paidAmount，对其余明细按 saleAmount 比例分摊剩余实付，最后一行吃尾差。
     */
    private static void fillPartialDetailDisplayAmounts(List<SoB2cDetailDTO.ListDTO> details, BigDecimal mainAmount, BigDecimal mainPaidAmount) {
        List<SoB2cDetailDTO.ListDTO> needPaidAlloc = new ArrayList<>();
        BigDecimal saleTotalForAlloc = BigDecimal.ZERO;
        BigDecimal fixedPaidSum = BigDecimal.ZERO;

        for (SoB2cDetailDTO.ListDTO detail : details) {
            BigDecimal legacyAmount = MathUtil.getValue(detail.getAmount());
            BigDecimal saleAmount = preferPositive(detail.getSaleAmount(), legacyAmount);
            detail.setSaleAmount(MathUtil.getValue(saleAmount).setScale(SCALE, RoundingMode.HALF_UP));

            BigDecimal paidAmount = detail.getPaidAmount();
            if (paidAmount != null && paidAmount.compareTo(BigDecimal.ZERO) > 0) {
                paidAmount = paidAmount.setScale(SCALE, RoundingMode.HALF_UP);
                detail.setPaidAmount(paidAmount);
                fixedPaidSum = fixedPaidSum.add(paidAmount);
            } else {
                needPaidAlloc.add(detail);
                saleTotalForAlloc = saleTotalForAlloc.add(detail.getSaleAmount());
            }
        }

        BigDecimal remainingPaid = mainPaidAmount.subtract(fixedPaidSum);
        if (remainingPaid.compareTo(BigDecimal.ZERO) < 0) {
            remainingPaid = BigDecimal.ZERO;
        }

        if (CollUtil.isNotEmpty(needPaidAlloc)) {
            if (saleTotalForAlloc.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal allocated = BigDecimal.ZERO;
                int lastIdx = needPaidAlloc.size() - 1;
                for (int i = 0; i < needPaidAlloc.size(); i++) {
                    SoB2cDetailDTO.ListDTO detail = needPaidAlloc.get(i);
                    BigDecimal paid;
                    if (i == lastIdx) {
                        paid = remainingPaid.subtract(allocated).setScale(SCALE, RoundingMode.HALF_UP);
                    } else {
                        paid = detail.getSaleAmount().multiply(remainingPaid)
                                .divide(saleTotalForAlloc, SCALE, RoundingMode.HALF_UP);
                        allocated = allocated.add(paid);
                    }
                    detail.setPaidAmount(paid);
                }
            } else if (mainAmount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal allocated = BigDecimal.ZERO;
                int lastIdx = needPaidAlloc.size() - 1;
                for (int i = 0; i < needPaidAlloc.size(); i++) {
                    SoB2cDetailDTO.ListDTO detail = needPaidAlloc.get(i);
                    BigDecimal legacyAmount = MathUtil.getValue(detail.getAmount());
                    BigDecimal paid;
                    if (i == lastIdx) {
                        paid = remainingPaid.subtract(allocated).setScale(SCALE, RoundingMode.HALF_UP);
                    } else {
                        paid = legacyAmount.multiply(remainingPaid)
                                .divide(mainAmount, SCALE, RoundingMode.HALF_UP);
                        allocated = allocated.add(paid);
                    }
                    detail.setPaidAmount(paid);
                }
            } else {
                for (SoB2cDetailDTO.ListDTO detail : needPaidAlloc) {
                    detail.setPaidAmount(MathUtil.getValue(detail.getAmount()).setScale(SCALE, RoundingMode.HALF_UP));
                }
            }
        }

        for (SoB2cDetailDTO.ListDTO detail : details) {
            BigDecimal discountAmount = detail.getDiscountAmount();
            if (discountAmount == null) {
                discountAmount = MathUtil.getValue(detail.getSaleAmount()).subtract(MathUtil.getValue(detail.getPaidAmount()));
            }
            detail.setDiscountAmount(discountAmount.setScale(SCALE, RoundingMode.HALF_UP));
        }
    }
}

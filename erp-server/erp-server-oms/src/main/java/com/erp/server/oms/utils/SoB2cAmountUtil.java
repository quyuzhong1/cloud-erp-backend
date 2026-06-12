package com.erp.server.oms.utils;

import cn.hutool.core.collection.CollUtil;
import com.common.core.utils.MathUtil;
import com.erp.model.oms.entity.SoB2cDetailEntity;
import com.erp.model.oms.entity.SoB2cEntity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * B2C 销售订单金额计算工具
 *
 * <p>统一处理：
 * <ul>
 *   <li>主表 paid_amount = amount - total_discount（实付总额）</li>
 *   <li>明细 sale_amount = price × qty（销售金额，折前）</li>
 *   <li>明细 paid_amount = sale_amount / main.amount × main.paid_amount，最后一行吃精度差</li>
 *   <li>明细 discount_amount = sale_amount - paid_amount</li>
 * </ul>
 *
 * <p>调用前置：主表的 amount、total_discount 已经从入参/平台推送/手工录入设置完毕。
 * 调用后明细的 saleAmount/paidAmount/discountAmount 与主表 paidAmount 都会被覆盖写入。
 */
public final class SoB2cAmountUtil {

    private static final int SCALE = 4;

    private SoB2cAmountUtil() {
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
        if (main == null || CollUtil.isEmpty(details)) {
            return;
        }
        BigDecimal mainAmount = MathUtil.getValue(main.getAmount());
        BigDecimal paidTotal = MathUtil.getValue(main.getPaidAmount());

        for (SoB2cDetailEntity d : details) {
            BigDecimal price = MathUtil.getValue(d.getPrice());
            BigDecimal qty = d.getQty() == null ? BigDecimal.ZERO : new BigDecimal(d.getQty());
            d.setSaleAmount(price.multiply(qty).setScale(SCALE, RoundingMode.HALF_UP));
        }

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
        applyMainPaidAmount(main);
        applyDetailAmounts(main, details);
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
}

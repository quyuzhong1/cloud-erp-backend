#!/usr/bin/env python3
"""Apply Excel 39 hardcoded precision changes (4 -> 6) to whitelisted erp-server files only."""

from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]

RULES = {
    "erp-server/erp-server-wms/src/main/java/com/erp/server/wms/service/impl/SoOutstockServiceImpl.java": [
        ("MathUtil.multiplyWithTwo(price, exchangeRate,4)", "MathUtil.multiplyWithTwo(price, exchangeRate,6)"),
        ("MathUtil.divide(allAmountLocalCurrency, BigDecimal.valueOf(actualQty), 4)", "MathUtil.divide(allAmountLocalCurrency, BigDecimal.valueOf(actualQty), 6)"),
        ("MathUtil.multiplyWithTwo(taxPrice, exchangeRate, 4)", "MathUtil.multiplyWithTwo(taxPrice, exchangeRate, 6)"),
    ],
    "erp-server/erp-server-wms/src/main/java/com/erp/server/wms/service/impl/SoReturnNoticeServiceImpl.java": [
        (".setScale(4, RoundingMode.DOWN)\n                .stripTrailingZeros();", ".setScale(6, RoundingMode.DOWN)\n                .stripTrailingZeros();"),
    ],
    "erp-server/erp-server-wms/src/main/java/com/erp/server/wms/rocketmq/sync/impl/SyncB2CSoOutstockServiceImpl.java": [
        (".divide(totalStd , 4 , RoundingMode.DOWN);", ".divide(totalStd , 6 , RoundingMode.DOWN);"),
        ("nowAllAmountLocalCurrency = allAmountLocalCurrency.multiply(positionGoodsCount).divide(sum , 4 , RoundingMode.DOWN);", "nowAllAmountLocalCurrency = allAmountLocalCurrency.multiply(positionGoodsCount).divide(sum , 6 , RoundingMode.DOWN);"),
    ],
    "erp-server/erp-server-oms/src/main/java/com/erp/server/oms/service/impl/SoDetailServiceImpl.java": [
        ("item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate,4));", "item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate,6));"),
        ("item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate,4));", "item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate,6));"),
    ],
    "erp-server/erp-server-oms/src/main/java/com/erp/server/oms/service/impl/SoInfoServiceImpl.java": [
        ("item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate));", "item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate, 6));"),
        ("item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate));", "item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate, 6));"),
        ("result.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate,4));", "result.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate,6));"),
    ],
    "erp-server/erp-server-oms/src/main/java/com/erp/server/oms/service/impl/ExhibitionOrderDetailServiceImpl.java": [
        ("item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate,4));", "item.setPriceLc(MathUtil.multiplyWithTwo(price, exchangeRate,6));"),
        ("item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate,4));", "item.setTaxPriceLc(MathUtil.multiplyWithTwo(taxPrice, exchangeRate,6));"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/InventorySkuCostServiceImpl.java": [
        (",4)", ",6)"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/TmsFirstMileReconciliationServiceImpl.java": [
        ("cost.multiply(rate).setScale(4, RoundingMode.DOWN)", "cost.multiply(rate).setScale(6, RoundingMode.DOWN)"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/FirstMileEstimatedBillServiceImpl.java": [
        ("item.setCostTotal(total.setScale(4, RoundingMode.DOWN));", "item.setCostTotal(total.setScale(6, RoundingMode.DOWN));"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/LogisticsBillCostServiceImpl.java": [
        ("exchangeEstimatedShippingCost).setScale(4, RoundingMode.DOWN)", "exchangeEstimatedShippingCost).setScale(6, RoundingMode.DOWN)"),
        (".multiply(rate).setScale(4 , RoundingMode.DOWN)", ".multiply(rate).setScale(6 , RoundingMode.DOWN)"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/SmallBagCostAllocationServiceImpl.java": [
        (".multiply(refund).setScale(4, RoundingMode.HALF_UP)", ".multiply(refund).setScale(6, RoundingMode.HALF_UP)"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/TransferDeclareServiceImpl.java": [
        (".multiply(rate).setScale(4, RoundingMode.DOWN)", ".multiply(rate).setScale(6, RoundingMode.DOWN)"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/TmsB2cDeclareReconciliationServiceImpl.java": [
        ("data.setTotalCost(totalCost.setScale(4, RoundingMode.DOWN));", "data.setTotalCost(totalCost.setScale(6, RoundingMode.DOWN));"),
        ("actualShippingCost = actualShippingCost.setScale(4, RoundingMode.DOWN);", "actualShippingCost = actualShippingCost.setScale(6, RoundingMode.DOWN);"),
        ("actualDeclareCost = actualDeclareCost.setScale(4, RoundingMode.DOWN);", "actualDeclareCost = actualDeclareCost.setScale(6, RoundingMode.DOWN);"),
        ("actualOtherCost = actualOtherCost.setScale(4, RoundingMode.DOWN);", "actualOtherCost = actualOtherCost.setScale(6, RoundingMode.DOWN);"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/TmsB2cDeclareReconciliationDetailServiceImpl.java": [
        ("unitDgFee.divide(rate , 4 , RoundingMode.HALF_UP)", "unitDgFee.divide(rate , 6 , RoundingMode.HALF_UP)"),
        ("unitXgFee.divide(rate , 4 , RoundingMode.HALF_UP)", "unitXgFee.divide(rate , 6 , RoundingMode.HALF_UP)"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/TmsFirstMileReconciliationDetailServiceImpl.java": [
        ("actualListDTO.setTotalLogisticsCost(actualShippingCostExchange.add(actualDeclareCostExchange).add(actualOtherCostExchange).add(actualOtherTaxCostExchange).setScale(4, RoundingMode.DOWN));", "actualListDTO.setTotalLogisticsCost(actualShippingCostExchange.add(actualDeclareCostExchange).add(actualOtherCostExchange).add(actualOtherTaxCostExchange).setScale(6, RoundingMode.DOWN));"),
        ("estimatedListDTO.setTotalLogisticsCost(estimatedShippingCostExchange.add(estimatedDeclareCostExchange).add(estimatedOtherCostExchange).add(estimatedOtherTaxCostExchange).setScale(4, RoundingMode.DOWN));", "estimatedListDTO.setTotalLogisticsCost(estimatedShippingCostExchange.add(estimatedDeclareCostExchange).add(estimatedOtherCostExchange).add(estimatedOtherTaxCostExchange).setScale(6, RoundingMode.DOWN));"),
        ("diffListDTO.setShippingCost(actualShippingCostExchange.subtract(estimatedShippingCostExchange).setScale(4, RoundingMode.DOWN);", "diffListDTO.setShippingCost(actualShippingCostExchange.subtract(estimatedShippingCostExchange).setScale(6, RoundingMode.DOWN);"),
        ("diffListDTO.setDeclareCost(actualDeclareCostExchange.subtract(estimatedDeclareCostExchange).setScale(4, RoundingMode.DOWN);", "diffListDTO.setDeclareCost(actualDeclareCostExchange.subtract(estimatedDeclareCostExchange).setScale(6, RoundingMode.DOWN);"),
        ("diffListDTO.setOtherCost(actualOtherCostExchange.subtract(estimatedOtherCostExchange).setScale(4, RoundingMode.DOWN);", "diffListDTO.setOtherCost(actualOtherCostExchange.subtract(estimatedOtherCostExchange).setScale(6, RoundingMode.DOWN);"),
        ("diffListDTO.setOtherTaxCost(actualOtherTaxCostExchange.subtract(estimatedOtherTaxCostExchange).setScale(4, RoundingMode.DOWN);", "diffListDTO.setOtherTaxCost(actualOtherTaxCostExchange.subtract(estimatedOtherTaxCostExchange).setScale(6, RoundingMode.DOWN);"),
        ("record.setTotalLogisticsCost(totalCost.setScale(4, RoundingMode.DOWN));", "record.setTotalLogisticsCost(totalCost.setScale(6, RoundingMode.DOWN));"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/LogisticsLargeServiceImpl.java": [
        (".divide(rate, 4, RoundingMode.DOWN)", ".divide(rate, 6, RoundingMode.DOWN)"),
    ],
    "erp-server/erp-server-tms/src/main/java/com/erp/server/tms/service/impl/ShippingCalculationServiceImpl.java": [
        ("MathUtil.setScale(dto.getOtherCost(),4)", "MathUtil.setScale(dto.getOtherCost(),6)"),
        (", 4);", ", 6);"),
    ],
}

FILE_REGEX = {}


def apply_rules():
    changed = []
    for rel_path, pairs in RULES.items():
        path = ROOT / rel_path
        if not path.exists():
            print(f"SKIP missing: {rel_path}")
            continue
        text = path.read_text(encoding="utf-8")
        original = text
        for old, new in pairs:
            if old not in text:
                print(f"WARN pattern not found in {rel_path}: {old[:80]}...")
            text = text.replace(old, new)
        if text != original:
            path.write_text(text, encoding="utf-8", newline="\n")
            changed.append(rel_path)
            print(f"UPDATED {rel_path}")

    for rel_path, patterns in FILE_REGEX.items():
        path = ROOT / rel_path
        if not path.exists():
            print(f"SKIP missing: {rel_path}")
            continue
        text = path.read_text(encoding="utf-8")
        original = text
        for pattern, repl in patterns:
            text = re.sub(pattern, repl, text)
        if text != original:
            path.write_text(text, encoding="utf-8", newline="\n")
            changed.append(rel_path)
            print(f"UPDATED {rel_path}")

    print(f"\nTotal files changed: {len(set(changed))}")


if __name__ == "__main__":
    apply_rules()

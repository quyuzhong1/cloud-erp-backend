package com.erp.server.wms.util;

import cn.hutool.core.text.CharSequenceUtil;
import com.common.core.enums.ApiError;
import com.common.core.exception.ServiceException;
import com.common.core.utils.MathUtil;
import com.common.core.utils.MessageUtils;
import com.erp.model.wms.dto.VirtualInventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryDTO;
import com.erp.model.wms.dto.inventory.InventoryTransactionDTO;
import com.erp.model.wms.dto.inventory.VirtualInventoryStockDTO;
import com.erp.model.wms.enums.inventory.InventoryBusinessTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryRedisOpKeyEnum;
import com.erp.model.wms.enums.inventory.InventorySourceTypeEnum;
import com.erp.model.wms.enums.inventory.InventoryStatusEnum;
import com.erp.server.wms.utils.InventoryRedisUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

/**
 * 实体仓「未分配库存」出库校验白名单与聚合工具。
 * <p>
 * 未分配 = 实体仓(可用+冻结) − 虚拟仓已分配 − 在途预占；仅白名单内单据在出「可用」库存时需校验。
 * Redis TRY 阶段按仓库+SKU 原子预占，最终以 try.lua 为准；Java 预检需扣除 pending。
 * 未分配校验中「实体」取 Redis current 基量（不含 TRY 在途段），在途出库仅通过 {@code reserve} 扣减一次，
 * 避免与 {@code inventory:current} 上 TRY 负向段重复扣减。
 * </p>
 * <p>
 * <b>实体汇总：</b>TRY 参数传入同仓+SKU 下 {@code inventoryId} 列表，由 try.lua 读取
 * {@code inventory:current:{id}} 基量（首段，不含 TRY 在途段）汇总；在途出库由 {@code reserve} 单独扣减。
 * <b>虚拟已分配：</b>虚拟仓 Redis（{@code virtualinventoryredis}）与实体仓 Redis（{@code inventoryredis}）分实例，
 * try.lua 无法在单脚本内直读虚拟 {@code inventory:current:*}；因此 {@code virtualQty} 在 EVAL 前由 Java 单次读取并写入 ARGV，
 * 脚本内与 {@code entityQty}/{@code pending} 于 Phase 1 同阶段校验。Java 读 → EVAL 之间仍有理论窗口，
 * 同仓+SKU 并发出库由 {@code whsku:unalloc:reserve} 预占串行兜底，不以 virtual ARGV 快照作为唯一并发手段。
 * </p>
 */
@Slf4j
public final class InventoryUnallocCheckHelper {

    /** Redis 事务集合中未分配预占成员前缀，commit/rollback 脚本据此识别 */
    public static final String UNALLOC_TRANSACTION_MEMBER_PREFIX = "unalloc@@";

    /** TRY 参数字段中 ID 列表的分隔符（UUID 不含逗号） */
    public static final String INVENTORY_ID_LIST_DELIMITER = ",";

    /** TRY 失败文案中可出库未分配数量的占位符，由 Lua 替换 */
    public static final String UNALLOC_ERROR_ALLOWED_PLACEHOLDER = "ss1ss";

    /** TRY 失败文案中虚拟已分配数量的占位符，由 Lua 替换（与校验用 virtualQty 一致） */
    public static final String UNALLOC_ERROR_VIRTUAL_PLACEHOLDER = "ssvss";

    /** Lua {@code biz_error} 未分配失败前缀，Java 侧映射为 {@link ApiError#VM_CHECK_OUT_VIRTUAL_INVENTORY} */
    public static final String UNALLOC_LUA_ERROR_PREFIX = ApiError.VM_CHECK_OUT_VIRTUAL_INVENTORY.name() + "@@";

    /**
     * 通用库存 Lua 业务失败前缀，须与 try.lua {@code inventory_lua_biz_prefix} 一致；
     * Java 侧映射为 {@link ApiError#WAREHOUSE_INVENTORY_FAILED}。
     */
    public static final String INVENTORY_LUA_BIZ_ERROR_PREFIX = "INVENTORY_LUA_BIZ@@";

    /**
     * try.lua 中 {@code unalloc_lua_error_prefix} 必须与 {@link #UNALLOC_LUA_ERROR_PREFIX} 保持完全一致；
     * 修改 {@link ApiError#VM_CHECK_OUT_VIRTUAL_INVENTORY} 枚举名时须同步改 Lua 脚本。
     */
    public static final String UNALLOC_LUA_ERROR_PREFIX_SYNC_NOTE =
            "Sync with try.lua unalloc_lua_error_prefix = '" + UNALLOC_LUA_ERROR_PREFIX + "'";

    /** PG 路径未分配仓+SKU 锁等待秒数（短等待、快速失败） */
    public static final long UNALLOC_LOCK_WAIT_SECONDS = 5L;

    /** 仓+SKU 分组 key 分隔符（UUID 间拼接，避免 skuId+warehouseId 直接 concat 碰撞） */
    public static final String WAREHOUSE_SKU_GROUP_KEY_DELIMITER = ":";

    private static final List<String> VIRTUAL_CHECK_SOURCE_TYPES = Collections.unmodifiableList(Arrays.asList(
            InventorySourceTypeEnum.OTHER_OUTSTOCK.getCode(),
            InventorySourceTypeEnum.OTHER_INSTOCK.getCode(),
            InventorySourceTypeEnum.PURCHASE_RETURN_ORDER.getCode(),
            InventorySourceTypeEnum.RECEIVE_MATERIAL.getCode(),
            InventorySourceTypeEnum.RETURN_MATERIAL.getCode(),
            InventorySourceTypeEnum.MACHINE_INFO.getCode(),
            InventorySourceTypeEnum.PURCHASE_STOCK_IN.getCode(),
            InventorySourceTypeEnum.SO_RETURN_INSTOCK.getCode(),
            InventorySourceTypeEnum.SO_B2C_DELIVERY_INTERCEPT.getCode()
    ));

    private static final List<String> VIRTUAL_CHECK_BIZ_TYPES = Collections.unmodifiableList(Arrays.asList(
            InventoryBusinessTypeEnum.DIRECT_ALLOCATE.getCode(),
            InventoryBusinessTypeEnum.DIRECT_ALLOCATE_APPLY.getCode()
    ));

    private InventoryUnallocCheckHelper() {
    }

    /**
     * 对 TRY 参数字段做分隔符转义，避免 sku/仓库名含 {@code @@} 导致 Lua 拆段错位。
     *
     * @param value 原始业务字段
     * @return 转义后可安全拼入 ARGV 的字符串
     */
    public static String sanitizeTrySegment(String value) {
        if (CharSequenceUtil.isBlank(value)) {
            return "";
        }
        return value.replace(InventoryRedisUtil.atSign, "__").replace(InventoryRedisUtil.splitSign, "__");
    }

    /**
     * 构建 i18n 错误模板；已分配与可出库数量均由 Lua 用占位符替换，避免 Java 快照与校验值不一致。
     *
     * @param skuNo         SKU 编码
     * @param warehouseName 仓库名称
     * @return 已转义、可传入 try.lua 的错误文案模板
     */
    public static String buildUnallocErrorTemplate(String skuNo, String warehouseName) {
        String msg = MessageUtils.getMessage(ApiError.VM_CHECK_OUT_VIRTUAL_INVENTORY,
                skuNo, warehouseName, UNALLOC_ERROR_VIRTUAL_PLACEHOLDER, UNALLOC_ERROR_ALLOWED_PLACEHOLDER);
        return sanitizeTrySegment(msg);
    }

    /**
     * 判断单条库存交易是否属于「出可用且需未分配校验」白名单。
     *
     * @param transaction 库存交易明细
     * @return true 表示需参与未分配校验/预占
     */
    public static boolean needUnallocCheck(InventoryTransactionDTO transaction) {
        if (transaction == null || transaction.isIgnoreTransaction()) {
            return false;
        }
        return MathUtil.compareTo(transaction.getQty(), MathUtil.ZERO) < MathUtil.ZERO
                && InventoryStatusEnum.USABLE.getCode().equals(transaction.getInventoryStatus())
                && (VIRTUAL_CHECK_SOURCE_TYPES.contains(transaction.getSourceType())
                || VIRTUAL_CHECK_BIZ_TYPES.contains(transaction.getDictBizType()));
    }

    /**
     * 构建仓+SKU 分组 key。
     *
     * @param skuId       SKU ID
     * @param warehouseId 仓库 ID
     * @return 带分隔符的分组 key
     */
    public static String buildWarehouseSkuGroupKey(String skuId, String warehouseId) {
        return CharSequenceUtil.blankToDefault(skuId, "")
                + WAREHOUSE_SKU_GROUP_KEY_DELIMITER
                + CharSequenceUtil.blankToDefault(warehouseId, "");
    }

    /**
     * 从交易列表中筛出需未分配校验的明细。
     *
     * @param transactionList 原始交易列表
     * @return 白名单内的出库可用明细
     */
    public static List<InventoryTransactionDTO> filterNeedUnallocCheck(List<InventoryTransactionDTO> transactionList) {
        if (transactionList == null || transactionList.isEmpty()) {
            return Collections.emptyList();
        }
        return transactionList.stream()
                .filter(InventoryUnallocCheckHelper::needUnallocCheck)
                .collect(Collectors.toList());
    }

    /**
     * UAT {@code checkVirtualInventoryList} 预检 filter 单条判定，与 UAT 线上一致。
     *
     * @param transaction 库存交易明细
     * @return true 表示需参与 UAT 虚拟仓/未分配 Java 预检
     */
    public static boolean needUatVirtualInventoryCheck(InventoryTransactionDTO transaction) {
        if (transaction == null || transaction.isIgnoreTransaction()) {
            return false;
        }
        return MathUtil.compareTo(transaction.getQty(), MathUtil.ZERO) < MathUtil.ZERO
                && (!(transaction.isSameInventoryStatus() && transaction.isSameWarehouse())
                || !InventorySourceTypeEnum.MACHINE_INFO.getCode().equals(transaction.getSourceType()))
                && (InventoryStatusEnum.USABLE.getCode().equals(transaction.getInventoryStatus())
                || InventoryStatusEnum.FROZEN.getCode().equals(transaction.getInventoryStatus()));
    }

    /**
     * UAT {@code checkVirtualInventoryList} 预检 filter，供预检方法与锁范围共用。
     *
     * @param transactionList 原始交易列表
     * @return UAT 预检范围内的明细
     */
    public static List<InventoryTransactionDTO> filterUatVirtualInventoryCheckList(
            List<InventoryTransactionDTO> transactionList) {
        if (transactionList == null || transactionList.isEmpty()) {
            return Collections.emptyList();
        }
        return transactionList.stream()
                .filter(InventoryUnallocCheckHelper::needUatVirtualInventoryCheck)
                .collect(Collectors.toList());
    }

    /**
     * 判断本批交易是否需要获取 UAT 预检对齐的仓+SKU 共享锁。
     *
     * @param transactionList 库存交易列表
     * @return true 表示需加锁
     */
    public static boolean needsUatPreCheckLock(List<InventoryTransactionDTO> transactionList) {
        return !filterUatVirtualInventoryCheckList(transactionList).isEmpty();
    }

    /**
     * 基于 UAT 预检 filter 生成仓+SKU 维度 Redisson 锁 key。
     *
     * @param transactionList 库存交易列表
     * @return 锁 key 列表（去重、字典序）
     */
    public static List<String> buildUatPreCheckLockKeys(List<InventoryTransactionDTO> transactionList) {
        List<InventoryTransactionDTO> lockScopeList = filterUatVirtualInventoryCheckList(transactionList);
        if (lockScopeList.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> keys = new LinkedHashSet<>();
        for (List<InventoryTransactionDTO> group : groupByWarehouseSku(lockScopeList).values()) {
            InventoryTransactionDTO first = group.get(0);
            keys.add(InventoryRedisOpKeyEnum.getWhSkuKey(
                    InventoryRedisOpKeyEnum.WHSKU_UNALLOC_LOCK, first.getWarehouseId(), first.getSkuId()));
        }
        return keys.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 筛出需「实体仓未分配」校验的明细：白名单出库且未指定虚拟仓。
     * <p>指定 {@code virtualWarehouseId} 的明细仅做虚拟仓分配预检，不参与 {@code try.lua} 未分配 TRY。</p>
     *
     * @param transactionList 原始交易列表
     * @return 需实体仓未分配校验的明细
     */
    public static List<InventoryTransactionDTO> filterNeedEntityUnallocCheck(List<InventoryTransactionDTO> transactionList) {
        return filterNeedUnallocCheck(transactionList).stream()
                .filter(t -> CharSequenceUtil.isBlank(t.getVirtualWarehouseId()))
                .collect(Collectors.toList());
    }

    /**
     * 筛出需「指定虚拟仓已分配量」校验的明细。
     *
     * @param transactionList 原始交易列表
     * @return 指定虚拟仓的出库明细
     */
    public static List<InventoryTransactionDTO> filterNeedVirtualWarehouseCheck(List<InventoryTransactionDTO> transactionList) {
        return filterNeedUnallocCheck(transactionList).stream()
                .filter(t -> CharSequenceUtil.isNotBlank(t.getVirtualWarehouseId()))
                .collect(Collectors.toList());
    }

    /**
     * 按仓库+SKU+虚拟仓分组（指定虚拟仓预检用）。
     *
     * @param checkList 指定虚拟仓的校验明细
     * @return 分组后的明细
     */
    public static Map<String, List<InventoryTransactionDTO>> groupByWarehouseSkuVirtualWarehouse(
            List<InventoryTransactionDTO> checkList) {
        if (checkList == null || checkList.isEmpty()) {
            return Collections.emptyMap();
        }
        return checkList.stream().collect(Collectors.groupingBy(t ->
                buildWarehouseSkuGroupKey(t.getSkuId(), t.getWarehouseId())
                        + WAREHOUSE_SKU_GROUP_KEY_DELIMITER
                        + CharSequenceUtil.blankToDefault(t.getVirtualWarehouseId(), "")));
    }

    /**
     * 按仓库+SKU 汇总出库数量（正数）。
     *
     * @param checkList 白名单过滤后的明细
     * @return key=skuId:warehouseId，value=汇总出库量
     */
    public static Map<String, List<InventoryTransactionDTO>> groupByWarehouseSku(List<InventoryTransactionDTO> checkList) {
        if (checkList == null || checkList.isEmpty()) {
            return Collections.emptyMap();
        }
        return checkList.stream().collect(Collectors.groupingBy(
                t -> buildWarehouseSkuGroupKey(t.getSkuId(), t.getWarehouseId())));
    }

    /**
     * 计算分组内出库数量绝对值之和。
     *
     * @param group 同一仓库+SKU 下的明细
     * @return 出库数量（正数）
     */
    public static int sumOutboundQty(List<InventoryTransactionDTO> group) {
        return Math.abs(group.stream().map(InventoryTransactionDTO::getQty).reduce(MathUtil.ZERO, Integer::sum));
    }

    /**
     * 构建仓+SKU 未分配并发锁 Redis key 列表（去重、字典序）。
     * <p>
     * 基于 {@link #filterNeedUnallocCheck} 全量白名单出库明细生成锁 key，含指定虚拟仓出库；
     * 与 {@link #filterNeedEntityUnallocCheck}（仅未分配 TRY）解耦，避免虚拟仓预检无锁并发穿透。
     * </p>
     *
     * @param transactionList 原始或已过滤的交易列表
     * @return 仓+SKU 维度 Redisson 锁 key
     */
    public static List<String> buildUnallocLockKeys(List<InventoryTransactionDTO> transactionList) {
        List<InventoryTransactionDTO> lockScopeList = filterNeedUnallocCheck(transactionList);
        if (lockScopeList.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> keys = new LinkedHashSet<>();
        for (List<InventoryTransactionDTO> group : groupByWarehouseSku(lockScopeList).values()) {
            InventoryTransactionDTO first = group.get(0);
            keys.add(InventoryRedisOpKeyEnum.getWhSkuKey(
                    InventoryRedisOpKeyEnum.WHSKU_UNALLOC_LOCK, first.getWarehouseId(), first.getSkuId()));
        }
        return keys.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 判断本批交易是否需要获取仓+SKU 未分配共享锁（虚拟仓预检与实体未分配预检均适用）。
     *
     * @param transactionList 库存交易列表
     * @return true 表示至少有一条白名单出库需预检
     */
    public static boolean needsUnallocSharedLock(List<InventoryTransactionDTO> transactionList) {
        return !filterNeedUnallocCheck(transactionList).isEmpty();
    }

    /**
     * 虚拟仓库存交易按仓+SKU 构建未分配共享锁 key，与实体出库共用 {@code whsku:unalloc:lock}，
     * 避免分货/调拨与出库预检并发读写虚拟已分配量快照。
     *
     * @param transactionList 虚拟仓库存交易列表（调用方应已剔除 ignore 明细）
     * @return 仓+SKU 维度 Redisson 锁 key（去重、字典序）
     */
    public static List<String> buildUnallocLockKeysForVirtualStock(
            List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList) {
        if (transactionList == null || transactionList.isEmpty()) {
            return Collections.emptyList();
        }
        Set<String> keys = new LinkedHashSet<>();
        for (VirtualInventoryStockDTO.InventoryTransactionDTO transaction : transactionList) {
            if (transaction == null || transaction.isIgnoreTransaction()) {
                continue;
            }
            if (CharSequenceUtil.isBlank(transaction.getWarehouseId())
                    || CharSequenceUtil.isBlank(transaction.getSkuId())) {
                continue;
            }
            keys.add(InventoryRedisOpKeyEnum.getWhSkuKey(
                    InventoryRedisOpKeyEnum.WHSKU_UNALLOC_LOCK,
                    transaction.getWarehouseId(),
                    transaction.getSkuId()));
        }
        return keys.stream().sorted().collect(Collectors.toList());
    }

    /**
     * 判断虚拟仓库存交易是否需要获取仓+SKU 未分配共享锁。
     *
     * @param transactionList 虚拟仓库存交易列表
     * @return true 表示至少有一条有效仓+SKU 明细
     */
    public static boolean needsUnallocSharedLockForVirtualStock(
            List<VirtualInventoryStockDTO.InventoryTransactionDTO> transactionList) {
        return !buildUnallocLockKeysForVirtualStock(transactionList).isEmpty();
    }

    /**
     * 从已排序库存交易列表解析 Redis 事务 ID 的回退流水 ID（取首条非空 id），与预检/tryRedis 统一。
     *
     * @param transactionList 库存交易列表（调用方应已排序）
     * @return 首条流水 ID，无则 null
     */
    public static String resolveFallbackFlowId(List<InventoryTransactionDTO> transactionList) {
        if (transactionList == null || transactionList.isEmpty()) {
            return null;
        }
        return transactionList.stream()
                .map(InventoryTransactionDTO::getId)
                .filter(CharSequenceUtil::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    /**
     * 解析单次 TRY 的操作 ID：同一批流水 ID 排序后拼接，用于区分同一全局事务内的多次合法 TRY。
     * <p>须与 try.lua ARGV[7] 一致；重试同一批时 operationId 不变，可实现幂等。</p>
     *
     * @param transactionList 本次 TRY 的库存流水列表
     * @return 非空 operationId
     */
    public static String resolveTryOperationId(List<InventoryTransactionDTO> transactionList) {
        if (transactionList == null || transactionList.isEmpty()) {
            log.warn("resolveTryOperationId 库存流水列表为空");
            ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "库存流水列表为空");
        }
        return resolveTryOperationIdFromFlowIds(
                transactionList.stream().map(InventoryTransactionDTO::getId).collect(Collectors.toList()));
    }

    /**
     * 由流水 ID 集合解析单次 TRY 的 operationId（排序后逗号拼接）。
     *
     * @param flowIds 流水 ID 列表
     * @return 非空 operationId
     */
    public static String resolveTryOperationIdFromFlowIds(Collection<String> flowIds) {
        if (flowIds == null || flowIds.isEmpty()) {
            log.warn("resolveTryOperationIdFromFlowIds 流水 ID 为空");
            ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "流水ID为空");
        }
        String operationId = flowIds.stream()
                .filter(CharSequenceUtil::isNotBlank)
                .sorted()
                .collect(Collectors.joining(","));
        if (CharSequenceUtil.isBlank(operationId)) {
            log.warn("resolveTryOperationIdFromFlowIds 无法解析 operationId");
            ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "无法解析操作ID");
        }
        return operationId;
    }

    /**
     * 判断 Lua {@code biz_error} 文案是否为未分配校验失败（带 {@link #UNALLOC_LUA_ERROR_PREFIX}）。
     *
     * @param luaErr Lua 原始错误文案
     * @return true 表示未分配业务失败
     */
    public static boolean isUnallocLuaBusinessError(String luaErr) {
        return CharSequenceUtil.isNotBlank(luaErr) && luaErr.startsWith(UNALLOC_LUA_ERROR_PREFIX);
    }

    /**
     * 剥离未分配 Lua 错误前缀，得到可直接展示的业务文案。
     *
     * @param luaErr 带前缀的 Lua 错误文案
     * @return 去掉前缀后的消息
     */
    public static String stripUnallocLuaErrorPrefix(String luaErr) {
        if (!isUnallocLuaBusinessError(luaErr)) {
            return luaErr;
        }
        return luaErr.substring(UNALLOC_LUA_ERROR_PREFIX.length());
    }

    /** try.lua 中 {@code unalloc_lua_error_prefix} 字面量匹配，用于启动/单测校验与 Java 常量一致 */
    private static final Pattern TRY_LUA_UNALLOC_PREFIX_PATTERN =
            Pattern.compile("local\\s+unalloc_lua_error_prefix\\s*=\\s*'([^']+)'");

    /** try.lua 中 {@code inventory_lua_biz_prefix} 字面量匹配 */
    private static final Pattern TRY_LUA_INVENTORY_BIZ_PREFIX_PATTERN =
            Pattern.compile("local\\s+inventory_lua_biz_prefix\\s*=\\s*'([^']+)'");

    /**
     * 判断 Lua {@code biz_error} 文案是否为通用库存业务失败（带 {@link #INVENTORY_LUA_BIZ_ERROR_PREFIX}）。
     *
     * @param luaErr Lua 原始错误文案
     * @return true 表示通用库存 Lua 业务失败
     */
    public static boolean isInventoryLuaBusinessError(String luaErr) {
        return CharSequenceUtil.isNotBlank(luaErr) && luaErr.startsWith(INVENTORY_LUA_BIZ_ERROR_PREFIX);
    }

    /**
     * 剥离通用库存 Lua 错误前缀，得到可直接展示的业务文案。
     *
     * @param luaErr 带前缀的 Lua 错误文案
     * @return 去掉前缀后的消息
     */
    public static String stripInventoryLuaBusinessErrorPrefix(String luaErr) {
        if (!isInventoryLuaBusinessError(luaErr)) {
            return luaErr;
        }
        return luaErr.substring(INVENTORY_LUA_BIZ_ERROR_PREFIX.length());
    }

    /**
     * 校验 classpath 下 {@code lua/try.lua} 的 {@code unalloc_lua_error_prefix} 与
     * {@link #UNALLOC_LUA_ERROR_PREFIX} 一致，防止 Lua/Java 双端维护漂移。
     *
     * @throws IllegalStateException 脚本缺失、未定义前缀或与 Java 常量不一致
     */
    public static void assertTryLuaUnallocPrefixSynced() {
        assertTryLuaPrefixSynced(TRY_LUA_UNALLOC_PREFIX_PATTERN, "unalloc_lua_error_prefix", UNALLOC_LUA_ERROR_PREFIX);
    }

    /**
     * 校验 try.lua {@code inventory_lua_biz_prefix} 与 {@link #INVENTORY_LUA_BIZ_ERROR_PREFIX} 一致。
     *
     * @throws IllegalStateException 脚本缺失、未定义前缀或与 Java 常量不一致
     */
    public static void assertTryLuaInventoryBizPrefixSynced() {
        assertTryLuaPrefixSynced(TRY_LUA_INVENTORY_BIZ_PREFIX_PATTERN, "inventory_lua_biz_prefix",
                INVENTORY_LUA_BIZ_ERROR_PREFIX);
    }

    /**
     * 读取 try.lua 并校验指定前缀常量与 Java 一致。
     *
     * @param pattern     正则
     * @param luaVarName  Lua 变量名（日志用）
     * @param javaPrefix  Java 期望前缀
     */
    private static void assertTryLuaPrefixSynced(Pattern pattern, String luaVarName, String javaPrefix) {
        try (InputStream inputStream = InventoryUnallocCheckHelper.class.getClassLoader()
                .getResourceAsStream("lua/try.lua")) {
            if (inputStream == null) {
                throw new IllegalStateException("classpath 下未找到 lua/try.lua");
            }
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int read;
            while ((read = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, read);
            }
            String content = buffer.toString(StandardCharsets.UTF_8.name());
            Matcher matcher = pattern.matcher(content);
            if (!matcher.find()) {
                throw new IllegalStateException("try.lua 中未找到 " + luaVarName + " 定义");
            }
            String luaPrefix = matcher.group(1);
            if (!javaPrefix.equals(luaPrefix)) {
                throw new IllegalStateException(CharSequenceUtil.format(
                        "try.lua {}={} 与 Java {}={} 不一致",
                        luaVarName, luaPrefix, luaVarName, javaPrefix));
            }
        } catch (IOException e) {
            throw new IllegalStateException("读取 try.lua 失败", e);
        }
    }

    /**
     * 校验存在未分配 TRY 时必须有非零仓位库存 TRY 参数，避免仅写 reserve 不扣 {@code inventory:current}。
     *
     * @param unallocTryItems       未分配预占项
     * @param transactionRedisParam 仓位库存 TRY 参数（按 inventoryId 聚合后非零条目）
     */
    public static void assertUnallocTryRequiresInventoryParams(
            List<UnallocTryItem> unallocTryItems, List<String> transactionRedisParam) {
        if (unallocTryItems == null || unallocTryItems.isEmpty()) {
            return;
        }
        if (transactionRedisParam == null || transactionRedisParam.isEmpty()) {
            log.warn("未分配TRY参数存在但仓位库存TRY为空 items={}", unallocTryItems.size());
            ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "未分配预占缺少仓位库存TRY参数");
        }
    }

    /**
     * 校验未分配 TRY 项在 virtualQty&gt;0 时必须携带实体 inventoryId 列表，避免 Lua 静默跳过校验。
     *
     * @param unallocTryItems 未分配预占项
     */
    public static void assertUnallocTryItemsHaveInventoryIds(List<UnallocTryItem> unallocTryItems) {
        if (unallocTryItems == null || unallocTryItems.isEmpty()) {
            return;
        }
        for (UnallocTryItem item : unallocTryItems) {
            if (item.getVirtualQty() > 0 && item.getInventoryIds().isEmpty()) {
                log.warn("未分配TRY缺少实体inventoryId wh={}, sku={}, virtualQty={}",
                        item.getWarehouseId(), item.getSkuId(), item.getVirtualQty());
                ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "未分配预占缺少实体库存ID");
            }
        }
    }

    /**
     * 从 Redis 虚拟库存查询结果汇总同仓+SKU 已分配数量。
     *
     * @param virtualInventoryList {@link VirtualInventoryDTO.RedisVirtualInventoryReturnDTO} 列表
     * @param warehouseId          仓库 ID
     * @param skuId                SKU ID
     * @return 虚拟已分配汇总数量
     */
    public static int sumVirtualQty(List<VirtualInventoryDTO.RedisVirtualInventoryReturnDTO> virtualInventoryList,
                                    String warehouseId, String skuId) {
        if (virtualInventoryList == null || virtualInventoryList.isEmpty()) {
            return 0;
        }
        return virtualInventoryList.stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), warehouseId)
                        && CharSequenceUtil.equals(obj.getSkuId(), skuId))
                .map(VirtualInventoryDTO.RedisVirtualInventoryReturnDTO::getQty)
                .reduce(MathUtil.ZERO, Integer::sum);
    }

    /**
     * 筛选同仓+SKU 下实体库存 inventoryId（去重、保序）。
     *
     * @param entityInventoryList Redis 实体库存查询结果
     * @param warehouseId         仓库 ID
     * @param skuId               SKU ID
     * @return 可用于未分配基量汇总的 inventoryId 列表
     */
    public static List<String> filterInventoryIdsByWarehouseSku(
            List<InventoryDTO.RedisInventoryReturnDTO> entityInventoryList, String warehouseId, String skuId) {
        if (entityInventoryList == null || entityInventoryList.isEmpty()) {
            return Collections.emptyList();
        }
        return entityInventoryList.stream()
                .filter(obj -> CharSequenceUtil.equals(obj.getWarehouseId(), warehouseId)
                        && CharSequenceUtil.equals(obj.getSkuId(), skuId))
                .map(InventoryDTO.RedisInventoryReturnDTO::getInventoryId)
                .filter(CharSequenceUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    /**
     * 汇总 inventoryId 列表在 Redis 上的实体基量（current 首段，不含 TRY 在途段）。
     * <p>未分配 = 基量 − virtual − pending；在途出库只扣 pending 一次。</p>
     *
     * @param inventoryIds   实体库存 ID 列表
     * @param baseQtyLoader  按 inventoryId 读取基量的函数
     * @return 实体基量之和
     */
    public static int sumEntityBaseQty(List<String> inventoryIds, ToIntFunction<String> baseQtyLoader) {
        if (inventoryIds == null || inventoryIds.isEmpty() || baseQtyLoader == null) {
            return 0;
        }
        return inventoryIds.stream().mapToInt(baseQtyLoader).sum();
    }

    /**
     * 从已批量加载的基量映射汇总实体库存（避免预检逐条 Redis GET）。
     *
     * @param inventoryIds 实体库存 ID 列表
     * @param baseQtyMap   inventoryId → 基量
     * @return 实体基量之和
     */
    public static int sumEntityBaseQtyFromMap(List<String> inventoryIds, Map<String, Integer> baseQtyMap) {
        if (inventoryIds == null || inventoryIds.isEmpty() || baseQtyMap == null || baseQtyMap.isEmpty()) {
            return 0;
        }
        return inventoryIds.stream().mapToInt(id -> baseQtyMap.getOrDefault(id, 0)).sum();
    }

    /**
     * 解析未分配预占 Redis 值，汇总在途预占量。
     * <p>
     * 仅当 {@code excludeTransactionId} 与 {@code excludeOperationId} 同时非空时，才排除完全匹配的预占片段
     * （用于同批 TRY 重试幂等）；预检场景应两者均传 {@code null}，以计入同事务内其它批次的预占。
     * </p>
     *
     * @param reserveValue           Redis 中 {@code whsku:unalloc:reserve} 的原始字符串
     * @param excludeTransactionId   排除的事务 ID，可为空
     * @param excludeOperationId     排除的操作 ID，可为空
     * @return 在途预占出库数量之和
     */
    public static int sumPendingReserve(String reserveValue, String excludeTransactionId, String excludeOperationId) {
        if (CharSequenceUtil.isBlank(reserveValue)) {
            return 0;
        }
        String[] segments = reserveValue.split(InventoryRedisUtil.splitSign);
        int pending = 0;
        for (int i = 1; i < segments.length; i++) {
            String[] parts = segments[i].split(InventoryRedisUtil.atSign);
            if (parts.length >= 2) {
                String txn = parts[0];
                String opId = parts.length >= 3 ? parts[1] : "";
                if (CharSequenceUtil.isNotBlank(excludeTransactionId)
                        && CharSequenceUtil.isNotBlank(excludeOperationId)
                        && CharSequenceUtil.equals(txn, excludeTransactionId)
                        && CharSequenceUtil.equals(opId, excludeOperationId)) {
                    continue;
                }
                int qty = parts.length >= 3
                        ? parseReserveQtySegment(parts[2])
                        : parseReserveQtySegment(parts[1]);
                pending += qty;
            }
        }
        return pending;
    }

    /**
     * 查询当前事务是否已在预占 key 中登记及其数量。
     *
     * @param reserveValue  Redis 预占原始值
     * @param transactionId 当前事务 ID
     * @return 已预占数量；未登记则返回 {@code null}
     */
    public static Integer findReservedQtyForTransaction(String reserveValue, String transactionId) {
        if (CharSequenceUtil.isBlank(reserveValue) || CharSequenceUtil.isBlank(transactionId)) {
            return null;
        }
        String[] segments = reserveValue.split(InventoryRedisUtil.splitSign);
        for (int i = 1; i < segments.length; i++) {
            String[] parts = segments[i].split(InventoryRedisUtil.atSign);
            if (parts.length >= 2 && CharSequenceUtil.equals(parts[0], transactionId)) {
                return parts.length >= 3
                        ? parseReserveQtySegment(parts[2])
                        : parseReserveQtySegment(parts[1]);
            }
        }
        return null;
    }

    /**
     * 解析预占片段中的数量；非法或负数时 fail-closed，避免脏数据被当作 0 高估可出库量。
     *
     * @param qtySegment Redis 预占片段中的数量字符串
     * @return 解析后的非负数量
     */
    private static int parseReserveQtySegment(String qtySegment) {
        if (CharSequenceUtil.isBlank(qtySegment)) {
            log.warn("未分配预占 Redis 片段数量为空");
            ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "未分配预占数据异常");
        }
        try {
            int qty = Integer.parseInt(qtySegment.trim());
            if (qty < 0) {
                log.warn("未分配预占 Redis 片段数量为负 qty={}", qtySegment);
                ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "未分配预占数量异常");
            }
            return qty;
        } catch (NumberFormatException e) {
            log.warn("未分配预占 Redis 片段数量解析失败 qty={}", qtySegment);
            ServiceException.runError(ApiError.WAREHOUSE_INVENTORY_FAILED, "未分配预占数据解析失败");
        }
        throw new ServiceException(ApiError.WAREHOUSE_INVENTORY_FAILED, "未分配预占数据解析失败");
    }

    /**
     * 拼装 TRY 阶段未分配预占参数，供 try.lua ARGV[6] 使用。
     * <p>
     * 格式：warehouseId@@skuId@@outboundQty@@reserveKey@@entityKey@@virtualKey@@inventoryIds@@virtualQty@@errorTemplate。
     * {@code virtualQty} 为 EVAL 前 Java 单次读取写入，脚本内与 entity/pending 同阶段校验；镜像仅诊断用途。
     * </p>
     *
     * @param warehouseId    仓库 ID
     * @param skuId          SKU ID
     * @param outboundQty    出库数量（正数）
     * @param reserveKey     未分配预占 Redis key
     * @param entityKey      实体汇总镜像 Redis key
     * @param virtualKey     虚拟已分配诊断镜像 Redis key
     * @param inventoryIds   同仓+SKU 下可用/冻结库存的 inventoryId 列表（逗号拼接）
     * @param virtualQty     虚拟已分配数量（EVAL 前 Java 读取）
     * @param errorTemplate  i18n 错误模板（含 ss1ss/ssvss 占位符）
     * @return 单条未分配 TRY 参数字符串
     */
    public static String buildTryUnallocParam(String warehouseId, String skuId, int outboundQty,
                                              String reserveKey, String entityKey, String virtualKey,
                                              String inventoryIds, int virtualQty, String errorTemplate) {
        return String.join(InventoryRedisUtil.atSign,
                warehouseId,
                skuId,
                String.valueOf(Math.abs(outboundQty)),
                reserveKey,
                entityKey,
                virtualKey,
                CharSequenceUtil.blankToDefault(inventoryIds, ""),
                String.valueOf(virtualQty),
                errorTemplate);
    }

    /**
     * 未分配 TRY 聚合项：同一仓库+SKU 一条 Lua 预占参数。
     */
    @Getter
    public static final class UnallocTryItem {
        private final String warehouseId;
        private final String skuId;
        private final String skuNo;
        private final String warehouseName;
        private final int outboundQty;
        private final int virtualQty;
        private final List<String> inventoryIds;
        private final String reserveKey;
        private final String virtualKey;
        private final String entityKey;
        private final String errorTemplate;

        /**
         * @param warehouseId   仓库 ID
         * @param skuId         SKU ID
         * @param skuNo         SKU 编码
         * @param warehouseName 仓库名称
         * @param outboundQty   出库数量（正数）
         * @param virtualQty    虚拟已分配数量
         * @param inventoryIds  实体即时库存 ID 列表
         */
        public UnallocTryItem(String warehouseId, String skuId, String skuNo, String warehouseName,
                              int outboundQty, int virtualQty, List<String> inventoryIds) {
            this.warehouseId = warehouseId;
            this.skuId = skuId;
            this.skuNo = skuNo;
            this.warehouseName = warehouseName;
            this.outboundQty = outboundQty;
            this.virtualQty = virtualQty;
            this.inventoryIds = inventoryIds == null ? Collections.emptyList() : inventoryIds;
            this.reserveKey = InventoryRedisOpKeyEnum.getWhSkuKey(
                    InventoryRedisOpKeyEnum.WHSKU_UNALLOC_RESERVE, warehouseId, skuId);
            this.virtualKey = InventoryRedisOpKeyEnum.getWhSkuKey(
                    InventoryRedisOpKeyEnum.WHSKU_VIRTUAL, warehouseId, skuId);
            this.entityKey = InventoryRedisOpKeyEnum.getWhSkuKey(
                    InventoryRedisOpKeyEnum.WHSKU_ENTITY, warehouseId, skuId);
            this.errorTemplate = buildUnallocErrorTemplate(skuNo, warehouseName);
        }

        /**
         * 转为 try.lua 单条未分配参数字符串。
         *
         * @return ARGV[6] 中单条记录
         */
        public String toTryParam() {
            String inventoryIdParam = inventoryIds.stream()
                    .filter(CharSequenceUtil::isNotBlank)
                    .collect(Collectors.joining(INVENTORY_ID_LIST_DELIMITER));
            return buildTryUnallocParam(warehouseId, skuId, outboundQty,
                    reserveKey, entityKey, virtualKey, inventoryIdParam, virtualQty, errorTemplate);
        }
    }
}
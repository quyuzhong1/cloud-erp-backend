# 爱亚海外仓对接（临时工作文档）

> **用途**：供 `feature/20260714-aiya` 分支多会话对接使用。对接完成后可删除本目录，不必长期保留在主干。
>
> **来源**：`E:\Downloads\爱亚海外仓对接方案文档`（导入日期：2026-07-15）

## 目录说明

| 文件/目录 | 说明 |
|-----------|------|
| `爱亚海外仓对接方案文档.md` | 产品对接方案正文（含截图引用） |
| `图片和附件/` | 方案文档中的图片资源 |
| `README.md` | 本说明 + 跨会话待确认事项 |

## 使用约定

1. **以本目录文档为对接依据**；若产品侧更新文档，用新版覆盖同名文件，并在下方「文档变更记录」记一笔。
2. **对接中与产品确认的问题**记入「待产品确认」；确认后把结论写进「已确认结论」，避免多会话重复问。
3. 本目录为**临时资料**，默认不要求合入主分支；是否提交由当前分支作者自行决定。

## 文档变更记录

| 日期 | 说明 |
|------|------|
| 2026-07-15 | 从 Downloads 首次导入方案文档 + 图片附件 |
| 2026-07-15 | 完成「商品数据查询/SKU」对接开发：`AiyaSkuQueryDTO` / `AiyaOpenApiService.querySku` / `AiyaSkuStatusEnum` / `AiyaSkuInitHandler` / `AiyaSkuOmsSyncDmpHandler`，参照 WEGO SKU 链路实现，过程中发现的骨架代码与文档不一致项见下方「待产品确认」 |
| 2026-07-16 | 完成「库存数据」对接开发/修正：新增 `AiyaInventoryQueryDTO`，重写 `AiyaOpenApiService.queryInventory`（强类型 DTO，请求字段名 `pageNum`→`page` 修正）、`AiyaInventoryInitHandler`（单独解析 `inventoryVOList`，不再复用为 WEGO 分页结构设计的 `extractPageResult`），新增 Output 侧 `AiyaInventoryRocketMQTaskHandler`（对齐 `WegoInventoryRocketMQTaskHandler`，补齐推送至 WMS `overseas_inventory` 的缺失链路）。之前会话遗留的 `AiyaInventoryInitHandler` 是照抄 WEGO 结构、未对齐爱亚库存接口真实响应结构（`{Code,message,success,inventoryVOList}` vs WEGO 的 `{success,result:{list,pages,emptyFlag}}`），本次已修正，过程中发现的问题见下方「待产品确认」 |
| 2026-07-16 | 用用户提供的爱亚开放平台接口文档页面截图（比翻译稿更权威）核对「库存数据」接口，发现并修正两处出入：① 请求参数里**没有** `status` 字段（翻译稿里有，疑似跟 SKU 查询接口混淆），已从 `AiyaInventoryQueryDTO`/`AiyaOpenApiService.queryInventory` 移除；② 截图多出一个翻译稿没提到的可选字段 `domainCode`（未给出参数描述），已补充到 DTO/SDK 方法，默认不传。同时确认响应 `inventoryVOList` 明细比翻译稿多出 `customerCode`/`barcode`/`skuStatus` 三个字段（无需改代码即可透传，已更新相关 Javadoc），其中 `skuStatus` 可能带来 DMP 去重风险，详见下方「待产品确认」新增项 |
| 2026-07-16 | 梳理「6.2.2 商品映射」章节，对照现有公共 SKU 映射代码（`ListingInfoServiceImpl.syncWarehouseNotMatchSku`/`SkuMappingEntity`）：新增/更新（规则a/e）已复用平台无关的通用能力，未新增公共代码；但删除/禁用（规则b/c/d）目前完全未实现，且涉及是否要改动公共代码（影响WEGO等其它海外仓）的范围问题，见下方「商品映射相关」新增待确认项。文档一度从项目目录消失（只剩README），已由用户从原始下载文件重新恢复，内容比对与之前分析版本逐字节一致 |
| 2026-07-16 | 修复 `querySkuTest` 联调报错 `INVALID_DATA: Created time and Updated time and SKUs cannot be both empty`：确认方案文档「商品注册/查询」请求字段列表（`status`/`pageSize`/`page`/`customerCode`）不完整，爱亚网关底层 QERP Open API Platform `GLINK_QUERY_ITEM_NOTIFY` 真实规范要求 `skus`/`createdTime`范围/`updatedTime`范围三者至少一组非空。已给 `AiyaSkuQueryDTO`/`AiyaOpenApiService.querySku` 补充 `createdTimeFrom`/`createdTimeTo`，`AiyaSkuInitHandler` 固定传「开发起始日期 2026-07-14 00:00:00 ~ 当前时间」以满足全量拉取诉求，`AiyaOpenApiServiceManualTest.querySkuTest` 同步更新。残留风险见下方「待产品确认」新增项 |
| 2026-07-16 | 已与产品/业务确认：爱亚账号/SKU 不存在早于 2026-07-14 的创建记录，`AiyaSkuInitHandler.DEV_START_TIME` 固定锚点不会漏拉历史 SKU，相关「待产品确认」项已移至「已确认结论」，同步更新代码注释 |
| 2026-07-16 | 实测确认库存查询接口 `stockStatus` 参数**非必传，且最好不传**（不传即查询全部状态库存），此前的猜测（可能需按 GOOD/DAMAGE 分两次查）已被推翻。已从 `AiyaInventoryInitHandler` 移除 `STOCK_STATUS_PLACEHOLDER` 占位逻辑（不再传该参数），同步更新 `AiyaInventoryQueryDTO`/`AiyaOpenApiService.queryInventory`/`AiyaOpenApiServiceManualTest` 的注释，相关「待产品确认」项移至「已确认结论」 |
| 2026-07-21 | 测试环境执行爱亚库存定时/手动任务报「输入信息数据代码【inventory】没有符合条件的明细任务」：已定位为 DMP 配置层问题（`DmpInputHotfixCreateHandler`/`DmpInputDetailCreateHandler`），不是 SDK/InitHandler 代码问题。根因：服务商重新授权后旧 `dmp_cfg_input_detail` 大量软删，且明细 `next_level_id` 曾指向已作废的旧 `overseas_provider`。当前库内已有可用 normal 明细（`next_level_id=2079487193754226690`），并已跑出 finish 任务；但 `dmp_cfg_input_convert_mapping`（爱亚库存字段映射）仍为空、`dmp_cfg_output`（`AiyaInventoryRocketMQTaskHandler`）仍未配置，导致 `dmp_third_inventory` 落库字段为空且无法推送到 WMS |
| 2026-07-16 | 用户提供真实联调响应样例（不传 `stockStatus` 时的查询结果），核对后发现并修复一个真实 bug：`AiyaInventoryInitHandler.extractInventoryList` 里失败分支读取的是 `response.get("Code")`（大写C），但真实响应顶层字段其实是小写 `code`（跟 `AbstractAiyaInitHandler`/`AiyaSkuInitHandler`/`AiyaWarehouseInitHandler` 里其它接口一致），大写写法会导致失败时异常信息里的错误码永远打印成 `null`。已修正为 `response.get("code")`，同步更新 `AiyaOpenApiService.queryInventory`/`AiyaInventoryInitHandler` 里写错的 `{Code,...}` 结构说明为 `{code,...}`。此外真实样例其它字段（`success`/`inventoryVOList`/明细 11 个字段、`skuStatus="GOOD"`）均与此前按接口文档截图确认的清单一致，无其它出入；样例仍只有 3 个顶层字段，未出现 `total`/`pages` |
| 2026-07-16 | 用户提供 `querySku` 真实联调响应样例（2条测试SKU：test1602/test2717），核对后有 1 项新发现 + 1 处真实 bug 修复：① **响应顶层实际带 `total` 字段**（`{"total":2,"code":"SUCCESS","success":true,"itemList":[...]}`），此前「分页翻页终止条件」待确认项已解决，`AiyaSkuInitHandler` 已改为"累计拉取条数达到 `total`"与"本页条数<pageSize"任一满足即停止翻页，`total`缺失时自动退化为纯size判断；② `status` 字段实测确认真实取值为 `"Active"`（大小写与文档一致），`Inactive` 暂未见真实样例但 `needSync` 用 `equalsIgnoreCase` 兼容，不受影响；③ **真实bug**：`AiyaSkuOmsSyncDmpHandler.parseBarcodeList` 之前读取顶层 `barcode` 字段，但真实响应条码字段是顶层 `barcodeList` 数组（元素为 `{unit,barcode}` 对象，与 `packagingList` 同级、非嵌套关系），导致条码同步到 OMS 一直是空列表，已修正为解析 `barcodeList`；④ 顺带发现两条测试SKU响应里完全没有 `name` 键（只有 `description`），`AiyaSkuOmsSyncDmpHandler` 的 name 映射已改为"`name` 为空则回退用 `description`"，避免可选字段 `name` 未设置时未匹配表里名称长期为空 |
| 2026-07-17 | 完成「商品映射」规则 b/c/d（全量快照回收）开发：`SkuMappingEntity`/`WarehousePagingViewDTO` 新增 `status` 字段 + `SkuMappingStatusEnum`（启用/禁用，与 `isExpire` 是两个维度，不复用旧字段）；`WegoSkuSyncDTO.SkuItemDTO` 新增可选 `status`（源端原始状态）；`ListingInfoService` 新增平台无关的 `reconcileWarehouseSkuSnapshot`（空快照防呆 + 规则b删除 + 规则c/d禁用），配套 Feign 契约 `OmsListingInfoFeign#reconcileWarehouseSkuSnapshot`；`AiyaSkuOmsSyncDmpHandler` 改为除最后一批走原 `syncWarehouseNotMatchSku`（a/e），最后一批携带全量 skuItems 调新方法触发回收；并在 `SkuMappingServiceImpl` 一批"解析当前可用映射供业务使用"的方法（`getByAttribute`/`listBySkuNoList`/`listByInfo`/`listByPlatformSkuNoAndPlatform`/`listStockSkuNoByProductSkuIds`/`listByErpSkuIdAndType`/`listByWarehouseAndPlatformSku`/`listSkuMappingByParams`）追加 `status=enable` 过滤，覆盖头程发货单、海外仓入库单选库存SKU、B2C销售订单推三方仓出库单选库存SKU等链路；管理页面（`warehousePaging`/`listWarehouseExport`/`paging`/导出等）不过滤，仅新增 `status`/`statusName` 展示列 + 可选筛选参数。本轮只对爱亚生效，`listByListingIds`（历史遗留 warehouseId 回填、Excel 导入比对等通用工具方法，含义偏"全部记录"而非"仅可用记录"）保持不加过滤，避免误伤 Excel 导入等未验证场景 |
| 2026-07-17 | 代码审查复核 3 项问题：① 末批 `reconcileWarehouseSkuSnapshot` 请求体重新变为全量（架构取舍，已知晓暂不处理，见「待产品确认」新增项）；② 新增/改写的两处批次进度日志（`AiyaSkuOmsSyncDmpHandler` 原 160-161/177-180 行）此前误用 `log.info`，违反仓库 `java-log-min-warn.mdc` 规则，已改为 `log.warn`；③ `sku_mapping.status` 列 DDL 未纳入仓库迁移机制，属已知且已文档化的发布检查项，不涉及代码改动。同时把本次新增代码里引用外部方案文档规则编号（如"规则b"/"规则a/e"）的 Java 注释改写为直接描述行为，避免依赖不会保留在仓库里的外部文档 |
| 2026-07-17 | 查生产环境确认海外仓单服务商 SKU 峰值约2400条（艾姆勒iml），量级远低于万级，此前"末批请求体变大"问题按暂不处理结论保留。当天晚些时候用户重新判断：规则b（未映射且源端消失→删除）、规则c（已映射且源端消失→禁用）依赖的"快照中找不到SKU"场景在真实三方仓接口里不会发生（三方仓通常只会把SKU状态改成停用/作废，不会让SKU整条从拉取结果消失），因此**移除b/c的实现，只保留规则d**（已映射且源端状态非启用→禁用）。连带效果：`reconcileWarehouseSkuSnapshot` 不再需要"完整快照"做消失比对，`AiyaSkuOmsSyncDmpHandler` 因此简化为每批都独立调用同一方法（不再区分最后一批），恢复了原有 `SYNC_BATCH_SIZE=500` 分批保护，之前「末批请求体变大」的审查问题随架构简化一并解决。涉及改动：`WegoSkuSyncDTO.ReconcileResultDTO` 移除 `deletedCount`；`ListingInfoServiceImpl.reconcileWarehouseSkuSnapshot` 去掉"未映射消失删除"和"已映射消失禁用"两段逻辑，只保留"已映射+本批状态非active→禁用"；`ListingInfoService`/`OmsListingInfoFeign` Javadoc 同步更新 |
| 2026-07-17 | 规则b/c移除后，`reconcileWarehouseSkuSnapshot` 内部实现已经很薄（只是"调一次 `syncWarehouseNotMatchSku` + 加一段禁用判断"），用户提出"减少不必要的Feign调用，避免触发大数据量审查"，因此**把 `reconcileWarehouseSkuSnapshot` 的禁用逻辑直接合并进 `syncWarehouseNotMatchSku`，删除 `reconcileWarehouseSkuSnapshot` 方法/Feign接口/Controller端点**，两条链路（爱亚、WEGO）统一只调用一个方法。为了让调用方能拿到"禁用了多少条"，`syncWarehouseNotMatchSku` 返回值从 `Integer` 改为 `WegoSkuSyncDTO.ReconcileResultDTO`（`addedCount`+`disabledCount`），同步改了三处：`OmsListingInfoFeign`/`ListingInfoFeignController`/`ListingInfoService`+`ListingInfoServiceImpl`。**WEGO 链路同步适配**：`WegoSkuOmsSyncDmpHandler` 原来直接用 `Integer` 返回值，改为读取 `result.getAddedCount()`；因为 WEGO 从不传 `SkuItemDTO.status`，禁用分支的 `inactiveSkuNoSet` 判断条件（`StringUtils.isNotBlank(item.getStatus())`）恒为 false，禁用分支不会被触发，WEGO 现有行为不受影响。顺带把该处改动到的批次日志（`WegoSkuOmsSyncDmpHandler` 原 139 行）按 `java-log-min-warn.mdc` 规则由 `log.info` 改为 `log.warn`。 |
| 2026-07-17 | 用户对上一轮合并后的代码做了一次审查，反馈 3 项问题，逐一核实均真实存在并修复：① `ApiErrorDmp.MAPPING_WAREHOUSE_SKU_SNAPSHOT_EXCEED_LIMIT`（7513）是此前为"服务商单次SKU超过一万条报错"预留但从未真正接入判断逻辑的死代码，用户确认当前~2400条峰值下没必要实现该限制，**已直接删除该错误码**（常量定义+`values()`引用）；② `ListingInfoServiceImpl.syncWarehouseNotMatchSku` 里判断源端状态时硬编码了字符串 `"active"`，**已提取为类内 `private static final String STATUS_ACTIVE`** 常量，并在 Javadoc 说明这是 OMS 侧平台无关的约定值，不依赖 `erp-sdk-wms-aiya` 的 `AiyaSkuStatusEnum`；③ 禁用回收部分用 `listByAuthIds(authId)`（无任何过滤条件，拉取该服务商全部 listing 记录）后在 Java 侧过滤4个条件，**已改为复用方法开头已在用的 `listByAuth(type, platformSkuNoList, authIdList)`**，把 `inactiveSkuNoSet` 作为 `platformSkuNo IN` 条件下推到 SQL，查询范围从"该服务商全部listing"收窄为"本批状态非active的SKU对应的listing"（`sourceType`/`matchResult` 因 `listByAuth` 不支持这两个参数，继续在 Java 侧过滤）。三处均为内部实现细节调整，不改变 Feign 契约/Service 接口签名/日志文案 |
| 2026-07-20 | 补齐「人工重新启用」后端入口：`SkuMappingDTO.UpdateStatusDTO` + `SkuMappingService#updateStatus` + `POST /skuMaping/updateStatus`，仅库存SKU（`WAREHOUSE`）可批量改 `status`，写操作日志，返回 `BatchResultDTO`；相关待确认项已移至「已确认结论」 |
| 2026-07-20 | 启动「尾程-出库单对接」（文档 6.3.3 节），先做 AIYA 文档 vs 现有 WEGO 出库单实现的差异分析（未写代码）。核心发现见「尾程-出库单相关」待确认项 |
| 2026-07-21 | 依据用户提供的爱亚开放平台「创建/修改出库单」接口文档截图（图1-4 顶层、图5 `shippingInstructions`、图6 `shipTo`、图7 实际为顶层后续可选字段而非 items 子字段、图8 `shipFrom`）收敛建单报文：新增 `AiyaOutboundSaveDTO`；`AiyaOpenApiService.save2cOrder` 改为接强类型 DTO；完善 `AiyaOpenApiServiceManualTest.save2cOrderTest`。字段收敛原则：官方必填全留 + 方案文档有映射的可选字段保留；官方非必填且方案未映射的（udf*/代收货款/保价/托盘等）一律不进 DTO。关键纠正见下方「已确认结论」 |

## 待产品确认

（对接过程中往这里追加，格式示例）

- [ ] 问题描述……（会话/日期）

### SKU 查询相关（2026-07-15）

- [ ] **status 字段是否还有其它取值**：真实样例已确认 `Active` 大小写与文档一致（见「已确认结论」），但只见过 `Active`，`Inactive` 及是否存在其它状态值（比如草稿态）仍未见真实样例，需要一条已停用SKU的真实响应核对。
- [ ] **DMP 任务调度配置**：`AiyaSkuInitHandler`/`AiyaSkuOmsSyncDmpHandler` 这两个类写好了，但把它们注册进定时任务的配置是在数据库里配置的（不在代码仓库），需要找运维/产品在环境里把这两个 handler 的任务配置加上，否则代码上线了也不会被调度执行。
- [ ] **【新增】name 字段是否会真实populated**：2026-07-16 实测两条测试SKU响应里完全没有 `name` 键（只有必填的 `description`），`AiyaSkuOmsSyncDmpHandler` 已改为"name为空回退用description"防御性处理；但仍不确定爱亚后台正常建品流程下 `name` 是否会被填充，若长期都不填，回退逻辑虽不影响功能但 `WegoSkuSyncDTO.SkuItemDTO.name` 语义上会变成"实际存的是description"，无需现在处理，仅记录供后续排查参考。

### 库存数据相关（2026-07-16）

- [ ] **【新增，高优】`skuStatus` 是否会让同一 SKU 拆成多条库存明细**：接口文档截图确认响应 `inventoryVOList` 每条明细都带 `skuStatus`（商品状态），疑似跟 6.3.2「入库签收」段同名字段（GOOD/DAMAGE，良品/不良品）同义；2026-07-16 真实联调样例已证实 `skuStatus` 确实会返回具体取值 `"GOOD"`（不是"ALL"之类的汇总值），但样例里的两条测试 SKU 都只出现一条明细、值都是 `GOOD`，**没有出现同一 sku 两条明细的情况**——不能排除是因为这两个测试 SKU 没有不良品库存（`DAMAGE` 数量为0时接口可能不返回该状态的行），仍需找一个**有不良品库存的真实 SKU** 测试一次，才能确认是否会拆成两条明细。这会影响 `dmp_third_inventory` 的去重唯一键设计：如果唯一键只按 `warehouseCode+sku` 配置，一旦真的拆行，后到的一条会覆盖先到的一条，导致良品或不良品库存丢失。确认前不建议直接上线该链路的定时任务。
- [ ] **【新增】`domainCode` 用途未知**：接口文档截图里请求参数比之前的翻译稿多了一个可选字段 `domainCode`，没有任何参数描述。已在 `AiyaInventoryQueryDTO`/`AiyaOpenApiService.queryInventory` 里补充该字段（默认不传），但不确定是否需要传值、传什么值。需要找产品或爱亚侧确认这个字段的作用，确认后再决定要不要在 `AiyaInventoryInitHandler` 里默认填充。
- [ ] **分页翻页终止条件**：跟 SKU 一样，接口文档/真实响应样例只给了库存查询的响应结构（`{code,message,success,inventoryVOList}`），没有 `total`/`pages`/`emptyFlag` 之类的分页控制字段（2026-07-16 真实联调样例再次确认顶层只有 3 个字段）。当前实现按"本页返回条数 < pageSize"判断已到最后一页，还没有用超过一页的真实数据验证过这个判断方式对不对。
- [ ] **`skus[]`/`ignoreZero` 是否需要传**：当前 `AiyaInventoryInitHandler` 定时全量拉取时，只按仓库维度传 `warehouseCode`/`page`/`pageSize`/`stockStatus`，没有传这几个可选参数（认为"不传 skus 就返回该仓库全部 SKU 库存"）。接口文档截图确认 `pageSize`/`page` 的描述是"`skus` 不存在时必填"，说明不传 `skus` 时走分页全量拉取的用法是符合接口设计的；但仍需确认这个理解是否符合"定时拉取并展示，库存数量与爱亚后台一致"的业务预期。
- [ ] **`totalQty`/`skuDescription` 无落地字段**：`dmp_third_inventory`（三方仓库存中间表）没有对应这两个字段的列，文档自己给的字段映射表里这两项的"对应数大臣字段"也是空白。目前理解为不需要存这两个字段，但需要产品确认一下不存是否会影响后续排查/展示需求。同批截图核对还发现响应里另有 `customerCode`/`barcode` 两个中间表也没有列的字段，情况类似，一并确认。
- [ ] **DMP 任务与字段映射配置**：新增的 `AiyaInventoryInitHandler`（Input）和 `AiyaInventoryRocketMQTaskHandler`（Output）都需要在数据库配置表（`dmp_cfg_input_convert`/`dmp_cfg_output`/`dmp_cfg_input_convert_mapping`）里注册任务和字段映射，这部分在数据库里配置，不在代码仓库。建议的字段映射（爱亚字段 → `dmp_third_inventory` 列）：`warehouseCode→platform_warehouse_code`、`sku→product_sku`、`salableQty→sellable`（可售）、`occupiedQty→pi_freeze`（冻结）、`duePutawayQty→pending`（待上架）、`unavailableQty→unsellable`（不可售），其余在途/缺货类字段按文档默认0。**若上面「`skuStatus` 拆行」问题确认为真，这里的唯一键/映射设计也需要一并调整**。需要找运维/产品在环境里配置，否则代码上线了也不会被调度/推送。

### 商品映射相关（2026-07-16）

依据文档「6.2.2 商品映射」+「5. ERP新增/优化功能」章节梳理，对照现有公共 SKU 映射代码（`ListingInfoServiceImpl.syncWarehouseNotMatchSku`/`SkuMappingEntity`）后发现：

- 文档规则 a（数大臣未存在的新增）、e（存在且正常则更新name/barcode）**已经通过现有通用能力覆盖**：`AiyaSkuOmsSyncDmpHandler` 直接复用了平台无关的 `syncWarehouseNotMatchSku`（本来给 WEGO 用的），未新增/修改公共代码。
- 文档规则 d（爱亚已停用+已映射→改禁用）**2026-07-17 已开发完成**，详见下方「已确认结论」及「文档变更记录」。
- 文档规则 b（数大臣有爱亚无且未映射→删除）、c（已映射→改禁用）**2026-07-17 当天已与用户二次确认后判定不会真实发生，未实现**：三方仓拉取接口一般会把已下架/停用的 SKU 继续保留在全量结果里（只是状态变为停用/作废），不会整条从响应中消失，因此"快照中找不到对应SKU"这种场景不会出现，b/c 两条规则不需要实现，详见下方「已确认结论」。

以下遗留问题仍待确认，但均不阻塞已完成的开发：

- [ ] （关联「SKU 查询相关」的 `status` 字段疑问）规则 d 的判断依据是爱亚 `status` 非启用态（当前按"非 `Active`（忽略大小写）即视为停用"判断，未硬编码 `Inactive` 字符串），但 `Inactive` 真实取值大小写/是否还有其它状态值仍未拿到真实响应验证；即使大小写有出入，当前 `active`/`Active` 均已用 `equalsIgnoreCase` 兼容，不影响判断结果。
- [x] **【已解决】人工重新启用的入口未开发**：2026-07-20 已新增后端接口 `POST /skuMaping/updateStatus`（`SkuMappingController#updateStatus`），入参 `ids` + `status`（`SkuMappingStatusEnum`），仅允许对库存SKU（`RuleTypeEnum.WAREHOUSE`）类型批量启用/禁用，写操作日志，返回 `BatchResultDTO`；其它类型直接按条失败。前端页面按钮/交互仍需产品侧对接该接口，不在本次后端范围内。
- [ ] **【新增，中优】`SkuMappingServiceImpl` 中共享查询方法的 `status` 过滤覆盖范围**：本次已对确认与"头程发货单/海外仓入库单选库存SKU/B2C销售订单选库存SKU"等业务流程直接相关的方法（`getByAttribute`/`listBySkuNoList`/`listByInfo`(供`listBySkuList`)/`listByPlatformSkuNoAndPlatform`/`listStockSkuNoByProductSkuIds`/`listByErpSkuIdAndType`/`listByWarehouseAndPlatformSku`/`listSkuMappingByParams`）追加 `status=enable` 过滤；`listSkuBySkuNos`/`listByPlatformSkuNoList`/`findListDto`(`listByParams`，`isExpire`本身是可选参数、20+调用方语义不完全一致) 等方法暂未改动，`listByListingIds`（历史遗留字段回填、Excel导入比对等通用工具语义）也刻意保持不过滤。若后续发现有业务流程通过这些未过滤方法读到了已禁用映射并绕过拦截，需要补充确认调用语义后再决定是否追加过滤。
- [x] **【已解决】末批 `reconcileWarehouseSkuSnapshot` 请求体大小重新变为"全量"**：原实现为了支持规则b/c需要"完整快照"比对哪些SKU消失了，因此只在最后一批携带全量skuItems触发回收，重新引入了分批机制本要规避的超大请求体风险。2026-07-17 确认规则b/c不会真实发生（三方仓不会让SKU整条消失，只会变状态）后，回收逻辑简化为只看"本批SKU各自的状态"，不再需要完整快照比对，因此改为每一批都独立调用 `reconcileWarehouseSkuSnapshot`（不再区分是否最后一批），恢复了原有的 `SYNC_BATCH_SIZE=500` 分批保护，问题随架构简化一并解决。
- [ ] **【新增，代码审查发现，已知晓，暂不处理】`SkuMappingMapper.xml` 硬编码 `sm.status = 'enable'`（5处）与 Java 枚举值可能失配**：`SkuMappingStatusEnum.ENABLE` 的持久化 `code` 是 `"enable"`，XML 里直接写了同样的字符串常量，未通过 Mapper 参数传入。若以后修改枚举 `code`，这 5 处 XML 不会自动同步，导致业务判断口径不一致（不是SQL注入风险，是枚举单一来源约定问题）。已确认这 5 处写法跟随了同一文件里 `is_deleted = false`/`is_expire = false`/`match_result = 'false'` 等既有的硬编码字面量约定，不是本次改动独有的孤立问题，为保持同文件风格一致，暂不单独修改。若后续要统一治理，需要连同文件里其它历史硬编码字段一起按 Mapper 参数绑定方式改造，而非只改这 5 处。

### 尾程-出库单相关（2026-07-20）

对照文档 6.3.3 节与现有实现分析后，**仍待确认**项（建单请求字段类问题已移至下方「已确认结论」）：

- [ ] **【高优】出库单四个接口的真实 serviceType 标识**：代码里已改为 `GLINK_CREATE_ORDER_NOTIFY` / `GLINK_QUERY_ORDER_NOTIFY` / `GLINK_CANCEL_ORDER_NOTIFY`，仍需联调确认是否即为网关真实值。
- [ ] **建单幂等行为（联调）**：唯一键字段已确认为 `orderNumber`（见已确认结论）；仍需联调确认重复提交同一 `orderNumber` 的真实行为（报错拒绝 / upsert / 产生重复单），以及成功响应是否仍不回传独立出库单号。
- [ ] **取消/拦截为异步流程**：需确认「4、爱亚出库单查询」定时轮询是否就是唯一的拦截结果确认渠道，以及 `ThirdWarehouseCancelResultEnum.INTERCEPTING` 三态在 ERP 侧如何落地；`ThirdWarehouseCancelOutboundReq.confirmInterceptResult` 全仓库未见实际调用方。
- [ ] **`shipFrom` ERP 数据源**：请求字段约束已确认（见已确认结论）；仍需确认仓库 Entity/维度表是否已有寄件地址 + name/company 可取，还是要新增。
- [ ] **`shippingLabelSource` 三态 vs 现有配置二态**：AIYA 需要 ATTACHMENT/API/WMS_GEN，ERP「是否推海外仓面单」是二态，需确认如何映射第三态 WMS_GEN。
- [ ] **出库单查询响应结构是扁平还是嵌套**：方案文档看起来是扁平结构，当前 `AiyaOutboundResp` 骨架仍是 WEGO 嵌套结构，需要真实响应样例核实。
- [ ] **单据状态字母码 A/B/C/D 的完整语义**：尤其"D-锁住"未展开；`AiyaEnums.OrderStatusEnum` 仍是照抄 WEGO 的数字状态码占位。
- [ ] **"汉化管理"错误码翻译能力是否已有可复用实现**。
- [ ] **"超量发货"处理流程**：WEGO 无对应实现，AIYA 独有新分支。

## 已确认结论

（确认后从上面移到这里，写清结论与确认人/日期）

- **出库单创建/修改请求字段清单（2026-07-21，依据用户提供的爱亚开放平台接口文档截图，比方案文档翻译稿权威）**：
  - **顶层必填**：`customerCode`（SDK 注入）、`orderNumber`、`warehouseCode`、`orderTime`（格式 `yyyy-MM-dd'T'HH:mm:ssZ`，截图示例 `+0800`）、`shippingInstructions`、`shipTo`、`items[]`、`shipFrom`。
  - **顶层可选且方案文档有映射、已纳入 DTO**：`extOrderNumber`、`salesChannel`、`storeNumber`、`files[]`（ATTACHMENT 面单时用）。
  - **顶层可选且方案未映射、已从 DTO 剔除**：`poNumber`、`notes`、`signatureService`、`cashOnDelivery`、`freightCollect`、`collectingPaymentAmount`、`shippingRate`、`totalAmount`、`declaringValueAmount`、`orderType`、`currencyCode`、`latestShipDate`、`extUserId`、`isCb`、`documentType`、`invoiceList`、`externalPackageNumList`、`orderTagList`、`addedServices`、`startShipDate`、`originalOrderTime`、`supplier`、`udf5`~`udf15`、`cartonization`、`palletInfoList`、`isSpecifyBatch`、`addressType`、`platformOriginTrackingVos`、`doNumber`、`storeName`、`shippingType`、`priority`、`pickupCode`。
  - **`shippingInstructions` 必填子字段**：`carrier`、`carrierService`（默认 STD）、`shippingLabelSource`（ATTACHMENT/API/WMS_GEN）；可选且方案有映射：`trackingNumber`（方案文档误称为顶层 orderNumber 的「指定物流单号」）。官方另有 `carrierBillingType`/`shippingNotes`/`carrierAccount`/`serviceLevel`/`actualLogistic` 等可选未纳入。
  - **`shipTo` 必填子字段**：`name`、`mobileNumber`、`streetLine1`、`city`、`state`、`postalCode`、`countryCode`；可选且方案有映射：`email`、`streetLine2`、`district`（**官方为否**，方案文档标必填，按官方）。
  - **`shipFrom`（2026-07-21 用户补充确认）**：地址类必填 `streetLine1`/`city`/`state`/`postalCode`/`countryCode`；身份类 **`name`/`company` 必须至少填一个**（可同时填）。已写入 `AiyaOutboundSaveDTO.ShipFrom`。
  - **`files[]`（2026-07-21 用户补充确认）**：顶层可选；一旦下发条目则该条 **`fileType` 必填**，取值：`Shipping Label`（面单）/`Commercial Invoice`（发票）/`Bill of Lading`（提货单）/`Product Label`（商品标签）/`Carton Label`（箱贴）/`Packing List`（装箱单）/`Pallet Label`（板贴）/`Other`（其它）；另含 `fileName`/`fileUrl`。常量已落于 `AiyaOutboundSaveDTO.FileItem`，ManualTest ATTACHMENT 场景已按此传参。
  - **`items[]`（2026-07-21）**：方案文档映射为 `sku`/`quantity`；官方「图7」实为顶层后续可选字段而非 items 子字段展开，故明细子字段以方案文档为准落地，DTO 仅保留这两项。若后续补到官方 items 展开截图再核对是否有 lineNo 等额外必填。
  - 代码：`AiyaOutboundSaveDTO` + `AiyaOpenApiService.save2cOrder(DTO)` + `AiyaOpenApiServiceManualTest.save2cOrderTest`。
- **排重/关联键纠正（2026-07-21）**：唯一键是官方必填的 `orderNumber`（客户交易物流订单号=三方仓发货单号），不是方案文档映射表写的 `extOrderNumber`；`extOrderNumber` 官方为可选「客户销售平台编号」。此前差异分析里「用 extOrderNumber 做唯一键」的表述已废止。
- **库存查询接口真实请求/响应字段清单（2026-07-16，依据用户提供的爱亚开放平台接口文档页面截图，比方案文档翻译稿权威）**：
  - 请求字段（8个）：`customerCode`（必填）、`ignoreZero`（可选）、`domainCode`（可选，用途未知）、`skus[]`（可选，≤200个）、`pageSize`（`skus`不存在时必填）、`page`（`skus`不存在时必填）、`stockStatus`（文档标必填，实测非必传见下条）、`warehouseCode`（必填）。**没有 `status` 字段**（方案文档翻译稿里的 `status` 疑似跟 SKU 查询接口混淆，已从 `AiyaInventoryQueryDTO`/`AiyaOpenApiService.queryInventory`/`INVENTORY_QUERY_RESERVED_PARAM_KEYS` 移除）。
  - 响应 `inventoryVOList[]` 明细字段（11个）：`customerCode`、`warehouseCode`、`sku`、`skuDescription`、`barcode`、`skuStatus`、`totalQty`、`occupiedQty`、`salableQty`、`duePutawayQty`、`unavailableQty`。
  - 代码已按上述清单更新（`AiyaInventoryQueryDTO`/`AiyaOpenApiService.queryInventory`/`AiyaInventoryInitHandler` 的 Javadoc），但 `domainCode` 取值及 `skuStatus` 拆行问题仍未确认，见上方「待产品确认」。
- **`stockStatus` 实测非必传（2026-07-16，真实接口联调确认）**：库存查询接口文档标 `stockStatus` "是否必填=是"，但实测确认**不传也能正常查询，且不传效果更符合预期**（不传即返回全部状态库存，无需按 GOOD/DAMAGE 分别查两次）。已从 `AiyaInventoryInitHandler` 移除 `STOCK_STATUS_PLACEHOLDER` 占位值和 `reqDTO.setStockStatus(...)` 调用，改为默认不传该参数；`AiyaInventoryQueryDTO.stockStatus` 字段保留（供后续如需按状态过滤时使用），但去掉了"必填"相关注释。
- **库存查询响应顶层错误码字段为小写 `code`，不是 `Code`（2026-07-16，真实联调响应样例确认，修复代码 bug）**：真实响应样例 `{"code":"SUCCESS","success":true,"inventoryVOList":[...]}` 顶层字段是小写 `code`，跟项目里其它爱亚接口（`AbstractAiyaInitHandler`/`AiyaSkuInitHandler`/`AiyaWarehouseInitHandler`）一致。之前 `AiyaInventoryInitHandler.extractInventoryList` 误写成大写 `Code`，只在失败分支触发（`success=false` 时才会读取该字段拼进异常信息），不影响成功路径，但会导致失败时异常信息里的错误码显示成 `null`，已修正。同一份样例也确认了 `inventoryVOList` 明细 11 个字段与之前按接口文档截图确认的清单完全一致，且样例中 `skuStatus` 取值为 `"GOOD"`，验证了字段确实存在真实取值（但两条测试 SKU 都只有一条明细，尚未验证 `DAMAGE` 场景下是否会拆成两条，见上方「待产品确认」）。
- **SKU 查询接口真实必填约束（2026-07-16，联调报错 + 对照爱亚网关底层 QERP Open API Platform `GLINK_QUERY_ITEM_NOTIFY` 公开接口规范确认）**：方案文档「商品注册/查询」列出的请求字段（`status`/`pageSize`/`page`/`customerCode`）不完整，真实接口还有 `skus`（≤100个）/`createdTimeFrom`+`createdTimeTo`/`updatedTimeFrom`+`updatedTimeTo`，且这三组里**必须至少有一组非空**，否则报 `INVALID_DATA: Created time and Updated time and SKUs cannot be both empty`。已在 `AiyaSkuQueryDTO` 补充 `createdTimeFrom`/`createdTimeTo`（`updatedTimeFrom`/`updatedTimeTo`/`skus` 暂未接入），`AiyaSkuInitHandler` 固定传「2026-07-14 00:00:00（开发起始日期）~ 当前时间」，全量拉取以来创建的所有 SKU。
- **`createdTimeFrom` 固定锚点日期（2026-07-14）不会漏拉历史 SKU（2026-07-16，已与产品/业务确认）**：已确认爱亚该客户账号/SKU 不存在早于 2026-07-14 的创建记录，`AiyaSkuInitHandler.DEV_START_TIME` 固定为 `2026-07-14 00:00:00` 可以安全覆盖全部 SKU，无需再按账号建立时间调整锚点，此前的漏拉风险已排除。
- **SKU 查询响应真实字段结构（2026-07-16，真实联调响应样例确认）**：
  - 响应顶层**带 `total` 字段**（如 `{"total":2,"code":"SUCCESS","success":true,"itemList":[...]}`），文档未列出但真实存在，可用于判断是否已拉完全部数据；`AiyaSkuInitHandler` 已改用"累计条数达到 `total`"辅助判断翻页终止（`total` 缺失时自动退化为原有的"本页条数<pageSize"判断，双重兜底）。
  - `itemList` 每个元素返回的是**完整商品明细**（不只是查询/翻译稿列出的字段），实测包含：`sku`/`description`/`status`/`customerCode`/`shelfLifeUnit`/`requireSerialNumber`/`careMfgDate`/`careExpDate`/`careBatch`/`careOriginCountry`/`batteryFlag`/`hazmatFlag`/`liquidFlag`/`fragileFlag`/`barcodeList`（数组，元素为 `{unit,barcode}`）/`packagingList`（数组，元素为 `{unit,length,width,height,weight,dimensionUnit,weightUnit,overpackRequired}`，按 EA/INP/CS 三种单位各一条），以及单条 item 级别的 `code`/`success`。
  - `status` 真实取值确认为 `"Active"`（大小写与文档一致）。
  - `barcodeList` 与 `packagingList` 是**同级两个独立数组**，不是嵌套关系（`packagingList` 元素内没有各自的 `barcodeList`）；此前 `AiyaSkuOmsSyncDmpHandler.parseBarcodeList` 误读顶层 `barcode` 字段（实际不存在该字段），已修正为读取 `barcodeList[].barcode`，属于真实 bug 修复（此前条码同步到 OMS 一直是空列表）。
  - 两条测试样例响应里均**没有 `name` 键**（只有 `description`），`AiyaSkuOmsSyncDmpHandler` 已改为 name 为空时回退用 description，避免可选字段未设置导致未匹配表名称长期为空。
- **商品映射规则 b/c/d：状态字段设计（2026-07-17，已与用户确认）**：
  - 新增字段名为 `status`（不用 `mappingStatus`），不复用 `SkuMappingEntity.isExpire`（`isExpire` 语义是"是否已被新记录替代的历史版本"，与"业务上是否允许使用"是两个维度）。
  - 取值：`SkuMappingStatusEnum.ENABLE`/`DISABLE`，新建/历史数据默认 `ENABLE`（DB 列 `DEFAULT 'enable'`，Entity 字段 Java 侧默认值同步兜底）。
  - **规则b（未映射且源端消失→删除）、规则c（已映射且源端消失→禁用）不实现**：2026-07-17 当天二次确认，三方仓全量拉取接口通常会把已下架/停用的 SKU 继续保留在响应里（只是状态变化），不会整条从结果中消失，"快照中找不到对应SKU"这种场景在实际场景中不会发生，因此这两条规则不需要实现；只保留规则d（已映射且源端状态非启用→禁用）。原本为支持b/c设计的"删除 listing_info/sku_mapping"、"按完整快照比对哪些SKU消失了"等逻辑均已移除。
  - **规则d的判断不再需要"完整快照"**：判断是否禁用只依赖每条 SKU 自己携带的 `status`（是否为 `active`），不需要跟"本次任务拉到的全部SKU集合"比对，因此禁用逻辑可以按任意批次独立调用，不必等到最后一批才触发；`AiyaSkuOmsSyncDmpHandler` 已同步简化为每一批都调用同一个方法，不再区分是否最后一批，原有的 `SYNC_BATCH_SIZE=500` 分批保护也随之恢复（见上方「待产品确认」已解决项）。
  - **禁用逻辑已合并进 `syncWarehouseNotMatchSku`，不再有独立的 `reconcileWarehouseSkuSnapshot` 方法**（2026-07-17，见上方文档变更记录）：规则d移除"完整快照"依赖后，`reconcileWarehouseSkuSnapshot` 只是"调一次 `syncWarehouseNotMatchSku` + 加一段禁用判断"的薄封装，为减少不必要的 Feign 接口，已把禁用逻辑直接合并进 `syncWarehouseNotMatchSku` 本体，删除了 `reconcileWarehouseSkuSnapshot` 方法/Feign契约/Controller端点。爱亚、WEGO 现在统一调用同一个 `syncWarehouseNotMatchSku`；返回值类型改为 `WegoSkuSyncDTO.ReconcileResultDTO`（`addedCount`+`disabledCount`），WEGO 调用方已同步适配读取 `getAddedCount()`。方法本身仍按 `authId` 维度实现、不含爱亚专属逻辑，后续其它海外仓接入时可直接复用，无需再判断"是否传了 status"（未传 status 的调用方禁用分支天然不触发）。
  - **禁用后不会被系统自动重新置为启用**：只有规则 a/e（新增/更新）会持续跑，规则 d 的禁用是单向的，恢复启用需要人工介入。后端入口已于 2026-07-20 补齐：`POST /skuMaping/updateStatus`（仅库存SKU类型可改 `status`），见下方同日期结论。
  - **禁用状态需要在实际业务流程里拦截，不是只做展示**：已在 `SkuMappingServiceImpl` 中"解析当前可用映射供业务使用"的一批共享方法里追加 `status=enable` 过滤（清单见上方「待产品确认」），命中头程发货单下推海外仓入库单、海外仓入库单/B2C销售订单推三方仓出库单选库存SKU等链路；命中后复用各流程**现有的"SKU未映射"异常路径**，未新建异常类型，也未在报错文案上区分"从未映射"和"已被禁用"（如需区分可作为后续增强单独排期）。
  - DB 变更 `ALTER TABLE sku_mapping ADD COLUMN status varchar NOT NULL DEFAULT 'enable'` 未在仓库内找到迁移脚本机制（无 flyway/liquibase/`db/migration`），需要走仓库外流程手动执行，本次改动前需要先跟 DBA/运维确认已执行，否则代码里 `@TableField("status")` 会因列不存在报错。
- **人工启用/禁用库存SKU映射接口（2026-07-20）**：仿照 `/wms/warehouse-area/updateStatus`，在 `SkuMappingController` 新增 `POST /skuMaping/updateStatus`。请求体 `SkuMappingDTO.UpdateStatusDTO`（`ids` + `status`）；Service 按条校验「仅 `WAREHOUSE` 类型可改」、状态未变则记成功跳过、变更则写 `ModuleTypeEnum.SKU_MAPPING` 操作日志并 `updateBatchById`；返回 `List<BatchResultDTO>`，全部成功才 HTTP success。不加 `@DataPermission`（与同 Controller 的 `updateWarehouseSku`/`delete` 一致）。前端交互不在本次范围。

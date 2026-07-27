# 爱亚海外仓对接（临时工作文档）

> **用途**：供 `feature/20260714-aiya` 分支多会话对接使用。对接完成后可删除本目录，不必长期保留在主干。
>
> **来源**：`E:\Downloads\爱亚海外仓对接方案文档`（导入日期：2026-07-15）

## 目录说明

| 文件/目录 | 说明 |
|-----------|------|
| `爱亚海外仓对接方案文档.md` | 产品对接方案正文（含截图引用） |
| `图片和附件/` | 方案文档中的图片资源 |
| `sql/` | 各模块 DMP 落库脚本（映射 / output 等） |
| `sql/aiya_return_inbound_dmp_mapping.sql` | **退货入库**：字段映射 + 明细唯一键修正 + MQ output |
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
| 2026-07-22 | 用户反馈「任务拉取成功但三方仓库存无爱亚数据」：复核测试库确认根因仍是配置缺失——① `dmp_cfg_input_convert`=`2076948134579164619`（爱亚库存dmp）对应 mapping 表 0 条，导致 `dmp_third_inventory` 仅落出 `source_platform=aiya` 的空行（sku/仓码/数量全空，唯一键塌缩成 1 条）；② `dmp_cfg_output` 无 `AiyaInventoryRocketMQTaskHandler`，WMS `overseas_inventory` 无推送入口。需补 mapping + output 后重跑 Input，再触发 Output |
| 2026-07-16 | 用户提供真实联调响应样例（不传 `stockStatus` 时的查询结果），核对后发现并修复一个真实 bug：`AiyaInventoryInitHandler.extractInventoryList` 里失败分支读取的是 `response.get("Code")`（大写C），但真实响应顶层字段其实是小写 `code`（跟 `AbstractAiyaInitHandler`/`AiyaSkuInitHandler`/`AiyaWarehouseInitHandler` 里其它接口一致），大写写法会导致失败时异常信息里的错误码永远打印成 `null`。已修正为 `response.get("code")`，同步更新 `AiyaOpenApiService.queryInventory`/`AiyaInventoryInitHandler` 里写错的 `{Code,...}` 结构说明为 `{code,...}`。此外真实样例其它字段（`success`/`inventoryVOList`/明细 11 个字段、`skuStatus="GOOD"`）均与此前按接口文档截图确认的清单一致，无其它出入；样例仍只有 3 个顶层字段，未出现 `total`/`pages` |
| 2026-07-16 | 用户提供 `querySku` 真实联调响应样例（2条测试SKU：test1602/test2717），核对后有 1 项新发现 + 1 处真实 bug 修复：① **响应顶层实际带 `total` 字段**（`{"total":2,"code":"SUCCESS","success":true,"itemList":[...]}`），此前「分页翻页终止条件」待确认项已解决，`AiyaSkuInitHandler` 已改为"累计拉取条数达到 `total`"与"本页条数<pageSize"任一满足即停止翻页，`total`缺失时自动退化为纯size判断；② `status` 字段实测确认真实取值为 `"Active"`（大小写与文档一致），`Inactive` 暂未见真实样例但 `needSync` 用 `equalsIgnoreCase` 兼容，不受影响；③ **真实bug**：`AiyaSkuOmsSyncDmpHandler.parseBarcodeList` 之前读取顶层 `barcode` 字段，但真实响应条码字段是顶层 `barcodeList` 数组（元素为 `{unit,barcode}` 对象，与 `packagingList` 同级、非嵌套关系），导致条码同步到 OMS 一直是空列表，已修正为解析 `barcodeList`；④ 顺带发现两条测试SKU响应里完全没有 `name` 键（只有 `description`），`AiyaSkuOmsSyncDmpHandler` 的 name 映射已改为"`name` 为空则回退用 `description`"，避免可选字段 `name` 未设置时未匹配表里名称长期为空 |
| 2026-07-17 | 完成「商品映射」规则 b/c/d（全量快照回收）开发：`SkuMappingEntity`/`WarehousePagingViewDTO` 新增 `status` 字段 + `SkuMappingStatusEnum`（启用/禁用，与 `isExpire` 是两个维度，不复用旧字段）；`WegoSkuSyncDTO.SkuItemDTO` 新增可选 `status`（源端原始状态）；`ListingInfoService` 新增平台无关的 `reconcileWarehouseSkuSnapshot`（空快照防呆 + 规则b删除 + 规则c/d禁用），配套 Feign 契约 `OmsListingInfoFeign#reconcileWarehouseSkuSnapshot`；`AiyaSkuOmsSyncDmpHandler` 改为除最后一批走原 `syncWarehouseNotMatchSku`（a/e），最后一批携带全量 skuItems 调新方法触发回收；并在 `SkuMappingServiceImpl` 一批"解析当前可用映射供业务使用"的方法（`getByAttribute`/`listBySkuNoList`/`listByInfo`/`listByPlatformSkuNoAndPlatform`/`listStockSkuNoByProductSkuIds`/`listByErpSkuIdAndType`/`listByWarehouseAndPlatformSku`/`listSkuMappingByParams`）追加 `status=enable` 过滤，覆盖头程发货单、海外仓入库单选库存SKU、B2C销售订单推三方仓出库单选库存SKU等链路；管理页面（`warehousePaging`/`listWarehouseExport`/`paging`/导出等）不过滤，仅新增 `status`/`statusName` 展示列 + 可选筛选参数。本轮只对爱亚生效，`listByListingIds`（历史遗留 warehouseId 回填、Excel 导入比对等通用工具方法，含义偏"全部记录"而非"仅可用记录"）保持不加过滤，避免误伤 Excel 导入等未验证场景 |
| 2026-07-17 | 代码审查复核 3 项问题：① 末批 `reconcileWarehouseSkuSnapshot` 请求体重新变为全量（架构取舍，已知晓暂不处理，见「待产品确认」新增项）；② 新增/改写的两处批次进度日志（`AiyaSkuOmsSyncDmpHandler` 原 160-161/177-180 行）此前误用 `log.info`，违反仓库 `java-log-min-warn.mdc` 规则，已改为 `log.warn`；③ `sku_mapping.status` 列 DDL 未纳入仓库迁移机制，属已知且已文档化的发布检查项，不涉及代码改动。同时把本次新增代码里引用外部方案文档规则编号（如"规则b"/"规则a/e"）的 Java 注释改写为直接描述行为，避免依赖不会保留在仓库里的外部文档 |
| 2026-07-17 | 查生产环境确认海外仓单服务商 SKU 峰值约2400条（艾姆勒iml），量级远低于万级，此前"末批请求体变大"问题按暂不处理结论保留。当天晚些时候用户重新判断：规则b（未映射且源端消失→删除）、规则c（已映射且源端消失→禁用）依赖的"快照中找不到SKU"场景在真实三方仓接口里不会发生（三方仓通常只会把SKU状态改成停用/作废，不会让SKU整条从拉取结果消失），因此**移除b/c的实现，只保留规则d**（已映射且源端状态非启用→禁用）。连带效果：`reconcileWarehouseSkuSnapshot` 不再需要"完整快照"做消失比对，`AiyaSkuOmsSyncDmpHandler` 因此简化为每批都独立调用同一方法（不再区分最后一批），恢复了原有 `SYNC_BATCH_SIZE=500` 分批保护，之前「末批请求体变大」的审查问题随架构简化一并解决。涉及改动：`WegoSkuSyncDTO.ReconcileResultDTO` 移除 `deletedCount`；`ListingInfoServiceImpl.reconcileWarehouseSkuSnapshot` 去掉"未映射消失删除"和"已映射消失禁用"两段逻辑，只保留"已映射+本批状态非active→禁用"；`ListingInfoService`/`OmsListingInfoFeign` Javadoc 同步更新 |
| 2026-07-17 | 规则b/c移除后，`reconcileWarehouseSkuSnapshot` 内部实现已经很薄（只是"调一次 `syncWarehouseNotMatchSku` + 加一段禁用判断"），用户提出"减少不必要的Feign调用，避免触发大数据量审查"，因此**把 `reconcileWarehouseSkuSnapshot` 的禁用逻辑直接合并进 `syncWarehouseNotMatchSku`，删除 `reconcileWarehouseSkuSnapshot` 方法/Feign接口/Controller端点**，两条链路（爱亚、WEGO）统一只调用一个方法。为了让调用方能拿到"禁用了多少条"，`syncWarehouseNotMatchSku` 返回值从 `Integer` 改为 `WegoSkuSyncDTO.ReconcileResultDTO`（`addedCount`+`disabledCount`），同步改了三处：`OmsListingInfoFeign`/`ListingInfoFeignController`/`ListingInfoService`+`ListingInfoServiceImpl`。**WEGO 链路同步适配**：`WegoSkuOmsSyncDmpHandler` 原来直接用 `Integer` 返回值，改为读取 `result.getAddedCount()`；因为 WEGO 从不传 `SkuItemDTO.status`，禁用分支的 `inactiveSkuNoSet` 判断条件（`StringUtils.isNotBlank(item.getStatus())`）恒为 false，禁用分支不会被触发，WEGO 现有行为不受影响。顺带把该处改动到的批次日志（`WegoSkuOmsSyncDmpHandler` 原 139 行）按 `java-log-min-warn.mdc` 规则由 `log.info` 改为 `log.warn`。 |
| 2026-07-17 | 用户对上一轮合并后的代码做了一次审查，反馈 3 项问题，逐一核实均真实存在并修复：① `ApiErrorDmp.MAPPING_WAREHOUSE_SKU_SNAPSHOT_EXCEED_LIMIT`（7513）是此前为"服务商单次SKU超过一万条报错"预留但从未真正接入判断逻辑的死代码，用户确认当前~2400条峰值下没必要实现该限制，**已直接删除该错误码**（常量定义+`values()`引用）；② `ListingInfoServiceImpl.syncWarehouseNotMatchSku` 里判断源端状态时硬编码了字符串 `"active"`，**已提取为类内 `private static final String STATUS_ACTIVE`** 常量，并在 Javadoc 说明这是 OMS 侧平台无关的约定值，不依赖 `erp-sdk-wms-aiya` 的 `AiyaSkuStatusEnum`；③ 禁用回收部分用 `listByAuthIds(authId)`（无任何过滤条件，拉取该服务商全部 listing 记录）后在 Java 侧过滤4个条件，**已改为复用方法开头已在用的 `listByAuth(type, platformSkuNoList, authIdList)`**，把 `inactiveSkuNoSet` 作为 `platformSkuNo IN` 条件下推到 SQL，查询范围从"该服务商全部listing"收窄为"本批状态非active的SKU对应的listing"（`sourceType`/`matchResult` 因 `listByAuth` 不支持这两个参数，继续在 Java 侧过滤）。三处均为内部实现细节调整，不改变 Feign 契约/Service 接口签名/日志文案 |
| 2026-07-24 | 将 `WegoSkuSyncDTO` 重命名为平台无关的 `WarehouseSkuSyncDTO`（爱亚/WEGO 共用，不再用 WEGO 专名前缀承载通用契约）。 |
| 2026-07-20 | 补齐「人工重新启用」后端入口：`SkuMappingDTO.UpdateStatusDTO` + `SkuMappingService#updateStatus` + `POST /skuMaping/updateStatus`，仅库存SKU（`WAREHOUSE`）可批量改 `status`，写操作日志，返回 `BatchResultDTO`；相关待确认项已移至「已确认结论」 |
| 2026-07-20 | 启动「尾程-出库单对接」（文档 6.3.3 节），先做 AIYA 文档 vs 现有 WEGO 出库单实现的差异分析（未写代码）。核心发现见「尾程-出库单相关」待确认项 |
| 2026-07-21 | 依据用户提供的爱亚开放平台「创建/修改出库单」接口文档截图（图1-4 顶层、图5 `shippingInstructions`、图6 `shipTo`、图7 实际为顶层后续可选字段而非 items 子字段、图8 `shipFrom`）收敛建单报文：新增 `AiyaOutboundSaveDTO`；`AiyaOpenApiService.save2cOrder` 改为接强类型 DTO；完善 `AiyaOpenApiServiceManualTest.save2cOrderTest`。字段收敛原则：官方必填全留 + 方案文档有映射的可选字段保留；官方非必填且方案未映射的（udf*/代收货款/保价/托盘等）一律不进 DTO。关键纠正见下方「已确认结论」 |
| 2026-07-22 | 完成「尾程-出库单对接」核心链路（建单/截单/查询 + DMP 定时同步），用户明确同意 `shipFrom` 用占位常量、「汉化管理」错误码翻译与「超量发货」本次不做。改动清单：① `AiyaEnums.OrderStatusEnum` 由 WEGO 数字状态码占位改为方案文档字母码 A/B/C/D 映射 `SoB2cBillStatusEnum`；② `AiyaOutboundResp` 由 WEGO 嵌套分页结构改为扁平 `{success,code,message,resultList[]}`；③ `AiyaOpenApiService.search2cOrder`/`query2cOrderPage`（原指向同一 serviceType）合并为 `query2cOrder`，`intercept2cOrder` 入参由 `no` 改为 `orderNumber`；④ `AiyaHandlerServiceImpl` 实现 `createOutboundBill`/`cancelOutboundBill`/`queryOutboundBill`，`orderNumber` 直取 `referenceNo` 做幂等键（不需要 WEGO 那套"订单已存在"反查兜底），截单按 `TongYouHandlerServiceImpl` 三态范式处理；⑤ 新增 DMP 三件套 `AiyaOutboundInitHandler`/`AiyaOutBoundDmpHandler`/`AiyaOutboundRocketMQTaskHandler`，结构对齐 WEGO 出库单同步链路；⑥ `AiyaOpenApiServiceManualTest` 用 `query2cOrderTest`/`intercept2cOrderTest` 替换原 `search2cOrderTest`/`query2cOrderPageTest`。以上均为**按当前理解实现，尚未有真实接口响应样例验证**，详见下方「待产品确认」新增/保留项 |
| 2026-07-23 | 完成「调整单/库存状态转化」对接（文档 6.3.5）：爱亚按数大臣 OpenAPI 协议推送（`method=aiyaChangeAttribute`），auth `AiyaOpenApi` → WMS `receiveAiyaChangeAttribute` 幂等落已审核《仓位移动》。仅处理 `CHANGE_STATUS` + `GOOD↔DAMAGE`；仓/SKU 走爱亚仓库映射与 SKU 映射；双侧空仓位；操作类型 `overseasStockStatusConvert`。详见下方「调整单相关」与「已确认结论」 |
| 2026-07-24 | 完成「退货入库单对接」（文档 6.3.4）DMP 拉取链路：按用户确认口径用 `GLINK_BATCH_QUERY_ASN_NOTIFY`（`asnType=RETURN`）拉取，过滤 `status=Fulfilled` + `putawayStage=COMPLETED`，时间窗与头程一致用 `receiveTime`；明细取 `asnLineItems.putawayedQuantity` + `skuStatus`。新增 `AiyaReturnInstockInitHandler` / `AiyaReturnInstockDmpHandler` / `AiyaReturnInstockDetailDmpHandler` / `AiyaReturnInstockRocketMQTaskHandler`；**不改**数臣生成退货入库单/预入库单的现有代码。删除误仿 WEGO 的骨架 `AiyaReturnOrderResp` / `queryReturnOrderPage` / `RETURN_ORDER_QUERY_PAGE` 及对应 `ApiError`。test 库已有 `dmp_cfg_input`（爱亚退货入库）及 convert 指向上述 Handler，但 mapping/output 为空，脚本见 `sql/aiya_return_inbound_dmp_mapping.sql`。详见下方「退货入库相关」与「已确认结论」 |
| 2026-07-24 | 尾程出库单联调确认并落文档：① 建单幂等重复提交返回 `success=true, message="Order already exist."`，Handler 当成功、无需反查；② 截单首次成功 / 重复截单 `This order has been cancelled!` 均当成功；③ `success=false` 直接 `failure` 透传爱亚原文，去掉「已出库」关键词与 `INTERCEPTING` 猜测分支；④ `intercept2cOrder` 入参为 `orderNumbers[]`；⑤ **`shipFrom` 可不传**（官方曾标必填，实测不填可建单），去掉占位常量，Handler 默认不下发 |
| 2026-07-27 | 调整单审查续修：① 分步提交后幂等按审核状态续跑；② 同 SKU+出库状态汇总校验空仓位库存；③ `confirmDate` 固定 `Asia/Shanghai`。ApiError 11255 不改 |
| 2026-07-27 | 调整单入口改 DMP Webhook：`POST /webhook/receive/aiyaChangeAttribute`（裸 body + MetaResponse）；新增 `AiyaChangeAttributeWebhookHandler`；删除 auth `AiyaOpenApi` |
| 2026-07-27 | 审查收尾：DTO `partnerId`/`customerCode` 注释去掉 OpenAPI 残留；联调清单与「待确认」统一将 `verify` 标为上线阻断（非可选）；唯一索引仍按约定不加 |
| 2026-07-27 | 调整单鉴权讨论确认：不做业务鉴权；成功落单/幂等命中写 WMS 仓位移动 `operate_log`（`operation=爱亚Webhook接收`） |
| 2026-07-27 | **SKU 对照改回旧链路**：取消爱亚/WEGO Feign `syncWarehouseNotMatchSku` 主路径；新增 `AiyaSkuInfoDmpHandler`/`WegoSkuInfoDmpHandler` + `AiyaProductRocketMQTaskHandler`/`WegoProductRocketMQTaskHandler`；`PlatformListingConsumer` 补齐 WAREHOUSE 默认 disable/停用回收；公共逻辑抽 `WarehouseSkuReconcileHelper`；`PlatformDictEnum` 补 `WEGO`。环境切换见 `SKU旧链路切换清单.md` |

## 退货入库相关（2026-07-24）

### 总流程（对齐 WEGO，数臣生成复用现有逻辑）

```text
DMP定时「爱亚退货入库」
  → AiyaReturnInstockInitHandler（batchQueryAsn asnType=RETURN + receiveTime）
  → 过滤 Fulfilled + COMPLETED，归一化 asnLineItems
  → Mongo → AiyaReturnInstockDmpHandler / DetailDmpHandler
  → dmp_third_return_inbound(+detail)
  → AiyaReturnInstockRocketMQTaskHandler
  → 现有海外仓退货入库/预入库消费（不改数臣生成代码）
```

### 已实现代码入口

| 环节 | 类 |
|------|-----|
| Init 拉取 | `AiyaReturnInstockInitHandler` |
| 主表 DMP | `AiyaReturnInstockDmpHandler`（`putawayTime`→`putAwayTime`，强制海外仓类型） |
| 明细 DMP | `AiyaReturnInstockDetailDmpHandler`（`skuStatus=DAMAGE`→`defectiveProductFlag=true`） |
| MQ 推送 | `AiyaReturnInstockRocketMQTaskHandler`（组装 `PlatformReturnInstockDTO`） |
| SDK | 复用 `AiyaOpenApiService.batchQueryAsn` + `AiyaInboundResp`（**不用**已删除的 returnorder 骨架） |

### 字段映射（Init 归一化 → DMP）

| Mongo / 爱亚字段 | DMP 列 | 说明 |
|------------------|--------|------|
| `asnNumber` | `platform_return_order_no` | 第三方退货单号，幂等键 |
| `refNumber` | `order_reference_no` | 参考单号（下游判退货入库 vs 预入库） |
| `trackingNumber` | `return_logistic_code` | 退货物流单号 |
| `warehouseCode` | `warehouse_code` | 仓库编码 |
| `authId` | `auth_id` | 授权 ID |
| `status` | `status` | 单据状态原文 |
| `putawayTime` | `put_away_time` | Handler 解析，非 mapping |
| `sku` | `product_sku` | 明细 |
| `putawayedQuantity` | `must_qty` / `receive_qty` / `real_qty` | 上架量 |
| `thirdDetailId` | `third_detail_id` | `{asnNumber}_{lineNo}_{GOOD\|DAMAGE}` |
| `skuStatus` | → `defectiveProductFlag` | 无 mapping，DetailHandler 转换 |

固定值（convert `fixed_value_json` 已有）：`sourcePlatform=aiya`，`warehousePlatformType=overseasWarehouse`。

### DMP 配置状态（test）

| 配置 | 状态 |
|------|------|
| `dmp_cfg_input` 爱亚退货入库 `2076940293432694212` | 已有 |
| convert：Init/主表/明细 Handler 类名 | 已有 |
| `dmp_cfg_input_detail` normal/history 任务 | 已有（有效） |
| `dmp_cfg_input_convert_mapping` | **缺** → 执行 SQL |
| 明细 `unique_field_name` | 原 `mainId,productSku,realQty` 有良/不良冲突风险 → SQL 改为 `mainId,thirdDetailId` |
| `dmp_cfg_output` `AiyaReturnInstockRocketMQTaskHandler` | **缺** → 执行 SQL（`type_id` 与 WEGO 退货同为 `1895034764931390862`） |

**落库脚本**：[`sql/aiya_return_inbound_dmp_mapping.sql`](sql/aiya_return_inbound_dmp_mapping.sql)（需在 erp-dmp 库执行，代码仓库不自动跑）。

### 本期明确不做（退货入库）

- 不改数臣《退货入库单》/《预入库单》生成逻辑（参考单号关联、客户/组织回填等走现有海外仓消费）
- 不做数臣创建退货单再推爱亚（文档明确本期只拉已上架退货）
- 不使用 / 已删除 `returnorder.queryPage` 骨架

### 待联调 / 待配置（退货入库）

- [ ] **【高优】在 test/uat 执行** `sql/aiya_return_inbound_dmp_mapping.sql`（mapping + unique_field + output）
- [ ] **【高优】真实 RETURN 样例**：`Fulfilled`+`COMPLETED` 单据，核对 `asnLineItems` 是否按 GOOD/DAMAGE 拆行、`putawayedQuantity`/`putawayTime`/`refNumber` 是否有值
- [ ] 跑一次「爱亚退货入库」任务：核对 `dmp_third_return_inbound` 落库 + MQ 推送 + 下游生成退货入库/预入库
- [ ] 无参考单号 / 多平台订单号等边界：确认仍走现有预入库/告警逻辑（代码未改下游）

---

## 调整单相关（2026-07-23）

### 调用约定

- 入口：`POST /webhook/receive/aiyaChangeAttribute`（DMP `WebhookController`，网关免登路径已含 `/webhook/receive/`）
- 请求体：爱亚 `changeAttribute4Edi` **裸业务 JSON**（无 OpenAPI 外壳、无 `method/sign/data`）
- 响应：爱亚 `MetaResponse`（`success` / `code` / `message` / `data`），非 `ApiResult`
- 链路：`WebhookController` → `AiyaChangeAttributeWebhookHandler` → WMS `receiveAiyaChangeAttribute` → 已审核《仓位移动》

联调手册见：
[`aiyaChangeAttribute-postman-test.md`](./aiyaChangeAttribute-postman-test.md)

业务报文示例（与对方 JSON 契约一致）：

```json
{
  "partnerId": "PARTNER001",
  "customerCode": "CUST001",
  "warehouseCode": "WH001",
  "changeAttributeNumber": "CA202506140001",
  "confirmDate": 1752470400000,
  "type": "CHANGE_STATUS",
  "changeList": [
    {
      "sku": "SKU-001",
      "changeQty": 10,
      "fromStatus": "GOOD",
      "toStatus": "DAMAGE"
    }
  ]
}
```

### 实施配置（待完成）

#### 回调 URL（给爱亚 / EDI）

对方按 `changeAttribute4Edi` **裸报文**推送，不再使用 OpenAPI / `sys_referer_config` 验签。

告知对方：

- 回调地址：`https://{环境网关或域名}/webhook/receive/aiyaChangeAttribute`  
  （落 DMP；网关免登白名单已含 `/webhook/receive/`）
- Content-Type：`application/json`
- Body：与 `changeAttribute4Edi(2).json` 业务字段一致（无外壳）
- 成功判定：HTTP 200 且响应体 `success === true`（MetaResponse）

- [ ] 已将回调 URL 同步给爱亚/EDI（配置到 `glink.change.feed.back.url` 或对等项）  
- [ ] 首笔联调：对方推送裸 body，我方返回 MetaResponse，并落已审核《仓位移动》  
- [x] **鉴权口径（2026-07-27 讨论确认）**：不做业务鉴权（与极兔等 Webhook 一致）；接口调用写入 WMS `operate_log`（仓位移动单，`operation=爱亚Webhook接收`）

> 历史脚本 [`sql/aiya_openapi_referer_config.sql`](sql/aiya_openapi_referer_config.sql) 仅用于 OpenAPI 调试，**调整单回调不再依赖**。

### 联调注意（已实现口径）

- 仓库映射：`overseas_provider_warehouse.platform_warehouse_code` + 平台 `aiya`
- SKU 映射：`skuMappingFeign.listByWarehouseAndPlatformSku`
- 幂等：`source_type=aiyaChangeAttribute` + `source_code=changeAttributeNumber`  
  - 代码：`@DistributeLocker(businessType=aiyaChangeAttribute, unlockAfterTx=false)` + 落单前先查后写（不加 DB 唯一索引）  
  - 事务：无外层 `@Transactional`，对齐 `addAndApprove`（`add` → `submit` → `approve` 分步提交，避免事务内 Feign/流程）  
  - 幂等续跑：已存在且 `approve` 直接返回；`waitSubmit` 续 `submit+approve`；`approveIng` 续 `approve`；`reject` 抛错需人工处理  
  - 建单前按 `(skuId, outInventoryStatus)` 汇总校验空仓位库存（避免同行拆行超库存）  
  - `confirmDate` → `billDate` 按 `Asia/Shanghai` 解释毫秒时间戳
- 处理策略（折中）：
  - 非 `CHANGE_STATUS`：忽略并成功返回（MetaResponse `success=true`，`data` 可为空串）
  - `CHANGE_STATUS`：全有或全无——任一行非 GOOD↔DAMAGE / 数量非法则整单失败
  - 仓/SKU 映射失败：失败 MetaResponse（`success=false`）
  - 幂等重复：成功，`data` 为原主单 id
- 状态映射：爱亚 `GOOD`→ERP `usable`（可用）；`DAMAGE`→ERP `defectiveProduct`（不良品）
- 仓位：取货/上架默认空仓位（`""`）；库存校验按空仓位过滤；操作类型 `overseasStockStatusConvert` 已放宽「同仓位禁止」校验
- 不良品出库：建单阶段校验空仓位上的 `defectiveProductQty`（`WH_LOCATION_MOVE_DEFECTIVE_QTY_EXCEEDS`）
- `customerCode`：DTO 必填校验，不参与仓路由（映射表无客户字段）
- 代码入口：`WebhookController#receiveWebhook(aiyaChangeAttribute)` → `AiyaChangeAttributeWebhookHandler` → `WarehouseLocationMoveFeign#receiveAiyaChangeAttribute` → `WarehouseLocationMoveServiceImpl#receiveAiyaChangeAttribute`

### 数据链路示例（调整单）

公共前置：对方回调 URL 已指向我们 webhook；ERP 已维护该仓 `overseas_provider_warehouse`（platform=`aiya`）与 SKU 映射；空仓位上已有对应状态库存。

#### 1）GOOD → DAMAGE（成功）

请求业务体要点：`type=CHANGE_STATUS`，`fromStatus=GOOD`，`toStatus=DAMAGE`，`changeQty=10`，`changeAttributeNumber=CA001`

落库：

- `warehouse_location_move`：`source_type=aiyaChangeAttribute`，`source_code=CA001`，`operate_type=overseasStockStatusConvert`，已审核，`bill_date`←`confirmDate`
- 明细：出库 `usable` / 入库 `defectiveProduct`，双侧仓位 `""`，备注 `CA001+爱亚海外仓良品转不良品`

库存：空仓位 `usable -10`，`defectiveProduct +10`

响应：

```json
{ "success": true, "code": "SUCCESS", "message": null, "data": "主单id" }
```

#### 2）DAMAGE → GOOD（成功）

请求：`fromStatus=DAMAGE`，`toStatus=GOOD`，`changeQty=5`，`changeAttributeNumber=CA002`

落库：明细出库 `defectiveProduct` / 入库 `usable`，备注 `CA002+爱亚海外仓不良品转良品`

库存：空仓位 `defectiveProduct -5`，`usable +5`  
（若不良品库存不足，建单即失败：`WH_LOCATION_MOVE_DEFECTIVE_QTY_EXCEEDS`）

响应：`success=true`，`data` = 新主单 id

#### 3）幂等重复（成功返回原单）

同一 `changeAttributeNumber=CA001` 再推一次

行为：分布式锁内查到已有 `source_type+source_code`，不新建单、不改库存

响应：`success=true`，`data` = 首次落单的主单 id

#### 4）仓/SKU 映射失败（失败）

`warehouseCode` 或 `sku` 在 ERP 无映射

行为：WMS 抛 `ServiceException`；Webhook 转为 MetaResponse 失败

响应示例：

```json
{ "success": false, "code": "INVALID_OPERATION", "message": "未找到爱亚仓库映射...", "data": null }
```

#### 5）非 CHANGE_STATUS（忽略成功）

请求：`type=CHANGE_BATCHNO`（或其它非库存状态转移）

行为：warn 日志后直接返回，不落单

响应：

```json
{ "success": true, "code": "SUCCESS", "message": null, "data": "" }
```

补充：`type=CHANGE_STATUS` 时任一行非 GOOD↔DAMAGE / 数量非法 → **整单失败**（全有或全无，不再静默丢明细）

### 待产品确认 / 待联调验证（调整单）

核心代码已按约定写完，以下需联调或产品确认后才能算闭环：

- [ ] **【高优】爱亚侧回调是否已就绪**：确认 `glink.change.feed.back.url`（或对等）已指向 `POST /webhook/receive/aiyaChangeAttribute`
- [ ] **【高优】真实推送样例**：要一份真实 `CHANGE_STATUS` + `GOOD↔DAMAGE` 的请求体
- [ ] **【高优】空仓位 + 即时库存**：联调验证审核后可用/不良品数量是否正确增减
- [x] **不良品出库数量校验**：已补（2026-07-27）
- [x] **仓/SKU 映射失败策略**：抛错 → MetaResponse 失败（2026-07-27）
- [x] **并发幂等**：`@DistributeLocker` + 先查后写（2026-07-27）
- [x] **入口协议**：改为 DMP Webhook 裸报文 + MetaResponse（2026-07-27）；不再走 `AiyaOpenApi`
- [x] **鉴权口径**：不做业务鉴权；调用审计靠仓位移动 `operate_log`（2026-07-27 讨论确认）
- [ ] **过期自动良转不良**：是否全部走本回调
- [ ] **金蝶/下游是否需同步**：本期未做
- [ ] **冒烟用例**：Postman 直推裸 body 到 webhook，核对仓位移动 + 即时库存

### 本期明确不做（调整单）

- 批次号 / 生产日期 / 失效日期 / 原产国类转移（`CHANGE_BATCHNO` 等）
- `invType` 冻结量转移（`CHANGE_HOLD_QTY`）
- OpenAPI 外壳验签入口（对方无法按我方 `requestExample` 传参）
- 主动轮询爱亚转移单查询 API（契约不存在）

---

## 待产品确认

（其它模块历史待确认项；调整单专用项见上方「调整单相关」）

### SKU 查询相关（2026-07-15）

- [ ] **status 字段是否还有其它取值**：真实样例已确认 `Active` 大小写与文档一致（见「已确认结论」），但只见过 `Active`，`Inactive` 及是否存在其它状态值（比如草稿态）仍未见真实样例，需要一条已停用SKU的真实响应核对。
- [ ] **DMP 任务调度配置**：`AiyaSkuInitHandler`/`AiyaSkuOmsSyncDmpHandler` 这两个类写好了，但把它们注册进定时任务的配置是在数据库里配置的（不在代码仓库），需要找运维/产品在环境里把这两个 handler 的任务配置加上，否则代码上线了也不会被调度执行。
- [ ] **【新增】name 字段是否会真实populated**：2026-07-16 实测两条测试SKU响应里完全没有 `name` 键（只有必填的 `description`），`AiyaSkuOmsSyncDmpHandler` 已改为"name为空回退用description"防御性处理；但仍不确定爱亚后台正常建品流程下 `name` 是否会被填充，若长期都不填，回退逻辑虽不影响功能但 `WarehouseSkuSyncDTO.SkuItemDTO.name` 语义上会变成"实际存的是description"，无需现在处理，仅记录供后续排查参考。

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

### 尾程-出库单相关（2026-07-20，2026-07-22 更新）

核心链路（建单/截单/查询/DMP同步）已按当前理解完成开发，见下方「已确认结论」；以下均为**代码已实现但尚无真实接口响应样例验证**，仍需联调确认（不阻塞已完成开发，出问题时按这里逐项排查）：

- [x] **【高优】出库单相关接口的真实 serviceType 标识**：代码里已改为 `GLINK_CREATE_ORDER_NOTIFY`（建单）/`GLINK_QUERY_ORDER_NOTIFY`（查询，合并后的 `query2cOrder` 仍用此常量）/`GLINK_CANCEL_ORDER_NOTIFY`（截单），仍需联调确认是否即为网关真实值。
- [x] **【已确认】建单成功响应不回传独立出库单号（2026-07-22 联调）**：真实响应为 `{"success":true,"code":"SUCCESS","message":null,"data":null}`，`data` 为空；`AiyaHandlerServiceImpl.createOutboundBill` 已用下发的 `orderNumber`（=`referenceNo`）作为 `shippingOrderNo` 回写，无需改代码。
- [x] **【已确认】建单幂等行为（2026-07-24 联调）**：重复提交同一 `orderNumber` 返回 `{"success":true,"code":"SUCCESS","message":"Order already exist.","data":null}`。与 WEGO（`success=false, errorCode=2000` 需反查）不同，爱亚直接 `success=true`，Handler 按成功回写 `shippingOrderNo=orderNumber` 即可，无需反查兜底。注意：这是「已存在当成功」，不是内容 upsert。
- [x] **【已纠正】查询接口入参按方案文档落地（2026-07-22）**：强类型 `AiyaOutboundQueryDTO`；必填 `warehouseCode`，可选 `shippingTimeFrom`/`shippingTimeTo`/`page`/`pageSize`。方案文档写的 `pageNum` 有误，真实网关字段为 `page`（与 SKU/库存/入库一致），SDK 已按 `page` 下发。发运时间是 `shippingTime*`，不是建单的 `orderTime`，也不是入库/退货的 `putawayCompletedTime*`。
- [x] **【已补充】查询增加 `createdTimeFrom`/`createdTimeTo`（2026-07-23）**：`shippingTime*` 只能查到已发货单；DMP Init / Handler 反查改为按创建时间窗口拉取（对齐 WEGO `orderDate*`），`shippingTime*` 仍保留为可选过滤。
- [x] **【已确认】截单接口响应判定（2026-07-24 联调）**：首次成功 `success=true, message=null`；重复截单 `success=true, message="This order has been cancelled!"` 按幂等成功（`INTERCEPTION_SUCCESSFUL`）。`success=false` 不区分错误码，直接 `failure` 透传爱亚 `message`/`code` 原文（不再猜「已出库」/`INTERCEPTING`）。
- [ ] **取消/拦截业务异步流程（方案文档仍写异步）**：方案文档写取消后先「拦截中」、靠定时查询收敛；当前 Handler 截单接口侧已改为同步判定（成功/幂等成功 / `failure` 原文），不再在取消接口返回 `INTERCEPTING`。DMP `AiyaOutboundInitHandler` 仍负责日常状态同步（含取消后状态 B）。`ThirdWarehouseCancelOutboundReq.confirmInterceptResult` 全仓库未见实际调用方，是否还要二次确认拦截结果待产品定。
- [x] **【已确认】`shipFrom` 可不传（2026-07-24 联调）**：官方文档曾标必填，实测不填也能建单成功。`AiyaHandlerServiceImpl` / ManualTest 默认不下发；DTO 仍保留 `ShipFrom` 嵌套类便于按需透传。不再需要 ERP 寄件人数据源/占位常量。
- [x] **【已确认】`shippingLabelSource` 只用二态（方案文档已写清）**：爱亚接口枚举虽有 ATTACHMENT/API/**WMS_GEN** 三值，但方案文档业务映射只有两档——①「是否推海外仓面单=是」→ ATTACHMENT（附图 + 运单号/渠道）；②「=否」→ API（海外仓面单，物流渠道非必填）。**没有**映射到 WMS_GEN，ERP `isPushLabel` 二态与方案一致即可，无需第三态。
- [x] **【已确认】出库单查询响应为扁平结构（2026-07-22 联调）**：顶层 `{success, code, message, total, orderInfoList:[]}`，列表字段是 `orderInfoList`（不是同族仓库/承运商接口的 `resultList`）；`total` 失败时可为 null，成功分页是否回填待再确认。
- [ ] **单据状态字母码 A/B/C/D 的完整语义**：`AiyaEnums.OrderStatusEnum` 已改为方案文档字母码（A-已出库/B-已取消/C-库存不足/D-锁住），但"D-锁住"具体语义（被谁锁、是否会自动解锁）未展开，且不排除还有未列出的状态码，需联调确认。
- [ ] **DMP 任务与字段映射配置**：新增的 `AiyaOutboundInitHandler`（Input）/`AiyaOutBoundDmpHandler`（Convert）/`AiyaOutboundRocketMQTaskHandler`（Output）均需在数据库配置表里注册；建议字段映射：`orderNumber→order_code`/`reference_no`、`warehouseCode→warehouse_code`、`orderStatus→order_status`、`trackingNumber→tracking_no`、`actualLogistic→carrier_name`、`shippingTime→date_shipping`（Convert 侧也会解析 shippingTime）。需要找运维/产品在环境里配置。
- [ ] **"汉化管理"错误码翻译能力是否已有可复用实现**：本次不做，留待后续单独排期。
- [ ] **"超量发货"处理流程**：WEGO 无对应实现，AIYA 独有新分支；本次不做，留待后续单独排期。

## 已确认结论

（确认后从上面移到这里，写清结论与确认人/日期）

- **退货入库拉取口径（2026-07-24，用户确认）**：
  1. 接口用已有 `batchQueryAsn`（`GLINK_BATCH_QUERY_ASN_NOTIFY`），固定 `asnType=RETURN`；响应结构对齐桌面样例 `asnInfoList` / `AiyaInboundResp`。
  2. 「完全上架」过滤：`asnType=RETURN` + `putawayStage=COMPLETED` + `status=Fulfilled`。
  3. 时间窗与头程入库一致：`receiveTimeFrom`/`receiveTimeTo`（整天边界）。
  4. 明细数量/良不良：按方案文档取 `asnLineItems.putawayedQuantity` + `skuStatus`（GOOD→可用，DAMAGE→不良），**不**用头程入库那套 PV 上架流水。
  5. **不改**数臣生成退货入库单/预入库单的现有代码；DMP 任务/convert 类名已在库中建好，补 mapping + MQ output 即可。
  6. 误仿 WEGO 的 `returnorder.queryPage` / `AiyaReturnOrderResp` 无用，已从 SDK 删除。

- **调整单入口协议（2026-07-27，用户确认）**：对方无法按我方 OpenAPI `requestExample` 传参；改为 DMP `POST /webhook/receive/aiyaChangeAttribute`，请求体对齐 `changeAttribute4Edi` 裸 JSON，响应 `MetaResponse`。已删除 `AiyaOpenApi#aiyaChangeAttribute`。

- **出库单创建/修改请求字段清单（2026-07-21，依据用户提供的爱亚开放平台接口文档截图，比方案文档翻译稿权威）**：
  - **顶层必填**：`customerCode`（SDK 注入）、`orderNumber`、`warehouseCode`、`orderTime`（格式 `yyyy-MM-dd'T'HH:mm:ssZ`，截图示例 `+0800`）、`shippingInstructions`、`shipTo`、`items[]`（`shipFrom` 官方截图曾列入必填，见下条联调纠正）。
  - **顶层可选且方案文档有映射、已纳入 DTO**：`extOrderNumber`、`salesChannel`、`storeNumber`、`files[]`（ATTACHMENT 面单时用）。
  - **顶层可选且方案未映射、已从 DTO 剔除**：`poNumber`、`notes`、`signatureService`、`cashOnDelivery`、`freightCollect`、`collectingPaymentAmount`、`shippingRate`、`totalAmount`、`declaringValueAmount`、`orderType`、`currencyCode`、`latestShipDate`、`extUserId`、`isCb`、`documentType`、`invoiceList`、`externalPackageNumList`、`orderTagList`、`addedServices`、`startShipDate`、`originalOrderTime`、`supplier`、`udf5`~`udf15`、`cartonization`、`palletInfoList`、`isSpecifyBatch`、`addressType`、`platformOriginTrackingVos`、`doNumber`、`storeName`、`shippingType`、`priority`、`pickupCode`。
  - **`shippingInstructions` 必填子字段**：`carrier`、`carrierService`（默认 STD）、`shippingLabelSource`（ATTACHMENT/API/WMS_GEN）；可选且方案有映射：`trackingNumber`（方案文档误称为顶层 orderNumber 的「指定物流单号」）。官方另有 `carrierBillingType`/`shippingNotes`/`carrierAccount`/`serviceLevel`/`actualLogistic` 等可选未纳入。
  - **`shipTo` 必填子字段**：`name`、`mobileNumber`、`streetLine1`、`city`、`state`、`postalCode`、`countryCode`；可选且方案有映射：`email`、`streetLine2`、`district`（**官方为否**，方案文档标必填，按官方）。
  - **`shipFrom`（2026-07-24 联调纠正）**：官方曾标必填，实测**可不传**仍建单成功；Handler/ManualTest 默认不下发；DTO 保留 `ShipFrom` 供可选透传。若填写：地址类 streetLine1/city/state/postalCode/countryCode；身份类 name/company 至少填一个。
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
- **商品映射规则 a/d/e/f：状态字段设计（2026-07-17 起，2026-07-23 按最新方案文档对齐）**：
  - 新增字段名为 `status`（不用 `mappingStatus`），不复用 `SkuMappingEntity.isExpire`（`isExpire` 语义是"是否已被新记录替代的历史版本"，与"业务上是否允许使用"是两个维度）。
  - 取值：`SkuMappingStatusEnum.ENABLE`/`DISABLE`。Entity 字段 Java 默认值仍为 `ENABLE`（避免 B2C/客户等其它创建路径误伤）；**三方仓同步新增的未匹配占位映射显式写 `DISABLE`**。
  - **a**：数大臣不存在且源端启用 → 新增未匹配 listing + 占位 mapping（`status=disable`）。源端停用的新 SKU 不落库。
  - **b/c 不实现**：爱亚不存在删除 SKU 场景（方案文档已划掉）。
  - **d**：已映射且源端停用 → 映射关系置 `disable`，不做自动恢复；前端「启动」是否置灰由产品定，后端 `updateStatus` 仍可手动启停。
  - **e**：已映射且源端启用 → 忽略（仅纠偏 name/barcode）。
  - **f**：未映射且源端停用 → 软删 listing + 占位 mapping；`ReconcileResultDTO` 增加 `deletedCount`。
  - **完成映射时启用**：`updateWarehouseSku` / `addWarehouseSku` / 仓库 Excel 导入新建已绑定产品 SKU 的记录时显式 `status=enable`。
  - **禁用逻辑已合并进 `syncWarehouseNotMatchSku`**，爱亚/WEGO 统一调用；入参/出参为平台无关的 `WarehouseSkuSyncDTO`（原 `WegoSkuSyncDTO` 已重命名）；返回值 `addedCount`+`disabledCount`+`deletedCount`；WEGO 不传 status 时禁用/删除分支不触发。
  - **禁用状态在实际业务流程里拦截**：业务查询方法追加 `status=enable` 过滤（清单见上方「待产品确认」）。
  - DB 变更 `ALTER TABLE sku_mapping ADD COLUMN status varchar NOT NULL DEFAULT 'enable'` 需走仓库外流程手动执行。
  - **前端联调**：列表需展示/筛选 `status`；同步新增的未匹配多为禁用；映射成功后应变启用；规则 d 禁用后「启动」按钮是否置灰由产品/前端定。
- **人工启用/禁用库存SKU映射接口（2026-07-20）**：仿照 `/wms/warehouse-area/updateStatus`，在 `SkuMappingController` 新增 `POST /skuMaping/updateStatus`。请求体 `SkuMappingDTO.UpdateStatusDTO`（`ids` + `status`）；Service 按条校验「仅 `WAREHOUSE` 类型可改」、状态未变则记成功跳过、变更则写 `ModuleTypeEnum.SKU_MAPPING` 操作日志并 `updateBatchById`；返回 `List<BatchResultDTO>`，全部成功才 HTTP success。不加 `@DataPermission`（与同 Controller 的 `updateWarehouseSku`/`delete` 一致）。前端交互不在本次范围。
- **尾程-出库单核心链路（2026-07-22 起实现，2026-07-24 联调纠正 `shipFrom`/建单幂等/截单判定；「汉化管理」「超量发货」本次不做）**：
  - **建单幂等键**：`orderNumber` 直接取 ERP `referenceNo`（发货单号），不加时间戳后缀。
  - **建单成功响应（2026-07-22 联调确认）**：真实样例 `{"success":true,"code":"SUCCESS","message":null,"data":null}`，不回传独立出库单号；ERP 侧以建单时下发的 `orderNumber` 作为 `shippingOrderNo` 回写，后续查询/取消均以此号为 key。
  - **建单幂等（2026-07-24 联调确认）**：重复提交同一 `orderNumber` 返回 `success=true, message="Order already exist."`（非失败）；Handler 识别后打 warn 日志，仍按成功回写 `shippingOrderNo`。与 WEGO `errorCode=2000`+反查路径不同，爱亚**不需要**反查兜底。不是内容 upsert。
  - **`shipFrom`（2026-07-24 联调确认）**：可不传；Handler 默认不下发，无需 ERP 寄件人数据源/占位常量。
  - **状态枚举**：`AiyaEnums.OrderStatusEnum` 改为字母码 `A`(已出库→SHIPPED)/`B`(已取消→DISUSE)/`C`(库存不足→EXCEPTION)/`D`(锁住→EXCEPTION)，替换掉此前照抄 WEGO 的数字状态码占位。
  - **查询入参（2026-07-23）**：`AiyaOutboundQueryDTO` 支持 `createdTimeFrom`/`createdTimeTo`（主窗口）+ 可选 `shippingTime*`/`page`/`pageSize`。`AiyaOutboundInitHandler` / Handler `queryOutboundBill` 按创建时间窗口拉，对齐 WEGO `orderDate*`，避免 `shippingTime*` 漏未发货单。
  - **响应结构（2026-07-22 联调确认）**：扁平 `{success, code, message, total, orderInfoList:[OutboundOrderDTO]}`；明细按文档映射 `orderNumber`/`shippingTime`/`actualLogistic`/`trackingNumber`/`sku`+`qty`；单据状态字段名文档未给出，暂用 `orderStatus`。
  - **`safeResponseLog`（2026-07-22）**：改为读取爱亚字段 `code`/`message`（此前误用 WEGO 的 `errorCode`/`errorMsg`，失败日志会打成 null）。
  - **SDK 方法**：`query2cOrder(AiyaOutboundQueryDTO.QueryReqDTO)`；`intercept2cOrder` 入参为 `orderNumbers[]`（字符串集合，对齐方案文档与入库取消 `asnNumbers[]`）。
  - **截单响应判定（2026-07-24 联调确认）**：首次成功 `success=true, message=null`；重复截单 `success=true, message="This order has been cancelled!"` → `INTERCEPTION_SUCCESSFUL`（幂等）；`success=false` → `failure` 透传爱亚 `message`/`code` 原文（不再做「已出库」/`INTERCEPTING` 猜测分支）。
  - **`AiyaHandlerServiceImpl`**：`createOutboundBill` 按 `isPushLabel`+`labelUrl` 二态映射 `shippingLabelSource`，不下发 `shipFrom`；`cancelOutboundBill` 成功侧幂等、失败侧原文；`queryOutboundBill` 近 7 天创建窗口扫仓库后本地按 `orderNumber` 过滤。
  - **DMP 三件套**：`AiyaOutboundInitHandler`（按仓库 + `createdTimeFrom`/`createdTimeTo` 窗口分页拉取）、`AiyaOutBoundDmpHandler`（`shippingTime`→`dateShipping`）、`AiyaOutboundRocketMQTaskHandler`（按 `AiyaEnums.OrderStatusEnum` 映射后推送）。
  - 本次不做「汉化管理」错误码翻译、「超量发货」处理流程，均留待后续单独排期。
  - 涉及新增 `ApiError`：`WH_AIYA_SDK_OUTBOUND_QUERY_NO_RESPONSE`/`WH_AIYA_SDK_OUTBOUND_QUERY_FAILED`/`WH_AIYA_SDK_OUTBOUND_QUERY_CONVERT_FAILED`/`WH_AIYA_OUTBOUND_CODE_REQUIRED`/`WH_AIYA_OUTBOUND_DETAIL_EMPTY`（`ApiErrorWms` 11250~11254）。

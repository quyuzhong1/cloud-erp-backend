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
| 2026-07-16 | 用户提供真实联调响应样例（不传 `stockStatus` 时的查询结果），核对后发现并修复一个真实 bug：`AiyaInventoryInitHandler.extractInventoryList` 里失败分支读取的是 `response.get("Code")`（大写C），但真实响应顶层字段其实是小写 `code`（跟 `AbstractAiyaInitHandler`/`AiyaSkuInitHandler`/`AiyaWarehouseInitHandler` 里其它接口一致），大写写法会导致失败时异常信息里的错误码永远打印成 `null`。已修正为 `response.get("code")`，同步更新 `AiyaOpenApiService.queryInventory`/`AiyaInventoryInitHandler` 里写错的 `{Code,...}` 结构说明为 `{code,...}`。此外真实样例其它字段（`success`/`inventoryVOList`/明细 11 个字段、`skuStatus="GOOD"`）均与此前按接口文档截图确认的清单一致，无其它出入；样例仍只有 3 个顶层字段，未出现 `total`/`pages` |
| 2026-07-16 | 用户提供 `querySku` 真实联调响应样例（2条测试SKU：test1602/test2717），核对后有 1 项新发现 + 1 处真实 bug 修复：① **响应顶层实际带 `total` 字段**（`{"total":2,"code":"SUCCESS","success":true,"itemList":[...]}`），此前「分页翻页终止条件」待确认项已解决，`AiyaSkuInitHandler` 已改为"累计拉取条数达到 `total`"与"本页条数<pageSize"任一满足即停止翻页，`total`缺失时自动退化为纯size判断；② `status` 字段实测确认真实取值为 `"Active"`（大小写与文档一致），`Inactive` 暂未见真实样例但 `needSync` 用 `equalsIgnoreCase` 兼容，不受影响；③ **真实bug**：`AiyaSkuOmsSyncDmpHandler.parseBarcodeList` 之前读取顶层 `barcode` 字段，但真实响应条码字段是顶层 `barcodeList` 数组（元素为 `{unit,barcode}` 对象，与 `packagingList` 同级、非嵌套关系），导致条码同步到 OMS 一直是空列表，已修正为解析 `barcodeList`；④ 顺带发现两条测试SKU响应里完全没有 `name` 键（只有 `description`），`AiyaSkuOmsSyncDmpHandler` 的 name 映射已改为"`name` 为空则回退用 `description`"，避免可选字段 `name` 未设置时未匹配表里名称长期为空 |

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
- 文档规则 b（数大臣有爱亚无且未映射→删除）、c（已映射→改禁用+按钮置灰）、d（爱亚已停用+已映射→改禁用）**目前完全未实现**：`syncWarehouseNotMatchSku` 只做"存在则更新、不存在则插入"，不会对"本次增量里没出现的旧SKU"做任何删除/禁用判断，也没用到爱亚 `status` 字段。`SkuMappingEntity` 已有 `effectiveTime`/`expireTime`/`isExpire` 字段，"启用/禁用"状态本身大概率不用改表结构，缺的是**触发禁用/删除的全量对比逻辑**。

- [ ] **【高优，未开发】b/c/d 全量对比删除/禁用逻辑的实现边界**：要判断"哪些旧SKU这次没出现"，必须知道"本次全量拉取的完整SKU集合"，但 `AiyaSkuInitHandler`→`AiyaSkuOmsSyncDmpHandler` 是分页/分批推给 OMS 的（`SYNC_BATCH_SIZE=500`）。DMP 任务框架是否有"本次全量已跑完"的信号可以拿到完整集合再做对比？还是需要在爱亚侧新增逻辑攒一次全量快照？需要找技术确认任务框架能力。
- [ ] **【高优，需产品明确范围】这套 b/c/d 状态机要不要对现有 WEGO/谷仓等海外仓也生效**：文档原话"其它海外仓接口也同上处理"，听起来产品希望做成所有海外仓服务商通用能力，但这样会影响现有 WEGO 同步链路的行为，风险面更大。需要跟产品确认：这次是只对爱亚生效（爱亚专属代码里做全量对比），还是要顺带把公共 `ListingInfoServiceImpl`/`SkuMappingServiceImpl` 一起改掉（影响所有服务商）。
- [ ] **规则 b/c 边界理解待确认**：理解为 b 针对"未映射"的 `ListingInfoEntity`（原始爱亚SKU行，还没绑定productSkuId）直接物理删除；c 针对"已映射"的 `SkuMappingEntity` 行只做禁用（`isExpire=true`），不删除。这个理解目前只是读文档推断，未跟产品核实过。
- [ ] **"启动按钮置灰"是否需要后端额外拦截**：如果只是前端按 `isExpire` 状态置灰按钮，后端只要保证 `isExpire=true` 的映射在业务使用（如推单选仓库sku）时被正常拦截即可；如果还要求"被系统自动禁用的映射，人工不能手动重新启用"这类额外规则，文档未写清楚，需要产品确认。
- [ ] （关联「SKU 查询相关」的 `status` 字段疑问）规则 d 的判断依据是爱亚 `status=Inactive`，但 `status` 真实取值大小写/是否还有其它状态值仍未拿到真实响应验证，会直接影响 d 规则判断条件。

## 已确认结论

（确认后从上面移到这里，写清结论与确认人/日期）

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

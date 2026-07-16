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

## 待产品确认

（对接过程中往这里追加，格式示例）

- [ ] 问题描述……（会话/日期）

### SKU 查询相关（2026-07-15）

- [ ] **分页翻页终止条件**：文档「商品注册/查询」接口只给了 `page`/`pageSize`/`status` 请求字段和 `{code,message,success,itemList}` 响应结构，没有 `total`/`pages`/`emptyFlag` 之类的分页控制字段（WEGO 的 SKU 接口有）。当前实现暂时按"本页返回条数 < pageSize"判断已到最后一页，需要用真实测试环境联调一次确认这个判断方式对不对，或者接口其实有别的字段没写进文档。
- [ ] **status 字段真实取值**：文档写的是字符串 `Active`/`Inactive`（区分大小写没写清楚），当前 `AiyaSkuStatusEnum` 按这两个值实现、且两种状态都会同步。需要拿一次真实响应样例核对大小写、是否还有其它状态值（比如草稿态）。
- [ ] **barcode 是否会返回多个**：文档只说"产品条形码"，没说单个还是数组。当前实现照 WEGO 的方式做了兼容（数组或单字符串都能处理），但没有真实数据验证过。
- [ ] **DMP 任务调度配置**：`AiyaSkuInitHandler`/`AiyaSkuOmsSyncDmpHandler` 这两个类写好了，但把它们注册进定时任务的配置是在数据库里配置的（不在代码仓库），需要找运维/产品在环境里把这两个 handler 的任务配置加上，否则代码上线了也不会被调度执行。

### 库存数据相关（2026-07-16）

- [ ] **`stockStatus` 取值未文档化（新增线索，仍未确认）**：接口文档截图确认 `stockStatus`（商品库存状态）为必填，但依然没给出可选枚举值列表。新线索：文档 6.3.2「入库签收」段有同名字段 `skuStatus`，取值为 `GOOD`/`DAMAGE`（良品/不良品），而这次核对的库存接口**响应**里恰好也返回了 `skuStatus` 字段——推测请求里的 `stockStatus` 很可能也是 `GOOD`/`DAMAGE` 这套枚举，而不是一个能表示"查全部"的值（如果是，可能需要按 `GOOD`、`DAMAGE` 各查一次才能拿到全量库存，而不是一次查询）。这只是根据文档其它段落的推测，**未经真实接口验证**，当前代码（`AiyaInventoryInitHandler`/`AiyaOpenApiServiceManualTest`）仍用占位值 `"ALL"`。需要用真实沙箱账号跑一次 `queryInventoryTest` 或找产品/爱亚接口文档确认真实取值，确认后替换 `AiyaInventoryInitHandler.STOCK_STATUS_PLACEHOLDER`。
- [ ] **【新增，高优】`skuStatus` 是否会让同一 SKU 拆成多条库存明细**：接口文档截图确认响应 `inventoryVOList` 每条明细都带 `skuStatus`（商品状态），若其含义确实是良品/不良品（呼应上一条推测），该库存接口可能对同一 `warehouseCode+sku` 按状态各返回一条明细（如 GOOD 一条、DAMAGE 一条），而不是合并成一条。这会影响 `dmp_third_inventory` 的去重唯一键设计：如果唯一键只按 `warehouseCode+sku` 配置，后到的一条会覆盖先到的一条，导致良品或不良品库存丢失。**这是本次截图核对新发现的风险点，需要用真实数据验证是否拆行，并跟产品/DMP 配置人员确认唯一键要不要把 `skuStatus` 也纳入**，确认前不建议直接上线该链路的定时任务。
- [ ] **【新增】`domainCode` 用途未知**：接口文档截图里请求参数比之前的翻译稿多了一个可选字段 `domainCode`，没有任何参数描述。已在 `AiyaInventoryQueryDTO`/`AiyaOpenApiService.queryInventory` 里补充该字段（默认不传），但不确定是否需要传值、传什么值。需要找产品或爱亚侧确认这个字段的作用，确认后再决定要不要在 `AiyaInventoryInitHandler` 里默认填充。
- [x] **`status` 字段确认为翻译稿误引入，已移除**：接口文档截图的请求参数列表里没有 `status` 字段（只有 `customerCode`/`ignoreZero`/`domainCode`/`skus[]`/`pageSize`/`page`/`stockStatus`/`warehouseCode` 共 8 个），之前方案文档翻译稿里出现的 `status` 疑似是跟"商品数据查询/SKU"接口混淆导致的错误。已从 `AiyaInventoryQueryDTO`、`AiyaOpenApiService.queryInventory`、`INVENTORY_QUERY_RESERVED_PARAM_KEYS` 中移除，不再需要产品确认。
- [ ] **分页翻页终止条件**：跟 SKU 一样，接口文档只给了库存查询的请求字段和响应结构（`{Code,message,success,inventoryVOList}`），没有 `total`/`pages`/`emptyFlag` 之类的分页控制字段。当前实现按"本页返回条数 < pageSize"判断已到最后一页，需要用真实测试环境联调一次确认这个判断方式对不对。
- [ ] **`skus[]`/`ignoreZero` 是否需要传**：当前 `AiyaInventoryInitHandler` 定时全量拉取时，只按仓库维度传 `warehouseCode`/`page`/`pageSize`/`stockStatus`，没有传这几个可选参数（认为"不传 skus 就返回该仓库全部 SKU 库存"）。接口文档截图确认 `pageSize`/`page` 的描述是"`skus` 不存在时必填"，说明不传 `skus` 时走分页全量拉取的用法是符合接口设计的；但仍需确认这个理解是否符合"定时拉取并展示，库存数量与爱亚后台一致"的业务预期。
- [ ] **`totalQty`/`skuDescription` 无落地字段**：`dmp_third_inventory`（三方仓库存中间表）没有对应这两个字段的列，文档自己给的字段映射表里这两项的"对应数大臣字段"也是空白。目前理解为不需要存这两个字段，但需要产品确认一下不存是否会影响后续排查/展示需求。同批截图核对还发现响应里另有 `customerCode`/`barcode` 两个中间表也没有列的字段，情况类似，一并确认。
- [ ] **DMP 任务与字段映射配置**：新增的 `AiyaInventoryInitHandler`（Input）和 `AiyaInventoryRocketMQTaskHandler`（Output）都需要在数据库配置表（`dmp_cfg_input_convert`/`dmp_cfg_output`/`dmp_cfg_input_convert_mapping`）里注册任务和字段映射，这部分在数据库里配置，不在代码仓库。建议的字段映射（爱亚字段 → `dmp_third_inventory` 列）：`warehouseCode→platform_warehouse_code`、`sku→product_sku`、`salableQty→sellable`（可售）、`occupiedQty→pi_freeze`（冻结）、`duePutawayQty→pending`（待上架）、`unavailableQty→unsellable`（不可售），其余在途/缺货类字段按文档默认0。**若上面「`skuStatus` 拆行」问题确认为真，这里的唯一键/映射设计也需要一并调整**。需要找运维/产品在环境里配置，否则代码上线了也不会被调度/推送。

## 已确认结论

（确认后从上面移到这里，写清结论与确认人/日期）

- **库存查询接口真实请求/响应字段清单（2026-07-16，依据用户提供的爱亚开放平台接口文档页面截图，比方案文档翻译稿权威）**：
  - 请求字段（8个）：`customerCode`（必填）、`ignoreZero`（可选）、`domainCode`（可选，用途未知）、`skus[]`（可选，≤200个）、`pageSize`（`skus`不存在时必填）、`page`（`skus`不存在时必填）、`stockStatus`（必填，取值未知）、`warehouseCode`（必填）。**没有 `status` 字段**。
  - 响应 `inventoryVOList[]` 明细字段（11个）：`customerCode`、`warehouseCode`、`sku`、`skuDescription`、`barcode`、`skuStatus`、`totalQty`、`occupiedQty`、`salableQty`、`duePutawayQty`、`unavailableQty`。
  - 代码已按上述清单更新（`AiyaInventoryQueryDTO`/`AiyaOpenApiService.queryInventory`/`AiyaInventoryInitHandler` 的 Javadoc），但 `stockStatus`/`domainCode` 取值及 `skuStatus` 拆行问题仍未确认，见上方「待产品确认」。

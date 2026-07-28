# 爱亚/WEGO SKU 旧链路切换清单

> 代码已改为：`Init → Mongo → *SkuInfoDmpHandler → dmp_product_info/dmp_sku_info → *ProductRocketMQTaskHandler → PlatformListingConsumer`。
> **无环境配置则任务跑不通。**

## 1. Input Handler 切换

| 平台 | 原 Handler（已 @Deprecated，仍可兼容） | 建议改为 |
|------|----------------------------------------|----------|
| 爱亚 | `...dmp.aiya.AiyaSkuOmsSyncDmpHandler` | `...dmp.aiya.AiyaSkuInfoDmpHandler` |
| WEGO | `...dmp.wego.WegoSkuOmsSyncDmpHandler` | `...dmp.wego.WegoSkuInfoDmpHandler` |

Init 可保留：`AiyaSkuInitHandler` / `WegoSkuInitHandler`。

## 2. convert_mapping（必须）

草稿 SQL（按 test 库 ID + 极风/谷仓结构拟定）：
[`sql/aiya_wego_sku_old_path_dmp.sql`](./sql/aiya_wego_sku_old_path_dmp.sql)

为爱亚/WEGO SKU Input 配置两套 convert（对齐极风/谷仓）：

| storage_name | 关键字段建议 |
|--------------|----------------|
| `dmp_product_info` | `source_platform`←`sourcePlatform`/`aiya`\|`wego`，`auth_id`←`authId`，`spu_id`/`spu_no`←`sku` |
| `dmp_sku_info` | `sku_no`←`skuNo`，`name`←`name`，`sku_id`←条码串，`status`←`status`，`main_id` 挂父 product |

Handler 已在 `getDetailList` 写入：`spuId/spuNo/skuNo/name/status/skuId/sourcePlatform`。

## 3. Output（必须）

| 平台 | Output Handler | Topic / Tag |
|------|----------------|-------------|
| 爱亚 | `...mq.aiya.AiyaProductRocketMQTaskHandler` | 与现商品一致：`PLATFORM_PULL_DATA_TOPIC` + `third_system_product_tag` |
| WEGO | `...mq.wego.WegoProductRocketMQTaskHandler` | 同上 |

**必须同时有 `dmp_cfg_output_detail`（按授权 `next_level_id`）**，否则 Input 写完 `dmp_sku_info` 后不会创建 Output 任务、MQ 不推 OMS。  
授权时 `TbTaskTypeService.addDmpOutputDetail` 会给**当时已存在**的 output 补 detail；若先插 output 后授权未再触发，需执行  
[`sql/aiya_wego_sku_output_detail_patch.sql`](./sql/aiya_wego_sku_output_detail_patch.sql)。

`platformStatus` 来自 `dmp_sku_info.status`（爱亚原文 Active/Inactive；WEGO 已在 Input 归一 1→Active、4→Inactive）。

人工 `updateStatus` 启用：若 `listing_info.platform_status` 为停用（非 Active），接口拒绝。

## 4. 消费端规则（已合代码）

`PlatformListingConsumer` 在 `type=WAREHOUSE` 时：

- 爱亚/WEGO 新增占位 mapping → `status=disable`，`hasMappingAll=true`（其它仓仍默认 enable，避免旧仓回归）
- 新 SKU 且 `platformStatus` 非 Active → 不落库（仅当消息带了状态时）
- 已存在且停用 → 已映射禁用 / 未匹配占位软删
- Active 恢复 **不**自动 enable
- 前端启停置灰看 `listing_info.platform_status`

## 5. Feign 下线

`syncWarehouseNotMatchSku` 已 `@Deprecated`，过渡期保留。确认无调用方后删除 Feign/Controller/Service 与 `WarehouseSkuSyncDTO`。

## 6. 回归清单

- [ ] 爱亚 Active 新增 → listing + mapping(disable)
- [ ] 爱亚 Inactive 已映射 → mapping disable
- [ ] 爱亚 Inactive 未匹配 → 软删
- [ ] 人工绑定后 enable；Active 再同步不自动 enable
- [ ] WEGO 已提交(1)/废弃(4) 同上；草稿(0) 不进库
- [ ] 极风等旧仓不传 platformStatus → 行为与改前一致（不回收）
- [ ] B2C listing 无回归

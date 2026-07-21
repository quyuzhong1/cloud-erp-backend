# 迭代 26.7.3 前端接口文档

> **分支**：`feature/20260720-26.7.3`  
> **适用范围**：PC Web、PDA（不含小程序 UI 改造、不含直接调拨批量填充）  
> **统一响应**：`ApiResult<T>`，`code=200` 为成功；批量操作为 `List<BatchResultDTO>` 时逐条看 `success`  
> **路径说明**：下列路径为各微服务 Controller 映射；经网关访问时需加对应服务前缀（如 WMS `/wms`、DMP `/dmp`、PLM `/plm`，以实际网关配置为准）

---

## 目录

1. [环境依赖](#1-环境依赖)
2. [新增接口](#2-新增接口)
3. [变更接口](#3-变更接口)
4. [枚举与字典](#4-枚举与字典)
5. [错误码](#5-错误码)
6. [不在本文档范围的接口](#6-不在本文档范围的接口)

---

## 1. 环境依赖

上线/联调前需确认：

| 项 | 说明 |
|----|------|
| 数据库脚本 | 执行 `docs/sql/iter_26_7_3.sql`（箱唛作废字段、退货原因类型、寄修状态迁移、`outbound_track_status`、节点配置等） |
| 定时任务 | XXL-JOB 配置 `AfterSaleTrackSyncJob`，建议每 **6 小时** 执行一次，用于同步寄修单「商家寄件物流状态」 |
| 导出任务 | 售后装箱导出异步任务类型：`EXPORT_WMS_AFTER_SALE_PACK` |

---

## 2. 新增接口

### 2.1 售后装箱 · 箱唛作废（PC）

**POST** `/afterSalePack/invalid`

**权限**：`wms:afterSalePack:invalid`

**请求体**

```json
{
  "ids": ["箱唛主键id"],
  "remark": "作废原因（可选）"
}
```

**响应**

```json
{
  "code": 200,
  "data": [
    {
      "success": true,
      "id": "xxx",
      "code": "BOX20250720001",
      "msg": "作废成功"
    }
  ]
}
```

**业务规则**

- 仅 **空箱唛**（`totalQty=0` 且 `skuSpeciesQty=0`）、**待装箱**、**未被单据绑定**（`isUse=false`）、**未作废** 可作废
- 批量时部分失败：`code=200` 但 `data` 中个别 `success=false`，需逐条展示 `msg`

---

### 2.2 售后装箱 · 箱唛作废（PDA）

**POST** `/pda/afterSalePack/invalid`

契约与 [2.1](#21-售后装箱--箱唛作废pc) 完全一致。

---

### 2.3 售后装箱 · 导出（PC）

**POST** `/afterSalePack/exportExcel`

**权限**：`wms:afterSalePack:exportExcel`  
**高级搜索**：需支持 `@WebAdvanceQuery`（与列表 `paging` 相同筛选条件）

**请求体**

```json
{
  "ids": ["可选，勾选导出"],
  "params": {
    "sqlMap": {},
    "permissionSql": ""
  }
}
```

> `ExportDTO` 继承列表分页/搜索参数，未勾选 `ids` 时按当前高级搜索条件全量导出。

**响应**

```json
{
  "code": 200,
  "data": true
}
```

**说明**

- 异步导出，前端轮询/订阅下载中心
- 任务事件码：`EXPORT_WMS_AFTER_SALE_PACK`
- Excel 模板字段见 `AfterSalePackDTO.ExportViewDTO`（含作废状态、箱唛、SKU、仓位等）

---

### 2.4 采购退货 · 默认组织（PDA）

**GET** `/pdaPoReturn/getDefaultOrg?returnWarehouseId={warehouseId}`

**请求参数**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| returnWarehouseId | string | 是 | 退货仓库 ID |

**响应**

```json
{
  "code": 200,
  "data": {
    "returnOrgId": "退料组织ID",
    "returnOrgName": "退料组织名称",
    "purchaseOrgId": "默认采购组织ID",
    "purchaseOrgName": "东莞市简拍智造科技有限公司"
  }
}
```

**前端交互**

1. 用户选择「退货仓库」后调用  
2. `returnOrg*` 由仓库带出，只读展示即可  
3. `purchaseOrg*` 默认填充，**允许用户修改**

---

### 2.5 系统配置 · 采购退货默认采购组织（PC）

**GET** `/cfgSetting/getPoReturnDefaultOrgSetting`

**响应**

```json
{
  "code": 200,
  "data": {
    "purchaseOrgId": "默认采购组织ID"
  }
}
```

**PC 采购退货页联调说明**

- PC **暂无** 与 PDA 同名的 `getDefaultOrg` 接口
- 推荐做法：选择退货仓库后  
  - 退料组织：从仓库详情接口取 `orgId` / 组织名称  
  - 采购组织：调用本接口取 `purchaseOrgId`，再查组织名称展示；用户可改

---

### 2.6 字典 · 采购退货原因类型（PC/PDA）

走现有字典接口，**新增 type**：

**GET** `/dict/drop/down?type=poReturnReasonType`

（WMS 服务下完整路径一般为 `/wms/dict/drop/down`，以网关为准）

**响应示例**

```json
{
  "code": 200,
  "data": [
    { "value": "defect", "name": "瑕疵品" },
    { "value": "other", "name": "其它" }
  ]
}
```

**前端交互**

| returnReasonType | UI |
|------------------|-----|
| `defect`（默认） | 下拉选「瑕疵品」，可不展示文本框 |
| `other` | 下拉选「其它」，展示文本框填写说明，提交到 `returnRemark` |

---

## 3. 变更接口

### 3.1 DMP 寄修单（PC）

服务前缀：**DMP** `/afterSale`

#### 3.1.1 列表 / 详情 · 新增出参字段

涉及接口：

- `POST /afterSale/paging`
- `POST /afterSale/tabList`
- `GET /afterSale/view`

**新增字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| dictPlatformName | string | 平台名称（列表/详情展示） |
| outboundTrackStatus | string | 商家寄件物流状态 code（Track123） |
| outboundTrackStatusName | string | 商家寄件物流状态名称（列表「商家寄件物流状态」列） |

**状态枚举变更**

| code | 展示名（nodeName） | 说明 |
|------|-------------------|------|
| toBeShipped | 待商家寄出 | 维修完成，等待/已下物流 |
| **completed** | **已完成** | **新增**；获取商家寄出物流单号后进入 |
| terminated | 已终止 | 不变 |

> 进度节点以 `GET /afterSale/getNodeList` 返回为准（含 index=6 的 `completed`）。

#### 3.1.2 修改状态

**POST** `/afterSale/changeStatus`

**请求体（节选）**

```json
{
  "ids": ["寄修单id"],
  "node": "completed",
  "trackNo": "商家寄出快递单号",
  "remark": "维修备注（待商家寄出时可填）"
}
```

**变更说明**

- `node` 支持 **`completed`**
- 变更为 `completed` 时 **`trackNo` 必填**（商家寄出快递单号）
- 成功后单据状态为已完成，后端会注册 TMS 物流轨迹
- 待商家寄出 → 已完成：也可由物流下单接口自动完成（见下）

#### 3.1.3 物流下单

**POST** `/afterSale/logisticsOrder`

**变更说明**

- 【下单至物流平台】：沿用本接口，顺丰下单 **成功后直接 `status=completed`**，无需再手工改状态
- 【自行寄出】：不走本接口；在编辑/改状态中填写商家寄出单号（`OutboundTrackNoTypeEnum.MANUAL`）

#### 3.1.4 导出

**POST** `/afterSale/export`

**变更说明**

| Excel 列 | 说明 |
|----------|------|
| 平台名称 | **新增**，字段 `dictPlatformName` |
| 平台单号 | 原「订单编号」列，字段仍为 `platformCode` |

#### 3.1.5 进度节点

**GET** `/afterSale/getNodeList`

返回节点列表新增最后一项：

```json
{ "index": 6, "node": "completed", "nodeName": "已完成" }
```

---

### 3.2 售后装箱（PC / PDA）

#### 3.2.1 列表 / 详情 · 新增出参

涉及接口（PC `/afterSalePack/*`，PDA `/pda/afterSalePack/*`）：

- `POST .../paging`
- `GET .../view`
- `GET .../viewByCode`

**新增字段**

| 字段 | 类型 | 说明 |
|------|------|------|
| invalidStatus | boolean | false=未作废，true=已作废 |
| invalidStatusName | string | 作废状态名称 |
| invalidRemark | string | 作废原因 |
| invalidTime | datetime | 作废时间（若有） |

**PC 列表**：头部增加「作废」按钮；列表增加「作废状态」列。

#### 3.2.2 扫箱唛 · 行为变更

以下场景扫描 **已作废** 箱唛会失败（无需新接口，文档标注即可）：

- 采购退货扫箱唛  
- PDA 箱唛移仓  
- PDA/PC 装箱扫箱唛  

错误提示示例：`扫描箱唛失败，箱唛已被作废`（错误码见 [5. 错误码](#5-错误码)）。

---

### 3.3 采购退货（PC / PDA）

服务路径：**WMS** `/purchaseReturnOrder`

#### 3.3.1 写接口 · 新增入参

涉及：`POST .../add`、`/update`、`/addAndSubmit` 等

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| returnReasonType | string | 否 | `defect` / `other`，**不传默认 `defect`** |
| returnRemark | string | 条件 | `returnReasonType=other` 时填写具体原因 |

#### 3.3.2 读接口 · 新增出参

涉及：`POST .../paging`、`GET .../view` 等

| 字段 | 类型 | 说明 |
|------|------|------|
| returnReasonType | string | 原因类型 code |
| returnReasonTypeName | string | 原因类型名称 |
| returnReasonOther | string | 「其它」时的说明文案 |

> 历史数据：`return_reason_type` 由 SQL 迁移；含「瑕疵」归为 `defect`，其余为 `other`，原 `returnRemark` 保留。

---

### 3.4 PDA SKU 扫码（PLM）

**POST** `/product/detail/search/pdaSearchSku`

**请求体（节选）**

```json
{
  "skuNo": "扫描码（SKU / EAN / 客户SKU / 京东条码）"
}
```

**变更说明**

- 除 SKU 编码、EAN 外，支持 **客户 SKU（SKU 对照表）**
- **可能返回多条** `SkuVO`，前端需弹窗供用户选择后再继续装箱
- 单条仍返回数组 `[{ skuId, skuNo, ... }]`

**响应示例**

```json
{
  "code": 200,
  "data": [
    { "skuId": "id1", "skuNo": "1602", "productName": "..." },
    { "skuId": "id2", "skuNo": "1602-B", "productName": "..." }
  ]
}
```

---

### 3.5 系统配置 · 总览（可选）

**GET** `/cfgSetting/view`

`ViewDTO` 中可能包含 `poReturnDefaultOrgSettingDTO`（与 [2.5](#25-系统配置--采购退货默认采购组织pc) 一致），系统配置页维护默认采购组织时使用。

---

## 4. 枚举与字典

### 4.1 寄修单状态 `AfterSaleStatusEnum`

| code | nodeName |
|------|----------|
| approveIng | 客服审核 |
| toBeReturned | 待客户寄件 |
| afterSalesReceived | 待售后签收 |
| repair | 检测/维修中 |
| toBeShipped | 待商家寄出 |
| **completed** | **已完成** |
| terminated | 已终止 |

### 4.2 采购退货原因类型 `PoReturnReasonTypeEnum`

| code | name |
|------|------|
| defect | 瑕疵品 |
| other | 其它 |

### 4.3 商家寄出单号来源（寄修单内部字段，供排查）

| type | 说明 |
|------|------|
| API | 物流平台下单 |
| MANUAL | 自行寄出 / 手工填写 |

### 4.4 异步导出任务

| 事件码 | 说明 |
|--------|------|
| EXPORT_WMS_AFTER_SALE_PACK | 售后装箱导出 |
| EXPORT_DMP_AFTER_SALE | 寄修单导出（列变更见 3.1.4） |

---

## 5. 错误码

### 5.1 售后装箱 / 箱唛

| 错误码 | 文案 |
|--------|------|
| WH_AFTER_SALE_PACK_INVALID_STATUS | 箱唛【{0}】已作废 |
| WH_AFTER_SALE_PACK_VOID_NOT_EMPTY | 箱唛【{0}】非空箱，不能作废 |
| WH_AFTER_SALE_PACK_VOID_IN_USE | 箱唛【{0}】已被单据使用，不能作废 |
| WH_AFTER_SALE_PACK_SCAN_VOIDED | 扫描箱唛失败，箱唛已被作废 |

---

## 6. 不在本文档范围的接口

| 项 | 原因 |
|----|------|
| `POST feign/skuMapping/listByScanCode` | 服务间 Feign，PLM 内部调用 |
| `POST /feign/export/exportAfterSalePack` | file 服务导出回调 |
| `AfterSaleTrackSyncJob` | 运维定时任务，非页面接口 |
| `GET /afterSale/syncWdtToAfterSale` | 后台 WDT 同步，非前端常规调用 |
| 小程序：地址识别、粘贴单号、复制地址、卡片交互 | 无本次后端新接口 |
| 直接调拨单「批量填充」 | 本次未实现 |

---

## 附录：前端对接 Checklist

- [ ] PC 装箱：作废按钮 + 作废状态列 + `/afterSalePack/invalid`
- [ ] PC 装箱：导出 + `/afterSalePack/exportExcel`
- [ ] PDA 装箱：`/pda/afterSalePack/invalid`
- [ ] PC/PDA 扫箱：作废箱唛错误提示
- [ ] PC 寄修：状态机含 `completed`、列表物流状态列、导出列
- [ ] PC 寄修：物流下单成功即已完成；自行寄出走改状态/编辑单号
- [ ] PC/PDA 采购退货：`returnReasonType` 下拉 + 默认瑕疵品
- [ ] PDA 采购退货：选仓后调 `/pdaPoReturn/getDefaultOrg`
- [ ] PC 采购退货：默认组织（仓库 org + `/cfgSetting/getPoReturnDefaultOrgSetting`）
- [ ] PDA 装箱扫码：`pdaSearchSku` 多 SKU 选择
- [ ] 联调环境已执行 SQL + 配置轨迹同步 Job

---

*文档版本：26.7.3 · 与分支 `feature/20260720-26.7.3` 后端代码一致*

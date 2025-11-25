# 支持 JavaDoc 的 OpenApi 接口文档

> 本文档自动生成于: Wed Sep 24 15:57:15 CST 2025

## 📋 概述

本文档包含了所有使用 `@OpenApi` 注解的接口方法，包含详细的字段信息解析和智能描述生成。

**总接口数量**: 115

## 📑 目录

- **SampleOpenApi** (93 个接口)
- **TestOpenApi** (4 个接口)
- **WarehouseOpenApi** (5 个接口)
- **SoB2cOpenApi** (1 个接口)
- **ShopifyServerOpenApi** (1 个接口)
- **AfterSaleOpenApi** (11 个接口)

## 🏷️ SampleOpenApi

**类描述**: package com.erp.server.auth.controller.openapi; import com.common.business.dto.base.BaseDropDownDTO; import com.common.business.dto.base.BaseIdsDTO; import com.common.business.dto.base.BaseResultDTO; import com.common.business.dto.base.BatchResultDTO; import com.common.business.dto.base.PagingDTO; import com.common.business.dto.base.BaseSearchDTO; import com.common.business.dto.base.PermissionsDTO; import com.common.business.vo.PagingVO; import com.common.core.controller.vo.ApiResult; import com.erp.model.oms.dto.ExhibitionOrderDTO; import com.erp.model.oms.entity.DictBasicEntity; import com.erp.model.sys.dto.CfgQueryConditionDTO; import com.erp.model.sys.dto.DictCountryDTO; import com.erp.model.sys.dto.DictPartitionDTO; import com.erp.model.sys.dto.SysAccountingCompanyDTO; import com.erp.model.sys.dto.TypeAndValueDTO; import com.erp.model.sys.vo.SysDeptDropDownVO; import com.common.business.dto.FindUserDTO; import com.erp.model.wms.dto.SampleBackInfoDTO; import com.erp.model.wms.dto.SampleBorrowInfoDTO; import com.erp.model.wms.dto.SampleLedgerDTO; import com.erp.model.wms.dto.SampleLedgerFlowDTO; import com.erp.model.wms.dto.WarehouseDTO; import com.erp.model.wms.dto.SampleRecipientDTO; import com.erp.model.wms.dto.SampleReturnInfoDTO; import com.erp.model.wms.dto.SampleScrapInfoDTO; import com.erp.rpc.oms.feign.ExhibitionOrderFeign; import com.erp.rpc.oms.feign.OmsDropDownFeign; import com.erp.rpc.oms.feign.OmsFeign; import com.erp.rpc.sys.feign.CfgQueryConditionFeign; import com.erp.rpc.sys.feign.SysFeign; import com.erp.rpc.wms.feign.SampleFeign; import com.erp.rpc.wms.feign.WmsFeign; import com.erp.rpc.plm.feign.PlmFeign; import com.erp.rpc.scm.feign.ScmFeign; import com.erp.server.auth.config.OpenApi; import org.springframework.stereotype.Component; import javax.annotation.Resource; import javax.validation.Valid; import java.util.List;

**接口数量**: 93

### 1. exhibitionOrderGenerateDownstreamByExhibitionOrder

**方法名**: `exhibitionOrderGenerateDownstreamByExhibitionOrder`

**描述**: exhibitionOrderGenerateDownstreamByExhibitionOrder

**📥 请求参数**:

#### 参数: exhibitionOrderId

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `DownstreamDTO`

**泛型返回类型**: `com.erp.model.oms.dto.ExhibitionOrderDTO$DownstreamDTO`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `generateSoOutstockViewDTOList` | `List` | - | generatesooutstockviewdto列表 |
| `otherInstockAddDTO` | `AddDTO` | - | otherinstock添加dto |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ sourceType` | `String` | - | 类型 |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ sourceId` | `String` | - | ID |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ sourceCode` | `String` | - | 编码 |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ isProcess` | `Boolean` | - | 是否处理 |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ detailList` | `List` | @NotEmpty, @Valid | detail列表 |
| `soId` | `String` | - | ID |

**💡 调用示例**:

```json
{
  "method": "exhibitionOrderGenerateDownstreamByExhibitionOrder",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 2. exhibitionOrderListFreezeQtyBySku

**方法名**: `exhibitionOrderListFreezeQtyBySku`

**描述**: exhibitionOrderListFreezeQtyBySku

**📥 请求参数**:

#### 参数: dto

- **类型**: `SearchDTO`
- **泛型类型**: `com.erp.model.oms.dto.ExhibitionOrderDTO$SearchDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `childId` | `String` | - | ID |
| `skuIds` | `List` | - | ID |

**📤 返回类型**: `List`

**泛型返回类型**: `java.util.List<com.erp.model.oms.dto.ExhibitionOrderDTO$FreezeQtyBySku>`

**💡 调用示例**:

```json
{
  "method": "exhibitionOrderListFreezeQtyBySku",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 3. cfgQueryConditionGetQueryCondition

**方法名**: `cfgQueryConditionGetQueryCondition`

**描述**: cfgQueryConditionGetQueryCondition

**📥 请求参数**:

#### 参数: code

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.sys.dto.CfgQueryConditionDTO$ViewDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "cfgQueryConditionGetQueryCondition",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 4. omsDropDownListInternalSalesPlatform

**方法名**: `omsDropDownListInternalSalesPlatform`

**描述**: omsDropDownListInternalSalesPlatform

**📥 请求参数**:

#### 参数: key

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO$CommonDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "omsDropDownListInternalSalesPlatform",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 5. wmsWarehouseList

**方法名**: `wmsWarehouseList`

**描述**: wmsWarehouseList

**📥 请求参数**:

#### 参数: showByAuth

- **类型**: `Boolean`
- **泛型类型**: `java.lang.Boolean`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.WarehouseDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "wmsWarehouseList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 6. omsDropDownList

**方法名**: `omsDropDownList`

**描述**: omsDropDownList

**📥 请求参数**:

#### 参数: key

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO$CommonDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "omsDropDownList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 7. wmsDictList

**方法名**: `wmsDictList`

**描述**: wmsDictList

**📥 请求参数**:

#### 参数: key

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO$CommonDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "wmsDictList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 8. sampleLedgerView

**方法名**: `sampleLedgerView`

**描述**: sampleLedgerView

**📥 请求参数**:

#### 参数: id

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SampleLedgerDTO$ViewDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleLedgerView",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 9. omsDropDownTree

**方法名**: `omsDropDownTree`

**描述**: omsDropDownTree

**📥 请求参数**:

#### 参数: key

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `List`

**泛型返回类型**: `java.util.List<com.common.business.dto.base.BaseDropDownDTO$Tree>`

**💡 调用示例**:

```json
{
  "method": "omsDropDownTree",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 10. sysCompanyList

**方法名**: `sysCompanyList`

**描述**: sysCompanyList

**📥 请求参数**: 无

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.sys.dto.SysAccountingCompanyDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sysCompanyList",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 11. sampleRecipientSkuCost

**方法名**: `sampleRecipientSkuCost`

**描述**: sampleRecipientSkuCost

**📥 请求参数**:

#### 参数: dto

- **类型**: `SkuCostQueryDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleRecipientDTO$SkuCostQueryDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleRecipientDTO$SkuDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientSkuCost",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 12. sampleBorrowInfoInvalid

**方法名**: `sampleBorrowInfoInvalid`

**描述**: sampleBorrowInfoInvalid

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoInvalid",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 13. sampleRecipientApprove

**方法名**: `sampleRecipientApprove`

**描述**: sampleRecipientApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 14. sampleRecipientUpdate

**方法名**: `sampleRecipientUpdate`

**描述**: sampleRecipientUpdate

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleRecipientDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | @NotBlank | ID |
| `detailList` | `List` | - | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `invalidTime` | `LocalDateTime` | - | ID |
| `recipientDate` | `LocalDate` | @NotNull | recipient日期 |
| `usage` | `String` | @NotBlank | usage |
| `warehouseId` | `String` | @NotBlank, @Size | 仓库ID |
| `userId` | `String` | @NotBlank, @Size | 用户ID |
| `deptId` | `String` | @NotBlank, @Size | 部门ID |
| `pickOrgId` | `String` | @NotBlank, @Size | ID |
| `usageScope` | `String` | @NotBlank | usagescope |
| `useUserId` | `String` | @NotBlank, @Size | ID |
| `remark` | `String` | @Size | 备注 |
| `isDelivery` | `Boolean` | @NotNull | 是否delivery |
| `receiveAddress` | `String` | @Size | 地址 |
| `receiverName` | `String` | @Size | 名称 |
| `receivePhone` | `String` | @Size | 电话 |
| `skuTotalCost` | `BigDecimal` | @Digits | sku总计cost |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<?>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientUpdate",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 15. sampleBorrowInfoUpdate

**方法名**: `sampleBorrowInfoUpdate`

**描述**: sampleBorrowInfoUpdate

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleBorrowInfoDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | - | ID |
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `borrowUserId` | `String` | @NotBlank | ID |
| `borrowUserName` | `String` | - | 用户名称 |
| `borrowDeptId` | `String` | @NotBlank | ID |
| `borrowDeptName` | `String` | - | 部门名称 |
| `lendUserId` | `String` | @NotBlank | ID |
| `lendUserName` | `String` | - | 用户名称 |
| `lendDeptId` | `String` | @NotBlank | ID |
| `lendDeptName` | `String` | - | 部门名称 |
| `borrowDate` | `LocalDate` | @NotNull | borrow日期 |
| `estimatedReturnDate` | `LocalDate` | @NotNull | estimatedreturn日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<?>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoUpdate",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 16. sampleBorrowInfoTabList

**方法名**: `sampleBorrowInfoTabList`

**描述**: sampleBorrowInfoTabList

**📥 请求参数**:

#### 参数: param

- **类型**: `PermissionsDTO`
- **泛型类型**: `com.common.business.dto.base.PermissionsDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleBorrowInfoDTO$TabListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoTabList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 17. sampleRecipientAddAndSubmit

**方法名**: `sampleRecipientAddAndSubmit`

**描述**: sampleRecipientAddAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleRecipientDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | - | detail列表 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `invalidTime` | `LocalDateTime` | - | ID |
| `recipientDate` | `LocalDate` | @NotNull | recipient日期 |
| `usage` | `String` | @NotBlank | usage |
| `warehouseId` | `String` | @NotBlank, @Size | 仓库ID |
| `userId` | `String` | @NotBlank, @Size | 用户ID |
| `deptId` | `String` | @NotBlank, @Size | 部门ID |
| `pickOrgId` | `String` | @NotBlank, @Size | ID |
| `usageScope` | `String` | @NotBlank | usagescope |
| `useUserId` | `String` | @NotBlank, @Size | ID |
| `remark` | `String` | @Size | 备注 |
| `isDelivery` | `Boolean` | @NotNull | 是否delivery |
| `receiveAddress` | `String` | @Size | 地址 |
| `receiverName` | `String` | @Size | 名称 |
| `receivePhone` | `String` | @Size | 电话 |
| `skuTotalCost` | `BigDecimal` | @Digits | sku总计cost |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientAddAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 18. sampleRecipientListSku

**方法名**: `sampleRecipientListSku`

**描述**: sampleRecipientListSku

**📥 请求参数**:

#### 参数: dto

- **类型**: `SkuListQueryDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleRecipientDTO$SkuListQueryDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `searchKeyword` | `String` | - | 搜索keyword |
| `warehouseId` | `String` | - | 仓库ID |
| `advanceQueryDTOList` | `List` | - | advance查询dto列表 |
| `currPage` | `Integer` | - | currpage |
| `pageSize` | `Integer` | - | pagesize |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SampleRecipientDTO$SkuListResponseDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientListSku",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 19. sampleBackInfoAdd

**方法名**: `sampleBackInfoAdd`

**描述**: sampleBackInfoAdd

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleBackInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `backDate` | `LocalDate` | @NotNull | back日期 |
| `sourceId` | `String` | - | ID |
| `sourceCode` | `String` | - | 编码 |
| `sourceType` | `String` | - | 类型 |
| `userId` | `String` | @NotBlank | 用户ID |
| `userName` | `String` | - | 用户名称 |
| `deptId` | `String` | @NotBlank | 部门ID |
| `warehouseId` | `String` | @NotBlank | 仓库ID |
| `warehouseName` | `String` | - | 仓库名称 |
| `orgId` | `String` | @NotBlank | ID |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoAdd",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 20. sampleBorrowInfoSubmit

**方法名**: `sampleBorrowInfoSubmit`

**描述**: sampleBorrowInfoSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 21. sampleBackInfoUpdate

**方法名**: `sampleBackInfoUpdate`

**描述**: sampleBackInfoUpdate

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleBackInfoDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | @NotBlank | ID |
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `backDate` | `LocalDate` | @NotNull | back日期 |
| `sourceId` | `String` | - | ID |
| `sourceCode` | `String` | - | 编码 |
| `sourceType` | `String` | - | 类型 |
| `userId` | `String` | @NotBlank | 用户ID |
| `userName` | `String` | - | 用户名称 |
| `deptId` | `String` | @NotBlank | 部门ID |
| `warehouseId` | `String` | @NotBlank | 仓库ID |
| `warehouseName` | `String` | - | 仓库名称 |
| `orgId` | `String` | @NotBlank | ID |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<?>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoUpdate",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 22. sampleBackInfoPaging

**方法名**: `sampleBackInfoPaging`

**描述**: sampleBackInfoPaging

**📥 请求参数**:

#### 参数: dto

- **类型**: `PagingDTO`
- **泛型类型**: `com.common.business.dto.base.PagingDTO<com.erp.model.wms.dto.SampleBackInfoDTO$PagingParamDTO>`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `currPage` | `Integer` | @NotNull | currpage |
| `pageSize` | `Integer` | - | pagesize |
| `lastId` | `String` | - | ID |
| `params` | `Object` | @NotNull, @Valid | params |
| `orderBy` | `String` | - | 顺序人 |
| `isSearchCount` | `Boolean` | - | 是否搜索计数 |
| `SORT_SPLIT` | `String` | - | sort_split |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SampleBackInfoDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoPaging",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 23. sampleBackInfoAddAndSubmit

**方法名**: `sampleBackInfoAddAndSubmit`

**描述**: sampleBackInfoAddAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleBackInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `backDate` | `LocalDate` | @NotNull | back日期 |
| `sourceId` | `String` | - | ID |
| `sourceCode` | `String` | - | 编码 |
| `sourceType` | `String` | - | 类型 |
| `userId` | `String` | @NotBlank | 用户ID |
| `userName` | `String` | - | 用户名称 |
| `deptId` | `String` | @NotBlank | 部门ID |
| `warehouseId` | `String` | @NotBlank | 仓库ID |
| `warehouseName` | `String` | - | 仓库名称 |
| `orgId` | `String` | @NotBlank | ID |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoAddAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 24. sampleRecipientUpdateAndSubmit

**方法名**: `sampleRecipientUpdateAndSubmit`

**描述**: sampleRecipientUpdateAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleRecipientDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | @NotBlank | ID |
| `detailList` | `List` | - | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `invalidTime` | `LocalDateTime` | - | ID |
| `recipientDate` | `LocalDate` | @NotNull | recipient日期 |
| `usage` | `String` | @NotBlank | usage |
| `warehouseId` | `String` | @NotBlank, @Size | 仓库ID |
| `userId` | `String` | @NotBlank, @Size | 用户ID |
| `deptId` | `String` | @NotBlank, @Size | 部门ID |
| `pickOrgId` | `String` | @NotBlank, @Size | ID |
| `usageScope` | `String` | @NotBlank | usagescope |
| `useUserId` | `String` | @NotBlank, @Size | ID |
| `remark` | `String` | @Size | 备注 |
| `isDelivery` | `Boolean` | @NotNull | 是否delivery |
| `receiveAddress` | `String` | @Size | 地址 |
| `receiverName` | `String` | @Size | 名称 |
| `receivePhone` | `String` | @Size | 电话 |
| `skuTotalCost` | `BigDecimal` | @Digits | sku总计cost |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.Void>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientUpdateAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 25. sampleBorrowInfoApprove

**方法名**: `sampleBorrowInfoApprove`

**描述**: sampleBorrowInfoApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 26. sampleBorrowInfoView

**方法名**: `sampleBorrowInfoView`

**描述**: sampleBorrowInfoView

**📥 请求参数**:

#### 参数: id

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SampleBorrowInfoDTO$ViewDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoView",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 27. sampleRecipientFinishRecipient

**方法名**: `sampleRecipientFinishRecipient`

**描述**: sampleRecipientFinishRecipient

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientFinishRecipient",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 28. sampleBorrowInfoAddAndSubmit

**方法名**: `sampleBorrowInfoAddAndSubmit`

**描述**: sampleBorrowInfoAddAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleBorrowInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `borrowUserId` | `String` | @NotBlank | ID |
| `borrowUserName` | `String` | - | 用户名称 |
| `borrowDeptId` | `String` | @NotBlank | ID |
| `borrowDeptName` | `String` | - | 部门名称 |
| `lendUserId` | `String` | @NotBlank | ID |
| `lendUserName` | `String` | - | 用户名称 |
| `lendDeptId` | `String` | @NotBlank | ID |
| `lendDeptName` | `String` | - | 部门名称 |
| `borrowDate` | `LocalDate` | @NotNull | borrow日期 |
| `estimatedReturnDate` | `LocalDate` | @NotNull | estimatedreturn日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoAddAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 29. sampleRecipientView

**方法名**: `sampleRecipientView`

**描述**: sampleRecipientView

**📥 请求参数**:

#### 参数: id

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SampleRecipientDTO$ViewDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientView",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 30. sampleBorrowInfoAdd

**方法名**: `sampleBorrowInfoAdd`

**描述**: @Resource private SampleFeign sampleFeign; @Resource private ExhibitionOrderFeign exhibitionOrderFeign; @Resource private OmsDropDownFeign omsDropDownFeign; @Resource private CfgQueryConditionFeign cfgQueryConditionFeign; @Resource private SysFeign sysFeign; @Resource private WmsFeign wmsFeign; @Resource private PlmFeign plmFeign; @Resource private OmsFeign omsFeign; @Resource private ScmFeign scmFeign; // ==================== 样品借用单相关接口 ====================

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleBorrowInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `borrowUserId` | `String` | @NotBlank | ID |
| `borrowUserName` | `String` | - | 用户名称 |
| `borrowDeptId` | `String` | @NotBlank | ID |
| `borrowDeptName` | `String` | - | 部门名称 |
| `lendUserId` | `String` | @NotBlank | ID |
| `lendUserName` | `String` | - | 用户名称 |
| `lendDeptId` | `String` | @NotBlank | ID |
| `lendDeptName` | `String` | - | 部门名称 |
| `borrowDate` | `LocalDate` | @NotNull | borrow日期 |
| `estimatedReturnDate` | `LocalDate` | @NotNull | estimatedreturn日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoAdd",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 31. sampleRecipientTabList

**方法名**: `sampleRecipientTabList`

**描述**: sampleRecipientTabList

**📥 请求参数**:

#### 参数: param

- **类型**: `PermissionsDTO`
- **泛型类型**: `com.common.business.dto.base.PermissionsDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleRecipientDTO$TabListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientTabList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 32. sampleBackInfoTabList

**方法名**: `sampleBackInfoTabList`

**描述**: sampleBackInfoTabList

**📥 请求参数**:

#### 参数: param

- **类型**: `PermissionsDTO`
- **泛型类型**: `com.common.business.dto.base.PermissionsDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleBackInfoDTO$TabListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoTabList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 33. sampleBackInfoUpdateAndSubmit

**方法名**: `sampleBackInfoUpdateAndSubmit`

**描述**: sampleBackInfoUpdateAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleBackInfoDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | @NotBlank | ID |
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `backDate` | `LocalDate` | @NotNull | back日期 |
| `sourceId` | `String` | - | ID |
| `sourceCode` | `String` | - | 编码 |
| `sourceType` | `String` | - | 类型 |
| `userId` | `String` | @NotBlank | 用户ID |
| `userName` | `String` | - | 用户名称 |
| `deptId` | `String` | @NotBlank | 部门ID |
| `warehouseId` | `String` | @NotBlank | 仓库ID |
| `warehouseName` | `String` | - | 仓库名称 |
| `orgId` | `String` | @NotBlank | ID |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.Void>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoUpdateAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 34. sampleBackInfoSubmit

**方法名**: `sampleBackInfoSubmit`

**描述**: sampleBackInfoSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 35. sampleBackInfoApprove

**方法名**: `sampleBackInfoApprove`

**描述**: sampleBackInfoApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 36. sampleBackInfoDisApprove

**方法名**: `sampleBackInfoDisApprove`

**描述**: sampleBackInfoDisApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoDisApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 37. sampleBackInfoDelete

**方法名**: `sampleBackInfoDelete`

**描述**: sampleBackInfoDelete

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoDelete",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 38. sampleBorrowInfoDelete

**方法名**: `sampleBorrowInfoDelete`

**描述**: sampleBorrowInfoDelete

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoDelete",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 39. sampleBorrowInfoFinishBorrow

**方法名**: `sampleBorrowInfoFinishBorrow`

**描述**: sampleBorrowInfoFinishBorrow

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoFinishBorrow",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 40. sampleRecipientSubmit

**方法名**: `sampleRecipientSubmit`

**描述**: sampleRecipientSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 41. sampleBorrowInfoPaging

**方法名**: `sampleBorrowInfoPaging`

**描述**: sampleBorrowInfoPaging

**📥 请求参数**:

#### 参数: dto

- **类型**: `PagingDTO`
- **泛型类型**: `com.common.business.dto.base.PagingDTO<com.erp.model.wms.dto.SampleBorrowInfoDTO$PagingParamDTO>`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `currPage` | `Integer` | @NotNull | currpage |
| `pageSize` | `Integer` | - | pagesize |
| `lastId` | `String` | - | ID |
| `params` | `Object` | @NotNull, @Valid | params |
| `orderBy` | `String` | - | 顺序人 |
| `isSearchCount` | `Boolean` | - | 是否搜索计数 |
| `SORT_SPLIT` | `String` | - | sort_split |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SampleBorrowInfoDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoPaging",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 42. sampleRecipientDisApprove

**方法名**: `sampleRecipientDisApprove`

**描述**: sampleRecipientDisApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientDisApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 43. sampleRecipientInvalid

**方法名**: `sampleRecipientInvalid`

**描述**: sampleRecipientInvalid

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientInvalid",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 44. sampleRecipientCancelProcess

**方法名**: `sampleRecipientCancelProcess`

**描述**: sampleRecipientCancelProcess

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientCancelProcess",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 45. sampleBorrowInfoUpdateAndSubmit

**方法名**: `sampleBorrowInfoUpdateAndSubmit`

**描述**: sampleBorrowInfoUpdateAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleBorrowInfoDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | - | ID |
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `borrowUserId` | `String` | @NotBlank | ID |
| `borrowUserName` | `String` | - | 用户名称 |
| `borrowDeptId` | `String` | @NotBlank | ID |
| `borrowDeptName` | `String` | - | 部门名称 |
| `lendUserId` | `String` | @NotBlank | ID |
| `lendUserName` | `String` | - | 用户名称 |
| `lendDeptId` | `String` | @NotBlank | ID |
| `lendDeptName` | `String` | - | 部门名称 |
| `borrowDate` | `LocalDate` | @NotNull | borrow日期 |
| `estimatedReturnDate` | `LocalDate` | @NotNull | estimatedreturn日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.Void>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoUpdateAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 46. sampleRecipientAdd

**方法名**: `sampleRecipientAdd`

**描述**: sampleRecipientAdd

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleRecipientDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | - | detail列表 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `invalidTime` | `LocalDateTime` | - | ID |
| `recipientDate` | `LocalDate` | @NotNull | recipient日期 |
| `usage` | `String` | @NotBlank | usage |
| `warehouseId` | `String` | @NotBlank, @Size | 仓库ID |
| `userId` | `String` | @NotBlank, @Size | 用户ID |
| `deptId` | `String` | @NotBlank, @Size | 部门ID |
| `pickOrgId` | `String` | @NotBlank, @Size | ID |
| `usageScope` | `String` | @NotBlank | usagescope |
| `useUserId` | `String` | @NotBlank, @Size | ID |
| `remark` | `String` | @Size | 备注 |
| `isDelivery` | `Boolean` | @NotNull | 是否delivery |
| `receiveAddress` | `String` | @Size | 地址 |
| `receiverName` | `String` | @Size | 名称 |
| `receivePhone` | `String` | @Size | 电话 |
| `skuTotalCost` | `BigDecimal` | @Digits | sku总计cost |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientAdd",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 47. sampleBorrowInfoDisApprove

**方法名**: `sampleBorrowInfoDisApprove`

**描述**: sampleBorrowInfoDisApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoDisApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 48. sampleBorrowInfoCancelProcess

**方法名**: `sampleBorrowInfoCancelProcess`

**描述**: sampleBorrowInfoCancelProcess

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBorrowInfoCancelProcess",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 49. sampleRecipientPaging

**方法名**: `sampleRecipientPaging`

**描述**: sampleRecipientPaging

**📥 请求参数**:

#### 参数: dto

- **类型**: `PagingDTO`
- **泛型类型**: `com.common.business.dto.base.PagingDTO<com.erp.model.wms.dto.SampleRecipientDTO$PagingParamDTO>`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `currPage` | `Integer` | @NotNull | currpage |
| `pageSize` | `Integer` | - | pagesize |
| `lastId` | `String` | - | ID |
| `params` | `Object` | @NotNull, @Valid | params |
| `orderBy` | `String` | - | 顺序人 |
| `isSearchCount` | `Boolean` | - | 是否搜索计数 |
| `SORT_SPLIT` | `String` | - | sort_split |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SampleRecipientDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientPaging",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 50. sampleRecipientDelete

**方法名**: `sampleRecipientDelete`

**描述**: sampleRecipientDelete

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientDelete",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 51. sampleRecipientSkuAvailableStock

**方法名**: `sampleRecipientSkuAvailableStock`

**描述**: sampleRecipientSkuAvailableStock

**📥 请求参数**:

#### 参数: dto

- **类型**: `SkuAvailableStockQueryDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleRecipientDTO$SkuAvailableStockQueryDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleRecipientDTO$SkuAvailableStockDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleRecipientSkuAvailableStock",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 52. sampleReturnInfoInvalid

**方法名**: `sampleReturnInfoInvalid`

**描述**: sampleReturnInfoInvalid

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoInvalid",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 53. sampleReturnInfoCancelProcess

**方法名**: `sampleReturnInfoCancelProcess`

**描述**: sampleReturnInfoCancelProcess

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoCancelProcess",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 54. sampleReturnInfoUpdateAndSubmit

**方法名**: `sampleReturnInfoUpdateAndSubmit`

**描述**: sampleReturnInfoUpdateAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleReturnInfoDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | - | ID |
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `sourceId` | `String` | @NotBlank | ID |
| `sourceCode` | `String` | - | 编码 |
| `sourceType` | `String` | - | 类型 |
| `returnUserId` | `String` | @NotBlank | ID |
| `returnUserName` | `String` | - | 用户名称 |
| `returnDeptId` | `String` | @NotBlank | ID |
| `returnDeptName` | `String` | - | 部门名称 |
| `receiverUserId` | `String` | @NotBlank | ID |
| `receiverUserName` | `String` | - | 用户名称 |
| `receiverDeptId` | `String` | @NotBlank | ID |
| `receiverDeptName` | `String` | - | 部门名称 |
| `returnDate` | `LocalDate` | @NotNull | return日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.Void>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoUpdateAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 55. sampleBackInfoInvalid

**方法名**: `sampleBackInfoInvalid`

**描述**: sampleBackInfoInvalid

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoInvalid",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 56. sampleScrapInfoUpdate

**方法名**: `sampleScrapInfoUpdate`

**描述**: sampleScrapInfoUpdate

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleScrapInfoDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | @NotBlank | ID |
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `scrapUserId` | `String` | @NotBlank | ID |
| `scrapUserName` | `String` | - | 用户名称 |
| `scrapDeptId` | `String` | @NotBlank | ID |
| `scrapDeptName` | `String` | - | 部门名称 |
| `scrapDate` | `LocalDate` | @NotNull | scrap日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<?>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoUpdate",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 57. sampleReturnInfoSubmit

**方法名**: `sampleReturnInfoSubmit`

**描述**: sampleReturnInfoSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 58. sampleScrapInfoAdd

**方法名**: `sampleScrapInfoAdd`

**描述**: sampleScrapInfoAdd

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleScrapInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `scrapUserId` | `String` | @NotBlank | ID |
| `scrapUserName` | `String` | - | 用户名称 |
| `scrapDeptId` | `String` | @NotBlank | ID |
| `scrapDeptName` | `String` | - | 部门名称 |
| `scrapDate` | `LocalDate` | @NotNull | scrap日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoAdd",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 59. sampleReturnInfoAddAndSubmit

**方法名**: `sampleReturnInfoAddAndSubmit`

**描述**: sampleReturnInfoAddAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleReturnInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `sourceId` | `String` | @NotBlank | ID |
| `sourceCode` | `String` | - | 编码 |
| `sourceType` | `String` | - | 类型 |
| `returnUserId` | `String` | @NotBlank | ID |
| `returnUserName` | `String` | - | 用户名称 |
| `returnDeptId` | `String` | @NotBlank | ID |
| `returnDeptName` | `String` | - | 部门名称 |
| `receiverUserId` | `String` | @NotBlank | ID |
| `receiverUserName` | `String` | - | 用户名称 |
| `receiverDeptId` | `String` | @NotBlank | ID |
| `receiverDeptName` | `String` | - | 部门名称 |
| `returnDate` | `LocalDate` | @NotNull | return日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoAddAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 60. sampleScrapInfoInvalid

**方法名**: `sampleScrapInfoInvalid`

**描述**: sampleScrapInfoInvalid

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoInvalid",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 61. sampleLedgerListSku

**方法名**: `sampleLedgerListSku`

**描述**: sampleLedgerListSku

**📥 请求参数**:

#### 参数: pagingDTO

- **类型**: `PagingDTO`
- **泛型类型**: `com.common.business.dto.base.PagingDTO<com.erp.model.wms.dto.SampleLedgerDTO$SearchDTO>`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `currPage` | `Integer` | @NotNull | currpage |
| `pageSize` | `Integer` | - | pagesize |
| `lastId` | `String` | - | ID |
| `params` | `Object` | @NotNull, @Valid | params |
| `orderBy` | `String` | - | 顺序人 |
| `isSearchCount` | `Boolean` | - | 是否搜索计数 |
| `SORT_SPLIT` | `String` | - | sort_split |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SampleLedgerDTO$SkuAvailableQtyDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleLedgerListSku",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 62. sysDictPartitionDropDown

**方法名**: `sysDictPartitionDropDown`

**描述**: sysDictPartitionDropDown

**📥 请求参数**:

#### 参数: dto

- **类型**: `SelectDTO`
- **泛型类型**: `com.erp.model.sys.dto.DictPartitionDTO$SelectDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `searchKeyword` | `String` | - | 搜索keyword |
| `disabled` | `Boolean` | - | disabled |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.sys.dto.DictPartitionDTO$DictDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sysDictPartitionDropDown",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 63. wmsDropDownApproveStatusList

**方法名**: `wmsDropDownApproveStatusList`

**描述**: wmsDropDownApproveStatusList

**📥 请求参数**: 无

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO$CommonDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "wmsDropDownApproveStatusList",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 64. sysDepartmentDropDown

**方法名**: `sysDepartmentDropDown`

**描述**: sysDepartmentDropDown

**📥 请求参数**: 无

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.sys.vo.SysDeptDropDownVO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sysDepartmentDropDown",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 65. omsDropDownGetByTypeAndValue

**方法名**: `omsDropDownGetByTypeAndValue`

**描述**: omsDropDownGetByTypeAndValue

**📥 请求参数**:

#### 参数: dto

- **类型**: `TypeAndValueDTO`
- **泛型类型**: `com.erp.model.sys.dto.TypeAndValueDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `type` | `String` | - | 类型 |
| `value` | `String` | - | value |

**📤 返回类型**: `DictBasicEntity`

**泛型返回类型**: `com.erp.model.oms.entity.DictBasicEntity`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @TableField | 备注 |
| `value` | `String` | @TableField | value |
| `type` | `String` | @TableField | 类型 |
| `subType` | `String` | @TableField | 类型 |
| `name` | `String` | @TableField | 名称 |
| `status` | `Boolean` | @TableField | 状态 |
| `sort` | `Integer` | @TableField | 排序 |
| `REMARK` | `String` | - | 备注 |
| `VALUE` | `String` | - | value |
| `TYPE` | `String` | - | 类型 |
| `NAME` | `String` | - | 名称 |
| `STATUS` | `String` | - | 状态 |
| `SORT` | `String` | - | sort |
| `id` | `String` | @TableId | ID |
| `createUserId` | `String` | @TableField | ID |
| `createUserName` | `String` | @TableField | 用户名称 |
| `createTime` | `LocalDateTime` | @TableField | 创建时间 |
| `updateUserId` | `String` | @TableField | ID |
| `updateUserName` | `String` | @TableField | 用户名称 |
| `updateTime` | `LocalDateTime` | @TableField | 更新时间 |
| `version` | `Integer` | @Version | 版本号 |
| `isDeleted` | `Boolean` | @TableField, @TableLogic | 是否deleted |
| `FIELD_ID` | `String` | - | ID |
| `CREATE_TIME` | `String` | - | create_time |
| `UPDATE_TIME` | `String` | - | update_time |
| `FIELD_VERSION` | `String` | - | 版本号 |
| `IS_DELETED` | `String` | - | is_deleted |
| `UPDATE_USER_ID` | `String` | - | ID |
| `UPDATE_USER_NAME` | `String` | - | 名称 |
| `serialVersionUID` | `long` | - | ID |
| `log` | `Log` | - | log |

**💡 调用示例**:

```json
{
  "method": "omsDropDownGetByTypeAndValue",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 66. sampleReturnInfoPaging

**方法名**: `sampleReturnInfoPaging`

**描述**: sampleReturnInfoPaging

**📥 请求参数**:

#### 参数: dto

- **类型**: `PagingDTO`
- **泛型类型**: `com.common.business.dto.base.PagingDTO<com.erp.model.wms.dto.SampleReturnInfoDTO$PagingParamDTO>`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `currPage` | `Integer` | @NotNull | currpage |
| `pageSize` | `Integer` | - | pagesize |
| `lastId` | `String` | - | ID |
| `params` | `Object` | @NotNull, @Valid | params |
| `orderBy` | `String` | - | 顺序人 |
| `isSearchCount` | `Boolean` | - | 是否搜索计数 |
| `SORT_SPLIT` | `String` | - | sort_split |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SampleReturnInfoDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoPaging",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 67. sampleReturnInfoView

**方法名**: `sampleReturnInfoView`

**描述**: sampleReturnInfoView

**📥 请求参数**:

#### 参数: id

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SampleReturnInfoDTO$ViewDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoView",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 68. scmDropDownApproveStatusList

**方法名**: `scmDropDownApproveStatusList`

**描述**: scmDropDownApproveStatusList

**📥 请求参数**: 无

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO$CommonDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "scmDropDownApproveStatusList",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 69. sampleReturnInfoDisApprove

**方法名**: `sampleReturnInfoDisApprove`

**描述**: sampleReturnInfoDisApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoDisApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 70. sampleScrapInfoApprove

**方法名**: `sampleScrapInfoApprove`

**描述**: sampleScrapInfoApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 71. sampleBackInfoView

**方法名**: `sampleBackInfoView`

**描述**: sampleBackInfoView

**📥 请求参数**:

#### 参数: id

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SampleBackInfoDTO$ViewDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoView",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 72. sampleScrapInfoView

**方法名**: `sampleScrapInfoView`

**描述**: sampleScrapInfoView

**📥 请求参数**:

#### 参数: id

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SampleScrapInfoDTO$ViewDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoView",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 73. plmCommonFindUserList

**方法名**: `plmCommonFindUserList`

**描述**: plmCommonFindUserList

**📥 请求参数**:

#### 参数: dto

- **类型**: `BaseSearchDTO`
- **泛型类型**: `com.common.business.dto.base.BaseSearchDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `searchKeyword` | `String` | - | 搜索keyword |
| `flagId` | `String` | - | ID |
| `categoryId` | `String` | - | ID |
| `state` | `Integer` | - | state |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.FindUserDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "plmCommonFindUserList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 74. sampleLedgerTabList

**方法名**: `sampleLedgerTabList`

**描述**: sampleLedgerTabList

**📥 请求参数**:

#### 参数: param

- **类型**: `PermissionsDTO`
- **泛型类型**: `com.common.business.dto.base.PermissionsDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleLedgerDTO$TabListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleLedgerTabList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 75. sampleReturnInfoDelete

**方法名**: `sampleReturnInfoDelete`

**描述**: sampleReturnInfoDelete

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoDelete",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 76. sampleLedgerFlowList

**方法名**: `sampleLedgerFlowList`

**描述**: sampleLedgerFlowList

**📥 请求参数**:

#### 参数: param

- **类型**: `PermissionsDTO`
- **泛型类型**: `com.common.business.dto.base.PermissionsDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleLedgerFlowDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleLedgerFlowList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 77. sampleScrapInfoPaging

**方法名**: `sampleScrapInfoPaging`

**描述**: sampleScrapInfoPaging

**📥 请求参数**:

#### 参数: dto

- **类型**: `PagingDTO`
- **泛型类型**: `com.common.business.dto.base.PagingDTO<com.erp.model.wms.dto.SampleScrapInfoDTO$PagingParamDTO>`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `currPage` | `Integer` | @NotNull | currpage |
| `pageSize` | `Integer` | - | pagesize |
| `lastId` | `String` | - | ID |
| `params` | `Object` | @NotNull, @Valid | params |
| `orderBy` | `String` | - | 顺序人 |
| `isSearchCount` | `Boolean` | - | 是否搜索计数 |
| `SORT_SPLIT` | `String` | - | sort_split |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SampleScrapInfoDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoPaging",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 78. sampleReturnInfoApprove

**方法名**: `sampleReturnInfoApprove`

**描述**: sampleReturnInfoApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 79. sampleScrapInfoCancelProcess

**方法名**: `sampleScrapInfoCancelProcess`

**描述**: sampleScrapInfoCancelProcess

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoCancelProcess",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 80. sampleLedgerPaging

**方法名**: `sampleLedgerPaging`

**描述**: sampleLedgerPaging

**📥 请求参数**:

#### 参数: dto

- **类型**: `PagingDTO`
- **泛型类型**: `com.common.business.dto.base.PagingDTO<com.erp.model.wms.dto.SampleLedgerDTO$PagingParamDTO>`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `currPage` | `Integer` | @NotNull | currpage |
| `pageSize` | `Integer` | - | pagesize |
| `lastId` | `String` | - | ID |
| `params` | `Object` | @NotNull, @Valid | params |
| `orderBy` | `String` | - | 顺序人 |
| `isSearchCount` | `Boolean` | - | 是否搜索计数 |
| `SORT_SPLIT` | `String` | - | sort_split |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.wms.dto.SampleLedgerDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleLedgerPaging",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 81. sampleScrapInfoAddAndSubmit

**方法名**: `sampleScrapInfoAddAndSubmit`

**描述**: sampleScrapInfoAddAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleScrapInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `scrapUserId` | `String` | @NotBlank | ID |
| `scrapUserName` | `String` | - | 用户名称 |
| `scrapDeptId` | `String` | @NotBlank | ID |
| `scrapDeptName` | `String` | - | 部门名称 |
| `scrapDate` | `LocalDate` | @NotNull | scrap日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoAddAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 82. sampleScrapInfoUpdateAndSubmit

**方法名**: `sampleScrapInfoUpdateAndSubmit`

**描述**: sampleScrapInfoUpdateAndSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleScrapInfoDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | @NotBlank | ID |
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `scrapUserId` | `String` | @NotBlank | ID |
| `scrapUserName` | `String` | - | 用户名称 |
| `scrapDeptId` | `String` | @NotBlank | ID |
| `scrapDeptName` | `String` | - | 部门名称 |
| `scrapDate` | `LocalDate` | @NotNull | scrap日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.Void>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoUpdateAndSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 83. sampleScrapInfoTabList

**方法名**: `sampleScrapInfoTabList`

**描述**: sampleScrapInfoTabList

**📥 请求参数**:

#### 参数: param

- **类型**: `PermissionsDTO`
- **泛型类型**: `com.common.business.dto.base.PermissionsDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleScrapInfoDTO$TabListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoTabList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 84. sampleReturnInfoAdd

**方法名**: `sampleReturnInfoAdd`

**描述**: sampleReturnInfoAdd

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleReturnInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `sourceId` | `String` | @NotBlank | ID |
| `sourceCode` | `String` | - | 编码 |
| `sourceType` | `String` | - | 类型 |
| `returnUserId` | `String` | @NotBlank | ID |
| `returnUserName` | `String` | - | 用户名称 |
| `returnDeptId` | `String` | @NotBlank | ID |
| `returnDeptName` | `String` | - | 部门名称 |
| `receiverUserId` | `String` | @NotBlank | ID |
| `receiverUserName` | `String` | - | 用户名称 |
| `receiverDeptId` | `String` | @NotBlank | ID |
| `receiverDeptName` | `String` | - | 部门名称 |
| `returnDate` | `LocalDate` | @NotNull | return日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoAdd",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 85. sampleReturnInfoTabList

**方法名**: `sampleReturnInfoTabList`

**描述**: sampleReturnInfoTabList

**📥 请求参数**:

#### 参数: param

- **类型**: `PermissionsDTO`
- **泛型类型**: `com.common.business.dto.base.PermissionsDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.wms.dto.SampleReturnInfoDTO$TabListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoTabList",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 86. sampleScrapInfoSubmit

**方法名**: `sampleScrapInfoSubmit`

**描述**: sampleScrapInfoSubmit

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoSubmit",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 87. sampleLedgerFlowDetail

**方法名**: `sampleLedgerFlowDetail`

**描述**: sampleLedgerFlowDetail

**📥 请求参数**:

#### 参数: id

- **类型**: `String`
- **泛型类型**: `java.lang.String`
**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.wms.dto.SampleLedgerFlowDTO$ViewDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleLedgerFlowDetail",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 88. sysDictCountryList

**方法名**: `sysDictCountryList`

**描述**: sysDictCountryList

**📥 请求参数**: 无

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.sys.dto.DictCountryDTO$ListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sysDictCountryList",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 89. sampleReturnInfoUpdate

**方法名**: `sampleReturnInfoUpdate`

**描述**: sampleReturnInfoUpdate

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateDTO`
- **泛型类型**: `com.erp.model.wms.dto.SampleReturnInfoDTO$UpdateDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | - | ID |
| `detailList` | `List` | @NotEmpty | detail列表 |
| `clientType` | `ClientTypeEnum` | - | 类型 |
| `sourceId` | `String` | @NotBlank | ID |
| `sourceCode` | `String` | - | 编码 |
| `sourceType` | `String` | - | 类型 |
| `returnUserId` | `String` | @NotBlank | ID |
| `returnUserName` | `String` | - | 用户名称 |
| `returnDeptId` | `String` | @NotBlank | ID |
| `returnDeptName` | `String` | - | 部门名称 |
| `receiverUserId` | `String` | @NotBlank | ID |
| `receiverUserName` | `String` | - | 用户名称 |
| `receiverDeptId` | `String` | @NotBlank | ID |
| `receiverDeptName` | `String` | - | 部门名称 |
| `returnDate` | `LocalDate` | @NotNull | return日期 |
| `remark` | `String` | @Size | 备注 |
| `attachmentNameList` | `List` | - | 名称 |
| `attachmentUrlList` | `List` | - | attachmenturl列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<?>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleReturnInfoUpdate",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 90. sampleScrapInfoDisApprove

**方法名**: `sampleScrapInfoDisApprove`

**描述**: sampleScrapInfoDisApprove

**📥 请求参数**:

#### 参数: dto

- **类型**: `RemarkDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$RemarkDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `remark` | `String` | @NotBlank, @Size | 备注 |
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoDisApprove",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 91. sampleScrapInfoDelete

**方法名**: `sampleScrapInfoDelete`

**描述**: sampleScrapInfoDelete

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleScrapInfoDelete",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 92. omsCustomerListEnable

**方法名**: `omsCustomerListEnable`

**描述**: omsCustomerListEnable

**📥 请求参数**:

#### 参数: dto

- **类型**: `PermissionsDTO`
- **泛型类型**: `com.common.business.dto.base.PermissionsDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO$CommonDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "omsCustomerListEnable",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 93. sampleBackInfoCancelProcess

**方法名**: `sampleBackInfoCancelProcess`

**描述**: sampleBackInfoCancelProcess

**📥 请求参数**:

#### 参数: dto

- **类型**: `IdsDTO`
- **泛型类型**: `com.common.business.dto.base.BaseIdsDTO$IdsDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `ids` | `List` | @NotEmpty | ID |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BatchResultDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "sampleBackInfoCancelProcess",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

## 🏷️ TestOpenApi

**类描述**: TestOpenApi

**接口数量**: 4

### 1. test

**方法名**: `test`

**描述**: test

**📥 请求参数**:

#### 参数: req

- **类型**: `OpenApiReqDTO`
- **泛型类型**: `com.erp.model.sys.dto.OpenApiReqDTO`
- **注解**: @Validated

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `timestamp` | `Long` | @NotNull | 时间戳 |
| `signType` | `String` | @NotBlank | 签名类型 |
| `sign` | `String` | @NotBlank | 签名 |
| `version` | `String` | @NotBlank | 版本号 |
| `method` | `String` | @NotBlank | 方法名 |
| `charset` | `String` | @NotBlank | 编码格式 |
| `data` | `String` | - | 业务数据 |

**📤 返回类型**: `OpenApiReqDTO`

**泛型返回类型**: `com.erp.model.sys.dto.OpenApiReqDTO`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `timestamp` | `Long` | @NotNull | 时间戳 |
| `signType` | `String` | @NotBlank | 签名类型 |
| `sign` | `String` | @NotBlank | 签名 |
| `version` | `String` | @NotBlank | 版本号 |
| `method` | `String` | @NotBlank | 方法名 |
| `charset` | `String` | @NotBlank | 编码格式 |
| `data` | `String` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "test",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 2. testDto

**方法名**: `testDto`

**描述**: @OpenApi("testString") public String testString() { return "测试返回String"; }

**📥 请求参数**: 无

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "testDto",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 3. testVoid

**方法名**: `testVoid`

**描述**: testVoid

**📥 请求参数**: 无

**📤 返回类型**: `void`

**💡 调用示例**:

```json
{
  "method": "testVoid",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 4. testString

**方法名**: `testString`

**描述**: @OpenApi("testVoid") public void testVoid() { log.debug("测试testVoid"); }

**📥 请求参数**: 无

**📤 返回类型**: `String`

**泛型返回类型**: `java.lang.String`

**💡 调用示例**:

```json
{
  "method": "testString",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

## 🏷️ WarehouseOpenApi

**类描述**: package com.erp.server.auth.controller.openapi; import javax.annotation.Resource; import javax.validation.Valid; import com.common.core.controller.vo.ApiResult; import com.erp.model.sys.openapi.CollectorPacksDTO; import com.erp.model.sys.openapi.DimensionalWeightDTO; import com.erp.model.sys.openapi.ReturnTrackingDTO; import com.erp.rpc.plm.feign.PlmTaskFeign; import com.erp.rpc.wms.feign.PackingTaskFeign; import com.erp.rpc.wms.feign.SoB2cDeliveryFeign; import com.erp.server.auth.config.OpenApi;

**接口数量**: 5

### 1. dimensionalWeightPackage

**方法名**: `dimensionalWeightPackage`

**描述**: @OpenApi("dimensionalWeightMeasure") public ApiResult<String> dimensionalWeightMeasure(@Valid DimensionalWeightDTO dto) { String result = plmTaskFeign.dimensionalWeightMeasure(dto); return ApiResult.success(result); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `CollectorPacksDTO`
- **泛型类型**: `com.erp.model.sys.openapi.CollectorPacksDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `barCode` | `String` | @NotBlank | 编码 |
| `operatorId` | `String` | @NotBlank | ID |
| `imageUrl` | `String` | @NotBlank | imageurl |
| `videoUrl` | `String` | - | ID |
| `warehouseId` | `String` | - | 仓库ID |
| `packageNo` | `String` | - | package否 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.String>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "dimensionalWeightPackage",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 2. dimensionalWeightReturn

**方法名**: `dimensionalWeightReturn`

**描述**: @OpenApi("dimensionalWeightPackage") public ApiResult<String> dimensionalWeightPackage(@Valid CollectorPacksDTO dto) { return ApiResult.success(""); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `ReturnTrackingDTO`
- **泛型类型**: `com.erp.model.sys.openapi.ReturnTrackingDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `barCode` | `String` | @NotBlank | 编码 |
| `operatorId` | `String` | @NotBlank | ID |
| `imageUrl` | `String` | @NotBlank | imageurl |
| `videoUrl` | `String` | - | ID |
| `warehouseId` | `String` | - | 仓库ID |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.String>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "dimensionalWeightReturn",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 3. dimensionalWeightMeasure

**方法名**: `dimensionalWeightMeasure`

**描述**: @OpenApi("dimensionalWeight") public ApiResult<String> dimensionalWeight(@Valid DimensionalWeightDTO dto) { //设备回传的称重量方信息 return packingTaskFeign.dimensionalWeight(dto); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `DimensionalWeightDTO`
- **泛型类型**: `com.erp.model.sys.openapi.DimensionalWeightDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `barCode` | `String` | @NotBlank | 编码 |
| `volumeWeight` | `BigDecimal` | @NotNull | volumeweight |
| `length` | `BigDecimal` | @NotNull | length |
| `width` | `BigDecimal` | @NotNull | ID |
| `height` | `BigDecimal` | @NotNull | height |
| `weight` | `BigDecimal` | @NotNull | weight |
| `volume` | `String` | @NotNull | volume |
| `operatorId` | `String` | - | ID |
| `imageUrl` | `String` | @NotBlank | imageurl |
| `videoUrl` | `String` | - | ID |
| `warehouseId` | `String` | - | 仓库ID |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.String>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "dimensionalWeightMeasure",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 4. dimensionalWeight

**方法名**: `dimensionalWeight`

**描述**: @Resource private SoB2cDeliveryFeign soB2cDeliveryFeign; @Resource private PackingTaskFeign packingTaskFeign; @Resource private PlmTaskFeign plmTaskFeign;

**📥 请求参数**:

#### 参数: dto

- **类型**: `DimensionalWeightDTO`
- **泛型类型**: `com.erp.model.sys.openapi.DimensionalWeightDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `barCode` | `String` | @NotBlank | 编码 |
| `volumeWeight` | `BigDecimal` | @NotNull | volumeweight |
| `length` | `BigDecimal` | @NotNull | length |
| `width` | `BigDecimal` | @NotNull | ID |
| `height` | `BigDecimal` | @NotNull | height |
| `weight` | `BigDecimal` | @NotNull | weight |
| `volume` | `String` | @NotNull | volume |
| `operatorId` | `String` | - | ID |
| `imageUrl` | `String` | @NotBlank | imageurl |
| `videoUrl` | `String` | - | ID |
| `warehouseId` | `String` | - | 仓库ID |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.String>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "dimensionalWeight",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 5. dimensionalWeightPipeline

**方法名**: `dimensionalWeightPipeline`

**描述**: @Resource private SoB2cDeliveryFeign soB2cDeliveryFeign; @Resource private PackingTaskFeign packingTaskFeign; @Resource private PlmTaskFeign plmTaskFeign;

**📥 请求参数**:

#### 参数: dto

- **类型**: `DimensionalWeightDTO`
- **泛型类型**: `com.erp.model.sys.openapi.DimensionalWeightDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `barCode` | `String` | @NotBlank | 编码 |
| `volumeWeight` | `BigDecimal` | @NotNull | volumeweight |
| `length` | `BigDecimal` | @NotNull | length |
| `width` | `BigDecimal` | @NotNull | ID |
| `height` | `BigDecimal` | @NotNull | height |
| `weight` | `BigDecimal` | @NotNull | weight |
| `volume` | `String` | @NotNull | volume |
| `operatorId` | `String` | - | ID |
| `imageUrl` | `String` | @NotBlank | imageurl |
| `videoUrl` | `String` | - | ID |
| `warehouseId` | `String` | - | 仓库ID |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.String>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "dimensionalWeightPipeline",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

## 🏷️ SoB2cOpenApi

**类描述**: SoB2cOpenApi

**接口数量**: 1

### 1. getB2cOrderDeliveryInfo

**方法名**: `getB2cOrderDeliveryInfo`

**描述**: getB2cOrderDeliveryInfo

**📥 请求参数**:

#### 参数: orderDeliveryReq

- **类型**: `PagingDTO`
- **泛型类型**: `com.common.business.dto.base.PagingDTO<com.erp.model.oms.dto.SoB2cForeignDTO$OrderDeliveryReq>`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `currPage` | `Integer` | @NotNull | currpage |
| `pageSize` | `Integer` | - | pagesize |
| `lastId` | `String` | - | ID |
| `params` | `Object` | @NotNull, @Valid | params |
| `orderBy` | `String` | - | 顺序人 |
| `isSearchCount` | `Boolean` | - | 是否搜索计数 |
| `SORT_SPLIT` | `String` | - | sort_split |
| `dataScope` | `Integer` | - | 业务数据 |
| `permissionSql` | `String` | - | permissionsql |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.vo.PagingVO<com.erp.model.oms.dto.SoB2cForeignDTO$OrderDeliveryResp>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "getB2cOrderDeliveryInfo",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

## 🏷️ ShopifyServerOpenApi

**类描述**: ShopifyServerOpenApi

**接口数量**: 1

### 1. getB2cOrderLogisticInfo

**方法名**: `getB2cOrderLogisticInfo`

**描述**: @Resource private SoB2cForeignFeign soB2cForeignFeign;

**📥 请求参数**:

#### 参数: dto

- **类型**: `SoB2cLogisticQueryDTO`
- **泛型类型**: `com.erp.model.oms.dto.ShopifyServerSoB2cDTO$SoB2cLogisticQueryDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `trackingNumber` | `String` | - | tracking号 |
| `orderNumber` | `String` | - | 顺序号 |
| `contactInfo` | `String` | - | contactinfo |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.oms.dto.ShopifyServerSoB2cDTO$SoB2cLogisticInfoDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "getB2cOrderLogisticInfo",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

## 🏷️ AfterSaleOpenApi

**类描述**: package com.erp.server.auth.controller.openapi; import cn.hutool.core.collection.CollUtil; import com.common.business.dto.base.BaseDropDownDTO; import com.common.business.dto.base.BaseResultDTO; import com.common.business.dto.base.BatchResultDTO; import com.common.core.controller.vo.ApiResult; import com.erp.model.dmp.dto.AfterSaleDTO; import com.erp.model.dmp.dto.AfterSaleProgressDTO; import com.erp.model.dmp.dto.ThridUserInfoDTO; import com.erp.rpc.dmp.feign.AfterSaleFeign; import com.erp.rpc.oms.feign.OmsDropDownFeign; import com.erp.server.auth.config.OpenApi; import org.springframework.web.bind.annotation.RequestParam; import javax.annotation.Resource; import javax.validation.Valid; import java.util.List;

**接口数量**: 11

### 1. add

**方法名**: `add`

**描述**: @OpenApi("getDetailByPlatformCode") public ApiResult<List<AfterSaleDTO.DropDownDTO>> getDetailByPlatformCode(AfterSaleDTO.OpenApiCommonDTO dto){ return afterSaleFeign.getDetailByPlatformCode(dto.getPlatformCode()); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.dmp.dto.AfterSaleDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `status` | `String` | - | 状态 |
| `billDate` | `LocalDate` | - | bill日期 |
| `thridUserId` | `String` | - | ID |
| `thridUserName` | `String` | @Size | ID |
| `phoneNumber` | `String` | @Size | 电话 |
| `type` | `String` | - | 类型 |
| `thridType` | `String` | - | 类型 |
| `platformCode` | `String` | @NotBlank, @Size | 编码 |
| `dictPlatform` | `String` | @NotBlank, @Size | dictplatform |
| `buyDate` | `LocalDate` | - | buy日期 |
| `address` | `String` | @NotBlank, @Size | 地址 |
| `faultDesc` | `String` | @NotBlank, @Size | fault描述 |
| `repairInvoiceCode` | `String` | @Size | 编码 |
| `totalPrice` | `BigDecimal` | @Digits, @Min | 价格 |
| `totalRepairAmount` | `BigDecimal` | @Digits, @Min | 金额 |
| `remark` | `String` | @Size | 备注 |
| `csrRemark` | `String` | @Size | 备注 |
| `rmaRemark` | `String` | @Size | 备注 |
| `attachmentList` | `List` | - | attachment列表 |
| `attachNameList` | `List` | - | 名称 |
| `attachUrlList` | `List` | - | attachurl列表 |
| `outboundTrackNo` | `String` | @Size | outboundtrack否 |
| `returnTrackNo` | `String` | @Size | returntrack否 |
| `detailList` | `List` | - | detail列表 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BaseResultDTO$AddDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "add",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 2. listSalesPlatform

**方法名**: `listSalesPlatform`

**描述**: @OpenApi("code2Session") public ApiResult<ThridUserInfoDTO.CodeToSessionResp> code2Session(@Valid ThridUserInfoDTO.CodeToSessionDTO dto){ return afterSaleFeign.code2Session(dto); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `OpenApiCommonDTO`
- **泛型类型**: `com.erp.model.dmp.dto.AfterSaleDTO$OpenApiCommonDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | - | ID |
| `key` | `String` | - | key |
| `platformCode` | `String` | - | 编码 |
| `code` | `String` | - | 编码 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.common.business.dto.base.BaseDropDownDTO$CommonDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "listSalesPlatform",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 3. getDetailByPlatformCode

**方法名**: `getDetailByPlatformCode`

**描述**: @Resource private AfterSaleFeign afterSaleFeign; @Resource private OmsDropDownFeign omsDropDownFeign;

**📥 请求参数**:

#### 参数: dto

- **类型**: `OpenApiCommonDTO`
- **泛型类型**: `com.erp.model.dmp.dto.AfterSaleDTO$OpenApiCommonDTO`

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | - | ID |
| `key` | `String` | - | key |
| `platformCode` | `String` | - | 编码 |
| `code` | `String` | - | 编码 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.dmp.dto.AfterSaleDTO$DropDownDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "getDetailByPlatformCode",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 4. getNodeList

**方法名**: `getNodeList`

**描述**: @OpenApi("getRepairHistory") public ApiResult<List<AfterSaleProgressDTO.RepairHistoryListDTO>> getRepairHistory( @Valid AfterSaleDTO.ThridUserDTO dto){ return afterSaleFeign.getRepairHistory(dto); }

**📥 请求参数**: 无

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.dmp.dto.AfterSaleDTO$NodeDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "getNodeList",
  "data": "",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 5. addThridUser

**方法名**: `addThridUser`

**描述**: @OpenApi("getDetailByPlatformCode") public ApiResult<List<AfterSaleDTO.DropDownDTO>> getDetailByPlatformCode(AfterSaleDTO.OpenApiCommonDTO dto){ return afterSaleFeign.getDetailByPlatformCode(dto.getPlatformCode()); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `AddDTO`
- **泛型类型**: `com.erp.model.dmp.dto.ThridUserInfoDTO$AddDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `type` | `String` | - | 类型 |
| `openid` | `String` | - | ID |
| `unionid` | `String` | - | ID |
| `userInfo` | `UserInfo` | - | userinfo |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ nickName` | `String` | - | 名称 |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ avatarUrl` | `String` | - | avatarurl |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ gender` | `Integer` | - | gender |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ country` | `String` | - | 国家 |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ province` | `String` | - | 省份 |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ city` | `String` | - | 城市 |
| `&nbsp;&nbsp;&nbsp;&nbsp;└─ language` | `String` | - | language |
| `rawData` | `String` | - | 业务数据 |
| `signature` | `String` | - | 签名 |
| `encryptedData` | `String` | - | 业务数据 |
| `iv` | `String` | - | iv |
| `cloudID` | `String` | - | ID |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.dmp.dto.ThridUserInfoDTO$AddResultDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "addThridUser",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 6. invalidByCode

**方法名**: `invalidByCode`

**描述**: @OpenApi("udpateTrackNo") public ApiResult<Boolean> udpateTrackNo(@Valid AfterSaleDTO.UpdateTrackNoDTO dto){ return afterSaleFeign.udpateTrackNo(dto); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `OpenApiCommonDTO`
- **泛型类型**: `com.erp.model.dmp.dto.AfterSaleDTO$OpenApiCommonDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | - | ID |
| `key` | `String` | - | key |
| `platformCode` | `String` | - | 编码 |
| `code` | `String` | - | 编码 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.common.business.dto.base.BatchResultDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "invalidByCode",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 7. udpateTrackNo

**方法名**: `udpateTrackNo`

**描述**: @OpenApi("getNodeList") public ApiResult<List<AfterSaleDTO.NodeDTO>> getNodeList(){ return afterSaleFeign.getNodeList(); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `UpdateTrackNoDTO`
- **泛型类型**: `com.erp.model.dmp.dto.AfterSaleDTO$UpdateTrackNoDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `code` | `String` | @NotBlank | 编码 |
| `trackNo` | `String` | @NotBlank | track否 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.lang.Boolean>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "udpateTrackNo",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 8. code2Session

**方法名**: `code2Session`

**描述**: @OpenApi("addThridUser") public ApiResult<ThridUserInfoDTO.AddResultDTO> addThridUser(@Valid ThridUserInfoDTO.AddDTO dto){ return afterSaleFeign.addThridUser(dto); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `CodeToSessionDTO`
- **泛型类型**: `com.erp.model.dmp.dto.ThridUserInfoDTO$CodeToSessionDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `jsCode` | `String` | @NotBlank | 编码 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.dmp.dto.ThridUserInfoDTO$CodeToSessionResp>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "code2Session",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 9. view

**方法名**: `view`

**描述**: @OpenApi("invalidByCode") public ApiResult<BatchResultDTO> invalidByCode(@Valid AfterSaleDTO.OpenApiCommonDTO dto){ return afterSaleFeign.invalidByCode(dto.getCode()); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `OpenApiCommonDTO`
- **泛型类型**: `com.erp.model.dmp.dto.AfterSaleDTO$OpenApiCommonDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `id` | `String` | - | ID |
| `key` | `String` | - | key |
| `platformCode` | `String` | - | 编码 |
| `code` | `String` | - | 编码 |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.dmp.dto.AfterSaleDTO$ViewDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "view",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 10. getRepairHistory

**方法名**: `getRepairHistory`

**描述**: @OpenApi("getRepairRecord") public ApiResult<AfterSaleProgressDTO.RepairRecordDTO> getRepairRecord( @Valid AfterSaleDTO.ProgressDTO dto) { return afterSaleFeign.getRepairRecord(dto); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `ThridUserDTO`
- **泛型类型**: `com.erp.model.dmp.dto.AfterSaleDTO$ThridUserDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `thridUserId` | `String` | @NotBlank | ID |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<java.util.List<com.erp.model.dmp.dto.AfterSaleProgressDTO$RepairHistoryListDTO>>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "getRepairHistory",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

### 11. getRepairRecord

**方法名**: `getRepairRecord`

**描述**: @OpenApi("add") public ApiResult<BaseResultDTO.AddDTO> add( @Valid AfterSaleDTO.AddDTO dto){ dto.setType("wx"); if(CollUtil.isEmpty(dto.getDetailList()) && CollUtil.isEmpty(dto.getAttachmentList())){ return  ApiResult.error(500, "sku明细或图片附件至少填写一种"); } return afterSaleFeign.add(dto); }

**📥 请求参数**:

#### 参数: dto

- **类型**: `ProgressDTO`
- **泛型类型**: `com.erp.model.dmp.dto.AfterSaleDTO$ProgressDTO`
- **注解**: @Valid

**字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `code` | `String` | @NotBlank | 编码 |
| `thridUserId` | `String` | @NotBlank | ID |

**📤 返回类型**: `ApiResult`

**泛型返回类型**: `com.common.core.controller.vo.ApiResult<com.erp.model.dmp.dto.AfterSaleProgressDTO$RepairRecordDTO>`

**返回字段详情**:

| 字段名 | 字段类型 | 注解 | 说明 |
|--------|----------|------|------|
| `msg` | `String` | - | msg |
| `code` | `Integer` | - | 编码 |
| `traceId` | `String` | - | ID |
| `data` | `Object` | - | 业务数据 |

**💡 调用示例**:

```json
{
  "method": "getRepairRecord",
  "data": "{\"参数名\": \"参数值\"}",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```

---

## 📖 使用说明

### 接口调用方式

所有接口都通过统一的 OpenApi 入口进行调用：

```http
POST /open/api/invoke
Content-Type: application/json

{
  "method": "接口名称",
  "data": "业务参数JSON字符串",
  "timestamp": 1640995200000,
  "signType": "AES",
  "sign": "签名串",
  "version": "1.0",
  "charset": "UTF-8"
}
```


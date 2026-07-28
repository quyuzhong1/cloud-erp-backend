# 爱亚调整单 Webhook 联调手册（Postman）

对方按 `changeAttribute4Edi` **裸报文**推送；我方入口为 DMP Webhook，**不再使用** OpenAPI / `AiyaOpenApi` / 造签。

---

## 1. 接口

| 项 | 值 |
|----|-----|
| Method | `POST` |
| URL | `{{HOST}}/webhook/receive/aiyaChangeAttribute` |
| Content-Type | `application/json` |
| 鉴权 | 网关免登（`AuthPassPath` 含 `/webhook/receive/`）；不做业务 `verify`；成功后写入仓位移动 `operate_log` |
| 请求体 | 与对方 `changeAttribute4Edi` 业务 example 一致（裸 JSON） |
| 响应 | MetaResponse：`{ success, code, message, data }` |

---

## 2. 可导入 Postman 的 curl（复制整段）

Postman 操作：`Import` → `Raw text` → 粘贴下面整段 → `Continue` → `Import`。  
导入后把 URL 里的主机改成你的环境即可。

```bash
curl --location 'https://your-test-gateway.com/webhook/receive/aiyaChangeAttribute' \
--header 'Content-Type: application/json' \
--data '{
  "partnerId": "PARTNER001",
  "customerCode": "CUST001",
  "warehouseCode": "WH001",
  "changeAttributeNumber": "CA-POSTMAN-G2D-001",
  "confirmDate": 1752470400000,
  "type": "CHANGE_STATUS",
  "changeList": [
    {
      "sku": "SKU-001",
      "changeQty": 1,
      "unit": "EA",
      "invType": "CHANGE_AVAILABLE_QTY",
      "fromStatus": "GOOD",
      "toStatus": "DAMAGE"
    }
  ]
}'
```

导入后请立刻改成真实值：

- `your-test-gateway.com` → 测试网关域名
- `warehouseCode` / `sku` / `customerCode` → 已映射的仓码与平台 SKU
- `changeAttributeNumber` → 每次新测换新单号（避免幂等命中旧单）

成功响应示例：

```json
{
  "success": true,
  "code": "SUCCESS",
  "message": null,
  "data": "仓位移动主单id"
}
```

---

## 3. 其它用例 Body（导入后可改 Body）

占位符请换成真实值。

### 用例 1：GOOD → DAMAGE（成功）— 同上 §2

### 用例 2：DAMAGE → GOOD

```json
{
  "partnerId": "PARTNER001",
  "customerCode": "CUST001",
  "warehouseCode": "WH001",
  "changeAttributeNumber": "CA-POSTMAN-D2G-001",
  "confirmDate": 1752470400000,
  "type": "CHANGE_STATUS",
  "changeList": [
    {
      "sku": "SKU-001",
      "changeQty": 1,
      "fromStatus": "DAMAGE",
      "toStatus": "GOOD"
    }
  ]
}
```

成功：`success=true`。不良品不足：`success=false`，`code=INVALID_OPERATION`。

### 用例 3：幂等重复

用用例 1 同一 `changeAttributeNumber` 再发一次。  
预期：`success=true`，`data` 与首次主单 id 相同。

### 用例 4：映射失败

```json
{
  "customerCode": "CUST001",
  "warehouseCode": "NOT_EXIST_WH",
  "changeAttributeNumber": "CA-POSTMAN-MAP-FAIL-001",
  "confirmDate": 1752470400000,
  "type": "CHANGE_STATUS",
  "changeList": [
    {
      "sku": "SKU-001",
      "changeQty": 1,
      "fromStatus": "GOOD",
      "toStatus": "DAMAGE"
    }
  ]
}
```

预期：`success=false`，`code=INVALID_OPERATION`。

### 用例 5：非 CHANGE_STATUS（忽略成功）

```json
{
  "customerCode": "CUST001",
  "warehouseCode": "WH001",
  "changeAttributeNumber": "CA-POSTMAN-IGNORE-001",
  "confirmDate": 1752470400000,
  "type": "CHANGE_BATCHNO",
  "changeList": [
    {
      "sku": "SKU-001",
      "changeQty": 1,
      "fromStatus": "GOOD",
      "toStatus": "DAMAGE"
    }
  ]
}
```

预期：`success=true`，`data=""`（或不落单）。

### 用例 6：必填缺失（INVALID_DATA）

去掉 `changeAttributeNumber` 或 `changeList` 后发送。  
预期：`success=false`，`code=INVALID_DATA`（Webhook 侧 JSR-303 校验）。

---

## 4. 给对方的回调地址

```text
https://{环境}/webhook/receive/aiyaChangeAttribute
```

Body 保持 changeAttribute 业务 JSON；成功判定：`HTTP 200` 且 `success === true`。

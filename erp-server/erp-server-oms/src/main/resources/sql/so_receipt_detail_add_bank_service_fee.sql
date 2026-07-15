-- 收款单明细银行手续费 bank_service_fee
-- 若已有 service_fee：RENAME；否则 ADD

-- 已有 service_fee 时：
-- ALTER TABLE public.so_receipt_detail RENAME COLUMN service_fee TO bank_service_fee;

-- 尚无列时：
ALTER TABLE public.so_receipt_detail
    ADD COLUMN IF NOT EXISTS bank_service_fee numeric(18,6) NOT NULL DEFAULT 0;

COMMENT ON COLUMN public.so_receipt_detail.bank_service_fee IS '银行手续费';

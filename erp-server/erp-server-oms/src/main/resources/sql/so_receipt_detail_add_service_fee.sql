-- 收款单明细增加手续费
-- 表：public.so_receipt_detail

ALTER TABLE public.so_receipt_detail
    ADD COLUMN IF NOT EXISTS service_fee numeric(18,6) NOT NULL DEFAULT 0;

COMMENT ON COLUMN public.so_receipt_detail.service_fee IS '手续费';

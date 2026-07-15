-- 销售变更单增加销售员/销售部门（审批通过回写 so_info，再推金蝶）
-- 表：public.so_change

ALTER TABLE public.so_change ADD COLUMN IF NOT EXISTS seller_id varchar(19) NOT NULL DEFAULT '';
ALTER TABLE public.so_change ADD COLUMN IF NOT EXISTS seller_name varchar(50) NOT NULL DEFAULT '';
ALTER TABLE public.so_change ADD COLUMN IF NOT EXISTS sales_dept_id varchar(19) NOT NULL DEFAULT '';

COMMENT ON COLUMN public.so_change.seller_id IS '销售员id（变更后）';
COMMENT ON COLUMN public.so_change.seller_name IS '销售员名称（变更后）';
COMMENT ON COLUMN public.so_change.sales_dept_id IS '销售部门id（变更后）';

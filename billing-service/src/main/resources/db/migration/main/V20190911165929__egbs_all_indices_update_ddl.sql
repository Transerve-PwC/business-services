CREATE INDEX IF NOT EXISTS  idx_egbs_bill_v1_id ON public.egbs_bill_v1 USING btree (id);
CREATE INDEX IF NOT EXISTS  idx_egbs_bill_v1_isactive ON public.egbs_bill_v1 USING btree (isactive);
CREATE INDEX IF NOT EXISTS  idx_egbs_bill_v1_tenantid ON public.egbs_bill_v1 USING btree (tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_egbs_bill_v1 ON public.egbs_bill_v1 USING btree (id, tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_egbs_billaccountdetails_v1 ON public.egbs_billaccountdetail_v1 USING btree (id, tenantid);
CREATE INDEX IF NOT EXISTS  idx_egbs_billdetail_v1_businessservice ON public.egbs_billdetail_v1 USING btree (businessservice);
CREATE INDEX IF NOT EXISTS  idx_egbs_billdetail_v1_consumercode ON public.egbs_billdetail_v1 USING btree (consumercode);
CREATE INDEX IF NOT EXISTS  idx_egbs_billdetail_v1_tenantid ON public.egbs_billdetail_v1 USING btree (tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_egbs_billdetail_v1 ON public.egbs_billdetail_v1 USING btree (id, tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_biz_srvc_det ON public.egbs_business_service_details USING btree (id, tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  unq_businessservice ON public.egbs_business_service_details USING btree (businessservice, tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_egbs_collectedreceipts ON public.egbs_collectedreceipts USING btree (id, tenantid);
CREATE INDEX IF NOT EXISTS  idx_egbs_demand_v1_businessservice ON public.egbs_demand_v1 USING btree (businessservice);
CREATE INDEX IF NOT EXISTS  idx_egbs_demand_v1_consumercode ON public.egbs_demand_v1 USING btree (consumercode);
CREATE INDEX IF NOT EXISTS  idx_egbs_demand_v1_consumertype ON public.egbs_demand_v1 USING btree (consumertype);
CREATE INDEX IF NOT EXISTS  idx_egbs_demand_v1_id ON public.egbs_demand_v1 USING btree (id);
CREATE INDEX IF NOT EXISTS  idx_egbs_demand_v1_payer ON public.egbs_demand_v1 USING btree (payer);
CREATE INDEX IF NOT EXISTS  idx_egbs_demand_v1_taxperiodfrom ON public.egbs_demand_v1 USING btree (taxperiodfrom);
CREATE INDEX IF NOT EXISTS  idx_egbs_demand_v1_taxperiodto ON public.egbs_demand_v1 USING btree (taxperiodto);
CREATE INDEX IF NOT EXISTS  idx_egbs_demand_v1_tenantid ON public.egbs_demand_v1 USING btree (tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_egbs_demand_v1 ON public.egbs_demand_v1 USING btree (id, tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  uk_egbs_demand_v1_consumercode_businessservice ON public.egbs_demand_v1 USING btree (consumercode, tenantid, taxperiodfrom, taxperiodto, businessservice);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_egbs_demand_v1_audit ON public.egbs_demand_v1_audit USING btree (id, tenantid);
CREATE INDEX IF NOT EXISTS  idx_egbs_demanddetail_v1_demandid ON public.egbs_demanddetail_v1 USING btree (demandid);
CREATE INDEX IF NOT EXISTS  idx_egbs_demanddetail_v1_tenantid ON public.egbs_demanddetail_v1 USING btree (tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_egbs_demanddetail_v1 ON public.egbs_demanddetail_v1 USING btree (id, tenantid);
CREATE UNIQUE INDEX IF NOT EXISTS  pk_egbs_demanddetail_v1_audit ON public.egbs_demanddetail_v1_audit USING btree (id, tenantid);

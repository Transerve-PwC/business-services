package org.egov.collection.repository;

import lombok.extern.slf4j.Slf4j;
import org.egov.collection.model.Payment;
import org.egov.collection.model.PaymentDetail;
import org.egov.collection.model.PaymentSearchCriteria;
import org.egov.collection.repository.querybuilder.PaymentQueryBuilder;
import org.egov.collection.repository.rowmapper.BillRowMapper;
import org.egov.collection.repository.rowmapper.PaymentRowMapper;
import org.egov.collection.web.contract.Bill;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.Identity;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.Collections.reverseOrder;
import static org.egov.collection.repository.querybuilder.PaymentQueryBuilder.*;

@Slf4j
@Repository
public class PaymentRepository {


    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private PaymentQueryBuilder paymentQueryBuilder;

    private PaymentRowMapper paymentRowMapper;
    
    private BillRowMapper billRowMapper;

    @Autowired
    public PaymentRepository(NamedParameterJdbcTemplate namedParameterJdbcTemplate, PaymentQueryBuilder paymentQueryBuilder, 
    		PaymentRowMapper paymentRowMapper, BillRowMapper billRowMapper) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.paymentQueryBuilder = paymentQueryBuilder;
        this.paymentRowMapper = paymentRowMapper;
        this.billRowMapper = billRowMapper;
    }




    @Transactional
    public void savePayment(Payment payment){
        try {

            List<MapSqlParameterSource> paymentDetailSource = new ArrayList<>();
            List<MapSqlParameterSource> billSource = new ArrayList<>();
            List<MapSqlParameterSource> billDetailSource = new ArrayList<>();
            List<MapSqlParameterSource> billAccountDetailSource = new ArrayList<>();

            for (PaymentDetail paymentDetail : payment.getPaymentDetails()) {
                paymentDetailSource.add(getParametersForPaymentDetailCreate(payment.getId(), paymentDetail));
                billSource.add(getParamtersForBillCreate(paymentDetail.getBill()));
                paymentDetail.getBill().getBillDetails().forEach(billDetail -> {
                    billDetailSource.add(getParamtersForBillDetailCreate(billDetail));
                    billDetail.getBillAccountDetails().forEach(billAccountDetail -> {
                        billAccountDetailSource.add(getParametersForBillAccountDetailCreate(billAccountDetail));
                    });
                });

            }
            namedParameterJdbcTemplate.update(INSERT_PAYMENT_SQL, getParametersForPaymentCreate(payment));
            namedParameterJdbcTemplate.batchUpdate(INSERT_PAYMENTDETAIL_SQL, paymentDetailSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(INSERT_BILL_SQL, billSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(INSERT_BILLDETAIL_SQL, billDetailSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(INSERT_BILLACCOUNTDETAIL_SQL,  billAccountDetailSource.toArray(new MapSqlParameterSource[0]));

        }catch (Exception e){
            log.error("Failed to persist payment to database", e);
            throw new CustomException("PAYMENT_CREATION_FAILED", e.getMessage());
        }
    }


    public List<Payment> fetchPayments(PaymentSearchCriteria paymentSearchCriteria){
        Map<String, Object> preparedStatementValues = new HashMap<>();
        String query = paymentQueryBuilder.getPaymentSearchQuery(paymentSearchCriteria, preparedStatementValues);
        log.info("Query: "+query);
        log.info("preparedStatementValues: "+preparedStatementValues);
        List<Payment> payments = namedParameterJdbcTemplate.query(query, preparedStatementValues,paymentRowMapper);
        if(!CollectionUtils.isEmpty(payments)) {
            Set<String> billIds = new HashSet<>();
            for(Payment payment : payments) {
            	billIds.addAll(payment.getPaymentDetails().stream().map(detail -> detail.getBillId()).collect(Collectors.toSet()));
            }
            Map<String, Bill> billMap = getBills(billIds);
            for(Payment payment : payments) {
            	payment.getPaymentDetails().forEach(detail -> {
            		detail.setBill(billMap.get(detail.getBillId()));
            	});
            }
           payments.sort(reverseOrder(Comparator.comparingLong(Payment::getTransactionDate)));
        }

        return payments;
    }
    
    private Map<String, Bill> getBills(Set<String> ids){
    	Map<String, Bill> mapOfIdAndBills = new HashMap<>();
        Map<String, Object> preparedStatementValues = new HashMap<>();
        preparedStatementValues.put("id", ids);
        String query = paymentQueryBuilder.getBillQuery();
        List<Bill> bills = namedParameterJdbcTemplate.query(query, preparedStatementValues, billRowMapper);
        bills.forEach(bill -> {
        	mapOfIdAndBills.put(bill.getId(), bill);
        });
        
        return mapOfIdAndBills;

    }



    public void updateStatus(List<Payment> payments){
        List<MapSqlParameterSource> paymentSource = new ArrayList<>();
        List<MapSqlParameterSource> paymentDetailSource = new ArrayList<>();
        List<MapSqlParameterSource> billSource = new ArrayList<>();
        try {

            for(Payment payment : payments){
                paymentSource.add(getParametersForPaymentStatusUpdate(payment));
                for (PaymentDetail paymentDetail : payment.getPaymentDetails()) {
                    paymentDetailSource.add(getParametersForPaymentDetailStatusUpdate(paymentDetail));
                    billSource.add(getParamtersForBillStatusUpdate(paymentDetail.getBill()));
                }
            }

            namedParameterJdbcTemplate.batchUpdate(COPY_PAYMENT_SQL, paymentSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(COPY_PAYMENTDETAIL_SQL, paymentDetailSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(COPY_BILL_SQL, billSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(STATUS_UPDATE_PAYMENT_SQL, paymentSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(STATUS_UPDATE_PAYMENTDETAIL_SQL, paymentDetailSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(STATUS_UPDATE_BILL_SQL, billSource.toArray(new MapSqlParameterSource[0]));
        }
        catch(Exception e){
            log.error("Failed to persist cancel Receipt to database", e);
            throw new CustomException("CANCEL_RECEIPT_FAILED", "Unable to cancel Receipt");
        }
    }


    public void updatePayment(List<Payment> payments){
        List<MapSqlParameterSource> paymentSource = new ArrayList<>();
        List<MapSqlParameterSource> paymentDetailSource = new ArrayList<>();
        List<MapSqlParameterSource> billSource = new ArrayList<>();
        List<MapSqlParameterSource> billDetailSource = new ArrayList<>();

        try {

            for (Payment payment : payments) {
                paymentSource.add(getParametersForPaymentUpdate(payment));
                payment.getPaymentDetails().forEach(paymentDetail -> {
                    paymentDetailSource.add(getParametersForPaymentDetailUpdate(paymentDetail));
                    billSource.add(getParamtersForBillUpdate(paymentDetail.getBill()));

                    paymentDetail.getBill().getBillDetails().forEach(billDetail -> {
                        billDetailSource.add(getParamtersForBillDetailUpdate(billDetail));
                    });

                });
            }
            namedParameterJdbcTemplate.batchUpdate(UPDATE_PAYMENT_SQL, paymentSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(UPDATE_PAYMENTDETAIL_SQL, paymentDetailSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(UPDATE_BILL_SQL, billSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(UPDATE_BILLDETAIL_SQL, billDetailSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(COPY_PAYMENT_SQL, paymentSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(COPY_PAYMENTDETAIL_SQL, paymentDetailSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(COPY_BILL_SQL, billSource.toArray(new MapSqlParameterSource[0]));
            namedParameterJdbcTemplate.batchUpdate(COPY_BILLDETAIL_SQL, billDetailSource.toArray(new MapSqlParameterSource[0]));
        }catch (Exception e){
            log.error("Failed to update receipt to database", e);
            throw new CustomException("RECEIPT_UPDATION_FAILED", "Unable to update receipt");
        }
    }



}

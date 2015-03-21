package com.ofg.loan;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

/**
 * Created by Administrator on 21.03.15.
 */
@Entity
public class LoanEntity {

    @Column
    private Number amount;

    @Column
    @Id
    private String id;

    LoanEntity() {
    }

    public LoanEntity(Number amount, String id) {
        this.amount = amount;
        this.id = id;
    }

    public Number getAmount() {
        return amount;
    }

    public String getId() {
        return id;
    }
}

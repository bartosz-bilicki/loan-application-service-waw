package com.ofg.loan;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

/**
 * Created by Administrator on 21.03.15.
 */
public interface LoanRepository extends CrudRepository<LoanEntity,String> {

}

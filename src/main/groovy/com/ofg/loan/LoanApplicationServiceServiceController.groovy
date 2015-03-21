package com.ofg.loan

import com.codahale.metrics.MetricRegistry
import com.netflix.hystrix.HystrixCommand
import com.netflix.hystrix.HystrixObservableCommand
import com.nurkiewicz.asyncretry.AsyncRetryExecutor;
import com.ofg.infrastructure.web.resttemplate.fluent.ServiceRestClient
import groovy.json.JsonOutput
import groovy.transform.TypeChecked;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;

@Slf4j
@RestController
@RequestMapping("/api/loanApplication")
@TypeChecked
//@Api(value = "pairId", description = "Collects places from tweets and propagates them to Collerators")
public class LoanApplicationServiceServiceController {

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private ServiceRestClient client;

    @Autowired
    private AsyncRetryExecutor executor;

    @Autowired
    MetricRegistry metricRegistry;

    @RequestMapping(method = RequestMethod.POST)
    public void loan(@RequestBody @NotNull LoanServiceRequest request) {

        metricRegistry.counter("call.load.before").inc();
        LoanEntity loan = new LoanEntity(request.amount, request.loanId);
        loanRepository.save(loan);

        callFraudDetectionService(request.firstName, request.lastName, request.job, request.amount, request.age, request.loanId)
        callReportingService(request.firstName, request.lastName, request.age, request.loanId);
        metricRegistry.counter("call.load.after").inc();
    }

    @RequestMapping(method = RequestMethod.POST)
    public void loan_broken(@RequestBody @NotNull LoanServiceRequest request) {
        metricRegistry.counter("call.load_broken.before").inc();
        LoanEntity loan = new LoanEntity(request.amount, request.loanId);
        loanRepository.save(loan);

        callFraudDetectionService(request.firstName, request.lastName, request.job, request.amount, request.age, request.loanId)
        callReportingServiceBroken(request.firstName, request.lastName, request.age, request.loanId);
        metricRegistry.counter("call.load_broken.after").inc();
    }

    private void callFraudDetectionService(String firstName, String lastName, String job, Number amount, Number age, String loanId) {
        client
                .forService("fraud-detection-service")
                .retryUsing(executor)
                .put()
                .withCircuitBreaker(
                HystrixCommand.Setter.withGroupKey({ 'group_key' }),
                { 'body to return upon fallback' })
                .onUrl("/api/loanApplication/" + loanId)
                .body(JsonOutput.toJson([
                firstName: firstName,
                lastName : lastName,
                job      : job,
                amount   : amount,
                age      : age
        ]))
                .withHeaders()
                .contentTypeJson()
                .andExecuteFor().ignoringResponseAsync();
    }

    private void callReportingService(String url,String firstName, String lastName, Number age, String loanId) {
        client
                .forService("reporting-service")
                .retryUsing(executor)
                .post()
                .withCircuitBreaker(
                HystrixCommand.Setter.withGroupKey({ 'group_key' }),
                { 'body to return upon fallback' })
                .onUrl(url)
                .body(JsonOutput.toJson([
                firstName: firstName,
                lastName : lastName,
                loanid   : loanId,
                age      : age
                ]))
                .withHeaders()
                .contentTypeJson()
                .andExecuteFor().ignoringResponseAsync();
    }

    private void callReportingService(String firstName, String lastName, Number age, String loanId) {
        callReportingService("/api/client",firstName,lastName,age,loanId);
    }

    private void callReportingServiceBroken(String firstName, String lastName, Number age, String loanId) {
        callReportingService("/api/client-broken",firstName,lastName,age,loanId);
    }
}

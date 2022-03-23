package io.apimap.api.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.trace.http.HttpTrace;
import org.springframework.boot.actuate.trace.http.InMemoryHttpTraceRepository;
import org.springframework.stereotype.Repository;

/**
 * Tried to use {@link org.springframework.web.filter.CommonsRequestLoggingFilter} but I didn't get it to work.
 */
@Repository
public class LogUsageInformationRepository extends InMemoryHttpTraceRepository {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogUsageInformationRepository.class);

    @Override
    public void add(HttpTrace trace) {
        LOGGER.info(getInformationFromHttpTrace(trace) );
        super.add(trace);
    }

    private String getInformationFromHttpTrace(HttpTrace trace){
        //Principal and session seems to be always null
        //Not fetching timestamp since we are adding our one timestamp in the log
        return getRequest(trace.getRequest()) +
               ", " + getResponse(trace.getResponse()) +
               ", Timetaken=" + trace.getTimeTaken() + " milliseconds";
    }

    private String getResponse(HttpTrace.Response response) {
        //Headers are not that interesting
        if(response!=null){
            return "Status=" + response.getStatus();
        } else {
            return null;
        }
    }

    private String getRequest(HttpTrace.Request request) {
        //Remote address seems to be always null
        //Headers are not that interesting
        if(request!=null){
            return "Method=" + request.getMethod() +
                    ", URI=" + request.getUri();
        } else {
            return null;
        }
    }
}
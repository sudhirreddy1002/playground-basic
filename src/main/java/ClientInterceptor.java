import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.util.StopWatch;

import java.io.IOException;
import java.util.ArrayList;

class ClientInterceptor implements IClientInterceptor {
    static ArrayList<Long> responseTimes = new ArrayList<>();

    @Override
    public void interceptRequest(IHttpRequest iHttpRequest) {

    }

    @Override
    public void interceptResponse(IHttpResponse iHttpResponse) throws IOException {
        StopWatch sw =  iHttpResponse.getRequestStopWatch();
        responseTimes.add(sw.getMillis());
    }
}



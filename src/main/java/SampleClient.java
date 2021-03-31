import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.StopWatch;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Patient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.stream.LongStream;

public class SampleClient {
    static private IGenericClient client;
    static private HashSet<String> names = new HashSet<>();  // Unique Family names

    public static void main(String[] theArgs) {

        // Create a FHIR client
        FhirContext fhirContext = FhirContext.forR4();
        client = fhirContext.newRestfulGenericClient("http://hapi.fhir.org/baseR4");
        client.registerInterceptor(new LoggingInterceptor(false));
        client.registerInterceptor(new ClientInterceptor());

        double respAvg = getAvgTimes(false);
        System.out.println("Average time taken for requests:  "+ respAvg );

        for (int i = 0; i < 3; i++) {
            if (i<=1)
                respAvg = getAvgTimes(true);
            else
                respAvg = getAvgTimes(false);
            System.out.println("Average time taken for requests:  "+ respAvg );
        }
    }

    public static double getAvgTimes(boolean useCache){
        iterateName(useCache);
        return ClientInterceptor.responseTimes.stream().mapToLong(v -> v).average().orElse(0.0);

    }

    private static void searchByFamily(String family, boolean userCache){

        System.out.println("Searching "+ family);



        //    Search for Patient resources
        if(userCache){
            Bundle response = client
                    .search()
                    .forResource("Patient")
                    .where(Patient.FAMILY.matches().value(family))
                    .returnBundle(Bundle.class)
                    .cacheControl(new CacheControlDirective().setNoCache(false))
                    .execute();

        } else {
            Bundle response = client
                    .search()
                    .forResource("Patient")
                    .where(Patient.FAMILY.matches().value(family))
                    .returnBundle(Bundle.class)
                    .cacheControl(new CacheControlDirective().setNoCache(true))
                    .execute();
        }

//        System.out.println(response);
    }

    // Load Names from file into memory
    private static void iterateName(boolean useCache){
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream is = cl.getResourceAsStream("last_names.txt");
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        br.lines().forEach(
                line -> searchByFamily(line, useCache)
        );

    }

}


class ClientInterceptor implements IClientInterceptor{
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



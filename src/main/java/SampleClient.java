import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.CacheControlDirective;
import ca.uhn.fhir.rest.client.api.IClientInterceptor;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.IHttpRequest;
import ca.uhn.fhir.rest.client.api.IHttpResponse;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import ca.uhn.fhir.util.StopWatch;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.StringType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
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
        Bundle response;


        //    Search for Patient resources
        if(userCache){
            response = client
                    .search()
                    .forResource("Patient")
                    .where(Patient.FAMILY.matches().value(family))
                    .returnBundle(Bundle.class)
                    .cacheControl(new CacheControlDirective().setNoCache(false))
                    .execute();

        } else {
            response = client
                    .search()
                    .forResource("Patient")
                    .where(Patient.FAMILY.matches().value(family))
                    .returnBundle(Bundle.class)
                    .cacheControl(new CacheControlDirective().setNoCache(true))
                    .execute();
        }

        System.out.println(response);
        printDetails(response);
    }

    private static void printDetails(Bundle response){
        List<HashMap<String, String>> patients = new ArrayList<>();
        ArrayList<Bundle.BundleEntryComponent> entry = (ArrayList<Bundle.BundleEntryComponent>) response.getEntry();
//        Bundle.BundleEntryComponent e = entry.get(0);
        entry.forEach(
                e -> {
                    Patient p = (Patient) e.getResource();
                    List<HumanName> name =  p.getName();
                    String lastName = name.get(0).getFamily();
                    StringType firstName = name.get(0).getGiven().get(0);
                    HashMap<String, String> details = new HashMap<>();
                    details.put("first_name", firstName.toString());
                    details.put("last_name", lastName);
                    Date dob = p.getBirthDate();
                    if(dob != null){
                        details.put("dob", p.getBirthDate().toString());
                    } else {
                        details.put("dob", "");
                    }
                    patients.add(details);
                }
        );

        // Sort and print
        patients.stream().sorted(Comparator.comparing(a -> a.get("first_name"))).forEach(System.out::println);

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

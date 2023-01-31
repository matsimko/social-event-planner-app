package clients;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;

import org.apache.cxf.jaxrs.client.JAXRSClientFactory;
import org.apache.cxf.jaxrs.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import lombok.Getter;
import lombok.Setter;

public class Client {
	
	@Getter @Setter
	private static String url = "http://localhost:8000";
	
	public static <T> T getAuthClient(Class<T> clientClass, String encodedCredentials) {
		T client = getClient(clientClass);
		if(encodedCredentials != null) {
			WebClient.client(client).header(HttpHeaders.AUTHORIZATION, encodedCredentials);
		}
		
		return client;
	}
	
	public static <T> T getClient(Class<T> clientClass) {
		Map<String, Object> properties = new HashMap<String, Object>();

		ObjectMapper mapper = new ObjectMapper();
		// Register the JavaTimeModule so that class like Instant
		// can be serialized/deserialized (other solution would be to send the date as a string and work with that)
		mapper.registerModule(new JavaTimeModule());
		mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
		
		JacksonJsonProvider jsonProvider = new JacksonJaxbJsonProvider()
				.disable(SerializationFeature.WRAP_ROOT_VALUE)
				.disable(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED);
		jsonProvider.setMapper(mapper);

		T client = JAXRSClientFactory.create(url, clientClass,
				Collections.singletonList(jsonProvider),
				properties, true);
		return client;
	}
}

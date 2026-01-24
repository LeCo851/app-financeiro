package br.com.leandrocoelho.backend.service.integration;

import br.com.leandrocoelho.backend.integration.pluggy.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class PluggyService {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;

    public PluggyService(
            RestClient.Builder builder,
            @Value("${pluggy.base-url}") String baseUrl,
            @Value("${pluggy.client-id}") String clientId,
            @Value("${pluggy.client-secret}") String clientSecret){

        this.restClient = builder.baseUrl(baseUrl).build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    private String getApiKey(){
        var request = new PluggyAuthRequestDto(clientId, clientSecret);

        PluggyAuthResponseDto responseDto = restClient.post()
                .uri("/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PluggyAuthResponseDto.class);

        if(responseDto == null || responseDto.apiKey() == null){
            throw  new RuntimeException("Falha ao autenticar no Pluggy");
        }
        return responseDto.apiKey();
    }

    public String createConnectToken(){
        String apiKey = getApiKey();

        var request = new PluggyConnectTokenRequestDto(null);

        PluggyConnectTokenResponseDto responseDto = restClient.post()
                .uri("/connect_token")
                .header("X-API-KEY", apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(PluggyConnectTokenResponseDto.class);

        if(responseDto == null || responseDto.accessToken() == null){
            throw new RuntimeException("Falha ao criar Connect Token");
        }
        return responseDto.accessToken();
    }
    
    public List<PluggyAccountDto> getAccounts(String itemId){
        String apiKey = getApiKey();
        
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/accounts")
                        .queryParam("itemId",itemId)
                        .build())
                .header("X-API-KEY",apiKey)
                .retrieve()
                .body(new ParameterizedTypeReference<PluggyResultDto<PluggyAccountDto>>(){});
        return response != null ? response.results() : List.of();
    }
    
    public List<PluggyTransactionDto> getTransactions(String accountId){
        String apiKey = getApiKey();
        
        var response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/transactions")
                        .queryParam("accountId", accountId)
                        .build())
                .header("X-API-KEY",apiKey)
                .retrieve()
                .body(new ParameterizedTypeReference<PluggyResultDto<PluggyTransactionDto>>() {});

        return response != null ? response.results() : List.of();
    }
}

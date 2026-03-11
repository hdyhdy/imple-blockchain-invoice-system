package com.invoice.config;

import org.fisco.bcos.sdk.BcosSDK;
import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Configuration
public class FiscoBcosConfig {

    @Value("${fisco.config.config-file}")
    private String configFile;

    @Value("${fisco.config.group-id}")
    private int groupId;

    @Value("${fisco.contract.address}")
    private String contractAddress;

    @Bean
    public BcosSDK bcosSDK() {
        // 加载配置文件
        File config = new File(getClass().getResource(configFile).getFile());
        return BcosSDK.build(config.getAbsolutePath());
    }

    @Bean
    public Client client(BcosSDK bcosSDK) {
        return bcosSDK.getClient(groupId);
    }

    @Bean
    public CryptoKeyPair cryptoKeyPair(Client client) {
        return client.getCryptoSuite().createKeyPair();
    }

    @Bean
    public InvoiceRegistry invoiceRegistry(Client client, CryptoKeyPair cryptoKeyPair) {
        // 加载智能合约
        return InvoiceRegistry.load(contractAddress, client, cryptoKeyPair);
    }
}

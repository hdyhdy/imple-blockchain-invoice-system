package com.invoice.blockchain;

import org.fisco.bcos.sdk.client.Client;
import org.fisco.bcos.sdk.crypto.keypair.CryptoKeyPair;

import java.math.BigInteger;

public class InvoiceRegistry {

    private final Client client;
    private final CryptoKeyPair cryptoKeyPair;
    private final String contractAddress;

    private InvoiceRegistry(Client client, CryptoKeyPair cryptoKeyPair, String contractAddress) {
        this.client = client;
        this.cryptoKeyPair = cryptoKeyPair;
        this.contractAddress = contractAddress;
    }

    public static InvoiceRegistry load(String contractAddress, Client client, CryptoKeyPair cryptoKeyPair) {
        return new InvoiceRegistry(client, cryptoKeyPair, contractAddress);
    }

    public Client getClient() {
        return client;
    }

    public CryptoKeyPair getCryptoKeyPair() {
        return cryptoKeyPair;
    }

    public String getContractAddress() {
        return contractAddress;
    }
}

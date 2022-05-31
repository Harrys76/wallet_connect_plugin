package com.wallet.connect.plugin.wallet_connect_plugin

//MainNet
val DEFAULT_MAIN_CHAINS = listOf(
        "eip155:1",
        "eip155:10",
        "eip155:100",
        "eip155:137",
        "eip155:42161",
        "eip155:42220",
        "cosmos:cosmoshub-4",
        "solana:4sGjMW1sUnHzSxGspuhpqLDx6wiyjNtZ"
)

val DEFAULT_EIP155_METHODS = listOf(
        "eth_sendTransaction",
        "eth_signTransaction",
        "eth_sign",
        "personal_sign",
        "eth_signTypedData"
)

val DEFAULT_EIP_155_EVENTS = listOf(
        "chainChanged",
        "accountsChanged"
)

val DEFAULT_COSMOS_METHODS = listOf(
        "cosmos_signDirect",
        "cosmos_signAmino"
)

val DEFAULT_COSMOS_EVENTS = emptyList<String>()

val DEFAULT_SOLANA_METHODS = listOf(
        "solana_signTransaction",
        "solana_signMessage"
)

val DEFAULT_SOLANA_EVENTS = emptyList<String>()
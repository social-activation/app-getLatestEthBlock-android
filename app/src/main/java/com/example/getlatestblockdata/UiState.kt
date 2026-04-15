package com.example.getlatestblockdata

data class UiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val blockNumber: String = "",
    val blockHash: String = "",
    val parentHash: String = "",
    val timestamp: String = "",
    val miner: String = "",
    val gasUsed: String = "",
    val gasLimit: String = "",
    val baseFeePerGasGwei: String = "",
    val transactionCount: String = "",
    val blockSizeBytes: String = "",
    val stateRoot: String = "",
    val summaryJson: String = "",
    val lastUpdated: String = ""
)
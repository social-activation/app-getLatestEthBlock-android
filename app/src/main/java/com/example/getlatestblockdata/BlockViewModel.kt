package com.example.getlatestblockdata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BlockViewModel : ViewModel() {

    private val repository = EthereumRepository(
        projectId = BuildConfig.INFURA_PROJECT_ID,
        network = BuildConfig.ETH_NETWORK
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    fun fetchLatestBlock() {
        _uiState.value = _uiState.value.copy(
            isLoading = true,
            errorMessage = null
        )

        viewModelScope.launch(Dispatchers.IO) {
            val result = repository.fetchLatestBlock()

            _uiState.value = result.fold(
                onSuccess = { block ->
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = null,
                        blockNumber = block.blockNumber,
                        blockHash = block.blockHash,
                        parentHash = block.parentHash,
                        timestamp = block.timestamp,
                        miner = block.miner,
                        gasUsed = block.gasUsed,
                        gasLimit = block.gasLimit,
                        baseFeePerGasGwei = block.baseFeePerGasGwei,
                        transactionCount = block.transactionCount,
                        blockSizeBytes = block.blockSizeBytes,
                        stateRoot = block.stateRoot,
                        summaryJson = block.summaryJson,
                        lastUpdated = nowString()
                    )
                },
                onFailure = { error ->
                    _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Unknown error"
                    )
                }
            )
        }
    }

    private fun nowString(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}
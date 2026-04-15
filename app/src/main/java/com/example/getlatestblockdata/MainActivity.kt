package com.example.getlatestblockdata

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.getlatestblockdata.ui.theme.GetLatestBlockDataTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BlockViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            GetLatestBlockDataTheme {
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Ethereum Latest Block",
                            style = MaterialTheme.typography.headlineMedium
                        )

                        Text(
                            text = "Each tap makes one Infura call and shows the latest block.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Button(
                            onClick = { viewModel.fetchLatestBlock() },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Text("Get Latest Block")
                        }

                        if (state.isLoading) {
                            CircularProgressIndicator()
                            Text("Loading latest block...")
                        }

                        state.errorMessage?.let { error ->
                            Text(
                                text = "Error: $error",
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        if (state.blockNumber.isNotBlank()) {
                            HorizontalDivider()

                            Text(
                                text = "Block Number: ${state.blockNumber}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Block Hash: ${state.blockHash}")
                            Text("Parent Hash: ${state.parentHash}")
                            Text("Timestamp: ${state.timestamp}")
                            Text("Miner: ${state.miner}")
                            Text("Gas Used: ${state.gasUsed}")
                            Text("Gas Limit: ${state.gasLimit}")
                            Text("Base Fee: ${state.baseFeePerGasGwei} Gwei")
                            Text("Transaction Count: ${state.transactionCount}")
                            Text("Block Size: ${state.blockSizeBytes} bytes")
                            Text("State Root: ${state.stateRoot}")
                            Text("Last Updated: ${state.lastUpdated}")

//                            HorizontalDivider()
//
//                            Text(
//                                text = "Compact JSON Summary",
//                                style = MaterialTheme.typography.titleMedium
//                            )
//                            Text(
//                                text = state.summaryJson,
//                                style = MaterialTheme.typography.bodySmall
//                            )
                        }
                    }
                }
            }
        }
    }
}
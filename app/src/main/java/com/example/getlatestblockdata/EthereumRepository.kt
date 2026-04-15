package com.example.getlatestblockdata

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BlockSummary(
    val blockNumber: String,
    val blockHash: String,
    val parentHash: String,
    val timestamp: String,
    val miner: String,
    val gasUsed: String,
    val gasLimit: String,
    val baseFeePerGasGwei: String,
    val transactionCount: String,
    val blockSizeBytes: String,
    val stateRoot: String,
    val summaryJson: String
)

class EthereumRepository(
    private val projectId: String,
    private val network: String
) {
    private val client = OkHttpClient()

    fun fetchLatestBlock(): Result<BlockSummary> {
        return try {
            if (projectId.isBlank()) {
                Result.failure(
                    IllegalStateException("Missing INFURA_PROJECT_ID in local.properties")
                )
            } else if (network.isBlank()) {
                Result.failure(
                    IllegalStateException("Missing ETH_NETWORK in local.properties")
                )
            } else {
                val normalizedNetwork = network.trim().lowercase(Locale.US)
                val url = "https://$normalizedNetwork.infura.io/v3/$projectId"

                val rpcBody = JSONObject().apply {
                    put("jsonrpc", "2.0")
                    put("method", "eth_getBlockByNumber")
                    put("params", JSONArray().put("latest").put(false))
                    put("id", 1)
                }

                val requestBody = rpcBody.toString()
                    .toRequestBody("application/json".toMediaType())

                val request = Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return Result.failure(
                            IllegalStateException("HTTP ${response.code}: ${response.message}")
                        )
                    }

                    val responseText = response.body?.string().orEmpty()
                    val root = JSONObject(responseText)

                    if (root.has("error")) {
                        return Result.failure(
                            IllegalStateException(root.getJSONObject("error").toString(2))
                        )
                    }

                    val result = root.optJSONObject("result")
                        ?: return Result.failure(
                            IllegalStateException("No block data returned from Infura")
                        )

                    val blockNumber = hexToDecimalString(result.optString("number", ""))
                    val blockHash = result.optString("hash", "")
                    val parentHash = result.optString("parentHash", "")
                    val miner = result.optString("miner", "")
                    val gasUsed = hexToDecimalString(result.optString("gasUsed", ""))
                    val gasLimit = hexToDecimalString(result.optString("gasLimit", ""))
                    val baseFeePerGasWei = hexToDecimalString(result.optString("baseFeePerGas", ""))
                    val transactionCount = result.optJSONArray("transactions")?.length()?.toString() ?: "0"
                    val blockSizeBytes = hexToDecimalString(result.optString("size", ""))
                    val stateRoot = result.optString("stateRoot", "")
                    val timestamp = formatEthereumTimestamp(result.optString("timestamp", ""))

                    val baseFeePerGasGwei = weiToGweiString(baseFeePerGasWei)

                    val summary = JSONObject().apply {
                        put("blockNumber", blockNumber)
                        put("blockHash", blockHash)
                        put("parentHash", parentHash)
                        put("timestamp", timestamp)
                        put("miner", miner)
                        put("gasUsed", gasUsed)
                        put("gasLimit", gasLimit)
                        put("baseFeePerGasGwei", baseFeePerGasGwei)
                        put("transactionCount", transactionCount)
                        put("blockSizeBytes", blockSizeBytes)
                        put("stateRoot", stateRoot)
                    }

                    Result.success(
                        BlockSummary(
                            blockNumber = blockNumber,
                            blockHash = blockHash,
                            parentHash = parentHash,
                            timestamp = timestamp,
                            miner = miner,
                            gasUsed = gasUsed,
                            gasLimit = gasLimit,
                            baseFeePerGasGwei = baseFeePerGasGwei,
                            transactionCount = transactionCount,
                            blockSizeBytes = blockSizeBytes,
                            stateRoot = stateRoot,
                            summaryJson = summary.toString(2)
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun hexToDecimalString(hex: String): String {
        if (hex.isBlank()) return ""
        return try {
            hex.removePrefix("0x").toBigInteger(16).toString(10)
        } catch (_: Exception) {
            hex
        }
    }

    private fun formatEthereumTimestamp(hexTimestamp: String): String {
        return try {
            val seconds = hexTimestamp.removePrefix("0x").toLong(16)
            val date = Date(seconds * 1000)
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            hexTimestamp
        }
    }

    private fun weiToGweiString(weiString: String): String {
        return try {
            if (weiString.isBlank()) return ""
            val gwei = weiString.toBigDecimal().divide("1000000000".toBigDecimal())
            gwei.stripTrailingZeros().toPlainString()
        } catch (_: Exception) {
            weiString
        }
    }
}
package com.example.second_project.blockchain

import LecturePurchaseEvent
import LectureSystem
import TransactionEvent
import android.util.Log
import io.reactivex.rxjava3.core.Flowable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.gas.DefaultGasProvider
import java.math.BigInteger

private const val TAG = "BlockChainManager_야옹"

class BlockchainManager {
    private val web3j: Web3j
    private val lectureSystem: LectureSystem
    private val credentials: Credentials

    init {
        // Web3j 설정
        web3j = Web3j.build(HttpService("https://rpc-amoy.polygon.technology/"))
        val lectureSystemAddress = "0x5532EDfa8C6a10e0FA62Cc8f8c221c1573D0fcbc"
        credentials = Credentials.create("0000000000000000000000000000000000000000000000000000000000000000")

        // LectureSystem 스마트 컨트랙트 로드
        lectureSystem = LectureSystem.load(
            lectureSystemAddress,
            web3j,
            credentials,
            DefaultGasProvider()
        )
    }

    suspend fun getTransactionHistory(userId: BigInteger) {
        withContext(Dispatchers.IO) {
            // 입금 이벤트
            val depositFlowable: Flowable<TransactionEvent> = lectureSystem.tokenDepositedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ) ?: Flowable.empty()

            Log.d(TAG, "getTransactionHistory: 입금 이벤트!")

            depositFlowable.subscribe(
                { event: TransactionEvent ->
                    if (event.userId == userId) {
                        Log.d(TAG, "Deposit: Amount=${event.amount}, Type=${event.activityType}")
                    }
                },
                { error ->
                    Log.e(TAG, "Error fetching deposits", error)
                },
                {
                    Log.d(TAG, "Deposit events fetched.")
                }
            )

            // 출금 이벤트
            val withdrawalFlowable: Flowable<TransactionEvent> = lectureSystem.tokenWithdrawnEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ) ?: Flowable.empty()
            Log.d(TAG, "getTransactionHistory: 출금 이벤트!")

            withdrawalFlowable.subscribe(
                { event: TransactionEvent ->
                    if (event.userId == userId) {
                        Log.d(TAG, "Withdraw: Amount=${event.amount}, Type=${event.activityType}")
                    }
                },
                { error ->
                    Log.e(TAG, "Error fetching withdrawals", error)
                },
                {
                    Log.d(TAG, "Withdrawal events fetched.")
                }
            )

            // 강의 구매 이벤트
            val purchaseFlowable: Flowable<LecturePurchaseEvent> = lectureSystem.lecturePurchasedEventFlowable(
                DefaultBlockParameterName.EARLIEST,
                DefaultBlockParameterName.LATEST
            ) ?: Flowable.empty()

            Log.d(TAG, "getTransactionHistory: 강의 구매 이벤트!")

            purchaseFlowable.subscribe(
                { event: LecturePurchaseEvent ->
                    if (event.userId == userId) {
                        Log.d(TAG, "Purchase: Amount=${event.amount}, Lecture=${event.lectureTitle}")
                    }
                },
                { error ->
                    Log.e(TAG, "Error fetching purchases", error)
                },
                {
                    Log.d(TAG, "Purchase events fetched.")
                }
            )
        }
    }
}

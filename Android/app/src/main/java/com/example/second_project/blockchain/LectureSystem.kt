import io.reactivex.rxjava3.core.Flowable
import android.util.Log
import org.web3j.abi.EventEncoder
import org.web3j.abi.datatypes.Event
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.methods.request.EthFilter
import org.web3j.tx.Contract
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger
import org.web3j.abi.TypeReference

private const val TAG = "LectureSystem_야옹"

data class TransactionEvent(
    val userId: BigInteger,
    val amount: BigInteger,
    val activityType: String
)

data class LecturePurchaseEvent(
    val userId: BigInteger,
    val amount: BigInteger,
    val lectureTitle: String
)

class LectureSystem(
    contractAddress: String,
    web3j: Web3j,
    credentials: Credentials,
    gasProvider: ContractGasProvider
) : Contract("", contractAddress, web3j, credentials, gasProvider) {

    companion object {
        fun load(
            contractAddress: String,
            web3j: Web3j,
            credentials: Credentials,
            gasProvider: ContractGasProvider
        ): LectureSystem {
            return LectureSystem(contractAddress, web3j, credentials, gasProvider)
        }
    }

    private val events = mapOf(
        "TokenDeposited" to Event(
            "TokenDeposited",
            listOf(
                TypeReference.create(Uint256::class.java, true),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Utf8String::class.java)
            )
        ),
        "TokenWithdrawn" to Event(
            "TokenWithdrawn",
            listOf(
                TypeReference.create(Uint256::class.java, true),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Utf8String::class.java)
            )
        ),
        "LecturePurchased" to Event(
            "LecturePurchased",
            listOf(
                TypeReference.create(Uint256::class.java, true),
                TypeReference.create(Uint256::class.java),
                TypeReference.create(Utf8String::class.java)
            )
        )
    )

    // 입금 이벤트
    fun tokenDepositedEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<TransactionEvent> {
        val event = events["TokenDeposited"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress).addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val transactionEvent = TransactionEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                activityType = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(TAG, "tokenDepositedEventFlowable: ${transactionEvent.amount}, UserId: ${transactionEvent.userId}, Type: ${transactionEvent.activityType}")
            transactionEvent
        }
    }

    // 출금 이벤트
    fun tokenWithdrawnEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<TransactionEvent> {
        val event = events["TokenWithdrawn"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress).addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val transactionEvent = TransactionEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                activityType = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(TAG, "TokenWithdrawn: ${transactionEvent.amount}, UserId: ${transactionEvent.userId}, Type: ${transactionEvent.activityType}")
            transactionEvent
        }
    }

    // 강의 구매 이벤트
    fun lecturePurchasedEventFlowable(
        fromBlock: DefaultBlockParameter,
        toBlock: DefaultBlockParameter
    ): Flowable<LecturePurchaseEvent> {
        val event = events["LecturePurchased"]!!
        return Flowable.fromPublisher(
            web3j.ethLogFlowable(
                EthFilter(fromBlock, toBlock, contractAddress).addSingleTopic(EventEncoder.encode(event))
            )
        ).map { log ->
            val eventValues = Contract.staticExtractEventParameters(event, log)
            val lecturePurchaseEvent = LecturePurchaseEvent(
                userId = eventValues.indexedValues[0].value as BigInteger,
                amount = eventValues.nonIndexedValues[0].value as BigInteger,
                lectureTitle = eventValues.nonIndexedValues[1].value as String
            )
            Log.d(TAG, "LecturePurchased: ${lecturePurchaseEvent.amount}, UserId: ${lecturePurchaseEvent.userId}, Title: ${lecturePurchaseEvent.lectureTitle}")
            lecturePurchaseEvent
        }
    }
}
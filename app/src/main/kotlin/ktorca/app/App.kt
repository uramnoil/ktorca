/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package ktorca.app

import ktorca.*

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.*
import ktorca.CompletionHandler
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.CoroutineContext

fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

/**
 * ルーティング
 */
fun Application.router() {
    routing {
        get("/") {
            completable {
                // Controller・UseCaseInteractor・Presenterを生成
                val presenter   = IncrementCountPresenter(call, it, coroutineContext)
                val usecase     = IncrementCountUseCaseInteractor(presenter, coroutineContext)
                val controller  = HogeController(usecase)

                // Controllerの処理を実行
                controller.doHoge()
            }
        }
    }
}

/**
 * コントローラ
 */
class HogeController(private val input: IncrementCountInputBoundary) {
    fun doHoge() {
        // ユースケースを実行
        // フレームワークから受け取った検証済みの値を即座に渡す
        input.execute()
    }
}

/**
 * I/B
 * UseCaseInteactorに実装する
 */
interface IncrementCountInputBoundary {
    /**
     * UseCaseを実行する
     */
    fun execute()
}

/**
 * O/B
 * Presenterに実装する
 */
interface IncrementCountOutputBoundary {
    /**
     * ユースケースの完了通知を受け取る
     */
    fun onComplete(count: Int)
}

/**
 * ユースケース実装クラス
 */
class IncrementCountUseCaseInteractor(private val output: IncrementCountOutputBoundary, context: CoroutineContext) : IncrementCountInputBoundary, CoroutineScope by CoroutineScope(context) {

    companion object {
        /** アクセスカウンタ */
        val counter = AtomicInteger(0)
    }

    /**
     * ユースケースの中身
     * 今回はアクセスカウンタをインクリメントした結果をO/Bに渡す
     * 途中にdelay関数を使って重い処理を表現している
     */
    override fun execute() {
        launch {
            val counter = counter.getAndIncrement()

            // 重い処理
            delay(1_000)

            // 完了通知
            output.onComplete(counter)
        }
    }
}

/**
 * Presenter
 */
class IncrementCountPresenter(private val call: ApplicationCall, private val onComplete: CompletionHandler, context: CoroutineContext) : IncrementCountOutputBoundary, CoroutineScope by CoroutineScope(context) {
    /**
     * 現在のアクセスカウントを返却する
     */
    override fun onComplete(count: Int) {
        launch {
            call.respondText { "count: $count" }

            // 完了通知
            onComplete()
        }
    }
}
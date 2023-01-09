package com.zheltoukhov.xres.server

import com.zheltoukhov.xres.server.transaction.TransactionManager
import jetbrains.exodus.entitystore.PersistentEntityStores

const val PORT_PARAM = "XRES_PORT"
const val DIR_PARAM = "XRES_DIR"

const val DEFAULT_STORE_DIR = ".store-file"

fun main(args: Array<String>) {
    val appArgs = getAppConfig()
    val store = PersistentEntityStores.newInstance(appArgs.storeDir)
    val transactionFactory = TransactionManager(store)
    val commandHandler = CommandHandler(transactionFactory)
    KtorSocketServer(appArgs.port, commandHandler).run()
}

fun getAppConfig() = AppConfig(
    port = System.getenv(PORT_PARAM)?.toIntOrNull(),
    storeDir = System.getenv(DIR_PARAM) ?: DEFAULT_STORE_DIR
)

data class AppConfig(
    val port: Int?,
    val storeDir: String
)
package com.zheltoukhov.xres.server

import com.zheltoukhov.xres.server.transaction.TransactionProvider
import jetbrains.exodus.entitystore.PersistentEntityStores

const val PORT_PARAM = "-p"
const val FILE_PARAM = "-f"

const val DEFAULT_STORE_DIR = ".store-file"

fun main(args: Array<String>) {
    val appArgs = parseArgs(args)
    val store = PersistentEntityStores.newInstance(appArgs.storeDir)
    val transactionFactory = TransactionProvider(store)
    val commandHandler = CommandHandler(transactionFactory)
    KtorSocketServer(appArgs.port, commandHandler).run()
}

fun parseArgs(args: Array<String>): AppArguments {
    val argsMap = mutableMapOf<String, String>()
    val iterator = args.iterator()
    while (iterator.hasNext()) {
        val key = iterator.next()
        if (iterator.hasNext()) {
            argsMap[key] = iterator.next()
        }
    }

    return AppArguments(
        port = argsMap[PORT_PARAM]?.toIntOrNull(),
        storeDir = argsMap[FILE_PARAM] ?: DEFAULT_STORE_DIR
    )
}

data class AppArguments(
    val port: Int?,
    val storeDir: String
)
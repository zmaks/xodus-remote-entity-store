package com.zheltoukhov.xres.server

const val PORT_PARAM = "-p"

fun main(args: Array<String>) {
    val appArgs = parseArgs(args)
    KtorSocketServer(appArgs.port).run()
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
        port = argsMap[PORT_PARAM]?.toIntOrNull()
    )
}

data class AppArguments(
    val port: Int?
)
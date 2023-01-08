package com.zheltoukhov.xres.protocol.command

class Commands(
    val beginTx: BeginTxCommand = BeginTxCommand(),
    val commit: CommitCommand = CommitCommand(),
    val flush: FlushCommand = FlushCommand(),
    val abort: AbortCommand = AbortCommand(),
    val create: CreateCommand = CreateCommand(),
    val update: UpdateCommand = UpdateCommand(),
    val delete: DeleteCommand = DeleteCommand(),
    val get: GetCommand = GetCommand(),
    val find: FindCommand = FindCommand()
)
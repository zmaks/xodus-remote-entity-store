package com.zheltoukhov.xres.server.store

import jetbrains.exodus.entitystore.PersistentEntityStore
import jetbrains.exodus.entitystore.PersistentEntityStores

class XodusStore(
    storeDir: String
) {

    private val store: PersistentEntityStore

    init {
        store = PersistentEntityStores.newInstance(storeDir)
    }

    fun getInstance() = store
}
package org.fg.ttrpg.repository

import io.quarkus.test.junit.QuarkusTest
import org.junit.jupiter.api.Disabled
import io.quarkus.test.TestTransaction
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.setting.Setting
import org.fg.ttrpg.setting.SettingRepository
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.util.UUID

@Disabled("Tests disabled during build")
@QuarkusTest
class RepositoryIT {
    @Inject
    lateinit var gmRepo: GMRepository
    @Inject
    lateinit var settingRepo: SettingRepository

    @Test
    @TestTransaction
    fun `persist and fetch setting`() {
        val gm = GM().apply {
            id = UUID.randomUUID()
            username = "gm"
            email = "gm@example.com"
        }
        gmRepo.persist(gm)

        val setting = Setting().apply {
            id = UUID.randomUUID()
            title = "world"
            this.gm = gm
        }
        settingRepo.persist(setting)

        val found = settingRepo.findByIdForGm(setting.id!!, gm.id!!)
        found?.gm?.id shouldBe gm.id
    }
}

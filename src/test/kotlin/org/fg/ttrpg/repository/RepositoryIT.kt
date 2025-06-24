package org.fg.ttrpg.repository

import io.kotest.matchers.shouldBe
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import org.fg.ttrpg.account.GM
import org.fg.ttrpg.account.GMRepository
import org.fg.ttrpg.genre.Genre
import org.fg.ttrpg.genre.GenreRepository
import org.fg.ttrpg.setting.*
import org.jdbi.v3.core.Jdbi
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

@QuarkusTest
class RepositoryIT {
    @Inject
    lateinit var gmRepo: GMRepository

    @Inject
    lateinit var settingRepo: SettingRepository

    @Inject
    lateinit var objectRepo: SettingObjectRepository

    @Inject
    lateinit var templateRepo: TemplateRepository

    @Inject
    lateinit var genreRepo: GenreRepository

    @Inject
    lateinit var jdbi: Jdbi

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

    @Test
    @TestTransaction
    fun `persist and fetch gm`() {
        val gm = GM().apply {
            id = UUID.randomUUID()
            username = "gm2"
            email = "gm2@example.com"
        }
        gmRepo.persist(gm)

        val found = gmRepo.findById(gm.id!!)
        found?.username shouldBe "gm2"
    }

    @Test
    @TestTransaction
    fun `persist and fetch template`() {
        val gm = GM().apply {
            id = UUID.randomUUID()
            username = "gm3"
            email = "gm3@example.com"
        }
        gmRepo.persist(gm)

        val setting = Setting().apply {
            id = UUID.randomUUID()
            title = "world"
            this.gm = gm
        }
        settingRepo.persist(setting)

        val genre = Genre().apply {
            id = UUID.randomUUID()
            title = "genre"
            code = "code-${id.toString().substring(0,8)}"
            this.setting = setting
        }
        genreRepo.insert(genre)

        val template = Template().apply {
            id = UUID.randomUUID()
            title = "tpl"
            type = "npc"
            jsonSchema = "{}"
            this.gm = gm
            this.genre = genre
        }
        templateRepo.persist(template)

        val found = templateRepo.findByIdForGm(template.id!!, gm.id!!)
        found?.genre?.id shouldBe genre.id
    }

    @Test
    @TestTransaction
    fun `persist and fetch setting object`() {
        val gm = GM().apply {
            id = UUID.randomUUID()
            username = "gm4"
            email = "gm4@example.com"
        }
        gmRepo.persist(gm)

        val setting = Setting().apply {
            id = UUID.randomUUID()
            title = "world"
            this.gm = gm
        }
        settingRepo.persist(setting)

        val genre = Genre().apply {
            id = UUID.randomUUID()
            title = "genre"
            code = "code-${id.toString().substring(0,8)}"
            this.setting = setting
        }
        genreRepo.insert(genre)

        val template = Template().apply {
            id = UUID.randomUUID()
            title = "tpl"
            type = "npc"
            jsonSchema = "{}"
            this.gm = gm
            this.genre = genre
        }
        templateRepo.persist(template)

        val obj = SettingObject().apply {
            id = UUID.randomUUID()
            slug = "slug"
            title = "obj"
            payload = "{}"
            createdAt = Instant.now()
            this.setting = setting
            this.template = template
            this.gm = gm
        }
        objectRepo.persist(obj)

        val found = objectRepo.findByIdForGm(obj.id!!, gm.id!!)
        found?.slug shouldBe "slug"
    }

    @Test
    @TestTransaction
    fun `insert genre`() {
        val gm = GM().apply {
            id = UUID.randomUUID()
            username = "gm5"
            email = "gm5@example.com"
        }
        gmRepo.persist(gm)

        val setting = Setting().apply {
            id = UUID.randomUUID()
            title = "world"
            this.gm = gm
        }
        settingRepo.persist(setting)

        val genre = Genre().apply {
            id = UUID.randomUUID()
            title = "genre"
            code = "code-${id.toString().substring(0,8)}"
            this.setting = setting
        }
        genreRepo.insert(genre)

        val count = jdbi.withHandle<Int, Exception> { handle ->
            handle.createQuery("SELECT count(*) FROM genre WHERE id = :id")
                .bind("id", genre.id)
                .mapTo(Int::class.java)
                .one()
        }
        count shouldBe 1
    }
}

package org.fg.ttrpg.timeline

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.quarkus.test.TestTransaction
import io.quarkus.test.junit.QuarkusTest
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import org.fg.ttrpg.calendar.CalendarSystem
import org.fg.ttrpg.calendar.CalendarSystemRepository
import org.fg.ttrpg.setting.*
import org.fg.ttrpg.testutils.IntegrationTestHelper
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.*

@QuarkusTest
class TimelineObjectUsageRepositoryIT : IntegrationTestHelper() {

    @Inject
    lateinit var objectRepo: SettingObjectRepository

    @Inject
    lateinit var calendarRepo: CalendarSystemRepository

    @Inject
    lateinit var eventRepo: TimelineEventRepository

    @Inject
    lateinit var usageRepo: TimelineObjectUsageRepository

    @Transactional
    fun createSettingObject(id: UUID, setting: Setting, template: Template, gmId: UUID): SettingObject {
        val gm = gmRepo.findById(gmId)
        return SettingObject().apply {
            this.id = id
            slug = "slug-$id"
            title = "obj"
            payload = "{}"
            createdAt = Instant.now()
            this.setting = setting
            this.template = template
            this.gm = gm
        }.also { objectRepo.persist(it) }
    }

    @Test
    @TestTransaction
    fun `usage view returns expected rows`() {
        val gm = createGm(UUID.randomUUID())
        val setting = createSetting(UUID.randomUUID(), gm.id!!)
        val template = createTemplate(UUID.randomUUID(), gm.id!!, "{}")
        val obj1 = createSettingObject(UUID.randomUUID(), setting, template, gm.id!!)
        val obj2 = createSettingObject(UUID.randomUUID(), setting, template, gm.id!!)

        val cal = CalendarSystem().apply {
            id = UUID.randomUUID()
            name = "cal"
            epochLabel = "CE"
            months = "[]"
            this.setting = setting
        }
        calendarRepo.persist(cal)

        val event = TimelineEvent().apply {
            id = UUID.randomUUID()
            title = "evt"
            startDay = 1
            calendar = cal
            objectRefs = mutableListOf(obj1, obj2)
        }
        eventRepo.persist(event)

        val objects = usageRepo.listObjectsForEvent(event.id!!)
        objects.map { it.settingObjectId } shouldContainExactlyInAnyOrder listOf(obj1.id, obj2.id)

        val events = usageRepo.listEventsForObject(obj1.id!!)
        events.map { it.eventId } shouldBe listOf(event.id)
    }
}

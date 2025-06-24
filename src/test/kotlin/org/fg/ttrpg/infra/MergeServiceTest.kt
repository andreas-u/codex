package org.fg.ttrpg.infra

import com.fasterxml.jackson.databind.ObjectMapper
import org.fg.ttrpg.infra.merge.MergeService
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Disabled


class MergeServiceTest {
    private val mapper = ObjectMapper()
    private val service = MergeService()

    @Test
    fun `merge patch replaces and removes`() {
        val original = """{"name":"old","extra":"keep"}"""
        val patch = """{"name":"new","extra":null}"""
        val result = mapper.readTree(service.merge(original, patch))
        result.get("name").asText() shouldBe "new"
        // removed field should be null
        result.get("extra").shouldBeNull()
    }

    @Test
    fun `merge patch merges nested objects`() {
        val original = mapper.readTree("""{"obj":{"a":1,"b":2},"c":3}""")
        val patch = mapper.readTree("""{"obj":{"b":null,"c":4}}""")
        val result = service.merge(original, patch)

        val obj = result.get("obj")
        obj.get("a").asInt() shouldBe 1
        // b should be removed
        obj.get("b").shouldBeNull()
        obj.get("c").asInt() shouldBe 4
        result.get("c").asInt() shouldBe 3
    }
}

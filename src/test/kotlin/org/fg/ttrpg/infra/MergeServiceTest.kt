package org.fg.ttrpg.infra

import com.fasterxml.jackson.databind.ObjectMapper
import org.fg.ttrpg.infra.merge.MergeService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MergeServiceTest {
    private val mapper = ObjectMapper()
    private val service = MergeService()

    @Test
    fun `merge patch replaces and removes`() {
        val original = """{"name":"old","extra":"keep"}"""
        val patch = """{"name":"new","extra":null}"""
        val result = mapper.readTree(service.merge(original, patch))
        assertEquals("new", result.get("name").asText())
        // removed field should be null
        assertEquals(null, result.get("extra"))
    }

    @Test
    fun `merge patch merges nested objects`() {
        val original = mapper.readTree("""{"obj":{"a":1,"b":2},"c":3}""")
        val patch = mapper.readTree("""{"obj":{"b":null,"c":4}}""")
        val result = service.merge(original, patch)

        val obj = result.get("obj")
        assertEquals(1, obj.get("a").asInt())
        // b should be removed
        assertEquals(null, obj.get("b"))
        assertEquals(4, obj.get("c").asInt())
        assertEquals(3, result.get("c").asInt())
    }
}

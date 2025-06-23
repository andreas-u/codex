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
}
